package com.singularity.clover.babysitter.sms;

import com.singularity.clover.babysitter.BabysitterApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//@Deprecated
public class SmsReceiver extends BroadcastReceiver {

	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";
	public static final String SMS_INBOX_URI = "content://sms/inbox";
	
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    
    public static final String DISPLAY_NAME ="display_name";
    public static final String ID ="_id";
    
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    
    public static final int MESSAGE_IS_NOT_READ = 0;
    public static final int MESSAGE_IS_READ = 1;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if(action.equals(SMS_RECEIVED)){
			//NotificationFactory.instance().notifyObservers();   
			BabysitterApplication.instance();
		}
        
        /* WARNING!!! 
         * If you uncomment next line then received SMS will not be put to incoming.
         * Be careful!*/
        //this.abortBroadcast();

	}

}
