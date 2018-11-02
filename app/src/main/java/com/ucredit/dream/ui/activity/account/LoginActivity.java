package com.ucredit.dream.ui.activity.account;

import org.apache.http.Header;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextWatcher;
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
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.ui.activity.main.HomePageActivity;
import com.ucredit.dream.ui.activity.main.LoadingActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.GetKeySecretRequest;
import com.ucredit.dream.utils.request.GetKeySecretRequest.GetKeySecretListener;
import com.ucredit.dream.utils.request.LoginRequest;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.request.LoginRequest.LoginListener;
import com.ucredit.dream.utils.update.CheckUpdateUtil;




public class LoginActivity extends BaseActivity{
    
    @ViewInject(R.id.phone)
    private CustomEditText phone;
    @ViewInject(R.id.password)
    private CustomEditText password;
    @ViewInject(R.id.login)
    private Button login;
    
    private LoginRequest loginRequest;  
    private Dialog loadingDialog;
    
    private GetKeyAndSecretRecever myReceiver;
    public static final int REQUEST_LOGIN=10;
    
    private EditTextUtils editTextUtils = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        loadingDialog = Utils.showProgressDialog(LoginActivity.this, "登录中...",
            true);
        
        Utils.setCommonEditText(phone, this);
        editTextUtils = new EditTextUtils(password, this);
        editTextUtils.setPassword();
        registerReceiver();
        
        TextWatcher changedListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                String phoneText = phone.getText().toString();
                String passwordText = password.getText().toString();
                if (Utils.isNotEmptyString(phoneText)
                        &&Utils.isNotEmptyString(passwordText)
                        &&phoneText.length()==11
                        &&passwordText.length()>=6) {
                    login.setEnabled(true);
                }else {
                    login.setEnabled(false);
                }
                if (phoneText.length()==11) {
                    password.requestFocus();
                }
                Utils.replaceSpace(password);
            }
        });
        phone.addTextChangedListener(changedListener);
        password.addTextChangedListener(changedListener);
        
        //检查更新
        CheckUpdateUtil.getInstance().startCheck(this, false);
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    
    @Override
    protected boolean hasTitle() {
        return false;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return null;
    }

    @Override
    protected int getContentId() {
        return R.layout.login;
    }
    
    @OnClick({R.id.login,R.id.regist,R.id.forget})
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.login:
                if (inputCheck()) {
                    sendLoginRequest();
                }
                break;
            case R.id.regist:
                Intent intent=new Intent(this, RegistActivity.class);
                startActivityForResult(intent, REQUEST_LOGIN);
                break;
            case R.id.forget:
                toActivity(ResetValidateActivity.class); //找回密码
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (Activity.RESULT_OK == resultCode) {
                    finish();
                }
                break;
            case LoginRequest.REQUESTCODE_VERIFY:
                if(data != null){
                    loginRequest.setVerifyCode(data.getStringExtra("verifyCode"));
                    loginRequest.startLogin();
                }
                break;
            default:
                break;
        }
    }  


    private boolean inputCheck() {
        String phoneText = phone.getText().toString();
        String passwordText = password.getText().toString();
        if (!Utils.isNotEmptyString(phoneText)) {
            Utils.MakeToast(this, "请输入您的手机号");
            return false;
        }
        if (!Utils.isNotEmptyString(passwordText)) {
            Utils.MakeToast(this, "请输入您的密码");
            return false;
        }
        if (passwordText.length()<6||passwordText.length()>20) {
        	Utils.MakeToast(this, "输入6－20位英文数字，或混合的密码");
			return false;
		}
        return true;
    }    
    
    private void sendLoginRequest() {
        loginRequest = new LoginRequest(phone.getText().toString(), password
            .getText().toString() , LoginActivity.this, new LoginListener() {

            @Override
            public void onSuccess() {
                loadingDialog.dismiss();
                //3.根据状态跳转到相应界面
                Intent intent=new Intent(LoginActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure() {
                editTextUtils.setEditTextVisible();
                loadingDialog.dismiss();
            }

            @Override
            public void onStart() {
            	checkNet(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						sendLoginRequest();
						
					}
            		
            	});
                loadingDialog.show();
            }

        }, false);
        loginRequest.startLogin();
    }
    
    
    class GetKeyAndSecretRecever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo  mobNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo  wifiNetInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            
            boolean isMobNet = true;
            if(mobNetInfo != null && !mobNetInfo.isConnected()){
                isMobNet = false;
            }
            
            boolean isWifiNet = true;
            if(wifiNetInfo != null && !wifiNetInfo.isConnected()){
                isWifiNet = false;
            }
            
            if (!isMobNet && !isWifiNet) {
                Utils.MakeToast(LoginActivity.this, "网络不可用");
            }else {
                //重新获取dboubleCheck，key和Secret
                getDoubleCheck();
            }
        }

    }
    
    private  void registerReceiver(){
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver=new GetKeyAndSecretRecever();
        this.registerReceiver(myReceiver, filter);
    }
    
    private  void unregisterReceiver(){
        this.unregisterReceiver(myReceiver);
    }
    
    private void getDoubleCheck() {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setURLEncodingEnabled(false);
        RequestParams params = new RequestParams();
        params.put("os", "1");
        params.put("channel", UcreditDreamApplication.UMENG_CHANNEL);
        asyncHttpClient.get(Constants.URL_APP_VERSION, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("doublecheck", "" + responseString);
                    if (JSON.parseObject(responseString).getBooleanValue(
                        "success")
                        & JSON.parseObject(responseString).getString("result") != null) {
                        JSONObject response = JSON.parseObject(responseString);
                        Logger.e("doublecheck",
                            ""
                                + response.getJSONObject("result")
                                    .getBooleanValue("doublecheck"));
                        if (response.getJSONObject("result").getBooleanValue(
                            "doublecheck")) {
                            UcreditDreamApplication.isDoubleCheck = true;
                        }
                        getKeyAndSecret();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("doublecheck", "" + responseString);
                    new RequestFailureHandler(LoginActivity.this, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getDoubleCheck();
                        }
                    }).handleMessage(statusCode);
                }
            });
    }
    
    /**
     * 获取Key和Secret,只有成功之后才能调用登录
     */
    private void getKeyAndSecret() {
        new GetKeySecretRequest(UcreditDreamApplication.clientId,this,
            new GetKeySecretListener() {

                @Override
                public void onSuccess() {
                    
                }

                @Override
                public void onFailure() {
                    Utils.customDialog(LoginActivity.this, "数据初始化失败，请重新启动应用！",
                        new DialogListenner() {

                            @Override
                            public void confirm() {
                                System.exit(0);
                            }
                        });
                }
            }).getKeyAndSecret();
    }
    
}
