package edu.byuh.cis.ussupremecourt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
//import android.widget.Toast;
import android.content.Context;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends Activity implements OnSeekBarChangeListener,View.OnClickListener {

	private JView jv;
	private SeekBar timeSlider;
	private ImageButton fowButton;
	private ImageButton bacButton;

	//static final int DATE_DIALOG_ID = 999;
	public static int STARTING_YEAR = 1789;
	public static int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);
	private HashMap<Calendar, String> texts = new HashMap();
	private Calendar dates;
	//Toast t;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the seekBar
		setContentView(R.layout.main);

		jv = (JView) findViewById(R.id.jView1);
		int diff = CURRENT_YEAR - STARTING_YEAR;
		timeSlider = (SeekBar) findViewById(R.id.seekBar1);
		timeSlider.setMax(diff);
		timeSlider.setProgress(diff);
		timeSlider.setOnSeekBarChangeListener(this);

		jv.setMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
		jv.setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		jv.setYear(CURRENT_YEAR);

		fowButton = (ImageButton) findViewById(R.id.fowbutton);
		bacButton = (ImageButton) findViewById(R.id.bacbutton);

		fowButton.setOnClickListener((android.view.View.OnClickListener) this);
		bacButton.setOnClickListener((android.view.View.OnClickListener) this);
		readEvent();
		updateTextView();

		//Due to Edge-to-Edge: move the toasts down, so they appear
		//below the toolbar
		ViewCompat.setOnApplyWindowInsetsListener(jv, (v, windowInsets) -> {
			Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			mlp.leftMargin = insets.left;
			mlp.topMargin = insets.top;
			mlp.rightMargin = insets.right;
			v.setLayoutParams(mlp);
			return WindowInsetsCompat.CONSUMED;
		});

		//Due to Edge-to-Edge: Move the slider up, so it appears
		//above the navigation bar.
		ViewCompat.setOnApplyWindowInsetsListener(timeSlider, (v, windowInsets) -> {
			Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			// Apply the insets as a margin to the view. This solution sets only the
			// bottom, left, and right dimensions, but you can apply whichever insets are
			// appropriate to your layout. You can also update the view padding if that's
			// more appropriate.
			ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			mlp.leftMargin = insets.left;
			mlp.bottomMargin = insets.bottom;
			mlp.rightMargin = insets.right;
			v.setLayoutParams(mlp);

			// Return CONSUMED if you don't want the window insets to keep passing
			// down to descendant views.
			return WindowInsetsCompat.CONSUMED;
		});
	}

	private void showToast(CharSequence words) {
		jv.createFakeToast(words);
		Log.d("***FAKE_TOAST***", words.toString());
	}

	private void cancelToast() {
		jv.cancelFakeToast();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
								  boolean fromUser) {
		// length_edit.setText(Integer.toString(progress + 1835));
//		TextView tv = (TextView) findViewById(R.id.textView2);
//		tv.setText(Integer.toString(progress + STARTING_YEAR));
		if (fromUser) {
			//only set the month and day if the user touched the
			//slider bar. If done via the calendar widget, the
			//month and day are set in onOptionsItemSelected.
			if (progress == 0) {
				//if user sets slider to 1832, go to March 8.
				jv.setMonth(10);
				jv.setDay(5);
			} else if (progress == (CURRENT_YEAR-STARTING_YEAR)) {
				//if user sets slider to current year, go to today's date.
				jv.setMonth(Calendar.getInstance().get(Calendar.MONTH)+1);
				jv.setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
			} else {
				jv.setMonth(1);
				jv.setDay(1);//default the slider bar as the first day of each year 
			}
		}
		// send the year to jview
		//Log.d("progress year", "" + progress + STARTING_YEAR);
		jv.setYear(progress + STARTING_YEAR);
		//v.setText(get(jv.getYear()));
		//Log.d("CS203", "about to call respondToDateChange");
		jv.respondToDateChange();
		updateTextView();

		//enable back and forward button
		bacButton.setEnabled(true);
		fowButton.setEnabled(true);
	}
	public void updateTextView(){
		int year = jv.getYear();
		int day = jv.getDay();
		int month =jv.getMonth();
		Calendar dates = new GregorianCalendar(year, month - 1, day);
		//set max and min bounds disabling button
		//SimpleDateFormat month_date = new SimpleDateFormat("MMM");
		String month_name = dates.getDisplayName(Calendar.MONTH,Calendar.SHORT, Locale.getDefault());
		TextView tv = (TextView) findViewById(R.id.textView2);
		tv.setText(day
				+ " " +
				month_name
				+ " " +
				year);
	}

	public void setYear(int year) {
		//FIXME dirty hack; duplicate code from onProgressChanged.
		if (year == STARTING_YEAR) {
			//if user sets slider to 1832, go to March 8.
			jv.setMonth(10);
			jv.setDay(5);
		} else if (year == CURRENT_YEAR) {
			//if user sets slider to current year, go to today's date.
			jv.setMonth(Calendar.getInstance().get(Calendar.MONTH)+1);
			jv.setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		} else {
			jv.setMonth(1);
			jv.setDay(1);//default the slider bar as the first day of each year
		}
		timeSlider.setProgress(year-STARTING_YEAR);
		updateTextView();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_go_to_today:
				// start datePicker with dialog box
				DatePickerDialog dpd = new DatePickerDialog(this,DatePickerDialog.THEME_HOLO_DARK,
						new OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
												  int monthOfYear, int dayOfMonth) {
								//Log.d("CS203", "inside ondateset");
								jv.setMonth(monthOfYear+1);//the month is sends to JView is 0 based
								jv.setDay(dayOfMonth);
								//no need to call jv.setYear b/c onProgressChanged does that.
								//sync with sliderbar
								if (timeSlider.getProgress() == (year-STARTING_YEAR)) {
									//if the user is changing the day or month but not the year,
									//then calling timeSlider.setProgress() will not trigger
									//a call to onProgressChanged. So force the jview to
									//update manually.
									jv.respondToDateChange();
									updateTextView();
								} else {
									timeSlider.setProgress(year - STARTING_YEAR);
								}
							}

						}, CURRENT_YEAR, 1, 1);
				dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
				dpd.getDatePicker().setMinDate(-5687711539858L);//need api 11 up, time calculates by Unix time,
				//use calendar object and turn to a long and
				//get the the seconds , before 1970 is negative, after is positive
				//-4256373600000L
				dpd.show();
				dpd.updateDate(jv.getYear(),jv.getMonth()-1, jv.getDay());

				break;

			case R.id.action_about://method chaining
				new AlertDialog.Builder(this)
						.setTitle(R.string.action_about)
						.setMessage(R.string.about_detail)
						.setNeutralButton(R.string.ok,null)
						.show();
				break;
			/*case R.id.action_prefs:
				Intent i = new Intent(this, MyPreferences.class);
				startActivity(i);*/
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}



	CharSequence text = null;

	@Override
	public void onClick(View v) {

		Date dateShown = new Date(jv.getYear(),jv.getMonth(),jv.getDay());
		Date dateBegan = new Date(1789,10,5);
		Context context = getApplicationContext();
		switch (v.getId()) {
			case R.id.bacbutton:
				fowButton.setEnabled(true);
				cancelToast();
				if (dateShown.after(dateBegan)){
					//Log.d("CS203", "");
					jv.goBack();
					updateTextView();
				}else{
					bacButton.setEnabled(false);
					showToast("You have reached the beginning of the timeline");
					//t = Toast.makeText(context,"You have reached the beginning of the timeline",Toast.LENGTH_LONG);
					//t.setGravity(Gravity.TOP, 0, 0);
					//t.show();
				}
		}
		switch (v.getId()) {
			case R.id.fowbutton:
				cancelToast();
				bacButton.setEnabled(true);


				if(jv.goForward()==false){
					setYear(CURRENT_YEAR);
					//t = Toast.makeText(context,"You've reached the current date",Toast.LENGTH_LONG);
					//t.setGravity(Gravity.TOP, 0, 0);
					//t.show();
					showToast("You've reached the current date");
					fowButton.setEnabled(false);
				}
				else{
					text = texts.get(jv.getDate());
					showToast(text);
				}

				updateTextView();
		}
	}
	//this method handles all the toast from scanning to showing all the actual toast
	public void readEvent(){
		try {
			AssetManager assets = getResources().getAssets();
			InputStream inputfile = assets.open("eventdesc");
			Scanner s = new Scanner(inputfile);
			while(s.hasNext()){
				String line = s.nextLine();
				String splitBy= "\\|";

				String[] events = line.split(splitBy);
				int year = Integer.parseInt(events[0].substring(0, 4));
				int month = Integer.parseInt(events[0].substring(4, 6));
				int day = Integer.parseInt(events[0].substring(6, 8));
				//Log.d("prints the dates" , year+"/"+month+"/"+ day);
				dates = new GregorianCalendar(year,month-1,day);
				//Log.d("CS203", "" +events[1]);

				texts.put(dates, events[1]);

			}
			s.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	public static float findThePerfectFontSize(float dim) {
		float fontSize = 1;
		Paint p = new Paint();
		p.setTextSize(fontSize);
		while (true) {
			float asc = -p.getFontMetrics().ascent;
			if (asc > dim) {
				break;
			}
			fontSize++;
			p.setTextSize(fontSize);
		}
		return fontSize;
	}

}
