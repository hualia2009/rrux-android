package com.ucredit.dream.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

// 创建本地的图片
public class ImageHelper {

    /**
     * @param filePath
     *        本地保存的文件路径
     * @return 返回压缩过的图片
     */
    public static Bitmap createSmallImage(String filePath) {
        Bitmap bitmap = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 150 * 150);
        opts.inJustDecodeBounds = false;

        try {
            bitmap = BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
        }
        return bitmap;
    }

    /**
     * 保存压缩的图片
     * 
     * @param bitmap
     *        需要保存的图片
     * @param file
     *        图片的保存完整路径
     * @return true 保存成功 ，false 否则保存失败
     */
    public static boolean saveCompressBitmap(File file) {
        FileOutputStream fileOutputStream = null;
        Bitmap bitmap = createImage(file.toString());
        try {
            fileOutputStream = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 90,
                fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 保存压缩的图片
     * 
     * @param bitmap
     *        需要保存的图片
     * @param file
     *        图片的保存完整路径
     * @return true 保存成功 ，false 否则保存失败
     */
    public static boolean saveCompressBitmap(Bitmap bitmap, File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 90,
                fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }
    
    public static Bitmap loadBitmap(String imgpath) {
        Bitmap bm = createImage(imgpath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgpath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            int length=exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH,0);
            int width=exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            // 计算旋转角度 
            if (length>width) {
                Matrix m = new Matrix();
                m.postRotate(90);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(),
                    m, true);
            } 
        }
        return bm;
    }
    
    public static boolean saveOrientationBitmap(Bitmap bitmap, File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 90,
                fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        return false;
    }

    /**
     * 创建800*480分辨率的图片
     * 
     * @param filePath
     * @return
     */
    public static Bitmap createImage(String filePath) {
        Bitmap bitmap = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        opts.inSampleSize = computeSampleSize(opts, -1, 800 * 480);
        opts.inJustDecodeBounds = false;

        try {
            bitmap = BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
        }
        return bitmap;
    }

    public static int computeSampleSize(BitmapFactory.Options options,

    int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
            maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
            .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
            Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.  
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and
     * height.
     * 
     * @param filename
     *        The full path of the file to decode
     * @param reqWidth
     *        The requested width of the resulting bitmap
     * @param reqHeight
     *        The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect
     *         ratio and dimensions that are equal to or greater than the
     *         requested width and height
     */
    public static Bitmap decodeSamllBitmapFromFile(String filename,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions  
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize  
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
            reqHeight);

        // Decode bitmap with inSampleSize set  
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options}
     * object when decoding bitmaps using the decode* methods from
     * {@link BitmapFactory}. This implementation calculates the closest
     * inSampleSize that will result in the final decoded bitmap having a width
     * and height equal to or larger than the requested width and height. This
     * implementation does not ensure a power of 2 is returned for inSampleSize
     * which can be faster when decoding but results in a larger bitmap which
     * isn't as useful for caching purposes.
     * 
     * @param options
     *        An options object with out* params already populated (run
     *        through a decode* method with inJustDecodeBounds==true
     * @param reqWidth
     *        The requested width of the resulting bitmap
     * @param reqHeight
     *        The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image  
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and  
            // width  
            final int heightRatio = Math.round((float) height
                / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will  
            // guarantee a final image  
            // with both dimensions larger than or equal to the requested height  
            // and width.  
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            // This offers some additional logic in case the image has a strange  
            // aspect ratio. For example, a panorama may have a much larger  
            // width than height. In these cases the total pixels might still  
            // end up being too large to fit comfortably in memory, so we should  
            // be more aggressive with sample down the image (=larger  
            // inSampleSize).  

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down  
            // further  
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

}
