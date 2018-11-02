package com.ucredit.dream.utils.request;

import org.apache.http.Header;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;


public class GetKeySecretRequest {
    
    private String clientId;
    private GetKeySecretListener getKeySecretListener;
    private Context mContext;
    
    private boolean isKeySuccess = false;
    private boolean isSecretSuccess = false;

    public GetKeySecretRequest(String clientId,Context mContext,
            GetKeySecretListener getKeySecretListener){
        this.clientId = clientId;
        this.getKeySecretListener = getKeySecretListener;
        this.mContext = mContext;
    }
    
    public void getKeyAndSecret(){
        getEncryptKey();
        getSecret();
    }
    
    private void onRequestFinish(){
        if(!isKeySuccess || !isSecretSuccess){
            return;
        }
        getKeySecretListener.onSuccess();
    }
    
    /**
     * 获取AES KEY
     */
    private void getEncryptKey() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.post(Constants.API_ENCRYPT_KEY + clientId,
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("GetKeyRequest", "get key failure!"
                        + responseString);
                    getKeySecretListener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getEncryptKey();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("GetKeyRequest",
                        "getEncryptKey() responseString:" + responseString);
                    UcreditDreamApplication.key = JSON.parseObject(responseString).getString("result");
                    Logger.e("GetKeySecretRequest", "key:" + UcreditDreamApplication.key);

                    RequestParams.key = UcreditDreamApplication.key;
                    RequestParams.clientId = clientId;
                    
                    isKeySuccess = true;
                    onRequestFinish();
                }

            });

    }
    
    private void getSecret() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.post(Constants.API_ENCRYPT_SECRET + clientId,
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("GetSecretRequest", "getsecret() failure!"
                        + responseString);
                    getKeySecretListener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getSecret();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("GetSecretRequest",
                        "getsecret() responseString:" + responseString);
                    if (JSON.parseObject(responseString).getBooleanValue("success")) {
                        UcreditDreamApplication.secret = JSON.parseObject(
                            JSON.parseObject(responseString).getString("result"))
                            .getString("client_secret");
                        Logger.e("GetKeySecretRequest", "secret:" + UcreditDreamApplication.secret);
                        
                        isSecretSuccess = true;
                        onRequestFinish();
                    }
                }

            });
    }
    
    public interface GetKeySecretListener{
        public void onSuccess();
        public void onFailure();
    }
}
