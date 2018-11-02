package com.ucredit.dream.utils.request;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ApplyStateRequest {
    
    private Context mContext;
    private QueryApplyListener queryApplyListener;

    public ApplyStateRequest(Context mContext,QueryApplyListener queryApplyListener){
        this.mContext = mContext;
        this.queryApplyListener = queryApplyListener;
    }
    
    public interface QueryApplyListener {
        public void onStart();

        public void onSuccess(String responseString);

        public void onFailure();
    }
    
    public void queryApply(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.post(Constants.API_QUERY_APPLY,
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("queryApply", "queryApply failure!"
                        + responseString);
                    if(queryApplyListener != null){
                        queryApplyListener.onFailure();
                    }
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            queryApply();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("queryApply", "queryApply responseString:"
                        + responseString);
                    if(queryApplyListener != null){
                        queryApplyListener.onSuccess(responseString); 
                    }
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    if(queryApplyListener != null){
                        queryApplyListener.onStart();
                    }
                }

            });
    }
}
