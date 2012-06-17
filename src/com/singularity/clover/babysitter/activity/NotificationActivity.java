package com.singularity.clover.babysitter.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.R;
import com.singularity.clover.babysitter.notification.Notification;
import com.singularity.clover.babysitter.notification.Notification.Status;
import com.singularity.clover.babysitter.notification.NotificationFactory;
import com.singularity.clover.babysitter.widgets.DateSlider.DateSlider;
import com.singularity.clover.babysitter.widgets.DateSlider.DateTimeSlider;
import com.singularity.clover.babysitter.widgets.DateSlider.labeler.TimeLabeler;

public class NotificationActivity extends Activity {
	
	public static final String ACTION_NEW =
			"com.sinaularity.clover.babysitter.activity.NEW_NOTIFICATION";
	public static final String ACTION_EDIT=
			"com.singularity.clover.babysitter.activity.EDIT_NOTIFICATION";
	public static final String IN_NOTIFICATION_ID ="notification_id";
	
	public final static int OPENED_EDIT = 0;
	public final static int CLOSED_EDIT = 1;

	protected static final int TIMEDATE_DIALOG_ID = 0;	
	private Button mBtnTimeDate = null;
	private ToggleButton mBtnStatus = null;
	private Button mBtnSetOrSave = null;
	private Button mBtnSendOrCancel = null;
	private Notification mNotification = null;
	private EditText mEditDescription = null;
	private Button mAddDescription = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_notification_activity);

		mBtnTimeDate = (Button) findViewById(R.id.buttonTimeDate);
		mBtnStatus = (ToggleButton) findViewById(R.id.toggleButtonStatus);
		mBtnSetOrSave = (Button) findViewById(R.id.buttonSetOrSave);
		mBtnSendOrCancel = (Button) findViewById(R.id.buttonSendOrCancel);
		mEditDescription = (EditText) findViewById(R.id.editDescription);
		mAddDescription = (Button) findViewById(R.id.buttonAddDescription);
		Typeface typeface = Typeface.createFromAsset(getAssets(), Global.FONT_PATH);
		mBtnTimeDate.setTypeface(typeface);
		mBtnStatus.setTypeface(typeface);
		mBtnSetOrSave.setTypeface(typeface);
		mBtnSendOrCancel.setTypeface(typeface);
		mAddDescription.setTypeface(typeface);
		handleIntent(getIntent());
	}
	
	private void handleIntent(Intent intent){
		String action = intent.getAction();
		mBtnSendOrCancel.setTag(CLOSED_EDIT);
		mBtnSetOrSave.setTag(CLOSED_EDIT);
		if(action.equals(ACTION_NEW)){
			mBtnStatus.setChecked(true);
		}else if(action.equals(ACTION_EDIT)){
			int id = intent.getExtras().getInt(IN_NOTIFICATION_ID, Global.ID_INVALID);
			if(id != Global.ID_INVALID){
				mNotification = NotificationFactory.load(id);
				if(mNotification == null){
					Log.d("NotificationAcitivity","Id"+id+" isn't in table.");
					setResult(RESULT_CANCELED);
					finish();
				}
			}else{
				Log.d("NotificationAcitivity","can't get intent extra");
				setResult(RESULT_CANCELED);
				finish();
			}
			long triggerDate = mNotification.getlTriggerDate();
			Calendar selectedDate = Calendar.getInstance();
			selectedDate.setTimeInMillis(triggerDate);
			String content = "<big>"+DateFormat.format(
					"kk:mm",selectedDate).toString()+"</big>"+
					" <small>"+DateFormat.format("EE MMM d yyy",selectedDate).toString()+
					"</small>";
			mBtnTimeDate.setText(Html.fromHtml(content));
			
			mBtnStatus.setChecked(mNotification.getEnumStatus() == Notification.Status.ON);
			mBtnStatus.setEnabled(true);
			mAddDescription.setText(mNotification.getStrDescription());
			
			mBtnSetOrSave.setVisibility(View.GONE);
			mBtnSendOrCancel.setEnabled(true);
		}
	}

	public void onBtnTimeDateClick(View v) {
		showDialog(TIMEDATE_DIALOG_ID);
	}
	
	public void onBtnStatusClick(View v){
		ToggleButton btn = (ToggleButton) v;
		if(btn.isChecked()){
			mNotification.setEnumStatus(Status.ON);
		}else{
			mNotification.setEnumStatus(Status.OFF);
		}
		mNotification.update();
	}
	
	public void onBtnAddDescriptionClick(View v){
		mEditDescription.setVisibility(View.VISIBLE);
		mAddDescription.setEnabled(false);
		
		mBtnSendOrCancel.setEnabled(true);
		mBtnSendOrCancel.setText(R.string.cancel);
		mBtnSendOrCancel.setTag(OPENED_EDIT);
		
		mBtnSetOrSave.setEnabled(true);
		mBtnSetOrSave.setTag(OPENED_EDIT);
		
		mEditDescription.setText(mAddDescription.getText());
		
		String action = getIntent().getAction();
		if(action.equals(ACTION_NEW)){
			
		}else if(action.equals(ACTION_EDIT)){
			mBtnSetOrSave.setVisibility(View.VISIBLE);
		}
			
	}
	
	public void onBtnSetOrSaveClick(View v){
		int flag = (Integer) v.getTag();
		String action = getIntent().getAction();
		switch(flag){
		case OPENED_EDIT:
			Editable text = mEditDescription.getText();
			if(mNotification == null){
				mBtnSendOrCancel.setEnabled(false);
				mBtnSetOrSave.setEnabled(false);}
			mBtnSendOrCancel.setText(R.string.send);
			
			mEditDescription.clearFocus();
			mEditDescription.setVisibility(View.GONE);
			mAddDescription.setEnabled(true);
			mBtnSendOrCancel.setTag(CLOSED_EDIT);
			mBtnSetOrSave.setTag(CLOSED_EDIT);
			if(action.equals(ACTION_NEW)){
				String content = mEditDescription.getText().toString();
				int index = content.indexOf("|");
				if(index >= 0 && text != null){
					text.replace(index, index+1, "&");}
				if(text != null){
					if(text.equals("")){	
					}else{
						mAddDescription.setText(text);
					}
				}else{}
				
				if(mNotification != null){
					mNotification.setStrDescription(null);
					if(text != null){
						if(text.equals("")){	
						}else{
							mNotification.setStrDescription(text.toString());}
					}else{}
					if(mNotification.getlID() != Global.ID_INVALID)
						mNotification.update();}
				
			}else if(action.equals(ACTION_EDIT)){
				mBtnSetOrSave.setVisibility(View.GONE);
				String content = mEditDescription.getText().toString();
				int index = content.indexOf("|");
				if(index >= 0 && text != null){
					text.replace(index, index+1, "&");}
				if(text != null){
					mNotification.setStrDescription(text.toString());
					mAddDescription.setText(mNotification.getStrDescription());
				}else{
					mNotification.setStrDescription(null);
					mAddDescription.setText(mNotification.getStrDescription());}
				mNotification.update();
			}
			break;
		case CLOSED_EDIT:
			if(action.equals(ACTION_NEW)){
				CharSequence content = mAddDescription.getText();
				String des;
				if(content != null){
					des = content.toString();
					des = des.replace('|','&');
				}else{
					des = null;}
				
				if(des != null){
					mNotification.setStrDescription(des);
					if(des.equals("")){
						mAddDescription.setText(mNotification.getStrDescription());
					};
				}else{
					mNotification.setStrDescription(null);
					mAddDescription.setText(mNotification.getStrDescription());}
				mNotification.save();
				mBtnSetOrSave.setVisibility(View.GONE);
			}else if(action.equals(ACTION_EDIT)){
			}
			
			mBtnSendOrCancel.setEnabled(true);
			mBtnSetOrSave.setEnabled(false);
			break;	
		}

	}
	
	public void onBtnSendOrCancelClick(View v){
		int flag = (Integer) v.getTag();
		String action = getIntent().getAction();
		switch(flag){
		case OPENED_EDIT:
			if(mNotification == null){
				mBtnSendOrCancel.setEnabled(false);
				mBtnSetOrSave.setEnabled(false);}
			mBtnSendOrCancel.setText(R.string.send);
			mEditDescription.setVisibility(View.GONE);
			mAddDescription.setEnabled(true);
			mBtnSendOrCancel.setTag(CLOSED_EDIT);
			mBtnSetOrSave.setTag(CLOSED_EDIT);
			if(action.equals(ACTION_NEW)){
				if(mNotification != null)
					mBtnSetOrSave.setVisibility(View.GONE);
			}else if(action.equals(ACTION_EDIT)){
				mBtnSetOrSave.setVisibility(View.GONE);
			}
			break;
		case CLOSED_EDIT:
			NotificationFactory.instance().SendNotification(this, mNotification);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final Calendar calendar = Calendar.getInstance();
		if(mNotification != null){
			calendar.setTimeInMillis(mNotification.getlTriggerDate());}
		switch (id) {
		case TIMEDATE_DIALOG_ID:
			mDateTimeSlider = new DateTimeSlider(
					this, mDateTimeSetListener, calendar);
			return mDateTimeSlider;
		default:
			return null;
		}
	}

	private DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
		public void onDateSet(DateSlider view, Calendar selectedDate) {
			// update the dateText view with the corresponding date
			/*int minute = selectedDate.get(Calendar.MINUTE)
					/ TimeLabeler.MINUTEINTERVAL * TimeLabeler.MINUTEINTERVAL;*/
			String minute = DateFormat.format("mm", selectedDate).toString();
			String content = String.format(
					"<big>%tH:"+minute+"</big> <small>%ta %te. %tB</small>",
					selectedDate,selectedDate, selectedDate, selectedDate);
			mBtnTimeDate.setText(Html.fromHtml(content));
			
			String action = getIntent().getAction();		
			if(action.equals(ACTION_NEW)){
				if(mNotification == null){
					mNotification = NotificationFactory.create(selectedDate.getTimeInMillis());
				}else{
					mNotification.setlTriggerDate(selectedDate.getTimeInMillis());
					mNotification.update();}
				mBtnSetOrSave.setEnabled(true);
				mBtnStatus.setEnabled(true);
			}else if(action.equals(ACTION_EDIT)){
				mNotification.setlTriggerDate(selectedDate.getTimeInMillis());
				mNotification.update();
			}	
		}
	};
	private DateTimeSlider mDateTimeSlider;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		String action = getIntent().getAction();
		if(action.equals(ACTION_NEW)){
			
		}else if(action.equals(ACTION_EDIT)){
			}
	}
	
	public DateTimeSlider getDateSlider(){
		return mDateTimeSlider;
	}
	
	public Notification getNotification(){
		return mNotification;
	}
}
