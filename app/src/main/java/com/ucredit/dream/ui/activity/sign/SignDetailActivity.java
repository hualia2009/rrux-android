package com.ucredit.dream.ui.activity.sign;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.ui.activity.BankCardAuthorizeActivity;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.request.UserStateRequest;
import com.ucredit.dream.utils.request.UserStateRequest.QueryUserListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import exocr.bankcard.CardRecoActivity;
import exocr.bankcard.EXBankCardInfo;

public class SignDetailActivity extends BaseActivity {

    @ViewInject(R.id.advertise_detail_content)
    private WebView webView;
    @ViewInject(R.id.cancel)
    private Button cancel;
    @ViewInject(R.id.agree)
    private Button agree;
    
    @ViewInject(R.id.sign_borrow_done)
    private ImageView protocolBorrowDone;
    @ViewInject(R.id.sign_credit_done)
    private ImageView protocolCreditDone;
    @ViewInject(R.id.sign_huakou_done)
    private ImageView protocolHuakouDone;
    
    @ViewInject(R.id.sign_borrow_layout)
    private RelativeLayout signBorrowLayout;
    @ViewInject(R.id.sign_credit_layout)
    private RelativeLayout signCreditLayout;
    @ViewInject(R.id.sign_huakou_layout)
    private RelativeLayout signHuakouLayout;
    @ViewInject(R.id.sign_borrow_txt)
    private TextView borrowTitle;
    
    private static final int REQUESTCODE_PHOTO_HUAKOU = 40;
    
    File huakouFile = null;
    
