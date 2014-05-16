package ch.goetschy.android.accounts.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import ch.goetschy.android.accounts.BuildConfig;

import android.content.ContentResolver;
import android.util.Log;

public class Filter implements Serializable {
	/**
	 * This class allows to filter transactions by date and type
	 */
	private static final long serialVersionUID = 1L;
	public static final int DAY = 0;
	public static final int WEEK = 1;
	public static final int MONTH = 2;
	public static final int YEAR = 3;
	public static final int CUSTOM = 4;
	public static final int NONE = 5;

	public static String millisToText(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return "" + (c.get(Calendar.YEAR) % 100) + "/"
				+ (c.get(Calendar.MONTH) + 1) + "/"
				+ c.get(Calendar.DAY_OF_MONTH);/* month begin with 0 */
	}

	private boolean dateFilter;
	private long upperBound;
	private long lowerBound;
	private int interval;

	private boolean typeFilter;
	private ArrayList<Type> typesList;

	public Filter() {
		setDateFilter(true);
		upperBound = 0;
		lowerBound = 0;
		interval = 2;

		setTypeFilter(false);
		setTypesList(new ArrayList<Type>());
	}

	public long getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(long upperBound) {
		this.upperBound = upperBound;
	}

	public long getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(long lowerBound) {
		this.lowerBound = lowerBound;
	}

	public boolean isDateFilter() {
		return dateFilter;
	}

	public void setDateFilter(boolean dateFilter) {
		this.dateFilter = dateFilter;
	}

	public boolean isTypeFilter() {
		return typeFilter;
	}

	public void setTypeFilter(boolean typeFilter) {
		this.typeFilter = typeFilter;
	}

	public ArrayList<Type> getTypesList() {
		return typesList;
	}

	public ArrayList<Type> getTypesList(ContentResolver contentResolver) {
		if (this.isTypeFilter())
			return typesList;
		else
			return Type.getTypes(contentResolver);
		
	}

	public void setTypesList(ArrayList<Type> typesList) {
		this.typesList = typesList;
	}

	public void addType(Type newType) {
		this.typesList.add(newType);
	}

	public void clearTypesList() {
		this.typesList.clear();
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {

		if (BuildConfig.DEBUG) {
			Log.w("filter", "begin setInterval");
			Log.w("filter", "interval : " + interval);
		}

		// for none filter
		if (interval == NONE)
			this.setDateFilter(false);
		else {
			this.interval = interval;
			computeUpperBound(); // update the upper bound
		}

		if (BuildConfig.DEBUG)
			Log.w("filter", "end setInterval");
	}

	public void computeUpperBound() {

		Calendar c = getCalendar();

		switch (interval) {
		case DAY:
			lowerBound = c.getTimeInMillis();
			c.add(Calendar.DAY_OF_MONTH, 1);
			upperBound = c.getTimeInMillis();
			break;
		case WEEK:
			c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
			lowerBound = c.getTimeInMillis();
			c.add(Calendar.WEEK_OF_MONTH, 1);
			upperBound = c.getTimeInMillis();
			break;
		case MONTH:
			c.set(Calendar.DAY_OF_MONTH, 1);
			lowerBound = c.getTimeInMillis();
			c.add(Calendar.MONTH, 1);
			upperBound = c.getTimeInMillis();
			break;
		case YEAR:
			c.set(Calendar.DAY_OF_YEAR, 1);
			lowerBound = c.getTimeInMillis();
			c.add(Calendar.YEAR, 1);
			upperBound = c.getTimeInMillis();
			break;
		default: // custom
		}
	}

	public boolean isSelected(Transaction trans) {
		boolean dateOK = !dateFilter // not a date filter
				|| (dateFilter && isDateSelected(trans.getDate()));
		// or date is selected

		boolean typeOK = !typeFilter
				|| (typeFilter && isTypeSelected(trans.getType()));

		return dateOK && typeOK;
	}

	private boolean isDateSelected(long date) {
		return (date >= lowerBound && date < upperBound);
	}

	public boolean isTypeSelected(Type type) {
		long typeId = type.getId();
		for (Type i : typesList) {
			if (i.getId() == typeId)
				return true;
		}
		return false;
	}

	public void previous() {
		if (BuildConfig.DEBUG)
			Log.w("filter", "previous");

		upperBound = lowerBound;

		Calendar c = getCalendar();
		int calInterval = getCalendarInterval();
		c.add(calInterval, -1);
		lowerBound = c.getTimeInMillis();
	}

	public void next() {
		if (BuildConfig.DEBUG)
			Log.w("filter", "next");

		lowerBound = upperBound;

		Calendar c = getCalendar();
		int calInterval = getCalendarInterval();
		c.add(calInterval, 1);
		upperBound = c.getTimeInMillis();
	}

	private int getCalendarInterval() {
		switch (interval) {
		case DAY:
			return Calendar.DAY_OF_MONTH;
		case WEEK:
			return Calendar.WEEK_OF_MONTH;
		case MONTH:
			return Calendar.MONTH;
		case YEAR:
			return Calendar.YEAR;
		default: // month
			return Calendar.MONTH;
		}
	}

	private Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTimeInMillis(lowerBound);
		// beginning of day
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.clear(Calendar.MINUTE);
		c.clear(Calendar.SECOND);
		c.clear(Calendar.MILLISECOND);

		return c;
	}

	/*
	 * Adapts the filter to display transactions in the filter (with the last),
	 * but not too many of them.
	 */
	public void adaptToAccount(Account account) {
		// TODO
		setInterval(Filter.NONE);
	}

}
