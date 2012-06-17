package com.singularity.clover.babysitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

public class BabysitterPreference extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		RingtonePreference ringtonePre = (RingtonePreference) findPreference(
				getString(R.string.ringtone_preference_key));
		CheckBoxPreference checkAlarm = (CheckBoxPreference) findPreference(
				getString(R.string.alarm_predefined_key));
		final ListPreference snoozeList = (ListPreference) findPreference(
				getString(R.string.set_delay_time_key));
		Preference prefAbout = findPreference(getString(R.string.about_key));
		Boolean isActive = prefs.getBoolean(getString(R.string.alarm_predefined_key), true);
		snoozeList.setEnabled(isActive);
		snoozeList.setSummary(prefs.getString(getString(R.string.set_delay_time_key)+" "+
				getString(R.string.minutes),
				getResources().getStringArray(R.array.snooze_time_array)[0]));
		//ringtonePre.setSummary(prefs.getString(getString(R.string.ringtone_preference_key), null));
		
		checkAlarm.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Boolean isActive = (Boolean) newValue;
				snoozeList.setEnabled(isActive);
				setResult(RESULT_OK);
				return true;
			}
		});
		
		snoozeList.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary((CharSequence) newValue+" "+getString(R.string.minutes));
				return true;
			}
		});
		
		ringtonePre.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				/*Uri content = Uri.parse((String)arg1);
				String name = content.getLastPathSegment();
				arg0.setSummary(name);*/
				return true;
			}
		});
		
		prefAbout.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(getString(R.string.website)));
				startActivity(intent);
				return true;
			}
		});
		
	}
	
}
