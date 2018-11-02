package exocr.bankcard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.ImageHelper;

/* CardScanner.java
 * See the file "LICENSE.md" for the full license governing this code.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowManager;

/**
 * Encapsulates the core image scanning.
 * 
 * As of 7/20/12, the flow should be:
 * 
 * 1. CardIOActivity sets up the CardScanner, Preview and Overlay. 
 * 2. As each frame is received & processed by the scanner, the scanner notifies the activity of any relevant changes. (e.g. edges
 *    detected, scan complete etc.) 
 * 3. CardIOActivity passes on the information to the preview and overlay, which can then update themselves as needed. 
 * 4. Once a result is reported, CardIOActivty closes the scanner and launches the next activity.
 * 
 * HOWEVER, at the moment, the CardScanner is directly communicating with the Preview.
 * 
 */
class CardScanner implements Camera.PreviewCallback, Camera.AutoFocusCallback, SurfaceHolder.Callback {
    private static final String TAG = CardScanner.class.getSimpleName();

    private static final float MIN_FOCUS_SCORE = 3.5f; 
    private static final long AUTOFOCUS_TIMEOUT 				= 1000;
    private static final long MINIMUM_TIME_BETWEEN_DETECTIONS 	= 3000;
    private static final int CAMERA_CONNECT_TIMEOUT 			= 5000;
    private static final int CAMERA_CONNECT_RETRY_INTERVAL 		= 50;
    ////////////////////////////////////////////////////////////////////////
    private Bitmap cardBitmap;
    // member data
    protected WeakReference<CardRecoActivity> mScanActivityRef;

    private long mAutoFocusStartedAt = 0;
    private long mAutoFocusCompletedAt = 0;

    private Camera mCamera = null;
    private byte[] mPreviewBuffer;

    // accessed by test harness subclass.
    protected boolean useCamera = true;
    private boolean isSurfaceValid = false;
    private boolean isFirstFrame = true;

    private Point screenResolution;
    private Point cameraResolution;
    private int previewFormat;
    private int previewFps;
    // read by CardIOActivity to set up Preveiw
    public int mPreviewWidth;
    public int mPreviewHeight;
    private final Context context;
    List<Size> mSupportedPreviewSizes;
   
    //recognition valuables
    private EXBankCardInfo mCardInfo = null;
	public byte []bResultBuf;
	public int    nResultLen;
	public static final int mMaxStreamBuf = 1024;
	
	//////////////////////////////////////////////
	private boolean flagFocused;
    
	private Handler handler = new Handler();
    private ExecutorService executorService = null;
	
    void createExecutor() {
    	int recognizerNumber = Math.min(3, Runtime.getRuntime().availableProcessors());
		executorService = Executors.newFixedThreadPool(recognizerNumber);
    }

    /** Construction */
    CardScanner(CardRecoActivity scanActivity) {
    	
    	Log.i("DEBUG_TIME", "CardScanner_CardScanner="+System.currentTimeMillis());
    	
        Intent scanIntent = scanActivity.getIntent();
        mScanActivityRef = new WeakReference<CardRecoActivity>(scanActivity);
        cardBitmap = null;
        context = scanActivity.getApplicationContext();
        //screenResolution = new Point(0, 0);
        
        mCardInfo = new EXBankCardInfo(); 
		bResultBuf = new byte[mMaxStreamBuf];
		nResultLen = 0;
		flagFocused = false;
		
		createExecutor();
		bHasResult = false;
		
		EXBankCardReco.nativeCheckSignature(context);
    }
    
