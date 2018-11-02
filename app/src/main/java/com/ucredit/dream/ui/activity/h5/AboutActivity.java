package com.ucredit.dream.ui.activity.h5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Utils;

public class AboutActivity extends BaseActivity {

    @ViewInject(R.id.protocol_declare)
    private RelativeLayout protocolDeclare; //免责声明
    @ViewInject(R.id.version)
    private TextView version;
    
    @OnClick({R.id.protocol_declare})
    private void onclick(View view){
        Intent intent = new Intent(this, AdvertiseActivity.class);
        switch (view.getId()) {
            case R.id.protocol_declare:
                intent.putExtra("title", "免责声明");
                intent.putExtra("url", Constants.H5_PROTOCOL+"declare");
                startActivity(intent);
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
        return "关于我们";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        version.setText("版本："+Utils.getVersion(this));
    }

}
