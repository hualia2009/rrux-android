package com.ucredit.dream.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.account.AccountActivity;
import com.ucredit.dream.ui.activity.account.LoginActivity;
import com.ucredit.dream.ui.activity.charge.RechargePaybackInfoActivity;
import com.ucredit.dream.ui.activity.h5.AboutActivity;
import com.ucredit.dream.ui.activity.main.FeedBackActivity;
import com.ucredit.dream.ui.activity.main.HomePageActivity;
import com.ucredit.dream.utils.SharePreferenceUtil;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;

public class LeftMenuFragment extends Fragment {

    @ViewInject(R.id.setting_phone)
    private TextView mainPage;
    @ViewInject(R.id.setting_phone)
    private TextView settingPhone;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_menu_layout, null);
        ViewUtils.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(UcreditDreamApplication.mUser != null){
            settingPhone.setText(Utils.getProtectedMobile(UcreditDreamApplication.mUser.getPhone()));
        }
    }
    
    @OnClick({ R.id.charge,R.id.setting_phone,R.id.setting_account,R.id.setting_feedback
        ,R.id.setting_about,R.id.setting_exit})
    public void onClick(View view) {
        HomePageActivity mainActivity = (HomePageActivity) getActivity();
        if (mainActivity == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.charge:
            	if (UcreditDreamApplication.mUser==null
            	||UcreditDreamApplication.mUser.getUserState()==null
            	||!UcreditDreamApplication.mUser.getUserState().isCanRecharge()
            	||UcreditDreamApplication.applyState==null
            	||UcreditDreamApplication.applyState.getId()==null) {
            		Utils.MakeToast(mainActivity, "您的申请还未完成，请完成后再操作");
				}else {
					Intent intent=new Intent(mainActivity, RechargePaybackInfoActivity.class);
					intent.putExtra("lendId", UcreditDreamApplication.applyState.getId());
					startActivity(intent);
				}
        	    break;
            case R.id.setting_phone:
                mainActivity.getSlidingMenu().showContent();
                break;
            case R.id.setting_account:
                mainActivity.startActivity(new Intent(mainActivity, AccountActivity.class));
                break;
            case R.id.setting_feedback:
                mainActivity.startActivity(new Intent(mainActivity, FeedBackActivity.class));
//                Utils.openQQ("412091963", getActivity());
                break;
            case R.id.setting_about:
                mainActivity.startActivity(new Intent(mainActivity, AboutActivity.class));
                break;
            case R.id.setting_exit:
                exit(getActivity());
                break;
            default:
                break;
        }

    }
    
    private void exit(final Context context){
        Utils.customDialog(context, "您确定要退出吗？",
            new DialogListenner() {

                @Override
                public void confirm() {
                    // TODO 退出操作
                    UcreditDreamApplication.mUser = null;

                    SharePreferenceUtil.getInstance(
                        context).setUserName("");
                    SharePreferenceUtil.getInstance(
                        context).setPassword("");
                    SharePreferenceUtil.getInstance(
                        context).setSalt("");
                    Intent intent=new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
    }
    
}
