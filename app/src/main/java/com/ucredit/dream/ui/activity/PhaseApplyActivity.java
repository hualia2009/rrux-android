package com.ucredit.dream.ui.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.bean.LessonInfo;
import com.ucredit.dream.bean.ProductInfo;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class PhaseApplyActivity extends BaseActivity {

    @ViewInject(R.id.submit)
    private TextView button;
    @ViewInject(R.id.money)
    private EditText money;
    @ViewInject(R.id.way)
    private TextView way;
    @ViewInject(R.id.deadline)
    private TextView deadline;
    @ViewInject(R.id.deadlinedetail)
    private TextView deadlineDetail;
    @ViewInject(R.id.calculate)
    private TextView calculate;
    @ViewInject(R.id.range)
    private TextView range;
    
    //校区id
    private String campusId;
    //校区名称
    private String campusName;
    //机构id
    private String orgId;
    //机构名称
    private String orgName;
    
    private int amount;
    private int minAmount;
    //当前选中的课程信息
    private LessonInfo currentInfo;
    //当前选中的产品信息
    private ProductInfo currentProduct;
    
    private ArrayList<String> deadlineList;
    
    private Handler handler;
    private Runnable runnable;
    
    @OnClick({R.id.submit,R.id.deadline,R.id.calculate})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.submit:
                if (!Utils.isNotEmptyString(money.getText().toString())) {
                    Utils.MakeToast(this, "请输入分期金额");
                }else {
                    try {
                        int i=Integer.valueOf(money.getText().toString());
                        if (i<minAmount||i>amount) {
                            Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
                            money.setText("");
                        }else if (null==currentProduct) {
                            Utils.MakeToast(this, "请选择还款期限");
                        }else if (!Utils.isNotEmptyString(deadlineDetail.getText().toString())) {
                            Utils.MakeToast(this, "请先试算");
                        }else {
                            Utils.customDialog(this, "借款额度："+money.getText().toString()+
                                "，还款期限："+currentProduct.getName()+"，是否确认", new DialogListenner() {
                                
                                @Override
                                public void confirm() {
                                    submit();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
                        money.setText("");
                    }
                }
                break;
            case R.id.calculate:
                checkCalculate();
                break;
            case R.id.deadline:
                final AlertDialog deadlineDialog = Utils.spinnerDialog(this,
                    deadlineList);
                ListView deadlineList = (ListView) deadlineDialog.findViewById(R.id.list);
                deadlineList.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {
                        if (currentInfo.getArrayList().get(arg2).getName().startsWith("0")) {
                            way.setText("等额本息");
                        } else {
                            way.setText("先息后本");
                        }
                        deadline.setText(PhaseApplyActivity.this.deadlineList.get(arg2));
                        currentProduct = currentInfo.getArrayList().get(arg2);
                        deadlineDetail.setText("");
                        deadlineDialog.dismiss();
                        check();
                        handler.post(runnable);
                    }
                });
                break;   
            default:
                break;
        }
    }


    
    private void check() {
    	if (!Utils.isNotEmptyString(money.getText().toString())) {
    		Utils.MakeToast(this, "请输入分期金额");
    	}else {
    		try {
    			int i=Integer.valueOf(money.getText().toString());
    			if (i<minAmount||i>amount) {
    				Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
    			}else {
					button.setEnabled(true);
				}
    		} catch (Exception e) {
    			Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
    		}
    		
    	}
    }   
    
    
    private void checkCalculate() {
        if (!Utils.isNotEmptyString(money.getText().toString())) {
            Utils.MakeToast(this, "请输入分期金额");
            button.setEnabled(false);
        }else {
            try {
                int i=Integer.valueOf(money.getText().toString());
                if (i<minAmount||i>amount) {
                    Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
                    button.setEnabled(false);
                }else if (null==currentProduct) {
                    Utils.MakeToast(this, "请您选择还款期限");
                    button.setEnabled(false);
                }else {
                    getCalculate();
                }
            } catch (Exception e) {
                Utils.MakeToast(this, "分期金额只能在"+minAmount+"-"+amount+"范围内");
                button.setEnabled(false);
            }
            
        }
    }    

    private void submit() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("applyAmount", money.getText().toString());
        params.put("campusId", campusId);
        params.put("campusName", campusName);
        params.put("courseId", currentInfo.getId());
        params.put("courseName", currentInfo.getName());
        params.put("orgId", orgId);
        params.put("orgName", orgName);
        params.put("period", currentProduct.getName());
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.LESSON_SUBMIT, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("lesson_submit  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        Utils.MakeToast(PhaseApplyActivity.this, "提交成功");
                        setResult(Activity.RESULT_OK);
                        finish();
                    }else {
                        Utils.MakeToast(PhaseApplyActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("lesson_submit  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(PhaseApplyActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                submit();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						submit();
    					}
    				  });
                    setMask(true);
                }
                
            }); 
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "分期申请";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_phaseapply;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        Intent intent=getIntent();
        campusId=intent.getStringExtra("campusId");
        campusName=intent.getStringExtra("campusName");
        orgId=intent.getStringExtra("orgId");
        orgName=intent.getStringExtra("orgName");
        double dou=Double.valueOf(intent.getStringExtra("minAmount"));
        minAmount=(int)dou;
        currentInfo=(LessonInfo) intent.getSerializableExtra("lesson");
        double d=Double.valueOf(currentInfo.getAmount());
        amount=(int) d;
        range.setText("额度范围"+minAmount+"-"+amount+"元");
        deadlineList=new ArrayList<String>();
        for (ProductInfo info : currentInfo.getArrayList()) {
            deadlineList.add(info.getName());
        }
        handler = new Handler(){
        };
        runnable = new Runnable(){

            @Override
            public void run() {
                checkCalculate();
            }
            
        };
        money.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
                    deadlineDetail.setText("");
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1500);
            }
            
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {

            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
               
            }
        });
        new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		        imm.showSoftInput(money,0); 
			}
		}, 200);
    }

    protected void getCalculate() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("amount", money.getText().toString());
        params.put("orgId", orgId);
        params.put("period", currentProduct.getName());
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.LESSON_CALCULATE, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("lesson_calculate  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if(response.getString("result")!=null){
                            deadlineDetail.setText(response.getString("result"));
                        }
                        button.setEnabled(true);
                    }else {
                        Utils.MakeToast(PhaseApplyActivity.this, response
                            .getJSONObject("error").getString("message"));
                        button.setEnabled(false);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("lesson_calculate  failure", statusCode + " " + responseString);
                    setMask(false);
                    button.setEnabled(false);
                    new RequestFailureHandler(PhaseApplyActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getCalculate();
                            }
                        }).handleMessage(statusCode);
                }
                
                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getCalculate();
    					}
    				  });
                    setMask(true);
                }
                
            }); 
    }

}
