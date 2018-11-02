package com.ucredit.dream.utils.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.ucredit.dream.R;
import com.ucredit.dream.utils.FileUtil;
import com.ucredit.dream.utils.Utils;

/***
 * 升级服务
 */
public class UpdateApkService extends IntentService {

    public UpdateApkService() {
        super("updateapk");
    }

    public static final int UPDATE_PROGRESS = 0;
    public static final String Install_Apk = "Install_Apk";
    /******** download progress step *********/
    private static final int down_step_custom = 3;

    private static final int TIMEOUT = 10 * 1000;// 超时
    private static String down_url;

    private String app_name;

    private NotificationManager notificationManager;
    private Notification notification;
    private PendingIntent pendingIntent;
    private RemoteViews contentView;
    private String fileNameString;

    @SuppressWarnings("deprecation")
    private void download() {
        try {
            long downloadSize = downloadUpdateFile(down_url,
                FileUtil.updateFile.toString());
            if (downloadSize > 0) {
                Uri uri = Uri.fromFile(FileUtil.updateFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri,
                    "application/vnd.android.package-archive");
                pendingIntent = PendingIntent.getActivity(
                    UpdateApkService.this, 0, intent, 0);

//                notification.flags = Notification.FLAG_AUTO_CANCEL;
//                notification.setLatestEventInfo(UpdateApkService.this,
//                    app_name, getString(R.string.down_sucess), pendingIntent);

                Notification.Builder builder = new Notification.Builder(UpdateApkService.this)
                        .setAutoCancel(true)
                        .setContentTitle(app_name)
                        .setContentText(getString(R.string.down_sucess))
                        .setContentIntent(pendingIntent);
                notification=builder.getNotification();

                notificationManager.notify(R.layout.notification_item,
                    notification);
                Intent intent2 = new Intent();
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.setAction(android.content.Intent.ACTION_VIEW);
                intent2.setDataAndType(Uri.fromFile(FileUtil.updateFile),
                    "application/vnd.android.package-archive");
                startActivity(intent2);

                Bundle resultData = new Bundle();
                resultData.putInt("progress", 100);
                resultData.putString("update_file_path",
                    FileUtil.getUpdateFilePath(fileNameString, this));
            }
        } catch (Exception e) {
//            notification.flags = Notification.FLAG_AUTO_CANCEL;
//            notification.setLatestEventInfo(UpdateApkService.this, app_name,
//                getString(R.string.down_fail), null);
            Notification.Builder builder = new Notification.Builder(UpdateApkService.this)
                    .setAutoCancel(true)
                    .setContentTitle(app_name)
                    .setContentText(getString(R.string.down_fail))
                    .setContentIntent(pendingIntent);
            notification=builder.getNotification();
            notificationManager
                .notify(R.layout.notification_item, notification);
            new File(FileUtil.getUpdateFilePath(fileNameString, this)).delete();
        }
    }

    /**
     * 方法描述：createNotification方法
     * 
     * @param
     * @return
     * @see UpdateApkService
     */
    @SuppressWarnings("deprecation")
    public void createNotification() {
        notification = new Notification(R.drawable.ic_launcher,// 应用的图标
            app_name + getString(R.string.is_downing),
            System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        /*** 自定义 Notification 的显示 ****/
        contentView = new RemoteViews(getPackageName(),
            R.layout.notification_item);
        contentView.setTextViewText(R.id.notificationTitle, app_name
            + getString(R.string.is_downing));
        contentView.setTextViewText(R.id.notificationPercent, "0%");
        contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
        notification.contentView = contentView;

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.layout.notification_item, notification);
    }

    /***
     * down file
     * 
     * @return
     * @throws MalformedURLException
     */
    public long downloadUpdateFile(String down_url, String file)
            throws Exception {

        int down_step = down_step_custom;// 提示step
        int totalSize;// 文件总大小
        int downloadCount = 0;// 已经下载好的大小
        int updateCount = 0;// 已经上传的文件大小

        InputStream inputStream;
        OutputStream outputStream;

        URL url = new URL(down_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url
            .openConnection();
        httpURLConnection.setConnectTimeout(TIMEOUT);
        httpURLConnection.setReadTimeout(TIMEOUT);
        // 获取下载文件的size
        totalSize = httpURLConnection.getContentLength();

        if (httpURLConnection.getResponseCode() == 404) {
            throw new Exception("fail!");
            // 这个地方应该加一个下载失败的处理，但是，因为我们在外面加了一个try---catch，已经处理了Exception,
            // 所以不用处理
        }

        inputStream = httpURLConnection.getInputStream();
        outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉

        byte buffer[] = new byte[1024];
        int readsize = 0;

        while ((readsize = inputStream.read(buffer)) != -1) {

            outputStream.write(buffer, 0, readsize);
            downloadCount += readsize;// 时时获取下载到的大小
            /*** 每次增张3% **/
            if (updateCount == 0
                || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
                updateCount += down_step;
                // 改变通知栏
                contentView.setTextViewText(R.id.notificationPercent,
                    updateCount + "%");
                contentView.setProgressBar(R.id.notificationProgress, 100,
                    updateCount, false);
                notification.contentView = contentView;
                notificationManager.notify(R.layout.notification_item,
                    notification);
            }
        }
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
        inputStream.close();
        outputStream.close();

        return downloadCount;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        app_name = bundle.getString("Key_App_Name");
        down_url = bundle.getString("Key_Down_Url");

        fileNameString = app_name + bundle.getString("version_name");
        try {
            File file = new File(FileUtil.getUpdateFilePath(fileNameString,
                this));
            if (file.exists()) {
                Intent intent2 = new Intent();
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.setAction(android.content.Intent.ACTION_VIEW);
                intent2.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
                startActivity(intent2);
                return;
            }
        } catch (Exception e) {
        }
        // create file,应该在这个地方加一个返回值的判断SD卡是否准备好，文件是否创建成功，等等！
        FileUtil.createFile(app_name + bundle.getString("version_name"), this);

        if (FileUtil.isCreateFileSucess == true) {
            createNotification();
            download();
        } else {
            Utils.MakeToast(this, this.getString(R.string.insert_card));
        }
        return;
    }

}