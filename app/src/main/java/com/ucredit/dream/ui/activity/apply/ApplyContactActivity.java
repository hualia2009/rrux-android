package com.ucredit.dream.ui.activity.apply;

import org.apache.http.Header;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.City;
import com.ucredit.dream.bean.Province;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.CityWheelAdapter;
import kankan.wheel.widget.adapters.ProvinceWheelAdapter;

public class ApplyContactActivity extends BaseActivity {
	@ViewInject(R.id.cancel)
	private Button cancel;

	@ViewInject(R.id.roott)
	private RelativeLayout root;
	@ViewInject(R.id.pop)
	private LinearLayout pop;
	@ViewInject(R.id.wheel_layout)
	private LinearLayout wheelLayout;
	@ViewInject(R.id.province)
	private WheelView provinceWheel;
	@ViewInject(R.id.city)
	private WheelView cityWheel;
	@ViewInject(R.id.v1)
	private View view1;
	@ViewInject(R.id.v2)
	private View view2;
	@ViewInject(R.id.v3)
	private View view3;

	@ViewInject(R.id.choose)
	private TextView choose;
	@ViewInject(R.id.address_pre)
	private TextView address;
	@ViewInject(R.id.address_street)
	private EditText address_street; 
	@ViewInject(R.id.email)
	private EditText email; 

	@ViewInject(R.id.position_layout)
	private LinearLayout positionLayout;
	@ViewInject(R.id.limit_bottom_button)
	private Button commit;

	float foot;
	int wheellayoutHeight;
	int state = 0;

	// 城市列表
	ProvinceWheelAdapter provinceAdapter;
	CityWheelAdapter cityAdapter;

	private Province provinceSelected = null;
	private City citySelected = null;

	private boolean scrolling = false;

	// 判断数据是否有变化，无变化则不保存，直接下一步
	String _resideAddress = "";
	String _provinceCity = "";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initAddressView();
		//根据定位设置省市
		if(Utils.isNotEmptyString(UcreditDreamApplication.province)){
		    provinceWheel.setCurrentItem(provinceAdapter
		        .whereProvince(UcreditDreamApplication.province));
		}else{
		    provinceWheel.setCurrentItem(provinceAdapter
                .whereProvince("北京市"));
		}
		if(Utils.isNotEmptyString(UcreditDreamApplication.city)){
		    cityWheel.setCurrentItem(cityAdapter
		        .whereCity(UcreditDreamApplication.city));
		}else{
		    cityWheel.setCurrentItem(cityAdapter
                .whereCity("北京市"));
		}
	    
		initData();
		
