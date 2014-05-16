package ch.goetschy.android.accounts.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Type;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class FilterActivity extends SherlockFragmentActivity implements
		DatePickerListener, ToolTipView.OnToolTipViewClickedListener {

	static final private int LOWER = 1;
	static final private int UPPER = 2;

	private int actualBound = LOWER;

	private Filter filter;
	private Spinner dateSpinner;
	private ArrayList<CheckBox> typeBoxList;
	private ArrayList<Type> typesList;

	private Button lowerBound;
	private Button upperBound;

	private CheckBox dateCheckbox;
	private CheckBox typeCheckbox;

	private ToolTipView mTooltipDateFilter = null;
	private ToolTipView mTooltipInterval = null;
	private ToolTipView mTooltipTypesFilter = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_filter);

		Button confirmButton = (Button) findViewById(R.id.activity_filter_confirm);
		Button cancelButton = (Button) findViewById(R.id.activity_filter_cancel);

		dateCheckbox = (CheckBox) findViewById(R.id.activity_filter_date_box);
		typeCheckbox = (CheckBox) findViewById(R.id.activity_filter_type_box);

		dateSpinner = (Spinner) findViewById(R.id.activity_filter_interval_spinner);

		lowerBound = (Button) findViewById(R.id.activity_filter_bound1);
		upperBound = (Button) findViewById(R.id.activity_filter_bound2);

		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_filter_tooltiplayout);

		// data from parent activity
		Bundle extras = getIntent().getExtras();

		if (extras != null) // get filter
			filter = (Filter) extras.getSerializable(Filter.class.toString());
		else {
			Toast.makeText(FilterActivity.this, "No filter in bundle",
					Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// force overflow
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
		}

		// confirm
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(true);
			}
		});

		// cancel
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(false);
			}
		});

		// date filter -------------
		dateCheckbox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton box,
							boolean isChecked) {
						// if before was none filter
						if (dateSpinner.getSelectedItemPosition() == Filter.NONE
								&& isChecked)
							dateSpinner.setSelection(Filter.CUSTOM);

						dateSpinner.setEnabled(isChecked);
						lowerBound.setEnabled(isChecked);

						upperBound.setEnabled(isChecked
								&& dateSpinner.getSelectedItemPosition() == Filter.CUSTOM);
						// upper bound only if custom
					}

				});

		dateSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position == Filter.NONE) { // disable date filter
							dateCheckbox.setChecked(false);
							dateSpinner.setEnabled(false);
							lowerBound.setEnabled(false);
							upperBound.setEnabled(false);
						} else {
							filter.setInterval(position);
							setBounds();

							// upper bound only if custom
							upperBound.setEnabled(position == Filter.CUSTOM);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});

		dateSpinner.setEnabled(dateCheckbox.isChecked()); // init

		// date buttons
		lowerBound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actualBound = LOWER;
				DatePickerFragment dateFragment = new DatePickerFragment();
				dateFragment.setParent(FilterActivity.this);
				dateFragment.setMillis(filter.getLowerBound());
				dateFragment.show(getSupportFragmentManager(), "datePicker");
			}
		});

		upperBound.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				actualBound = UPPER;
				DatePickerFragment dateFragment = new DatePickerFragment();
				dateFragment.setParent(FilterActivity.this);
				dateFragment.setMillis(filter.getUpperBound());
				dateFragment.show(getSupportFragmentManager(), "datePicker");
			}
		});

		// type filter -------------

		typeBoxList = new ArrayList<CheckBox>();
		LinearLayout checkBoxGroup = (LinearLayout) findViewById(R.id.activity_filter_checkbox_list);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		typesList = Type.getTypes(getContentResolver());
		int nbrTypes = typesList.size();

		for (int i = 0; i < nbrTypes; i++) {
			CheckBox newBox = (CheckBox) inflater.inflate(
					R.layout.activity_filter_checkbox, null, false);
			newBox.setText(typesList.get(i).getName());
			typeBoxList.add(newBox);
			checkBoxGroup.addView(newBox);
		}

		typeCheckbox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton box,
							boolean isChecked) {
						enableTypesCheckBoxes(isChecked);
					}

				});

		enableTypesCheckBoxes(typeCheckbox.isChecked()); // init

		// -------------------------

		// fill with filter data
		fillData();
	}

	/*
	 * finish with saving or not
	 */
	public void finish(boolean save) {
		if (save) {
			save();
			Intent resultIntent = new Intent();
			resultIntent.putExtra(Filter.class.toString(), filter);
			setResult(RESULT_OK, resultIntent);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	/*
	 * update the text in the bound views
	 */
	public void setBounds() {
		lowerBound.setText(Filter.millisToText(filter.getLowerBound()));
		upperBound.setText(Filter.millisToText(filter.getUpperBound() - 1));
	}

	/*
	 * enable or disable all the types checkboxes
	 */
	private void enableTypesCheckBoxes(boolean enabled) {
		for (CheckBox box : typeBoxList)
			box.setEnabled(enabled);
	}

	/*
	 * update the displayed informations
	 */
	private void fillData() {
		dateCheckbox.setChecked(filter.isDateFilter());

		dateSpinner.setSelection(filter.getInterval());
		setBounds();

		typeCheckbox.setChecked(filter.isTypeFilter());
		int size = typesList.size();
		for (int i = 0; i < size; i++) {
			typeBoxList.get(i).setChecked(
					filter.isTypeSelected(typesList.get(i)));
		}
	}

	private void makeToast(String msg) {
		Toast.makeText(FilterActivity.this, msg, Toast.LENGTH_LONG).show();
	}

	// ACTION BAR ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_filter, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish(true);
			return true;
		case R.id.menu_filter_help:
			if (mTooltipDateFilter == null && this.mTooltipTypesFilter == null
					&& this.mTooltipInterval == null)
				nextTooltip(null);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	// save in filter object
	private void save() {

		// enable/disable date-type filter
		filter.setDateFilter(dateCheckbox.isChecked());
		filter.setTypeFilter(typeCheckbox.isChecked());

		if (filter.isTypeFilter()) {
			// clear existing list
			filter.clearTypesList();

			// add checked types to the filter
			int size = typeBoxList.size();
			for (int i = 0; i < size; i++) {
				if (typeBoxList.get(i).isChecked())
					filter.addType(typesList.get(i));
			}
		}
	}

	@Override
	public void setDate(int year, int month, int day) {
		final Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		if (actualBound == LOWER) {
			filter.setLowerBound(c.getTimeInMillis());
			filter.computeUpperBound();
		} else {
			filter.setUpperBound(c.getTimeInMillis());
		}

		setBounds();
	}

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addDateFilterTooltip();
		} else if (toolTipView == mTooltipDateFilter) {
			mTooltipDateFilter = null;
			addIntervalTooltip();
		} else if (toolTipView == mTooltipInterval) {
			mTooltipInterval = null;
			addTypesFilterTooltip();
		} else if (toolTipView == mTooltipTypesFilter) {
			mTooltipTypesFilter = null;
		}
	}

	private void addDateFilterTooltip() {
		mTooltipDateFilter = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Enable or disable the \ntime selection.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.activity_filter_date_box));
		mTooltipDateFilter.setOnToolTipViewClickedListener(this);
	}

	private void addIntervalTooltip() {
		mTooltipInterval = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Select the time interval in which you\n want to see the transactions.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.activity_filter_interval_spinner));
		mTooltipInterval.setOnToolTipViewClickedListener(this);
	}

	private void addTypesFilterTooltip() {
		mTooltipTypesFilter = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Enable or disable the type selection. Only\nthe transactions of the selected types\n will be displayed.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.activity_filter_type_box));
		mTooltipTypesFilter.setOnToolTipViewClickedListener(this);
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}
}
