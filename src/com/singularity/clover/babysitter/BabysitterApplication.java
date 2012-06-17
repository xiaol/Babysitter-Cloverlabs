package com.singularity.clover.babysitter;



import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.singularity.clover.babysitter.activity.BabysitterActivity;
import com.singularity.clover.babysitter.activity.KeepMeAliveService;
import com.singularity.clover.babysitter.database.DBAdapter;
import com.singularity.clover.babysitter.notification.NotificationFactory;
import com.singularity.clover.babysitter.notification.NotificationManager;
import com.singularity.clover.babysitter.sms.SmsObserver;
import com.singularity.clover.babysitter.sms.SmsReceiver;

public class BabysitterApplication extends Application {
	public BabysitterApplication(){}
	
	private static BabysitterApplication self = null;
	private NotificationManager mNotificationManager = null;
	private SmsObserver mSmsObserver;
	private BabysitterActivity mListActivity;
	
	@Override
	public void onCreate() {
		Log.d("BabysitterApplication", "onCreate()");
		Log.d("BabysitterApplication", getApplicationContext().
					getResources().getConfiguration().locale.getLanguage());
		
		super.onCreate();
		DBAdapter.initialize(this);
		
		self = this;
		mNotificationManager = new NotificationManager();
		mSmsObserver = new SmsObserver(new Handler(), getApplicationContext());
		NotificationFactory.instance().registerObserver(mNotificationManager);
		
		Global.PREFIX_SMS = getString(R.string.prefix_sms);
		Global.SUFFIX_SMS = getString(R.string.suffix_sms);
		
		getContentResolver().registerContentObserver(
				Uri.parse(SmsReceiver.SMS_URI), true,mSmsObserver);
		NotificationFactory.instance().notifyObservers();
		getApplicationContext().startService(
				new Intent(getApplicationContext(), KeepMeAliveService.class));
		
	}
	
	public static BabysitterApplication instance(){
		return self;
	}
	
	public NotificationManager getNotificationManager(){
		return mNotificationManager;
	}
	
	public SmsObserver getSmsObserver(){
		return mSmsObserver;
	}
	
	/**
	 * used for testCase
	 * @param testCase
	 */
	private Object mTestCase = null;
	public void setTestCase(Object testCase){
		mTestCase = testCase;
	}
	
	public Object getTestCase(){
		return mTestCase;
	}
	
	public boolean isNotification(String body){
		if(body.contains(Global.PREFIX_SMS_EN)
				|| body.contains(Global.PREFIX_SMS_ZH)){
			return true;
		}else{
			return false;}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mListActivity = null;
	}
	
	public boolean isListActivityAlive(boolean update){
		if(update && mListActivity != null){
			mListActivity.updateList(null,false);
			return true;
		}else{
			return false;
		}
	}
	
	public void setListActivity(BabysitterActivity list){
		mListActivity = list;
	}
	
	public void removeListActivity(){
		mListActivity = null;
	}
}