		TextWatcher changedListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                String addressStr = address.getText().toString();
                String addressStreetStr = address_street.getText().toString();
                String emailStr = email.getText().toString();
                if (Utils.isNotEmptyString(addressStr)
                        &&Utils.isNotEmptyString(addressStreetStr)&&Utils.isNotEmptyString(emailStr)
                        ) {
                    commit.setEnabled(true);
                }else {
                    commit.setEnabled(false);
                }
            }
        });
		address.addTextChangedListener(changedListener);
		address_street.addTextChangedListener(changedListener);
		email.addTextChangedListener(changedListener);
	}
	
	@OnClick({ R.id.limit_bottom_button, R.id.address_pre, R.id.choose_layout,
			R.id.roott })
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.limit_bottom_button:
			submitNext();
			break;
		case R.id.address_pre:
			popAddress();
			break;
		case R.id.choose_layout:
			popAddress();
			break;
		case R.id.roott:
			if (state == 1) {
				popAddress();
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
	    if (state == 1) {
	        popAddress();
            return;
        }
	    super.onBackPressed();
	}

	@Override
	protected boolean hasTitle() {
		return true;
	}

	@Override
	protected CharSequence getPublicTitle() {
		return "居住地址";
	}

	@Override
	protected int getContentId() {
		return R.layout.activity_apply_contact;
	}

	/**
	 * 接收通知状态变更
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != resultCode) {
			return;
		}
		switch (requestCode) {
		case ApplyActivity.APPLY_CONTACT_CODE:
			setResult(ApplyActivity.APPLY_CONTACT_CODE);
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		AsyncHttpClient httpClient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.setUseJsonStreamer(true);
		httpClient.post(this, Constants.APPLICATION_ADDRESS_QUERY, params,
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
					    setMask(false);
						JSONObject response = JSON.parseObject(responseString);
						if (response.getJSONObject("result")!=null && response.getBoolean("success")) {
						    Logger.e("student_aid_list  failure", statusCode + " "
	                                + responseString);
							response = response.getJSONObject("result");
							address_street.setText(response
									.getString("resideAddress"));
							_resideAddress = response
									.getString("resideAddress");
							provinceWheel.setCurrentItem(provinceAdapter
									.whereProvince(response
											.getIntValue("resideProvice")),
									true);
							cityWheel.setCurrentItem(cityAdapter
									.whereCity(response
											.getIntValue("resideCity")), true);
							_provinceCity = response
									.getIntValue("resideProvice")
									+ "_"
									+ response.getIntValue("resideCity");
							email.setText(response.getString("email"));
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
					    setMask(false);
						Logger.e("student_aid_list  failure", statusCode + " "
								+ responseString);
						new RequestFailureHandler(ApplyContactActivity.this,
								new GetTokenListener() {

									@Override
									public void onSuccess() {
										initData();
									}
								}).handleMessage(statusCode);
					}

					@Override
					public void onStart() {
						super.onStart();
		            	checkNet(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								initData();
								
							}
		            		
		            	});						
						setMask(true);
					}
				});
	}

	private void submitNext() {
		String temp = address_street.getText().toString();
		if (provinceSelected == null && citySelected == null) {
			Utils.MakeToast(ApplyContactActivity.this, "请选择地区");
			return;
		}
		if (temp != null && temp.length() < 1) {
			Utils.MakeToast(ApplyContactActivity.this, "请输入详细地址");
			return;
		}
		if (Utils.stringFilter(temp)) {
            Utils.MakeToast(this, "详细地址不能含有特殊字符");
            return;
        }
		if(!Utils.isNotEmptyString(email.getText().toString())){
		    Utils.MakeToast(this, "请您输入邮箱地址");
		    return;
		}
		if(!Utils.checkEmail(email.getText().toString())){
		    Utils.MakeToast(this, "邮箱输入有误，请检查");
		    return;
		}
		String tempCode = provinceSelected.getCode() + "_"
				+ citySelected.getCode();
		if (!tempCode.equalsIgnoreCase(_provinceCity)
				|| !temp.equalsIgnoreCase(_resideAddress)) {// 有数据变化
			AsyncHttpClient httpClient = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("email", email.getText().toString().trim());
			params.put("resideAddress", temp);
			params.put("resideCity", citySelected.getCode());
			params.put("resideProvice", provinceSelected.getCode());
			params.setUseJsonStreamer(true);
			httpClient.post(this, Constants.APPLICATION_ADDRESS_SUBMIT, params,
					new TextHttpResponseHandler() {

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								String responseString) {
							Logger.e("student_aid_list  success", statusCode
									+ " " + responseString);
							setMask(false);
							JSONObject response = JSON
									.parseObject(responseString);
							if (response.getBoolean("success")) {
								toActivityForResult(ApplyContactInfoActivity.class,ApplyActivity.APPLY_CONTACT_CODE);
							} else {
								Utils.MakeToast(ApplyContactActivity.this,
										response.getJSONObject("error")
												.getString("message"));
							}
						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								String responseString, Throwable throwable) {
							Logger.e("student_aid_list  failure", statusCode
									+ " " + responseString);
							setMask(false);
							new RequestFailureHandler(
									ApplyContactActivity.this,
									new GetTokenListener() {

										@Override
										public void onSuccess() {
											submitNext();
										}
									}).handleMessage(statusCode);
						}

						@Override
						public void onStart() {
							super.onStart();
			            	checkNet(new OnClickListener(){

								@Override
								public void onClick(View arg0) {
									submitNext();
									
								}
			            		
			            	});							
							setMask(true);
						}
					});
		} else {// 直接下一步
			toActivityForResult(ApplyContactInfoActivity.class,ApplyActivity.APPLY_CONTACT_CODE);
		}
	}

	private void initAddressView() {
		choose.setText("选择地址");
		pop.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						pop.getViewTreeObserver().removeGlobalOnLayoutListener(
								this);
						wheellayoutHeight = wheelLayout.getMeasuredHeight();
						ViewHelper.setY(pop, root.getHeight());
						view1.getLayoutParams().height = (int) (provinceWheel
								.getItemHeight() * 1.2);
						view2.getLayoutParams().height = (int) (provinceWheel
								.getItemHeight() * 1.2);
						view3.getLayoutParams().height = (int) (provinceWheel
								.getItemHeight() * 1.2);
						view1.requestLayout();
						view2.requestLayout();
						view3.requestLayout();

					}
				});

		provinceWheel.setVisibleItems(5);
		provinceAdapter = new ProvinceWheelAdapter(this,
				UcreditDreamApplication.provinceList);
		provinceAdapter.setTextSize(16);
		provinceWheel.setViewAdapter(provinceAdapter);
		cityWheel.setVisibleItems(5);

		provinceWheel.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					updateCities(newValue);
				}
			}
		});

		provinceWheel.addScrollingListener(new OnWheelScrollListener() {
			@Override
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
				cityWheel.setEnabled(false);
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				updateCities(provinceWheel.getCurrentItem());
				cityWheel.setEnabled(true);
				updateAddress(cityWheel.getCurrentItem());
			}
		});

		updateCities(0);

		cityWheel.addChangingListener(new OnWheelChangedListener() {

			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					updateAddress(newValue);
				}
			}
		});

		cityWheel.addScrollingListener(new OnWheelScrollListener() {

			@Override
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
				provinceWheel.setEnabled(false);
			}

			@Override
			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				updateAddress(cityWheel.getCurrentItem());
				provinceWheel.setEnabled(true);
			}
		});
	}

	protected void updateAddress(int cityIndex) {
		provinceSelected = provinceAdapter.getCurrentProvince(provinceWheel
				.getCurrentItem());
		citySelected = cityAdapter.getCurrentCity(cityIndex);
		address.setText(provinceSelected.getName() + " "
				+ citySelected.getName());
	}

	private void updateCities(int index) {
		provinceSelected = provinceAdapter.getCurrentProvince(index);
		cityAdapter = new CityWheelAdapter(this, provinceSelected.getCitys());
		cityAdapter.setTextSize(16);
		cityWheel.setViewAdapter(cityAdapter);
		cityWheel.setCurrentItem(cityAdapter.getItemsCount() / 2);
		updateAddress(cityAdapter.getItemsCount() / 2);
	}

	private void popAddress() {
		if (state == 0) {//
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(address.getWindowToken(), 0);
			commit.setEnabled(false);
			ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 100.0f);
			animator.setDuration(200);
			animator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					state = 1;
					commit.startAnimation(AnimationUtils.loadAnimation(
							ApplyContactActivity.this, R.anim.push_bottom_out));
					commit.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {

				}
			});
			animator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					DisplayMetrics metric = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metric);
					ViewHelper.setTranslationY(pop, metric.heightPixels / 2);
				}
			});
			animator.start();
		} else if (state == 1) {
			commit.setEnabled(true);
			ValueAnimator animator = ValueAnimator.ofFloat(100.0f, 0.0f);
			animator.setDuration(200);
			animator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {

				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					state = 0;
					commit.startAnimation(AnimationUtils.loadAnimation(
							ApplyContactActivity.this, R.anim.push_bottom_in));
					commit.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {

				}
			});
			animator.addUpdateListener(new AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					ViewHelper.setTranslationY(pop, root.getHeight());
				}
			});
			animator.start();
		}
	}

}
