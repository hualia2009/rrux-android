package com.ucredit.dream.ui.activity.juxinli;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.VerifyCodeTimer;




public class ElectricBusinessActivity extends BaseActivity {

    @ViewInject(R.id.logo)
    private ImageView logo;
    
    @ViewInject(R.id.password)
    private CustomEditText password;
    @ViewInject(R.id.name)
    private EditText account;
    @ViewInject(R.id.next)
    private TextView button;
    
    @ViewInject(R.id.send)
    private TextView sendVerify;
    @ViewInject(R.id.input)
    private EditText verify;
    
    private String juxinliToken;
    private String website;

    private Dialog loadingDialog;
    private VerifyCodeTimer timer;
    
    public static final int RESULT_BACK=2; 
    
    @OnClick({R.id.next,R.id.send,R.id.skip})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.next:
                if (inputCheck()) {
                    collectBusiness("SUBMIT_CAPTCHA");
                }
                break;
            case R.id.send:
                collectBusiness("RESEND_CAPTCHA");
                break;
            case R.id.skip:
                Utils.customDialog(this, "电商认证有助于您提升审核速度和借款额度，确认跳过此步验证吗？", new DialogListenner() {
                    
                    @Override
                    public void confirm() {
                        skip();
                    }
                });
                break;

            default:
                break;
        }
    }
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(account.getText().toString())) {
            Utils.MakeToast(this, "请输入您的账号");
            return false;
        }
        
        if (!Utils.isNotEmptyString(password.getText().toString())) {
            Utils.MakeToast(this, "请输入您的密码");
            return false;
        }
        return true;
    }   
    
    private void skip() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("token", juxinliToken);
        params.put("website", website);
        Logger.e("skipdianshang", juxinliToken+"    "+website);
        httpClient.post(Constants.API_JUXINLI_SKIPBUSINESS, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("juxinli_skip", statusCode + " "
                        + responseString);
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                            JSONObject result = response.getJSONObject("result");
                            if (result.getBoolean("finish")) {
                                confirmFinish();
                            } else {
                                Intent intent = new Intent(
                                    ElectricBusinessActivity.this,
                                    ElectricBusinessActivity.class);
                                intent.putExtra("token", juxinliToken);
                                intent.putExtra("website", result
                                    .getJSONObject("next_datasource")
                                    .getString("website"));
                                startActivityForResult(
                                    intent,
                                    PhoneVerifyActivity.REQUEST_JDTAOBAO);
                            }
                        } else {
                            Utils.MakeToast(ElectricBusinessActivity.this, response
                                .getJSONObject("error").getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("juxinli_skip", statusCode + " juxinli_skip "
                        + responseString);
                    Utils
                    .MakeToast(
                        ElectricBusinessActivity.this,
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
        return "京东账号";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_electricbusiness;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        loadingDialog = Utils.showProgressDialog(ElectricBusinessActivity.this,
            "提交中...", true);
        new EditTextUtils(password, this).setPassword();
        TextView back = (TextView) findViewById(R.id.title_left);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                setResult(RESULT_BACK);
                finish();
                Utils.exitActivityAnimation(ElectricBusinessActivity.this);
            }
        });
        juxinliToken = getIntent().getStringExtra("token");
        website = getIntent().getStringExtra("website");
        if ("jingdong".equals(website)) {
            logo.setImageResource(R.drawable.jingdong);
        } else if ("taobao".equals(website)) {
            logo.setImageResource(R.drawable.taobao);
            TextView textView = (TextView) findViewById(R.id.title);
            textView.setText("淘宝账号");
        } else {
            Utils.MakeToast(this, "暂不支持的电商");
            finish();
        }
        timer = new VerifyCodeTimer(60000, 1000, sendVerify);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_BACK);
        finish();
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
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("juxinli_finish", statusCode + " "
                        + responseString);
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                            Utils.MakeToast(ElectricBusinessActivity.this,
                                "认证完成");
                            setResult(Activity.RESULT_OK);
                            finish();
                        } else {
                            Utils.MakeToast(ElectricBusinessActivity.this,
                                response.getJSONObject("error")
                                    .getString("message"));
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
                        ElectricBusinessActivity.this,
                        "连接超时，请重试");
                }
            });
    }

    private void collectBusiness(final String type) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        params.put("token", juxinliToken);
        if (password.getEditableText().toString() == null) {
            params.put("password", "");
        } else {
            params.put("password", password.getEditableText().toString());
        }
        if (account.getEditableText().toString() == null) {
            params.put("account", "");
        } else {
            params.put("account", account.getEditableText().toString());
        }
        if (verify.getEditableText().toString() != null) {
            params.put("captcha", verify.getEditableText().toString());
        }
        params.put("type", type);
        params.put("website", website);
        httpClient.post(Constants.API_JUXINLI_COLLECT, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    button.setEnabled(false);
                    loadingDialog.show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("juxinli_collect", statusCode + " "
                        + responseString);
                    button.setEnabled(true);
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
                                        sendVerify
                                            .setVisibility(View.VISIBLE);
                                        verify.setVisibility(View.VISIBLE);
                                        break;
                                    case 10008:
                                        Intent intent = new Intent(
                                            ElectricBusinessActivity.this,
                                            ElectricBusinessActivity.class);
                                        intent.putExtra("token", juxinliToken);
                                        intent.putExtra("website", result
                                            .getJSONObject("next_datasource")
                                            .getString("website"));
                                        startActivityForResult(
                                            intent,
                                            PhoneVerifyActivity.REQUEST_JDTAOBAO);
                                        break;
                                    case 10007:
                                        Utils.MakeToast(
                                            ElectricBusinessActivity.this,
                                            "简单密码或初始密码无法登录");
                                        break;
                                    default:
                                        Utils.MakeToast(
                                            ElectricBusinessActivity.this,
                                            result.getString("content"));
                                        break;
                                }
                            }
                        } else {
                            Utils.MakeToast(ElectricBusinessActivity.this,
                                response.getJSONObject("error")
                                .getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.MakeToast(ElectricBusinessActivity.this, "服务器错误");
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("juxinli_collect", statusCode
                        + " juxinli_collect " + responseString);
                    button.setEnabled(true);
                    loadingDialog.dismiss();
                    Utils
                    .MakeToast(
                        ElectricBusinessActivity.this,
                        "连接超时，请重试");
                    new RequestFailureHandler(ElectricBusinessActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                            }
                        }).handleMessage(statusCode);
                }
            });
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
            case PhoneVerifyActivity.REQUEST_JDTAOBAO:
                if (Activity.RESULT_OK == resultCode) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }else if (RESULT_BACK==resultCode) {
                    setResult(RESULT_BACK);
                    finish();
                }
                break;
            default:
                break;
        }
    }
    
}
