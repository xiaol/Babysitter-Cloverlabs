package com.singularity.clover.babysitter.sms;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.notification.Notification;
import com.singularity.clover.babysitter.notification.Notification.Attribute;
import com.singularity.clover.babysitter.notification.NotificationFactory;
import com.singularity.clover.babysitter.notification.SmsLauncher;

public class SmsObserver extends ContentObserver {
	private Context mContext = null;

	public SmsObserver(Handler handler, Context context) {
		super(handler);
		mContext = context;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.d("SmsObserver", "Sms is changed");
		Cursor cursor = null;
		cursor = mContext.getContentResolver().query(
				Uri.parse(SmsReceiver.SMS_URI), null, null, null,SmsReceiver.DATE + " DESC limit 1");
		if (cursor != null) {
			long createDate;
			String body, address;
			int person;
			@SuppressWarnings("unused")
			int type, status, id;

			while (cursor.moveToNext()) {
				id = cursor.getInt(0);
				createDate = cursor.getLong(cursor.getColumnIndex(SmsReceiver.DATE));
				body = cursor.getString(cursor.getColumnIndex(SmsReceiver.BODY));
				address = cursor.getString(cursor.getColumnIndex(SmsReceiver.ADDRESS));
				type = cursor.getInt(cursor.getColumnIndex(SmsReceiver.TYPE));
				status = cursor.getInt(cursor.getColumnIndex(SmsReceiver.STATUS));
				person = cursor.getInt(cursor.getColumnIndex(SmsReceiver.PERSON));

				if (BabysitterApplication.instance().isNotification(body)) {
					Log.d("SmsObserver", "Sms:" + id+ " is kind of notification");
					Notification notification = NotificationFactory
							.create(System.currentTimeMillis());
					if (SmsLauncher.deserializedString(body, notification,
							Global.PREFIX_SMS_EN, Global.SUFFIX_SMS_EN)) {
					} else {
						if(SmsLauncher.deserializedString(body, notification,
								Global.PREFIX_SMS_ZH, Global.SUFFIX_SMS_ZH)){
							}else{}
						}
					String[] dates = {
							Long.toString(notification.getlTriggerDate()),
							Long.toString(notification.getlTriggerDate() + 1000) };
					ArrayList<Notification> sent = NotificationFactory
							.loadFromTable("WHERE trigger_date > ? AND trigger_date < ?",dates);
					if (sent.isEmpty()) {
						if (type == SmsReceiver.MESSAGE_TYPE_INBOX ) {
							String from = address;
							Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
							Cursor phoneCurosr = mContext.getContentResolver().query(
									uri, new String[]{PhoneLookup.DISPLAY_NAME},null,null,null);
							if(phoneCurosr.moveToFirst()){
								from = phoneCurosr.getString(0);}
							notification = NotificationFactory.instance().
									create(createDate, body, from,Attribute.RECEIVED);
							BabysitterApplication.instance().getNotificationManager()
									.alertReceivedNotification(notification);
						}
					}else{
						if (type != SmsReceiver.MESSAGE_TYPE_INBOX && address != null) {
							ContentValues contentValues = new ContentValues();
							sent.get(0).preUpdateAttribute(Attribute.SENT,contentValues);
							sent.get(0).preUpdateFrom(address, contentValues);
							sent.get(0).update(contentValues);
						}
					}
				}
				cursor.close();

			}
			if (Global.DEBUG_MODE) {
				synchronized (BabysitterApplication.instance().getTestCase()) {
					BabysitterApplication.instance().getTestCase().notify();
				}
			}
		}
	}

}