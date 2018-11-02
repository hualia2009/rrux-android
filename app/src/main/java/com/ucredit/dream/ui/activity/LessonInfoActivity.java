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
import com.ucredit.dream.bean.LessonInfo;
import com.ucredit.dream.bean.ProductInfo;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class LessonInfoActivity extends BaseActivity {

    @ViewInject(R.id.next)
    private TextView button;
    @ViewInject(R.id.institution)
    private TextView institution;
    @ViewInject(R.id.schoolzone)
    private TextView schoolzone;
    @ViewInject(R.id.lesson)
    private TextView lesson;
    @ViewInject(R.id.price)
    private TextView price;
    @ViewInject(R.id.desc)
    private TextView desc;
    
    private ArrayList<LessonInfo> list;
    private ArrayList<String> lessonList;
    
    //校区id
    private String campusId;
    //校区名称
    private String campusName;
    //机构id
    private String orgId;
    //机构名称
    private String orgName;
    //当前选中的课程信息
    private LessonInfo currentInfo;
    //最小金额
    private String minAmount;
    
    
    @OnClick({R.id.next,R.id.lesson})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.next:
                if (currentInfo!=null) {
                  Intent intent=new Intent(this, PhaseApplyActivity.class);
                  intent.putExtra("campusId", campusId);
                  intent.putExtra("campusName", campusName);
                  intent.putExtra("orgId", orgId);
                  intent.putExtra("orgName", orgName);
                  intent.putExtra("lesson", currentInfo);
                  intent.putExtra("minAmount", minAmount);
                  startActivityForResult(intent,QRCodeActivity.REQUEST);
                }else {
                    Utils.MakeToast(this, "请选择课程");
                }
                break;
            case R.id.lesson:
                if (list!=null&&list.size()>0) {
                    final AlertDialog lessonDialog=Utils.spinnerDialog(this, lessonList);
                    ListView lessonList=(ListView) lessonDialog.findViewById(R.id.list);
                    lessonList.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int arg2, long arg3) {
                            lesson.setText(LessonInfoActivity.this.lessonList.get(arg2));
                            double d=Double.valueOf(LessonInfoActivity.this.list.get(arg2).getAmount());
                            price.setText((int)d+"元");
                            currentInfo=LessonInfoActivity.this.list.get(arg2);
                            button.setEnabled(true);
                            lessonDialog.dismiss();
                        }
                    });
                }
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case QRCodeActivity.REQUEST:
                if (resultCode==Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                break;
            default:
                break;
        }
    }
    
    private void getLessonInfo() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        Logger.e("campusId", campusId);
        params.put("id", campusId);
        params.setUseJsonStreamer(true);
        httpClient.post(this, Constants.LESSON_INFO, params,
            new TextHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                    Logger.e("lesson_info  success", statusCode + " " + responseString);
                    setMask(false);
                    JSONObject response = JSON.parseObject(responseString);
                    if (response.getBoolean("success")) {   
                        if(response.getJSONObject("result")!=null){
                            JSONObject jsonObject=response.getJSONObject("result");
                            campusName=jsonObject.getString("campusName");
                            orgName=jsonObject.getString("orgName");
                            orgId=jsonObject.getString("orgId");
                            minAmount=jsonObject.getString("minAmount");
                            campusId=jsonObject.getString("campusId");
                            desc.setText(jsonObject.getString("orgDesc"));
                            institution.setText(orgName);
                            schoolzone.setText(campusName);
                            JSONArray jsonArray=jsonObject.getJSONArray("course");
                            list=new ArrayList<LessonInfo>();
                            lessonList=new ArrayList<String>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object=jsonArray.getJSONObject(i);
                                LessonInfo lessonInfo=new LessonInfo();
                                lessonInfo.setName(object.getString("name"));
                                lessonInfo.setDiscription(object.getString("discription"));
                                lessonInfo.setAmount(object.getString("amount"));
                                lessonInfo.setId(object.getString("id"));
                                JSONArray product=object.getJSONArray("phase");
                                ArrayList<ProductInfo> productInfos=new ArrayList<ProductInfo>();
                                for (int j = 0; j < product.size(); j++) {
                                    ProductInfo info=new ProductInfo();
                                    info.setId(product.getJSONObject(j).getString("id"));
                                    info.setName(product.getJSONObject(j).getString("name"));
                                    info.setDesc(product.getJSONObject(j).getString("desc"));
                                    productInfos.add(info);
                                }
                                lessonInfo.setArrayList(productInfos);
                                list.add(lessonInfo);
                                lessonList.add(object.getString("name"));
                            }
                        }
                    }else {
                        Utils.MakeToast(LessonInfoActivity.this, response
                            .getJSONObject("error").getString("message"));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                    Logger.e("lesson_info  failure", statusCode + " " + responseString);
                    setMask(false);
                    new RequestFailureHandler(LessonInfoActivity.this,
                        new GetTokenListener() {

                            @Override
                            public void onSuccess() {
                                getLessonInfo();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						getLessonInfo();
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
        return "课程信息";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_lessoninfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        campusId=getIntent().getStringExtra("campusId");
        getLessonInfo();
    }

}
