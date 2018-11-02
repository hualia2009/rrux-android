package com.ucredit.dream.ui.activity.apply;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;

public class ApplyPreStudyActivity extends BaseActivity {
    @ViewInject(R.id.graduation)
    RadioButton graduation;
    @ViewInject(R.id.reading)
    RadioButton reading;
    @ViewInject(R.id.diploma)
    private TextView diploma;
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
    File xueLiFile = null;
    private String diplomaNames[] = new String[] { "博士及以上", "硕士", "大学本科", "大专",
        "高中", "中专", "初中及以下", "技校", "职高" };

    private boolean isUpload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        
        TextWatcherListener changedListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                if(!"请选择".equals(diploma.getText())){
                    agree.setEnabled(true);
                }else{
                    agree.setEnabled(false);
                }
            }
        });
        diploma.addTextChangedListener(changedListener);
    }

    @OnClick({ R.id.agree, R.id.diploma, R.id.diploma_rightarrow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agree:
                //跳转到下一个页面
                if ("请选择".equals(diploma.getText())) {
                    Utils.MakeToast(ApplyPreStudyActivity.this, "请选择学历");
                    return;
                }

                Intent intent = new Intent(ApplyPreStudyActivity.this,
                    ApplyStudyActivity.class);
                intent.putExtra("diplomaWhich", diplomaWhich);
                intent.putExtra("graduation", graduation.isChecked()?1:0);
                startActivityForResult(intent,ApplyActivity.APPLY_STUDY_CODE);
                break;
            case R.id.diploma:
                showSelectDialog();
                break;
            case R.id.diploma_rightarrow:
                showSelectDialog();
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == ApplyActivity.APPLY_STUDY_CODE){
            setResult(ApplyActivity.APPLY_STUDY_CODE);
            finish();
        }
    }

    public void changeDiploma(int newValue) {
        diploma.setText(diplomaNames[newValue]);
        selectIndex = newValue;
        if (selectIndex > 3 || isUpload) {//显示拍照
            graduation.setChecked(true);
            reading.setVisibility(View.GONE);
        } else {
            reading.setVisibility(View.VISIBLE);
        }
    }

    private int diplomaWhich = 2;

    public void showSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择学历");
        // 选择下标
        builder.setSingleChoiceItems(diplomaNames, diplomaWhich,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    diplomaWhich = which;
                    changeDiploma(which);
                    dialog.cancel();
                }
            });
        builder.create().show();
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
        return R.layout.activity_apply_pre_study;
    }

}
