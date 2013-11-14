package ch.goetschy.android.accounts.activities;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	private long time = 0;
	private DatePickerListener parent;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		parent.setDate(year, monthOfYear, dayOfMonth);
	}
	
	public void setParent(DatePickerListener parent){
		this.parent = parent;
	}
	
	public void setMillis(long time) {
		this.time = time;
	}

}