    /** Reads, one time, values from the camera that are needed by the app. */
    void initFromCameraParameters(Camera camera) {
      Camera.Parameters parameters = camera.getParameters();
      previewFormat = parameters.getPreviewFormat();
      
	  WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
      Display display = manager.getDefaultDisplay();
      
      //screenResolution = new Point(800, 480);
      //cameraResolution = new Point(800, 480);
      //return;
      //display.getRealSize(screenResolution);
      screenResolution = getRealScreenSize();
      Log.d(TAG, "Screen resolution: " + screenResolution);
      
      //hold previewSizes
      mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
      cameraResolution = getOptimalPreviewSize(mSupportedPreviewSizes, screenResolution.x, screenResolution.y);      
    }
    //get real screen size
	private Point getRealScreenSize() {
		int heightPixels, widthPixels;
		Point screenResolution = null;
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = manager.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;
		heightPixels = metrics.heightPixels;
		widthPixels  = metrics.widthPixels;
		// includes window decorations (statusbar bar/navigation bar)
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
			try {
				heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
				widthPixels  = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
			} catch (Exception ignored) {
			}
		// includes window decorations (statusbar bar/navigation bar)
		else if (Build.VERSION.SDK_INT >= 17)
			try {
				android.graphics.Point realSize = new android.graphics.Point();
				Display.class.getMethod("getRealSize", android.graphics.Point.class).invoke(d, realSize);
				heightPixels = realSize.y;
				widthPixels = realSize.x;
			} catch (Exception ignored) {
				
			}
		//Log.e("realHightPixels-heightPixels", heightPixels + "width");
		screenResolution = new Point(widthPixels, heightPixels); 
		return screenResolution;
	}
	
    /** Get optimal preview size*/
    private Point getOptimalPreviewSize(List<Size> sizes, int w, int h) {    	
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;
        Point cameraResolution = null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        
        cameraResolution = new Point(optimalSize.width, optimalSize.height);  
        //cameraResolution = new Point(640, 480);  
        return cameraResolution;
    }
    
