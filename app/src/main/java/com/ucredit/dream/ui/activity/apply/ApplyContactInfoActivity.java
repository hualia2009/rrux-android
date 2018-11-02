package com.ucredit.dream.ui.activity.apply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.ucredit.dream.R;
import com.ucredit.dream.bean.Contact;
import com.ucredit.dream.contact.ContactActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.TextWatcherCallBack;
import com.ucredit.dream.utils.TextWatcherListener;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.Utils.DialogListenner;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

@SuppressLint("HandlerLeak")
public class ApplyContactInfoActivity extends BaseActivity {

	private static final int PICK_CONTACT_FATHER = 1;
	private static final int PICK_CONTACT_MOTHER = 2;
	private static final int PICK_CONTACT_MATE = 3;
	private static final int PICK_CONTACT_OTHER = 4;

	@ViewInject(R.id.fatherPhone)
	private TextView fatherPhone;
	@ViewInject(R.id.motherPhone)
	private TextView motherPhone;
	@ViewInject(R.id.matePhone)
	private TextView matePhone;
	@ViewInject(R.id.otherPhone)
	private TextView otherPhone;
	@ViewInject(R.id.other)
	private TextView other;

	@ViewInject(R.id.fatherName)
	private EditText fatherName;
	@ViewInject(R.id.motherName)
	private EditText motherName;
	@ViewInject(R.id.mateName)
	private EditText mateName;
	@ViewInject(R.id.otherName)
	private EditText otherName;

	@ViewInject(R.id.father_layout)
	private LinearLayout fatherLayout;
	@ViewInject(R.id.mother_layout)
	private LinearLayout motherLayout;
	@ViewInject(R.id.spouse_layout)
	private LinearLayout spouseLayout;
	
	@ViewInject(R.id.father_input_layout)
	private LinearLayout fatherInputLayout;
	@ViewInject(R.id.mother_input_layout)
	private LinearLayout motherInputLayout;
	@ViewInject(R.id.spouse_input_layout)
	private LinearLayout spouseInputLayout;
	@ViewInject(R.id.other_input_layout)
	private LinearLayout otherInputLayout;
	
	@ViewInject(R.id.line1)
	private LinearLayout line1;
	@ViewInject(R.id.line2)
	private LinearLayout line2;

	@ViewInject(R.id.fathercheck)
	private ImageView fatherImage;
	@ViewInject(R.id.mothercheck)
	private ImageView motherImage;
	@ViewInject(R.id.spousecheck)
	private ImageView spouseImage;
	
	private boolean fatherChecked=true;
	private boolean motherChecked=true;
	private boolean spouseChecked=true;
	
	private int checked=3;
	
	
	@ViewInject(R.id.submit)
	private Button submit;

	Map<String, String> IDS = new HashMap<String, String>();
	private String relationNames[] = new String[] { "其他", "单位", "子女",
			"兄弟姐妹", "其他亲属", "朋友", "同事" };
	private int relationCode[] = new int[] { 0, 2, 4, 6, 7, 8, 9 };
    
	public static boolean upload = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		Utils.customDialog(this, "为保障您可以顺利申请借款，请输入真实有效的联系信息", new DialogListenner() {
            
            @Override
            public void confirm() {
                
            }
        }); 

		queryContact();
		
