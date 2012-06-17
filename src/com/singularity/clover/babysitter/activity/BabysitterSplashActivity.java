package com.singularity.clover.babysitter.activity;

import com.singularity.clover.babysitter.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BabysitterSplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.layout_splash_activity);
		SplashHandler handler = new SplashHandler();
		handler.sendEmptyMessageDelayed(0, 500);
	}
	
	
	private class SplashHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Intent intent = new Intent(BabysitterSplashActivity.this, BabysitterActivity.class);
			startActivity(intent);
			finish();
		}
		
	}
}