    private String tempTitle = "借款协议";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_OK);
        ViewUtils.inject(this);
        
        boolean isWe = getIntent().getBooleanExtra("we", false);
        if(isWe){
            tempTitle = "授权委托书";
            borrowTitle.setText(tempTitle);
        }
        
        showAgreeDialog();
    }
    
    private void showAgreeDialog(){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.show();
        Window window = dialog.getWindow();
        View view = LayoutInflater.from(this).inflate(R.layout.alert_dialog,
            null);
        window.setContentView(view);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        LayoutParams params = window.getAttributes();
        Point point = new Point();
        display.getSize(point);
        params.width = (int) (point.x * 0.8);
        window.setAttributes(params);

        TextView titleTextView = (TextView) window.findViewById(R.id.title);
        TextView contentTextView = (TextView) window.findViewById(R.id.content);
        Button confirm = (Button) window.findViewById(R.id.confirm);
        Button cancel = (Button) window.findViewById(R.id.cancel);

        titleTextView.setText("温馨提示");
        contentTextView.setText("签署协议后，本人的贷款行为已受到法律保护，合同即日生效。本人主动放弃贷款所产生的违约费用，将由本人承担。");
        confirm.setText("确认");
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        cancel.setText("返回");
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.cancel();
                finish();
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        getUserState();
    }
    
    private void getUserState(){
        new UserStateRequest(this, new QueryUserListener() {

            @Override
            public void onSuccess(String responseString) {
                UcreditDreamApplication.mUser.setUserState((UserState
                    .parseUserState(responseString)));
                initProtocolState();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }
        }).queryUser();
    }
    
    private void initProtocolState(){
        if(UcreditDreamApplication.mUser.getUserState().isSignMapTransfer()){
            protocolHuakouDone.setVisibility(View.VISIBLE);
            signHuakouLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            signHuakouLayout.setEnabled(false);
            //划扣授权书完成，下面两个按钮可以点击，并且改变颜色
            signBorrowLayout.setEnabled(true);
            signBorrowLayout.setBackgroundResource(R.drawable.apply_btn_bg);
            signCreditLayout.setEnabled(true);
            signCreditLayout.setBackgroundResource(R.drawable.apply_btn_bg);
        }else{
            signBorrowLayout.setBackgroundResource(R.drawable.apply_btn_bg_disable);
            signBorrowLayout.setEnabled(false);
            signCreditLayout.setBackgroundResource(R.drawable.apply_btn_bg_disable);
            signCreditLayout.setEnabled(false);
            return; //必须先完成划扣
        }
        if(UcreditDreamApplication.mUser.getUserState().isSignMapLend()){
            protocolBorrowDone.setVisibility(View.VISIBLE);
            signBorrowLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            signBorrowLayout.setEnabled(false);
        }
        if(UcreditDreamApplication.mUser.getUserState().isSignMapCredit()){
            protocolCreditDone.setVisibility(View.VISIBLE);
            signCreditLayout.setBackgroundResource(R.drawable.apply_btn_bg_done);
            signCreditLayout.setEnabled(false);
        }
    }
    
    @OnClick({R.id.cancel,R.id.agree,R.id.sign_borrow_layout,R.id.sign_credit_layout,R.id.sign_huakou_layout})
    public void onClick(View view){
        Intent intent = new Intent(this, AdvertiseActivity.class);
        boolean hasSigned;
        switch (view.getId()) {
            case R.id.agree:
                signConfirm();
                break;
            case R.id.sign_borrow_layout:
                hasSigned = UcreditDreamApplication.mUser.getUserState().isSignMapLend();
                intent.putExtra("title", tempTitle);
                intent.putExtra("url", Constants.API_SIGN_PROTOCOL+"?applyid="
                    + UcreditDreamApplication.applyState.getId()
                    +"&type=4&protocolType=LOAN&access_token="
                    + UcreditDreamApplication.token
                    + "&clientid="
                    + UcreditDreamApplication.clientId
                    + "&hassigned="
                    + hasSigned);
                startActivity(intent);
                break;
            case R.id.sign_credit_layout:
                hasSigned = UcreditDreamApplication.mUser.getUserState().isSignMapCredit();
                intent.putExtra("title", "信用服务协议");
                intent.putExtra("url", Constants.API_SIGN_PROTOCOL+"?applyid="
                    + UcreditDreamApplication.applyState.getId()
                    +"&type=3&protocolType=CREDIT&access_token="
                    + UcreditDreamApplication.token
                    + "&clientid="
                    + UcreditDreamApplication.clientId
                    + "&hassigned="
                    + hasSigned);
                startActivity(intent);
                break;
            case R.id.sign_huakou_layout:
            	Intent intentScan=new Intent(this, CardRecoActivity.class);
            	intentScan.putExtra("type", "NoActivityResult");
            	startActivity(intentScan);
//                startActivityForResult(intentScan, BankCardAuthorizeActivity.REQUEST_SCAN);
                break;
            default:
                break;
        }
    }
    
    private void signConfirm() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("lendId", UcreditDreamApplication.applyState.getId());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_SIGN_CONFIRM, params,
            new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("signConfirm failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(SignDetailActivity.this,
                        new GetTokenListener() {
                            @Override
                            public void onSuccess() {
                                signConfirm();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("signConfirm success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        Utils.MakeToast(SignDetailActivity.this, "签约完成");
                        finish();
                    } else {
                        Utils.MakeToast(SignDetailActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_PHOTO_HUAKOU:
            	if (RESULT_OK == resultCode) {
            		File file = FileUtil.handleImage(FileUtil.FILE_NAME_UPLOAD_HUAKOU,this,
                            FileUtil.FILE_NAME_UPLOAD_HUAKOU_COMPRESS);
                        if(file!=null){
                            huakouFile = file;
                        }else{
                            huakouFile = null;
                        }
                        Utils.customDialog(this, "已上传资料将无法再进行更改，请确认是否提交？", new DialogListenner() {
                            
                            @Override
                            public void confirm() {
                                uploadFile();
                            }
                        });	
				}
                
                break;
            case BankCardAuthorizeActivity.REQUEST_SCAN:
                if (CardRecoActivity.RESULT_CARD_INFO == resultCode) {
                    EXBankCardInfo cardInfo=data.getParcelableExtra(CardRecoActivity.EXTRA_SCAN_RESULT);
                    String id=data.getStringExtra("bankcardId");
                    Intent intent=new Intent(this, BankCardAuthorizeActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("cardInfo", cardInfo);
                    startActivity(intent); 
                }else if (Activity.RESULT_OK == resultCode) {
                	Intent intent=new Intent(this, BankCardAuthorizeActivity.class);
                    startActivity(intent); 
				}
            	break;
            default:
                break;
        }
    }
    
    /**
     * 上传文件
     */
    private void uploadFile() {
        AsyncHttpClient httpClient = new AsyncHttpClient(false);
        File fileHuakou = new File(FileUtil.getUcreditDir(this),
            FileUtil.FILE_NAME_UPLOAD_HUAKOU_COMPRESS);
        //只要有一个文件不存在，或者某一个文件大小为0，都不能上传
        if (!fileHuakou.exists() || fileHuakou.length() <= 0) {
            Utils.MakeToast(this, "划扣协议照片不存在或文件大小太小，请重新拍摄");
            return;
        }

        RequestParams params = new RequestParams();
        try {
            params.put("file1", fileHuakou);
            params.put("filename1", "huakou");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("USER_ID", UcreditDreamApplication.mUser.getUserId());
            jsonObject.put("APPLY_ID", "");
            jsonObject.put("TO_PDF", "true");
            params.put("params", jsonObject.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        httpClient.post(Constants.UPLOAD, params,
            new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        byte[] responseBody) {
                    setMask(false);
                    Logger.e("upload success", statusCode + " login error"
                            + new String(responseBody));
                    try {
                        JSONObject object = new JSONObject(new String(
                            responseBody));
                        if (object.optBoolean("success") == true) {
                              if (object.getJSONArray("result")!=null) {
                                JSONArray jsonArray=object.getJSONArray("result");
                                String fileId=jsonArray.getJSONObject(0).getString("fileId");
                                notifyUpload(fileId);
                              }
                        } else {
                            resetUploadStatus();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        resetUploadStatus();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        byte[] responseBody, Throwable error) {
                    resetUploadStatus();
                    setMask(false);
                    Logger.e("upload failure", statusCode + " login error"
                            + new String(responseBody));
                    new RequestFailureHandler(SignDetailActivity.this,
                        new GetTokenListener() {
                            @Override
                            public void onSuccess() {
                                uploadFile();
                            }
                        }).handleMessage(statusCode);
                }

                private void resetUploadStatus() {
                    Utils.MakeToast(SignDetailActivity.this, "划扣协议上传失败，请稍后重试");
                }

                @Override
                public void onProgress(int bytesWritten, int totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }
            });
    }
    
    /**
     * 通知服务器
     * @param fileId
     */
    private void notifyUpload(final String fileId){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("lendId", UcreditDreamApplication.applyState.getId());
        params.put("transferAgreement", fileId);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_SIGN_HUAKOU_PROTOCOL, params,
            new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }
                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("notifyUpload failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(SignDetailActivity.this,
                        new GetTokenListener() {
                            @Override
                            public void onSuccess() {
                                notifyUpload(fileId);
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("notifyUpload success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        Utils.MakeToast(SignDetailActivity.this, "划扣协议上传成功");
                        UcreditDreamApplication.mUser.getUserState().setSignMapTransfer(true);
                        initProtocolState();
                    } else {
                        Utils.MakeToast(SignDetailActivity.this, response
                            .getJSONObject("error").getString("message"));
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
        return "在线签约";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_sign_detail;
    }

}
