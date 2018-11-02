package com.ucredit.dream.service;

import org.apache.http.Header;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.SharePreferenceUtil;

public class LocationService extends Service {

    private final String TAG="LocationService";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e(TAG, "onCreate");
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Logger.e(TAG,"onStartCommand " + intent.getStringExtra("latitude") + " " + intent.getStringExtra("longitude"));
			submitLocation(intent.getStringExtra("latitude"), intent.getStringExtra("longitude"),
					intent.getStringExtra("date"), intent.getStringExtra("time"), startId);
		}
		return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
	}

    private void submitLocation(final String latitude,final String longitude,final String date,final String time,final int startId) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.LOCATION_SUBMIT, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e(TAG+" location_submit  success", statusCode + " " + responseString);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        Logger.e(TAG+" location_submit", date+"#"+time);
                        SharePreferenceUtil.getInstance(LocationService.this).setLocationDate(date);
                        if (Integer.valueOf(time)<12) {
                            SharePreferenceUtil.getInstance(LocationService.this).setLocationTime("am");
                        } else {
                            SharePreferenceUtil.getInstance(LocationService.this).setLocationTime("pm");
                        }
                    }else {
                        Logger.e(TAG+" location_submit", response.getJSONObject("error").getString("message"));
                    }
                    stopSelf(startId);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e(TAG+" location_submit  failure", statusCode + " " + responseString);
                    stopSelf(startId);
                }
                
            });
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");
    }

    
    
}
