package exocr.idcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ucredit.dream.R;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.ImageHelper;
import com.ucredit.dream.utils.Utils;

import exocr.exocrengine.EXIDCardResult;
import exocr.exocrengine.EXOCREngine;

public class CaptureActivity extends Activity implements Callback {
	public static final String EXTRA_SCAN_RESULT = "exocr.idcard.scanResult";
	private static final String TAG = CaptureActivity.class.getSimpleName();
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private int time;
	private byte dbpath[];
	private Bitmap logo;
	private boolean bLight;
	
	//save last time recognize result
	private EXIDCardResult cardlast = null;
	//current time recognition result
	private EXIDCardResult cardRest = null;
	
	private static int uniqueOMatic = 10;
	private static final int REQUEST_DATA_ENTRY = uniqueOMatic++;

	public static Bitmap IDCardFrontFullImage = null;
	public static Bitmap IDCardBackFullImage = null;
	
	private final ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
        }
    };
    
	//===========指定识别分类器模型库文件加载到SD卡中的保存位置及文件名==========================================
	//注意：受动态库限制，路径及文件名不可更改
    private final String RESOURCEFILEPATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()+"/exidcard/";   //模型库路径
    private final String DICTPATH = "/data/data/com.exidcard";
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//CameraManager
		CameraManager.init(getApplication());
		try {
            hardwareSupportCheck();
        } catch (Exception e) {
            Utils.MakeToast(this, "相机打开失败，请确定是否开启权限。");
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		// FLAG_TRANSLUCENT_NAVIGATION
		if(Build.VERSION.SDK_INT >= 19){
			getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		setContentView(R.layout.main);

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		txtResult = (TextView) findViewById(R.id.txtResult);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		time = 0;
		logo = BitmapFactory.decodeResource(this.getResources(), R.drawable.yidaoboshi96);
		viewfinderView.setLogo(logo);
		if (getIntent().getStringExtra("type").equals("back")) {
			viewfinderView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idcard_back_mask));
		} else {
			viewfinderView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.idcard_front_mask));
		}
		bLight = false;
				
	    String path = this.getApplicationContext().getFilesDir().getAbsolutePath();
		//InitDict(RESOURCEFILEPATH);
	    InitDict(path);
	}
	public static boolean hardwareSupportCheck() {
	      // Camera needs to open
	        Camera c = null;
	        try {
	            c = Camera.open();
	        } catch (RuntimeException e) {
	            throw new RuntimeException();
	        }
	        if (c == null) {
	            return false;
	        } else {
	            c.release();
	        }
	        return true;
	    }

	public boolean InitDict(String dictpath)
	{
		dbpath = new byte[256];
		//if the dict not exist, copy from the assets
		if(CheckExist(dictpath+"/zocr0.lib") == false ){
			File destDir = new File(dictpath);
			if (!destDir.exists()) { destDir.mkdirs(); }
			  
			boolean a = copyFile("zocr0.lib", dictpath+"/zocr0.lib");
			if (a == false){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("exidcard dict Copy ERROR!\n");
				builder.setMessage(dictpath+" can not be found!");
				builder.setCancelable(true);
				builder.create().show();
				return false;
			}
		}
		
		
		String filepath = dictpath;
		
		//string to byte
		for (int i = 0; i < filepath.length(); i++)
			dbpath[i] = (byte)filepath.charAt(i);
		dbpath[filepath.length()] = 0;
		
		int nres = EXOCREngine.nativeInit(dbpath);
		
		if (nres < 0){
			Log.d("ExTranslator.nativeExInit", "Init Error = "+nres);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("exidcard dict Init ERROR!\n");
			builder.setMessage(filepath+" can not be found!");
			builder.setCancelable(true);
			builder.create().show();
			return false;
		}else{
			//just test
			//ExTranslator.nativeExTran(imgdata, width, height, pixebyte, pitch, flft, ftop, frgt, fbtm, result, maxsize)
		}
		//sign ocr sdk
		EXOCREngine.nativeCheckSignature(this.getApplicationContext());
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.IDpreview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {		
		inactivityTimer.shutdown();
		EXOCREngine.nativeDone();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
	//show the decode result
	public void handleDecode(EXIDCardResult result) {
		//inactivityTimer.onActivity();
		if(result == null)
		{
			return;
		}
//		playBeepSoundAndVibrate();
		
		//txtResult.setText(obj.getBarcodeFormat().toString() + ":"+ obj.getText());
		//txtResult.setText("decode txt:\n"+ obj.getText());
		/*
		//show the result
		if (result == null){
			txtResult.setText("");
			//imgView.setImageBitmap(null);

		}else{
			
			txtResult.setText(result.getText()+"\n"+time++);
			//imgView.setImageBitmap(result.GetFaceBitmap());
		}
		*/
//		Intent intent = new Intent(CaptureActivity.this, IDCardEditActivity.class);
//		if (result != null) {
//			intent.putExtra(EXTRA_SCAN_RESULT, result);
//			result = null;
//		}
//		intent.putExtras(getIntent()); // passing on any received params (such as isCvv and language)
//		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
//				| Intent.FLAG_ACTIVITY_NO_HISTORY
//				| Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		Log.i("DEBUG_TIME", "CardRecoActivity_nextActivity2="+System.currentTimeMillis());
//		startActivityForResult(intent, REQUEST_DATA_ENTRY);
		Intent intent = new Intent();
		intent.putExtras(getIntent());
		String fileName = "";
        if (result != null) {
            if (result.type == 1) {
                if (IDCardFrontFullImage != null && !IDCardFrontFullImage.isRecycled()) {
                    IDCardFrontFullImage.recycle();
                }
                fileName = FileUtil.FILE_NAME_UPLOAD_RENXIANG_COMPRESS;//身份证正面
            } else {
                if (IDCardBackFullImage != null && !IDCardBackFullImage.isRecycled()) {
                    IDCardBackFullImage.recycle();
                }
                fileName = FileUtil.FILE_NAME_UPLOAD_GUOHUI_COMPRESS;//身份证反面
            }
            ImageHelper.saveCompressBitmap(
                result.stdCardIm,new File(FileUtil.getUcreditDir(CaptureActivity.this), fileName));
            intent.putExtra("nColorType", result.nColorType);
            intent.putExtra(EXTRA_SCAN_RESULT, result);
            result = null;
        }
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	@Override
	public boolean onTouchEvent (MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		Point res = CameraManager.get().getResolution();
		
		if (event.getAction() == MotionEvent.ACTION_UP){
			if(x > res.x*8/10 && y < res.y/4) { return false; }
			
			handleDecode(null);
			cardlast = null;
			//点击重新聚焦
			handler.restartAutoFocus();
			return true;
		}
		return false;
	}
	
	public boolean copyFile(String from, String to) {
		// 例：from:890.salid;
		// to:/mnt/sdcard/to/890.salid
		boolean ret = false;
		try {
			int bytesum = 0;
			int byteread = 0;

			InputStream inStream = getResources().getAssets().open(from);// 将assets中的内容以流的形式展示出来
			File file = new File(to);
			OutputStream fs = new FileOutputStream(file);// to为要写入sdcard中的文件名称
			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			fs.close();
			ret = true;

		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}
	
	//check one file 
	public boolean CheckExist(String filepath)
	{
		int i;
		File file = new File (filepath);
		if (file.exists()){
			return true;
		}
		return false;
	}
	
	public void SetRecoResult(EXIDCardResult result)
	{
		cardRest = result;
	}
	
	//check is equal()
	public boolean CheckIsEqual(EXIDCardResult cardcur){
		if (cardlast == null){
			cardlast = cardcur;
			return false;
		}else{
			if (cardlast.name.equals(cardcur.name) &&
			    cardlast.sex.equals(cardcur.sex) &&
			    cardlast.nation.equals(cardcur.nation) &&
			    cardlast.cardnum.equals(cardcur.cardnum) &&
			    cardlast.address.equals(cardcur.address)){
				return true;
			}else{
				cardlast = cardcur; cardcur = null;
				return false;
			}
		}
	}
	
	public ShutterCallback getShutterCallback(){
		return shutterCallback;
	}
	public void OnFlashBtnClick(View view){
		if(bLight){
			CameraManager.get().disableFlashlight();
			bLight = false;
		}else{
			CameraManager.get().enableFlashlight();
			bLight = true;
		}
	}
	//////////////////////////////////////////////////////////////////////////
	public void OnShotBtnClick(View view) {
		//Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show();
		handleDecode(null);
		//playBeepSoundAndVibrate();
		handler.takePicture();
	}	
}