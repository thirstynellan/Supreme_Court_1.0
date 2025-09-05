package edu.byuh.cis.ussupremecourt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class JView extends View implements TickListener {

	private Paint paint, cPaint, textPaint;
	int sWidth;
	//int sHeight;
	int minorRadius;
	int majorRadius;
	int cx;
	int cy;
	private Path circle1;
	private Path circle2;
	private ReadCSV csv;
	private ReadList list;
	private int bWidth;
	private int bHeight;
	private int year;
	private int month;
	private int day;
	private Calendar date = new GregorianCalendar();
	private boolean firstTime = true;
	private ArrayList<Calendar> sortKeys = new ArrayList<Calendar>();
	private boolean animating;
	private float outerCircleRadius, innerCircleRadius, whiteCircleRadius;
	private static final float TWO_PI = (float)(2f * Math.PI);
	private static final float PI_HALVES = (float)(Math.PI/2);
	private Path quLines, fpLines;
	private Map<Judge, Delta> deltas;
	private static final int FRAMES = 30;
	private int currentFrame;
	private PointF touchDown;
	private float oldDegrees;
	private Rotation rot;
	private float downRadius;
	private float boingDegrees;
	private boolean bounceBack;
	private FakeToast fakeToast;

	public void createFakeToast(CharSequence words) {
		fakeToast = new FakeToast(this, words.toString());

	}

	public void cancelFakeToast() {
		if (fakeToast != null) {
			fakeToast = null;
			invalidate();
		}
	}

	private enum Rotation {
		CW,		//clockwise
		CCW		//counter-clockwise
	}

	private class JudgeLayout {
		int tweNumber;
		int preNumber;
		Map<Judge, JudgePosition> positions;

		boolean contains(Judge a) {
			return positions.containsKey(a);
		}
		Set<Judge> getJudges() {
			return positions.keySet();
		}
	}

	private class JudgePosition {
		float angle;
		float radius;
		RectF bounds;
		boolean grounded;

		public JudgePosition() {
			bounds = new RectF();
			grounded = false;
		}
	}

	private class Delta {
		float dAngle;
		PointF dPos = null;

		boolean isNull() {
			return ((dPos == null && Math.abs(dAngle) <= 0.0001) ||
					(dPos != null && (dPos.x == 0 && dPos.y == 0)));
		}

		@Override
		public String toString() {
			return "angle: " + dAngle + ", dPos: " +
					((dPos == null) ? "null" : ("x=" + dPos.x + " y=" + dPos.y));
		}
	}

	private JudgeLayout currentLayout, newLayout;

	public JView(Context c, AttributeSet abs) {
		super(c, abs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.LTGRAY);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.rgb(63,63,63));
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		animating = false;
		currentLayout = null;
		newLayout = null;
		touchDown = null;
		Timer timer = new Timer();
		timer.addFollower(this);
		//velocity = 0;
		boingDegrees = 0;
		//touching = false;
		rot = Rotation.CCW;
		bounceBack = false;

	}

	public boolean goForward() {
		//Log.d("CS203","This is a test for forward button");
		int i = sortKeys.indexOf(date);
		if (i==-1) {
			for (int j = 0; j < sortKeys.size(); j++) {
				if (((Calendar) sortKeys.get(j)).before(date) == true) {
					date = (Calendar) sortKeys.get(j);
					i = sortKeys.indexOf(date);
					break;
				}
			}
		}
		if (i == 0) {

			return false;
		}else{
			if (i <= sortKeys.size()) {
				date = sortKeys.get(i - 1);
				year = date.get(Calendar.YEAR);
				month = date.get(Calendar.MONTH) + 1;
				day = date.get(Calendar.DAY_OF_MONTH);
				respondToDateChange();
			}
		}

		return true;
	}
	public void goBack() {
		int i = sortKeys.indexOf(date);

		if (i==-1) {
			for (int j = 0; j < sortKeys.size(); j++) {
				if (((Calendar) sortKeys.get(j)).before(date) == true) {
					date = (Calendar) sortKeys.get(j);
					i = sortKeys.indexOf(date);
					break;
				}
			}
		}
		if(i>=0){
			date = sortKeys.get(i + 1);
			year = date.get(Calendar.YEAR);
			month = date.get(Calendar.MONTH)+1;
			day = date.get(Calendar.DAY_OF_MONTH);
			respondToDateChange();
		}
	}


	private void setDefaultImageSize() {
		bHeight = minorRadius / 6;
		bWidth = bHeight * 4 / 5;
	}

	public JudgeLayout calculatePositions() {
		JudgeLayout pp = new JudgeLayout();
		pp.positions = new HashMap<>();
		quLines = new Path();
		fpLines = new Path();
		if (year != 0) {
			// date.set(year,month-1,day,0,0,0);//month is 0 based with set
			// method
			date = new GregorianCalendar(year, month - 1, day);
			//log("New date is " + day + " " + month + " " + year);

			if (list.twelveList.containsKey(date) == false) {
				// loop through the sorted dates from back wards and use the
				// before method, if return true then set the date
				for (int i = 0; i < sortKeys.size(); i++) {
					if (((Calendar) sortKeys.get(i)).before(date) == true) {
						date = (Calendar) sortKeys.get(i);
						break;
					}
				}
			}
			pp.tweNumber = list.twelveList.get(date).size();
			pp.preNumber = list.presidencyList.get(date).size();
		} else {
			pp.tweNumber = 12;
			pp.preNumber = 3;
		}

		// Find the width and height of the bitmap image, use RectF to scale it
		if (pp.preNumber > 6 && pp.preNumber <= 8) {
			bHeight = minorRadius / 10;
			bWidth = bHeight * 4 / 5;
		} else if (pp.preNumber == 9) {
			bHeight = minorRadius / 11;
			bWidth = bHeight * 5 / 6;
		} else {
			setDefaultImageSize();
		}

		//compute positions of quorum of twelve
		float radians = -PI_HALVES; //top of circle
		float increment = TWO_PI / pp.tweNumber;
		float fudge = 0;	//for "spring back" when user tries to advance past current year
		float fudgeFactor = boingDegrees / pp.tweNumber;
		for (Judge a : list.twelveList.get(date)) {
			JudgePosition pos = pp.positions.get(a);
			if (pos == null) {
				pos = new JudgePosition();
				pp.positions.put(a, pos);
			}
			if (bouncing()) {
				pos.grounded = true;
				//log("fudging " + a.getName() + " by " + fudge + " radians. Original=" + radians + " Fudged=" + (radians + fudge));
			}
			pos.angle = radians + fudge;
			fudge += fudgeFactor;
			pos.radius = (outerCircleRadius + whiteCircleRadius) / 2f;
			float pictureCenterX = (float) (cx + pos.radius * Math.cos(pos.angle));
			float pictureCenterY = (float) (cy + pos.radius * Math.sin(pos.angle));
			float spokeX = (float) (cx + outerCircleRadius * Math.cos(pos.angle + increment/2));
			float spokeY = (float) (cy + outerCircleRadius * Math.sin(pos.angle + increment/2));
			pos.bounds.set(pictureCenterX-bWidth/2,
					pictureCenterY-bHeight/2-bHeight*0.1f,
					pictureCenterX+bWidth/2,
					pictureCenterY+bHeight/2-bHeight*0.1f);
			quLines.moveTo(cx, cy);
			quLines.lineTo(spokeX, spokeY);
			radians -= increment;
		}

		//compute positions of first presidency members
		radians = -PI_HALVES; //top of circle
		increment = TWO_PI / pp.preNumber;
		for (Judge a : list.presidencyList.get(date)) {
			JudgePosition pos = pp.positions.get(a);
			if (pos == null) {
				pos = new JudgePosition();
				pp.positions.put(a, pos);
			}
			pos.angle = radians;
			//this is the swankiest ternary I've ever written!!
			pos.radius = innerCircleRadius * (pp.preNumber == 1 ? 0f :
					pp.preNumber == 2 ? 0.5f :
							pp.preNumber == 3 ? 0.55f :
									pp.preNumber == 4 ? 0.6f :
											pp.preNumber == 5 ? 0.65f :
													0.7f);
			float pictureCenterX = (float) (cx + pos.radius * Math.cos(pos.angle));
			float pictureCenterY = (float) (cy + pos.radius * Math.sin(pos.angle));
			float spokeX = (float) (cx + innerCircleRadius * Math.cos(pos.angle + increment/2));
			float spokeY = (float) (cy + innerCircleRadius * Math.sin(pos.angle + increment/2));
			pos.bounds.set(pictureCenterX-bWidth/2,
					pictureCenterY-bHeight/2-bHeight*0.1f,
					pictureCenterX+bWidth/2,
					pictureCenterY+bHeight/2-bHeight*0.1f);
			fpLines.moveTo(cx, cy);
			fpLines.lineTo(spokeX, spokeY);
			radians -= increment;
		}

		//hack for 1833, where Joseph Smith had no counselors.This will also handle
		if (list.presidencyList.get(date).size() == 1) {
			fpLines.reset();
		}
		//in the begining when there was no chief justice
		if (list.twelveList.get(date).size() == 1) {
			quLines.reset();
		}

		return pp;
	}

	@Override
	public void onDraw(Canvas c) {
		if (firstTime) {
			cancelFakeToast();
			System.out.print("Test"+month+day+year);
			sWidth = getWidth();
			int sHeight = getHeight();
			minorRadius = Math.min(sWidth, sHeight);
			majorRadius = Math.max(sWidth, sHeight);
			cx = sWidth / 2;
			//if (sHeight > sWidth) {
			//cy = (int) (sWidth / 2.2 + sHeight / 11);
			//} else {
			cy = sHeight/2;
			//}
			outerCircleRadius = minorRadius / 2f;
			whiteCircleRadius = minorRadius*0.21f;
			innerCircleRadius = minorRadius*0.19f;
			setDefaultImageSize();

			textPaint.setTextSize(findThePerfectFontSize(sWidth * 0.018f)); // font is measured by fraction of screen size

			circle1 = new Path();
			circle1.addCircle(cx, cy, outerCircleRadius, Direction.CW);
			circle2 = new Path();
			circle2.addCircle(cx, cy, whiteCircleRadius, Direction.CW);

			csv = new ReadCSV(getContext().getAssets(), getContext(), bWidth, bHeight);
			list = new ReadList(getContext().getAssets(), csv);
			setKeys(list.twelveList.keySet());

			currentLayout = calculatePositions();

			firstTime = false;
		}

		// draw back ground color
		c.drawColor(Color.rgb(63,63,63));

		// draw the biggest circle
		cPaint.setColor(Color.rgb(255,255,255));
		c.drawPath(circle1, cPaint);

		//if (!animating && !bouncing()) { //sing it, deMorgan!
		if (!(animating || bouncing())) {
			//draw the lines
			paint.setColor(Color.rgb(177, 221, 140));
			c.drawPath(quLines, paint);
		}

		// draw the second circle
		cPaint.setColor(Color.rgb(177, 221, 140));
		c.drawPath(circle2, cPaint);

		// draw the smallest circle
		cPaint.setColor(Color.rgb(255,255,255));
		c.drawCircle(cx, cy, innerCircleRadius, cPaint);

		if (!animating || bounceBack) {
			//draw the FP lines
			c.drawPath(fpLines, paint);
		}

		for (Judge ap : currentLayout.positions.keySet()) {

			//if we're animating, don't instantiate anything.
			if (animating) {
				RectF imgPos = currentLayout.positions.get(ap).bounds;
				c.drawBitmap(ap.getPhoto(), imgPos.left, imgPos.top, paint);
			} else {
				Bitmap z = Bitmap.createScaledBitmap(ap.getPhoto(), bWidth, bHeight, true);
				c.drawBitmap(z, null, currentLayout.positions.get(ap).bounds, paint);
			}

			if (!animating || bounceBack) {
				String judgeName = ap.getName();
				int indexOfLastName = judgeName.lastIndexOf(' ');
				String lastName = judgeName.substring(indexOfLastName);
				String givenNames = judgeName.substring(0, indexOfLastName);

				c.drawText(givenNames, currentLayout.positions.get(ap).bounds.centerX(),
						currentLayout.positions.get(ap).bounds.bottom - textPaint.ascent(), textPaint);
				c.drawText(lastName, currentLayout.positions.get(ap).bounds.centerX(),
						currentLayout.positions.get(ap).bounds.bottom - 2*textPaint.ascent(), textPaint);
			}
		}
		if (fakeToast != null) {
			fakeToast.draw(c);
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent me) {
		//ignore touch events during animation
		//if (animating) return true;

		float x = me.getX(); // get the position of where the user
		// touched the screen
		float y = me.getY();
		//Log.d("touch point", "" + x + "; " + y);

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			touchDown = new PointF(x,y);
			downRadius = (float)Math.hypot(touchDown.x-cx, touchDown.y-cy);
			oldDegrees = (float)Math.atan2(y-cy, x-cx);
			//prevTouchTime = me.getDownTime();
		} else if (me.getAction() == MotionEvent.ACTION_UP) {
			//Log.d("CS203", "finger up!");
			if (boingDegrees != 0) {
				boingDegrees = 0;
				bounceBack = true;
				//log("ready to bounce back!");
				respondToDateChange();
			}
			//was this a simple single tap? check by calculating distance
			//between down and up event.
			float dx = x-touchDown.x;
			float dy = y-touchDown.y;
			double dist = Math.hypot(dx, dy);
			if (dist < minorRadius /50) {
				//finger did not move much, consider it a tap.
				for (Judge ap : currentLayout.positions.keySet()) {
					RectF image = currentLayout.positions.get(ap).bounds;
					if (image.contains(x, y)){
						//get that person's bio in a PopupWindow class/ new activity/ dialog box
						LinearLayout linearlt = new LinearLayout(getContext());
						linearlt.setBackgroundColor(Color.rgb(63,63,63));
						linearlt.setPadding(5,5,5,5);
						ScrollView scrollvw = new ScrollView(getContext());
						ImageView imagevw = new ImageView(getContext());
						Bitmap largePhoto = BitmapFactory.decodeResource(getResources(), ap.resID);
						imagevw.setImageBitmap(largePhoto);
						imagevw.setPadding(10,10,0,10);
						imagevw.setBackgroundColor(Color.rgb(63,63,63));
						if (getWidth() >= getHeight()) {
							//stack the image and biography vertically on tall screens,
							//or horizontally on short screens
							linearlt.setOrientation(LinearLayout.HORIZONTAL);
						} else {
							linearlt.setOrientation(LinearLayout.VERTICAL);
						}
						TextView textvw = new TextView(getContext());
						textvw.setText(ap.getBio());
						textvw.setTextColor(Color.rgb(177, 221, 140));
						textvw.setPadding(15, 10, 15, 0);
						scrollvw.addView(textvw);
						linearlt.addView(imagevw);
						linearlt.addView(scrollvw);

						//when user taps on an image
						AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
						ab.setTitle(ap.getName())
								.setView(linearlt)
								.setNeutralButton("OK", null)
								.show();
						break;
					}
				}
			}
			touchDown = null;
		} else if (me.getAction() == MotionEvent.ACTION_MOVE && touchDown != null) {
			if (downRadius >= innerCircleRadius/2 && downRadius <= outerCircleRadius) {
				float degreeDelta=0;
				//float dx = x-touchDown.x;
				//float dy = y-touchDown.y;
				float curRadius = (float)Math.hypot(x-cx, y-cy);
				float degree = (float)(Math.atan2(y-cy, x-cx));
				if (curRadius >= innerCircleRadius/2 && curRadius <= outerCircleRadius) {
					//hack - check for pi to -pi switchover,
					//keep velocity and direction constant
					if (Math.signum(oldDegrees) == Math.signum(degree)) {
						degreeDelta = degree - oldDegrees;
						if (degree < oldDegrees) {
							//clockwise, move forward in time
							if (year <= MainActivity.CURRENT_YEAR) {
								rot = Rotation.CCW;
							}
						} else {
							if (year >= MainActivity.STARTING_YEAR) {
								rot = Rotation.CW;
							}
						}
					} else {
						if (rot == Rotation.CW) {
							//crossing from positive PI to negative PI
							if (oldDegrees > degree) {
								degreeDelta = (float)((Math.PI-oldDegrees) + (Math.PI + degree));
							} else {
								degreeDelta = Math.abs(oldDegrees) + degree;
							}
						} else {
							//crossing from negative PI to positive PI
							if (oldDegrees < degree) {
								degreeDelta = (float)((-Math.PI-oldDegrees) - (Math.PI - degree));
							} else {
								degreeDelta = -(Math.abs(degree) + oldDegrees);
							}
						}
						//log("crossing the border! old="+oldDegrees + "; new=" + degree + "; delta=" + Math.toDegrees(degreeDelta));
					}
					int yearDelta = (int)(degreeDelta * (50 / Math.PI));//50 years = half circle
					int newYear = year + yearDelta;
					if (newYear > MainActivity.CURRENT_YEAR && MyPreferences.getAnimationPreference(getContext())) {
						boingDegrees += degreeDelta;
						currentLayout = calculatePositions();
						invalidate();
						//log("peering into the future! " + Math.toDegrees(boingDegrees));
					}
					newYear = Math.max(newYear, MainActivity.STARTING_YEAR);
					newYear = Math.min(newYear, MainActivity.CURRENT_YEAR);
					if (!bouncing()) {
						((MainActivity)getContext()).setYear(newYear);
					}
					oldDegrees = degree;
				}
			}

		}

		return true;
	}

