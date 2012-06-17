package com.singularity.clover.babysitter.notification;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;

import com.singularity.clover.babysitter.database.DBAdapter;
import com.singularity.clover.babysitter.notification.Notification.Attribute;

public class NotificationFactory {
	public static String ASDBY_TRIGGER_DATE = "order by trigger_date ASC";
	public static String DESCBY_CREATE_DATE = "order by create_date DESC";
	public static String LIMIT_X = "limit ";
	public static String STATUS_ON = "status = 'ON'";
	public static String AFTER_TIRGGER_DATE_X = "trigger_date > ";
	
	protected RocketLauncher<String,Activity> mSmsLauncher;
	
	private ArrayList<Observer> mArrayObservers = new ArrayList<NotificationFactory.Observer>();
	
	private static NotificationFactory self = null;
	private NotificationFactory(){
		mSmsLauncher = new SmsLauncher();
	}
	
	public static NotificationFactory instance(){
		if(self == null){
			self = new NotificationFactory();}
		return self;
	}
	
	public static Notification load(int id){
		Cursor curs = DBAdapter.instance().retrieveById(Notification.TAG, id);
		Notification notification = null;
		if(curs.moveToFirst()){
			notification = new Notification();
			notification.load(curs);	}
		
		curs.close();
		return notification;
	}
	
	public static Notification create(long lTriggerDate){
		Notification notification = new Notification();

		notification.lCreateDate = System.currentTimeMillis();
		notification.lTriggerDate = lTriggerDate;
		return notification;
	}
	
	
	public static ArrayList<Notification> loadFromTable(String whereClause,String[] whereArgs){
		Cursor curs = DBAdapter.instance().retrieveAll(Notification.TAG, whereClause, whereArgs);
		ArrayList<Notification> arrayNotifications = new ArrayList<Notification>();
		if(curs.moveToFirst()){
			do{
				Notification notification = new Notification();
				notification.load(curs);
				arrayNotifications.add(notification);
			}while(curs.moveToNext());
		}
		curs.close();
		return arrayNotifications;
	}
	
	@Deprecated
	public static void clearDB(){
		DBAdapter.instance().deleteEntry(Notification.TAG, null, null);
	}
	
	
	/*@Deprecated
	 * public Notification loadFromSms(SmsMessage sms){
		Notification notification = new Notification();
		notification.lCreateDate = System.currentTimeMillis();
		if(mSmsLauncher.deserialized(sms, notification)){
			notification.save();
			return notification;
		}else{
			return null;}
	}*/
	
	public Notification create(long createDate ,String smsBody,
			String fromAddress, Attribute attribute){
		Notification notification = new Notification();
		notification.lCreateDate = createDate;
		if(mSmsLauncher.deserialized(smsBody, notification)){
			notification.strFromContacter = fromAddress;
			notification.enumAttribute = attribute;
			notification.save();
			return notification;
		}else{
			return null;}		
	}
	
	public interface Observer{
		void handleNotificationChanged();
	}
	
	public void registerObserver(Observer observer){
		mArrayObservers.add(observer);
	}
	
	public void notifyObservers(){
		for(Observer entity:mArrayObservers){
			entity.handleNotificationChanged();}
	}
	
	@SuppressWarnings("rawtypes") 
	public void setLauncher(RocketLauncher launcher){}
	
	public void SendNotification(Activity activity,Notification notification){
		mSmsLauncher.launch(notification, activity);
	}
}
