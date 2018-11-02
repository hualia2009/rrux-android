package com.ucredit.dream.ui.activity.account;

import android.os.Bundle;
import android.view.View;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.main.BaseActivity;

public class AccountActivity extends BaseActivity {

    
    @OnClick({R.id.password,R.id.email})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.password:
                toActivity(AccountPasswordActivity.class);
                break;

            case R.id.email:
                toActivity(AccountEmailActivity.class);
                break;
            default:
                break;
        }
    }
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "账号修改";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
    }

}
