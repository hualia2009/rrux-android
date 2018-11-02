package com.ucredit.dream.ui.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.custom.widgt.CustomEditText;
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
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.Utils.TextViewClickListener;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ResetValidateActivity extends BaseActivity {
    
    @ViewInject(R.id.phone)
    private CustomEditText phone;
    @ViewInject(R.id.service)
    private TextView service;
    @ViewInject(R.id.next)
    private Button next;

    @OnClick({ R.id.next })
    private void click(View view) {
        switch (view.getId()) {
            case R.id.next:
                if (inputCheck()) {
                    checkRegisted();
                }
                break;
            default:
                break;
        }
    }

    private boolean inputCheck() {
        String userText = phone.getText().toString();
        
        if (!Utils.isNotEmptyString(userText)) {
            Utils.MakeToast(this, "请输入您的手机号");
            return false;
        }
        return true;
    }
    
	private void checkRegisted() {
		AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.API_IS_VALIDATE,
            params, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("isregisted", statusCode + " onFailure "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(ResetValidateActivity.this, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            checkRegisted();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    setMask(false);
                    Logger.e("isregisted", statusCode + " " + responseString);
                    try {
                    	JSONObject object = new JSONObject(responseString);
                        if (object.getBoolean("success")) {//没有注册过
                        	Utils.MakeToast(ResetValidateActivity.this,"您的手机号未注册");
                        } else {
                        	Intent intent = new Intent(ResetValidateActivity.this,
                                    ResetValidateVerifyActivity.class);
                                intent.putExtra("phone", "" + phone.getText().toString());
                                startActivityForResult(intent,
                                    ResetPasswordActivity.RESULT_CODE_FINISH);
                        } 	
					} catch (JSONException e) {
						e.printStackTrace();
					}
                    
                }
            });
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == ResetPasswordActivity.RESULT_CODE_FINISH){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        Utils.setCommonEditText(phone, this);
        Utils.setTextViewClick("如果你无法收到信息请联系【客服】", service, 12, 16, this,new TextViewClickListener() {
            
            @Override
            public void onClick(View view) {
                Utils.customDialog(ResetValidateActivity.this, ""+Constants.CUSTOMER_SERVICE, new DialogListenner() {
                    
                    @Override
                    public void confirm() {
                        Utils.dial(Constants.CUSTOMER_SERVICE, ResetValidateActivity.this);
                    }
                },"拨打客服热线");
            }
        },R.color.textview_inner_color,false);
        
        TextWatcherListener changeListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                String phoneText = phone.getText().toString();
                if(Utils.isNotEmptyString(phoneText)
                        && phoneText.length()==11){
                    next.setEnabled(true);
                }else{
                    next.setEnabled(false);
                }
                Utils.replaceSpace(phone);
            }
        });
        phone.addTextChangedListener(changeListener);
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "验证手机";
    }

    @Override
    protected int getContentId() {
        return R.layout.modify_validate_password;
    }
   


    

}
