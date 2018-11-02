package com.ucredit.dream.ui.activity.apply;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.ui.activity.validate.IDScanActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import exocr.exocrengine.EXIDCardResult;
import exocr.idcard.CaptureActivity;

public class ApplyActivity extends BaseActivity {
	
	public static final int APPLY_CONTACT_CODE = 100;
	public static final int APPLY_JXL_CODE = 101;
	public static final int APPLY_STUDY_CODE = 102;
	public static final int APPLY_ID_CODE = 103;

    @ViewInject(R.id.cancel)
    private Button cancel;
    @ViewInject(R.id.agree)
    private Button agree;
    
    @ViewInject(R.id.apply_id_layout)
    private RelativeLayout applyIdLayout;
    @ViewInject(R.id.apply_contact_layout)
    private RelativeLayout applyContactLayout;
    @ViewInject(R.id.apply_study_layout)
    private RelativeLayout applyStudyLayout;
    
    @ViewInject(R.id.apply_id_done)
    private ImageView applyIdDone;
    @ViewInject(R.id.apply_contact_done)
    private ImageView applyContactDone;
    @ViewInject(R.id.apply_study_done)
    private ImageView applyStudyDone;
    
    // 用户资料状态
    UserState state = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        this.state = UcreditDreamApplication.mUser.getUserState();
        updateSuccessState();
    }

    @OnClick({ R.id.agree, R.id.apply_id_layout, R.id.apply_contact_layout,
        R.id.apply_study_layout })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agree:
            	if(checkSubmit()){
	                Utils.customDialog(this, "已上传资料将无法再进行更改，请确认是否提交？", new DialogListenner() {
	                    @Override
	                    public void confirm() {
	                        submitData();
	                    }
	                });
            	}else{
            		Utils.MakeToast(ApplyActivity.this, "请先填写完成联络资料、学籍信息");
            	}
                break;
            case R.id.apply_id_layout:
                if(!state.isIdcard() || !state.isIdentify()){
                    Intent intent=new Intent(this, CaptureActivity.class);
                    intent.putExtra("type", "front");
                    startActivityForResult(intent, IDScanActivity.REQUESTCODE_SCAN_IDCARD);
                }
                break;
            case R.id.apply_contact_layout:
            	if(!state.isUserInfoMapContact()){
            	    toActivityForResult(ApplyContactActivity.class,APPLY_CONTACT_CODE);
            	}
                break;
            case R.id.apply_study_layout:
            	if(!state.isUserInfoMapEducation()){
            		toActivityForResult(ApplyPreStudyActivity.class,APPLY_STUDY_CODE);
            	}
                break;
            default:
                break;
        }
    }
    /**
     * 检查状态是否成功
     * @return
     */
    public boolean checkSubmit(){
    	return !state.isUserInfo()&&state.isUserInfoMapContact()&&state.isUserInfoMapEducation()&&state.isIdcard()&&state.isIdentify();
    }
    
    /**
     * 更新状态标记
     */
    private void updateSuccessState(){
        updateViewState();
       	UcreditDreamApplication.mUser.setUserState(state);//回执状态
       	Logger.e("state", state.isUserInfo()+"------");
       	if(checkSubmit()){
       		agree.setEnabled(true);
       	}else{
       		agree.setEnabled(false);
       	}
    }
    
    private void updateViewState() {
        if(state.isIdcard() && state.isIdentify()){//设置身份证完成
            applyIdLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            applyIdDone.setVisibility(View.VISIBLE);  
            
            applyContactLayout.setEnabled(true);
            applyContactLayout.setBackgroundResource(R.drawable.apply_btn_bg);
            applyStudyLayout.setEnabled(true);
            applyStudyLayout.setBackgroundResource(R.drawable.apply_btn_bg);
        }else{//必须先完成身份验证
            applyContactLayout.setBackgroundResource(R.drawable.apply_btn_bg_disable);
            applyContactLayout.setEnabled(false);
            applyStudyLayout.setBackgroundResource(R.drawable.apply_btn_bg_disable);
            applyStudyLayout.setEnabled(false);
            return;
        }
        
        if(state.isUserInfoMapContact()){//设置联系人背景颜色，同时显示完成图标
            applyContactLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            applyContactDone.setVisibility(View.VISIBLE);
        }
        
        if(state.isUserInfoMapEducation()){//设置学籍信息背景颜色
            applyStudyLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            applyStudyDone.setVisibility(View.VISIBLE);            
        }
        
    }

    /**
     * 接收通知状态变更
     */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        //如果是扫描身份证，跳转到扫描身份证页面，并且将已扫描的信息传递到下个页面
        if(IDScanActivity.REQUESTCODE_SCAN_IDCARD == requestCode && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            if (extras == null)
                throw new IllegalStateException("Didn't find any extras!");
            EXIDCardResult capture = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
            //判断是否为黑白照片
            int nColorType = extras.getInt("nColorType");
            if(nColorType == 0){
                Utils.MakeToast(ApplyActivity.this, Constants.ID_SCAN_TIP);
                return;
            }
            int type = 1;
            if(capture != null){
                type = capture.type;
                if(type == 1){
                    Intent intent=new Intent(this, IDScanActivity.class);
                    intent.putExtra("name", ""+capture.name);
                    intent.putExtra("idCard", ""+capture.cardnum);
                    intent.putExtra("address",""+capture.address);
                    startActivityForResult(intent, IDScanActivity.REQUESTCODE_SCAN_IDCARD);
                    return;
                }else{
                    Utils.MakeToast(ApplyActivity.this, "请选择身份证正面拍摄");
                }
            }else{
                return;
            }
        }
        
        if(resultCode == APPLY_ID_CODE){
            state.setIdentify(true);
            state.setIdcard(true);
            updateSuccessState();
            return;
        }
        
        if(requestCode!=resultCode){
        	return;
        }
        switch (requestCode) {
        case APPLY_CONTACT_CODE:
        	state.setUserInfoMapContact(true);
        	updateSuccessState();
            break;
        case APPLY_STUDY_CODE:
        	state.setUserInfoMapEducation(true);
        	updateSuccessState();
            break;
        default:
            break;
        }
    }
	
	private void submitData(){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params=new RequestParams();
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.APPLICATION_LEND_SUBMIT,params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						submitData();
    						
    					}
                		
                	});                    
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                	Logger.e("submit userinfo failure", statusCode + " "
							+ responseString);
                	setMask(false);
					new RequestFailureHandler(ApplyActivity.this,
							new GetTokenListener() {
								@Override
								public void onSuccess() {
									submitData();
								}
							}).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("submit_userinfo_success", statusCode + " "
                        + responseString);
                    setMask(false);
                    JSONObject response;
                    try {
                        response = new JSONObject(responseString);
                        if (response.getBoolean("success")) {
                        	Utils.MakeToast(ApplyActivity.this,"您的资料信息已成功提交！");
                        	state.setUserInfo(true);
                        	updateSuccessState();
                        	setResult(RESULT_OK);
                        	finish();
                        } else {
                        	Utils.MakeToast(ApplyActivity.this, response
                                    .getJSONObject("error").getString("message"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.MakeToast(ApplyActivity.this, "请求失败");
                    }

                }
            });
    }

    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "申请借款";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_apply_money;
    }

}
