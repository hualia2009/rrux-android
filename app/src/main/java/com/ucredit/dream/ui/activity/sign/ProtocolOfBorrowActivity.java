package com.ucredit.dream.ui.activity.sign;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.h5.MyWebView;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

public class ProtocolOfBorrowActivity extends BaseActivity {

    @ViewInject(R.id.advertise_detail_content)
    private MyWebView webView;

    
    private String url;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);        
        url = Constants.H5_PROTOCOL+"loan_statement";
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebChromeClient(new WebChromeClient());
        webSettings.setDefaultFontSize(25);
        webView.loadUrl(url);        

    }
    
  
   
    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "借款声明";
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_borrow_protocol;
    }

}
