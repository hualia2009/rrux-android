package com.ucredit.dream.ui.activity.apply;

import java.io.File;

import org.apache.http.Header;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.custom.widgt.CustomEditText;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ApplyStudyActivity extends BaseActivity {
	@ViewInject(R.id.username)
	private EditText username;
	@ViewInject(R.id.password)
	private CustomEditText password;
	@ViewInject(R.id.schoolName)
	private EditText schoolName;
	@ViewInject(R.id.low_layout)
	private LinearLayout lowLayout;
	@ViewInject(R.id.high_layout)
	private LinearLayout highLayout;
	@ViewInject(R.id.agree)
	private Button agree;
	@ViewInject(R.id.forget)
	private TextView forget;
	
	//验证码
	@ViewInject(R.id.verifyLayout)
	private LinearLayout verifyLayout;
	@ViewInject(R.id.verifyCode)
	private EditText verifyCode;
	@ViewInject(R.id.verifyImage)
	private ImageView verifyImage;
	
	@ViewInject(R.id.study_notice)
	private TextView studyNotice;
	int selectIndex = 2;
	private int graduation = 1;
	File xueLiFile = null;
	
	
    private EditTextUtils editTextUtils = null;
	
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		
		TextWatcherListener changedListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                if(selectIndex<4 && Utils.isNotEmptyString(username.getText().toString()) && 
                        Utils.isNotEmptyString(password.getText().toString())){
                    agree.setEnabled(true);
                }else if(selectIndex >= 4 && Utils.isNotEmptyString(schoolName.getText().toString()) 
                        && schoolName.getText().toString().length() >=4){
                    agree.setEnabled(true);
                }else{
                    agree.setEnabled(false);
                }
            }
        });
		schoolName.addTextChangedListener(changedListener);
		username.addTextChangedListener(changedListener);
		password.addTextChangedListener(changedListener);
		
		//根据上个页面的学历初始化界面
		initViewByDiploma(getIntent().getIntExtra("diplomaWhich", 2));
		graduation = getIntent().getIntExtra("graduation", 1);//默认选择是已毕业
		
        editTextUtils = new EditTextUtils(password, this);
        editTextUtils.setPassword();
		
		handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1001){
                    String imageStr = (String) msg.obj;
                    verifyLayout.setVisibility(View.VISIBLE);
                    verifyImage.setImageBitmap(Utils.base64ToBitmap(imageStr));
                }else if(msg.what == 1002){
                    verifyCode.setText("");
                    verifyLayout.setVisibility(View.GONE);
                    Utils.MakeToast(ApplyStudyActivity.this, "请求出错，请重新提交！");
                }
            }
		    
		};
	}

	@OnClick({ R.id.agree,R.id.regist_xuexin,R.id.forget,R.id.verifyImage})
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.agree:
			Utils.customDialog(this, "已上传资料将无法再进行更改，请确认是否提交？", new DialogListenner() {
				@Override
				public void confirm() {
					submitData();
				}
			});
			break;
		case R.id.forget:
		    Intent intent = new Intent(ApplyStudyActivity.this, AdvertiseActivity.class);
            intent.putExtra("title", "找回密码");
            intent.putExtra("url", Constants.APPLICATION_STUDY_RETRIVE);
            intent.putExtra("zoom", true);
            startActivity(intent);
		    break;
		case R.id.verifyImage:
		    refreshVerifyCode();
		    break;
		case R.id.regist_xuexin:
		    Intent intentXuexin = new Intent(ApplyStudyActivity.this, AdvertiseActivity.class);
		    intentXuexin.putExtra("title", "注册学信网");
		    intentXuexin.putExtra("url", Constants.APPLICATION_STUDY_REGIST);
		    intentXuexin.putExtra("zoom", true);
            startActivity(intentXuexin);
		    break;
		default:
			break;
		}
	}
	
	/**
	 * 刷新图片验证码
	 */
	private void refreshVerifyCode() {
	    AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.post(Constants.APPLICATION_STUDY_REFRESH_VERIFY,
            new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						refreshVerifyCode();
    						
    					}
                		
                	});                    
                    setMask(true);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("notify_upload failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(ApplyStudyActivity.this,
                        new GetTokenListener() {
                            @Override
                            public void onSuccess() {
                                refreshVerifyCode();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("notify_upload success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if(response.getJSONObject("result") != null && 
                            response.getJSONObject("result").containsKey("image")){
                        String imageStr = response.getJSONObject("result").getString("image");
                        Message msg = new Message();
                        msg.obj = imageStr;
                        msg.what = 1001;
                        handler.sendMessage(msg);
                    }
                }

            });
    }

    @Override
    protected void setRightButton(ImageView imageView) {
	    imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.register_xuexin);
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ApplyStudyActivity.this, AdvertiseActivity.class);
                intent.putExtra("title", "注册学信网");
                intent.putExtra("url", Constants.APPLICATION_STUDY_REGIST);
                intent.putExtra("zoom", true);
                startActivity(intent);
            }
        });
    }

    @Override
    protected boolean hasRightButton() {
        return false;
    }

    /**
	 * 提交数据
	 */
	public void submitData(){
		if(selectIndex<4){//需要填学信网账号的情况
		    if(!Utils.isNotEmptyString(username.getText().toString())){
		        Utils.MakeToast(ApplyStudyActivity.this,"请您输入账号");
		        return;
		    }
		    if(!Utils.isNotEmptyString(password.getText().toString())){
		        Utils.MakeToast(ApplyStudyActivity.this,"请您输入密码");
		        return;
		    }
		}else{//需要填写学校的情况
			if(!Utils.isNotEmptyString(schoolName.getText().toString())){
				Utils.MakeToast(ApplyStudyActivity.this,"请您填写学校名称");
				return;
			}
		}
		submit();
	}
	
	
	/**
	 * 提交数据
	 * @param xueLiId
	 */
	private void submit(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(UcreditDreamApplication.applyState != null){
            params.put("applicationId", ""+UcreditDreamApplication.applyState.getId());
        }
        if(Utils.isNotEmptyString(verifyCode.getText().toString())){
            params.put("captcha", verifyCode.getText().toString());
        }
        params.put("current", graduation);//当前情况，在读还是毕业
        params.put("diploma", "");//毕业证附件id
        params.put("education", selectIndex);
        params.put("account", username.getText().toString());
        params.put("password", password.getText().toString());
        params.put("schoolName", schoolName.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.APPLICATION_STUDY_SUBMIT, params,
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
                    Logger.e("notify_upload failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(ApplyStudyActivity.this,
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
                    Logger.e("notify_upload success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if(Utils.isNotEmptyString(response.getString("result")) && 
                            response.getJSONObject("result").containsKey("image")){
                        Utils.MakeToast(ApplyStudyActivity.this, "您的账号和密码存在隐患，请输入验证码");
                        String imageStr = response.getJSONObject("result").getString("image");
                        Message msg = new Message();
                        msg.obj = imageStr;
                        msg.what = 1001;
                        handler.sendMessage(msg);
                        return;
                    }
                    if (response.getBooleanValue("success")) {
                        Utils.MakeToast(ApplyStudyActivity.this, "学籍信息保存成功");
                        setResult(ApplyActivity.APPLY_STUDY_CODE);
                        finish();
                    } else {
                        editTextUtils.setEditTextVisible();
                        Utils.MakeToast(ApplyStudyActivity.this, response
                            .getJSONObject("error").getString("message"));
                        if(Utils.isNotEmptyString(response.getJSONObject("error").getString("code"))
                                && response.getJSONObject("error").getIntValue("code") == 4){
                            refreshVerifyCode();
                        }
                        if(Utils.isNotEmptyString(response.getJSONObject("error").getString("code"))
                                && response.getJSONObject("error").getIntValue("code") == 5){
                        }
                        if(Utils.isNotEmptyString(response.getJSONObject("error").getString("code"))
                                && response.getJSONObject("error").getIntValue("code") == 6){
                            Message msg = new Message();
                            msg.what = 1002;
                            handler.sendMessage(msg);
                        }
                    }
                }

            });
    }
	
	public void initViewByDiploma(int newValue) {
		selectIndex = newValue;
		if (selectIndex > 3) {//显示拍照
			this.highLayout.setVisibility(View.GONE);
			this.lowLayout.setVisibility(View.VISIBLE);
		} else {
			this.lowLayout.setVisibility(View.GONE);
			this.highLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected boolean hasTitle() {
		return true;
	}

	@Override
	protected CharSequence getPublicTitle() {
		return "学籍信息";
	}

	@Override
	protected int getContentId() {
		return R.layout.activity_apply_study;
	}

}
