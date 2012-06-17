package com.singularity.clover.babysitter.notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.Global;

public class SmsLauncher implements RocketLauncher<String,Activity>{
	
	private Activity mPreActivity = null;
	
	public SmsLauncher(){}
	
	protected void sendSmsOlder(String body, Context context){
		 Intent sendIntent = new Intent(Intent.ACTION_VIEW);
	     sendIntent.putExtra("sms_body", body); 
	     sendIntent.setType("vnd.android-dir/mms-sms");
	     context.startActivity(sendIntent);
	}
	
	protected void sendSms(String body,Context context){
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.putExtra("sms_body",body);
		sendIntent.setData(Uri.parse("sms:"));
		context.startActivity(sendIntent);
	}

	@Override
	public boolean launch(Notification notification,Activity preActivity) {
		String body = serialized(notification);
		mPreActivity = preActivity;
		if(mPreActivity != null){
			sendSms(body,mPreActivity);
			return true;
		}else{
			return false;}
	}
	
	@Override
	public String serialized(Notification notification){
		String result = Global.PREFIX_SMS;
		Date date = new Date(notification.lTriggerDate);
		String where =BabysitterApplication.instance().getResources().
				getConfiguration().locale.getLanguage();
		SimpleDateFormat format;
		format = new SimpleDateFormat(Global.DATE_FORMAT,Locale.US);
		
		result += format.format(date);
		result += Global.SMS_SPLIT_INDICATOR;
		result += notification.getStrDescription();
		result += Global.SUFFIX_SMS;
		
		return result;
	}
	
	@Override
	public boolean deserialized(String body,Notification notification){

		 if(deserializedString(body, notification,
				 Global.PREFIX_SMS_EN,Global.SUFFIX_SMS_EN)){
			 return true;
		 }else{
			if(deserializedString(body, notification,
					 Global.PREFIX_SMS_ZH, Global.SUFFIX_SMS_ZH)){
				return true;
			}else{
				return false;}
		 }
	}
	
	public static boolean deserializedString(String body,
			Notification notification,String prefix,String suffix){
		if(body == null){
			return false;}
		/*
		 * 如果文本被修改后，容错到什么级别
		 * */
		body.trim();
		int indexStart = body.indexOf(prefix);
		if(indexStart == -1){
			return false;}
		int indexEnd = body.lastIndexOf(suffix);
		String content;
		if(indexEnd != -1){
			content = body.substring(indexStart + prefix.length(),indexEnd);
		}else{
			content = body.substring(indexStart + prefix.length());}
		
		String[] arrays = content.split(Global.SMS_SPLIT_INDICATOR_REGEX);
		
		//String dtStart = "2010-10-15T09:27:37Z";  
		SimpleDateFormat format = new SimpleDateFormat(Global.DATE_FORMAT,Locale.US);  
		try {  
		    Date date = format.parse(arrays[0]);  
		    notification.lTriggerDate = date.getTime(); 
		} catch (ParseException e) {  
		    e.printStackTrace();  
		}
		notification.strDescription = arrays[1];
		return true;
	}
	
}
