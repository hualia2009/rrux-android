package com.ucredit.dream.utils;


import android.widget.TextView;

import com.ucredit.dream.R;

public class VerifyCodeTimer extends CountDownTimer {

    private TextView textView;
    
    private long left;

    public VerifyCodeTimer(long millisInFuture, long countDownInterval,
            TextView textView) {
        super(millisInFuture, countDownInterval);
        this.textView = textView;
        left=millisInFuture;
    }

    @Override
    public void onFinish() {
    	left=0;
    	cancel();
        textView.setEnabled(true);
        textView.setBackgroundResource(R.drawable.button_bg);
        textView.setText("发送验证码");
    }

    @Override
    public void onTick(long arg0) {
    	left=arg0;
        textView.setEnabled(false);
        textView.setBackgroundResource(R.drawable.inputhalf_gray);
        textView.setText("重新获取(" + Math.round(((float)arg0/1000)) + "s)");
    }

	public long getLeft() {
		return left;
	}
    
}
