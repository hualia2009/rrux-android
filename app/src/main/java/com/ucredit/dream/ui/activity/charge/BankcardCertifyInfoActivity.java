package com.ucredit.dream.ui.activity.charge;

import java.util.ArrayList;

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
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import exocr.bankcard.CardRecoActivity;
import exocr.bankcard.EXBankCardInfo;

public class BankcardCertifyInfoActivity extends BaseActivity {

    @ViewInject(R.id.card)
    private EditText cardText;
    @ViewInject(R.id.name)
    private EditText nameText;
    @ViewInject(R.id.idcard)
    private EditText idcardText;
    @ViewInject(R.id.phone)
    private EditText phoneText;
    @ViewInject(R.id.bank)
    private TextView bankText;
    @ViewInject(R.id.scan)
    private ImageView scan;
    
    private static final int REQUESTCODE_SCAN_BANK = 10;
    private static final int REQUESTCODE_BIND_BANK_VERIFY = 20;
    
    private String bankId;
    private ArrayList<String> bankNames;
    private ArrayList<String> bankCodes;
    
    @OnClick({R.id.submit,R.id.scan,R.id.bank})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                if (inputCheck()) {
                    submit();
                }
                break;
            case R.id.bank:
                if (bankNames!=null&&bankNames.size()>0) {
                    final AlertDialog bankDialog=Utils.spinnerDialog(this, bankNames);
                    ListView lessonList=(ListView) bankDialog.findViewById(R.id.list);
                    lessonList.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int arg2, long arg3) {
                            bankText.setText(bankNames.get(arg2));
                            bankId=bankCodes.get(arg2);
                            Logger.e("bankId", bankId);
                            bankDialog.dismiss();
                        }
                    });
                }
                break;
            case R.id.scan:
                Intent intent=new Intent(this, CardRecoActivity.class);
                startActivityForResult(intent, REQUESTCODE_SCAN_BANK);
                break;
            default:
                break;
        }
    }
    
    private void submit() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        final String string=cardText.getText().toString();
        RequestParams params = new RequestParams();
        params.put("bankCardNo", string);
        params.put("bankId", bankId);
        params.put("cardHolderName", nameText.getText().toString());
        params.put("idcard", idcardText.getText().toString());
        params.put("mobile", phoneText.getText().toString());
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.BIND_VERIFY, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("bind_bank  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if (response.getJSONObject("result")!=null) {
                            Intent intent=new Intent(BankcardCertifyInfoActivity.this,BindBankcardVerifyActivity.class);
                            intent.putExtra("bankCardNo", cardText.getText().toString());
                            intent.putExtra("bankId", bankId);
                            intent.putExtra("cardHolderName", nameText.getText().toString());
                            intent.putExtra("idcard", idcardText.getText().toString());
                            intent.putExtra("mobile", phoneText.getText().toString());
                            intent.putExtra("externalRefNumber", response.getJSONObject("result").getString("externalRefNumber"));
                            intent.putExtra("token", response.getJSONObject("result").getString("token"));
                            startActivityForResult(intent, REQUESTCODE_BIND_BANK_VERIFY);
                        }
                    }else {
                        Utils.MakeToast(BankcardCertifyInfoActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("bind_bank  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(BankcardCertifyInfoActivity.this,
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
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						submit();
    						
    					}
                		
                	});
                    setMask(true);
                }
                
            }); 
    }    
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(cardText.getText().toString())) {
            Utils.MakeToast(this, "请输入您的银行卡号");
            return false;
        }
        if (!Utils.isNotEmptyString(bankId)) {
            Utils.MakeToast(this, "请选择您的开户银行");
            return false;
        }
        if (!Utils.isNotEmptyString(nameText.getText().toString())) {
            Utils.MakeToast(this, "请您输入账户姓名");
            return false;
        }
        
        if (!Utils.isNotEmptyString(idcardText.getText().toString())) {
            Utils.MakeToast(this, "请您输入身份证号");
            return false;
        }
        if (!Utils.isNotEmptyString(phoneText.getText().toString())) {
            Utils.MakeToast(this, "请输入您的银行预留手机号");
            return false;
        }
        return true;
    }      
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUESTCODE_SCAN_BANK:
                if (CardRecoActivity.RESULT_CARD_INFO == resultCode) {
                    EXBankCardInfo cardInfo=data.getParcelableExtra(CardRecoActivity.EXTRA_SCAN_RESULT);
                    cardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                    if (null==bankNames||bankNames.size()<=0) {
                        return;
                    }
                    for (String string : bankNames) {
                        if (cardInfo.strBankName.contains(string)) {
                            cardInfo.strBankName=string;
                            break;
                        }
                    }
                    Logger.e("cardInfo.strBankName", cardInfo.strBankName);
                    if (bankNames.indexOf(cardInfo.strBankName)>=0) {
                        bankText.setText(cardInfo.strBankName);
                        bankId=bankCodes.get(bankNames.indexOf(cardInfo.strBankName));
                    }else {
						Utils.MakeToast(this, "暂不支持");
					}
                }
                break;
            case REQUESTCODE_BIND_BANK_VERIFY:
                if (Activity.RESULT_OK==resultCode) {
                	Logger.e("bindbanksuccess", "bindbanksuccess");
                    Intent intent=new Intent();
                    intent.putExtra("bankid", data.getStringExtra("bankid"));
                    intent.putExtra("card", data.getStringExtra("card"));
                    intent.putExtra("id", data.getStringExtra("id"));
                    setResult(Activity.RESULT_OK, intent);
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
        return "银行卡认证";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_bankcardcertifyinfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        bankNames=new ArrayList<String>();
        bankCodes=new ArrayList<String>();
        getBankList();
        nameText.setText(UcreditDreamApplication.mUser.getName());
        phoneText.setText(UcreditDreamApplication.mUser.getPhone());
        idcardText.setText(UcreditDreamApplication.mUser.getIdentityNo());
        
        Utils.isCameraGranted(this, new DialogListenner() {
            
            @Override
            public void confirm() {
                finish();
            }
        });
    }

    
    
	private void getBankList() {
		AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.SUPPORTED_BANKS, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("supported_banks  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if (response.getJSONArray("result")!=null) {
                            for (int i = 0; i < response.getJSONArray("result").size(); i++) {
								bankNames.add(response.getJSONArray("result").getJSONObject(i).getString("name"));
								bankCodes.add(response.getJSONArray("result").getJSONObject(i).getString("code"));
								
							}
                        }
                    }else {
                        Utils.MakeToast(BankcardCertifyInfoActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("supported_banks  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(BankcardCertifyInfoActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getBankList();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                	checkNet(new OnClickListener(){

    					@Override
    					public void onClick(View arg0) {
    						getBankList();
    						
    					}
                		
                	});
                    setMask(true);
                }
                
            }); 
	}
    
}
