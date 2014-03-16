package com.nexercise.client.android.activities;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nexercise.client.android.NexerciseApplication;
import com.nexercise.client.android.R;
import com.nexercise.client.android.adapters.SlideMenuAdapter;
import com.nexercise.client.android.base.BaseActivity;
import com.nexercise.client.android.components.HoloSeekBar;
import com.nexercise.client.android.components.HoloSeekBar.OnHoloSeekBarChangeListener;
import com.nexercise.client.android.components.RangeSeekBar;
import com.nexercise.client.android.components.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.nexercise.client.android.constants.APIConstants;
import com.nexercise.client.android.constants.CacheConstants;
import com.nexercise.client.android.constants.DisplayConstants;
import com.nexercise.client.android.constants.MenuConstants;
import com.nexercise.client.android.constants.MessagesConstants;
import com.nexercise.client.android.constants.RewardConstants;
import com.nexercise.client.android.constants.UserPreferencesConstants;
import com.nexercise.client.android.entities.EndLocation;
import com.nexercise.client.android.entities.Exercise;
import com.nexercise.client.android.entities.ExerciseSession;
import com.nexercise.client.android.entities.ExerciseSessionEvent;
import com.nexercise.client.android.entities.NXRMenuItem;
import com.nexercise.client.android.entities.UserInfo;
import com.nexercise.client.android.entities.WeatherResponse;
import com.nexercise.client.android.helpers.FlurryHelper;
import com.nexercise.client.android.helpers.LocationHelper;
import com.nexercise.client.android.helpers.NxrActionBarMenuHelper;
import com.nexercise.client.android.helpers.PreferenceHelper;
import com.nexercise.client.android.helpers.WebServiceHelper;
import com.nexercise.client.android.model.DataLayer;
import com.nexercise.client.android.model.Factory;
import com.nexercise.client.android.model.Model;
import com.nexercise.client.android.task.WeatherTask;
import com.nexercise.client.android.utils.Funcs;

public class SelfReportActivity extends BaseActivity implements OnClickListener {

	Button btnSubmitButton;
	//Button btnDiscardActivity;
	Button back;
	Button btnCancel;
	Exercise exercise;
	public int hours = 0;
	public int mins = 0;
	public Date timeStarted;
	private int distanceTens = 0;
	private int distanceDecimals = 0;
	private long distanceInMeters = 0;
	String timeStartededDisplayText;
	String exerciseTitle;
	String exerciseDisplayTitle;
	float exerciseAccelarationThreshold;
	int exerciseInActivityGracePeriod;
	String UUID = "";
	LocationHelper locHelper;
	final int TIME_STARTED_DIALOG = 11;
	final int TIME_EXERCISED_DIALOG = 22;
	final int DISTANCE_EXERCISED_DIALOG = 33;
	final int EXERCISE_NAME_DIALOG = 44;
	public static final String HOURS = "hours";
	public static final String MINS = "mins";
	public static final String DISPLAY_TEXT_TIME_FINISH = "display";
	public static final String DATE_IN_MILLIS = "date";
	public static final String DISTANCE_IN_METERS = "distanceInMeters";
	public static final String IS_DISTANCE_BASED = "isDistance";
	public static final String DISTANCE_TENS = "distanceTens";
	public static final String DISTANCE_DECIMALS = "distanceDecimals";
	public static final String EXERCISE_NAME = "exerciseName";
	public static final String EXERCISE_DISPLAY_NAME = "ExerciseDisplayName";
	public static final String EXERCISE_DP = "ExerciseDP"; 
	public static final String EXERCISE_AT =  "ExerciseAT";
	public static final String EXERCISE_IS_DISTANCE_BASED = "ExerciseIsDistanceBased";
	int startVal, endVal, distanceVal;
	private boolean showDistanceInMiles = true;
	UserInfo userInfo;
	TextView startTimeValueText, endTimeValueText, durationValueText, txtDistanceValue, txtActivityName;
	int selectedDay, selectedMonth;
	RangeSeekBar<Integer> seekBar; 
	HoloSeekBar<Integer> seekBarHolo ;
	static int selectedDateIndex = 7;
	static int selectedTimeIndex = 0;
	HorizontalScrollView calendarScroller;
	HorizontalScrollView timeHoursScroller;
	LinearLayout dateScroller;
	LinearLayout timeScroller;
	private boolean showDistanceToast = false;
	/**  Navigation Drawer menu  changes  starts*/
	SlideMenuAdapter mCustomMenuAdapter;
	List<NXRMenuItem> mCustomMenuList;
	private ListView mDrawerListView;
	private DrawerLayout mDrawerLayout;
	private NxrActionBarMenuHelper mActionBarHelper;
	private Menu mOptionsMenu;
	private boolean isActivityNameDialogShown = false;
	/**  Navigation Drawer menu  changes  ends*/
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//	requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.self_report_activity);

		btnSubmitButton = (Button) findViewById(R.id.btnSubmit);

		btnSubmitButton.setOnClickListener(this);

		exerciseTitle = this.getIntent().getStringExtra("ExerciseName");
		checkUnitOfDistance();

