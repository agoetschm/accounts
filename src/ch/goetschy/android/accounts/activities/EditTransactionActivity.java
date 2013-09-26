package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;
import java.util.Calendar;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.DatePickerDialog;

public class EditTransactionActivity extends Activity {

	private EditText mName;
	private EditText mAmount;
	private Spinner mType;
	private Button mDateButton;
	private TextView mDateTextView;
	private Transaction transaction;
	private long parent_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_transaction);

		mName = (EditText) findViewById(R.id.edit_transaction_name);
		mAmount = (EditText) findViewById(R.id.edit_transaction_amount);
		mType = (Spinner) findViewById(R.id.edit_transaction_type);
		mDateButton = (Button) findViewById(R.id.edit_transaction_date_button);
		mDateTextView = (TextView) findViewById(R.id.edit_transaction_date);
		Button confirmButton = (Button) findViewById(R.id.edit_transaction_confirm);
		Button deleteButton = (Button) findViewById(R.id.edit_transaction_delete);

		// data from parent activity
		Bundle extras = getIntent().getExtras();

		// account object
		transaction = new Transaction();

		// if saved instance
		if (savedInstanceState == null) {
			transaction.setUri(null);
			parent_id = -1;
		} else {
			transaction
					.setUri((Uri) savedInstanceState
							.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			parent_id = savedInstanceState
					.getLong(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE);
		}

		// edit or add
		if (extras != null) {
			Uri uri = extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE);
			if (uri != null) {
				transaction.setUri(uri);
				fillData();
			}
			parent_id = extras
					.getLong(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE);
		}

		// type spinner
		ArrayList<Type> typesList = new ArrayList<Type>();// Type.getTypes(getContentResolver());
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		// test
		typesList.add(new Type(1, "type1"));
		typesList.add(new Type(2, "type2"));
		typesList.add(new Type(3, "type3"));
		// end test
		int typesListSize = typesList.size();
		for (int i = 0; i < typesListSize; i++) {
			typeAdapter.add(typesList.get(i).getName());
		}
		typeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mType.setAdapter(typeAdapter);

		
		// date
		mDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dateFragment = new DatePickerFragment();
				dateFragment.setTextView(mDateTextView);
			    dateFragment.show(getFragmentManager(), "datePicker");
			}
		});
		
		
		// confirm button
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mName.getText().toString())) {
					makeToast(getString(R.string.edit_transaction_name_toast));
				} else if (!validAmount(mAmount.getText().toString())) {
					makeToast(getString(R.string.edit_transaction_amount_toast));
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		});

		// delete button
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				if (transaction.getUri() == null) {
					finish();
				} else {
					transaction.delete(getContentResolver());
					finish();
				}
			}
		});
	}

	private void fillData() {
		if (transaction.loadFromDB(getContentResolver())) {
			mName.setText(transaction.getName());
			mAmount.setText(String.valueOf(transaction.getAmount()));
		}
	}

	private boolean validAmount(String amount) {
		return amount.matches("-?\\d+(\\.\\d+)?");
	}

	private void makeToast(String msg) {
		Toast.makeText(EditTransactionActivity.this, msg, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	protected void onPause() {
		save();
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		save();
		outState.putParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
				transaction.getUri());
		outState.putLong(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE,
				parent_id);
	}

	private void save() {
		String name = mName.getText().toString();
		String amount = mAmount.getText().toString();

		if (name.length() == 0 || !validAmount(amount))
			return;
		Log.w("editTransaction", "save " + name);
		transaction.setName(name);
		transaction.setAmount(Integer.parseInt(amount));
		transaction.setParent(new Account(parent_id));
		transaction.saveInDB(getContentResolver());
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		private TextView textView;
		
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
	        final Calendar c = Calendar.getInstance();
	        int year = c.get(Calendar.YEAR);
	        int month = c.get(Calendar.MONTH);
	        int day = c.get(Calendar.DAY_OF_MONTH);

	        // Create a new instance of DatePickerDialog and return it
	        return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			textView.setText(dayOfMonth + "." + monthOfYear + "." + year);
			
		}


		public void setTextView(TextView textView) {
			this.textView = textView;
		}

		
	}

}
