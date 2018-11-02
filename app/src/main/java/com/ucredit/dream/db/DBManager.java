package com.ucredit.dream.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ucredit.dream.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;


public class DBManager {
    private final int BUFFER_SIZE = 400000;
    public static final String DB_NAME = "zone.db"; //保存的数据库文件名
    public static final String PACKAGE_NAME = "com.ucredit.dream";
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME+"/databases";  //在手机里存放数据库的位置
 
    private SQLiteDatabase database;
    private Context context;
 
    public DBManager(Context context) {
        this.context = context;
        this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
    }
 
    public SQLiteDatabase openDatabase() {
         
        return this.database;
    }
 
    private SQLiteDatabase openDatabase(String dbfile) {
        String databaseFilename = DB_PATH + "/" + DB_NAME; 
        
        File dir = new File(DB_PATH);
        if (!dir.exists())   
            dir.mkdir();  

            
 
        try {
            if (!(new File(databaseFilename)).exists()) {
                InputStream is = this.context.getResources().openRawResource(
                        R.raw.zone); //欲导入的数据库
                FileOutputStream fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,
                    null);
            return db;
        } catch (FileNotFoundException e) {
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return null;
    }
    
    public void closeDatabase() {
        this.database.close();
    }
 
}
