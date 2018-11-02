package com.ucredit.dream.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.ImageHelper;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class BankCardUploadActivity extends BaseActivity {

    @ViewInject(R.id.image)
    private ImageView imageView;
    @ViewInject(R.id.submit)
    private Button submit;
    
    private static final int REQUESTCODE_PHOTO_BANKCARD = 20;
    private static final int REQUESTCODE_DEDUCT = 30;
    
    private boolean isFileBankcard = false;
    private Dialog loadingDialog;
    private String bankcardId;
    
    private boolean signed;
    
    @OnClick({ R.id.submit, R.id.take,R.id.image})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.submit:
                if (bankcardId==null) {
                    Utils.MakeToast(this, "请上传银行卡照片");
                } else {
                    if (signed) {
                        submit(Constants.BANKCARD_CHANGE);
                    } else {
                        submit(Constants.BANKCARD_AUTHORIZE);
                    }
                }
                break;
            case R.id.take:
            case R.id.image:    
                FileUtil.takePhoto(BankCardUploadActivity.this,
                    FileUtil.FILE_NAME_UPLOAD_BANKCARD,
                    REQUESTCODE_PHOTO_BANKCARD);
                break;
            default:
                break;
        }
    }
    


    private void submit(final String prefix) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("accountName", getIntent().getStringExtra("accountName"));
        params.put("bank", getIntent().getStringExtra("bank"));
        params.put("bankName", getIntent().getStringExtra("bankName"));
        params.put("bankCardNo", getIntent().getStringExtra("bankCardNo"));
        params.put("branch", getIntent().getStringExtra("branch"));
        params.put("branchId", getIntent().getStringExtra("branchId"));
        params.put("city", getIntent().getStringExtra("city"));
        params.put("idCard", getIntent().getStringExtra("idCard"));
        params.put("province", getIntent().getStringExtra("province"));
        params.put("bankCardAttach", bankcardId);
        params.put("mobile", getIntent().getStringExtra("mobile"));
        params.setUseJsonStreamer(true);
        httpClient.post(this, prefix, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("bankcard_authorize  success", statusCode + " " + responseString);
                    loadingDialog.dismiss();
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        Utils.MakeToast(BankCardUploadActivity.this, "银行卡信息保存成功");
//                        if (signed) {
                            Intent intent=new Intent(BankCardUploadActivity.this, AdvertiseActivity.class);
                            boolean hasSigned = UcreditDreamApplication.mUser.getUserState().isSignMapTransfer();
                            intent.putExtra("title", "划扣授权书");
                            intent.putExtra("url", Constants.API_SIGN_PROTOCOL+"?applyid="
                                + UcreditDreamApplication.applyState.getId()
                                +"&type=5&protocolType=TRANSFER&access_token="
                                + UcreditDreamApplication.token
                                + "&clientid="
                                + UcreditDreamApplication.clientId
                                );
                            startActivityForResult(intent, REQUESTCODE_DEDUCT);
//                        } else {
//                          setResult(Activity.RESULT_OK);
//                          finish();
//                        }
                    }else {
                        Utils.MakeToast(BankCardUploadActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("bankcard_authorize  failure", statusCode + " " + responseString);
                    loadingDialog.dismiss();
                    new RequestFailureHandler(BankCardUploadActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                submit(prefix);
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    loadingDialog = Utils.showProgressDialog(BankCardUploadActivity.this, "提交中...", true);
                    loadingDialog.show();
                }
                
            }); 
    }    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUESTCODE_PHOTO_BANKCARD:
                isFileBankcard = true;
                handleImage(FileUtil.FILE_NAME_UPLOAD_BANKCARD,this,
                    FileUtil.FILE_NAME_UPLOAD_BANKCARD_COMPRESS);
                break;
            case REQUESTCODE_DEDUCT:
                if (Activity.RESULT_OK == resultCode) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }    
    
    private void handleImage(String fileName, Context mContext,
            String compressFileName) {
        //创建文件，用来展示
        File file = new File(FileUtil.getUcreditDir(mContext), fileName);
        File fileOrientation=new File(FileUtil.getUcreditDir(mContext), FileUtil.FILE_NAME_UPLOAD_BANKCARD_ORIENTATION);
        if (!file.exists()) {
            return;
        }
        ImageHelper.saveCompressBitmap(ImageHelper.loadBitmap(file.toString()), fileOrientation);
        //压缩文件，用来上传
        ImageHelper.saveCompressBitmap(
            ImageHelper.createImage(file.toString()),
            new File(FileUtil.getUcreditDir(mContext), compressFileName));
        //展示没有压缩的图片
        ImageLoader.getInstance().displayImage("file:///" + fileOrientation.getPath(),
            imageView);
        
        //上传图片
        uploadFile();
    }    
    


    
    private void uploadFile() {
        AsyncHttpClient httpClient = new AsyncHttpClient(false);
        File fileBankcard = new File(FileUtil.getUcreditDir(this),
            FileUtil.FILE_NAME_UPLOAD_BANKCARD_COMPRESS);
        //只要有一个文件不存在，或者某一个文件大小为0，都不能上传
        if (!fileBankcard.exists() || fileBankcard.length() <= 0 || !isFileBankcard) {
            Utils.MakeToast(this, "请拍摄银行卡照片");
            return;
        }

        RequestParams params = new RequestParams();
        try {
            params.put("file1", fileBankcard);
            params.put("filename1", "Bankcard");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("USER_ID", UcreditDreamApplication.mUser.getUserId());
            jsonObject.put("APPLY_ID", "");
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
                    loadingDialog.dismiss();
                    Logger.e("upload success", statusCode + " login error"
                            + new String(responseBody));
                    try {
                        JSONObject object = new JSONObject(new String(
                            responseBody));
                        
                        if (object.optBoolean("success") == true) {
                              if (object.getJSONArray("result")!=null) {
                                JSONArray jsonArray=object.getJSONArray("result");
                                bankcardId=jsonArray.getJSONObject(0).getString("fileId");
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
                    loadingDialog.dismiss();
                    Logger.e("upload failure", statusCode + " login error"
                            + "BankCardUploadActivity uploadfile");
                    new RequestFailureHandler(BankCardUploadActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                uploadFile();
                            }
                        }).handleMessage(statusCode);
                }

                private void resetUploadStatus() {
                    Utils.MakeToast(BankCardUploadActivity.this, "银行卡照片上传失败，请重新上传");
                }

                @Override
                public void onProgress(int bytesWritten, int totalSize) {
                    super.onProgress(bytesWritten, totalSize);
                    int progress = (int) ((bytesWritten * 1.0 / totalSize) * 100);
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    loadingDialog.show();
                    bankcardId=null;
                }

            });
    }    
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "银行卡上传";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_bankcardupload;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFileBankCard", isFileBankcard);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        if(savedInstanceState != null){
            isFileBankcard = savedInstanceState.getBoolean("isFileBankCard");
        }
        loadingDialog = Utils.showProgressDialog(this, "银行卡照片上传中...", true);
        if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getUserState()!=null) {
            signed=UcreditDreamApplication.mUser.getUserState().isSign();
        }
        if (signed) {
            submit.setText("下一步");
        } else {
            submit.setText("提交");
        }
    }

}

