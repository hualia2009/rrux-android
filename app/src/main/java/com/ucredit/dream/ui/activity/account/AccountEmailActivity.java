package com.ucredit.dream.ui.activity.account;

import org.apache.http.Header;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class AccountEmailActivity extends BaseActivity {

    @ViewInject(R.id.tip)
    private TextView tip;
    @ViewInject(R.id.email)
    private EditText email;
    @ViewInject(R.id.submit)
    private TextView submit;
    
    private String tipMsg = "请填入有效邮箱（例：工作邮箱或者学校邮箱）有助于快速帮您通过审核。";

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "邮箱设置";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_account_email_set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getEmail()!=null) {
            email.setText(UcreditDreamApplication.mUser.getEmail());
            email.setSelection(email.getText().length());//光标移到最后
            submit.setEnabled(true);
        }
        tip.setText(tipMsg);
        
        TextWatcherListener changeListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                if(Utils.isNotEmptyString(email.getText().toString())){
                    submit.setEnabled(true);
                }else{
                    submit.setEnabled(false);
                }
            }
        });
        email.addTextChangedListener(changeListener);
    }

    @OnClick({ R.id.submit })
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    setEmail();
                }
                break;

            default:
                break;
        }
    }

    private void setEmail() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("email", email.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_SET_EMAIL, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						setEmail();
    					}
    				  });
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("setEmail", statusCode + " onFailure "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(AccountEmailActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                setEmail();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    setMask(false);
                    Logger.e("setEmail", statusCode + " " + responseString);
                    JSONObject obj = JSON.parseObject(responseString);
                    if (obj.getBooleanValue("success")) {
                        Utils.MakeToast(AccountEmailActivity.this,
                            obj.getString("result"));
                        UcreditDreamApplication.mUser.setEmail(email.getText().toString());
                    } else {
                        Utils.MakeToast(AccountEmailActivity.this, obj
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    private boolean inputCheck() {
        String emailStr = email.getText().toString();
        if (!Utils.checkEmail(emailStr)) {
            Utils.MakeToast(this, "邮箱输入有误，请检查");
            return false;
        }
        return true;
    }

}
