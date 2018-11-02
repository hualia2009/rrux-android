package com.ucredit.dream.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.Picture;

public class PictureDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "picture.db";
    public static final int DB_VERSION = 1;
    private static PictureDbHelper mInstance = null;
    
    public static final String TABLE_PICTURE_NAME = "picture";
    public static final String FIELD_PICTURE_KEYID = "_id";
    public static final String FIELD_PICTURE_USERID = "userid";
    public static final String FIELD_PICTURE_STATUS = "status";
    public static final String FIELD_PICTURE_TIME = "time";
    public static final String FIELD_PICTURE_LOCALURL = "localurl";
    public static final String FIELD_PICTURE_TYPE = "type";
    public static final String FIELD_PICTURE_ID = "aid";
    public static final String FIELD_PICTURE_CUSTOMID = "customid";
    public static final String FIELD_PICTURE_UPLOADEDTIME = "uploadedtime";
    public static final String FIELD_PICTURE_REMOTEURL = "remoteurl";
    
    
    private PictureDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static PictureDbHelper GetInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PictureDbHelper(context);
        }
        return mInstance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        String sql_system = "CREATE TABLE IF NOT EXISTS "
                + TABLE_PICTURE_NAME + "( "
                + FIELD_PICTURE_KEYID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + FIELD_PICTURE_USERID + " text,"
                + FIELD_PICTURE_STATUS + " text,"
                + FIELD_PICTURE_TIME + " text,"
                + FIELD_PICTURE_LOCALURL + " text,"
                + FIELD_PICTURE_CUSTOMID + " text,"
                + FIELD_PICTURE_ID + " text,"
                + FIELD_PICTURE_TYPE + " text,"
                + FIELD_PICTURE_UPLOADEDTIME + " text,"
                + FIELD_PICTURE_REMOTEURL + " text"
                + ")";
        db.execSQL(sql_system);        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        createTable(db);
    }

    private void dropTable(SQLiteDatabase db) {
        String sql_system = "DROP TABLE IF EXISTS " + TABLE_PICTURE_NAME;
        db.execSQL(sql_system);
    }
    
    public void insertPicture(Picture picture) {
//      if (memorandum == null || UcreditSaleApplication.mUser == null) {
//          return;
//      }
      ContentValues values = new ContentValues();
      values.put(PictureDbHelper.FIELD_PICTURE_USERID, picture.getUserId());
      values.put(PictureDbHelper.FIELD_PICTURE_STATUS, picture.getStatus());
      values.put(PictureDbHelper.FIELD_PICTURE_TIME, String.valueOf(picture.getTime()));
      values.put(PictureDbHelper.FIELD_PICTURE_LOCALURL, picture.getPictureURL());
      values.put(PictureDbHelper.FIELD_PICTURE_ID, picture.getId());
      values.put(PictureDbHelper.FIELD_PICTURE_CUSTOMID, picture.getCustomId());
      values.put(PictureDbHelper.FIELD_PICTURE_TYPE, picture.getAttachType());
      values.put(PictureDbHelper.FIELD_PICTURE_UPLOADEDTIME, picture.getUploadedTime());
      values.put(PictureDbHelper.FIELD_PICTURE_REMOTEURL, picture.getRemoteUrl());
      getWritableDatabase().insert(PictureDbHelper.TABLE_PICTURE_NAME, null,
          values);
    }    
    
    public void updatePicture(Picture picture) {
//      if (memorandum == null || UcreditSaleApplication.mUser == null) {
//          return;
//      }
        ContentValues values = new ContentValues();
        values.put(PictureDbHelper.FIELD_PICTURE_USERID, picture.getUserId());
        values.put(PictureDbHelper.FIELD_PICTURE_STATUS, picture.getStatus());
        values.put(PictureDbHelper.FIELD_PICTURE_TIME, String.valueOf(picture.getTime()));
        values.put(PictureDbHelper.FIELD_PICTURE_LOCALURL, picture.getPictureURL());
        values.put(PictureDbHelper.FIELD_PICTURE_ID, picture.getId());
        values.put(PictureDbHelper.FIELD_PICTURE_CUSTOMID, picture.getCustomId());
        values.put(PictureDbHelper.FIELD_PICTURE_TYPE, picture.getAttachType());
        values.put(PictureDbHelper.FIELD_PICTURE_UPLOADEDTIME, picture.getUploadedTime());
        values.put(PictureDbHelper.FIELD_PICTURE_REMOTEURL, picture.getRemoteUrl());
        getWritableDatabase().update(PictureDbHelper.TABLE_PICTURE_NAME,
            values,"time=?  AND userid=? AND customid =?",new String[]{String.valueOf(picture.getTime()),picture.getUserId(),picture.getCustomId()});
    }    
    
    public void deletePicture(Picture picture) {
        if (picture == null || UcreditDreamApplication.mUser == null) {
            return;
        }
        getWritableDatabase().delete(
            TABLE_PICTURE_NAME,
            "time=?  AND userid=? AND customid =?",
            new String[] { ""+picture.getTime(),
                picture.getUserId(), picture.getCustomId()});
    }    
    
    public ArrayList<Picture> getPictures(String userId,String customId) {
        ArrayList<Picture> pictureList = new ArrayList<Picture>();
        String sql = "select * from " + PictureDbHelper.TABLE_PICTURE_NAME
            + " where userid =? AND customid =?";
        Cursor cursor = getWritableDatabase().rawQuery(sql,
            new String[] { userId,customId});
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Picture picture = new Picture();
            picture.setUserId(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_USERID)));
            picture.setStatus(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_STATUS)));
            picture.setTime(Long.valueOf(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_TIME))));
            picture.setPictureURL(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_LOCALURL)));
            picture.setId(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_ID)));
            picture.setCustomId(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_CUSTOMID)));
            picture.setAttachType(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_TYPE)));
            picture.setUploadedTime(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_UPLOADEDTIME)));
            picture.setRemoteUrl(cursor.getString(cursor
                .getColumnIndex(PictureDbHelper.FIELD_PICTURE_REMOTEURL)));
            pictureList.add(picture);
        }
        cursor.close();
        return pictureList;
    }        
    
}
