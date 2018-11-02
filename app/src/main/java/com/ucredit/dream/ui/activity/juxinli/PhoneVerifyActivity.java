package com.ucredit.dream.ui.activity.juxinli;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.custom.widgt.CustomEditText;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.apply.ApplyActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.VerifyCodeTimer;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;


public class PhoneVerifyActivity extends BaseActivity {

    @ViewInject(R.id.password)
    private CustomEditText password;
    @ViewInject(R.id.input)
    private EditText verify;
    @ViewInject(R.id.phone)
    private EditText phone;
    @ViewInject(R.id.submit)
    private TextView button;
    @ViewInject(R.id.server)
    private TextView reset;
    @ViewInject(R.id.send)
    private TextView send;
    @ViewInject(R.id.tips)
    private TextView tips;
    
    private String juxinliToken;
    private String phoneNum;
    private String website;
    private int status = -1;
    private boolean submitStatus = true;//提交按钮的状态，true时为正常提交，false时为动态码提交，默认为true
    
    public static final int REQUEST_JDTAOBAO = 8;
    public static final int REQUEST_RESET = 9;
    
    private Dialog loadingDialog;
    private VerifyCodeTimer timer;

    private int errorTimes;
    
    @OnClick({R.id.submit,R.id.server, R.id.send})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (submitStatus) {
                    collectMobile("");
                } else {
                    collectMobile("SUBMIT_CAPTCHA");
                }
                break;
            case R.id.server:
                Intent intent = new Intent(PhoneVerifyActivity.this,
                    ResetServePasswordActivity.class);
                intent.putExtra("token", juxinliToken);
                intent.putExtra("phone", phoneNum);
                intent.putExtra("website", website);
                intent.putExtra("reset_pwd_method", status);
                startActivityForResult(intent, REQUEST_RESET);
                break;
            case R.id.send:
                collectMobile("RESEND_CAPTCHA");
                break;    
            default:
                break;
        }
    }    
    
    private void collectMobile(final String type) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("token", juxinliToken);
        params.put("account", phoneNum);
        if (Utils.isNotEmptyString(password.getText().toString())) {
            params.put("password", password.getText().toString());
        }else {
            params.put("password", "");
        }
        if (Utils.isNotEmptyString(verify.getText().toString())) {
            params.put("captcha", verify.getText().toString());
            Logger.e("captcha", "ca"+verify.getText().toString()+"ca");
        }else {
            params.put("captcha", "");
            Logger.e("captcha", "ca"+""+"ca");
        }
        
        params.put("type", type);
        params.put("website", website);
        httpClient.post(Constants.API_JUXINLI_COLLECT, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    button.setEnabled(false);
                    send.setEnabled(false);
                    loadingDialog.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("juxinli_collect", statusCode + " "
                        + responseString);
                    button.setEnabled(true);
                    send.setEnabled(true);
                    loadingDialog.dismiss();
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                            if (type.equals("RESEND_CAPTCHA")&&timer != null) {
                                    timer.start();
                            }
                            JSONObject result = response
                                .getJSONObject("result");
                            if (result.getBoolean("finish")) {
                                confirmFinish();
                            } else {
                                switch (result.getInt("process_code")) {
                                    case 10002:
                                        Utils
                                            .MakeToast(
                                                PhoneVerifyActivity.this,
                                                "请输入验证码");
                                        send.setVisibility(View.VISIBLE);
                                        verify.setVisibility(View.VISIBLE);
                                        tips.setText("请输入验证码");
                                        password.setVisibility(View.INVISIBLE);
                                        submitStatus = false;
                                        break;
                                    case 10008:
                                        Intent intent = new Intent(
                                            PhoneVerifyActivity.this,
                                            ElectricBusinessActivity.class);
                                        intent.putExtra("token", juxinliToken);
                                        intent.putExtra("website", result
                                            .getJSONObject("next_datasource")
                                            .getString("website"));
                                        startActivityForResult(intent,
                                            REQUEST_JDTAOBAO);
                                        break;
                                    case 0:
                                        Utils.MakeToast(
                                            PhoneVerifyActivity.this,
                                            "采集请求超时");
                                        Utils.customDialog(PhoneVerifyActivity.this,"运营商暂不支持查询服务，请您继续完成其他申请步骤", new DialogListenner() {
                                            
                                            @Override
                                            public void confirm() {
                                                confirmFinish();
                                            }
                                        });
                                        break;
                                    case 30000:
                                    case 31000:
                                        Utils.MakeToast(
                                            PhoneVerifyActivity.this,
                                            result.getString("content"));
                                        Utils.customDialog(PhoneVerifyActivity.this,"运营商暂不支持查询服务，请您继续完成其他申请步骤", new DialogListenner() {
                                            
                                            @Override
                                            public void confirm() {
                                                confirmFinish();
                                            }
                                        });
                                        break;
                                    case 10003:    
                                    case 10007:
                                        Utils.MakeToast(
                                            PhoneVerifyActivity.this,
                                            result.getString("content"));
                                        dealErrorTime();
                                        break;
                                    default:
                                        Utils.MakeToast(
                                            PhoneVerifyActivity.this,
                                            result.getString("content"));
                                        break;
                                }
                            }
                        } else {
                            Utils.MakeToast(PhoneVerifyActivity.this, "采集失败");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.MakeToast(PhoneVerifyActivity.this, "服务器错误");
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("juxinli_collect", statusCode
                        + " juxinli_collect " + responseString);
                    button.setEnabled(true);
                    send.setEnabled(true);
                    loadingDialog.dismiss();
                    Utils
                    .MakeToast(
                        PhoneVerifyActivity.this,
                        "连接超时，请重试");
                    new RequestFailureHandler(PhoneVerifyActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                collectMobile(type);
                            }
                        }).handleMessage(statusCode);
                }
            });
    }    
    
    private void confirmFinish() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        if (UcreditDreamApplication.applyState!=null) {
            params.put("applicationId", UcreditDreamApplication.applyState.getId());
        }
        httpClient.post(Constants.JUXINLI_FINISH, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("juxinli_finish", statusCode + " "
                        + responseString);
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                            Utils.MakeToast(PhoneVerifyActivity.this, "认证完成");
                            setResult(ApplyActivity.APPLY_JXL_CODE);
                            finish();
                        } else {
                            Utils.MakeToast(PhoneVerifyActivity.this, response
                                .getJSONObject("error").getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("juxinli_finish", statusCode + " juxinli_finish "
                        + responseString);
                    Utils
                    .MakeToast(
                        PhoneVerifyActivity.this,
                        "连接超时，请重试");
                }
            });
    }    
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "手机验证";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_phoneverify;
    }

    
    
    @Override
    protected void onStart() {
        super.onStart();
        errorTimes=0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        loadingDialog = Utils.showProgressDialog(PhoneVerifyActivity.this,
            "提交中...", true);
        new EditTextUtils(password, this).setPassword();
        juxinliToken = getIntent().getStringExtra("token");
        phoneNum = getIntent().getStringExtra("phone");
        phone.setText(phoneNum);
        website = getIntent().getStringExtra("website");
        status = getIntent().getIntExtra("reset_pwd_method", -1);
        if (0 == status) {
            reset.setVisibility(View.GONE);
            tips.setText("如忘记服务密码，请直接点击下一步将动态密码发送至手机");
        }
        timer = new VerifyCodeTimer(60000, 1000, send);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_JDTAOBAO:
                if (Activity.RESULT_OK == resultCode) {
                    setResult(ApplyActivity.APPLY_JXL_CODE);
                    finish();
                }else if (ElectricBusinessActivity.RESULT_BACK==resultCode) {
                    setResult(ElectricBusinessActivity.RESULT_BACK);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void dealErrorTime() {
        if (errorTimes>=2) {
            Utils.customDialog(PhoneVerifyActivity.this,"系统检测到您失败次数过多，是否跳过该流程", new DialogListenner() {
                
                @Override
                public void confirm() {
                    confirmFinish();
                }
            });
        } else {
            errorTimes++;
        }
    }    
    
}
