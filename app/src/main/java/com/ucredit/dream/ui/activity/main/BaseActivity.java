package com.ucredit.dream.ui.activity.main;

import java.util.ArrayList;

import com.loopj.android.http.RequestParams;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.ApplyState;
import com.ucredit.dream.bean.City;
import com.ucredit.dream.bean.User;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.utils.Utils;
import com.umeng.analytics.MobclickAgent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * baseActivity
 * 
 * @author huangli
 */
public abstract class BaseActivity extends FragmentActivity {

	protected TextView back;
    private FrameLayout cover;
    private ImageView progress;
    protected RelativeLayout nonet;
    protected Button refresh;
    private OnClickListener clickListener;
    
    private ExitBroadCast exitReceiver = new ExitBroadCast();

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            if (UcreditDreamApplication.mUser==null) {
                UcreditDreamApplication.mUser=(User) savedInstanceState.getSerializable("muser");
            }
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
            UcreditDreamApplication.applyState = (ApplyState) savedInstanceState.getSerializable("applyState");
            UcreditDreamApplication.isDoubleCheck = savedInstanceState.getBoolean("isDoubleCheck");
            UcreditDreamApplication.token = savedInstanceState.getString("token");
            RequestParams.clientId = savedInstanceState.getString("clientId");
            RequestParams.key = savedInstanceState.getString("key");
            if (UcreditDreamApplication.city==null) {
				UcreditDreamApplication.city=savedInstanceState.getString("city");
			}
            if (UcreditDreamApplication.cityList==null) {
				UcreditDreamApplication.cityList=(ArrayList<City>) savedInstanceState.getSerializable("cityList");
			}
        }
        
        setContentView(R.layout.activity_base);
        initSonActivity();
        animation=AnimationUtils.loadAnimation(this, R.drawable.rotate_progress);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ExitBroadCast.ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(exitReceiver, filter);
        UcreditDreamApplication.list.add(this);
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
        outState.putString("city", UcreditDreamApplication.city);
        outState.putSerializable("cityList", UcreditDreamApplication.cityList);
    }

    private void initSonActivity() {
        if (hasTitle()) {
            back = (TextView) findViewById(R.id.title_left);
            if (!hasBack()) {
                back.setVisibility(View.GONE);
            }
            TextView textView = (TextView) findViewById(R.id.title);
            back.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    finish();
                    Utils.exitActivityAnimation(BaseActivity.this);
                }
            });
            textView.setText(getPublicTitle());
            ImageView imageView=(ImageView) findViewById(R.id.imgRight);
            if (hasRightButton()) {
                setRightButton(imageView);
            }
        } else {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_header);
            layout.setVisibility(View.GONE);
        }
        cover =(FrameLayout) findViewById(R.id.cover);
        progress=(ImageView) findViewById(R.id.rotate_progress);
        nonet=(RelativeLayout) findViewById(R.id.no_net);
        refresh=(Button) findViewById(R.id.refresh);
        nonet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		});
        cover.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
        FrameLayout root = (FrameLayout) findViewById(R.id.activity_content);
        root.addView(getLayoutInflater().inflate(getContentId(), null));
    }

    protected void setRightButton(ImageView imageView) {
        
    }

    private boolean isNetworkAvailable() {
		return Utils.isNetwokAvailable(this);
	}
    
	protected void checkNet(OnClickListener listener) {
		if (!isNetworkAvailable()) {
			this.clickListener=listener;
			nonet.setVisibility(View.VISIBLE);
			refresh.setOnClickListener(listener);
			if (back!=null) {
				back.setEnabled(false);
			}
		} else {
			this.clickListener=null;
			nonet.setVisibility(View.GONE);
			if (back!=null) {
				back.setEnabled(true);
			}
		}
	}
    
    protected boolean hasRightButton() {
        return false;
    }

    protected void setMask(boolean b) {
    	
		if (b) {
			cover.setVisibility(View.VISIBLE);
			progress.startAnimation(animation);
			progress.setVisibility(View.VISIBLE);
		} else {
            cover.setVisibility(View.GONE);
            progress.clearAnimation();
            progress.setVisibility(View.GONE);
		}
	}
    
    protected void toActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    protected void toActivityForResult(Class<?> activity,int requestCode) {
        Intent intent = new Intent(this, activity);
        startActivityForResult(intent,requestCode);
    }

    protected abstract boolean hasTitle();

    protected boolean hasBack() {
        return true;
    }

    protected abstract CharSequence getPublicTitle();

    protected abstract int getContentId();

    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        
    }

    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(exitReceiver);
        UcreditDreamApplication.list.remove(this);
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
