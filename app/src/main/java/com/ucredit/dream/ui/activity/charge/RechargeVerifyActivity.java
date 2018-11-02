package com.ucredit.dream.ui.activity.charge;

import org.apache.http.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.VerifyCodeTimer;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class RechargeVerifyActivity extends BaseActivity {

    @ViewInject(R.id.input)
    private EditText editEdit;
    @ViewInject(R.id.send)
    private TextView sendText;
    
    private VerifyCodeTimer timer;
    
    //进件id
    private String lendId;
    //银行卡id
    private String id;
    private String fee;
    private String rechargeAmount;
    private String totalAmount;
    
    private String externalRefNumber;
    private String token;
    
    @OnClick({R.id.submit,R.id.send,R.id.server})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    submit();
                }
                break;
            case R.id.send:
                sendVerify();
                break;
            case R.id.server:
                Utils.dial(Constants.CUSTOMER_SERVICE, RechargeVerifyActivity.this);
                break;
            default:
                break;
        }
    }
    
    private boolean inputCheck() {  
        if (!Utils.isNotEmptyString(editEdit.getText().toString())) {
            Utils.MakeToast(this, "请输入验证码");
            return false;
        }
        if (!Utils.isNotEmptyString(externalRefNumber)||!Utils.isNotEmptyString(token)) {
            Utils.MakeToast(this, "您的验证码已过期，请点击重新发送");
            return false;
        }
        return true;
    }           
    
    private void sendVerify() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("applicationId", lendId);
        params.put("fee", fee);
        params.put("rechargeAmount", rechargeAmount);
        params.put("totalAmount", totalAmount);
        params.put("userBankId", id);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.PAY_VERIFY, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						sendVerify();
    						
    					}
                		
                	});
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("pay_verify failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RechargeVerifyActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                submit();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("pay_verify success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if (response.getJSONObject("result")!=null) {
                            externalRefNumber=response.getJSONObject("result").getString("externalRefNumber");
                            token=response.getJSONObject("result").getString("token");
                        }
                        if (timer != null) {
                            timer.start();
                        }
                    } else {
                        Utils.MakeToast(RechargeVerifyActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    
    private void submit() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("applicationId", lendId);
        params.put("externalRefNumber", externalRefNumber);
        params.put("fee", fee);
        params.put("rechargeAmount", rechargeAmount);
        params.put("token", token);
        params.put("totalAmount", totalAmount);
        params.put("userBankId", id);
        params.put("verifyCode", editEdit.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.PAY, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						submit();
    						
    					}
                		
                	});
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("pay failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RechargeVerifyActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                submit();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("pay success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        Utils.MakeToast(RechargeVerifyActivity.this, "充值还款成功！");
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Utils.MakeToast(RechargeVerifyActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }
  
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "充值还款";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    
    @Override
    protected int getContentId() {
        return R.layout.activity_rechargeverify;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        timer = new VerifyCodeTimer(60000, 1000, sendText);
        lendId=getIntent().getStringExtra("lendId");
        id=getIntent().getStringExtra("id");
        rechargeAmount=getIntent().getStringExtra("rechargeAmount");
        totalAmount=getIntent().getStringExtra("totalAmount");
        fee=getIntent().getStringExtra("fee");
        sendVerify();
    }

}
