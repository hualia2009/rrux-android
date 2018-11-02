package com.ucredit.dream.ui.activity;

import java.util.ArrayList;

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
import com.ucredit.dream.bean.Bank;
import com.ucredit.dream.bean.BranchBank;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.VerifyCodeTimer;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import exocr.bankcard.CardRecoActivity;
import exocr.bankcard.EXBankCardInfo;

public class BankCardAuthorizeActivity extends BaseActivity {

    @ViewInject(R.id.city)
    private TextView cityText;
    @ViewInject(R.id.branch)
    private TextView branchText;
    @ViewInject(R.id.bankcard)
    private EditText bankcardText; 
    @ViewInject(R.id.bank)
    private TextView bankText; 
    @ViewInject(R.id.verify)
    private EditText verify;
    @ViewInject(R.id.sendverify)
    private TextView sendverify;
    @ViewInject(R.id.scan)
    private ImageView scan;
    @ViewInject(R.id.submit)
    private Button submit;
    
    private final static int REQUEST_CITY=1;
    private final static int REQUEST_BRANCH=2;
    public final static int REQUEST_SCAN=3;
    private final static int REQUEST_TAKEBANKCARD=4;
    
    private String provinceId;
    private String cityId;
    private String branchId;
    private String bankId;
    private String bankName;
    
    private boolean signed;
    
    private ArrayList<Bank> banks;
    private ArrayList<String> bankNames;
    
    
    private String bankcardId;
    
