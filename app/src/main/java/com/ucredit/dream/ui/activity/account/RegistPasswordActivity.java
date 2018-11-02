package com.ucredit.dream.ui.activity.account;

import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.bairong.mobile.BrAgent;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.crypt.CryptUtils;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.User;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.ui.activity.main.HomePageActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.CountDownTimer;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.request.LoginRequest.LoginListener;
import com.ucredit.dream.utils.request.VerifyCodeRequest;
import com.ucredit.dream.utils.request.VerifyCodeRequest.VerifyCodeListener;
import com.ucredit.dream.view.GridPasswordView;
import com.ucredit.dream.view.GridPasswordView.OnPasswordChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import cn.fraudmetrix.android.FMAgent;


public class RegistPasswordActivity extends BaseActivity{
    
    @ViewInject(R.id.verifyCode)
    private GridPasswordView verifycode;
    @ViewInject(R.id.confirm)
    private Button confirm;    
    @ViewInject(R.id.countdown)
    private TextView countdown;
    @ViewInject(R.id.send)
    private TextView send;
    @ViewInject(R.id.num)
    private TextView num;
    @ViewInject(R.id.tip)
    private TextView tip;
    @ViewInject(R.id.title)
    private TextView title;
    
    boolean isVisible = false;
    private String phone = "";
    private String password = "";
    private VerifyTimer timer;
    private static long millisecond=0;
    private static long pageTimeTag;
    
    private boolean cancelTag=true;
    public static final int REGISTPASSWORD_HASREGISTED=77;
    
