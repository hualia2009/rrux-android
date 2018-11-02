package com.ucredit.dream.utils;

import java.util.Locale;

import org.apache.http.Header;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.crypt.CryptUtils;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;


/**
 * 处理所有请求错误码，特别是4001需要重新获取token
 * 
 * @author Li Huang
 */
public class RequestFailureHandler {

    private int retryCount = 3;
    private GetTokenListener getTokenListener;
    private Context mContext;

    public RequestFailureHandler(Context mContext,
            GetTokenListener getTokenListener) {
        this.getTokenListener = getTokenListener;
        this.mContext = mContext;
    }
 

    public void handleMessage(int statusCode) {
        Logger.e("statusCode", statusCode+"");
        switch (statusCode) {
            case 401://重新请求token
                refreshToken();
                break;
            case 0://无网络的情况
                Utils.MakeToast(mContext, "请检查网络并重试！");
                break;
            default:
                break;
        }
    }

    private void refreshToken() {
        if (UcreditDreamApplication.mUser == null) {
            return;
        }
        AsyncHttpClient httpClient = new AsyncHttpClient();

        long time = System.currentTimeMillis()*1000;
        StringBuilder sb = new StringBuilder();
        sb.append("?client_id=").append(UcreditDreamApplication.clientId);
        sb.append("&refresh_token=").append(UcreditDreamApplication.refreshToken);
        sb.append("&client_secret=").append(UcreditDreamApplication.secret);
        sb.append("&grant_type=").append("refresh_token");
        sb.append("&scope=").append("read");
        sb.append("&username=").append(UcreditDreamApplication.mUser.getPhone());
        sb.append("&password=").append(
            Utils.getEncryptPassword(UcreditDreamApplication.mUser.getPassword()));
        sb.append("&t=").append(time);
        sb.append("&md=").append(getTokenEncryptStr(time));
        httpClient.post(Constants.API_ENCRYPT_TOKEN + sb.toString(),
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("refreshToken", statusCode+" get token failure!"
                        + responseString);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("refreshToken", "getToken() responseString:"
                        + responseString);
                    JSONObject result = JSON.parseObject(JSON.parseObject(
                        responseString).getString("result"));
                    UcreditDreamApplication.token = result
                            .getString("access_token");
                    UcreditDreamApplication.refreshToken = result
                        .getString("refresh_token");
                    if (retryCount > 0) {
                        getTokenListener.onSuccess();
                        retryCount--;
                    } else {
                        Utils.MakeToast(mContext, "请求失败，请检查网络重新登录试试");
                    }
                }

            });
    }

    private String getTokenEncryptStr(long time) {
        String encryptStr = new CryptUtils().getEncryptStr(
            UcreditDreamApplication.clientId,
            UcreditDreamApplication.secret,
            "refresh_token",
            UcreditDreamApplication.mUser.getPhone(),
            Utils.getEncryptPassword(UcreditDreamApplication.mUser.getPassword()),
            time+"",
            UcreditDreamApplication.salt,
            UcreditDreamApplication.refreshToken);
        return encryptStr.toUpperCase(Locale.getDefault());
    }
}
