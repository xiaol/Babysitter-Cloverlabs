package com.singularity.clover.babysitter.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TextView;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.BabysitterPreference;
import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.R;
import com.singularity.clover.babysitter.database.DBAdapter;
import com.singularity.clover.babysitter.notification.Notification;
import com.singularity.clover.babysitter.notification.Notification.Attribute;
import com.singularity.clover.babysitter.notification.Notification.Status;
import com.singularity.clover.babysitter.notification.NotificationFactory;
import com.singularity.clover.babysitter.widgets.DateSlider.DateSlider.OnDateSetListener;
import com.singularity.clover.babysitter.widgets.DateSlider.DateSlider;
import com.singularity.clover.babysitter.widgets.DateSlider.DateTimeSlider;

public class BabysitterActivity extends ListActivity {
	
	private Typeface mTypeface = null;
	private NotificationCursorAdapter mAdapter = null;
	private final int REQUESTCODE_NOTIFICATION_ACTIVITY = 0;
	private final int REQUESTCODE_PREFERENCE_ACTIVITY = 1;
	private final int MULTIPLE_CHOICE_MODE = 2;
	private final int NONE_CHOICE_MODE = 0;
	
	private int mMode = NONE_CHOICE_MODE;
	private View mBtnDelete = null,mBtnDeleteCancel = null,mDeletePanel = null;
	private View mBtnAdd = null, mSayLine = null;
	private TextView mTextTutorial;
	private ArrayList<Integer> mMultipleArrayIds = new ArrayList<Integer>();
	private Random mDice;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
        		WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.layout_babysitter_activity);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isPredinedAlarm = prefs.getBoolean(
        		getString(R.string.alarm_predefined_key), true);
        Cursor cursor;
        if(isPredinedAlarm){
	        cursor = DBAdapter.instance().retrieveAll(Notification.TAG,
	        		 NotificationFactory.ASDBY_TRIGGER_DATE, null);
        }else{
        	cursor = DBAdapter.instance().retrieveAll(Notification.TAG,
	        		 "where _id <> 1 "+NotificationFactory.ASDBY_TRIGGER_DATE, null );}
        
        mAdapter = new NotificationCursorAdapter(this, cursor);
        View header = View.inflate(this, R.layout.layout_header_of_list, null);
        header.setTag(Global.ID_INVALID); //when hierarchy changed,need this
		getListView().addHeaderView(header);
        getListView().setAdapter(mAdapter);
        AssetManager mgr = getAssets();
		mTypeface = Typeface.createFromAsset(mgr,Global.FONT_PATH);
		mBtnDelete = findViewById(R.id.buttonDelete);
		mBtnDeleteCancel = findViewById(R.id.buttonDeleteCancel);
		mBtnAdd = findViewById(R.id.buttonAdd);
		mDeletePanel = findViewById(R.id.deletePanel);
		mSayLine = findViewById(R.id.sayline);
		mTextTutorial = (TextView) findViewById(R.id.tutorialText);
		getListView().setOnHierarchyChangeListener(mHierarchyChangelistener);
		
		mDice = new Random(System.currentTimeMillis());
    }
    
    
    @Override
	protected void onPause() {
    	BabysitterApplication.instance().removeListActivity();
		super.onPause();
	}

	@Override
	protected void onResume() {
		BabysitterApplication.instance().setListActivity(this);
		super.onResume();
	}


	public class NotificationCursorAdapter extends CursorAdapter{
    	private Cursor mCursor = null;
    	
		public NotificationCursorAdapter(Context context, Cursor c) {
			super(context, c);
			mCursor = c;
			
		}

		@Override
		public void bindView(View listItem, Context arg1, Cursor cur) {
			fillView(cur,listItem);
		}

		@Override
		public View newView(Context context, Cursor cur, ViewGroup arg2) {
			View listItem = getLayoutInflater().inflate(
					R.layout.layout_notification_list_item, null);
			TextView textTime = (TextView) listItem.findViewById(
					R.id.textNotificationTime);
			textTime.setTypeface(mTypeface);
			View content = listItem.findViewById(R.id.notificationItem);
			fillView(cur, listItem);
			content.setOnClickListener(mOnNotificationClick);
			content.setOnLongClickListener(mOnNotificationLongClick);
			return listItem;
		}
		
		private void fillView(Cursor cur,View listItem){
			TextView textTime = (TextView) listItem.findViewById(
					R.id.textNotificationTime);
			TextView textDescription = (TextView) listItem.findViewById(
					R.id.textNotificationDescription);
			TextView textDate = (TextView) listItem.findViewById(
					R.id.textNotificationDate);
			View content = listItem.findViewById(R.id.notificationItem);
			content.setTag(cur.getInt(0));
			long triggerDate = cur.getLong(2);
			String description = cur.getString(3);
			
			Notification.Attribute attribute = Notification.Attribute.valueOf(cur.getString(7));
			Status status = Notification.Status.valueOf(cur.getString(4));
			String address = cur.getString(5);
		
			if(cur.getInt(0) == Global.SQLITE3_FISRT_ID){
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BabysitterActivity.this);
				int minutes = Integer.parseInt(
						prefs.getString(getString(R.string.set_delay_time_key),
						getResources().getStringArray(R.array.snooze_time_values_array)[0]));
				description = String.format(getString(R.string.description_snooze_alarm), minutes
						+" " +getString(R.string.minutes));
				address = getString(R.string.app_name);
				switch (status) {
				case ON:	
					break;
				case OFF:
					triggerDate = System.currentTimeMillis()+minutes*60*1000;
					ContentValues contentValues = new ContentValues();
					contentValues.put("trigger_date", triggerDate);
					contentValues.put("attribute",Attribute.RECEIVED.name());
					DBAdapter.instance().updateEntry(Notification.TAG, Global.SQLITE3_FISRT_ID, contentValues);
					break;
				default:
					break;
				}
			}
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(triggerDate);
			textTime.setText(DateFormat.format("kk:mm", date));
			textDate.setText(DateFormat.format("EEEE MMM d", date));
			textDescription.setText(description);
			
			adjustLayout(listItem, attribute,address);
			turnOn(listItem, status,triggerDate);
		}
		
		private void adjustLayout(View listItem, 
				Notification.Attribute attribute,String address){
			View left1 = listItem.findViewById(R.id.leftMargin1);
			View right1 = listItem.findViewById(R.id.rightMargin1);
			View left2 = listItem.findViewById(R.id.leftMargin2);
			View right2 = listItem.findViewById(R.id.rightMargin2);	
			TextView textStatusAddress = (TextView) listItem.findViewById(
					R.id.textNotificationAddress);
			switch(attribute){
			case OWEN:
				left1.setVisibility(View.INVISIBLE);
				right1.setVisibility(View.INVISIBLE);
				left2.setVisibility(View.GONE);
				right2.setVisibility(View.GONE);
				textStatusAddress.setVisibility(View.GONE);
				break;
			case RECEIVED:
				left1.setVisibility(View.GONE);
				right1.setVisibility(View.INVISIBLE);
				left2.setVisibility(View.GONE);
				right2.setVisibility(View.INVISIBLE);
				
				textStatusAddress.setVisibility(View.VISIBLE);
				textStatusAddress.setText(
						String.format(getString(R.string.from_address), address));
				break;
			case SENT:
				left1.setVisibility(View.INVISIBLE);
				right1.setVisibility(View.GONE);
				left2.setVisibility(View.INVISIBLE);
				right2.setVisibility(View.GONE);
				
				textStatusAddress.setVisibility(View.VISIBLE);
				textStatusAddress.setText(getString(R.string.to_address));
				break;
			}
			
			
		}
		
		private void turnOn(View listItem,
				Notification.Status status,long triggerDate){
			View content = listItem.findViewById(R.id.notificationItem);
			if(System.currentTimeMillis() > triggerDate){
				content.setBackgroundResource(R.drawable.list_item_gray_bg);
				return;}
			
			switch(status){
				case ON:
					content.setBackgroundResource(R.drawable.list_item_bg);
					break;
				case OFF:
					content.setBackgroundResource(R.drawable.list_item_gray_bg);
					break;
			}
		}
		
		public Cursor getCursor(){
			return mCursor;
		}
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.getCursor().close();
	}
    
    private OnClickListener mOnNotificationClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (mMode) {
			case MULTIPLE_CHOICE_MODE:
				View item = v;
				Integer notificationId = (Integer) v.getTag();
				if(notificationId == Global.SQLITE3_FISRT_ID){
					popupTutorial(getString(R.string.tutorial_disable_pre_alarm));
					break;}
				if(mMultipleArrayIds.contains(notificationId)){	
					Notification notification = NotificationFactory.load(notificationId);
					if(notification != null){
						mAdapter.turnOn(v, notification.getEnumStatus(),
								notification.getlTriggerDate());
					}else{
						Log.d("BabysitterActivity",
								"OnClickListener ,but can't find Notification");}
					mMultipleArrayIds.remove(notificationId);
					if(mMultipleArrayIds.isEmpty()){
						mBtnDelete.setEnabled(false);}
				}else{
					mMultipleArrayIds.add(notificationId);
					item.setBackgroundResource(R.drawable.list_item_orange_with_alarm);
					mBtnDelete.setEnabled(true);
				}	
				break;
			case NONE_CHOICE_MODE:
				Integer broughtId = (Integer) v.getTag();
				Intent intent = new Intent(BabysitterActivity.this, NotificationActivity.class);
				intent.setAction(NotificationActivity.ACTION_EDIT);
				intent.putExtra(NotificationActivity.IN_NOTIFICATION_ID, broughtId);
				startActivityForResult(intent, REQUESTCODE_NOTIFICATION_ACTIVITY);
				break;

			default:
				break;
			}
		}
	};

	private OnLongClickListener mOnNotificationLongClick = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Integer notificationId = (Integer) v.getTag();
			mLongClickedNotification =NotificationFactory.load(notificationId);
			switch (mMode) {
			case NONE_CHOICE_MODE:
				showDialog(NotificationActivity.TIMEDATE_DIALOG_ID);
				return true;
			default:
				return false;
			}
		}
	};
	
	private Notification mLongClickedNotification = null;
	private DateTimeSlider mDateSlider = null;
	public DateTimeSlider getDateSlider(){
		return mDateSlider;}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		final Calendar calendar = Calendar.getInstance();
		if(mLongClickedNotification != null){
			calendar.setTimeInMillis(mLongClickedNotification.getlTriggerDate());}
		switch (id) {
		case NotificationActivity.TIMEDATE_DIALOG_ID:
			mDateSlider = new DateTimeSlider(
					this, mDateListener, calendar);
			return mDateSlider;
		default:
			return null;
		}
	}
	
	OnDateSetListener mDateListener = new OnDateSetListener() {
		
		@Override
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			mLongClickedNotification.setlTriggerDate(selectedDate.getTimeInMillis());
			mLongClickedNotification.update();
			updateList(null,false);
		}
	};

	public void onAddButtonClick(View v){
    	Intent intent = new Intent(this,NotificationActivity.class);
    	intent.setAction(NotificationActivity.ACTION_NEW);
    	startActivityForResult(intent, REQUESTCODE_NOTIFICATION_ACTIVITY);
    }
	
	public void onDeleteButtonClick(View v){
		String where = "_id =?";
		
		for(int entity:mMultipleArrayIds){
			String[] whereClause = {Integer.toString(entity)};
			DBAdapter.instance().deleteEntry(
					Notification.TAG, 	where, whereClause);
		}
		mMultipleArrayIds.clear();	
		mDeletePanel.setVisibility(View.GONE);
		mBtnAdd.setVisibility(View.VISIBLE);
		
		Cursor oldCur = mAdapter.getCursor();
		Cursor cursor = DBAdapter.instance().retrieveAll(Notification.TAG,
        		 NotificationFactory.ASDBY_TRIGGER_DATE, null);
		mAdapter.changeCursor(cursor);
		mAdapter.notifyDataSetChanged();
		oldCur.close();
		mMode = NONE_CHOICE_MODE;
	}
	
	public void onBtnLogoClick(View v){
		Intent intent = new Intent(this, BabysitterPreference.class);
		startActivityForResult(intent,REQUESTCODE_PREFERENCE_ACTIVITY);
	}
	
	public void onDeleteCancelButtonClick(View v){
		mMultipleArrayIds.clear();
		mDeletePanel.setVisibility(View.GONE);
		mBtnAdd.setVisibility(View.VISIBLE);
		mMode = NONE_CHOICE_MODE;
		mAdapter.notifyDataSetChanged();
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_NOTIFICATION_ACTIVITY:
			updateList(null, false);
			if(1 == mDice.nextInt(3)){
				popupTutorial(getString(R.string.long_click));}
			break;
		case REQUESTCODE_PREFERENCE_ACTIVITY:
			if(resultCode == RESULT_OK){
				updateList(null, false);}
			break;
		default:
			break;
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 5, getString(R.string.delete)).setIcon(
		        android.R.drawable.ic_menu_delete);
		return true;
	}
    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case Menu.FIRST + 1:
        	mMode = MULTIPLE_CHOICE_MODE;
        	mDeletePanel.setVisibility(View.VISIBLE);
        	mBtnAdd.setVisibility(View.INVISIBLE);
    		mBtnDelete.setEnabled(false);
            break;
        }
		return false;
	}
	
	 @Override
	 public boolean onPrepareOptionsMenu(Menu menu) {
	    return true;}

	 
	private void popupTutorial(String words){
		mTextTutorial.setVisibility(View.VISIBLE);
		mSayLine.setVisibility(View.VISIBLE);
		
		mTextTutorial.setText(words);
		Handler handler = new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message arg0) {
				mTextTutorial.setVisibility(View.GONE);
				mSayLine.setVisibility(View.GONE);
				return true;
			}
		});
		handler.sendEmptyMessageDelayed(1, 5000);
	}
	
	public void updateList(Cursor cursor,boolean withCursor){
		Cursor oldCur = mAdapter.getCursor();
		if(withCursor){
			mAdapter.changeCursor(cursor);
			oldCur.close();
		}else{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        boolean withDefault = prefs.getBoolean(
	        		getString(R.string.alarm_predefined_key), true);
			if(withDefault){
				cursor = DBAdapter.instance().retrieveAll(Notification.TAG,
						NotificationFactory.ASDBY_TRIGGER_DATE, null);
			}else{			
				cursor = DBAdapter.instance().retrieveAll(Notification.TAG,
		        		 "where _id <> 1 "+NotificationFactory.ASDBY_TRIGGER_DATE, null );}
			mAdapter.changeCursor(cursor);
			oldCur.close();}
		
		mAdapter.notifyDataSetChanged();
	}
	 
    private OnHierarchyChangeListener mHierarchyChangelistener = new OnHierarchyChangeListener() {
		
		@Override
		public void onChildViewRemoved(View arg0, View child) {
			Object tag = child.getTag();
			if(tag == null) return;
			Integer notificationId = (Integer) child.getTag();
			if(notificationId == Global.ID_INVALID || mMode != MULTIPLE_CHOICE_MODE){
				return;}
			View item = child.findViewById(R.id.notificationItem);
			if(mMultipleArrayIds.contains(notificationId)){
				item.setBackgroundResource(R.drawable.list_item_orange_with_alarm);
			}else{
				Notification notification = NotificationFactory.load(notificationId);
				if(notification != null){
					mAdapter.turnOn(child, notification.getEnumStatus(), notification.getlTriggerDate());
				}else{
					Log.d("BabysitterActivity",
							"OnHierarchyChange ,but can't find Notification");}
			}
		}
		
		@Override
		public void onChildViewAdded(View parent, View child) {
			Object tag = child.getTag();
			if(tag == null) return;
			Integer notificationId = (Integer) child.getTag();
			if(notificationId == Global.ID_INVALID || mMode != MULTIPLE_CHOICE_MODE){
				return;}
			
			View item = child.findViewById(R.id.notificationItem);
			if(mMultipleArrayIds.contains(notificationId)){
				item.setBackgroundResource(R.drawable.list_item_orange_with_alarm);
			}else{
				Notification notification = NotificationFactory.load(notificationId);
				if(notification != null){
					mAdapter.turnOn(child, notification.getEnumStatus(), notification.getlTriggerDate());
				}else{
					Log.d("BabysitterActivity",
							"OnHierarchyChange ,but can't find Notification");}
			}
		}
	};
	
	public NotificationCursorAdapter getAdapter(){
		return mAdapter;
	}
}