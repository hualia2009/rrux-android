package com.ucredit.dream.ui.fragment;

import org.apache.http.Header;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.ApplyState;
import com.ucredit.dream.bean.UserState;
import com.ucredit.dream.ui.activity.QRCodeActivity;
import com.ucredit.dream.ui.activity.RepayPlanActivity;
import com.ucredit.dream.ui.activity.StudentAidDetailActivity;
import com.ucredit.dream.ui.activity.StudentAidRecordActivity;
import com.ucredit.dream.ui.activity.apply.ApplyActivity;
import com.ucredit.dream.ui.activity.h5.AdvertiseActivity;
import com.ucredit.dream.ui.activity.h5.HeikaActivity;
import com.ucredit.dream.ui.activity.main.HomePageActivity;
import com.ucredit.dream.ui.activity.sign.SignDetailActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.Utils.TextViewClickListener;
import com.ucredit.dream.utils.request.ApplyStateRequest;
import com.ucredit.dream.utils.request.ApplyStateRequest.QueryApplyListener;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;
import com.ucredit.dream.utils.request.UserStateRequest;
import com.ucredit.dream.utils.request.UserStateRequest.QueryUserListener;

/**
 * @author Li Huang
 */
@SuppressLint("ValidFragment")
public class MainContentFragment extends Fragment {

    @ViewInject(R.id.pullscrollview)
    private PullToRefreshScrollView pullToRefreshScrollView;
    
    @ViewInject(R.id.left_menu)
    private ImageView leftMenu;
    @ViewInject(R.id.center_btn)
    private ImageView centerBtn;
    @ViewInject(R.id.state_notice)
    private TextView stateNotice;
    @ViewInject(R.id.state_detail)
    private TextView stateDetail;
    @ViewInject(R.id.main_action)
    private TextView mainAction;

    @ViewInject(R.id.pagerLayout)
    private LinearLayout pagerLayout;

    @ViewInject(R.id.viewPager)
    private ViewPager viewPager;

    private ClassAdapter adapter;
    private Handler handler;
    private Dialog loadingDialog;
    private String amount = "0";
    
    @ViewInject(R.id.cover)
    private FrameLayout cover;
    @ViewInject(R.id.rotate_progress)
    private ImageView progress;
    @ViewInject(R.id.no_net)
    protected RelativeLayout nonet;
    @ViewInject(R.id.refresh)
    protected Button refresh;
    
    @ViewInject(R.id.prize_layout)
    private LinearLayout prizeLayout;
    @ViewInject(R.id.prize_text)
    private TextView prizeText;
    private String prizeStr = "感谢您参与盛夏“搏”学礼活动，点击查看已中奖品>>";
    
