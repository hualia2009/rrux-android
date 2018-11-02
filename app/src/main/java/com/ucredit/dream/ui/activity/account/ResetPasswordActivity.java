package com.ucredit.dream.ui.activity.account;

import java.util.Locale;

import org.apache.http.Header;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.custom.widgt.CustomEditText;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.crypt.CryptUtils;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.AESUtils;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.SharePreferenceUtil;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ResetPasswordActivity extends BaseActivity {
    
    public static final int RESULT_CODE_FINISH = 100;

    @ViewInject(R.id.newly)
    private CustomEditText newly;
    @ViewInject(R.id.again)
    private CustomEditText again;
    private Context mContext;
    @ViewInject(R.id.next)
    private Button next;

    @OnClick(R.id.next)
    private void click(View view) {
        switch (view.getId()) {
            case R.id.next:
                if (inputCheck()) {
                    getSalt();
                }
                break;

            default:
                break;
        }
    }

    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(newly.getText().toString())) {
            Utils.MakeToast(this, "请您设置新密码");
            return false;
        }
        if (newly.getText().toString().length() < 6||newly.getText().toString().length() > 20) {
            Utils.MakeToast(this, "请输入6－20位英文数字，或混合的密码");
            return false;
        }
        if (!Utils.isNotEmptyString(again.getText().toString())) {
            Utils.MakeToast(this, "请输入您的密码");
            return false;
        }
        if (!newly.getText().toString().equals(again.getText().toString())) {
            Utils.MakeToast(this, "密码输入不一致，请重新输入");
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        new EditTextUtils(newly, this).setPassword();
        new EditTextUtils(again, this).setPassword();
        mContext = ResetPasswordActivity.this;
        
        TextWatcherListener changeListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                if(Utils.isNotEmptyString(newly.getText().toString()) && 
                        Utils.isNotEmptyString(again.getText().toString())){
                    next.setEnabled(true);
                }else{
                    next.setEnabled(false);
                }
                Utils.replaceSpace(newly);
                Utils.replaceSpace(again);
            }
        });
        newly.addTextChangedListener(changeListener);
        again.addTextChangedListener(changeListener);
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "修改密码";
    }

    @Override
    protected int getContentId() {
        return R.layout.modify_password;
    }
    
    private void getSalt(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        long time = System.currentTimeMillis()*1000;
        StringBuilder sb = new StringBuilder();
        sb.append("?phone=").append(getIntent().getStringExtra("phone"));
        sb.append("&clientId=").append(UcreditDreamApplication.clientId);
        sb.append("&t=").append(time);
        sb.append("&md=").append(getEncryptStr(time));
        Logger.e("getSalt", "sb:"+sb.toString());
        httpClient.post(Constants.API_ENCRYPT_SALT + sb.toString(),
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                	setMask(false);
                    Logger.e("UcreditSaleApplication", "get salt failure!"
                        + responseString);
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getSalt();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                	setMask(false);
                    Logger.e("UcreditSaleApplication", "getsalt() responseString:"
                        + responseString);
                    if (JSON.parseObject(responseString).getBooleanValue("success")) {
                        UcreditDreamApplication.salt = JSON.parseObject(responseString)
                                .getString("result");
                        SharePreferenceUtil.getInstance(mContext).setSalt(UcreditDreamApplication.salt);
                        modifyPassword();
                    } 
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getSalt();
    					}
    				  });     
                    setMask(true);
                }

            });
    }
    
    private String getEncryptStr(long time) {
        String encryptStr = new CryptUtils().getEncryptStr(
            getIntent().getStringExtra("phone"),
            UcreditDreamApplication.clientId,
            time+"",
            UcreditDreamApplication.secret);
        return encryptStr.toUpperCase(Locale.getDefault());
    }

    private void modifyPassword() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", getIntent().getStringExtra("phone"));
        params.put("password",Utils.getEncryptPassword(newly.getText().toString()));
        params.put("verifyCode", getIntent().getStringExtra("verifyCode"));
        params.setUseJsonStreamer(true);
        Logger.i("modifyPassword", "" + params.toString());
        httpClient.post(Constants.API_RESET_PASSWORD, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						modifyPassword();
    					}
    				  });                    
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("modifypassword failure", statusCode
                        + " login error" + responseString);
                    setMask(false);
                    new RequestFailureHandler(ResetPasswordActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                modifyPassword();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("modifypassword success", statusCode + " "
                        + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        Utils.MakeToast(ResetPasswordActivity.this, "修改成功！");

                        //将用户名和密码保存到本地，用于自动登录
                        if (UcreditDreamApplication.mUser != null) {
                            SharePreferenceUtil.getInstance(
                                ResetPasswordActivity.this).setUserName(
                                UcreditDreamApplication.mUser
                                    .getPhone());
                            SharePreferenceUtil.getInstance(
                                ResetPasswordActivity.this).setPassword(
                                    AESUtils.doEncryptCBC(newly.getText().toString()
                                        .trim(), UcreditDreamApplication.key));
                        }
                        setResult(RESULT_CODE_FINISH);
                        finish();
                    } else {
                        Utils.MakeToast(ResetPasswordActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

}
