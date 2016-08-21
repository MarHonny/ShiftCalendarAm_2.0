package mh.calendarlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class is a calendar widget for displaying and selecting dates.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public class CalendarView extends LinearLayout {


	private Drawable mTodayBackground;
	private boolean mShouldPickOnMonthChange = true;

	Context mContext;
	private ViewPager mPager;
	private MonthPagerAdapter mAdapter;
	private WeekLabelView mWeekLabelView;
	private OnDatePickListener mOnDatePickListener;
	private boolean mIsChangedByUser;

	int schemeID;
	String schemeGroup;

	AttributeSet attrs;

	public CalendarView(Context context) {
		this(context, null);
		mContext = context;
	}

	public CalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.attrs = attrs;
		mContext = context;
		init(attrs);
	}

	/* init lunar view */
	private void init(AttributeSet attrs) {
		/* get custom attrs */
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView);
			a.getBoolean(R.styleable.CalendarView_shouldPickOnMonthChange, mShouldPickOnMonthChange);
		a.recycle();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			/* if we're on good Android versions, turn off clipping for cool effects */
			setClipToPadding(false);
			setClipChildren(false);
		} else {
			/* old Android does not like _not_ clipping view pagers, we need to clip */
			setClipChildren(true);
			setClipToPadding(true);
		}

		/* set the orientation to vertical */
		setOrientation(VERTICAL);

		mWeekLabelView = new WeekLabelView(getContext());
		mWeekLabelView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWeekLabelBackgroundColor));
		addView(mWeekLabelView);

		mPager = new ViewPager(getContext());
		mPager.setOffscreenPageLimit(1);
		addView(mPager);


		mAdapter = new MonthPagerAdapter(getContext(), this);
		mPager.setAdapter(mAdapter);
		mPager.addOnPageChangeListener(mPageListener);
		mPager.setCurrentItem(mAdapter.getIndexOfCurrentMonth());
		mPager.setPageTransformer(false, new ViewPager.PageTransformer() {
			@Override
			public void transformPage(View page, float position) {
				page.setAlpha(1 - Math.abs(position));
			}
		});
	}



	/* page change listener */
	private ViewPager.OnPageChangeListener mPageListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}

		@Override
		public void onPageSelected(int position) {
			if (mIsChangedByUser) {
				mIsChangedByUser = false;
				return;
			}

			mAdapter.resetSelectedDay(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	public void reset() {
		backToToday();
		mAdapter = new MonthPagerAdapter(getContext(), this);
		mPager.setAdapter(mAdapter);
		mPager.addOnPageChangeListener(mPageListener);
		mPager.setCurrentItem(mAdapter.getIndexOfCurrentMonth());
	}

	/* get color with given color resource id */
	private int getColor(@ColorRes int resId) {
		return ContextCompat.getColor(getContext(), resId);
	}

	/* get color with given drawable resource id */
	private Drawable getDrawable(@DrawableRes int resId) {
		return ContextCompat.getDrawable(getContext(), resId);
	}

	/**
	 * Interface definition for a callback to be invoked when date picked.
	 */
	public interface OnDatePickListener {
		/**
		 * Invoked when date picked.
		 *
		 * @param view {@link CalendarView}
		 * @param monthDay {@link MonthDay}
		 */
		void onDatePick(CalendarView view, MonthDay monthDay);
	}

	/**
	 * Get the calendar grid color.
	 *
	 * @return calendar grid color
	 */
	protected int getGridColor() {
		return ContextCompat.getColor(mContext ,R.color.colorCalendarGrid);
	}

	protected int getTodayIndicatorColor() {
		return ContextCompat.getColor(mContext, R.color.colorTodayIndicator);
	}

	/**
	 * Get the color of month view background.
	 *
	 * @return color of month background
	 */
	protected int getMonthBackgroundColor() {
		return ContextCompat.getColor(mContext, R.color.colorCalendarBackGround);
	}

	/**
	 * Get the color uncheckable day view background.
	 *
	 * @return color of uncheckable day background
	 */
	protected int getMonthCheckableBackgroundColor() {
		return ContextCompat.getColor(mContext ,R.color.colorCalendarDefaultCheckable);
	}



	/**
	 * Get the color uncheckable day view background.
	 *
	 * @return color of uncheckable day background
	 */
	protected int getMonthUncheckableBackgroundColor() {
		return ContextCompat.getColor(mContext ,R.color.colorCalendarUnCheckable);
	}


	/**
	 * Get the text color of solar day.
	 *
	 * @return text color of solar day
	 */
	protected int getSolarTextColor() {
		return R.color.colorCalendarText;
	}

	/**
	 * Get the text color of lunar day.
	 *
	 * @return text color of lunar day
	 */
	protected int getShiftTextColor() {
		return ContextCompat.getColor(mContext, R.color.colorCalendarText);
	}

	/**
	 * Get the highlight color.
	 *
	 * @return thighlight color
	 */
	/*protected int getHightlightColor() {
		return mHightlistColor;
	}**/

	/**
	 * Get the color of uncheckable day.
	 *
	 * @return uncheckable color
	 */
	protected int getUnCheckableColor() {
		return ContextCompat.getColor(mContext, R.color.colorUnCheckableText);
	}

	/**
	 * Get the background of today.
	 *
	 * @return background drawable
	 */
	protected Drawable getTodayBackground() {
		return mTodayBackground;
	}

	/**
	 * Get the color of checked day.
	 *
	 * @return color of checked day
	 */
	/*int getCheckedDayBackgroundColor() {
		return checkedDayBackgroundColor;
	}*/

	/**
	 * Auto pick date when month changed or not.
	 *
	 * @return true or false
	 */
	protected boolean getShouldPickOnMonthChange() {
		return mShouldPickOnMonthChange;
	}

	/**
	 * Set the background drawable of today.
	 *
	 * @param resId drawable resource id
	 */
	public void setTodayBackground(@DrawableRes int resId) {
		mTodayBackground = getDrawable(resId);
	}

	/**
	 * Set on date click listener. This listener will be invoked
	 * when a day in month was picked.
	 *
	 * @param l date pick listner
	 */
	public void setOnDatePickListener(OnDatePickListener l) {
		mOnDatePickListener = l;
	}

	/**
	 * Dispatch date pick listener. This will be invoked be {@link MonthView}
	 *
	 * @param monthDay month day
	 */
	protected void dispatchDateClickListener(MonthDay monthDay) {
		if (mOnDatePickListener != null) {
			mOnDatePickListener.onDatePick(this, monthDay);
		}
	}

	/* show the month page with specified pager position and selected day */
	private void showMonth(int position, int selectedDay) {
		mIsChangedByUser = true;
		mAdapter.setSelectedDay(position, selectedDay);
		mPager.setCurrentItem(position, true);
	}


	public void setAccount(int schemeID, String schemeGroup) {
		this.schemeGroup = schemeGroup;
		this.schemeID = schemeID;
	}

	public int getSchemeID() {
		return schemeID;
	}

	public String getSchemeGroup() {
		return schemeGroup;
	}


	/**
	 * Show previous month page with selected day.
	 *
	 * @param selectedDay selected day
	 */
	protected void showPrevMonth(int selectedDay) {
		int position = mPager.getCurrentItem() - 1;
		showMonth(position, selectedDay);
	}

	/**
	 * Show next month page with selected day.
	 *
	 * @param selectedDay selected day
	 */
	protected void showNextMonth(int selectedDay) {
		int position = mPager.getCurrentItem() + 1;
		showMonth(position, selectedDay);
	}

	/**
	 * Show previous month view.
	 */
	public void showPrevMonth() {
		showPrevMonth(1);
	}

	/**
	 * Show next month view.
	 */
	public void showNextMonth() {
		showNextMonth(1);
	}

	/**
	 * Go to the month with specified year and month.
	 *
	 * @param year the specified year
	 * @param month the specified month
	 */
	public void goToMonth(int year, int month) {
		showMonth(mAdapter.getIndexOfMonth(year, month), 1);
	}

	/**
	 * Go to the month with specified year, month and day.
	 *
	 * @param year the specified year
	 * @param month the specified month
	 * @param day the specified day
	 */
	public void goToMonthDay(int year, int month, int day) {
		showMonth(mAdapter.getIndexOfMonth(year, month), day);
	}

	/**
	 * Go back to the month of today.
	 */
	public void backToToday() {
		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis());
		showMonth(mAdapter.getIndexOfCurrentMonth(), today.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Set the range of date.
	 *
	 * @param minYear min year
	 * @param maxYear max year
	 */
	public void setDateRange(int minYear, int maxYear) {
		Month min = new Month(minYear, 0, 1);
		Month max = new Month(maxYear, 11, 1);
		mAdapter.setDateRange(min, max);
	}


}
