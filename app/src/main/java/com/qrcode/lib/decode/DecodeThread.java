package com.qrcode.lib.decode;

import java.util.concurrent.CountDownLatch;

import com.ucredit.dream.ui.activity.QRCodeActivity;


import android.os.Handler;
import android.os.Looper;

final class DecodeThread extends Thread {

    QRCodeActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeThread(QRCodeActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}
