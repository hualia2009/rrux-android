package com.ucredit.dream.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.igexin.sdk.PushConsts;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.ui.activity.h5.HeikaActivity;
import com.ucredit.dream.ui.activity.main.HomePageActivity;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.SharePreferenceUtil;
import com.ucredit.dream.utils.Utils;


public class GetuiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {

            case PushConsts.GET_MSG_DATA:
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null
                    ) {
                    String data = new String(payload);
                    Logger.e("GetuiSdkDemo", "Got Payload:" + data);
                    try {
                        JSONObject object = new JSONObject(data);
                        String status = object.optString("status");
                        String title = object.optString("title");
                        String content = object.optString("content");
                        String time = object.optString("pushTime");
                        String url = object.optString("url");
                        NotificationManager manager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                        if (UcreditDreamApplication.mUser == null) {
                            return;
                        } else {
                            Intent toActivityPage;
                            if (Utils.isNotEmptyString(url)) {
                                toActivityPage=new Intent(context, HeikaActivity.class);
                                toActivityPage.putExtra("url", url);
                                toActivityPage.putExtra("title", title);
                            } else {
                                toActivityPage=new Intent(context, HomePageActivity.class);
                            }
                            PendingIntent pendingIntent = PendingIntent
                                .getActivity(context, 0, toActivityPage,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
//                            Notification noti = new Notification(
//                                R.drawable.ic_launcher, title,
//                                System.currentTimeMillis());
//
//                            noti.setLatestEventInfo(context, title, content,
//                                pendingIntent);
//                            noti.flags = Notification.FLAG_AUTO_CANCEL;

                            Notification.Builder builder = new Notification.Builder(context)
                                    .setAutoCancel(true)
                                    .setContentTitle(title)
                                    .setContentText(content)
                                    .setContentIntent(pendingIntent)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setWhen(System.currentTimeMillis());
                            Notification noti=builder.getNotification();
                            manager.notify(0, noti);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)           
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                Logger.e("cid", cid);
                if (SharePreferenceUtil.getInstance(context).getGetuiCid()
                    .equals("null")||!SharePreferenceUtil.getInstance(context).getGetuiCid().equals(cid)) {
                    UcreditDreamApplication.cid=cid;
                    SharePreferenceUtil.getInstance(context).setGetuiCid(cid);
                }
                break;
            default:
                break;
        }
    }
}
