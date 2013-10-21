package ch.goetschy.android.accounts.objects;

import java.util.Calendar;

import ch.goetschy.android.accounts.BuildConfig;

import android.util.Log;

public class Filter {
	public static final int DAY = 0;
	public static final int WEEK = 1;
	public static final int MONTH = 2;
	public static final int YEAR = 3;

	public static String millisToText(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return "" + (c.get(Calendar.YEAR) % 100) + "/"
				+ (c.get(Calendar.MONTH) + 1) + "/"
				+ c.get(Calendar.DAY_OF_MONTH);/* month begin with 0 */
	}

	private long upperBound;
	private long lowerBound;
	private int interval;
	private long typeId;

	public Filter() {
		upperBound = 0;
		lowerBound = 0;
		interval = 0;
		typeId = 0;
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

	public long getTypeId() {
		return typeId;
	}

	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;

		if (BuildConfig.DEBUG) {
			Log.w("filter", "begin setInterval");
			Log.w("filter", "interval : " + interval);
		}

		Calendar c = getCalendar();

		if (BuildConfig.DEBUG)
			Log.w("filter", "now : " + c.getTimeInMillis());

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
		default: // month
			c.set(Calendar.DAY_OF_MONTH, 0);
			lowerBound = c.getTimeInMillis();
			c.add(Calendar.MONTH, 1);
			upperBound = c.getTimeInMillis();
		}
		Log.w("filter", "end setInterval");
	}

	public boolean isSelected(long date) {
		return (date >= lowerBound && date < upperBound);
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

}
