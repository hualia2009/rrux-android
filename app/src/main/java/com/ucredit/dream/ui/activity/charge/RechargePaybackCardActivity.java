package com.ucredit.dream.ui.activity.charge;

import java.util.ArrayList;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.bean.BankCard;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.BankLogoUtil;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class RechargePaybackCardActivity extends BaseActivity {

    @ViewInject(R.id.list)
    private ListView listView;
    @ViewInject(R.id.empty)
    private TextView empty;
    
    private static final int ADD_BINDED_BANK = 10;
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "充值还款";
    }

    @Override
    protected void setRightButton(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.addcard);
        imageView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent(RechargePaybackCardActivity.this, BankcardCertifyInfoActivity.class);
                startActivityForResult(intent, ADD_BINDED_BANK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            Intent intent=new Intent();
            intent.putExtra("id", data.getStringExtra("id"));
            intent.putExtra("card", data.getStringExtra("card"));
            intent.putExtra("bankid", data.getStringExtra("bankid"));
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        
    }    
    
    @Override
    protected boolean hasRightButton() {
        return true;
    }    
    
    @Override
    protected int getContentId() {
        return R.layout.activity_rechargepaybackcard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        listView.setEmptyView(empty);
        getBindedBancard();
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
                	setMask(false);
                    Logger.e("binded_banklist failure", statusCode + " "
                        + responseString);
                    new RequestFailureHandler(RechargePaybackCardActivity.this,
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
                	setMask(false);
                    Logger.e("binded_banklist success", statusCode + " " + responseString);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        final ArrayList<BankCard> list = new ArrayList<BankCard>();
                        if (response.getJSONArray("result")!=null) {
                            JSONArray jsonArray=response.getJSONArray("result");
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object=jsonArray.getJSONObject(i);
                                BankCard bankCard=new BankCard();
                                bankCard.setBankId(object.getString("bankId"));
                                bankCard.setBankName(object.getString("bankName"));
                                bankCard.setId(object.getString("id"));
                                bankCard.setStorablePan(object.getString("storablePan"));
                                list.add(bankCard);
                            }
                        }
                        if (list.size()>0) {
                            RechargePaybackCardAdapter adapter=new RechargePaybackCardAdapter(list);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> arg0,
                                        View arg1, int arg2, long arg3) {
                                    Intent data=new Intent();
                                    data.putExtra("bankid", list.get(arg2).getBankId());
                                    data.putExtra("card", list.get(arg2).getStorablePan());
                                    data.putExtra("id", list.get(arg2).getId());
                                    setResult(Activity.RESULT_OK, data);
                                    finish();
                                }
                            });
                        }
                    } else {
                        Utils.MakeToast(RechargePaybackCardActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

            });
    }

    class RechargePaybackCardAdapter extends BaseAdapter {

        private ArrayList<BankCard> list = new ArrayList<BankCard>();

        public RechargePaybackCardAdapter(ArrayList<BankCard> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public BankCard getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                    R.layout.item_rechargepaybackcard, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(getItem(position).getBankName());
            holder.number.setText(getItem(position).getStorablePan().substring(0, getItem(position).getStorablePan().length()-4)+"****"+getItem(position).getStorablePan().substring(getItem(position).getStorablePan().length()-4, getItem(position).getStorablePan().length()));
            if (BankLogoUtil.getInstance().getLogoId(getItem(position).getBankId())!=null) {
            	holder.logo.setImageResource(BankLogoUtil.getInstance().getLogoId(getItem(position).getBankId()));
			}
            holder.cancle.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    Utils.customDialog(RechargePaybackCardActivity.this, "确定要删除此银行卡", new DialogListenner() {
                        
                        @Override
                        public void confirm() {
                            deleteBankCard(getItem(position),list);
                            
                        }
                    });
                }
            });
            return convertView;
        }

        protected void deleteBankCard(final BankCard bankCard,final ArrayList<BankCard> list) {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("id", bankCard.getId());
            params.setUseJsonStreamer(true);
            httpClient.post(Constants.DELETE_BANK, params,
                new TextHttpResponseHandler() {

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                            String responseString, Throwable throwable) {
                    	setMask(false);
                        Logger.e("delete_bankcard failure", statusCode + " "
                            + responseString);
                        new RequestFailureHandler(RechargePaybackCardActivity.this,
                            new GetTokenListener() {

                                @Override
                                public void onSuccess() {
                                    deleteBankCard(bankCard,list);
                                }
                            }).handleMessage(statusCode);
                    }

                    @Override
					public void onStart() {
						super.onStart();
		            	checkNet(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								deleteBankCard(bankCard, list);
								
							}
		            		
		            	});
		            	setMask(true);
					}

					@Override
                    public void onSuccess(int statusCode, Header[] headers,
                            String responseString) {
                    	setMask(true);
                        Logger.e("delete_bankcard", statusCode + " " + responseString);
                        JSONObject response = JSON.parseObject(responseString);
                        if (response.getBooleanValue("success")) {
                            Utils.MakeToast(RechargePaybackCardActivity.this, "删除成功！");
                            list.remove(bankCard);
                            notifyDataSetChanged();
                        } else {
                            Utils.MakeToast(RechargePaybackCardActivity.this, response
                                .getJSONObject("error").getString("message"));
                        }
                    }

                });
        }

        class ViewHolder {

            ImageView logo;
            TextView name;
            TextView type;
            TextView number;
            ImageView cancle;

            public ViewHolder(View view) {
                logo = (ImageView) view.findViewById(R.id.logo);
                name = (TextView) view.findViewById(R.id.name);
                type = (TextView) view.findViewById(R.id.type);
                number = (TextView) view.findViewById(R.id.number);
                cancle = (ImageView) view.findViewById(R.id.cancle);
                view.setTag(this);
            }
        }
    }        
    
}
