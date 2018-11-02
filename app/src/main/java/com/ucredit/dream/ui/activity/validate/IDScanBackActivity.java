package com.ucredit.dream.ui.activity.validate;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.ucredit.dream.ui.activity.apply.ApplyActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import exocr.exocrengine.EXIDCardResult;
import exocr.idcard.CaptureActivity;

public class IDScanBackActivity extends BaseActivity {

    @ViewInject(R.id.scan)
    private ImageView scan;
    @ViewInject(R.id.office)
    private EditText officeText;
    @ViewInject(R.id.validate)
    private EditText validateText;
    @ViewInject(R.id.id_info_linear)
    private LinearLayout idLinearLayout;
    @ViewInject(R.id.scan_tip)
    private TextView scanTip;
    
    private static final int REQUESTCODE_SCAN_IDCARD = 10;
    
    private String name = "";
    private String idCard = "";
    private String address = "";
    private String office = "";
    private String validate = "";
    
    @OnClick({R.id.submit,R.id.scan})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    if(check()){
                        Utils.customDialog(this, "已上传资料将无法再进行更改，请确认是否提交？", new DialogListenner() {
                            @Override
                            public void confirm() {
                                uploadFile();
                            }
                        });
                    }
                }
                break;
            case R.id.scan:
                Intent intent=new Intent(this, CaptureActivity.class);
                intent.putExtra("type", "back");
                startActivityForResult(intent, REQUESTCODE_SCAN_IDCARD);
                break;
            default:
                break;
        }
    } 
    
    private boolean check() {
        name = getIntent().getStringExtra("name");
        idCard = getIntent().getStringExtra("idCard");
        address = getIntent().getStringExtra("address");
        office = officeText.getText().toString();
        validate = validateText.getText().toString();
        if (!Utils.isNotEmptyString(name) || !Utils.isNotEmptyString(idCard)
                || !Utils.isNotEmptyString(address) || 
                !Utils.isNotEmptyString(office)
                || !Utils.isNotEmptyString(validate)) {
            Logger.e("check", " "+name+" "+idCard+" "+address+" "+office+" "+validate);
            Utils.MakeToast(this, "请完成身份证正面和背面扫描");
            return false;
        }
        return true;
    }
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(officeText.getText().toString())) {
            Utils.MakeToast(this, "请您输入签发机关");
            return false;
        }
        if (Utils.stringFilter(officeText.getText().toString())) {
            Utils.MakeToast(this, "签发机关不能包含特殊字符");
            return false;
        }
        if (!Utils.isNotEmptyString(validateText.getText().toString())) {
            Utils.MakeToast(this, "请您输入有效期限");
            return false;
        }
        
        if (Utils.stringFilter(validateText.getText().toString())) {
            Utils.MakeToast(this, "有效期限不能包含特殊字符");
            return false;
        }
        
        return true;
    }      
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            if (extras == null)
                throw new IllegalStateException("Didn't find any extras!");
            EXIDCardResult capture = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
            //判断是否为黑白照片
            int nColorType = extras.getInt("nColorType");
            if(nColorType == 0){
                Utils.MakeToast(IDScanBackActivity.this, Constants.ID_SCAN_TIP);
                return;
            }
            int type = 2;
            if(capture != null){
                type = capture.type;
                if(type == 2){
                    idLinearLayout.setVisibility(View.VISIBLE);
                    scanTip.setText("请扫描身份证核对以下信息，如有误请修改");
                    officeText.setText(capture.office);
                    validateText.setText(capture.validdate);
                }else{
                    Utils.MakeToast(IDScanBackActivity.this, "请选择身份证背面拍摄");
                }
            }else{
                return;
            }
        }
        
    }
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "证件识别";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_certificate_back_scan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        
        officeText.setText(getIntent().getStringExtra("office"));
        validateText.setText(getIntent().getStringExtra("validate"));
        
        scan.setImageResource(R.drawable.scan_anim);
        AnimationDrawable animationDrawable=(AnimationDrawable) scan.getDrawable();
        animationDrawable.start();
        
        Utils.isCameraGranted(this, new DialogListenner() {
            
            @Override
            public void confirm() {
                finish();
            }
        });
    }
    
    private void uploadFile() {
        AsyncHttpClient httpClient = new AsyncHttpClient(false);
        File fileRenxiang = new File(FileUtil.getUcreditDir(this),
            FileUtil.FILE_NAME_UPLOAD_RENXIANG_COMPRESS);
        File fileGuohui = new File(FileUtil.getUcreditDir(this),
            FileUtil.FILE_NAME_UPLOAD_GUOHUI_COMPRESS);
        //只要有一个文件不存在，或者某一个文件大小为0，都不能上传
        if (!fileRenxiang.exists() || fileRenxiang.length() <= 0 ) {
            Utils.MakeToast(this, "请重新扫描身份证正面");
            return;
        }
        if (!fileGuohui.exists() || fileGuohui.length() <= 0 ) {
            Utils.MakeToast(this, "请重新扫描身份证反面");
            return;
        }

        RequestParams params = new RequestParams();
        try {
            params.put("file1", fileRenxiang);
            params.put("filename1", "Renxiang");
            params.put("file2", fileGuohui);
            params.put("filename2", "Guohui");
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
                    setMask(false);
                    Logger.e("upload success", statusCode + " login error"
                            + new String(responseBody));
                    try {
                        JSONObject object = new JSONObject(new String(
                            responseBody));
                        
                        if (object.optBoolean("success") == true) {
                              if (object.getJSONArray("result")!=null) {
                                JSONArray jsonArray=object.getJSONArray("result");
                                String guohui=jsonArray.getJSONObject(0).getString("fileId");
                                String renxiang=jsonArray.getJSONObject(1).getString("fileId");
                                notifyUpload(renxiang,guohui);
                                Logger.e("file upload", "notify");
                              }
//                            Utils.MakeToast(TakeIDPhotoActivity.this, "身份证照片上传成功");
//                            setResult(300);
//                            finish();
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
                    new RequestFailureHandler(IDScanBackActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                uploadFile();
                            }
                        }).handleMessage(statusCode);
                }

                private void resetUploadStatus() {
                    Utils.MakeToast(IDScanBackActivity.this, "证件照片上传失败，请重新上传");
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
    
    private void notifyUpload(final String fileRenxiang,final String fileGuohui){
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("idcardFront", fileRenxiang);
        params.put("idcardBack", fileGuohui);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.UPLOAD_IDCARD_PHOTO, params,
            new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("notify_upload failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(IDScanBackActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                notifyUpload(fileRenxiang, fileGuohui);
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("notify_upload success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        submit();
                    } else {
                        Utils.MakeToast(IDScanBackActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }
    
    
    private void submit() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("userId", UcreditDreamApplication.mUser.getUserId());
        params.put("address",address);
        params.put("name",name.trim());
        params.put("idCard",idCard.trim());
        params.put("issueOrg",office);
        params.put("validPeriod",validate);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_BIND_ID_INFO, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("idcard_bind failure", statusCode + " login error"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(IDScanBackActivity.this,
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
                    Logger.e("idcard_bind success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = com.alibaba.fastjson.JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        UcreditDreamApplication.mUser.setName(name.trim());
                        UcreditDreamApplication.mUser.setIdentityNo(idCard.trim());
                        UcreditDreamApplication.mUser.getUserState().setIdentify(true);
                        Utils.MakeToast(IDScanBackActivity.this, "绑定成功！");
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Utils.MakeToast(IDScanBackActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    
}