    /**
     * Sets the camera up to take preview images which are used for both preview and decoding.
     * We detect the preview format here so that buildLuminanceSource() can build an appropriate
     * LuminanceSource subclass. In the future we may want to force YUV420SP as it's the smallest,
     * and the planar Y can be used for barcode scanning without a copy in some cases.
     */
    void setDesiredCameraParameters(Camera camera) {
      Camera.Parameters parameters = camera.getParameters();
      Log.d(TAG, "Setting preview size: " + cameraResolution);
      parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);     
      camera.setParameters(parameters);
    }

    /**
     * Connect or reconnect to camera. If fails, sleeps and tries again. Returns <code>true</code> if successful,
     * <code>false</code> if maxTimeout passes.
     */
    private Camera connectToCamera(int checkInterval, int maxTimeout) {
        long start = System.currentTimeMillis();
        if (useCamera) {
            do {
                try { // Camera.open() will open the back-facing camera. Front cameras are not attempted.
                    return Camera.open();
                } catch (RuntimeException e) {
                    try {
                        Log.w(TAG, "Wasn't able to connect to camera service. Waiting and trying again...");
                        Thread.sleep(checkInterval);
                    } catch (InterruptedException e1) {
                        Log.e(TAG, "Interrupted while waiting for camera", e1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected exception. Please report it to support@card.io", e);
                    maxTimeout = 0;
                }
            } while (System.currentTimeMillis() - start < maxTimeout);
        }
        Log.w(TAG, "camera connect timeout");
        return null;
    }

    /** prepare Scanner  */
    void prepareScanner() {
        Log.v(TAG, "prepareScanner()");
        mAutoFocusStartedAt = 0;
        mAutoFocusCompletedAt = 0;
        Log.i("DEBUG_TIME", "CardScanner_prepareScanner1="+System.currentTimeMillis());
        //open camera
        if (useCamera && mCamera == null) {
            mCamera = connectToCamera(CAMERA_CONNECT_RETRY_INTERVAL, CAMERA_CONNECT_TIMEOUT);
            if (mCamera == null) {
                Log.e(TAG, "prepare scanner couldn't connect to camera!");
                return;
            } else {
                Log.v(TAG, "camera is connected");
            }
            Log.i("DEBUG_TIME", "CardScanner_prepareScanner2="+System.currentTimeMillis());
            //init camera parameters
            initFromCameraParameters(mCamera);
            mPreviewWidth = cameraResolution.x;
            mPreviewHeight= cameraResolution.y;
            setDesiredCameraParameters(mCamera);
            
            Log.i("DEBUG_TIME", "CardScanner_prepareScanner3="+System.currentTimeMillis());
        } else if (!useCamera) {
            Log.w(TAG, "useCamera is false!");
        } else if (mCamera != null) {
            Log.v(TAG, "we already have a camera instance: " + mCamera);
        }
    }
    
    
    /** */
    @SuppressWarnings("deprecation")
    boolean resumeScanning(SurfaceHolder holder) {
    	Log.i("DEBUG_TIME", "CardScanner_resumeScanning1="+System.currentTimeMillis());
        Log.v(TAG, "resumeScanning(" + holder + ")");
        if (mCamera == null) {
            Log.v(TAG, "preparing the scanner...");
            prepareScanner();
            Log.v(TAG, "preparations complete");
        }
        if (useCamera && mCamera == null) {
            // prepare failed!
            Log.i(TAG, "null camera. failure");
            return false;
        }
        
        flagFocused = false;
        assert holder != null;
        //set 
        //setDesiredCameraParameters(mCamera);
        Log.i("DEBUG_TIME", "CardScanner_resumeScanning2="+System.currentTimeMillis());

        if (executorService == null) {
        	createExecutor();
        }
        bHasResult = false;

        if (useCamera && mPreviewBuffer == null) {
            int previewFormat = ImageFormat.NV21; // the default.
            Camera.Parameters parameters = mCamera.getParameters();
            previewFormat = parameters.getPreviewFormat();
            int bytesPerPixel = ImageFormat.getBitsPerPixel(previewFormat) / 8; 
            int bufferSize = mPreviewWidth * mPreviewHeight * bytesPerPixel * 3;
            mPreviewBuffer = new byte[bufferSize];
            mCamera.addCallbackBuffer(mPreviewBuffer);
        }

        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (useCamera){
        	mCamera.setPreviewCallbackWithBuffer(this); 
        }        
        if (isSurfaceValid) {
        	makePreviewGo(holder);
        }

        Log.i("DEBUG_TIME", "CardScanner_resumeScanning3="+System.currentTimeMillis());
        // Turn flash off
        //setFlashOn(false);
        Log.i("DEBUG_TIME", "CardScanner_resumeScanning4="+System.currentTimeMillis());
        return true;
    }

    public void pauseScanning() {
        setFlashOn(false);
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                Log.w(TAG, "can't stop preview display", e);
            }
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mPreviewBuffer = null;
            Log.d(TAG, "- released camera");
            mCamera = null;
        }
        
        if (executorService != null && executorService.isShutdown()) {
        	executorService = null;
        }

        Log.i(TAG, "scan paused"); // tests look for this message. don't delete it.
    }

    public void endScanning() {
        if (mCamera != null){
            pauseScanning();
        }
        mPreviewBuffer = null;
    }

    /////////////////////////////////////////////////////////////////////////////////
    /** SurfaceHolder callbacks   */
    private boolean makePreviewGo(SurfaceHolder holder) {
        // method name from http://www.youtube.com/watch?v=-WmGvYDLsj4
        assert holder != null;
        assert holder.getSurface() != null;
        Log.d(TAG, "surfaceFrame: " + String.valueOf(holder.getSurfaceFrame()));

        if (useCamera) {
            try {
            	//connect preview to the camera!!!
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, "can't set preview display", e);
                return false;
            }
            try {
                mCamera.startPreview();
                mCamera.autoFocus(this);
                Log.d(TAG, "startPreview success");
            } catch (RuntimeException e) {
                Log.e(TAG, "startPreview failed on camera. Error: ", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where  to draw.
        Log.d(TAG, "Preview.surfaceCreated()");

        if (mCamera != null || !useCamera) {
            isSurfaceValid = true;
            makePreviewGo(holder);
        } else {
            Log.wtf(TAG, "CardScanner.surfaceCreated() - camera is null!");
            return;
        }
        Log.d(TAG, "Preview.surfaceCreated(), surface is valid");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d(TAG, String.format("Preview.surfaceChanged(holder?:%b, f:%d, w:%d, h:%d )", (holder != null), format, width, height));
        
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Preview.surfaceDestroyed()");
        if (mCamera != null)
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
            } catch (Exception e) {
                Log.e(TAG, "error stopping camera", e);
            }
        isSurfaceValid = false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles processing of each frame.
     * 
     * This method is called by Android, never directly by application code.
     */
    private static boolean processingInProgress = false;
    private static boolean bHasResult = false;

	class recoThread implements Runnable {
		private CardScanner scanner;
		private byte[] data;

		recoThread(CardScanner scanner, byte[] data) {
			this.scanner = scanner;
			this.data = data;
		}

		@Override
		public void run() {
			synchronized (this) {
				processingInProgress = true;
				Rect guideRect = getGuideFrame();
				EXBankCardInfo cardInfo = new EXBankCardInfo();
				cardInfo.charCount = 0;
				cardInfo.focusScore = 0;
				// Log.i("DEBUG_TIME",
				// "CardScanner_onPreviewFrame1="+System.currentTimeMillis());
				///////////////////////////////////////////////////////////////////////////////////////////////////////
				cardInfo.focusScore = EXBankCardReco.nativeFocusScore(data, mPreviewWidth, mPreviewHeight,
						previewFormat, guideRect.left, guideRect.top, guideRect.right, guideRect.bottom);
				boolean sufficientFocus = (cardInfo.focusScore >= MIN_FOCUS_SCORE);
				if (!sufficientFocus) {
					triggerAutoFocus(false);
				} else {
					boolean bSucceed = false;
					// recognize it
					// mCardInfo.timestart = System.currentTimeMillis();
					// ==call jni
					nResultLen = EXBankCardReco.nativeRecoRawdat(data, mPreviewWidth, mPreviewHeight, previewFormat,
							guideRect.left, guideRect.top, guideRect.right, guideRect.bottom, bResultBuf,
							bResultBuf.length);

					if (nResultLen > 0) {
						bSucceed = EXBankCardReco.DecodeResult(bResultBuf, nResultLen, cardInfo);
						if (bSucceed) {
							// NOTE: 濡傛灉闇�淇濆瓨鍥惧儚锛岃鎮ㄦ墦寮�苟淇濆瓨鍒版寚瀹氱洰褰�							// savetoJPEG(data, mPreviewWidth, mPreviewHeight);
							bSucceed = EXBankCardReco.corpCardNumImage(data, mPreviewWidth, mPreviewHeight,
									previewFormat, guideRect, cardInfo);
						}
					}

					if (bSucceed) {
						bHasResult = true;
						mCardInfo = cardInfo;
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Log.i(TAG, mCardInfo.strNumbers + mCardInfo.strBankName);
								savetoJPEG(data, mPreviewWidth, mPreviewHeight);
								mScanActivityRef.get().onCardDetected(mCardInfo);
							}
						});
					}
				}
				processingInProgress = false;
			}
		}
	}
    
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    	Rect guideRect = null;
        if (data == null) { Log.w(TAG, "frame is null! skipping"); return; }
 
        if (processingInProgress) {
            Log.i(TAG, "processing in progress.... dropping frame");
            // return frame buffer to pool
            if (camera != null) camera.addCallbackBuffer(data);
            return;
        }      
        //first time to invalidateOverlapView, and return for update the overlap view;
        if(isFirstFrame){
        	mScanActivityRef.get().InvalidateOverlapView();
        	isFirstFrame = false;
        	if (camera != null) camera.addCallbackBuffer(data);
        	return;
        }
        //add bomber if it not focus, drop the frame, 鍑忚交璇嗗埆鍘嬪姏
        if(!flagFocused){
            Log.i(TAG, "processing in progress.... dropping frame");
            // return frame buffer to pool
            if (camera != null) camera.addCallbackBuffer(data);
            triggerAutoFocus(false);
            return;
        }
        
        if (bHasResult && executorService != null && !executorService.isShutdown()) {
        	executorService.shutdown();
        }
        
        if (executorService != null && !executorService.isShutdown()) {
        	executorService.execute(new recoThread(this, data));
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // give the image buffer back to the camera, AFTER we're done reading
        // the image.
        if (camera != null){ 
        	camera.addCallbackBuffer(data);
        }
    }
    
    //make the guide rect by the previewidth preview height
    Rect getGuideFrame( int previewWidth, int previewHeight) {
        Rect r = new Rect();
        
        int cardh, cardw;
        int lft, top, rgt, btm;
        
        cardw = previewWidth*70/100;
        if(cardw < 720) cardw = 720;
        if(previewWidth < cardw) cardw = previewWidth;
        
        cardh = (int)(cardw * 0.63084f);

        lft = (previewWidth-cardw)/2;
        top = (previewHeight-cardh)/2;
        rgt = lft + cardw;
        btm = top + cardh;        
        r = new Rect(lft, top, rgt, btm);   
        
        return r;
    }

    Rect getGuideFrame() {
        return getGuideFrame(mPreviewWidth, mPreviewHeight);
    }
    // ------------------------------------------------------------------------
    // CAMERA CONTROL & CALLBACKS
    // ------------------------------------------------------------------------

    /**
     * Invoked when autoFocus is complete
     * 
     * This method is called by Android, never directly by application code.
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        mAutoFocusCompletedAt = System.currentTimeMillis();
        flagFocused = success;
        if(success){
        	Log.d(TAG, "onAutoFocus success@@@@@@@@@@@@@@@@@@@@@@@@@");
        }else{
        	Log.d(TAG, "onAutoFocus failed###########################");
        }
    }

    /**
     * True if autoFocus is in progress
     */
    boolean isAutoFocusing() {
        return mAutoFocusCompletedAt < mAutoFocusStartedAt;
    }

    void toggleFlash() {
        Log.d(TAG, "toggleFlash: currently " + (isFlashOn() ? "ON" : "OFF"));
        setFlashOn(!isFlashOn());
        Log.d(TAG, "toggleFlash - now " + (isFlashOn() ? "ON" : "OFF"));
    }

    // ------------------------------------------------------------------------
    // MISC CAMERA CONTROL
    // ------------------------------------------------------------------------
    void triggerAutoFocus(boolean isManual) {
        if (useCamera && !isAutoFocusing())
            try {
                mAutoFocusStartedAt = System.currentTimeMillis();
                mCamera.autoFocus(this);
            } catch (RuntimeException e) {
                Log.w(TAG, "could not trigger auto focus: " + e);
            }
    }

    public boolean isFlashOn() {
        if (!useCamera)
            return false;
        Camera.Parameters params = mCamera.getParameters();
        return params.getFlashMode().equals(Parameters.FLASH_MODE_TORCH);
    }

    public boolean setFlashOn(boolean b) {
        if (mCamera != null) {
            Log.d(TAG, "setFlashOn: " + b);
            try {
                Camera.Parameters params = mCamera.getParameters();
                params.setFlashMode(b ? Parameters.FLASH_MODE_TORCH : Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                return true;
            } catch (RuntimeException e) {
                Log.w(TAG, "Could not set flash mode: " + e);
            }
        }
        return false;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
	//preview image to bitmap
	public static Bitmap convertToBitmap(byte []data, int width, int height, int format, Rect rect){
		Bitmap bitmap = null;		
		if(format == ImageFormat.NV21 || format == ImageFormat.YUY2){ 
			YuvImage yuv_image = new YuvImage(data, format, width, height, null); 
			// Convert YuV to Jpeg  
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();  
            yuv_image.compressToJpeg(rect, 100, output_stream);  
            // Convert from Jpeg to Bitmap  
            bitmap = BitmapFactory.decodeByteArray(output_stream.toByteArray(), 0, output_stream.size());
		}
		return bitmap;
	}
	
    //get the bitmap int the rect
    public static Bitmap corpBitmap(byte []data, int width, int height, int format, Rect rect){
		int w = rect.width();
		int h = rect.height();

		if(format == ImageFormat.NV21 || format == ImageFormat.YUY2){ 
			int frameSize = width*height;
			int []pixels = new int [w*h];
			byte []yuv = data;
			int yOffset = rect.top*width + rect.left;
			int uvOffset = (rect.top/2)*width + (rect.left/2)*2 + frameSize;
			int y, u, v, k;
			
			for(int i = 0; i < h; ++i){
				int outputOffset = i*w;
				for(int j = 0; j < w; ++j){
					y = (0xff & yuv[yOffset+j])-16;
					
					k = ((j>>1)<<1);
					v = (0xff & yuv[uvOffset+k])-128;
					u = (0xff & yuv[uvOffset+k+1])-128;
					
		            int y1192 = 1192 * y;
	                int r = (y1192 + 1634 * v);
	                int g = (y1192 - 833 * v - 400 * u);
	                int b = (y1192 + 2066 * u);

	                if (r < 0) r = 0; else if (r > 262143) r = 262143;
	                if (g < 0) g = 0; else if (g > 262143) g = 262143;
	                if (b < 0) b = 0; else if (b > 262143) b = 262143;
	                //0xargb ??	
	                pixels[outputOffset+j] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
				}
				yOffset += width;
				if(((rect.top+i) & 1) == 1){ uvOffset+= width; }
			}
			Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
			return bitmap;
		}
		return null;
    }
    
	//will implement in C
	private float getFocusScore(byte []data, int width, int height)
	{
		float focusScore = 0;
		int top = height/2-height/4;
		int btm = height/2+height/4;
		int lft = width/2 -width/4;
		int rgt = width/2 +width/4;
		int ii, jj;
		int offset = 0;
		int sum = 0, sum_square = 0, sobel;
		int pixelcount = 0;
		double mean;
		for(ii = top; ii < btm; ++ii){
			offset = ii*width+lft;
			for(jj = lft; jj < rgt; ++jj, ++offset){
				sobel = (data[offset-width-1]&0xff)+(data[offset+width+1]&0xff)-(data[offset-width+1]&0xff)-(data[offset+width-1]&0xff);
				sobel = Math.abs(sobel);
				sum += sobel;
				sum_square += sobel*sobel;
			}
		}
		pixelcount = (rgt-lft)*(btm-top);
		mean = sum*1.0f/pixelcount;
		focusScore = (float)Math.sqrt(sum_square*1.0f/pixelcount-mean*mean);
		
		return focusScore; 
	}
	
	// save to jpeg
	private void savetoJPEG(byte[] data, int width, int height) {
		int w, h;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String date = sDateFormat.format(new java.util.Date());
		
		String tofile = Environment.getExternalStorageDirectory()+File.separator+Environment.DIRECTORY_DCIM+File.separator+date+"_"+".jpg";
		//String tofile = Environment.()+File.separator+Environment.DIRECTORY_DCIM+File.separator+date+"_"+gcount+".jpg";
		//String tofile = "/sdcard/DCIM/"+"NV21_"+ date+"_"+gcount+".jpg";
		Rect frame = getGuideFrame(width, height);

		if (previewFormat == ImageFormat.NV21) {
			YuvImage img = new YuvImage(data, ImageFormat.NV21, width, height, null);
			OutputStream outStream = null;
			File file = new File(tofile);
			try {
				outStream = new FileOutputStream(file);
				img.compressToJpeg(frame, 100, outStream);
				outStream.flush();
				outStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ImageHelper.saveCompressBitmap(
	            ImageHelper.createImage(tofile),
	            new File(FileUtil.getUcreditDir(context), FileUtil.FILE_NAME_UPLOAD_BANKCARD_COMPRESS));
	}
}