		selectedDay = this.getIntent().getIntExtra("SelectedDay",1);
		selectedMonth = this.getIntent().getIntExtra("SelectedMonth",0);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, selectedMonth);  
		c.set(Calendar.DAY_OF_MONTH, selectedDay);  
		c.add(Calendar.HOUR, -hours);
		c.add(Calendar.MINUTE, -15);

		timeStarted = c.getTime();
		exercise = getDataLayer().searchExerciseType(exerciseTitle);
		mins = 15;

		Calendar today = Calendar.getInstance();

		//----------------------set date-------
		selectedDateIndex = getSelectedDateIndex(c.get(Calendar.DAY_OF_YEAR), today.get(Calendar.DAY_OF_YEAR));
		initDateScroller(selectedDateIndex, true);	

		//------------Range Seekbar
		startTimeValueText = (TextView)findViewById(R.id.startTimeValueText);
		endTimeValueText = (TextView)findViewById(R.id.endTimeValueText);
		durationValueText = (TextView)findViewById(R.id.durationValueText);
		txtDistanceValue = (TextView)findViewById(R.id.txtDistanceValue);
		txtActivityName = (TextView)findViewById(R.id.txtActivityName);

		seekBar = new RangeSeekBar<Integer>(0, 1440, SelfReportActivity.this);
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
				// handle changed range values
				if(!FlurryHelper.isSessionActive()){
					FlurryHelper.startSession(SelfReportActivity.this);
				}
				Map<String, String> flurryParams = new HashMap<String, String>();
				flurryParams.put("via", "slider");
				FlurryHelper.logEvent("A:SelfReport.SetStart", flurryParams);
				FlurryHelper.logEvent("A:SelfReport.SetLength", flurryParams);
				if((maxValue - minValue ) > 14 ){
					startVal = minValue;
					endVal = maxValue;
					startTimeValueText.setText( "" + convertToTime(minValue));
					endTimeValueText.setText("" + convertToTime(maxValue));
					durationValueText.setText( ""+ convertToTotalTime((maxValue - minValue )));
				}
				else{// reset time if duration less than 15
					startVal = minValue;
					endVal = startVal + 15;
					hours =0;
					mins =15;				
					seekBar.setSelectedMaxValue(endVal);
					seekBar.setSelectedMinValue(startVal);
				}
			}
		});
		seekBar.setNotifyWhileDragging(true);
		// add RangeSeekBar to pre-defined layout

		ViewGroup layout = (ViewGroup) findViewById(R.id.linearSeekBar);
		layout.addView(seekBar);

		Calendar calendarSelected = Calendar.getInstance();
		long minsDefault=this.getIntent().getLongExtra(SelfReportActivity.DATE_IN_MILLIS, -1);
		Date d = new Date();
		d.setTime(minsDefault);
		calendarSelected = Calendar.getInstance();
		calendarSelected.setTime(d);
		startVal = c.get(Calendar.HOUR)* 60 +c.get(Calendar.MINUTE);
		if(c.get(Calendar.AM_PM)== 1)
			startVal=startVal + 12 * 60;

		endVal = startVal + 15;

		seekBar.setSelectedMaxValue(endVal);
		seekBar.setSelectedMinValue(startVal);

		int index = 0;
		if(c.get(Calendar.AM_PM)== 0){
			if(c.get(Calendar.HOUR) == 12)
				index = 0;
			else
				index = c.get(Calendar.HOUR);
		}
		else{
			//if(c.get(Calendar.HOUR) < 9)
			index = c.get(Calendar.HOUR) + 12;
			//else
			//index = 14;
		}
		selectedTimeIndex = index;
		setTimeScroller(index,false);


		startTimeValueText.setText("" + convertToTime(startVal) );
		endTimeValueText.setText("" + convertToTime(endVal));
		durationValueText.setText( ""+ convertToTotalTime(15));

		//----------------------------------------
		if (isDistanceBased() ){
			initializeDistanceSeekbar();

		}
		// Click listener wheel picker
		durationValueText.setOnClickListener(this);
		startTimeValueText.setOnClickListener(this);
		txtActivityName.setOnClickListener(this);

		exerciseAccelarationThreshold = this.getIntent().getFloatExtra(
				"ExerciseAT", 0f); 
		exerciseInActivityGracePeriod = this.getIntent().getIntExtra(
				"ExerciseDP", 0);
		exerciseDisplayTitle = this.getIntent().getStringExtra(
				"ExerciseDisplayName");

		loadValues();
		
	}

	private void initializeDistanceSeekbar(){
		if ( showDistanceInMiles ) 
			seekBarHolo = new HoloSeekBar<Integer>(0, 100, SelfReportActivity.this);
		else
			seekBarHolo = new HoloSeekBar<Integer>(0, 200, SelfReportActivity.this);

		seekBarHolo.setSelectedMaxValue(0);
		seekBarHolo.setOnHoloSeekBarChangeListener(new OnHoloSeekBarChangeListener<Integer>() {

			@Override
			public void onRangeSeekBarValuesChanged(HoloSeekBar<?> bar,
					Integer minValue, Integer maxValue) {
				// TODO Auto-generated method stub
				if(!FlurryHelper.isSessionActive()){
					FlurryHelper.startSession(SelfReportActivity.this);
				}
				Map<String, String> flurryParams = new HashMap<String, String>();
				flurryParams.put("via", "slider");
				FlurryHelper.logEvent("A:SelfReport.SetDistance", flurryParams);
				if(maxValue == 0){
					txtDistanceValue.setText("N/A");
				}
				else{
					if ( showDistanceInMiles ){ 
						if(maxValue >= 90){
							if(!showDistanceToast){
								showDistanceToast = true;
								showDistanceToast();
							}
						}							
						else{
							showDistanceToast = false;
						}
						txtDistanceValue.setText("" + (maxValue/10d) + " miles");
					}
					else
					{
						if(maxValue >= 190)
						{
							if(!showDistanceToast){
								showDistanceToast = true;
								showDistanceToast();
							}
						}
						else{
							showDistanceToast = false;
						}
						txtDistanceValue.setText("" + (maxValue/10d) + " km");
					}
					distanceVal = maxValue;
					distanceTens = distanceVal / 10;
					double distance= (distanceVal / 10d);
					distanceDecimals = (int) ((distance - Math.floor(distance)) * 10);
				}
			}
		});
		seekBarHolo.setNotifyWhileDragging(true);
		// add RangeSeekBar to pre-defined layout
		ViewGroup linearSeekBarHolo = (ViewGroup) findViewById(R.id.linearSeekBarHolo);
		linearSeekBarHolo.removeAllViews();
		linearSeekBarHolo.addView(seekBarHolo);
		txtDistanceValue.setText("N/A");	

		txtDistanceValue.setOnClickListener(this);
	}

	private void showDistanceToast(){
		if(showDistanceToast){ // To avoid showing toast multiple times.
			Funcs.showVeryLongToastOnTop(MessagesConstants.SELF_REPORT_DISTANCE_MORE, SelfReportActivity.this, seekBar);
		}
	}

	private boolean isDistanceBased(){
		boolean isDistanceBased =false;
		try {
			if(exercise.distanceBased)
				isDistanceBased =true;
		} catch (Exception e) {
		}
		return isDistanceBased;
	}

	private  void  setStartTime(){
		int amPm;
		int hour, min;
		if(startVal >= 60 )
			hour = startVal / 60;
		else
			hour =0;

		if(startVal % 60 > 0)
			min = startVal % 60;
		else
			min = 0;
		if(hour > 12){
			hour = hour-12;
			if(hour == 12)
				amPm = 0;
			else
				amPm = 1;
		}
		else {
			if(hour == 12)
				amPm = 1;
			else
				amPm = 0;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, selectedMonth);  
		calendar.set(Calendar.DAY_OF_MONTH, selectedDay); 
		calendar.set(Calendar.AM_PM, amPm);
		int hourVal = hour == 12?0 : hour;
		calendar.set(Calendar.HOUR, hourVal);
		calendar.set(Calendar.MINUTE, min);
		calendar.set(Calendar.MILLISECOND , 0);
		timeStarted = new Date(calendar.getTimeInMillis());
	}

	private boolean isToday(){
		if(selectedDateIndex == 7 )
			return true;
		else
			return false;
	}


	private boolean isStartTimeFutureValue() {
		if(isToday()){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(timeStarted);

			Calendar maxCalendar = Calendar.getInstance();
			maxCalendar.add(Calendar.HOUR, -hours);
			maxCalendar.add(Calendar.MINUTE, -mins);

			if( calendar.getTimeInMillis() >maxCalendar.getTimeInMillis()){	
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	private long getCurrentDistance( ) {

		float meters = distanceTens + (distanceDecimals / 10f);
		if ( showDistanceInMiles ) {
			return (long) Math.ceil(meters / 0.000621371192d);
		}
		else {
			return (long) Math.ceil(meters / 0.001d);
		}
	}

	private String convertToTime(int value){
		String timeValue = "";
		String amPmVal;
		int hour, min;
		if(value >= 60 )
			hour = value / 60;
		else
			hour =0;

		if(value % 60 > 0)
			min = value % 60;
		else
			min = 0;
		if(hour > 12){
			hour = hour-12;
			if(hour == 12)
				amPmVal = "am";
			else
				amPmVal = "pm";
			if(hour > 12){// if duration or start time adjusted
				hour = hour-12;
				amPmVal = "am";
			}
		}
		else {
			if(hour == 12)
				amPmVal = "pm";
			else
				amPmVal = "am";
		}
		String minString,hourString;
		if(min<10)
			minString = "0" + min;
		else
			minString = ""+ min;

		if(hour == 0)
			hourString= "12";
		else
			hourString = ""+ hour;

		timeValue = hourString + ":" + minString + amPmVal;
		return timeValue;
	}

	private String convertToTotalTime(int value){
		String timeValue = "";
		int hour, min;
		if(value >= 60 )
			hour = value / 60;
		else
			hour =0;

		if(value % 60 > 0)
			min = value % 60;
		else
			min = 0;
		String minString,hourString;

		minString = ""+ min;

		hourString = ""+ hour;

		mins= min;
		hours =hour;
		if(hour == 0)
			timeValue = minString + "min";
		else if(min == 0)
			timeValue = hourString + "hr";
		else
			timeValue = hourString + "hr"+ " " + minString + "min";
		return timeValue;
	}

	@Override
	public void onStart() {
		super.onStart();
		//PocketChange.initialize(this, PocketChangeConstants.APP_ID,
		//		PocketChangeConstants.TEST_MODE); // Google Play
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			initDateScroller(selectedDateIndex, false);
			setTimeScroller(selectedTimeIndex, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}

		if(!isActivityNameDialogShown){
			isActivityNameDialogShown = true;
			exerciseNameDialog();
		}

		FlurryHelper.startSession(this);
		Map<String, String> flurryParams = new HashMap<String, String>();
		flurryParams.put("activity", exerciseTitle);
		FlurryHelper.logEvent("V:Nxr.SelfReport", flurryParams);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		FlurryHelper.endSession(this);
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	private void loadValues() {
		String timeValue = "";
		if(hours == 0)
			timeValue = mins + "min";
		else if(mins == 0)
			timeValue = hours + "hr";
		else
			timeValue = hours + "hr"+ " " + mins + "min";
		durationValueText.setText(timeValue);		
		startTimeValueText.setText( "" + convertToTime(startVal));
		endTimeValueText.setText("" + convertToTime(endVal));

		if(seekBar.getSelectedMinValue() < startVal){
			seekBar.setSelectedMaxValue(endVal);
			seekBar.setSelectedMinValue(startVal);
		}
		else{
			seekBar.setSelectedMinValue(startVal);
			seekBar.setSelectedMaxValue(endVal);
		}

		if(isDistanceBased()) {

			if((distanceTens == 0)&&(distanceDecimals == 0))
				txtDistanceValue.setText("N/A");
			else if ( showDistanceInMiles ) 
				txtDistanceValue.setText("" + distanceTens +"."+ distanceDecimals + " miles");
			else
				txtDistanceValue.setText("" + distanceTens +"."+ distanceDecimals + " km");
			seekBarHolo.setSelectedMaxValue((distanceTens * 10) + distanceDecimals);
		}

		calculateTimeFinishedString();



		TextView txtActivityName = (TextView) findViewById(R.id.txtActivityName);
		txtActivityName.setText( exerciseDisplayTitle + " ");

		/**  Navigation Drawer menu  changes  starts*/
		initNavigationDrawerMenu();
		/**  Navigation Drawer menu  changes  ends*/
		UUID = PreferenceHelper.getStringPreference(this,
				UserPreferencesConstants.USER_PREFERENCES,
				UserPreferencesConstants.USER_UUID, "");
		locHelper = new LocationHelper(SelfReportActivity.this);
		userInfo = getDataLayer().getUserInfo();

		try {
			if (exercise.distanceBased != null) {

				if (!exercise.distanceBased ){
					LinearLayout distanceBlock = (LinearLayout) findViewById(R.id.distanceBlock);
					distanceBlock.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
		}
	}

	private void checkUnitOfDistance() {

		String unit = PreferenceHelper.getStringPreference(this,
				DisplayConstants.PREF_NAME, DisplayConstants.PREF_KEY_DISTANCE,
				DisplayConstants.PREF_VALUE_ENGLISH);
		if (unit.contentEquals(DisplayConstants.PREF_VALUE_ENGLISH)) {
			showDistanceInMiles = true;
		} else if (unit.contentEquals(DisplayConstants.PREF_VALUE_METRIC)) {
			showDistanceInMiles = false;
		}
	}

	private void calculateTimeFinishedString() {

		if (timeStarted != null) {

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(timeStarted);

			String hourString;
			if(calendar.get(Calendar.HOUR) == 0)
				hourString = "12";
			else{
				if(calendar.get(Calendar.HOUR) < 10)
					hourString = "0"+calendar.get(Calendar.HOUR);
				else
					hourString = "" + calendar.get(Calendar.HOUR);
			}

			String minuteString;
			if(calendar.get(Calendar.MINUTE) < 10)
				minuteString = "0"+calendar.get(Calendar.MINUTE);
			else
				minuteString = ""+calendar.get(Calendar.MINUTE);

			String am_pm;
			if(calendar.get(Calendar.AM_PM) == 0)
				am_pm = "am";
			else
				am_pm = "pm";

			timeStartededDisplayText = hourString + ":" + minuteString + am_pm;
		}
	}

	public void exerciseNameDialog(){
		Intent intent = new Intent(SelfReportActivity.this,
				ExerciseListPopUpActivity.class);
		startActivityForResult(intent, EXERCISE_NAME_DIALOG);	
	}

	public void totalTimeExercisedDialog() {

		Intent intent = new Intent(SelfReportActivity.this,
				SelfReportTotalTimeActivity.class);
		try {
			if (exercise.distanceBased != null) { 
				intent.putExtra(IS_DISTANCE_BASED, false);
				intent.putExtra(HOURS, hours);
				intent.putExtra(MINS, mins);
				intent.putExtra(DISTANCE_TENS, distanceTens);
				intent.putExtra(DISTANCE_DECIMALS, distanceDecimals);
			} else {
				intent.putExtra(IS_DISTANCE_BASED, false);
			}
		} catch (Exception e) {

		}
		startActivityForResult(intent, TIME_EXERCISED_DIALOG);
	}

	public void timeStartedDialog() {

		Intent intent = new Intent(SelfReportActivity.this,
				SelfReportStartTimeActivity.class);
		intent.putExtra(DATE_IN_MILLIS, timeStarted.getTime());
		intent.putExtra(HOURS, hours);
		intent.putExtra(MINS, mins);
		startActivityForResult(intent, TIME_STARTED_DIALOG);
	}

	public void distanceExercisedDialog() {

		Intent intent = new Intent(SelfReportActivity.this,
				SelfReportDistanceActivity.class);
		intent.putExtra(IS_DISTANCE_BASED, true);
		intent.putExtra("distanceTens", distanceTens);
		intent.putExtra("distanceDecimals", distanceDecimals);
		intent.putExtra("motionTracked", false);

		startActivityForResult(intent, DISTANCE_EXERCISED_DIALOG);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == TIME_EXERCISED_DIALOG) {
				hours = data.getIntExtra(HOURS, 0);
				mins = data.getIntExtra(MINS, 0);

				endVal = startVal + hours * 60 + mins ;				

				Calendar maxCalendar = Calendar.getInstance();
				// For today
				if(selectedDay == maxCalendar.get(Calendar.DAY_OF_MONTH)){
					maxCalendar.add(Calendar.HOUR, -hours);
					maxCalendar.add(Calendar.MINUTE, -mins);

					Calendar calendarSelected = Calendar.getInstance();
					calendarSelected.setTime(timeStarted);

					if(calendarSelected.getTimeInMillis() > maxCalendar.getTimeInMillis())
						timeStarted = maxCalendar.getTime();
				}

			} else if (requestCode == TIME_STARTED_DIALOG) {
				timeStarted = new Date(data.getLongExtra(DATE_IN_MILLIS,
						Calendar.getInstance().getTimeInMillis()));
				timeStartededDisplayText = data
						.getStringExtra(DISPLAY_TEXT_TIME_FINISH);

				Calendar maxCalendar = Calendar.getInstance();
				if(selectedDay == maxCalendar.get(Calendar.DAY_OF_MONTH)){
					maxCalendar.add(Calendar.HOUR, -hours);
					maxCalendar.add(Calendar.MINUTE, -mins);

					Calendar calendarSelected = Calendar.getInstance();
					calendarSelected.setTime(timeStarted);

					if(calendarSelected.getTimeInMillis() > maxCalendar.getTimeInMillis())
						timeStarted = maxCalendar.getTime();
				}


				Calendar calendarSelected = Calendar.getInstance();
				calendarSelected.setTime(timeStarted);
				startVal = calendarSelected.get(Calendar.HOUR)* 60 +calendarSelected.get(Calendar.MINUTE);
				if(calendarSelected.get(Calendar.AM_PM)== 1)
					startVal=startVal + 12 * 60;

				endVal = startVal + hours * 60 + mins;

				//---- time scroller--------------
				int index = 0;
				if(calendarSelected.get(Calendar.AM_PM)== 0){
					if(calendarSelected.get(Calendar.HOUR) == 12)
						index = 0;
					else
						index = calendarSelected.get(Calendar.HOUR);
				}
				else{
					index = calendarSelected.get(Calendar.HOUR) + 12;
				}
				selectedTimeIndex = index;
				setTimeScroller(index,false);
			} else if (requestCode == DISTANCE_EXERCISED_DIALOG) {
				distanceTens = data.getIntExtra(DISTANCE_TENS, 0);
				distanceDecimals = data.getIntExtra(DISTANCE_DECIMALS, 0);
				distanceInMeters = data.getLongExtra(DISTANCE_IN_METERS, 0);
				checkUnitOfDistance();// check whether unit changed using wheel picker
				initializeDistanceSeekbar();
			}
			else if(requestCode == EXERCISE_NAME_DIALOG){
				exerciseTitle = data.getStringExtra( EXERCISE_NAME );
				exerciseDisplayTitle = data.getStringExtra( EXERCISE_DISPLAY_NAME );
				exerciseAccelarationThreshold = data.getFloatExtra( EXERCISE_AT, 0f );
				exerciseInActivityGracePeriod = data.getIntExtra( EXERCISE_DP , 0);

				exercise = getDataLayer().searchExerciseType(exerciseTitle);
				TextView txtActivityName = (TextView) findViewById(R.id.txtActivityName);
				txtActivityName.setText( exerciseDisplayTitle + " ");
				//----------------------------------------
				boolean isDistanceBased = data.getBooleanExtra(EXERCISE_IS_DISTANCE_BASED, false);
				if (isDistanceBased ){
					LinearLayout distanceBlock = (LinearLayout) findViewById(R.id.distanceBlock);
					distanceBlock.setVisibility(View.VISIBLE);
					initializeDistanceSeekbar();
				}
				else{
					LinearLayout distanceBlock = (LinearLayout) findViewById(R.id.distanceBlock);
					distanceBlock.setVisibility(View.GONE);
				}			 

				if(!PreferenceHelper.getBooleanPreference(this, DisplayConstants.PREF_NAME, DisplayConstants.PREF_KEY_WATCH_SELF_REPORT_TIP)){
					savePreference(DisplayConstants.PREF_NAME,
							DisplayConstants.PREF_KEY_WATCH_SELF_REPORT_TIP,
							true);
					Intent tipIntent = new Intent(
							com.nexercise.client.android.activities.SelfReportActivity.this,
							SelfReportTipOverlayActivity.class);
					tipIntent.putExtra("activityName", exerciseDisplayTitle);
					startActivity(tipIntent);
				}

			}
		}

		loadValues();
	}
	
	private void savePreference(String prefsName, String key, boolean value) {

		PreferenceHelper.putBooleanPreference(this, prefsName, key, value);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.durationValueText:
			totalTimeExercisedDialog();
			break;
		case R.id.txtDistanceValue:
			distanceExercisedDialog();
			break;
		case R.id.startTimeValueText:
			setStartTime();
			timeStartedDialog();
			break;
		case R.id.txtActivityName:
			exerciseNameDialog();
			break;
		case R.id.btnSubmit:
			distanceInMeters = getCurrentDistance();
			setStartTime();

			if(isStartTimeFutureValue())
				showFutureTimeAlert();
			else if ((hours == 0 && mins == 0))
				Funcs.showShortToast(
						MessagesConstants.ERROR_CANNOT_SUBMIT_INCOMPLETE,
						SelfReportActivity.this);			
			else if (Funcs.isInternetReachable(SelfReportActivity.this))
				new SubmitExerciseAsyncTask(this).execute("");
			else
				Funcs.showShortToast(
						MessagesConstants.ERROR_INTERNET_NOT_FOUND,
						SelfReportActivity.this);
			break;

		default:
			break;
		}
	}

	private void showFutureTimeAlert(){
		String message = MessagesConstants.WHEEL_PICKER_TIME_FUTURE_MESSAGE ;
		AlertDialog.Builder builder = new AlertDialog.Builder(
				SelfReportActivity.this);
		builder.setMessage(message)
		.setCancelable(true)
		.setPositiveButton(MessagesConstants.OK,
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,
					int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(true);
		alert.show();
	}

	@SuppressWarnings("unused")
	public class SubmitExerciseAsyncTask extends AsyncTask<String, Integer, Void> {

		Context _context;
		Model _dataModel;
		ProgressBar progressBarHorizontal;
		TextView txtProgressInfo;
		Dialog customProgress;
		String progressMessage = "";
		HashMap<String, Object> _winnings;
		String errorCode;

		public SubmitExerciseAsyncTask(Context context) {

			_context = context;
			_dataModel = new Model(_context);
			try {
				FlurryHelper.startSession(SelfReportActivity.this);
				customProgress = Funcs.getCustomProgressDialog(SelfReportActivity.this);
				txtProgressInfo = (TextView) customProgress.findViewById(R.id.txtProgressInfo);
				progressBarHorizontal = (ProgressBar) customProgress.findViewById(R.id.progressBarHorizontal);
				customProgress.show();
			} catch (Exception e) {
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			progressMessage = MessagesConstants.EXERCISE_SUBMIT_PROGRESS_STEP_1;
			publishProgress(20);
			ExerciseSession session = new ExerciseSession();
			session.exerciseActivity = exerciseTitle;
			try {
				if (exercise.distanceBased && distanceInMeters > 0){
					session.distanceInMeters = (int) distanceInMeters;
				}
			} catch (Exception e) {
				//session.distanceInMeters = 0;
			}
			
			// Check start year in case loging activity of Dec on Jan
			setStartYear();
			
			session.stepCount = 0;
			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(timeStarted);
			endCalendar.add(Calendar.HOUR_OF_DAY, +hours);
			endCalendar.add(Calendar.MINUTE, +mins);
			session.endTime = Funcs.getDateInGroovyFormat(endCalendar.getTime());
			session.startTime = Funcs.getDateInGroovyFormat(timeStarted);						
			session.metricsFile = "";
			session.isSelfReported = 1;
			session.secondsExercised = hours * 60 * 60 + mins * 60;
			session.secondsWarped = 0;
			session.startEnergy = 100;
			session.stepCount = 0;
			session.id = Funcs.getUUID();

			ExerciseSessionEvent exEvent = new ExerciseSessionEvent();
			exEvent.duration = 0;
			exEvent.name = "start";
			exEvent.time = Funcs.getDateInGroovyFormat(timeStarted);
			
			session.events.add(exEvent);

			exEvent = new ExerciseSessionEvent();
			exEvent.duration = 0;
			exEvent.name = "stop";
			exEvent.time = Funcs.getDateInGroovyFormat(endCalendar.getTime());
			
			session.events.add(exEvent);

			exEvent = new ExerciseSessionEvent();
			exEvent.duration = 0;
			exEvent.name = "inactivity";
			exEvent.time = Funcs.getDateInGroovyFormat(timeStarted);

			session.events.add(exEvent);

			Location loc = locHelper.getLastKnownLocation();
			session.endLocation = new EndLocation();
			session.endLocation.id = Funcs.getUUID();
			session.endLocation.radius = 0;

			if (loc != null
					&& timeStarted.getTime() + (31 * 60 * 1000) > System
					.currentTimeMillis()) {
				session.endLocation.latitude = loc.getLatitude();
				session.endLocation.longitude = loc.getLongitude();
				WeatherResponse weather = (new WeatherTask(loc.getLatitude(),
						loc.getLongitude(), SelfReportActivity.this, null))
						.doInBackground();
				if (weather != null) {
					ExerciseSessionEvent weatherEvent = new ExerciseSessionEvent();
					exEvent.name = "weather";
					exEvent.temp = weather.getTemperature();
					exEvent.condition = weather.getCondition();
					exEvent.time = Funcs.getCurrentDateTime();
					session.events.add(exEvent);
				}

			} else {
				session.endLocation.latitude = 0d;
				session.endLocation.longitude = 0d;

			}
			progressMessage = MessagesConstants.EXERCISE_SUBMIT_PROGRESS_STEP_2;
			publishProgress(40);
			HashMap<String, Object> responseHash = _dataModel
					.submitExerciseSession(session, UUID);
			progressMessage = MessagesConstants.EXERCISE_SUBMIT_PROGRESS_STEP_3;
			publishProgress(80);
			if (responseHash != null && responseHash.size() > 0) {
				Map<String, String> flurryParams = new HashMap<String, String>();
				flurryParams.put("activity", session.exerciseActivity);
				flurryParams.put("name", userInfo.userID);
				flurryParams.put("secondsExercised",
						String.valueOf(session.secondsExercised));
				if (responseHash.containsKey("serverError")) {
					flurryParams.put("submissionStatus", "NO");
					errorCode = responseHash.get("serverError").toString();
				} else {
					flurryParams.put("submissionStatus", "Yes");
					_winnings = responseHash;
					Log.e("Winnings", "Data Sent to Nexercise " + _winnings);

					if (_winnings != null) {
						//if (PreferenceHelper.getBooleanPreference(
						//		SelfReportActivity.this,
						//		DisplayConstants.PREF_NAME,
						//		DisplayConstants.PREF_KEY_POCKET_CHANGE)) {
						//	PocketChange.grantReward(PocketChangeConstants.LOG_PHYSICAL__ACTIVITIES,1);

						//}
						CacheConstants.makeUserCacheInvaid();
						getDataLayer().deleteLastEightDaysOfExerciseActivity();
						int secondsElapsed = hours * 60 * 60 + mins * 60;
						_winnings.put("ExerciseName", exerciseDisplayTitle);
						_winnings.put("NumberOfMinutesExercised",
								secondsElapsed / 60);
						_winnings.put("isSelfReported", true);
						getNexerciseApplication().showMainActivity(
								SelfReportActivity.this, true, session,
								_winnings);
					}
				}
				progressMessage = MessagesConstants.EXERCISE_SUBMIT_PROGRESS_STEP_4;
				publishProgress(99);
				FlurryHelper.logEvent("V:Nxr.SendData", flurryParams);
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressBarHorizontal.setProgress(values[0]);
			txtProgressInfo.setText(progressMessage);

		}
		@Override
		protected void onPostExecute(Void result) {
			try {
				customProgress.dismiss();
			} catch (Exception e) {
			}
			FlurryHelper.endSession(SelfReportActivity.this);
			if (_winnings != null) {
				Factory.killExerciseListActivity = true;
				SelfReportActivity.this.finish();
			} else if (errorCode != null) {
				if (errorCode.equals("403")) {
					Funcs.showAlertDialog(
							MessagesConstants.ERROR_ACTIVITY_NOT_SUBMITTED_FORBIDDEN,
							MessagesConstants.ERROR_USER_ACCOUNT_TITLE,
							SelfReportActivity.this);
				} else if (errorCode.equals("555")) {
					Funcs.showAlertDialog(
							MessagesConstants.ERROR_ACTIVITY_NOT_SUBMITTED_SESSION_OVERLAPS,
							MessagesConstants.ERROR_SESSION_OVERLAPS_TITLE,
							SelfReportActivity.this);
				} else {
					Funcs.showAlertDialog(
							MessagesConstants.ERROR_ACTIVITY_NOT_SUBMITTED,
							MessagesConstants.ERROR_TITLE,
							SelfReportActivity.this);
				}
			} else {
				Funcs.showAlertDialog(
						MessagesConstants.ERROR_ACTIVITY_NOT_SUBMITTED_CONNECTION_ERROR,
						MessagesConstants.ERROR_TITLE, SelfReportActivity.this);
			}
		}
	}
	
	private void setStartYear(){
		Calendar calendar = Calendar.getInstance();		
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(timeStarted);
		
		if(startCalendar.get(Calendar.MONTH)== 11 && calendar.get(Calendar.MONTH)== 0){
			startCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);  
			timeStarted = startCalendar.getTime();
		}
	}

	private DataLayer getDataLayer() {

		return ((NexerciseApplication) this.getApplication())
				.getDataLayerInstance();
	}

	private NexerciseApplication getNexerciseApplication() {

		return ((NexerciseApplication) this.getApplication());
	}

	private void initDateScroller(int selected, boolean isFirstTime){
		dateScroller=(LinearLayout)findViewById(R.id.dateScroller);
		String[] days = new String[] { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };
		dateScroller.removeAllViews();
		for(int i=0;i<8;i++){
			try {
				//calendar.
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, i-7);
				View item = ((LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.item_date_scroller, null, false);
				TextView dayText=(TextView)item.findViewById(R.id.day);
				dayText.setText(""+days[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
				TextView dateText=(TextView)item.findViewById(R.id.date);
				dateText.setText(""+calendar.get(Calendar.DAY_OF_MONTH));
				dateScroller.addView(item);
				View bottomLine = (View) item.findViewById(R.id.bottomLine);
				if (selected == i) {
					dayText.setTextColor(getResources().getColor(
							R.color.primary_orange));
					dateText.setTextColor(getResources().getColor(
							R.color.primary_orange));
					bottomLine.setBackgroundColor(getResources().getColor(
							R.color.primary_orange));
				}
				item.setTag(i);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							selectedDateIndex = Integer.parseInt(v.getTag().toString());
							initDateScroller(selectedDateIndex, false);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
		}

		for(int i=0;i<4;i++){
			try {

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, i+1);
				View item = ((LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.item_date_scroller, null, false);

				TextView dayText=(TextView)item.findViewById(R.id.day);
				dayText.setTextColor(getResources().getColor(R.color.light_grey));
				dayText.setText(""+days[calendar.get(Calendar.DAY_OF_WEEK) - 1]);

				TextView dateText=(TextView)item.findViewById(R.id.date);
				dateText.setTextColor(getResources().getColor(R.color.light_grey));
				dateText.setText(""+calendar.get(Calendar.DAY_OF_MONTH));

				dateScroller.addView(item);
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
		}
		calendarScroller = (HorizontalScrollView)findViewById(R.id.calendarScroller);
		Handler handler=new Handler();
		Runnable mTabSelector = new Runnable() {
			public void run() {
				try{
					View tabView = dateScroller.getChildAt(selectedDateIndex);
					final int scrollPos = tabView.getLeft() - (calendarScroller.getWidth() - tabView.getWidth()) / 2;
					calendarScroller.smoothScrollTo(scrollPos, 0);
				}catch(Exception e){

				}
			}
		};
		if(isFirstTime)
			handler.postDelayed(mTabSelector, 2000);
		else
			handler.postDelayed(mTabSelector, 10);
		//----------- set selectedDay and selectedMonth values -----------
		setSelectedDay();
		setSelectedMonth();
	}


	private void setTimeScroller(int index, boolean isFirstTime){

		timeScroller=(LinearLayout)findViewById(R.id.timeScroller);
		String[] hourVal = new String[] { "12am", "1am", "2am", "3am", "4am", "5am", "6am", "7am", "8am", "9am", "10am", "11am", "12pm" ,"1pm" ,"2pm" ,"3pm" ,"4pm" ,"5pm" ,"6pm" ,"7pm" ,"8pm", "9pm", "10pm", "11pm" };
		if(!isFirstTime){
			timeScroller.removeAllViews();
			for(int i=0; i<24; i++){
				try {

					View item = ((LayoutInflater) this
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
							.inflate(R.layout.item_time_scroller, null, false);
					TextView dayText=(TextView)item.findViewById(R.id.day);
					dayText.setText(""+hourVal[i]);

					timeScroller.addView(item);

					item.setTag(i);
					item.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								selectedTimeIndex = Integer.parseInt(v.getTag().toString());
								setTimeScroller(selectedTimeIndex, false);
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
							}
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
		}
		timeHoursScroller = (HorizontalScrollView)findViewById(R.id.timeHoursScroller);
		Handler handler=new Handler();
		Runnable mTabSelector = new Runnable() {
			public void run() {
				View tabView = timeScroller.getChildAt(selectedTimeIndex);

				final int scrollPos = tabView.getLeft() - (timeHoursScroller.getWidth() - tabView.getWidth()) / 2;
				timeHoursScroller.smoothScrollTo(scrollPos, 0);
			}
		};
		if(isFirstTime)
			handler.postDelayed(mTabSelector, 2000);
		else
			handler.postDelayed(mTabSelector, 10);

	}

	private void setSelectedDay(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, selectedDateIndex-7);
		selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
	}

	private void setSelectedMonth(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, selectedDateIndex-7);
		selectedMonth = calendar.get(Calendar.MONTH);
	}

	private int getSelectedDateIndex(int selectedDayOfYear, int todaysDayOfYear){
		int difference = 0;
		if(selectedDayOfYear > todaysDayOfYear){
			difference = 7 - (365 - selectedDayOfYear + todaysDayOfYear);
		}
		else{
			difference = 7 - (todaysDayOfYear-selectedDayOfYear);
		}
		
		if(difference < 0 || difference > 7)
			difference = 7;
		
		return difference;
	}
	
	/**  Navigation Drawer menu  changes  starts*/
	public void initNavigationDrawerMenu() {

		mActionBarHelper = new NxrActionBarMenuHelper(this);
		mCustomMenuList = mActionBarHelper.getMenuList();
		mActionBarHelper.removefromMenuItem(R.id.custom_menu_log_activity);
		mActionBarHelper.removefromMenuItem(R.id.custom_menu_see_history);
		mCustomMenuList.add(new NXRMenuItem(R.id.custom_menu_log_activity,
				MenuConstants.MENU_LOG_PAST_ACTIVITY, 1,
				R.drawable.ic_custom_past_activity, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mActionBarHelper.closeMenu();			
			}
		}));
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView) findViewById(R.id.list_view_drawer_menu);
		mDrawerLayout.setDrawerListener(mActionBarHelper.new NexerciseDrawerListener(mActionBarHelper));
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mActionBarHelper.setMenuList(mCustomMenuList);
		mActionBarHelper.setDrawerListView(mDrawerListView);
		mActionBarHelper.setDrawerLayout(mDrawerLayout);
		mActionBarHelper.setDrawerToggle();
		mCustomMenuAdapter = new SlideMenuAdapter(this, mCustomMenuList);
		Collections.sort(mCustomMenuList);
		if (mDrawerListView != null){
			mDrawerListView.setAdapter(mCustomMenuAdapter);
			mDrawerListView.setFastScrollEnabled(true);
			mDrawerListView.setSmoothScrollbarEnabled(true);
		}

		if(mActionBarHelper.getHeaderView() != null){
			mActionBarHelper.getHeaderView().setText(DisplayConstants.LOG_PAST_HEADING);
		}	
		super.setActionBarMenuHelper(mActionBarHelper);
		mCustomMenuAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(mActionBarHelper.getDrawerToggle() != null){
			if (mActionBarHelper.getDrawerToggle().onOptionsItemSelected(item)) {
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
	}
	/**  Navigation Drawer menu  changes  ends*/
	@Override
	public void initComponents() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fetchData() {
		// TODO Auto-generated method stub

	}

}
