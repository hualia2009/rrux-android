package com.ucredit.dream.ui.activity.charge;

import java.text.DecimalFormat;

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
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.BankLogoUtil;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class RechargePaybackInfoActivity extends BaseActivity {

    @ViewInject(R.id.amount)
    private EditText amountEdit;
    @ViewInject(R.id.card)
    private TextView cardText;
    @ViewInject(R.id.logo)
    private ImageView logo;
    @ViewInject(R.id.date)
    private TextView dateText;
    @ViewInject(R.id.should)
    private TextView shouldText;
    @ViewInject(R.id.monthly)
    private TextView monthlyText;
    @ViewInject(R.id.interest)
    private TextView interestText;
    @ViewInject(R.id.serve)
    private TextView serveText;
    @ViewInject(R.id.overdue)
    private TextView overdueText;
    @ViewInject(R.id.chargeall)
    private TextView chargeallText;
    
    private final int REQUEST_BIND_BANK=1;
    private final int REQUEST_VERIFY=2;
    private DecimalFormat df = new DecimalFormat("#.00");

    
    //进件id
    private String lendId;
    //银行卡id
    private String id;
    
    @OnClick({R.id.submit,R.id.card})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    double amountMoney = Double.valueOf(amountEdit.getText().toString());
                    Intent intentVerify=new Intent(this, RechargeVerifyActivity.class);
                    intentVerify.putExtra("lendId", lendId);
                    intentVerify.putExtra("id", id);
                    intentVerify.putExtra("totalAmount", df.format(amountMoney + Math.max( amountMoney * 0.35 / 100, 2)));
                    intentVerify.putExtra("fee", df.format(Math.max(amountMoney * 0.35 / 100, 2)));
                    intentVerify.putExtra("rechargeAmount", df.format(amountMoney));
                    startActivityForResult(intentVerify,REQUEST_VERIFY);
                }
                break;
            case R.id.card:
                    Intent intent=new Intent(RechargePaybackInfoActivity.this, RechargePaybackCardActivity.class);
                    startActivityForResult(intent, REQUEST_BIND_BANK);
                break;
            default:
                break;
        }
    }    
    
    
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(amountEdit.getText().toString())) {
            Utils.MakeToast(this, "请输入您的还款金额");
            return false;
        }
        if (null==id) {
            Utils.MakeToast(this, "请添加还款银行卡");
            return false;
        }
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BIND_BANK:
                if (resultCode==Activity.RESULT_OK) {
                    String card=data.getStringExtra("card");
                    String bankid=data.getStringExtra("bankid");
                    id=data.getStringExtra("id");
                    cardText.setText(card.substring(0, card.length()-4)+"****"+card.substring(card.length()-4, card.length()));
                    if (BankLogoUtil.getInstance().getLogoId(bankid)!=null) {
                    	logo.setImageResource(BankLogoUtil.getInstance().getLogoId(bankid));	
					}
                    
                }
                break;
            case REQUEST_VERIFY:
                if (resultCode==Activity.RESULT_OK) {
                    finish();
                }
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
        return "充值还款";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_rechargepaybackinfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        lendId=getIntent().getStringExtra("lendId");
        amountEdit.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                try {
                    double sum=Double.valueOf(arg0.toString());
                    chargeallText.setText("充值总额："+df.format(sum + Math.max( sum * 0.35 / 100, 2)));
                } catch (NumberFormatException e) {
                }
                
            }
            
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
                
            }
        });
        getBill();
        getBindedBancard();
    }



    private void getBill() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("applicationId", lendId);
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.BILL, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						getBill();
    						
    					}
                		
                	});
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("get_bill failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RechargePaybackInfoActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getBill();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("get_bill success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if (response.getJSONObject("result")!=null) {
                            JSONObject jsonObject=response.getJSONObject("result");
                            dateText.setText(jsonObject.getString("repayDate"));
                            shouldText.setText(jsonObject.getString("repayAmount"));
                            monthlyText.setText("月还本金："+jsonObject.getString("repayPrincipal"));
                            interestText.setText("当月利息："+jsonObject.getString("repayInterest"));
                            serveText.setText(jsonObject.getString("mgmtFee"));
                            overdueText.setText("逾期金额："+jsonObject.getString("overdueAmount"));
                            double sum=Double.valueOf(jsonObject.getString("repayPrincipal"))+Double.valueOf(jsonObject.getString("repayInterest"))+Double.valueOf(jsonObject.getString("mgmtFee"))+Double.valueOf(jsonObject.getString("overdueAmount"));
                            java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
                            amountEdit.setText(df.format(sum));
                            chargeallText.setText("充值总额："+df.format(sum + Math.max( sum * 0.35 / 100, 2)));
                        }else {
//                            popDialog();
                            dateText.setText("未出账单");
                            shouldText.setText("0");
                            monthlyText.setText("月还本金："+0);
                            interestText.setText("当月利息："+0);
                            serveText.setText("0");
                            overdueText.setText("逾期金额："+0);
                            amountEdit.setText("0");
                            chargeallText.setText("充值总额："+0);
                        }   
                    } else {
                        Utils.MakeToast(RechargePaybackInfoActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    private void getBindedBancard() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.BINDED_BANKLIST, params,
            new TextHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						getBindedBancard();
    						
    					}
                		
                	});
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("binded_firstbank failure", statusCode + " "
                        + responseString);
                    setMask(false);
                    new RequestFailureHandler(RechargePaybackInfoActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getBindedBancard();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("binded_firstbank success", statusCode + " "
                        + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if (response.getJSONArray("result") != null) {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if(jsonArray.size()>0){
                            JSONObject object = jsonArray.getJSONObject(0);
                            if (object!=null) {
                                String card = object.getString("storablePan");
                                String bankid = object.getString("bankId");
                                id = object.getString("id");
                                cardText.setText(card.substring(0, card.length()-4)+"****"+card.substring(card.length()-4, card.length()));
                                if (BankLogoUtil.getInstance().getLogoId(bankid)!=null) {
                                	logo.setImageResource(BankLogoUtil.getInstance().getLogoId(bankid));
								}
                            }
                            }
                        }
                    }
                }

            });
    }
    
    
}
