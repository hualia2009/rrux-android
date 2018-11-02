package com.ucredit.crash;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.utils.Constants;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.utils.mail.MailSenderInfo;
import com.ucredit.dream.utils.mail.SimpleMailSender;

/**
 * 全局异常处理，发送邮件至服务器
 * @author Li Huang
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getSimpleName();
    private static CrashHandler instance;
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;
    private Properties mCrashInfo = new Properties();

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }

        return instance;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        if(mDefaultHandler == null){
            exitCurrentApp();
            return;
        }
        
        if(ex == null){
            exitCurrentApp();
            return;
        }
        
        if(Logger.DEBUG){
            mDefaultHandler.uncaughtException(thread, ex);
            return;
        }
        
        ex.printStackTrace();
        new Thread() {
            @Override
            public void run() {
                // 在当前线程创建消息队列(对话框的显示需要消息队列)
                Looper.prepare();
                Utils.MakeToast(mContext, "程序出现异常，请重新启动应用重试或者联系相关人员！");
                collectDeviceInfo(mContext,ex);
                sendToEmail();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exitCurrentApp();
                    }
                }, 5 * 1000);
                Looper.loop();
            }
        }.start();
    }

    /**
     * 强制关闭程序<br>
     * FIXME 并不能退出所有Activity,目前尚未找到比较优雅的做法
     */
    private void exitCurrentApp() {
        if (UcreditDreamApplication.list!=null) {
            for (Activity activity : UcreditDreamApplication.list) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 收集信息
     * @param ctx
     */
    private void collectDeviceInfo(Context ctx,Throwable ex) {
        PackageHelper packageHelper = new PackageHelper(mContext);
        mCrashInfo.put("versionName", packageHelper.getLocalVersionName());
        mCrashInfo.put("versionCode", packageHelper.getLocalVersionCode() + "");
        mCrashInfo.put("umengChannel", ""
            + UcreditDreamApplication.UMENG_CHANNEL);
        mCrashInfo.put("province", "" + UcreditDreamApplication.province);
        mCrashInfo.put("city", "" + UcreditDreamApplication.city);
        mCrashInfo.put("clientId", "" + UcreditDreamApplication.clientId);
        if (UcreditDreamApplication.mUser != null) {
            mCrashInfo.put("phone",
                "" + UcreditDreamApplication.mUser.getPhone());
            mCrashInfo.put("pas",
                "" + UcreditDreamApplication.mUser.getPassword());
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        try {
            ex.printStackTrace(pw);
        } catch (Exception e1) {
            e1.printStackTrace();
        }finally{
            pw.close();
        }
        
        mCrashInfo.put("error", ""+ sw.toString());
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String fieldStr = "";
                try {
                    fieldStr = field.get(null).toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mCrashInfo.put(field.getName(), fieldStr);
            } catch (Exception e) {
                Logger.e(TAG, "Error while collecting device info"+e);
            }
        }
    }

    /**
     * 发送邮件
     */
    private void sendToEmail() {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                MailSenderInfo mailInfo = new MailSenderInfo();
                mailInfo.setMailServerHost("owa.ucredit.com");
                mailInfo.setMailServerPort("25");
                mailInfo.setValidate(true);
                mailInfo.setUserName("unotice@ucredit.com");
                mailInfo.setPassword("chuangXIN!@#");
                mailInfo.setFromAddress("unotice@ucredit.com");
                mailInfo.setToAddress(Constants.CUSTOMER_EMAIL);
                if(UcreditDreamApplication.mUser != null){
                    mailInfo.setSubject("人人U学Android异常反馈邮件:"+UcreditDreamApplication.mUser.getPhone());
                }else{
                    mailInfo.setSubject("人人U学Android异常反馈邮件:"+"13888888888");
                }
                mailInfo.setContent("用户信息：\n" + mCrashInfo.toString());
                Logger.e("sendToEmail", ""+mCrashInfo.toString());
                SimpleMailSender.sendHtmlMail(mailInfo);
            }
        }).start();
    }

}
