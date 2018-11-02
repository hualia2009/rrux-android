package com.ucredit.dream.ui.activity.juxinli;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
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
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.VerifyCodeTimer;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ResetServePasswordActivity extends BaseActivity {

    @ViewInject(R.id.input)
    private EditText verify;
    @ViewInject(R.id.password)
    private CustomEditText password;
    @ViewInject(R.id.send)
    private TextView send;
    @ViewInject(R.id.submit)
    private TextView button;
    
    private String juxinliToken;
    private String phoneNum;
    private String website;
    private int status = -1;

    private Dialog loadingDialog;
    private VerifyCodeTimer timer;
    
    @OnClick({R.id.submit,R.id.send})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                resetMobile("SUBMIT_RESET_PWD");
                break;
            case R.id.send:
                resetMobile("RESEND_RESET_PWD_CAPTCHA");
                break;
            default:
                break;
        }
    }       
    
    
    private void resetMobile(final String type) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("token", juxinliToken);
        params.put("account", phoneNum);
        if (password.getEditableText().toString() == null) {
            params.put("password", "");
        } else {
            params.put("password", password.getEditableText().toString());
        }
        if (verify.getEditableText().toString() == null) {
            params.put("captcha", "");
        } else {
            params.put("captcha", verify.getEditableText().toString());
        }
        params.put("type", type);
        params.put("website", website);
        httpClient.post(Constants.API_JUXINLI_RESET, params,
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
                    Logger
                        .e("juxinli_reset", statusCode + " " + responseString);
                    button.setEnabled(true);
                    send.setEnabled(true);
                    loadingDialog.dismiss();
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                            if (type.equals("RESEND_RESET_PWD_CAPTCHA")&&timer != null) {
                                timer.start();
                        }
                            JSONObject result = response
                                .getJSONObject("result");
                            if (result.getBoolean("finish")) {
                                Utils.MakeToast(ResetServePasswordActivity.this,
                                    "认证完成");
                                setResult(Activity.RESULT_OK);
                                finish();
                            } else {
                                switch (result.getInt("process_code")) {
                                    case 11000:
                                        Utils.MakeToast(
                                            ResetServePasswordActivity.this,
                                            "重置密码成功");
                                        finish();
                                        break;
                                    case 11001:
                                        Utils.MakeToast(
                                            ResetServePasswordActivity.this,
                                            "新密码格式错误");
                                        break;
                                    case 30000:
                                        Utils.MakeToast(
                                            ResetServePasswordActivity.this,
                                            "网络或运营商异常");
                                        break;
                                    case 31000:
                                        Utils.MakeToast(
                                            ResetServePasswordActivity.this,
                                            "重置密码失败，请到营业厅重置");
                                        break;
                                    default:
                                        Utils.MakeToast(ResetServePasswordActivity.this, result.getString("content"));
                                        break;
                                }
                            }
                        } else {
                            Utils
                                .MakeToast(ResetServePasswordActivity.this, response.getJSONObject("error")
                                    .getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.MakeToast(ResetServePasswordActivity.this, "服务器错误");
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("juxinli_reset", statusCode + " juxinli_reset "
                        + responseString);
                    button.setEnabled(true);
                    send.setEnabled(true);
                    loadingDialog.dismiss();
                    Utils
                    .MakeToast(
                        ResetServePasswordActivity.this,
                        "连接超时，请重试");
                    new RequestFailureHandler(ResetServePasswordActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                            }
                        }).handleMessage(statusCode);
                }
            });
    }    
    
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "服务密码重置";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_resetservepassword;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        loadingDialog = Utils.showProgressDialog(ResetServePasswordActivity.this,
            "提交中...", true);
        new EditTextUtils(password, this).setPassword();
        juxinliToken = getIntent().getStringExtra("token");
        phoneNum = getIntent().getStringExtra("phone");
        website = getIntent().getStringExtra("website");
        status = getIntent().getIntExtra("reset_pwd_method", -1);
        if (2 == status) {
            password.setVisibility(View.VISIBLE);
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
    
}
