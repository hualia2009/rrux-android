package com.ucredit.dream.utils.request;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class UserStateRequest {
    
    private Context mContext;
    private QueryUserListener queryUserListener;

    public UserStateRequest(Context mContext,QueryUserListener queryUserListener){
        this.mContext = mContext;
        this.queryUserListener = queryUserListener;
    }
    
    public interface QueryUserListener {
        public void onStart();

        public void onSuccess(String responseString);

        public void onFailure();
    }
    
    public void queryUser(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.post(Constants.API_USER_STATE,
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("queryUser", "queryApply failure!"
                        + responseString);
                    if(queryUserListener != null){
                        queryUserListener.onFailure();
                    }
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            queryUser();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("queryUser", "queryUser responseString:"
                        + responseString);
                    if(queryUserListener != null){
                        queryUserListener.onSuccess(responseString); 
                    }
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    if(queryUserListener != null){
                        queryUserListener.onStart();
                    }
                }

            });
    }
}
