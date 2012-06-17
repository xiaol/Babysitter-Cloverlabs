package com.singularity.clover.babysitter.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.singularity.clover.babysitter.BabysitterApplication;
import com.singularity.clover.babysitter.Global;
import com.singularity.clover.babysitter.R;
import com.singularity.clover.babysitter.activity.BabysitterActivity;
import com.singularity.clover.babysitter.activity.KeepMeAliveService;
import com.singularity.clover.babysitter.activity.WakeupActivity;
import com.singularity.clover.babysitter.drag.DragController;
import com.singularity.clover.babysitter.drag.DragLayer;
import com.singularity.clover.babysitter.drag.DragSource;
import com.singularity.clover.babysitter.drag.MyAbsoluteLayout;
import com.singularity.clover.babysitter.drag.MyAbsoluteLayout.LayoutParams;
import com.singularity.clover.babysitter.notification.Notification.Status;

public class NotificationManager implements NotificationFactory.Observer{
	public static String ACTION_ALARM_RING = 
			"com.singualrity.clover.babysitter.notification.RING";
	private static final long[] vibratePattern = {20000, 10000};
	
	private PendingIntent mPendingIntent = null;
	private Vibrator mVibrator = null;
	private AudioManager mAudioManager = null;
	private MediaPlayer mMediaPlayer = null;
	private Notification mNextNotification = null;
	private AlarmHandler mAlarmHandler = null;
	private float mSlipperX, mSlipperMinY,mSlipperMaxY;
	
	private int mAlarmVolumnLevel = 3;
	private final static int GENTLE_ALARM = 3;
	private int mVolumeIndex;
	private int mPreVolume;
	private int mLevelLength;
	//private DragLayer mAlertLayout;
	
	public NotificationManager(){
		mAlarmHandler = new AlarmHandler();
	}
	