    @OnClick({R.id.scan,R.id.submit,R.id.city,R.id.branch,R.id.bank,R.id.sendverify})
    private void onclick(View view) {
        switch (view.getId()) {
            case R.id.scan:
                Intent intentScan=new Intent(this, CardRecoActivity.class);
                startActivityForResult(intentScan, REQUEST_SCAN);
                break;
            case R.id.bank:
                if (banks!=null&&banks.size()>0) {
                    final AlertDialog bankDialog=Utils.spinnerDialog(this, bankNames);
                    ListView lessonList=(ListView) bankDialog.findViewById(R.id.list);
                    lessonList.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int arg2, long arg3) {
                            bankText.setText(bankNames.get(arg2));
                            bankId=banks.get(arg2).getBankId();
                            bankName=bankNames.get(arg2);
                            branchId=null;
                            branchText.setText("请选择支行");
                            bankDialog.dismiss();
                        }
                    });
                }else {
                    getBankList();
                }
                break;
            case R.id.submit:
            	if (submit.getText().toString().equals("下一步")) {
            		if (inputCheck2()) {
            		   Intent intentTake=new Intent(this, BankCardUploadActivity.class);
                       intentTake.putExtra("accountName", UcreditDreamApplication.mUser.getName());
                       intentTake.putExtra("bank", bankId);
                       intentTake.putExtra("bankName", bankName);
                       intentTake.putExtra("bankCardNo", bankcardText.getText().toString());
                       intentTake.putExtra("branch", branchText.getText().toString());
                       intentTake.putExtra("branchId", branchId);
                       intentTake.putExtra("city", cityId);
                       intentTake.putExtra("idCard", UcreditDreamApplication.mUser.getIdentityNo());
                       intentTake.putExtra("province", provinceId);
                       intentTake.putExtra("mobile", UcreditDreamApplication.mUser.getPhone());
                       startActivityForResult(intentTake, REQUEST_TAKEBANKCARD);
	                }
				} else {
					if (inputCheck()) {
	                    if (signed) {
	                        submit(Constants.BANKCARD_CHANGE);
	                    } else {
	                        submit(Constants.BANKCARD_AUTHORIZE);
	                    }
	                }
				}
                

                break;
            case R.id.city:
                Intent intentCity=new Intent(this, CityListActivity.class);
                startActivityForResult(intentCity, REQUEST_CITY);
                break;
            case R.id.branch:
                if (provinceId==null||cityId==null) {
                    Toast.makeText(this, "请先选择城市", Toast.LENGTH_SHORT).show();
                }else if (bankId==null) {
                    Toast.makeText(this, "请先选择开户行", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intentBranch=new Intent(this, BranchBankListActivity.class);
                    intentBranch.putExtra("provinceId", provinceId);
                    intentBranch.putExtra("cityId", cityId);
                    intentBranch.putExtra("bankId", bankId);
                    startActivityForResult(intentBranch, REQUEST_BRANCH);
                }
                break;
            default:
                break;
        }
    }
    
    private void submit(final String prefix) {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("accountName", UcreditDreamApplication.mUser.getName());
        params.put("bank", bankId);
        params.put("bankName", bankName);
        params.put("bankCardNo", bankcardText.getText().toString());
        params.put("branch", branchText.getText().toString());
        params.put("branchId", branchId);
        params.put("city", cityId);
        params.put("idCard", UcreditDreamApplication.mUser.getIdentityNo());
        params.put("province", provinceId);
        params.put("bankCardAttach", bankcardId);
        params.put("mobile", UcreditDreamApplication.mUser.getPhone());
        params.setUseJsonStreamer(true);
        httpClient.post(this, prefix, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("bankcard_authorize  success", statusCode + " " + responseString);
                    setMask(false);
                    com.alibaba.fastjson.JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        Utils.MakeToast(BankCardAuthorizeActivity.this, "银行卡信息保存成功");
                            Intent intent=new Intent(BankCardAuthorizeActivity.this, AdvertiseActivity.class);
                            intent.putExtra("title", "划扣授权书");
                            intent.putExtra("url", Constants.API_SIGN_PROTOCOL+"?applyid="
                                + UcreditDreamApplication.applyState.getId()
                                +"&type=5&protocolType=TRANSFER&access_token="
                                + UcreditDreamApplication.token
                                + "&clientid="
                                + UcreditDreamApplication.clientId
                                );
                            startActivityForResult(intent, REQUEST_TAKEBANKCARD);
                    }else {
                        Utils.MakeToast(BankCardAuthorizeActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("bankcard_authorize  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(BankCardAuthorizeActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                submit(prefix);
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							submit(prefix);
							
						}
					});
                    setMask(true);
                }
                
            }); 
    }     
     
    
    private boolean inputCheck() {
        if (!Utils.isNotEmptyString(bankId)) {
            Utils.MakeToast(this, "请选择您的开户银行");
            return false;
        }
        if (!Utils.isNotEmptyString(bankcardText.getText().toString())) {
            Utils.MakeToast(this, "请输入您的银行卡号");
            return false;
        }
        if (!Utils.isNotEmptyString(branchId)) {
            Utils.MakeToast(this, "请选择支行");
            return false;
        }
        if (!Utils.isNotEmptyString(provinceId)||!Utils.isNotEmptyString(cityId)) {
            Utils.MakeToast(this, "请选择城市");
            return false;
        }
        if (!Utils.isNotEmptyString(bankcardId)) {
            Utils.MakeToast(this, "请扫描银行卡");
            return false;
        }
        return true;
    }    
    private boolean inputCheck2() {
    	if (!Utils.isNotEmptyString(bankId)) {
    		Utils.MakeToast(this, "请选择您的开户银行");
    		return false;
    	}
    	if (!Utils.isNotEmptyString(bankcardText.getText().toString())) {
    		Utils.MakeToast(this, "请输入您的银行卡号");
    		return false;
    	}
    	if (!Utils.isNotEmptyString(branchId)) {
    		Utils.MakeToast(this, "请选择支行");
    		return false;
    	}
    	if (!Utils.isNotEmptyString(provinceId)||!Utils.isNotEmptyString(cityId)) {
    		Utils.MakeToast(this, "请选择城市");
    		return false;
    	}
    	return true;
    }    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CITY:
                if (Activity.RESULT_OK == resultCode) {
                    String city=data.getStringExtra("city");
                    setCity(city);
                }
                break;
            case REQUEST_BRANCH:
                if (Activity.RESULT_OK == resultCode) {
                    BranchBank branch=(BranchBank) data.getSerializableExtra("branch");
                    if (Utils.isNotEmptyString(branch.getName())) {
                        branchText.setText(branch.getName());
                        branchId=branch.getId();
                    }
                }
                break;
            case REQUEST_SCAN:
                if (CardRecoActivity.RESULT_CARD_INFO == resultCode) {
                    EXBankCardInfo cardInfo=data.getParcelableExtra(CardRecoActivity.EXTRA_SCAN_RESULT);
                    String id=data.getStringExtra("bankcardId");
                    bankcardId=id;
                    bankcardText.setEnabled(true);
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
                        bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                        bankId=banks.get(bankNames.indexOf(cardInfo.strBankName)).getBankId();
                        bankName=cardInfo.strBankName;
                        branchId=null;
                        branchText.setText("请选择支行");
                    } else {
                    	bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                    }
                    
                }else if (Activity.RESULT_OK == resultCode) {
                	bankcardText.setEnabled(true); 
                	submit.setText("下一步");
				}
                break;
            case REQUEST_TAKEBANKCARD:
                if (Activity.RESULT_OK == resultCode) {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void setCity(String city) {
        if (Utils.isNotEmptyString(city)) {
            int index=UcreditDreamApplication.cityNameList.indexOf(city);
            if (index>=0) {
                cityText.setText(UcreditDreamApplication.cityList.get(index).getProvince().getName()+" "+city);
                provinceId=""+UcreditDreamApplication.cityList.get(index).getProvince().getCode();
                cityId=""+UcreditDreamApplication.cityList.get(index).getCode();
                branchText.setText("请选择支行");
                branchId=null;
            }
        }
    }        
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "银行卡授权";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_bankcardauthorize;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getUserState()!=null) {
            signed=UcreditDreamApplication.mUser.getUserState().isSign();
        }
        if (getIntent().getStringExtra("id")==null) {
        	bankcardText.setEnabled(true);
        	submit.setText("下一步");
		}
        setCity(UcreditDreamApplication.city);
        if (savedInstanceState==null) {
        	getBankList();
		} else {
			if (banks==null) {
				banks=(ArrayList<Bank>) savedInstanceState.getSerializable("banks");
			}
			if (bankNames==null) {
				bankNames=(ArrayList<String>) savedInstanceState.getSerializable("bankNames");				
			}
			if (bankcardId==null) {
				bankcardId=savedInstanceState.getString("bankcardId");
			}
			EXBankCardInfo cardInfo=getIntent().getParcelableExtra("cardInfo");
            for (String string : bankNames) {
                if (cardInfo.strBankName.contains(string)) {
                    cardInfo.strBankName=string;
                    break;
                }
            }
            if (bankNames.indexOf(cardInfo.strBankName)>=0) {
                bankText.setText(cardInfo.strBankName);
                bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                bankId=banks.get(bankNames.indexOf(cardInfo.strBankName)).getBankId();
                bankName=cardInfo.strBankName;
                branchId=null;
                branchText.setText("请选择支行");
            } else {
            	bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
            }
            if (branchId==null) {
				branchId=savedInstanceState.getString("branchId");
				Logger.e("restorebranch", "id "+branchId);
				branchText.setText(savedInstanceState.getString("branchText"));
			}
		}
        
        
        scan.setImageResource(R.drawable.scan_anim);
        AnimationDrawable animationDrawable=(AnimationDrawable) scan.getDrawable();
        animationDrawable.start();
        
        Utils.isCameraGranted(this, new DialogListenner() {
            
            @Override
            public void confirm() {
                finish();
            }
        });
    }


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("banks", banks);
		outState.putSerializable("bankNames", bankNames);
		outState.putString("bankcardId", bankcardId);
		outState.putString("branchId", branchId);
		Logger.e("savebranch", "id "+branchId);
		outState.putString("branchText", branchText.getText().toString());
	}

	private void getBankList() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.BANK_LIST, params,
            new TextHttpResponseHandler() {

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

				@Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
					setMask(false);
                    Logger.e("bank_list  success", statusCode + " " + responseString);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if (response.getJSONArray("result")!=null) {
                            JSONArray jsonArray=response.getJSONArray("result");
                            banks=new ArrayList<Bank>();
                            bankNames=new ArrayList<String>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                Bank bank=new Bank();
                                bank.setBankId(jsonObject.getString("code"));
                                bank.setBankName(jsonObject.getString("name"));
                                banks.add(bank);
                                bankNames.add(jsonObject.getString("name"));
                            }
                            if (getIntent().getStringExtra("id")!=null) {
                            	bankcardId=getIntent().getStringExtra("id");
                            	bankcardText.setEnabled(true);
							}
                            if (getIntent().getParcelableExtra("cardInfo")!=null) {
                            	EXBankCardInfo cardInfo=getIntent().getParcelableExtra("cardInfo");
                                for (String string : bankNames) {
                                    if (cardInfo.strBankName.contains(string)) {
                                        cardInfo.strBankName=string;
                                        break;
                                    }
                                }
                                Logger.e("cardInfo.strBankName", cardInfo.strBankName);
                                if (bankNames.indexOf(cardInfo.strBankName)>=0) {
                                    bankText.setText(cardInfo.strBankName);
                                    bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                                    bankId=banks.get(bankNames.indexOf(cardInfo.strBankName)).getBankId();
                                    bankName=cardInfo.strBankName;
                                    branchId=null;
                                    branchText.setText("请选择支行");
                                } else {
                                	bankcardText.setText(cardInfo.strNumbers.replaceAll(" +",""));
                                }
							}
                        }
                    }else {
                        Utils.MakeToast(BankCardAuthorizeActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                	setMask(false);
                    Logger.e("bank_list  failure", statusCode + " " + responseString);
                    new RequestFailureHandler(BankCardAuthorizeActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getBankList();
                            }
                        }).handleMessage(statusCode);
                }
                
            }); 
    }

}
