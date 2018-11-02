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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class BindBankcardVerifyActivity extends BaseActivity {

    @ViewInject(R.id.input)
    private EditText editEdit;
    @ViewInject(R.id.send)
    private TextView sendText;
    @ViewInject(R.id.server)
    private TextView serverText;
    
    private String externalRefNumber;
    private String token;
    
    private String bankCardNo;
    private String bankId;
    private String cardHolderName;
    private String idcard;
    private String mobile;
    
    
    private VerifyCodeTimer timer;
    
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
                Utils.dial(Constants.CUSTOMER_SERVICE, BindBankcardVerifyActivity.this);
                break;
            default:
                break;
        }
    }
    
    private void submit() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("bankCardNo", bankCardNo);
        params.put("externalRefNumber", externalRefNumber);
        params.put("bankId", bankId);
        params.put("cardHolderName", cardHolderName);
        params.put("token", token);
        params.put("idcard", idcard);
        params.put("mobile", mobile);
        params.put("validCode", editEdit.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.BIND_BANK, params,
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
                    Logger.e("bind failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(BindBankcardVerifyActivity.this,
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
                    Logger.e("bind success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if (response.getJSONObject("result")!=null) {
                            JSONObject object=response.getJSONObject("result");
                            Utils.MakeToast(BindBankcardVerifyActivity.this, "绑定成功！");
                            Intent intent=new Intent();
                            intent.putExtra("bankid", object.getString("bankId"));
                            intent.putExtra("card", object.getString("cardNumber"));
                            intent.putExtra("id", object.getString("id"));
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                        
                    } else {
                        Utils.MakeToast(BindBankcardVerifyActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }    
    
    private void sendVerify() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("bankCardNo", bankCardNo);
        params.put("bankId", bankId);
        params.put("cardHolderName", cardHolderName);
        params.put("idcard", idcard);
        params.put("mobile", mobile);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.BIND_VERIFY, params,
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
                    Logger.e("bindbankcard_verify failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(BindBankcardVerifyActivity.this,
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
                    Logger.e("bindbankcard_verify success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if (response.getJSONObject("result")!=null) {
                            externalRefNumber=response.getJSONObject("result").getString("externalRefNumber");
                            token=response.getJSONObject("result").getString("token");
                        }
                    } else {
                        Utils.MakeToast(BindBankcardVerifyActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
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
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "银行卡认证";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_bindbankcardverify;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        timer = new VerifyCodeTimer(60000, 1000, sendText);
        timer.start();
        bankCardNo=getIntent().getStringExtra("bankCardNo");
        bankId=getIntent().getStringExtra("bankId");
        cardHolderName=getIntent().getStringExtra("cardHolderName");
        idcard=getIntent().getStringExtra("idcard");
        mobile=getIntent().getStringExtra("mobile");
        externalRefNumber=getIntent().getStringExtra("externalRefNumber");
        token=getIntent().getStringExtra("token");
    }

}
