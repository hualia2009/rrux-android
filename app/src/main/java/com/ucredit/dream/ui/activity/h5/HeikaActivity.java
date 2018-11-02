package com.ucredit.dream.ui.activity.h5;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint({ "NewApi", "JavascriptInterface" })
public class HeikaActivity extends BaseActivity {

    @ViewInject(R.id.advertise_detail_content)
    private WebView webView;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.submit)
    private Button back;
    
    private String url;

    
    private Handler handler;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        
        handler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                back.setVisibility(View.VISIBLE);
            };
        };
        
        url = getIntent().getStringExtra("url");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        webSettings.setDomStorageEnabled(true);   
        webSettings.setAppCacheMaxSize(1024*1024*8);  
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();  
        webSettings.setAppCachePath(appCachePath);  
        webSettings.setAllowFileAccess(true);  

        webSettings.setAppCacheEnabled(true); 
        
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new InJavaScriptLocalObj(this),
                "android");
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.e("@@@", ""+url);
                if (url.startsWith("heika:")) {
                    handleUrl(view,url);
                } else if(url.startsWith("mailto:") || url.startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url)); 
                    startActivity(intent); 
                } else if (url.contains("ucredit://backtoapp")) {
                    finish();
                } else{
                    view.loadUrl(url);
                }
                return true;
            }
            
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                    String url) {
                Logger.e("@@@", ""+url);
                if(url!=null &&url.contains("authResult")){
                    handler.sendEmptyMessage(0);
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Logger.e("onPageStarted", ""+url);
                setMask(true);
                if(url.contains("viewReport")){
                    // 设置可以支持缩放 
                    view.getSettings().setSupportZoom(true); 
                    // 设置出现缩放工具 
                    view.getSettings().setBuiltInZoomControls(true);
                    //扩大比例的缩放
                    view.getSettings().setUseWideViewPort(true);
                }else if(url.contains("https://ipcrs.pbccrc.org.cn/")){
                    Uri  uri = Uri.parse(url);
                    Intent  intent = new  Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setMask(false);
                view.loadUrl("javascript:window.android.showToast(" +
                        "document.getElementsByClassName('money')[0].innerHTML);");
            }

        });
        
        back.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                webView.loadUrl(Constants.HEIKA_UNION_PAY
                    +"?access_token="+UcreditDreamApplication.token
                    +"&clientid="+UcreditDreamApplication.clientId
                    +"#bankCode");
                back.setVisibility(View.GONE);
            }
        });

    }
    
    private void handleUrl(WebView view, String url) {
        back.setVisibility(View.GONE);
        if(url.startsWith("heika://setTitle")){
            Uri uri = Uri.parse(url);
            String titleStr = uri.getQueryParameter("title");
            title.setText(titleStr);
        }else if(url.startsWith("heika://cookieExpired")){
            view.loadUrl(Constants.HEIKA_INDEX);
        }else if(url.startsWith("heika://close")){
            finish();
        }
    }
    
    @Override
    // 设置回退
    // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
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
    
    public class InJavaScriptLocalObj {
        private Context mContext;

        /** Instantiate the interface and set the context */
        public InJavaScriptLocalObj(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        public void showToast(String toast) {
            Logger.e("html", toast);
        }
    }

}
