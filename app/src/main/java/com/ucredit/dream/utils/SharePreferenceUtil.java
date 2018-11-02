package com.ucredit.dream.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {

    private static SharePreferenceUtil util;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static SharePreferenceUtil getInstance(Context context) {
        if (null == util) {
            util = new SharePreferenceUtil(context);
        }
        return util;
    }

    private SharePreferenceUtil(Context context) {
        super();
        sharedPreferences = context.getSharedPreferences("ucredit",
            Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setWifi(boolean b) {
        editor.putBoolean("wifi", b);
        editor.commit();
    }

    public boolean getWifi() {
        return sharedPreferences.getBoolean("wifi", false);
    }
    
    public void setSkip() {
        editor.putBoolean("skip", true);
        editor.commit();
    }
    
    public boolean getSkip() {
        return sharedPreferences.getBoolean("skip", false);
    }


    public String getGetuiCid() {
        return sharedPreferences.getString("cid", "null");
    }

    public void setGetuiCid(String cid) {
        editor.putString("cid", cid);
        editor.commit();
    }

    public void setUserName(String userName) {
        editor.putString("username", userName);
        editor.commit();
    }

    public String getUserName() {
        return sharedPreferences.getString("username", "");
    }

    public void setPassword(String password) {
        editor.putString("password", password);
        editor.commit();
    }

    public String getPassword() {
        return sharedPreferences.getString("password", "");
    }
    
    public void setSalt(String salt) {
        editor.putString("salt", salt);
        editor.commit();
    }
    
    public String getSalt() {
        return sharedPreferences.getString("salt", "");
    }
    
    public void setRunning(boolean running) {
        Logger.e("setrunning", running+"");
        editor.putBoolean("running", running);
        editor.commit();
    }
    
    public boolean getRunning() {
        return sharedPreferences.getBoolean("running", false);
    }
    
    public void setVersionCode(int versionCode) {
        editor.putInt("versionCode", versionCode);
        editor.commit();
    }
    
    public int getVersionCode() {
        return sharedPreferences.getInt("versionCode", 0);
    }
    
    public void setLocationDate(String date) {
        editor.putString("locationDate", date);
        editor.commit();
    }
    
    public String getLocationDate() {
        return sharedPreferences.getString("locationDate","default");
    }
    public void setLocationTime(String time) {
        editor.putString("locationTime", time);
        editor.commit();
    }
    
    public String getLocationTime() {
        return sharedPreferences.getString("locationTime","default");
    }
      
    
    public void setGuide() {
        editor.putBoolean("guide", true);
        editor.commit();
    }

    public boolean getGuide() {
        return sharedPreferences.getBoolean("guide", false);
    }

}
