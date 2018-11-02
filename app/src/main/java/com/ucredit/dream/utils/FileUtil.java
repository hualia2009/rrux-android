package com.ucredit.dream.utils;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * 类描述：FileUtil
 * 
 * @version
 */
@SuppressLint("NewApi")
public class FileUtil {

    //学历
    public static final String FILE_NAME_UPLOAD_HUAKOU = "huakou.jpg";
    public static final String FILE_NAME_UPLOAD_HUAKOU_COMPRESS = "huakou_compress.jpg";
    //学历
    public static final String FILE_NAME_UPLOAD_XUELI = "xueli.jpg";
    public static final String FILE_NAME_UPLOAD_XUELI_COMPRESS = "xueli_compress.jpg";
   //手持身份证照片
    public static final String FILE_NAME_UPLOAD_SHOUCHI = "shouchi.jpg";
    public static final String FILE_NAME_UPLOAD_SHOUCHI_COMPRESS = "shouchi_compress.jpg";
    //身份证国徽面
    public static final String FILE_NAME_UPLOAD_GUOHUI = "guohui.jpg";
    public static final String FILE_NAME_UPLOAD_GUOHUI_COMPRESS = "guohui_compress.jpg";
    //身份证人像面
    public static final String FILE_NAME_UPLOAD_RENXIANG = "renxiang.jpg";
    public static final String FILE_NAME_UPLOAD_RENXIANG_COMPRESS = "renxiang_compress.jpg";
    
    public static final String FILE_NAME_UPLOAD_BANKCARD = "bankcard.jpg";
    public static final String FILE_NAME_UPLOAD_BANKCARD_COMPRESS = "bankcard_compress.jpg";
    public static final String FILE_NAME_UPLOAD_BANKCARD_ORIENTATION = "bankcard_orientation.jpg";

    public static final String FILE_NAME_UPLOAD_CLIENT = "client.jpg";
    
    public static File updateDir = null;
    public static File updateFile = null;

    public static boolean isCreateFileSucess;

    public static String getUpdateFilePath(String fileName, Context context) {
        // 当前挂载了sdcard
        File updateDir = getUcreditDir(context);
        File updateFile = new File(updateDir + "/" + fileName + ".apk");
        return updateFile.getAbsolutePath();

    }

    /*
     * 查找本地豪华礼物的目录
     */
    public static File getUcreditDir(Context context) {
        File cachDir = null;
        if (Environment.getExternalStorageState().equals(//有SD卡
            Environment.MEDIA_MOUNTED)) {
            cachDir = new File(Environment.getExternalStorageDirectory(),
                "ucredit_dream/");
            if (!cachDir.exists()) {
                cachDir.mkdirs();
            }
        } else {//无SD卡
            cachDir = context.getCacheDir();
            if (cachDir != null) {
                cachDir = new File(cachDir, "ucredit_dream/");
                if (!cachDir.exists()) {
                    cachDir.mkdirs();
                }
            } else {
                String cacheDirPath = "/data/data/" + context.getPackageName()
                    + "/cache/";
                Logger.w(
                    "Can't define system cache directory! '%s' will be used.",
                    cacheDirPath);
                cachDir = new File(cacheDirPath);
                if (!cachDir.exists()) {
                    cachDir.mkdirs();
                }
            }
        }
        return cachDir;
    }

    /**
     * 方法描述：createFile方法
     * 
     * @param String
     *        app_name
     * @return
     * @see FileUtil
     */
    public static void createFile(String app_name, Context context) {

        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
            .getExternalStorageState())) {
            isCreateFileSucess = true;

            updateDir = getUcreditDir(context);
            updateFile = new File(updateDir + "/" + app_name + ".apk");

            if (!updateDir.exists()) {
                updateDir.mkdirs();
            }
            if (!updateFile.exists()) {
                try {
                    updateFile.createNewFile();
                } catch (IOException e) {
                    isCreateFileSucess = false;
                    e.printStackTrace();
                }
            }

        } else {
            isCreateFileSucess = false;
        }
    }

    public static void takeFile(Activity activity,int requestCode) {
        Intent intent=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        
//        Intent intent = new Intent(Intent.ACTION_PICK);  
//        intent.setType("image/*");
        
//        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT  
//        intent.addCategory(Intent.CATEGORY_OPENABLE);  
//        intent.setType("image/jpeg");  
        activity.startActivityForResult(intent, requestCode);  
    }
    
    public static File handleImage(String fileName, Context mContext,
            String compressFileName) {
        //创建文件，用来展示
        File file = new File(FileUtil.getUcreditDir(mContext), fileName);
        if (!file.exists()) {
            return null;
        }
        File newFile = new File(FileUtil.getUcreditDir(mContext), compressFileName);
        //压缩文件，用来上传
        ImageHelper.saveCompressBitmap(
            ImageHelper.createImage(file.toString()),
            newFile);
        //展示没有压缩的图片
//        imageView.setVisibility(View.VISIBLE);
//        ImageLoader.getInstance().displayImage("file:///" + file.getPath(),
//            imageView);
        return newFile;
    }
    
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider  
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider  
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                        + split[1];
                }

                // TODO handle non-primary volumes  
            }
            // DownloadsProvider  
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider  
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                    selectionArgs);
            }
        }
        // MediaStore (and general)  
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address  
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File  
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    
    public static String getDataColumn(Context context, Uri uri,
            String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection,
                selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    } 
    
    public static void takePhoto(Activity activity, String fileName,
            int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        File file = new File(FileUtil.getUcreditDir(activity), fileName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        activity.startActivityForResult(intent, requestCode);
    }
    
    public static boolean isExternalStorageDocument(Uri uri) {  
        return "com.android.externalstorage.documents".equals(uri.getAuthority());  
    }  
      
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is DownloadsProvider. 
     */  
    public static boolean isDownloadsDocument(Uri uri) {  
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
    }  
      
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is MediaProvider. 
     */  
    public static boolean isMediaDocument(Uri uri) {  
        return "com.android.providers.media.documents".equals(uri.getAuthority());  
    }  
      
    /** 
     * @param uri The Uri to check. 
     * @return Whether the Uri authority is Google Photos. 
     */  
    public static boolean isGooglePhotosUri(Uri uri) {  
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
    }      
    
}