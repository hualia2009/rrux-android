package com.ucredit.dream.ui.activity.main;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.loopj.android.http.RequestParams;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.ApplyState;
import com.ucredit.dream.bean.User;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.ui.fragment.LeftMenuFragment;
import com.ucredit.dream.ui.fragment.MainContentFragment;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.update.CheckUpdateUtil;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomePageActivity extends SlidingFragmentActivity {

    private Fragment mContent;
    private Button refresh;
    private boolean isExit;
    private static final int MSG_EXIT_ROOM = 5001;
    private static final int MSG_EXIT_ROOM_DELAY = 2000;
    private Handler mHandler;
    

    private OnClickListener clickListener;
    private ExitBroadCast exitReceiver = new ExitBroadCast();

	public void setClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
//        	if (UcreditDreamApplication.millisecond==0) {
//				UcreditDreamApplication.millisecond=savedInstanceState.getLong("millisecond");
//			}
        	if (UcreditDreamApplication.mUser==null) {
                UcreditDreamApplication.mUser=(User) savedInstanceState.getSerializable("muser");
            }
            UcreditDreamApplication.applyState = (ApplyState) savedInstanceState.getSerializable("applyState");
            if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getUserState()==null) {
                UcreditDreamApplication.mUser.setUserState((UserState) savedInstanceState.getSerializable("userState"));
            }
            UcreditDreamApplication.key=savedInstanceState.getString("key");
            UcreditDreamApplication.secret=savedInstanceState.getString("secret");
            UcreditDreamApplication.clientId=savedInstanceState.getString("clientId");
            UcreditDreamApplication.salt=savedInstanceState.getString("salt");
            UcreditDreamApplication.cid=savedInstanceState.getString("cid");
            UcreditDreamApplication.lendId=savedInstanceState.getString("lendId");
            UcreditDreamApplication.refreshToken=savedInstanceState.getString("refreshToken");
            UcreditDreamApplication.isDoubleCheck = savedInstanceState.getBoolean("isDoubleCheck");
            UcreditDreamApplication.token = savedInstanceState.getString("token");
            RequestParams.clientId = savedInstanceState.getString("clientId");
            RequestParams.key = savedInstanceState.getString("key");
        }
        
        setContentView(R.layout.layout_main);
        // check if the content frame contains the menu frame
        if (findViewById(R.id.menu_frame) == null) {
            setBehindContentView(R.layout.main_menu_frame);
            getSlidingMenu().setSlidingEnabled(true);
            getSlidingMenu()
                .setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        } else {
            // add a dummy view
            View v = new View(this);
            setBehindContentView(v);
            getSlidingMenu().setSlidingEnabled(false);
            getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }

        // set the Above View Fragment
        if (savedInstanceState != null) {
            mContent = getSupportFragmentManager().getFragment(
                savedInstanceState, "mContent");
        }

        if (mContent == null) {
           mContent = new MainContentFragment();
        }
        FragmentTransaction transaction = getSupportFragmentManager()
            .beginTransaction();
        getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, mContent);
        transaction.commit();

        // set the Behind View Fragment
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.menu_frame, new LeftMenuFragment()).commit();

        // customize the SlidingMenu
        initSlidingMenu();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(ExitBroadCast.ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(exitReceiver, filter);
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_EXIT_ROOM:
                        HomePageActivity.this.isExit = false;
                        break;

                    default:
                        break;
                }
            }
        };
        UcreditDreamApplication.list.add(this);
        //检查更新
        CheckUpdateUtil.getInstance().startCheck(this, false);
        
    }

    private void initSlidingMenu() {
        final SlidingMenu sm = getSlidingMenu();
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        sm.setBehindOffset(outMetrics.widthPixels / 3);
        sm.setFadeEnabled(false);
        sm.setBehindScrollScale(0.25f);
        sm.setFadeDegree(0.25f);
//        sm.setBackgroundImage(R.drawable.main_menu_backgroud_bg);

    } 
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("muser", UcreditDreamApplication.mUser);
        outState.putSerializable("applyState", UcreditDreamApplication.applyState);
        if(UcreditDreamApplication.mUser != null){
            outState.putSerializable("userState", UcreditDreamApplication.mUser.getUserState());
        }
        outState.putString("key", UcreditDreamApplication.key);
        outState.putString("secret", UcreditDreamApplication.secret);
        outState.putString("clientId", UcreditDreamApplication.clientId);
        outState.putString("salt", UcreditDreamApplication.salt);
        outState.putString("cid", UcreditDreamApplication.cid);
        outState.putString("lendId", UcreditDreamApplication.lendId);
        outState.putString("refreshToken", UcreditDreamApplication.refreshToken);
        outState.putString("token", UcreditDreamApplication.token);
        outState.putBoolean("isDoubleCheck", UcreditDreamApplication.isDoubleCheck);
//        outState.putLong("millisecond", UcreditDreamApplication.millisecond);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (!this.isExit) {
                this.isExit = true;
                Utils.MakeToast(this, "再按一次退出应用");
                this.mHandler.sendEmptyMessageDelayed(MSG_EXIT_ROOM,
                    MSG_EXIT_ROOM_DELAY);
            } else {
                exitApplication();
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }
    
    private void exitApplication() {
        notifyExit();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(exitReceiver);
        UcreditDreamApplication.list.remove(this);
    }
    
    private void notifyExit(){
        Intent intent = new Intent();
        intent.setAction("com.ucredit.exit");
        HomePageActivity.this.sendBroadcast(intent);
    }
    
    public Button getRefresh() {
		return refresh;
	}

	public void setRefresh(Button refresh) {
		this.refresh = refresh;
	}

	class ExitBroadCast extends BroadcastReceiver {

        public static final String ACTION = "com.ucredit.exit";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION)) {
                finish();
            }else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            	boolean isWifiConnected = false;  
                boolean isMobileConnected = false;  
                ConnectivityManager connMgr = (ConnectivityManager)   
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);  
                NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
                if(networkInfo != null)  
                isWifiConnected = networkInfo.isConnected();  
                networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
                if(networkInfo != null)  
                isMobileConnected = networkInfo.isConnected();     
                if (isWifiConnected||isMobileConnected) {
                	if (clickListener!=null) {
    					clickListener.onClick(refresh);
    				}
				}
				
			}
        }

    }
    
}
