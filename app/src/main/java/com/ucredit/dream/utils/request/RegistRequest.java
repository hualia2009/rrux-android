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

public class RegistRequest {

    private String phone;
    private Context mContext;
    private hasRegistListener listener;

    public RegistRequest(String phone, Context mContext,
            hasRegistListener listener) {
        this.phone = phone;
        this.mContext = mContext;
        this.listener = listener;
    }

    public interface hasRegistListener {
        public void onStart();

        public void onSuccess(String responseString);

        public void onFailure();
    }

    public void hasRegisted() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_IS_VALIDATE, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    listener.onStart();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("hasRegisted", statusCode + " onFailure "
                        + responseString);
                    listener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            hasRegisted();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("hasRegisted", statusCode + " " + responseString);
                    listener.onSuccess(responseString);
                }

            });
    }

}
