package com.ucredit.dream.ui.activity;

import java.util.ArrayList;

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
import com.ucredit.dream.bean.StudentAidRecord;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class StudentAidRecordActivity extends BaseActivity {

    @ViewInject(R.id.list)
    private ListView listView;
    @ViewInject(R.id.empty)
    private TextView emptyView;

    private ArrayList<StudentAidRecord> list;
    
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "助学记录";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_studentaidrecord;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        listView.setEmptyView(emptyView);
        getAidList();
    }

    private void getAidList() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.STUDENT_AID_RECORD, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("student_aid_list  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if(response.getJSONArray("result")!=null){
                            JSONArray jsonArray=response.getJSONArray("result");
                            list=new ArrayList<StudentAidRecord>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                StudentAidRecord record=new StudentAidRecord();
                                record.setAmount(jsonObject.getString("actualAmount"));
                                record.setLendId(jsonObject.getString("id"));
                                record.setLesson(jsonObject.getString("courseName"));
                                record.setStatus(jsonObject.getString("state"));
                                record.setDate(jsonObject.getString("lendDate"));
                                record.setAbacusLoanId(jsonObject.getString("abacusLoanId"));
                                record.setApplyAmount(jsonObject.getString("applyAmount"));
                                list.add(record);
                            }
                            StudentAidRecordAdapter adapter=new StudentAidRecordAdapter(list);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new OnItemClickListener() {
                  
                              @Override
                              public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                      long arg3) {
                                      Intent intent=new Intent(StudentAidRecordActivity.this, StudentAidDetailActivity.class);
                                      intent.putExtra("lendId", list.get(arg2).getLendId());
                                      startActivity(intent);
                              }
                            });
                        }
                    }else {
                        Utils.MakeToast(StudentAidRecordActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("student_aid_list  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(StudentAidRecordActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getAidList();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getAidList();
    					}
    				  });
                    setMask(true);
                }
                
                
                
            }); 
    }
    
    class StudentAidRecordAdapter extends BaseAdapter {

        private ArrayList<StudentAidRecord> list = new ArrayList<StudentAidRecord>();

        public StudentAidRecordAdapter(ArrayList<StudentAidRecord> list) {
            super();
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public StudentAidRecord getItem(int position) {
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
                    R.layout.item_studentaidrecord, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.date.setText(getItem(position).getDate());
            
            holder.status.setText(getItem(position).getStatus());
            GradientDrawable shapeDrawable=(GradientDrawable) holder.status.getBackground();
            shapeDrawable.setColor(getColor(getItem(position).getStatus()));
            String string=holder.status.getText().toString();
            if ("拒".equals(string)||"申".equals(string)||"审".equals(string)) {
                holder.amount.setText("申请金额"+Integer.valueOf(getItem(position).getApplyAmount())/100+"元");
            } else {
                holder.amount.setText("共借款"+Integer.valueOf(getItem(position).getAmount())/100+"元");
            }
            holder.lesson.setText(getItem(position).getLesson());
            return convertView;
        }

        
        private int getColor(String state){
            int color=0;
            if ("申".equals(state)) {
                color=getResources().getColor(R.color.shen1);
            }else if ("拒".equals(state)) {
                color=getResources().getColor(R.color.ju);
            }else if ("审".equals(state)) {
                color=getResources().getColor(R.color.shen2);
            }else if ("签".equals(state)) {
                color=getResources().getColor(R.color.qian);
            }else if ("还".equals(state)) {
                color=getResources().getColor(R.color.huan);
            }else {
                color=getResources().getColor(R.color.qing);
            }
            return color;
        }
        

        class ViewHolder {

            TextView date;
            TextView status;
            TextView amount;
            TextView lesson;
            ImageView arrow;

            public ViewHolder(View view) {
                date = (TextView) view.findViewById(R.id.date);
                status = (TextView) view.findViewById(R.id.status);
                lesson = (TextView) view.findViewById(R.id.lesson);
                amount = (TextView) view.findViewById(R.id.amount);
                arrow = (ImageView) view.findViewById(R.id.arrow);
                view.setTag(this);
            }
        }
    }    
    
}
