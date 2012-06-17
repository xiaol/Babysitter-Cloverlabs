package com.singularity.clover.babysitter.activity;

import com.singularity.clover.babysitter.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class WakeupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.layout_wakeup);
	}

}
