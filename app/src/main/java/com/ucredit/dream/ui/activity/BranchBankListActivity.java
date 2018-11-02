package com.ucredit.dream.ui.activity;

import java.util.ArrayList;
import java.util.List;

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
import com.ucredit.dream.bean.BranchBank;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.view.ClearEditText;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BranchBankListActivity extends BaseActivity {

    @ViewInject(R.id.branchlist)
    private ListView sortListView;
    @ViewInject(R.id.filter_edit)
    private ClearEditText editText;
    
    private List<BranchBank> SourceDateList;
    private List<String> nameList;
    private BranchBankAdapter adapter;
    
    private String bankId;
    private String provinceId;
    private String cityId;
    
    
    @OnClick({R.id.cancle})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.cancle:
                finish();
                break;
            default:
                break;
        }
    }    
    
    @Override
    protected boolean hasTitle() {
        return false;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return null;
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_branchbanklist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        bankId=getIntent().getStringExtra("bankId");
        provinceId=getIntent().getStringExtra("provinceId");
        cityId=getIntent().getStringExtra("cityId");
        editText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        getBranchList();
    }

    private void getBranchList() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("bankId", bankId);
        params.put("cityId", cityId);
        params.put("provinceId", provinceId);
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.BRANCH_LIST, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                	setMask(false);
                    Logger.e("bank_list  success", statusCode + " " + responseString);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if(response.getJSONArray("result")!=null){
                            JSONArray jsonArray=response.getJSONArray("result");
                            SourceDateList=new ArrayList<BranchBank>();
                            nameList=new ArrayList<String>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object=jsonArray.getJSONObject(i);
                                BranchBank branchInfo=new BranchBank();
                                branchInfo.setName(object.getString("name"));
                                branchInfo.setId(object.getString("code"));
                                SourceDateList.add(branchInfo);
                                nameList.add(branchInfo.getName());
                            }
                            adapter = new BranchBankAdapter(SourceDateList);
                            sortListView.setAdapter(adapter);
                            sortListView.setOnItemClickListener(new OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> arg0,
                                        View arg1, int arg2, long arg3) {
                                    Intent data=new Intent();
                                    data.putExtra("branch", SourceDateList.get(arg2));
                                    setResult(Activity.RESULT_OK, data);
                                    finish();
                                }
                            });
                        }
                    }else {
                        Utils.MakeToast(BranchBankListActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                	setMask(false);
                    Logger.e("bank_list  failure", statusCode + " " + responseString);
                    new RequestFailureHandler(BranchBankListActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getBranchList();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener(){

						@Override
						public void onClick(View arg0) {
							getBranchList();
							
						}
                    	
                    });
                    setMask(true);
                }
                
                
                
            }); 
    }



    private void filterData(String filterStr){
        List<BranchBank> filterDateList = new ArrayList<BranchBank>();
        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(BranchBank sortModel : SourceDateList){
                String name = sortModel.getName();
                if(name.indexOf(filterStr.toString()) != -1 ){
                    filterDateList.add(sortModel);
                }
            }
        }
        adapter.updateListView(filterDateList);
    }        
    
    class BranchBankAdapter extends BaseAdapter {

        private List<BranchBank> list = new ArrayList<BranchBank>();

        public BranchBankAdapter(List<BranchBank> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public BranchBank getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                    R.layout.item_branchbank, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(list.get(position).getName());
            return convertView;
        }

        public void updateListView(final List<BranchBank> list){
            this.list = list;
            notifyDataSetChanged();
            sortListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0,
                        View arg1, int arg2, long arg3) {
                    Intent data=new Intent();
                    data.putExtra("branch", list.get(arg2));
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
            });
        }

        class ViewHolder {

            TextView name;

            public ViewHolder(View view) {
                name = (TextView) view.findViewById(R.id.name);
                view.setTag(this);
            }
        }
    }        
    
}
