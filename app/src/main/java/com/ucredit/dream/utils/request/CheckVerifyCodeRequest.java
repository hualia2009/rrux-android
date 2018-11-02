package com.ucredit.dream.utils.request;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class CheckVerifyCodeRequest {

    private String phone;
    private String type;
    private Context mContext;
    private CheckListener listener;
    private String verifyCode;

    public CheckVerifyCodeRequest(String phone,String verifyCode, Context mContext,
            CheckListener listener,String type) {
        this.phone = phone;
        this.mContext = mContext;
        this.listener = listener;
        this.type = type;
        this.verifyCode = verifyCode;
    }

    public interface CheckListener {
        public void onStart();

        public void onSuccess(String responseString);

        public void onFailure();
    }

    public void check() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        params.put("type", type);
        params.put("verifyCode", verifyCode);
        params.setUseJsonStreamer(true);
        httpClient.post(mContext, Constants.API_VERIFYCODE_CHECK,
            params, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    listener.onStart();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("check", statusCode + " onFailure "
                        + responseString);
                    listener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            check();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    listener.onSuccess(responseString);
                    Logger
                        .e("check", statusCode + " " + responseString);
                }
            });
    }

}