    private OnClickListener clickListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_content_layout, null);
        ViewUtils.inject(this, view);
        loadingDialog = Utils.showProgressDialog(getActivity(), "请求中...", true);
        initViews();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    adapter = new ClassAdapter(getActivity());
                    viewPager.setAdapter(adapter);
                    if(UcreditDreamApplication.mUser!=null && UcreditDreamApplication.mUser.getUserState() !=null){
                        initViewByState(UcreditDreamApplication.mUser
                            .getUserState().getState());
                    }
                }else if(msg.what == 1){//显示抽奖状态条
                    prizeLayout.setVisibility(View.VISIBLE);
                }else if(msg.what == 2){
                    prizeLayout.setVisibility(View.GONE);
                }
            }

        };
        
        Utils.setTextViewClick(prizeStr, prizeText, prizeStr.indexOf("，")+1, prizeStr.length(), 
            getActivity(), new TextViewClickListener() {
            
            @Override
            public void onClick(View view) {
                goPrize();
            }
        },R.color.textview_inner_color, false);
        
        return view;
    }

    private void setMask(boolean b) {
    	if (getActivity()==null) {
			return;
		}
		if (b) {
			cover.setVisibility(View.VISIBLE);
			progress.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.drawable.rotate_progress));
			progress.setVisibility(View.VISIBLE);
		} else {
            cover.setVisibility(View.GONE);
            progress.clearAnimation();
            progress.setVisibility(View.GONE);
		}
	}
    
    private void initViews() {
    	HomePageActivity homePageActivity=(HomePageActivity) getActivity();
    	homePageActivity.setRefresh(refresh);
        //1.初始化ViewPager
        viewPager.setPageMargin(20);
        viewPager.setOffscreenPageLimit(3);

        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int position) {

            }
        });

        pagerLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                return viewPager.dispatchTouchEvent(event);
            }
        });
        ILoadingLayout iLoadingLayout=pullToRefreshScrollView.getLoadingLayoutProxy();
        iLoadingLayout.setPullLabel("下拉刷新");
        iLoadingLayout.setRefreshingLabel("刷新中...");
        iLoadingLayout.setReleaseLabel("松开刷新");
        pullToRefreshScrollView.setOnRefreshListener(new OnRefreshListener2() {

            @Override
            public void onPullDownToRefresh(final PullToRefreshBase refreshView) {
                new UserStateRequest(getActivity(), new QueryUserListener() {

                    @Override
                    public void onSuccess(String responseString) {
                    	setMask(false);
                    	if(UcreditDreamApplication.mUser == null){
                    	    return;
                    	}
                        UcreditDreamApplication.mUser.setUserState((UserState
                            .parseUserState(responseString)));
                        pullToRefreshScrollView.onRefreshComplete();
                        if (UcreditDreamApplication.mUser.getUserState() != null) {
                            initViewByState(UcreditDreamApplication.mUser
                                .getUserState().getState());
                            //如果存在进件，获取进件信息
                            if (UcreditDreamApplication.mUser.getUserState().isExist()) {
                                getApplyInfo();
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        pullToRefreshScrollView.onRefreshComplete();
                        setMask(false);
                    }

                    @Override
                    public void onStart() {
                    	checkNet(new OnClickListener() {
        					
        					@Override
        					public void onClick(View arg0) {
        						onPullDownToRefresh(refreshView);
        					}
        				  });
                        setMask(true);
                    }
                }).queryUser();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase refreshView) {
                // TODO Auto-generated method stub
                
            }
        });
        HomePageActivity activity = (HomePageActivity) getActivity();
        if(activity != null){
            SlidingMenu menu = activity.getSlidingMenu();
            menu.addIgnoredView(pagerLayout);
        }

    }

    
    @Override
    public void onStart() {
        super.onStart();
        getUserState();
    }

	private void getUserState() {
		new UserStateRequest(getActivity(), new QueryUserListener() {

            @Override
            public void onSuccess(String responseString) {
            	setMask(false);
            	if(UcreditDreamApplication.mUser == null){
            	    return;
            	}
                UcreditDreamApplication.mUser.setUserState((UserState
                    .parseUserState(responseString)));
//               UcreditDreamApplication.mUser.getUserState().setState(UserState.STATE_AFTER_SCAN);
                if (UcreditDreamApplication.mUser.getUserState() != null) {
                    initViewByState(UcreditDreamApplication.mUser
                        .getUserState().getState());
                    //如果存在进件，获取进件信息
                    if (UcreditDreamApplication.mUser.getUserState().isExist()) {
                        getApplyInfo();
                    }
                }
            }

            @Override
            public void onStart() {
            	checkNet(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						getUserState();
					}
				  });
                 setMask(true);
            }

            @Override
            public void onFailure() {
                 setMask(false);
            }
        }).queryUser();
	}

    private void getApplyInfo() { 
        new ApplyStateRequest(getActivity(), new QueryApplyListener() {

            @Override
            public void onSuccess(String responseString) {
            	setMask(false);
                UcreditDreamApplication.applyState = ApplyState
                    .parseApplyState(responseString);
                amount = Integer.parseInt(UcreditDreamApplication.applyState
                    .getActualAmount()) / 100 + "";
                handler.sendEmptyMessage(0);
                isLuckyDraw();
            }

            @Override
            public void onStart() {
            	checkNet(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						getApplyInfo();
					}
				  });
                setMask(true);
            }

            @Override
            public void onFailure() {
                setMask(false);
            }
        }).queryApply();
    }

    /**
     * 页面主要点击逻辑：
     * 1.扫描课程；上方不显示课程，按钮为扫描按钮，扫描课程成功后，上方显示课程
     * 2.提交资料；上方不显示课程，按钮为提交资料，点击按钮跳转提交资料页面
     * 3.征信授权；上方不显示课程，按钮为征信授权，点击提交征信报告
     * 4.在线签约；征信通过之后，状态为在线签约状态，点击跳转签约
     * 5.还款；上方显示课程，按钮变为还款
     * 根据状态，跳转到不同页面
     * @param state
     */
    private void processHandler(int state) {
        Intent intent;
        switch (state) {
            case UserState.STATE_BEFORE_SCAN:
                if (UcreditDreamApplication.mUser.getUserState().isInRefusalPeriod()){//拒贷期
                    Utils.MakeToast(getActivity(), UcreditDreamApplication.mUser.getUserState().getRefusalMsg());
                    break;
                }
                if (!UcreditDreamApplication.mUser.getUserState().isSpecial()) {
                    Utils.dialDialog(getActivity(), "您的设备已进行过注册，如您需要继续进行申请，请联系客服人员帮助您操作。", new DialogListenner() {
                        
                        @Override
                        public void confirm() {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            Uri data = Uri.parse("tel:" + Constants.CUSTOMER_SERVICE);
                            intent.setData(data);
                            startActivity(intent);
                        }
                    });
                    break;
                }
            case UserState.STATE_USER_INFO_REJECT:
            case UserState.STATE_CREDIT_REJECT:
            case UserState.STATE_SIGN_PROTOCOL_REJECT:
            case UserState.STATE_REPAY_OVER:
                if (UcreditDreamApplication.mUser.getUserState().isInRefusalPeriod()){//拒贷期
                    Utils.MakeToast(getActivity(), UcreditDreamApplication.mUser.getUserState().getRefusalMsg());
                    break;
                }
                intent = new Intent(getActivity(), QRCodeActivity.class);//扫描页面
                startActivityForResult(intent, UserState.STATE_BEFORE_SCAN);
                break;
            case UserState.STATE_AFTER_SCAN:
                intent = new Intent(getActivity(),
                    ApplyActivity.class);//提交资料
                startActivity(intent);
                break;
            case UserState.STATE_USER_INFO_CHECK:
                break;
            case UserState.STATE_USER_INFO_PASS:
                signNotice();
                break;
            case UserState.STATE_CREDIT_CHECK:
                //征信报告
                intent = new Intent(getActivity(),HeikaActivity.class);
                intent.putExtra("url", Constants.HEIKA_INDEX
                    +"?access_token="+UcreditDreamApplication.token
                    +"&clientid="+UcreditDreamApplication.clientId);
                startActivity(intent);
                break;
            case UserState.STATE_CREDIT_PASS:
                if(UcreditDreamApplication.applyState != null){
                    intent = new Intent(getActivity(),StudentAidDetailActivity.class);
                    intent.putExtra("lendId", UcreditDreamApplication.applyState.getId());
                    getActivity().startActivity(intent);
                }else{
                    Utils.MakeToast(getActivity(), "请选择有效的校区二维码");
                }
                break;
            case UserState.STATE_SIGN_PROTOCOL_PASS:
                //征信报告
                intent = new Intent(getActivity(),HeikaActivity.class);
                intent.putExtra("url", Constants.HEIKA_INDEX
                    +"?access_token="+UcreditDreamApplication.token
                    +"&clientid="+UcreditDreamApplication.clientId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        
        if(requestCode == UserState.STATE_BEFORE_SCAN){
            Intent intent = new Intent(getActivity(),
                ApplyActivity.class);//提交资料
            startActivity(intent); 
        }
    }

    private void signNotice() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(UcreditDreamApplication.applyState != null){
            params.put("lendId", UcreditDreamApplication.applyState.getId());
        }
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_SIGN_NOTICE, params,
            new TextHttpResponseHandler() {
                @Override
                public void onStart() {
                    super.onStart();
                    checkNet(new OnClickListener() {
    					
    					@Override
    					public void onClick(View arg0) {
    						signNotice();
    					}
    				  });
                    setMask(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        String responseString, Throwable throwable) {
                	setMask(false);
                    Logger.e("notifyUpload failure", statusCode
                        + " login error" + responseString);
                    loadingDialog.dismiss();
                    Utils.MakeToast(getActivity(), "系统繁忙，请检查网络稍后重试！");
                    new RequestFailureHandler(getActivity(),
                        new GetTokenListener() {
                            @Override
                            public void onSuccess() {
                                signNotice();
                            }
                        }).handleMessage(statusCode);
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String responseString) {
                	setMask(false);
                    Logger.e("notifyUpload success", statusCode + " "
                        + responseString);
                    loadingDialog.dismiss();
                    com.alibaba.fastjson.JSONObject response = JSON
                        .parseObject(responseString);
                    if (response.getBooleanValue("success")) {
                        if(getActivity() == null){
                            return;
                        }
                        Intent intent = new Intent(getActivity(),
                            SignDetailActivity.class);//在线签约
                        String foundPattern = response.getJSONObject("result").getString("foundPattern");
                        if("WE.COM".equalsIgnoreCase(foundPattern)){
                            intent.putExtra("we", true);
                        }
                        startActivity(intent);
                    } else {
                        Utils.MakeToast(getActivity(),
                            response.getJSONObject("error")
                                .getString("message"));
                    }
                }

            });
    }
    
    /**
     * 抽奖逻辑
     */
    private void isLuckyDraw() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("applyId", UcreditDreamApplication.applyState.getId());
        params.setUseJsonStreamer(true);
        httpClient.post(Constants.API_PRIZE_CHECK, params,
            new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                checkNet(new OnClickListener() {
                    
                    @Override
                    public void onClick(View arg0) {
                        isLuckyDraw();
                    }
                });
                setMask(true);
            }
            
            @Override
            public void onFailure(int statusCode, Header[] headers,
                    String responseString, Throwable throwable) {
                setMask(false);
                Logger.e("isLuckyDraw failure", statusCode
                    + " isLuckyDraw error" + responseString);
                loadingDialog.dismiss();
                Utils.MakeToast(getActivity(), "系统繁忙，请检查网络稍后重试！");
                new RequestFailureHandler(getActivity(),
                    new GetTokenListener() {
                    @Override
                    public void onSuccess() {
                        isLuckyDraw();
                    }
                }).handleMessage(statusCode);
            }
            
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                    String responseString) {
                setMask(false);
                Logger.e("isLuckyDraw success", statusCode + " "
                        + responseString);
                loadingDialog.dismiss();
                JSONObject response = JSON
                        .parseObject(responseString);
                JSONObject result = response.getJSONObject("result");
                if (result.getBooleanValue("canPlay")) {//是否可以抽奖
                    goPrize();
                } else if(result.getBooleanValue("isPlayed")){
                    //显示抽奖条
                    handler.sendEmptyMessage(1);
                }
                if(!result.getBooleanValue("campaignGoing")){
                    //不显示抽奖条
                    handler.sendEmptyMessage(2);
                }
            }

        });
    }
    
    private void goPrize() {
        Intent intent = new Intent(getActivity(),AdvertiseActivity.class);
        intent.putExtra("title", "盛夏搏学礼");
        intent.putExtra("url", Constants.API_PRIZE_DETAIL
            +"?applyid="+UcreditDreamApplication.applyState.getId()
            +"&access_token="+UcreditDreamApplication.token
            +"&clientid="+UcreditDreamApplication.clientId);
        startActivity(intent);
    }

    private void initViewByState(int state) {
        String stateStr;
        stateDetail.setText("");
        switch (state) {
            case UserState.STATE_BEFORE_SCAN:
                pagerLayout.setVisibility(View.GONE);
                centerBtn.setImageResource(R.drawable.scan_anim);
                AnimationDrawable animationDrawable=(AnimationDrawable) centerBtn.getDrawable();
                animationDrawable.start();
                stateNotice.setText("打开新的篇章");
                stateDetail.setText("可咨询培训机构，扫描二维码申请贷款");
                mainAction.setText("扫码添加课程");
                break;
            case UserState.STATE_AFTER_SCAN:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_apply_btn_bg);
                stateNotice.setText("提交申请资料");
                if(Utils.isNotEmptyString(UcreditDreamApplication.mUser.getUserState().getMessage())){
                    stateStr = UcreditDreamApplication.mUser.getUserState().getMessage()+"【客服】";
                }else{
                    stateStr = "未完成贷款授信，需要您提供必要的补充资料如需其他帮助可联系【客服】";
                }
                Utils.setTextViewClick(stateStr, stateDetail, 
                    stateStr.indexOf("【"), stateStr.lastIndexOf("】")+1, getActivity(),new TextViewClickListener() {
                    
                    @Override
                    public void onClick(View view) {
                        Utils.customDialog(getActivity(), ""+Constants.CUSTOMER_SERVICE, new DialogListenner() {
                            
                            @Override
                            public void confirm() {
                                Utils.dial(Constants.CUSTOMER_SERVICE, getActivity());
                            }
                        },"拨打客服热线");
                    }
                },R.color.white,true);
                mainAction.setText("继续申请");
                break;
            case UserState.STATE_USER_INFO_CHECK:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_credit_skip_btn_bg);
                stateNotice.setText("稍候片刻");
                stateDetail.setText("请您稍候，贷款正在审批中。");
                mainAction.setText("");
                break;
            case UserState.STATE_USER_INFO_PASS:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_sign_btn_bg);
                stateNotice.setText("授信通过");
                stateDetail.setText("批核额度：" + amount + "元，请尽快签约，额度有效期限30天。");
                mainAction.setText("在线签约");
                break;
            case UserState.STATE_CREDIT_CHECK:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_credit_btn_bg);
                stateNotice.setText("还款中");
                stateStr = "贷款额度：" + amount + "元，记得按时还款哦。可以点击查看您的【还款计划】";
                Utils.setTextViewClick(stateStr, stateDetail, 
                    stateStr.indexOf("【"), stateStr.lastIndexOf("】")+1, getActivity(),new TextViewClickListener() {
                    
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), RepayPlanActivity.class);//还款
                        startActivity(intent);
                    }
                },R.color.white,true);
                mainAction.setText("征信授权");
                break;
            case UserState.STATE_CREDIT_PASS:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_repay_btn_bg);
                stateNotice.setText("还款中");
                stateStr = "贷款额度：" + amount + "元，记得按时还款哦。可以点击查看您的【还款计划】";
                Utils.setTextViewClick(stateStr, stateDetail, 
                    stateStr.indexOf("【"), stateStr.lastIndexOf("】")+1, getActivity(),new TextViewClickListener() {
                    
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), RepayPlanActivity.class);//还款
                        startActivity(intent);
                    }
                },R.color.white,true);
                mainAction.setText("还款计划");
                break;
            case UserState.STATE_SIGN_PROTOCOL_PASS:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.main_credit_btn_bg);
                stateNotice.setText("还款中");
                stateStr = "贷款额度：" + amount + "元，记得按时还款哦。可以点击查看您的【还款计划】";
                Utils.setTextViewClick(stateStr, stateDetail, 
                    stateStr.indexOf("【"), stateStr.lastIndexOf("】")+1, getActivity(),new TextViewClickListener() {
                    
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), RepayPlanActivity.class);//还款
                        startActivity(intent);
                    }
                },R.color.white,true);
                mainAction.setText("征信授权");
                break;
            case UserState.STATE_USER_INFO_REJECT:
            case UserState.STATE_CREDIT_REJECT:
            case UserState.STATE_SIGN_PROTOCOL_REJECT:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.scan_anim);
                AnimationDrawable animation=(AnimationDrawable) centerBtn.getDrawable();
                animation.start();
                stateNotice.setText("非常遗憾");
                stateDetail.setText("由于一些原因，您的贷款申请没有通过，您可以重新扫描申请");
                mainAction.setText("扫码添加课程");
                break;
            case UserState.STATE_REPAY_OVER:
                pagerLayout.setVisibility(View.VISIBLE);
                centerBtn.setImageResource(R.drawable.scan_anim);
                AnimationDrawable drawable=(AnimationDrawable) centerBtn.getDrawable();
                drawable.start();
                stateNotice.setText("贷款已结清");
                stateStr = "贷款额度：" + amount + "元，记得按时还款哦。可以点击查看您的【还款计划】";
                Utils.setTextViewClick(stateStr, stateDetail, 
                    stateStr.indexOf("【"), stateStr.lastIndexOf("】")+1, getActivity(),new TextViewClickListener() {
                    
                    @Override
                    public void onClick(View view) {
                      Intent intent = new Intent(getActivity(), RepayPlanActivity.class);//还款
                      startActivity(intent);
                    }
                },R.color.white,true);
                mainAction.setText("扫码新课程");
            default:
                break;
        }
    }

    private boolean isNetworkAvailable() {
		return Utils.isNetwokAvailable(getActivity());
	}
    
    private void checkNet(OnClickListener listener) {
    	HomePageActivity activity = (HomePageActivity) getActivity();
    	if (activity==null) {
			return;
		}
		if (!isNetworkAvailable()) {
			this.clickListener=listener;
			activity.setClickListener(listener);
			nonet.setVisibility(View.VISIBLE);
			refresh.setOnClickListener(listener);
		} else {
			this.clickListener=null;
			activity.setClickListener(null);
			nonet.setVisibility(View.GONE);
		}
	}
    
    @OnClick({ R.id.left_menu, R.id.center_btn, R.id.right_menu, R.id.main_action,R.id.no_net,R.id.prize_layout,R.id.prize_text})
    private void onClick(View view) {
        HomePageActivity activity = (HomePageActivity) getActivity();
        if (activity == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.left_menu://左侧按钮
                if (!activity.getSlidingMenu().isMenuShowing()) {
                    activity.getSlidingMenu().showMenu();
                }
                break;
            case R.id.right_menu://左侧按钮
                activity.startActivity(new Intent(activity,
                    StudentAidRecordActivity.class));
                break;
            case R.id.main_action:
            case R.id.center_btn://扫描按钮
                if(UcreditDreamApplication.mUser != null &&
                        UcreditDreamApplication.mUser.getUserState()!=null){
                    processHandler(UcreditDreamApplication.mUser.getUserState()
                        .getState());
                }
                break;
            case R.id.no_net:
            	break;
            case R.id.prize_layout:
            case R.id.prize_text:
                goPrize();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class ClassAdapter extends PagerAdapter {

        private LayoutInflater mInflater;

        public ClassAdapter(Context context) {
            if (context != null) {
                mInflater = LayoutInflater.from(context);
            }
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mInflater.inflate(R.layout.main_viewpager_item,
                container, false);
            container.addView(view);

            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            TextView courseName = (TextView) view.findViewById(R.id.courseName);
            TextView applyAmount = (TextView) view
                .findViewById(R.id.applyAmount);
            TextView courseTerm = (TextView) view.findViewById(R.id.term);
            TextView campusName = (TextView) view.findViewById(R.id.campusName);

            if (UcreditDreamApplication.applyState != null) {
                if (Utils.isNotEmptyString(UcreditDreamApplication.applyState
                    .getLogUrl())) {
                    ImageLoader.getInstance().displayImage(
                        Constants.API_IMAGE_PREFIX
                            + UcreditDreamApplication.applyState.getLogUrl()+"?access_token="
                                    + UcreditDreamApplication.token
                                    + "&clientid="
                                    + UcreditDreamApplication.clientId,
                        icon);
                }
                courseName.setText(UcreditDreamApplication.applyState
                    .getCourseName());
                applyAmount.setText(Integer
                    .parseInt(UcreditDreamApplication.applyState
                        .getActualAmount())
                    / 100 + "元");
                courseTerm.setText("期限："
                    + UcreditDreamApplication.applyState.getDesc());
                campusName.setText(UcreditDreamApplication.applyState
                    .getOrgName());
            }

            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if(UcreditDreamApplication.applyState != null){
                        Intent intent = new Intent(getActivity(),StudentAidDetailActivity.class);
                        intent.putExtra("lendId", UcreditDreamApplication.applyState.getId());
                        getActivity().startActivity(intent);
                    }else{
                        Utils.MakeToast(getActivity(), "课程信息有误，请联系相关人员。");
                    }
                }
            });
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

}
