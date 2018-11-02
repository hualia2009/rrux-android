package com.ucredit.dream.ui.activity.account;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.custom.widgt.CustomEditText;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.ui.activity.sign.ProtocolOfBorrowActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.EditTextUtils;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

public class RegistActivity extends BaseActivity {

    @ViewInject(R.id.phone)
    private CustomEditText phone;
    @ViewInject(R.id.password)
    private CustomEditText password;
    @ViewInject(R.id.next)
    private Button submit;
    @ViewInject(R.id.link)
    private TextView link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        Utils.setCommonEditText(phone, this);
        EditTextUtils editTextUtils=new EditTextUtils(password, this);
        editTextUtils.setPassword();	
        editTextUtils.setEditTextVisible();
        phone.setText(getPhoneNumber());

        new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		        imm.showSoftInput(phone,0); 
			}
		}, 100);
        phone.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (Utils.isNotEmptyString(getPhoneNumber())) {
					password.requestFocus();
				}
			}
		}, 200);
        TextWatcherListener changeListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                checkText();
            }
        });
        phone.addTextChangedListener(changeListener);
        password.addTextChangedListener(changeListener);
    }


	@Override
    protected boolean hasTitle() {
        return true;
    }

	private String getPhoneNumber(){   
	    TelephonyManager mTelephonyMgr;   
	    mTelephonyMgr = (TelephonyManager)  getSystemService(Context.TELEPHONY_SERVICE);  
	    String temp=mTelephonyMgr.getLine1Number();
	    if (!Utils.isNotEmptyString(temp)) {
			return"";
		}
	    int i=temp.indexOf("1");
	    if (i<0) {
	    	return"";
		}
	    return temp.substring(i); 
	}   
	
    @Override
    protected CharSequence getPublicTitle() {
        return "注册";
    }

    @Override
    protected int getContentId() {
        return R.layout.regist;
    }

    @OnClick({ R.id.link, R.id.next })
    public void onClick(View view) {
    	Intent intent;
        switch (view.getId()) {
            case R.id.link:
                intent=new Intent(this, ProtocolOfBorrowActivity.class);
                startActivity(intent);
                break;
            case R.id.next:
			    checkRegisted();
            	
                break;
            default:
                break;
        }
    }


	private void checkRegisted() {
		AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile", phone.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.API_IS_VALIDATE,
            params, new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("isregisted", statusCode + " onFailure "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RegistActivity.this, new GetTokenListener() {

                        @Override
                        public void onSuccess() {
                            checkRegisted();
                        }
                    }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    setMask(false);
                    Logger.e("isregisted", statusCode + " " + responseString);
                    try {
                    	JSONObject object = new JSONObject(responseString);
                        if (object.getBoolean("success")) {//没有注册过
                        	Intent intent=new Intent(RegistActivity.this, RegistPasswordActivity.class);
                            intent.putExtra("phone", phone.getText().toString());
                            intent.putExtra("password", password.getText().toString());
                            startActivityForResult(intent, LoginActivity.REQUEST_LOGIN);
                        } else {
                        	Utils.MakeToast(RegistActivity.this, object
                                    .getJSONObject("error").getString("message"));
                        }
					} catch (JSONException e) {
						e.printStackTrace();
					}
                    
                }
            });
	}


	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LoginActivity.REQUEST_LOGIN:
                if (Activity.RESULT_OK == resultCode) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }else if (RegistPasswordActivity.REGISTPASSWORD_HASREGISTED==resultCode) {
					finish();
				}
                break;
            default:
                break;
        }
    }  
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}


    
    public void checkText(){
        String phoneText = phone.getText().toString();
        String passwordText = password.getText().toString();
        if (Utils.isNotEmptyString(phoneText)
        		&&Utils.isNotEmptyString(passwordText)
        		&&phoneText.length()==11
        		&&passwordText.length()>=6) {
            submit.setEnabled(true);
        }else {
			submit.setEnabled(false);
		}
        if (phoneText.length()==11) {
			password.requestFocus();
		}
        Utils.replaceSpace(password);
    }

}