    private LoginRequest loginRequest;
    private boolean isVerify = false;
    private String type = "REGISTER"; //默认验证码类型为注册
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		isVerify = getIntent().getBooleanExtra("isVerify", false);
		if(isVerify){
		    title.setText("验证手机");
		}
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			    if(isVerify){
			        finish();
			        return;
			    }
			    handleBack();
			}
		});
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
				register();
			}
		});
		
		phone = getIntent().getStringExtra("phone");
		password = getIntent().getStringExtra("password");
		num.setText(""+phone);
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
        		Utils.customDialog(RegistPasswordActivity.this, "" + Constants.CUSTOMER_SERVICE,
						new DialogListenner() {

							@Override
							public void confirm() {
								Utils.dial(Constants.CUSTOMER_SERVICE, RegistPasswordActivity.this);
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



	@Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "注册";
    }

    @Override
    protected int getContentId() {
        return R.layout.regist_password;
    }
    
    @OnClick({R.id.confirm})
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.confirm:
                register();
                break;
            default:
                break;
        }
    }
    
    
    private void getVerifyCode() {
        if(isVerify){
            type = "LOGIN";
        }
        
        new VerifyCodeRequest(phone, this,
            new VerifyCodeListener() {

                @Override
                public void onStart() {
                  Logger.e("getVerifyCode", "onStart");
				  checkNet(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						getVerifyCode();
					}
				  });
                  setMask(true);
                }

                @Override
                public void onSuccess(String responseString) {
                  Logger.e("getVerifyCode", "onSuccess");
                  setMask(false);
                  if(isVerify){
                      doLoginVerify(responseString);
                      return;
                  }
                  doRegistVerify(responseString);
                }

                @Override
                public void onFailure() {
                	Logger.e("getVerifyCode", "onFailure");
                    setMask(false);
                }

            },type).getVerifyCode();

    }
    
    /**
     * 登录验证码处理
     * @param responseString
     */
    protected void doLoginVerify(String responseString) {
        try {
            JSONObject object = new JSONObject(responseString);
            if (object.getBoolean("success")) {//没有注册过
                if (timer!=null) {
                    timer.start();
                }
            } else {
                Utils.MakeToast(RegistPasswordActivity.this, object
                    .getJSONObject("error").getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册验证码发送处理
     * @param responseString
     */
    private void doRegistVerify(String responseString) {
        try {
            JSONObject object = new JSONObject(responseString);
            if (object.getBoolean("success")) {//没有注册过
                if (timer!=null) {
                    timer.start();
                }
            } else {
                if ("该用户已注册".equals(object.getJSONObject("error").getString("message"))) {
                    tip.setText("手机号："+phone+" 已注册，您可以点击");
                    num.setText("登录");
                    num.setTextColor(getResources().getColor(R.color.textview_inner_color));
                    num.setOnClickListener(new OnClickListener() {
                        
                        @Override
                        public void onClick(View arg0) {
                            setResult(REGISTPASSWORD_HASREGISTED);
                            finish();
                        }
                    });
                }
                Utils.MakeToast(RegistPasswordActivity.this, object
                    .getJSONObject("error").getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private boolean inputCheck() {
        String verifyText = verifycode.getPassWord();
        if (!Utils.isNotEmptyString(verifyText)) {
            Utils.MakeToast(this, "请输入验证码");
            return false;
        }
        return true;
    }
    
    private void register() {
        if(isVerify){//登录验证
            Intent intent = new Intent();
            intent.putExtra("verifyCode", verifycode.getPassWord());
            setResult(LoginRequest.REQUESTCODE_VERIFY,intent);
            finish();
            return;
        }
        
        if (inputCheck()) {
            getSalt();
        }
    }
    
    private String getEncryptStr(long time) {
        String encryptStr = new CryptUtils().getEncryptStr(
            phone,
            UcreditDreamApplication.clientId,
            time+"",
            UcreditDreamApplication.secret);
        return encryptStr.toUpperCase(Locale.getDefault());
    }
    
    /**
     * 获取盐，登录时通过secret获取
     */
    private void getSalt() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        long time = System.currentTimeMillis()*1000;
        StringBuilder sb = new StringBuilder();
        sb.append("?phone=").append(phone);
        sb.append("&clientId=").append(UcreditDreamApplication.clientId);
        sb.append("&t=").append(time);
        sb.append("&md=").append(getEncryptStr(time));
        httpClient.post(Constants.API_ENCRYPT_SALT + sb.toString(),
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							getSalt();
						}
					});
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("UcreditApplication", "get salt failure!"
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RegistPasswordActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getSalt();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("UcreditApplication", "getsalt() responseString:"
                        + responseString);
                    setMask(false);
                    UcreditDreamApplication.salt = JSON.parseObject(responseString)
                        .getString("result");

                    registerRequest();
                }

            });
    }
    
    private void registerRequest() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone);
        params.put("password",
            Utils.getEncryptPassword(password));
        params.put("verifyCode", verifycode.getPassWord());
        params.put("equipmentType", "ANDROID");
        params.put("equipmentId", UcreditDreamApplication.cid);
        params.put("dataSalt", UcreditDreamApplication.salt);
        params.put("equipmentId4CreditAudit", UcreditDreamApplication.clientId);
        params.put("tongdun", FMAgent.onEvent());
        params.put("bairong", BrAgent.getGid(this));
        params.put("clientIP", Utils.getLocalIpAddress());
        params.put("channel", UcreditDreamApplication.UMENG_CHANNEL);
        params.put("imei", Utils.getIMEI(this));
        params.put("email", "");
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.API_REGISTER, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
					checkNet(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							registerRequest();
						}
				    });
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("register", statusCode + " register error "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RegistPasswordActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                registerRequest();
                            }
                        }).handleMessage(statusCode);
                    
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("register", statusCode + " " + responseString);
                    setMask(false);
                    try {
                        JSONObject object = new JSONObject(responseString);
                        if (object.getBoolean("success")) {//没有注册过
                            User user = new User();
                            user.setUserId(object.getString("result"));
                            user.setPhone(phone);
                            user.setPassword(password);
                            
                            UcreditDreamApplication.mUser = user;

                            Utils.MakeToast(RegistPasswordActivity.this,
                                "注册成功！");

                            doRegist();
                            
//                            finish();
                        } else {
                            Utils.MakeToast(RegistPasswordActivity.this, object
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
            });

    }
    
    private void doRegist(){
        loginRequest = new LoginRequest(phone, password, this, new LoginListener() {
            
            @Override
            public void onSuccess() {
            	setMask(false);
                UserState userState=new UserState();
                userState.setFaceplus(false);
                userState.setIdentify(false);
                userState.setIdcard(false);
                UcreditDreamApplication.mUser.setUserState(userState);
                Intent intent = new Intent(RegistPasswordActivity.this,HomePageActivity.class);
                startActivity(intent);
                setResult(Activity.RESULT_OK);
                finish();
            }
            
            @Override
            public void onStart() {
				checkNet(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						doRegist();
					}
			    });            	
            	setMask(true);
            }
            
            @Override
            public void onFailure() {
            	setMask(false);
            }
        }, false);
        loginRequest.startLogin();
    }
    
    private void customDialog(Context context, String content,
            final DialogListenner dialogListenner) {
    	final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        View view = LayoutInflater.from(context).inflate(R.layout.alert_dialog,
            null);
        window.setContentView(view);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        LayoutParams params = window.getAttributes();
        Point point = new Point();
        display.getSize(point);
        params.width = (int) (point.x * 0.8);
        window.setAttributes(params);

        TextView titleTextView = (TextView) window.findViewById(R.id.title);
        TextView contentTextView = (TextView) window.findViewById(R.id.content);
        Button confirm = (Button) window.findViewById(R.id.confirm);
        Button cancel = (Button) window.findViewById(R.id.cancel);

        titleTextView.setText("温馨提示");
        contentTextView.setText(content);
        confirm.setText("确认");
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                dialogListenner.confirm();
            }
        });
        cancel.setText("取消");
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.cancel();
                cancelTag=false;
            }
        });
    }
    
    private void handleBack() {
		if (cancelTag) {
			customDialog(RegistPasswordActivity.this, "短信发送可能会有延迟，确定返回并重新开始？", new DialogListenner() {
				
				@Override
				public void confirm() {
					finish();
				}
			});
		} else {
		    finish();
		}
	}

	@Override
	public void onBackPressed() {
	    if(isVerify){
	        super.onBackPressed();
	        return;
	    }
		handleBack();
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
    
}
