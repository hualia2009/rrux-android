package com.ucredit.dream.ui.activity.validate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.apply.ApplyActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.IdCardUtils;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;

import exocr.exocrengine.EXIDCardResult;
import exocr.idcard.CaptureActivity;

public class IDScanActivity extends BaseActivity {

    @ViewInject(R.id.scan)
    private ImageView scan;
    @ViewInject(R.id.name)
    private EditText name;
    @ViewInject(R.id.idcard)
    private EditText idcard;
    @ViewInject(R.id.idaddress)
    private EditText idaddress;
    @ViewInject(R.id.id_info_linear)
    private LinearLayout idLinearLayout;
    @ViewInject(R.id.scan_tip)
    private TextView scanTip;
    
    public static final int REQUESTCODE_SCAN_IDCARD = 10;
    public static final int REQUESTCODE_SKIP_BACK = 20;
    public static final int REQUESTCODE_TO_BACK = 30;
    
    @OnClick({R.id.submit,R.id.scan})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    Intent data = new Intent(IDScanActivity.this,CaptureActivity.class);
                    data.putExtra("type", "back");
                    startActivityForResult(data, REQUESTCODE_TO_BACK);
                }
                break;
            case R.id.scan:
                Intent intent=new Intent(this, CaptureActivity.class);
                intent.putExtra("type", "front");
                startActivityForResult(intent, REQUESTCODE_SCAN_IDCARD);
                break;
            default:
                break;
        }
    }       
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(name.getText().toString())) {
            Utils.MakeToast(this, "请您输入姓名");
            return false;
        }
        if (Utils.stringFilter(name.getText().toString())) {
            Utils.MakeToast(this, "姓名中不能含有特殊字符");
            return false;
        }
        if (!Utils.isNotEmptyString(idcard.getText().toString())) {
            Utils.MakeToast(this, "请您输入身份证号");
            return false;
        }
        
        if (!IdCardUtils.isValid(idcard.getText().toString().toUpperCase())) {
            Utils.MakeToast(this, "身份证号不正确，请重新输入");
            return false;
        }
        
        if (!Utils.isNotEmptyString(idaddress.getText().toString())) {
            Utils.MakeToast(this, "请您输入家庭地址");
            return false;
        }
        if (Utils.stringFilter(idaddress.getText().toString())) {
            Utils.MakeToast(this, "家庭地址中不能包含特殊字符");
            return false;
        }
        return true;
    }      
    
    private  String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }
        
        if(requestCode == REQUESTCODE_TO_BACK){//跳转到扫描页
            Bundle extras = data.getExtras();
            if (extras == null)
                throw new IllegalStateException("Didn't find any extras!");
          //判断是否为黑白照片
            int nColorType = extras.getInt("nColorType");
            if(nColorType == 0){
                Utils.MakeToast(IDScanActivity.this, Constants.ID_SCAN_TIP);
                return;
            }
            EXIDCardResult capture = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
            int type = 2;
            if(capture != null){
                type = capture.type;
                if(type == 2){
                    Intent intent=new Intent(this, IDScanBackActivity.class);
                    intent.putExtra("name", ""+replaceBlank(name.getText().toString()));
                    intent.putExtra("idCard", ""+idcard.getText().toString());
                    intent.putExtra("address",""+idaddress.getText().toString());
                    intent.putExtra("office", ""+capture.office);
                    intent.putExtra("validate",""+capture.validdate);
                    startActivityForResult(intent, IDScanActivity.REQUESTCODE_SKIP_BACK);//跳到扫描结果页
                    return;
                }else{
                    Utils.MakeToast(IDScanActivity.this, "请选择身份证背面拍摄");
                }
            }else{
                return;
            }
        }
        
        if(requestCode == REQUESTCODE_SKIP_BACK){//从扫描结果页返回
            setResult(ApplyActivity.APPLY_ID_CODE);
            finish();
        }
        
        if(requestCode == REQUESTCODE_SCAN_IDCARD){
            Bundle extras = data.getExtras();
            if (extras == null)
                throw new IllegalStateException("Didn't find any extras!");
            EXIDCardResult capture = extras.getParcelable(CaptureActivity.EXTRA_SCAN_RESULT);
            //判断是否为黑白照片
            int nColorType = extras.getInt("nColorType");
            if(nColorType == 0){
                Utils.MakeToast(IDScanActivity.this, Constants.ID_SCAN_TIP);
                return;
            }
            int type = 1;
            if(capture != null){
                type = capture.type;
                if(type == 1){
                    idLinearLayout.setVisibility(View.VISIBLE);
                    scanTip.setText("请扫描身份证核对以下信息，如有误请修改");
                    name.setText(capture.name);
                    idcard.setText(capture.cardnum);
                    idaddress.setText(capture.address);
                }else{
                    Utils.MakeToast(IDScanActivity.this, "请选择身份证正面拍摄");
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
        return R.layout.activity_certificaterecognition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        
        name.setText(getIntent().getStringExtra("name"));
        idcard.setText(getIntent().getStringExtra("idCard"));
        idaddress.setText(getIntent().getStringExtra("address"));
        
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

    
}
