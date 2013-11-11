package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class FilterActivity extends Activity {

	private Filter filter;
	private Spinner dateSpinner;
	private ArrayList<CheckBox> typeBoxList;
	private ArrayList<Type> typesList;

	private CheckBox dateCheckbox;
	private CheckBox typeCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_filter);

		Button confirmButton = (Button) findViewById(R.id.activity_filter_confirm);
		Button cancelButton = (Button) findViewById(R.id.activity_filter_cancel);

		dateCheckbox = (CheckBox) findViewById(R.id.activity_filter_date_box);
		typeCheckbox = (CheckBox) findViewById(R.id.activity_filter_type_box);

		dateSpinner = (Spinner) findViewById(R.id.activity_filter_interval_spinner);

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

		// confirm
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				Intent resultIntent = new Intent();
				resultIntent.putExtra(Filter.class.toString(), filter);
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});

		// cancel
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		// date filter -------------
		dateCheckbox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton box,
							boolean isChecked) {
						dateSpinner.setEnabled(isChecked);
					}

				});

		dateSpinner.setEnabled(dateCheckbox.isChecked()); // init

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

	private void enableTypesCheckBoxes(boolean enabled) {
		for (CheckBox box : typeBoxList)
			box.setEnabled(enabled);
	}

	private void fillData() {
		dateCheckbox.setChecked(filter.isDateFilter());

		// TODO

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


	// save in filter object
	private void save() {

		// enable/disable date-type filter
		filter.setDateFilter(dateCheckbox.isChecked());
		filter.setTypeFilter(typeCheckbox.isChecked());

		if (filter.isDateFilter()) {
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
}