		TextWatcher changedListener = new TextWatcherListener(new TextWatcherCallBack() {
            
            @Override
            public void check() {
                final List<Contact> list = parseContact();
                if (list == null) {
                    submit.setEnabled(false);
                } else{
                    submit.setEnabled(true);
                }
            }
        });
		fatherPhone.addTextChangedListener(changedListener);
		motherPhone.addTextChangedListener(changedListener);
		matePhone.addTextChangedListener(changedListener);
		otherPhone.addTextChangedListener(changedListener);
	}

	protected void check() {
        // TODO Auto-generated method stub
        
    }

    private void queryContact() {
		AsyncHttpClient httpClient = new AsyncHttpClient();
		httpClient.post(this, Constants.API_QUERY_CONTACTS, null,
				new TextHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						setMask(false);
						Logger.e("queryContact", statusCode + " "
								+ responseString);
						JSONObject response = JSON.parseObject(responseString);
						if (response.getBoolean("success")) {
							com.alibaba.fastjson.JSONArray array = response
									.getJSONArray("result");
							for (int i = 0; i < array.size(); i++) {
								JSONObject obj = array.getJSONObject(i);
								String relative = obj.getString("relative");
								if ("FATHER".equalsIgnoreCase(relative)) {
									fatherName.setText(obj.getString("name"));
									fatherPhone.setText(obj
											.getString("cellphone"));
									IDS.put("FATHER", obj.getString("id"));
								}
								if ("MOTHER".equalsIgnoreCase(relative)) {
									motherName.setText(obj.getString("name"));
									motherPhone.setText(obj
											.getString("cellphone"));
									IDS.put("MOTHER", obj.getString("id"));
								}
								if ("SPOUSE".equalsIgnoreCase(relative)) {
									mateName.setText(obj.getString("name"));
									matePhone.setText(obj
											.getString("cellphone"));
									IDS.put("SPOUSE", obj.getString("id"));
								}
								if ("OTHER".equalsIgnoreCase(relative)) {
									otherInputLayout.setVisibility(View.VISIBLE);
									otherName.setText(obj.getString("name"));
									otherPhone.setText(obj
											.getString("cellphone"));
									IDS.put("OTHER", obj.getString("id"));
									int which = 0;
									try {
										which = Integer.valueOf(obj
												.getString("relation"));
									} catch (Exception e) {
										which = 0;
									}
									IDS.put("OTHER_TYPE", "" + which);
									for (int j = 0; j < relationCode.length; j++) {
										if (relationCode[j] == which) {
											other.setText(relationNames[j]);
										}
									}
								}else {
									otherInputLayout.setVisibility(View.GONE);
								}
							}
						}
					}

					@Override
					public void onStart() {
						super.onStart();
		            	checkNet(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								queryContact();
								
							}
		            		
		            	});		
		            	setMask(true);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						setMask(false);
						Logger.e("queryContact", statusCode + " "
								+ responseString);
						new RequestFailureHandler(
								ApplyContactInfoActivity.this,
								new GetTokenListener() {
									@Override
									public void onSuccess() {
										queryContact();
									}
								}).handleMessage(statusCode);
					}
				});
	}

	@Override
	protected boolean hasTitle() {
		return true;
	}

	@Override
	protected CharSequence getPublicTitle() {
		return "联系信息";
	}

	@Override
	protected int getContentId() {
		return R.layout.activity_apply_contact_info;
	}

	@OnClick({ R.id.submit, R.id.fatherChoose, R.id.motherChoose, R.id.mateChoose,
			R.id.otherChoose, R.id.other,R.id.father_layout,R.id.mother_layout,R.id.spouse_layout })
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.submit:
			final List<Contact> list = parseContact();
			if (list == null) {
				Utils.MakeToast(ApplyContactInfoActivity.this, "请至少填写两位联系人");
				return;
			} else if (otherInputLayout.getVisibility()==View.VISIBLE&&"请选择".equals(other.getText().toString())) {
				Utils.MakeToast(ApplyContactInfoActivity.this, "请选择与联系人关系");
				return;
			}else {
				Utils.customDialog(ApplyContactInfoActivity.this,
						"已上传资料将无法再进行更改，请确认是否提交？", new DialogListenner() {
							@Override
							public void confirm() {
								commit(list);
							}
						});
			}
			break;
		case R.id.fatherChoose:
			openContract(PICK_CONTACT_FATHER);
			break;
		case R.id.motherChoose:
			openContract(PICK_CONTACT_MOTHER);
			break;
		case R.id.mateChoose:
			openContract(PICK_CONTACT_MATE);
			break;
		case R.id.otherChoose:
			openContract(PICK_CONTACT_OTHER);
			break;
		case R.id.other:
			showSelectDialog();
			break;
		case R.id.father_layout:
		    if(!fatherChecked){
		        fatherLayout.setBackgroundColor(0xff7ED321);
		        fatherImage.setVisibility(View.VISIBLE);
		        fatherInputLayout.setVisibility(View.VISIBLE);
		        otherInputLayout.setVisibility(View.INVISIBLE);
		        checked++;
		        fatherChecked=true;
		    }else {
		    	if (checked<=1) {
					Utils.MakeToast(this, "请至少填写两位联系人");
					break;
				}
		    	if (checked<3) {
					otherInputLayout.setVisibility(View.VISIBLE);
				}
		    	fatherLayout.setBackgroundColor(0xffAFB6BE);
		        fatherImage.setVisibility(View.GONE);
		        fatherInputLayout.setVisibility(View.GONE);
		        
		        checked--;
		        fatherChecked=false;
			}
		    break;
		case R.id.mother_layout:
			if(!motherChecked){
		        motherLayout.setBackgroundColor(0xff7ED321);
		        motherImage.setVisibility(View.VISIBLE);
		        motherInputLayout.setVisibility(View.VISIBLE);
		        line1.setVisibility(View.VISIBLE);
		        otherInputLayout.setVisibility(View.INVISIBLE);
		        checked++;
		        motherChecked=true;
		    }else {
		    	if (checked<=1) {
					Utils.MakeToast(this, "请至少填写两位联系人");
					break;
				}
		    	if (checked<3) {
					otherInputLayout.setVisibility(View.VISIBLE);
				}
		    	motherLayout.setBackgroundColor(0xffAFB6BE);
		        motherImage.setVisibility(View.GONE);
		        motherInputLayout.setVisibility(View.GONE);
		        line1.setVisibility(View.GONE);
		        
		        checked--;
		        motherChecked=false;
			}
			break;
		case R.id.spouse_layout:
			if(!spouseChecked){
		        spouseLayout.setBackgroundColor(0xff7ED321);
		        spouseImage.setVisibility(View.VISIBLE);
		        spouseInputLayout.setVisibility(View.VISIBLE);
		        line2.setVisibility(View.VISIBLE);
		        otherInputLayout.setVisibility(View.INVISIBLE);
		        checked++;
		        spouseChecked=true;
		    }else {
		    	if (checked<=1) {
					Utils.MakeToast(this, "请至少填写两位联系人");
					break;
				}
		    	if (checked<3) {
		    		otherInputLayout.setVisibility(View.VISIBLE);
				}
		    	spouseLayout.setBackgroundColor(0xffAFB6BE);
		        spouseImage.setVisibility(View.GONE);
		        spouseInputLayout.setVisibility(View.GONE);
		        line2.setVisibility(View.GONE);
		        checked--;
		        spouseChecked=false;
			}
		    break;
		default:
			break;
		}
	}


    public void showSelectDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("选择关系");
		// 选择下标
		int which = 0;
		try {
			which = Integer.valueOf(IDS.get("OTHER_TYPE"));
			for (int j = 0; j < relationCode.length; j++) {
				if (relationCode[j] == which) {
					which = j;
					break;
				}
			}
		} catch (Exception e) {
			which = 0;
		}
		builder.setSingleChoiceItems(relationNames, which,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						other.setText(relationNames[which]);
						IDS.put("OTHER_TYPE", "" + relationCode[which]);
						dialog.cancel();
					}
				});
		builder.create().show();
	}

	/**
	 * 解析数据
	 * 
	 * @return
	 */
	private List<Contact> parseContact() {
		List<Contact> list = new ArrayList<Contact>();
		if (fatherImage.getVisibility()!=View.GONE
				&& fatherPhone.getText().toString().length() > 0
				&& fatherName.getText().toString().length() > 0) {
			Contact fatherContact = new Contact();
			fatherContact.setName(fatherName.getText().toString());
			fatherContact.setCellphone(fatherPhone.getText().toString());
			fatherContact.setRelative("FATHER");
			fatherContact.setRelation(5);
			fatherContact.setId(IDS.get("FATHER"));
			list.add(fatherContact);
		}
		if (spouseImage.getVisibility()!=View.GONE
				&& matePhone.getText().toString().length() > 0
				&& mateName.getText().toString().length() > 0) {
			Contact mateContact = new Contact();
			mateContact.setName(mateName.getText().toString());
			mateContact.setCellphone(matePhone.getText().toString());
			mateContact.setRelative("SPOUSE");
			mateContact.setRelation(3);
			mateContact.setId(IDS.get("SPOUSE"));
			list.add(mateContact);
		}
		if (motherImage.getVisibility()!=View.GONE
				&& motherPhone.getText().toString().length() > 0
				&& motherName.getText().toString().length() > 0) {
			Contact motherContact = new Contact();
			motherContact.setName(motherName.getText().toString());
			motherContact.setCellphone(motherPhone.getText().toString());
			motherContact.setRelative("MOTHER");
			motherContact.setRelation(5);
			motherContact.setId(IDS.get("MOTHER"));
			list.add(motherContact);
		}
		if (otherInputLayout.getVisibility()!=View.INVISIBLE
				&& otherPhone.getText().toString().length() > 0
				&& otherName.getText().toString().length() > 0) {
			Contact otherContact = new Contact();
			otherContact.setName(otherName.getText().toString());
			otherContact.setCellphone(otherPhone.getText().toString());
			otherContact.setRelative("OTHER");
			int which = 0;
			try {
				which = Integer.valueOf(IDS.get("OTHER_TYPE"));
			} catch (Exception e) {
				which = 0;
			}
			otherContact.setRelation(which);
			otherContact.setId(IDS.get("OTHER"));
			list.add(otherContact);
		}
		if (list.size() < 2)
            return null;// 必须上传两个联系人
		return list;
	}

	/**
	 * 上传手机所有联系人信息
	 */
	private void commit(final List<Contact> list) {
		AsyncHttpClient httpClient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		String str = JSON.toJSONString(list);
		try {
			params.put("contacts", new JSONArray(str));
		} catch (JSONException e) {
			Utils.MakeToast(ApplyContactInfoActivity.this, "数据错误请重进入页面重试");
		}
		params.setUseJsonStreamer(true);
		httpClient.post(this, Constants.API_SUBMIT_CONTACTS, params,
				new TextHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						Logger.e("commit", statusCode + " " + responseString);
						JSONObject response = JSON.parseObject(responseString);
						if (response.getBoolean("success")) {
							Utils.MakeToast(ApplyContactInfoActivity.this,
									"联系信息保存成功");
							setResult(ApplyActivity.APPLY_CONTACT_CODE);
							finish();
						} else {
							Utils.MakeToast(
									ApplyContactInfoActivity.this,
									response.getJSONObject("error").getString(
											"message"));
						}
						setMask(false);
					}

					@Override
					public void onStart() {
						super.onStart();
		            	checkNet(new OnClickListener(){

							@Override
							public void onClick(View arg0) {
								commit(list);
								
							}
		            		
		            	});						
						setMask(true);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						Logger.e("commit", statusCode + " " + responseString);
						setMask(false);
						new RequestFailureHandler(
								ApplyContactInfoActivity.this,
								new GetTokenListener() {
									@Override
									public void onSuccess() {
										commit(list);
									}
								}).handleMessage(statusCode);
					}
				});
	}

	/**
	 * 打开系统联系人
	 */
	private void openContract(int requestCode) {
	    Intent intent = new Intent(ApplyContactInfoActivity.this,ContactActivity.class);
	    startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转到联系人回来之后处理逻辑
	 */
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case PICK_CONTACT_FATHER:
			if (resultCode == Activity.RESULT_OK) {
				Contact contact = this.getContactPhone(data);
				fatherName.setText(contact.getName());
				fatherPhone.setText(contact.getCellphone());
			}
			break;
		case PICK_CONTACT_MOTHER:
			if (resultCode == Activity.RESULT_OK) {
				Contact contact = this.getContactPhone(data);
				motherName.setText(contact.getName());
				motherPhone.setText(contact.getCellphone());
			}
			break;
		case PICK_CONTACT_MATE:
			if (resultCode == Activity.RESULT_OK) {
				Contact contact = this.getContactPhone(data);
				mateName.setText(contact.getName());
				matePhone.setText(contact.getCellphone());
			}
			break;
		case PICK_CONTACT_OTHER:
			if (resultCode == Activity.RESULT_OK) {
				Contact contact = this.getContactPhone(data);
				otherName.setText(contact.getName());
				otherPhone.setText(contact.getCellphone());
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 从返回的数据获取联系人信息（姓名，电话号码）封装成Contact对象
	 * 
	 * @param cursor
	 * @return
	 */
	private Contact getContactPhone(Intent data) {
	    Contact contact = new Contact();
	    contact.setName(data.getStringExtra("name"));
	    contact.setCellphone(data.getStringExtra("phone"));
		return contact;
	}

	public String getContacts() {
		String result = "";
		ContentResolver cr = getContentResolver();
		// 取得电话本中开始一项的光标
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		// 向下移动光标
		while (cursor!=null && cursor.moveToNext()) {
			// 取得联系人名字
			int nameFieldColumnIndex = cursor
					.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			String contact = cursor.getString(nameFieldColumnIndex);
			// 取得电话号码
			String ContactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			Cursor phone = cr.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
							+ ContactId, null, null);

			while (phone.moveToNext()) {
				String PhoneNumber = phone
						.getString(phone
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				result += (contact + "###" + PhoneNumber + "\n");
			}
			phone.close();
		}
		Logger.e("getContacts", "" + cursor.getColumnCount());
		cursor.close();
		return result;
	}


}
