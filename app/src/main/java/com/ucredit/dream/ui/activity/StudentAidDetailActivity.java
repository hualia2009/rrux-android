package com.ucredit.dream.ui.activity;

import org.apache.http.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.charge.RechargePaybackInfoActivity;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import exocr.bankcard.CardRecoActivity;
import exocr.bankcard.EXBankCardInfo;

public class StudentAidDetailActivity extends BaseActivity {

    @ViewInject(R.id.repay_plan)
    private RelativeLayout repayPlan;
    @ViewInject(R.id.loan_protocol)
    private RelativeLayout loanProtocol;
    @ViewInject(R.id.repay_protocol)
    private RelativeLayout repayProtocol;
    @ViewInject(R.id.change_bankcard)
    private RelativeLayout changeBankcard;
    @ViewInject(R.id.repay_finished)
    private RelativeLayout repayFinished;
    @ViewInject(R.id.amount)
    private TextView amount;
    @ViewInject(R.id.way)
    private TextView way;
    @ViewInject(R.id.first_deadline)
    private TextView firstDeadline;
    @ViewInject(R.id.first_permonth)
    private TextView firstPermonth;
    @ViewInject(R.id.second_deadline)
    private TextView secondDeadline;
    @ViewInject(R.id.second_permonth)
    private TextView secondPermonth;
    @ViewInject(R.id.secondline)
    private View secondLine;
    @ViewInject(R.id.thirdline)
    private View thirdLine;
    @ViewInject(R.id.fourthline)
    private View fourLine;
    
    //进件号
    private String lendId;
    
    private String lendAgreement;
    private String creditAgreement;
    
    private String state;//进件状态
    
    private boolean signed;
    
    @OnClick({R.id.repay_plan,R.id.loan_protocol,R.id.repay_protocol,R.id.change_bankcard,R.id.repay_finished})
    private void onclick(View view){
        Intent intent;
        switch (view.getId()) {
            case R.id.repay_plan:
                intent=new Intent(this, RepayPlanActivity.class);
                intent.putExtra("lendId", lendId);
                startActivity(intent);
                break;
            case R.id.loan_protocol:
                intent = new Intent(this,AdvertiseActivity.class);
                intent.putExtra("title", "借款协议");
                intent.putExtra("zoom", true);
                intent.putExtra("pdf", true);
                intent.putExtra("url", Constants.PDF+lendAgreement);
                startActivity(intent);
                break;
            case R.id.repay_protocol:
                intent = new Intent(this,AdvertiseActivity.class);
                intent.putExtra("title", "信用服务协议");
                intent.putExtra("zoom", true);
                intent.putExtra("pdf", true);
                intent.putExtra("url", Constants.PDF+creditAgreement);
                startActivity(intent);
                break;
            case R.id.change_bankcard:
                intent = new Intent(this,CardRecoActivity.class);
                intent.putExtra("type", "NoActivityResult");
                startActivity(intent);
                break;
            case R.id.repay_finished:
            	intent = new Intent(this,AdvertiseActivity.class);
            	intent.putExtra("url", Constants.PAYED_FINISHED+UcreditDreamApplication.applyState.getId()+
            			"?access_token="
                                + UcreditDreamApplication.token
                                + "&clientid="
                                + UcreditDreamApplication.clientId);
            	startActivity(intent);
            	break;
            default:
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case BankCardAuthorizeActivity.REQUEST_SCAN:
            if (CardRecoActivity.RESULT_CARD_INFO == resultCode) {
                EXBankCardInfo cardInfo=data.getParcelableExtra(CardRecoActivity.EXTRA_SCAN_RESULT);
                String id=data.getStringExtra("bankcardId");
                Intent intent=new Intent(this, BankCardAuthorizeActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("cardInfo", cardInfo);
                startActivity(intent); 
            }else if (signed) {
            	Intent intent=new Intent(this, BankCardAuthorizeActivity.class);
                startActivity(intent); 
			}
        	break;
        default:
            break;
        }    
            
    }

    @Override
    protected void setRightButton(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.card);
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent(StudentAidDetailActivity.this, RechargePaybackInfoActivity.class);
                intent.putExtra("lendId", lendId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected boolean hasRightButton() {
        return true;
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "助学明细";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_studentaiddetail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        lendId=getIntent().getStringExtra("lendId");
        if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getUserState()!=null) {
            signed=UcreditDreamApplication.mUser.getUserState().isSign();
        }
        if (signed) {
            changeBankcard.setVisibility(View.VISIBLE);
            fourLine.setVisibility(View.VISIBLE);
        } else {
            changeBankcard.setVisibility(View.GONE);
            fourLine.setVisibility(View.GONE);
        }
        getAidDetail();
    }

    private void getAidDetail() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("lendId", lendId);
        Logger.e("getAidDetail", lendId);
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.STUDENT_AID_DETAIL, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("student_aid_detail  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if(response.getJSONObject("result")!=null){
                            JSONObject jsonObject=response.getJSONObject("result");
                            amount.setText(jsonObject.getString("applyAmount")+"元");
                            way.setText(jsonObject.getString("repayWay"));
                            if (jsonObject.getBoolean("isSettled")) {
								repayFinished.setVisibility(View.VISIBLE);
								repayPlan.setVisibility(View.GONE);
							} else {
                                repayFinished.setVisibility(View.GONE);
                                repayPlan.setVisibility(View.VISIBLE);
							}
                            JSONArray array=jsonObject.getJSONArray("deadline");
                            if(array != null){
                                for (int i = 0; i < array.size(); i++) {
                                    String[] strings=array.getString(i).split("#");
                                    if (0==i) {
                                        firstDeadline.setText(strings[0]);
                                        firstPermonth.setText(strings[1]);
                                    } else if (1==i) {
                                        secondDeadline.setText(strings[0]);
                                        secondPermonth.setText(strings[1]);
                                    }
                                }
                            }
                            state=jsonObject.getString("state");
                            if ("false".equals(state)) {
                                findViewById(R.id.imgRight).setVisibility(View.GONE);
                            }
                            lendAgreement=jsonObject.getString("lendAgreement");
                            creditAgreement=jsonObject.getString("creditAgreement");
                            if (!Utils.isNotEmptyString(lendAgreement)) {
                                loanProtocol.setVisibility(View.GONE);
                                secondLine.setVisibility(View.GONE);
                            }
                            if (!Utils.isNotEmptyString(creditAgreement)) {
                                repayProtocol.setVisibility(View.GONE);
                                thirdLine.setVisibility(View.GONE);
                            }
                        }else {
                            loanProtocol.setVisibility(View.GONE);
                            secondLine.setVisibility(View.GONE);
                            repayProtocol.setVisibility(View.GONE);
                            thirdLine.setVisibility(View.GONE);
                        }
                    }else {
                        Utils.MakeToast(StudentAidDetailActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("student_aid_detail  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(StudentAidDetailActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getAidDetail();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getAidDetail();
    					}
    				  });
                    setMask(true);
                }
                
                
                
            }); 
    }

}
