package com.ucredit.dream.ui.activity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.http.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.RepayPlan;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

public class RepayPlanActivity extends BaseActivity {

    @ViewInject(R.id.list)
    private ExpandableListView listView;
    
    private String lendId;
    
    private boolean signed=true;
    
    @ViewInject(R.id.payday)
    private TextView payDay;
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "还款计划";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_repayplan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        lendId=getIntent().getStringExtra("lendId");
        if (lendId==null&&UcreditDreamApplication.applyState!=null) {
            lendId=UcreditDreamApplication.applyState.getId();
        }
        if (UcreditDreamApplication.mUser!=null&&UcreditDreamApplication.mUser.getUserState()!=null) {
            signed=UcreditDreamApplication.mUser.getUserState().isSign();
        }
        getRepayList();
     
    }

    private void getRepayList() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("lendId", lendId);
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.REPAY_PLAN, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("repay_plan_list  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        
                        if(response.getJSONArray("result")!=null){
                            LinkedHashMap<String, ArrayList<RepayPlan>>hashMap=new LinkedHashMap<String, ArrayList<RepayPlan>>();
                            JSONArray jsonArray=response.getJSONArray("result");
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                String dateRange=jsonObject.getString("dateRange");
                                JSONArray array=jsonObject.getJSONArray("list");
                                ArrayList<RepayPlan> arrayList=new ArrayList<RepayPlan>();
                                for (int j = 0; j < array.size(); j++) {
                                    JSONObject object=array.getJSONObject(j);
                                    RepayPlan repayPlan=new RepayPlan();
                                    repayPlan.setDate(object.getString("RepayDate"));
                                    repayPlan.setMoney(object.getString("TotalAmount"));
                                    repayPlan.setTerm(object.getString("Phase"));
                                    arrayList.add(repayPlan);
                                }
                                hashMap.put(dateRange, arrayList);
                            }
                            RepayPlanAdapter adapter=new RepayPlanAdapter(hashMap);
                            listView.setAdapter(adapter);
                            listView.setGroupIndicator(null);
                            for(int i = 0; i < adapter.getGroupCount(); i++){  
                                listView.expandGroup(i);                        
                            }  
                            listView.setOnGroupClickListener(new OnGroupClickListener() {
                                
                                @Override
                                public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
                                        long arg3) {
                                    return true;
                                }
                            });
                        }
                    }else {
                        Utils.MakeToast(RepayPlanActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("repay_plan_list  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(RepayPlanActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getRepayList();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getRepayList();
    					}
    				  });
                    setMask(true);
                }
                
            }); 
    }

    class RepayPlanAdapter extends BaseExpandableListAdapter {

        private ArrayList<String> groups;

        private LinkedHashMap<String, ArrayList<RepayPlan>> hashMap;
        
        private RepayPlanAdapter(LinkedHashMap<String, ArrayList<RepayPlan>> hashMap) {
            super();
            this.hashMap=hashMap;
            groups=new ArrayList<String>();
            for (String string : hashMap.keySet()) {
                groups.add(string);
            }
        }

        @Override
        public Object getChild(int arg0, int arg1) {
            return null;
        }

        @Override
        public long getChildId(int arg0, int arg1) {
            return 0;
        }

        @Override
        public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
                ViewGroup arg4) {
            ChildViewHolder holder;
            if (null==arg3) {
                holder=new ChildViewHolder();
                arg3=View.inflate(RepayPlanActivity.this, R.layout.item_child_repayplan, null);
                holder.left=(TextView) arg3.findViewById(R.id.term);
                holder.center=(TextView) arg3.findViewById(R.id.date);
                holder.right=(TextView) arg3.findViewById(R.id.money);
                arg3.setTag(holder);
            } else {
                holder=(ChildViewHolder) arg3.getTag();
            }
            holder.center.setVisibility(View.VISIBLE);
            if (signed) {
                holder.center.setText(hashMap.get(groups.get(arg0)).get(arg1).getDate());
            } else {
                holder.center.setText("——");
            }
            holder.left.setText(hashMap.get(groups.get(arg0)).get(arg1).getTerm());
            
            holder.right.setText(hashMap.get(groups.get(arg0)).get(arg1).getMoney());
            return arg3;
        }

        @Override
        public int getChildrenCount(int arg0) {
            return hashMap.get(groups.get(arg0)).size();
        }

        @Override
        public Object getGroup(int arg0) {
            return null;
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public long getGroupId(int arg0) {
            return 0;
        }

        @Override
        public View getGroupView(int arg0, boolean arg1, View arg2,
                ViewGroup arg3) {
            GroupViewHolder holder;
            if (null==arg2) {
                holder=new GroupViewHolder();
                arg2=View.inflate(RepayPlanActivity.this, R.layout.item_group_repayplan, null);
                holder.textView=(TextView) arg2.findViewById(R.id.group);
                arg2.setTag(holder);
            } else {
                holder=(GroupViewHolder) arg2.getTag();
            }
            holder.textView.setVisibility(View.VISIBLE);
            if (!signed) {
                holder.textView.setText("");
            }else {
            	holder.textView.setText(groups.get(arg0));
			}
            return arg2;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int arg0, int arg1) {
            return false;
        }
        
        private  final class GroupViewHolder{
            public TextView  textView; 
        }
        private  final class ChildViewHolder{
            public TextView left; 
            public TextView center; 
            public TextView right;
        }
        
    }        
    
}
