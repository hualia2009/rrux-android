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

public class VerifyCodeRequest {

    private String phone;
    private String type;
    private Context mContext;
    private VerifyCodeListener listener;

    public VerifyCodeRequest(String phone, Context mContext,
            VerifyCodeListener listener,String type) {
        this.phone = phone;
        this.mContext = mContext;
        this.listener = listener;
        this.type = type;
    }

    public interface VerifyCodeListener {
        public void onStart();

        public void onSuccess(String responseString);

        public void onFailure();
    }

    public void getVerifyCode() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        params.put("type", type);
        params.setUseJsonStreamer(true);
        httpClient.post(mContext, Constants.API_SENDVERIFYCODE_REGISTER,
            params, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    listener.onStart();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("getVerifyCode", statusCode + " onFailure "
                        + responseString);
                    listener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getVerifyCode();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    listener.onSuccess(responseString);
                    Logger
                        .e("getVerifyCode", statusCode + " " + responseString);
                }
            });
    }

}
