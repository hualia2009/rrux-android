package com.ucredit.dream.ui.activity.main;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import cn.fraudmetrix.android.FMAgent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.igexin.sdk.PushManager;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.account.LoginActivity;
import com.ucredit.dream.utils.AESUtils;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.CustomVideoView;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.SharePreferenceUtil;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.GetKeySecretRequest;
import com.ucredit.dream.utils.request.GetKeySecretRequest.GetKeySecretListener;
import com.ucredit.dream.utils.request.LoginRequest;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.request.LoginRequest.LoginListener;

public class LoadingActivity extends Activity {

    @ViewInject(R.id.videoView)
    private CustomVideoView videoView;
    public static final int RESULT_CODE_GESTURE_CORRECT = 800;
    public static final int REQUEST_CODE_GESTURE_VERIFY = 801;

    private boolean isLoadingFinish = false; //视频和请求谁先完成开关
    private boolean isLoginFinish = false; //视频和请求谁先完成开关
    private boolean isLoginSuccess = false; //登录成功还是失败

    private String userName;
    private String password;
    private Handler handler = new Handler();
    
    private LoginRequest loginRequest;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UcreditDreamApplication.list.remove(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FMAgent.init(this, true);
        UcreditDreamApplication.list.add(this);
        setContentView(R.layout.player);
        UcreditDreamApplication.cid = SharePreferenceUtil.getInstance(this)
            .getGetuiCid();
        PushManager.getInstance().initialize(this.getApplicationContext());
        //加载首页
        playLoading();
        
        userName = SharePreferenceUtil.getInstance(this).getUserName();
        password = SharePreferenceUtil.getInstance(this).getPassword();
        getDoubleCheck();
    }

    /**
     * 加载loading画面
     */
    private void playLoading() {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isLoadingFinish = true;
                if(isUpdate() || !SharePreferenceUtil.getInstance(LoadingActivity.this).getGuide()){
                    Intent intent = new Intent(LoadingActivity.this, GuideActivity.class);
                    startActivity(intent);
                    SharePreferenceUtil.getInstance(LoadingActivity.this).setGuide();
                    finish();
                }else if (Utils.isNotEmptyString(userName)
                    && Utils.isNotEmptyString(password)) {
                    onRequestFinish();
                } else {
                    startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                    LoadingActivity.this.finish();
                }
            }
        }, 3000);
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
                        JSONObject config = JSON.parseObject(response.getJSONObject("result").getString("config"));
                        if(config != null){
                            UcreditDreamApplication.timeout = config.getIntValue("timeout") * 1000;
                            UcreditDreamApplication.rechargeRate = config.getString("rechargeRate");
                        }
                        getKeyAndSecret();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("doublecheck", "" + responseString);
                    new RequestFailureHandler(LoadingActivity.this, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getDoubleCheck();
                        }
                    }).handleMessage(statusCode);
                    isLoginFinish = true;
                    isLoginSuccess = false;
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
                    autoLogin(userName, password);
                }

                @Override
                public void onFailure() {
                    Utils.customDialog(LoadingActivity.this, "数据初始化失败，请重新启动应用！",
                        new DialogListenner() {

                            @Override
                            public void confirm() {
                                System.exit(0);
                            }
                        });
                }
            }).getKeyAndSecret();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LoginRequest.REQUESTCODE_VERIFY){
            if(data != null){
                loginRequest.setVerifyCode(data.getStringExtra("verifyCode"));
                loginRequest.startLogin();
            }else{
                Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            return;
        }
        toMainPage();
        finish();
    }

    /**
     * 自动登录
     * 1.查询本地数据登录用户名和密码
     * 2.用用户名和密码发起登录请求
     * 3.在注册，登录，修改密码成功3处地方需要将
     * 用户名和加密密码存储到本地
     */
    private void autoLogin(String userName, String password) {
        if (Utils.isNotEmptyString(userName)
            && Utils.isNotEmptyString(password)) {
            loginRequest = new LoginRequest(userName, AESUtils.doDecryptCBC(password,
                UcreditDreamApplication.key), this, new LoginListener() {

                @Override
                public void onSuccess() {
                    isLoginSuccess = true;
                    isLoginFinish = true;
                    onRequestFinish();
                }

                @Override
                public void onFailure() {
                    isLoginFinish = true;
                    isLoginSuccess = false;
                    onRequestFinish();
                };

                @Override
                public void onStart() {

                };

            }, true);
            loginRequest.startLogin();
        }
    }

    private void onRequestFinish() {
        if (!isLoginFinish || !isLoadingFinish) {
            return;
        }

        if (!isLoginSuccess) {
            Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        toMainPage();
        finish();
    }

    private void toMainPage() {
        Intent intent = new Intent(LoadingActivity.this, HomePageActivity.class);
        startActivity(intent);
    }

    protected boolean isUpdate() {
        PackageInfo packageInfo;
        try {
            packageInfo = LoadingActivity.this.getPackageManager().getPackageInfo(
                LoadingActivity.this.getPackageName(), 0);
            int versionCode = packageInfo.versionCode;
            Logger.e("versionCode", versionCode + "");
            Logger.e("versionCode", SharePreferenceUtil
                .getInstance(LoadingActivity.this).getVersionCode() + "");
            if (SharePreferenceUtil.getInstance(LoadingActivity.this).getVersionCode() < versionCode) {
                SharePreferenceUtil.getInstance(LoadingActivity.this).setVersionCode(
                    versionCode);
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
