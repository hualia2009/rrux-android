package com.ucredit.dream.utils.request;

import java.util.Locale;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import cn.fraudmetrix.android.FMAgent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bairong.mobile.BrAgent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.crypt.CryptUtils;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.User;
import com.ucredit.dream.ui.activity.account.RegistPasswordActivity;
import com.ucredit.dream.utils.AESUtils;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.SharePreferenceUtil;
import com.ucredit.dream.utils.Utils;

public class LoginRequest {

    public static final int STATUS_REGIST = 1;
    public static final int STATUS_BIND = 2;
    public static final int STATUS_FACE = 3;
    
    public static final int REQUESTCODE_VERIFY = 101;

    private String phone;
    private String password;
    private Context mContext;
    private boolean isAutoLogin;
    private LoginListener loginListener;
    
    private String verifyCode = "";

    public LoginRequest(String phone, String password, Context mContext,
            LoginListener loginListener, boolean isAutoLogin) {
        this.phone = phone;
        this.password = password;
        this.mContext = mContext;
        this.isAutoLogin = isAutoLogin;
        this.loginListener = loginListener;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    /**
     * 获取盐，登录时通过secret获取
     */
    public void startLogin() {
        getSalt();
    }
    
    private void getSalt(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        long time = System.currentTimeMillis()*1000;
        StringBuilder sb = new StringBuilder();
        sb.append("?phone=").append(phone);
        sb.append("&clientId=").append(UcreditDreamApplication.clientId);
        sb.append("&t=").append(time);
        sb.append("&md=").append(getEncryptStr(time));
        Logger.e("getSalt", "sb:"+sb.toString());
        httpClient.post(Constants.API_ENCRYPT_SALT + sb.toString(),
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("UcreditSaleApplication", "get salt failure!"
                        + responseString);
                    loginListener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            startLogin();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("UcreditSaleApplication", "getsalt() responseString:"
                        + responseString);
                    if (JSON.parseObject(responseString).getBooleanValue("success")) {
                        UcreditDreamApplication.salt = JSON.parseObject(responseString)
                                .getString("result");
                        SharePreferenceUtil.getInstance(mContext).setSalt(UcreditDreamApplication.salt);
                        login();
                    } 
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    loginListener.onStart();
                }

            });
    }

    private String getEncryptStr(long time) {
        String encryptStr = new CryptUtils().getEncryptStr(
            phone,
            UcreditDreamApplication.clientId,
            time+"",
            UcreditDreamApplication.secret);
        return encryptStr.toUpperCase(Locale.getDefault());
    }

    private String getTokenEncryptStr(long time) {
        String encryptStr = new CryptUtils().getEncryptStr(
            UcreditDreamApplication.clientId,
            UcreditDreamApplication.secret,
            "password",
            phone,
            Utils.getEncryptPassword(password),
            time+"",UcreditDreamApplication.salt);
        return encryptStr.toUpperCase(Locale.getDefault());
    }

    public void login() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        params.put("password", Utils.getEncryptPassword(password));
        params.put("equipmentType", "ANDROID");
        params.put("equipmentId", UcreditDreamApplication.cid);
        params.put("dataSalt", UcreditDreamApplication.salt);
        params.put("equipmentId4CreditAudit", UcreditDreamApplication.clientId);
        String string=FMAgent.onEvent();
        Logger.e("tongdun", string);
        params.put("tongdun", string);
        params.put("bairong", BrAgent.getGid(mContext));
        params.put("clientIP", Utils.getLocalIpAddress());
        params.put("imei", Utils.getIMEI(mContext));
        params.put("verifyCode", getVerifyCode());
        params.put("deviceType", Build.DEVICE);
        params.put("latitude", ((UcreditDreamApplication)mContext.getApplicationContext()).getLatitude());
        params.put("longitude", ((UcreditDreamApplication)mContext.getApplicationContext()).getLongtitude());
        params.setUseJsonStreamer(true);
        Logger.i("login params", params.toString());
        httpClient.post(mContext, Constants.API_LOGIN, params,
            new TextHttpResponseHandler() {

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("login", statusCode + " login error"
                        + responseString);
                    loginListener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            login();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("login", statusCode + " " + responseString);
                    JSONObject result = JSON.parseObject(responseString);
                    if (result.getBooleanValue("success")) {
                        doLogin(responseString);
                    } else {
                        JSONObject error = JSON.parseObject(result
                            .getString("error"));
                        if (!isAutoLogin) {
                            loginListener.onFailure();
                            Utils.MakeToast(mContext,
                                error.getString("message"));
                        }
                        if("1".equals(error.getString("code"))){//需要输入短信验证码
                            Activity activity = (Activity) mContext;
                            Intent intent = new Intent(mContext,RegistPasswordActivity.class);
                            intent.putExtra("isVerify", true);
                            intent.putExtra("phone", "" + phone);
                            activity.startActivityForResult(intent, REQUESTCODE_VERIFY);
                        }
                    }
                }
            });
    }

    public interface GetTokenListener {
        public void onSuccess();
    }

    public interface LoginListener {
        public void onStart();

        public void onSuccess();

        public void onFailure();
    }

    protected void doLogin(String jsonStr) {
        //1.解析json
        JSONObject json = JSON.parseObject(jsonStr);
        JSONObject result = JSON.parseObject(json.getString("result"));
        if (result == null) {
            return;
        }

        //2.生成user对象
        User user = new User();
        user.setUserId(result.getString("userId"));
        user.setCustomerId(result.getString("customerId"));
        user.setName(result.getString("name"));
        user.setIdentityNo(result.getString("identityNo"));
        user.setCipherStatus(result.getIntValue("cipherStatus"));
        user.setEmail(result.getString("email"));
        user.setPhone(result.getString("mobile"));
        user.setCustomerId(result.getString("customerId"));
        user.setPassword(password);
        UcreditDreamApplication.mUser = user;

        //3.将用户名和密码保存到本地，用于自动登录
        SharePreferenceUtil.getInstance(mContext).setUserName(phone);
        SharePreferenceUtil.getInstance(mContext).setPassword(
            AESUtils.doEncryptCBC(password, UcreditDreamApplication.key));

        SharePreferenceUtil.getInstance(mContext).setRunning(true);

        //4.获取token
        getToken();
    }

    /**
     * 获取headerToken
     */
    public void getToken() {
        AsyncHttpClient httpClient = new AsyncHttpClient();

        long time = System.currentTimeMillis()*1000;
        StringBuilder sb = new StringBuilder();
        sb.append("?client_id=").append(UcreditDreamApplication.clientId);
        sb.append("&client_secret=").append(UcreditDreamApplication.secret);
        sb.append("&grant_type=").append("password");
        sb.append("&scope=").append("read");
        sb.append("&username=").append(phone);
        sb.append("&password=").append(Utils.getEncryptPassword(password));
        sb.append("&t=").append(time);
        sb.append("&md=").append(getTokenEncryptStr(time));
        Logger.e("getToken", "sb:"+sb.toString());
        httpClient.post(Constants.API_ENCRYPT_TOKEN + sb.toString(),
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("UcreditSaleApplication", "get token failure!"
                        + responseString);
                    loginListener.onFailure();
                    new RequestFailureHandler(mContext, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            getToken();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("UcreditSaleApplication", "getToken() responseString:"
                        + responseString);
                    if(JSON.parseObject(responseString).getBooleanValue("success")){
                        JSONObject result = JSON.parseObject(JSON.parseObject(
                            responseString).getString("result"));
                        if(result!=null){
                            UcreditDreamApplication.token = result
                                    .getString("access_token");
                            UcreditDreamApplication.refreshToken = result
                                .getString("refresh_token");
                            loginListener.onSuccess();
                        }
                    }
                }

            });
    }
}