	public void scheduleNotification(Notification notification){
		Context context = BabysitterApplication.instance().getApplicationContext();
		Intent intent = new Intent(context, NotificationReceiver.class);
		intent.setAction(ACTION_ALARM_RING);
		intent.putExtra(NotificationReceiver.IN_NOTIFICAION_ID, notification.getlID());
		
		mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, notification.getlTriggerDate(), mPendingIntent);
	}
	
	public void cancelNotification(PendingIntent pendingIntent){
		if (pendingIntent != null) {
			Context context = BabysitterApplication.instance().getApplicationContext();
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
		}
	}

	@Override
	public void handleNotificationChanged() {
		ArrayList<Notification> result = NotificationFactory.loadFromTable(
				"where "+ NotificationFactory.STATUS_ON +" "+
				"and " + NotificationFactory.AFTER_TIRGGER_DATE_X + System.currentTimeMillis() +" "+
				NotificationFactory.ASDBY_TRIGGER_DATE +" "+
				NotificationFactory.LIMIT_X+1, null);
		
		if(result.isEmpty()){
			cancelNotification(mPendingIntent);
			mNextNotification = null;
			Log.d("NotificationManager", "sheduled null");
		}else{
			Notification notification = result.get(0);
			if(!notification.isEqual(mNextNotification)){
				scheduleNotification(notification);
				mNextNotification = notification;
				Log.d("NotificationManager", "sheduled id "+notification.getlID());}
		}
		
	}	
	
	private void initializedInstrument(){
		Context context = BabysitterApplication.instance().getApplicationContext();
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mMediaPlayer = new MediaPlayer();
		
		Uri alert;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String uriString = prefs.getString(context
				.getString(R.string.ringtone_preference_key), RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
		alert = Uri.parse(uriString);
		if (alert == null) {
			// alert is null, using backup
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				// I can't see this ever being null
				// (as always have a default notification) but just incase
				// alert backup is null, using 2nd backup
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		
		try {
			mMediaPlayer.setDataSource(context, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {	
					mMediaPlayer.start();
				}
			});
			mPreVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
			int volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
			if(volume != 0){
				mVolumeIndex = volume/mAlarmVolumnLevel;
				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
						mVolumeIndex,0 );
				}
		} catch (IOException e) {
			e.printStackTrace();}
	}
	
	public void fireAlarm(){
		if(mMediaPlayer != null){
			if(mMediaPlayer.isPlaying()){
				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
						mVolumeIndex, 0);
				return;
			}else{
				synchronized(mMediaPlayer){
					if(mMediaPlayer != null){
						mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
								mVolumeIndex, 0);
						return;
					}
				}
			}
		}
		initializedInstrument();
		mVibrator.vibrate(vibratePattern, 0);
	}
	
	public void stopAlarm(){
		if(mMediaPlayer != null){
			synchronized(mMediaPlayer){
				mVibrator.cancel();
				mMediaPlayer.stop();
				releaseInstrument();}
		}
	}
	
	private void releaseInstrument(){
		mMediaPlayer.release();
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
				mPreVolume,0 );
		mMediaPlayer = null;
	}
	
	public Notification getNextNotification(){
		return mNextNotification;
	}
	
	public MediaPlayer getMediaPlayer(){
		return mMediaPlayer;
	}
	
	public void autoCancelAlarm(long duration,int what){
		mAlarmHandler.removeMessages(what);
		mAlarmHandler.sendEmptyMessageDelayed(what,duration);
		mLevelLength = (int) (duration/mAlarmVolumnLevel);
		mAlarmHandler.removeMessages(GENTLE_ALARM);
		mAlarmHandler.sendEmptyMessageDelayed(GENTLE_ALARM, mLevelLength);
	}
	
	private class AlarmHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NotificationReceiver.CANCEL_AUTOCANCEL:
				stopAlarm();
				mAlarmHandler.removeMessages(GENTLE_ALARM);
				if(Global.DEBUG_MODE){
					Object testCase = BabysitterApplication.instance().getTestCase();
					if(testCase != null){
						synchronized(testCase){
							testCase.notify();}
					}
				}
				break;
			case GENTLE_ALARM:
				mAlarmHandler.sendEmptyMessageDelayed(GENTLE_ALARM, mLevelLength);
				int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
				mAudioManager.setStreamVolume(AudioManager.STREAM_RING, 
						volume + mVolumeIndex, 0);
			default:
				break;
			}
			
		}
		
	}
	
	public void alertReceivedNotification(final Notification notification){
		final Context context = BabysitterApplication.instance().getApplicationContext();
		final View layout = View.inflate(
				context,R.layout.layout_received_alert,null);
		TextView textNotification = (TextView) layout.findViewById(R.id.textAlertContent);
		Button buttonActive = (Button) layout.findViewById(R.id.buttonAlertActive);
		Button buttonDelay = (Button) layout.findViewById(R.id.buttonAlertDelay);
		
		long triggerDate = notification.getlTriggerDate();
		Calendar selectedDate = Calendar.getInstance();
		selectedDate.setTimeInMillis(triggerDate);
		String time ="<big>"+DateFormat.format(
				"kk:mm",selectedDate).toString()+"</big>"+
				" <small>"+DateFormat.format("EE MMM d yyy",selectedDate).toString()+
				"</small>";
		String content = notification.getStrDescription() + 
				"&nbsp&nbsp;<small>"+
				String.format(context.getString(R.string.from_address), notification.getstrFromContacter())
				+"</small><br />"+time ;
		textNotification.setText(Html.fromHtml(content));
		
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp;
        int pixelFormat;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;

        int width = (int) (dm.widthPixels/7.0*6);
        int height = (int) (dm.heightPixels/7.0*3);
        
        pixelFormat = PixelFormat.TRANSLUCENT;
        lp = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS 
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/,
                pixelFormat);
