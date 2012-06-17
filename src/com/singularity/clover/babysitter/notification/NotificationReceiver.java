package com.singularity.clover.babysitter.notification;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.sms.SmsReceiver;

public class NotificationReceiver extends BroadcastReceiver {

	public static final int ALARM_DURATION = 2*60*1000;
	public static final String IN_NOTIFICAION_ID = "notification_id";
	public static final int CANCEL_AUTOCANCEL = 2;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		NotificationManager notificationManager = 
				BabysitterApplication.instance().getNotificationManager();
		
		
		if(action.equals(NotificationManager.ACTION_ALARM_RING)){
			notificationManager.fireAlarm();
			notificationManager.autoCancelAlarm(ALARM_DURATION,CANCEL_AUTOCANCEL);
			if(Global.DEBUG_MODE){
				Notification testOne = notificationManager.getNextNotification();
				if(testOne != null){
					Date date = new Date(testOne.lTriggerDate);
					SimpleDateFormat format = new SimpleDateFormat(Global.DATE_FORMAT);
					Log.d("NotificationReceiver", 
							testOne.getlID()+" "+
							testOne.getstrFromContacter()+" " +
							testOne.getStrDescription()+" "+
							format.format(date));
				}
			}
			NotificationFactory.instance().notifyObservers();
			long id = intent.getExtras().getLong(IN_NOTIFICAION_ID);
			notificationManager.alert(id);
			
		}else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			NotificationFactory.instance().notifyObservers();
			BabysitterApplication.instance();
		}
	}
	
	
}
