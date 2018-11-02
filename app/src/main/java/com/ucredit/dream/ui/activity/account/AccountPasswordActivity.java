package com.ucredit.dream.ui.activity.account;

import org.json.JSONException;
import org.json.JSONObject;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.CountDownTimer;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.CheckVerifyCodeRequest;
import com.ucredit.dream.utils.request.CheckVerifyCodeRequest.CheckListener;
import com.ucredit.dream.utils.request.VerifyCodeRequest;
import com.ucredit.dream.utils.request.VerifyCodeRequest.VerifyCodeListener;
import com.ucredit.dream.view.GridPasswordView;
import com.ucredit.dream.view.GridPasswordView.OnPasswordChangedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AccountPasswordActivity extends BaseActivity {

	@ViewInject(R.id.verifyCode)
    private GridPasswordView verifycode;
	@ViewInject(R.id.countdown)
    private TextView countdown;
    @ViewInject(R.id.send)
    private TextView send;
    @ViewInject(R.id.confirm)
    private Button confirm; 
    private VerifyTimer timer;
    private Context mContext;
    private String phone;
    
    private static long millisecond=0;
    private static long pageTimeTag;

    @OnClick({ R.id.confirm})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.confirm:
                if (UcreditDreamApplication.mUser == null) {
                    Utils.MakeToast(this, "请先登录");
                }
                check();
                break;
            default:
                break;
        }
    }
 
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			int[] a=new int[2];
			verifycode.getLocationOnScreen(a);
			int x=a[0]+verifycode.getWidth()/2;
			int y=a[1]+verifycode.getHeight()/2;
			setSimulateClick(verifycode, x, y);
		}
	}

	private void setSimulateClick(View view, float x, float y) {
	    long downTime = SystemClock.uptimeMillis();
	    final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
	        MotionEvent.ACTION_DOWN, x, y, 0);
	    downTime += 1000;
	    final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
	        MotionEvent.ACTION_UP, x, y, 0);
	    view.onTouchEvent(downEvent);
	    view.onTouchEvent(upEvent);
	    downEvent.recycle();
	    upEvent.recycle();
	  }
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "验证手机";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_account_password;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == ResetPasswordActivity.RESULT_CODE_FINISH){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mContext = this;
        if (UcreditDreamApplication.mUser != null) {
            phone = UcreditDreamApplication.mUser.getPhone();
        }
        verifycode.setPasswordVisibility(true);
		verifycode.setOnPasswordChangedListener(new OnPasswordChangedListener() {
			
			@Override
			public void onTextChanged(String psw) {
				if (psw.length()==4) {
					confirm.setEnabled(true);
				} else {
                    confirm.setEnabled(false);
				}
			}
			
			@Override
			public void onInputFinish(String psw) {
				if (UcreditDreamApplication.mUser == null) {
                    Utils.MakeToast(AccountPasswordActivity.this, "请先登录");
                }
                check();
			}
		});
        initClickText();
        if (timer==null) {
			Logger.e("millisecond",""+millisecond );
			if (millisecond==0||millisecond==60000) {
				timer = new VerifyTimer(60000, 1000, send,countdown);
				getVerifyCode();
			} else {
				long interval=SystemClock.elapsedRealtime()-pageTimeTag;
				Logger.e("interval",""+interval );
				if (millisecond-interval>=1000) {
					timer =new VerifyTimer(millisecond-interval, 1000, send,countdown);
		            timer.start();
				} else {
					timer = new VerifyTimer(60000, 1000, send,countdown);
					getVerifyCode();
				}
	            
			}
		}
    }

    private void getVerifyCode() {
        new VerifyCodeRequest(phone, this, new VerifyCodeListener() {

            @Override
            public void onSuccess(String responseString) {
                setMask(false);
                try {
                    JSONObject object = new JSONObject(responseString);
                    if (object.getBoolean("success")) {//没有注册过
                        Utils.MakeToast(mContext, "发送成功！");
                        if (timer != null) {
                            timer.start();
                        }
                    } else {
                        Utils.MakeToast(mContext, object.getJSONObject("error")
                            .getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStart() {
                checkNet(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						getVerifyCode();
					}
				  });
                setMask(true);
            }

            @Override
            public void onFailure() {
                setMask(false);
            }
        }, "RESET").getVerifyCode();
    }
    
    private void initClickText() {		
        SpannableString ssb = new SpannableString("收不到验证码？重发短信或联系客服获取验证码");
        ssb.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View view) {
            	timer = new VerifyTimer(60000, 1000, send,countdown);
            	getVerifyCode();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setTypeface(Typeface.DEFAULT_BOLD);
                ds.setColor(getResources().getColor(R.color.textview_inner_color));
                ds.setUnderlineText(false);
            }

        }, 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ClickableSpan() {
        	
        	@Override
        	public void onClick(View view) {
        		Utils.customDialog(AccountPasswordActivity.this, "" + Constants.CUSTOMER_SERVICE,
						new DialogListenner() {

							@Override
							public void confirm() {
								Utils.dial(Constants.CUSTOMER_SERVICE, AccountPasswordActivity.this);
							}
						}, "拨打客服热线");
        	}
        	
        	@Override
        	public void updateDrawState(TextPaint ds) {
        		ds.setTypeface(Typeface.DEFAULT_BOLD);
        		ds.setColor(getResources().getColor(R.color.textview_inner_color));
        		ds.setUnderlineText(false);
        	}
        	
        }, 12, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        send.setHighlightColor(getResources().getColor(
                android.R.color.transparent));//方法重新设置文字背景为透明色。
        send.append(ssb);
        send.setMovementMethod(LinkMovementMethod.getInstance());
	}
    
    private void check(){
        new CheckVerifyCodeRequest(phone, verifycode.getPassWord(), mContext, new CheckListener() {
            
            @Override
            public void onSuccess(String responseString) {
                setMask(false);
                try {
                    JSONObject object = new JSONObject(responseString);
                    if (object.getBoolean("success")) {//没有注册过
                        Intent intent = new Intent(mContext, ResetPasswordActivity.class);
                        intent.putExtra("phone",
                            "" + UcreditDreamApplication.mUser.getPhone());
                        intent.putExtra("verifyCode", ""
                            + verifycode.getPassWord());
                        startActivityForResult(intent,ResetPasswordActivity.RESULT_CODE_FINISH);
                    } else {
                    	Utils.MakeToast(AccountPasswordActivity.this, object
                                .getJSONObject("error").getString("message"));
                            if ("验证码错误".equals(object
                                .getJSONObject("error").getString("message"))) {
								verifycode.clearPassword();
							}
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
            }
            
            @Override
            public void onStart() {
                checkNet(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						check();
					}
				  });
                setMask(true);
            }
            
            @Override
            public void onFailure() {
                setMask(false);
            }
        }, "RESET").check();
    }

    private class VerifyTimer extends CountDownTimer {

        private TextView countText;
        private TextView clickText;
        
        private long left;

        public VerifyTimer(long millisInFuture, long countDownInterval,
                TextView clickText,TextView countText) {
            super(millisInFuture, countDownInterval);
            this.clickText = clickText;
            this.countText = countText;
            left=millisInFuture;
        }

        @Override
        public void onFinish() {
        	left=0;
        	cancel();
        	clickText.setVisibility(View.VISIBLE);
            countText.setVisibility(View.GONE);
        }

        @Override
        public void onTick(long arg0) {
        	left=arg0;
            clickText.setVisibility(View.GONE);
            countText.setVisibility(View.VISIBLE);;
            countText.setText("重新获取验证码(" + Math.round(((float)arg0/1000)) + "s)");
        }

    	public long getLeft() {
    		return left;
    	}
        
    }        
 
	@Override
	protected void onDestroy() {
		super.onDestroy();
		pageTimeTag=SystemClock.elapsedRealtime();
		if (timer != null) {
            timer.cancel();
            millisecond=timer.getLeft();
            timer=null;
        }
	}
    
}