//        lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.CENTER;

        wm.addView(layout, lp);
        
        buttonActive.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ContentValues contentValues = new ContentValues();
				notification.preUpdateStatus(Status.ON, contentValues);
				notification.update(contentValues);
				try{
					wm.removeView(layout);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
				}
				BabysitterApplication.instance().isListActivityAlive(true);
			}
		});
        
        buttonDelay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ContentValues contentValues = new ContentValues();
				notification.preUpdateStatus(Status.OFF, contentValues);
				notification.update(contentValues);
				try{
					wm.removeView(layout);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
				}
				BabysitterApplication.instance().isListActivityAlive(true);
			}
		});
	}
	
	
	protected void alert(long id){
		final Context context = BabysitterApplication.instance().getApplicationContext();
		Intent intent = new Intent(context, BabysitterActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		
		context.startService(new Intent(context, KeepMeAliveService.class));
		final Notification notification = NotificationFactory.load((int)id);
		final DragLayer layer = (DragLayer) View.inflate(
				context,R.layout.layout_notification_panel,null);
		
		final DragController controller = new DragController(
				BabysitterApplication.instance().getApplicationContext());
		layer.setDragController(controller);
		controller.addDropTarget(layer);
		final TextView textTimeDate = (TextView) layer.findViewById(R.id.alertPanelText);
		final ImageView slipper = (ImageView) layer.findViewById(R.id.slipper);
		final ImageView slipperTracker = (ImageView) layer.findViewById(R.id.slipperTracker);
		final TextView textIndicator1 = (TextView) layer.findViewById(R.id.textIndicator1);
		final TextView textIndicator2 = (TextView) layer.findViewById(R.id.textIndicator2);
		final TextView textIndicator3 = (TextView) layer.findViewById(R.id.textIndicator3);
		final ImageView indicator0 = (ImageView) layer.findViewById(R.id.indicator0);
		final ImageView indicator1 = (ImageView) layer.findViewById(R.id.indicator1);
		final ImageView indicator2 = (ImageView) layer.findViewById(R.id.indicator2);
		final ImageView indicator3 = (ImageView) layer.findViewById(R.id.indicator3);
		
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), Global.FONT_PATH);
		textTimeDate.setTypeface(typeface);
		textIndicator1.setTypeface(typeface);
		textIndicator2.setTypeface(typeface);
		textIndicator3.setTypeface(typeface);
		
		textTimeDate.setText(notification.getStrDescription());
		textIndicator1.setText(R.string.two_hours_later);
		textIndicator2.setText(R.string.six_hours_later);
		textIndicator3.setText(R.string.one_day_later);
		
		TextPaint textPaint = new TextPaint();
		textPaint.setTypeface(typeface);
		textPaint.setTextSize(textIndicator1.getTextSize());
		Rect rect = new Rect();
		textPaint.getTextBounds(textIndicator1.getText().toString(),
				0, textIndicator1.getText().length(), rect);
		rect.height();
		
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		WindowManager.LayoutParams lp;
        int pixelFormat;

        pixelFormat = PixelFormat.TRANSLUCENT;
        
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;

        int width = (int) (dm.widthPixels/7.0*4);
        int height = (int) (dm.heightPixels/7.0*5);
        MyAbsoluteLayout.LayoutParams lpSlipper = (LayoutParams) slipper.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpSlipperT = (LayoutParams) slipperTracker.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpTIndicator1 = 
        		(LayoutParams) textIndicator1.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpTIndicator2 = 
        		(LayoutParams) textIndicator2.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpTIndicator3 = 
        		(LayoutParams) textIndicator3.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpIndicator0 = 
        		(LayoutParams) indicator0.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpIndicator1 = 
        		(LayoutParams) indicator1.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpIndicator2 = 
        		(LayoutParams) indicator2.getLayoutParams();
        MyAbsoluteLayout.LayoutParams lpIndicator3 = 
        		(LayoutParams) indicator3.getLayoutParams();
        
        textTimeDate.setMaxWidth((int) (width/3.0f*2-20*density));
        textIndicator1.setMaxWidth((int) (width/3.0f*2-40*density));
        textIndicator2.setMaxWidth((int) (width/3.0f*2-40*density));
        textIndicator3.setMaxWidth((int) (width/3.0f*2-40*density));
        
        textTimeDate.setEllipsize(TruncateAt.END);
        mSlipperX =  (width/3.0f*2)-slipper.getDrawable().getIntrinsicWidth()/2;
        lpSlipper.x = (int) mSlipperX;
        lpSlipperT.x = (int) ((width/3.0f*2) -slipperTracker.getDrawable().getIntrinsicWidth()/2);
        mSlipperMinY = 10*density;
        mSlipperMaxY = height - 10*density;
        final float length = mSlipperMaxY-mSlipperMinY;
        lpSlipperT.height = (int) (length);
        lpSlipperT.y = (int) mSlipperMinY;
        lpTIndicator1.y = (int) (length/2 + mSlipperMinY-5*density)-rect.height();
        lpTIndicator2.y = (int) (length/4*3 + mSlipperMinY-5*density)-rect.height();
        lpTIndicator3.y = (int) (length + mSlipperMinY-5*density)-rect.height();
        
        lpIndicator0.y = (int) (length/4 + mSlipperMinY);
        lpIndicator0.width = (int) (width/3.0f*2-30*density);
        lpIndicator1.y = (int) (length/2 + mSlipperMinY);
        lpIndicator2.y = (int) (length/4*3 + mSlipperMinY);
        lpIndicator3.y = (int) (length + mSlipperMinY);
        

        lp = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                     WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS| 
                     WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED 
                    /*| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM*/,
                pixelFormat);
