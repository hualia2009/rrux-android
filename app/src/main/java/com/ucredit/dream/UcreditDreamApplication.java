package com.ucredit.dream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.fraudmetrix.android.FMAgent;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.loopj.android.http.AsyncHttpClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.ucredit.crash.CrashHandler;
import com.ucredit.dream.bean.ApplyState;
import com.ucredit.dream.bean.City;
import com.ucredit.dream.bean.Province;
import com.ucredit.dream.bean.User;
import com.ucredit.dream.db.DBManager;
import com.ucredit.dream.service.LocationService;
import com.ucredit.dream.utils.DeviceUuidFactory;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.SharePreferenceUtil;

public class UcreditDreamApplication extends Application {
    
    public static String key = "";
    public static String secret = "";
    public static String clientId = "";
    public static String salt = "";
    public static String cid = "";
    public static String lendId = "";
    public static User mUser;
    public static ApplyState applyState;
    public static String refreshToken = "";
    public static String token = "";
    public static String UMENG_CHANNEL;
    public static boolean isDoubleCheck = false;

    public static String rechargeRate = "0.0035";
    public static int timeout = 30 * 1000;

    /** 百度定位 **/
    public LocationClient mLocationClient;
    public UcreditLocationListener mLocationListener;
    private String longtitude;
    private String latitude;
    public static String province = "";
    public static String city = "";
    
    public static List<Province> provinceList=new ArrayList<Province>();
    public static ArrayList<City> cityList=new ArrayList<City>();
    public static ArrayList<String> cityNameList=new ArrayList<String>();
    
    public static List<Activity> list;
    
    static{
        System.loadLibrary("CryptUtils");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        if (!getCurProcessName(this).equals("com.ucredit.dream")) {
            return;
        }
        initImageLoader();
        //获取clientId
        getClientId();
        list=new ArrayList<Activity>();
        CrashHandler.getInstance().init(getApplicationContext());
        FMAgent.init(this, true);
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
            UMENG_CHANNEL = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        initLoc();
        getAddressByDB();
    }
    
    /**
     * 获取设备唯一id
     * 
     * @return
     */
    private String getClientId() {
        clientId = new DeviceUuidFactory(getApplicationContext())
            .getDeviceUuid().toString();
        Logger.e("clientId", "" + clientId);
        AsyncHttpClient.mContext = getApplicationContext();
        return clientId;
    }

    private void initImageLoader() {
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .showImageOnLoading(R.drawable.default_appendix)
            .showImageForEmptyUri(R.drawable.default_appendix)
            .showImageOnFail(R.drawable.default_appendix)
            .displayer(new SimpleBitmapDisplayer()).build();
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(
            this).defaultDisplayImageOptions(displayImageOptions).build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
    }    
    
    public String getLongtitude() {
        return longtitude;
    }

    public String getLatitude() {
        return latitude;
    }
    
    private void initLoc() {
        mLocationClient = new LocationClient(this.getApplicationContext());
        mLocationListener = new UcreditLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
        InitLocation();
        mLocationClient.start();
    }

    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
        option.setCoorType("gcj02");//返回的定位结果是百度经纬度，默认值gcj02
//        option.setScanSpan(15000);//设置发起定位请求的间隔时间为10分钟
        option.setScanSpan(120000);//设置发起定位请求的间隔时间为2分钟
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    
    public class UcreditLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            String time=getTime();
            String date=getDate();
            int hour=Integer.valueOf(time);
            longtitude=""+location.getLongitude();
            latitude=""+location.getLatitude();
            if (latitude.equals("4.9E-324")) {
                latitude="";
            }
            if (longtitude.equals("4.9E-324")) {
                longtitude="";
            }
            province = location.getProvince(); 
            city = location.getCity();
            Logger.e("province",""+location.getProvince());
            Logger.e("city",""+location.getCity());
            Logger.e("longtitude",""+longtitude+" time "+time);
            Logger.e("latitude",""+latitude+" time "+time);

            if (!date.equals(SharePreferenceUtil.getInstance(getApplicationContext()).getLocationDate())) {
                if ((hour>=9&&hour<=10)||(hour>=14&&hour<=15)) {
                    postLocationInfo(time, date);
                }
            } else {
                if ((hour>=9&&hour<=10)&&!SharePreferenceUtil.getInstance(getApplicationContext()).getLocationTime().equals("am")) {
                    postLocationInfo(time, date);
                }
                if ((hour>=14&&hour<=15)&&!SharePreferenceUtil.getInstance(getApplicationContext()).getLocationTime().equals("pm")) {
                    postLocationInfo(time, date);
                }
            }
            
        }

        private void postLocationInfo(String time, String date) {
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.putExtra("longitude", "" + longtitude);
            intent.putExtra("latitude", "" + latitude);
            intent.putExtra("date", "" + date);
            intent.putExtra("time", "" + time);
            startService(intent);
        }
    }
    
    private String getDate(){
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        return str;
    }
    
    private String getTime(){
        SimpleDateFormat formatter = new SimpleDateFormat ("HH");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        return str;
    }
    
    private void getAddressByDB() {
        cityList.clear();
        cityNameList.clear();
        DBManager dbm = new DBManager(this);
        SQLiteDatabase db = dbm.openDatabase();
        if (db == null) {
            return;
        }
        Cursor cursor = db
            .rawQuery(
                "select distinct id,zone_name from m_zone where zone_level=?",
                new String[]{"1"});
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String provinceName = cursor.getString(cursor
                .getColumnIndex("zone_name"));
            int provinceCode = cursor.getInt(cursor
                .getColumnIndex("id"));
            Province province = new Province();
            province.setName(provinceName);
            province.setCode(provinceCode);
            List<City> temp = new ArrayList<City>();
            Cursor cityCursor = db.rawQuery(
                "select distinct id,zone_name,city_zone_id from m_zone where zone_level=? and province_zone_id=?",
                new String[] { "2",""+provinceCode });
            for (cityCursor.moveToFirst(); !cityCursor.isAfterLast(); cityCursor
                .moveToNext()) {
                String cityName = cityCursor.getString(cityCursor
                    .getColumnIndex("zone_name"));
                int cityCode = cityCursor.getInt(cityCursor
                    .getColumnIndex("city_zone_id"));
                City city = new City();
                city.setName(cityName);
                city.setCode(cityCode);
                city.setProvince(province);
                cityList.add(city);
                temp.add(city);
                cityNameList.add(cityName);
            }
            province.setCitys(temp);
            provinceList.add(province);
        } 
        dbm.closeDatabase();
    }
    
    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
    
}