//	public void setCSV(ReadCSV csv) {
//		this.csv = csv;
//	}
//
//	public ReadCSV getCSV() {
//		return csv;
//	}
//
//	public void setList(ReadList list) {
//		this.list = list;
//	}
//
//	public ReadList getList(ReadList list) {
//		return list;
//	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public Calendar getDate(){return date;}

	public void respondToDateChange() {
		newLayout = calculatePositions();
		if (MyPreferences.getAnimationPreference(getContext())) {
			animating = diff();
		} else {
			currentLayout = newLayout;
		}
		invalidate();
	}
	/**
	 *
	 * @return true if there's a difference between the old and new quorums; false if
	 * they're the same.
	 */
	private boolean diff() {
		//sanity check. Get out if we don't have anything to draw.
		if (currentLayout == null) return false;

		currentFrame = 0;
		deltas = new HashMap<>();

		for (Judge a : currentLayout.getJudges()) {
			JudgePosition oldPos = currentLayout.positions.get(a);
			Delta d = new Delta();
			if (newLayout.contains(a)) {
				//this judge is staying on-screen, just move him
				JudgePosition newPos = newLayout.positions.get(a);
				//this judge is in both old and new layouts
				if (oldPos.grounded && oldPos.radius == newPos.radius) {
					//the judge has not changed rings, rotate
					d.dAngle = (newPos.angle - oldPos.angle) / FRAMES;
					//log("need to move " + a.getName() + " by " + Math.toDegrees((newPos.angle - oldPos.angle)) + " degrees.");
				} else {
					//the judge is moving from one ring to another, move linearly.
					d.dPos = new PointF((newPos.bounds.left - oldPos.bounds.left)/FRAMES,
							(newPos.bounds.top - oldPos.bounds.top)/FRAMES);
					oldPos.grounded = false;
				}
			} else {
				//this judge is in the old but not the new,
				//so move radially outward like an explosion.
				double theta = -Math.atan2(oldPos.bounds.centerX()-cx, oldPos.bounds.centerY()-cy);
				if (rot == Rotation.CW) {
					theta += Math.PI;
				}
				double radius = majorRadius*0.75;
				float pictureCenterX = (float) (cx + radius * Math.cos(theta));
				float pictureCenterY = (float) (cy + radius * Math.sin(theta));
				RectF newPos = new RectF(pictureCenterX-bWidth/2,
						pictureCenterY-bHeight/2-bHeight*0.1f,
						pictureCenterX+bWidth/2,
						pictureCenterY+bHeight/2-bHeight*0.1f);
				d.dPos = new PointF((newPos.left - oldPos.bounds.left)/FRAMES,
						(newPos.top - oldPos.bounds.top)/FRAMES);
				oldPos.grounded = false;

			}
			deltas.put(a, d);
		}
		for (Judge a : newLayout.getJudges()) {
			if (!(currentLayout.contains(a))) {
				//this is a new judge, fly on-screen
				JudgePosition oldPos = new JudgePosition();
				JudgePosition newPos = newLayout.positions.get(a);
				//if the new position is on the right-hand side of the screen, fly from right to left.
				//if the new position is on the left-hand side of the screen, fly from left to right.
				if (newPos.bounds.centerX() < sWidth/2) {
					oldPos.bounds = new RectF(-bWidth, newPos.bounds.top, 0, newPos.bounds.bottom);
				} else {
					oldPos.bounds = new RectF(sWidth, newPos.bounds.top, sWidth+bWidth, newPos.bounds.bottom);
				}

				currentLayout.positions.put(a, oldPos);
				Delta d = new Delta();
				d.dPos = new PointF((newPos.bounds.left - oldPos.bounds.left)/FRAMES,
						(newPos.bounds.top - oldPos.bounds.top)/FRAMES);
				deltas.put(a, d);
			}
		}

		//sanity check: was there actually any change?
		boolean changed = false;
		for (Delta d : deltas.values()) {
			if (!(d.isNull())) {
				changed = true;
				break;
			}
		}
		return changed;
	}
	//all the dates are being put in a list
	public void setKeys(Set<Calendar> keys) {
		//this.keys = keys;
		sortKeys.addAll(keys);
		Collections.sort(sortKeys);
		Collections.reverse(sortKeys);
	}

	//dim is how many pixels tall you want your font to be
	public static float findThePerfectFontSize(float dim) {// get variable font
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

	@Override
	public void onTick() {
		if (animating) {
			for (Judge ap : currentLayout.positions.keySet()) {
				Delta d = deltas.get(ap);
				if (d.dPos == null) {
					//move it angularly, not linearly
					float radius = currentLayout.positions.get(ap).radius;
					float angle = currentLayout.positions.get(ap).angle += d.dAngle;
					float pictureCenterX = (float) (cx + radius * Math.cos(angle));
					float pictureCenterY = (float) (cy + radius * Math.sin(angle));
					currentLayout.positions.get(ap).bounds.set(pictureCenterX-bWidth/2,
							pictureCenterY-bHeight/2-bHeight*0.1f,
							pictureCenterX+bWidth/2,
							pictureCenterY+bHeight/2-bHeight*0.1f);

				} else {
					currentLayout.positions.get(ap).bounds.offset(d.dPos.x, d.dPos.y);
				}
			}
			currentFrame++;
			if (currentFrame > FRAMES) {
				animating = false;
				bounceBack = false;
				currentLayout = newLayout;
				//every judge is "grounded" at this point.
				for (JudgePosition ap : currentLayout.positions.values()) {
					ap.grounded = true;
				}
			}
			invalidate();
		}
	}

	private boolean bouncing() {
		return boingDegrees != 0;
	}

	private void log(String s) {
		Log.d("CS203", s);
	}

}