//        lp.token = mStatusBarView.getWindowToken();
        lp.gravity = Gravity.CENTER;

        wm.addView(layer, lp);
        final ContentValues contentValues = new ContentValues();
		controller.setDragListener(new DragController.DragListener() {
			
			private int[] levels = {2*60*60*1000,4*60*60*1000,18*60*60*1000};
			@Override
			public void onDraging(DragSource source, Object info, int[] coordinate) {
				int y = coordinate[1];
				layer.getLocationOnScreen(coordinate);
				coordinate[0] = (int) mSlipperX + coordinate[0]+
						slipper.getDrawable().getIntrinsicWidth()/2;
				int min = (int) (mSlipperMinY + coordinate[1]);
				int max = (int) (mSlipperMaxY+ coordinate[1]);
				if(y< min){
					coordinate[1] = min;
				}else if(y > max){
					coordinate[1] = max;
				}else{
					coordinate[1] = y;}
				
				float offset = coordinate[1] - min;
				long delayTime = 0;
				if(offset < length/4){
					
				}else if(offset < length/2){
					delayTime = (long) ((offset - length/4)/(length/4)*levels[0]);
				}else if(offset < length/4*3){
					delayTime = (long) ((offset - length/2)/(length/4)*levels[1]) + levels[0];
				}else{
					delayTime = (long) ((offset - length/4*3)/(length/4)*levels[2]) 
							+ levels[0]+ levels[1];
				}
				
				if(delayTime != 0){
					long triggerDate = notification.getlTriggerDate() + delayTime;
					Calendar date = Calendar.getInstance();
					date.setTimeInMillis(triggerDate);
					
					String content = "<big>"+DateFormat.format("kk:mm", date)+"</big>"+
							"<small>"+DateFormat.format("EE", date)+"</small><br />"
							+ "<small>"+DateFormat.format("MMM d", date) +"</small>";
					textTimeDate.setText(Html.fromHtml(content));
					
					notification.preUpdateTriggerDate(
							notification.lTriggerDate+delayTime,contentValues);
				}else{
					textTimeDate.setText(Html.fromHtml(context.getString(R.string.stop_alarm)));
				}
				
				
			}
			
			@Override
			public void onDragStart(DragSource source, Object info, int dragAction) {}
			
			@Override
			public void onDragEnd() {
				try{
					wm.removeView(layer);
				}catch(IllegalArgumentException e){
					e.printStackTrace();
				}
				if(contentValues.size() != 0){
					if(notification.getlID() == Global.SQLITE3_FISRT_ID){
						notification.preUpdateStatus(Status.ON, contentValues);}
					notification.update(contentValues);
				}else{
					if(notification.getlID() == Global.SQLITE3_FISRT_ID){
						notification.preUpdateStatus(Status.OFF, contentValues);
						notification.update(contentValues);}
				}
				BabysitterApplication.instance().isListActivityAlive(true);
			}
		});
		
		slipper.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent arg1) {
				int action = arg1.getAction();
				switch(action){
				case MotionEvent.ACTION_DOWN:
					controller.startDrag(v, layer, v, DragController.DRAG_ACTION_MOVE);
					mAlarmHandler.removeMessages(NotificationReceiver.CANCEL_AUTOCANCEL);
					mAlarmHandler.removeMessages(GENTLE_ALARM);
					stopAlarm();
				}
				return true;
			}
		});
		
	}
}
