package com.ucredit.dream.utils.update;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.Utils;

public class CheckUpdateUtil {

    private static CheckUpdateUtil instance = null;
    private Context context = null;
    private int localVersion;
    private boolean isFromSetting;
    private Dialog loadingDialog;

    private CheckUpdateUtil() {

    }

    public static CheckUpdateUtil getInstance() {
        if (instance == null) {
            instance = new CheckUpdateUtil();
        }
        return instance;
    }

    public static void destroy() {
        if (instance != null) {
            instance = null;
        }
    }

    public void startCheck(Context context, boolean isFromSetting) {
        this.context = context;
        this.isFromSetting = isFromSetting;
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
            checkUpdate();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate() {
        loadingDialog = Utils.showProgressDialog(context, "检查中...", true);
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setURLEncodingEnabled(false);
        RequestParams params = new RequestParams();
        params.put("os", "1");
        params.put("channel", UcreditDreamApplication.UMENG_CHANNEL);
        asyncHttpClient.get(Constants.URL_APP_VERSION, params,
            new BaseJsonHttpResponseHandler<UpdateInfo>() {

                @Override
                public void onSuccess(int statusCode, Header[] headers,
                        String rawJsonResponse, UpdateInfo response) {
                    Logger.e("checkUpdate", ""+rawJsonResponse +"" +statusCode);
                    if (null == response) {
                        return;
                    }
                    if (Integer.valueOf(response.getVersion()) > localVersion) {
                        showUpdateDialog(context, response);
                    } else {
                        if (isFromSetting) {
                            Utils.MakeToast(context, "已经是最新版本");
                        }
                    }
                    loadingDialog.dismiss();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                        Throwable throwable, String rawJsonData,
                        UpdateInfo errorResponse) {
                    Logger.e("checkUpdate", ""+rawJsonData +"" +statusCode);
                    loadingDialog.dismiss();
                }

                @Override
                public void onStart() {
                    super.onStart();
                    loadingDialog.show();
                }

                @Override
                protected UpdateInfo parseResponse(String rawJsonData,
                        boolean isFailure) throws Throwable {
                    if (Utils.isNotEmptyString(rawJsonData)) {
                        JSONObject jsonObject = new JSONObject(rawJsonData);
                        if (jsonObject.optBoolean("success")) {
                            JSONObject data = jsonObject
                                .optJSONObject("result");
                            UpdateInfo updateInfo = new UpdateInfo();
                            updateInfo.setVersion(data.optString("versionCode"));
                            updateInfo.setDownloadUrl(data
                                .optString("downloadURL"));
                            updateInfo.setContent(data.optString("versionInfo"));
                            if (0 == data.optInt("isForceUpdate")) {
                                updateInfo.setForceUpdate(false);
                            } else {
                                updateInfo.setForceUpdate(true);
                            }
                            return updateInfo;
                        }
                    }
                    return null;
                }
            });
    }

    protected void showUpdateDialog(final Context context,
            final UpdateInfo response) {
        WindowManager manager = ((Activity) context).getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = width * 3 / 4;//定义宽度  
        window.setAttributes(lp);
        View view = LayoutInflater.from(context).inflate(
            R.layout.checkupdate_util, null);
        window.setContentView(view);

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(!response.isForceUpdate());

        TextView content = (TextView) view.findViewById(R.id.update_content);
        content.setText(response.getContent());
        TextView confirm = (TextView) view.findViewById(R.id.update);
        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        confirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, UpdateApkService.class);
                Bundle bundle = new Bundle();
                bundle.putString("Key_App_Name",
                    context.getString(R.string.app_name));
                bundle.putString("Key_Down_Url", response.getDownloadUrl());
                bundle.putString("version_name", response.getVersion());
                intent.putExtras(bundle);
                context.startService(intent);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (!response.isForceUpdate()) {
                    dialog.dismiss();
                }
            }
        });
    }

}
