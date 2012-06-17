package com.singularity.clover.babysitter.notification;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.R;
import com.singularity.clover.babysitter.database.DBAdapter;

public class Notification {
	public static String TAG = "notification";
	
	public static String TABLE_SCHEMA = 
			"CREATE TABLE IF NOT EXISTS "
				 	+ "notification("
					+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "create_date INTEGER NOT NULL,"
					+ "trigger_date INTEGER NOT NULL,"
					+ "description STRING,"
					+ "status STRING NOT NULL,"
					+ "from_contacter STRING,"
					+ "to_contacter STRING,"
					+ "attribute STRING NOT NULL,"
					+ "notification_type STRING"
					+ ")";
	
	protected long lID = Global.ID_INVALID;
	protected long lCreateDate = Global.DATE_INVALID;
	protected long lTriggerDate = Global.DATE_INVALID;
	protected String strDescription = null;
	public enum Status{ON ,OFF};
	protected Status enumStatus = Status.ON;
	protected String strFromContacter = null;
	protected String strToContacter = null;
	public enum Attribute {RECEIVED,SENT,OWEN};
	public enum NotificationType {RING};
	protected Attribute enumAttribute = Attribute.OWEN;
	protected NotificationType enumNotificationType = NotificationType.RING;
	
	protected Notification(){}
	
	public void load(Cursor cur){
		lID = cur.getLong(0);
		lCreateDate = cur.getLong(1);
		lTriggerDate = cur.getLong(2);
		strDescription = cur.getString(3);
		enumStatus = Status.valueOf(cur.getString(4));
		strFromContacter = cur.getString(5);
		strToContacter = cur.getString(6);
		enumAttribute = Attribute.valueOf(cur.getString(7));
		enumNotificationType = NotificationType.valueOf(cur.getString(8));
	}
	
	public void save(){
		ContentValues contentValues = new ContentValues();
		contentValues.put("create_date", lCreateDate);
		contentValues.put("trigger_date",lTriggerDate);
		contentValues.put("description",strDescription);
		contentValues.put("status",enumStatus.name());
		contentValues.put("from_contacter",strFromContacter);
		contentValues.put("to_contacter", strToContacter);
		contentValues.put("attribute",enumAttribute.name());
		contentValues.put("notification_type",enumNotificationType.name());
		
		lID = DBAdapter.instance().insert(TAG, contentValues);
		NotificationFactory.instance().notifyObservers();
		Log.d("Notification","Notification "+lID+"is saved");
	}
	
	public void update(ContentValues contentValues){
		DBAdapter.instance().updateEntry(TAG,lID,contentValues);
		if( contentValues.containsKey("status") || contentValues.containsKey("trigger_date") 
				|| contentValues.containsKey("attribute")){
			NotificationFactory.instance().notifyObservers();
		}
	}
	
	public void update(){
		ContentValues contentValues = new ContentValues();
		contentValues.put("create_date", lCreateDate);
		contentValues.put("trigger_date",lTriggerDate);
		contentValues.put("description",strDescription);
		contentValues.put("status",enumStatus.name());
		contentValues.put("from_contacter",strFromContacter);
		contentValues.put("to_contacter", strToContacter);
		contentValues.put("attribute",enumAttribute.name());
		contentValues.put("notification_type",enumNotificationType.name());
		update(contentValues);
	}
	
	public void preUpdateStatus(Status enumStatus,ContentValues contentValues){
		contentValues.put("status",enumStatus.name());
	}
	
	public void preUpdateAttribute(Attribute enumAttribute,ContentValues contentValues){
		contentValues.put("attribute",enumAttribute.name());
	}
	
	public void preUpdateFrom(String strFrom, ContentValues contentValues){
		contentValues.put("from_contacter",strFrom);
	}
	
	public void preUpdateToContacter(String strTo,ContentValues contentValues){
		contentValues.put("to_contacter", strTo);
	}
	
	public void preUpdateTriggerDate(long lTriggerDate,ContentValues contentValues){
		contentValues.put("trigger_date", lTriggerDate);
	}
	
	public void delete(){
		String[] whereArgs = new String[] { Long.toString(lID) };
		DBAdapter.instance().deleteEntry(TAG, "_id=?", whereArgs);
	}
	
	public boolean isEqual(Notification notification){
		if(notification == null){
			return false;
		}else{
			return (lID == notification.getlID() 
					&& lTriggerDate == notification.getlTriggerDate());}
	}

	public long getlID() {
		return lID;
	}

	public void setlID(long lID) {
		this.lID = lID;
	}

	public long getlCreateDate() {
		return lCreateDate;
	}

	public void setlCreateDate(long lCreateDate) {
		this.lCreateDate = lCreateDate;
	}

	public long getlTriggerDate() {
		return lTriggerDate;
	}

	public void setlTriggerDate(long lTriggerDate) {
		this.lTriggerDate = lTriggerDate;
	}

	public String getStrDescription() {
		Context context = BabysitterApplication.instance().getApplicationContext();
		if(strDescription == null){
			String content = context.getString(R.string.alarm);
			return content;
		}else{
			if(strDescription.equals("")){
				String content = context.getString(R.string.alarm);
				return content;
			}
		}
		return strDescription;
	}

	public void setStrDescription(String strDescription) {
		this.strDescription = strDescription;
	}

	public Status getEnumStatus() {
		return enumStatus;
	}

	public void setEnumStatus(Status enumStatus) {
		this.enumStatus = enumStatus;
	}

	public String getstrFromContacter() {
		return strFromContacter;
	}

	public void setstrFromContacter(String strFromContacter) {
		this.strFromContacter = strFromContacter;
	}

	public String getstrToContacter() {
		return strToContacter;
	}

	public void setstrToContacter(String strToContacter) {
		this.strToContacter = strToContacter;
	}

	public Attribute getEnumAttribute() {
		return enumAttribute;
	}

	public void setEnumAttribute(Attribute enumAttribute) {
		this.enumAttribute = enumAttribute;
	}
	
}
