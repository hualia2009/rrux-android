package com.ucredit.dream.ui.activity.h5;

import java.lang.annotation.Annotation;

import org.apache.http.util.EncodingUtils;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.h5.MyWebView.ScrollInterface;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("NewApi")
public class AdvertiseActivity extends BaseActivity {

    @ViewInject(R.id.advertise_detail_content)
    private MyWebView webView;
    private String url;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        url = getIntent().getStringExtra("url");
        Logger.e("url", url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if(getIntent().getBooleanExtra("zoom", false)){
            // 设置可以支持缩放 
            webSettings.setSupportZoom(true); 
            // 设置出现缩放工具 
            webSettings.setBuiltInZoomControls(true);
            //扩大比例的缩放
            webSettings.setUseWideViewPort(true);
        }
        if(getIntent().getBooleanExtra("pdf", false)){
            webView.setInitialScale(25);
        }
        webView.setWebChromeClient(new WebChromeClient());
        if(getIntent().getBooleanExtra("post", false)){
            getIntent().getStringExtra("params");
            webView.postUrl(url, EncodingUtils.getBytes(getIntent().getStringExtra("params"),
                "base64"));
        }else{
            webView.loadUrl(url);
        }
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("ucredit://backtoapp")){
                    setResult(Activity.RESULT_OK);
                    finish();
                }else{
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                checkNet(new OnClickListener(){

                    @Override
                    public void onClick(View arg0) {
                        view.loadUrl(url);
                    }
                    
                });
                setMask(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setMask(false);
            }
            
            public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
                handler.proceed();
            }
        });
        
        webViewScroolChangeListener();

    }
    
    //核心代码
    private void webViewScroolChangeListener() {
        webView.setOnCustomScroolChangeListener(new ScrollInterface() {
            @Override
            public void onSChanged(int l, int t, int oldl, int oldt) {
                //WebView的总高度
                float webViewContentHeight=webView.getContentHeight() * webView.getScale();
                //WebView的现高度
                float webViewCurrentHeight=(webView.getHeight() + webView.getScrollY());
                if ((webViewContentHeight-webViewCurrentHeight) == 0) {
                    
                }
            }
        });
    }

    @Override
    protected boolean hasTitle() {
        return true;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return getIntent().getStringExtra("title");
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_advertise;
    }

}
