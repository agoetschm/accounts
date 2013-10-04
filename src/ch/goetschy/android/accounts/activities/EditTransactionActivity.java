package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import android.widget.RadioGroup;
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
	private long mTimeInMillis;
	private RadioGroup inOrOutRadio;

	private Transaction transaction;
	private long parent_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_transaction);

		// get widgets
		mName = (EditText) findViewById(R.id.edit_transaction_name);
		mAmount = (EditText) findViewById(R.id.edit_transaction_amount);
		mType = (Spinner) findViewById(R.id.edit_transaction_type);
		mDateButton = (Button) findViewById(R.id.edit_transaction_date_button);
		mDateTextView = (TextView) findViewById(R.id.edit_transaction_date);
		inOrOutRadio = (RadioGroup) findViewById(R.id.edit_transaction_radio);
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

			if (uri != null)
				transaction.setUri(uri);

			parent_id = extras
					.getLong(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE);
		}

		// radio
		inOrOutRadio.check(R.id.edit_transaction_credit);

		// type spinner
		ArrayList<Type> typesList = Type.getTypes(getContentResolver());
		if (typesList != null) {
			ArrayAdapter<Type> typeAdapter = new TypesAdapter(this, typesList);// ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
			//
			// int typesListSize = typesList.size();
			// for (int i = 0; i < typesListSize; i++) {
			// typeAdapter.add(typesList.get(i).getName());
			// }
			typeAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mType.setAdapter(typeAdapter);
		}

		Log.w("editTransaction", "2");

		// date
		mDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dateFragment = new DatePickerFragment();
				dateFragment.setParent(EditTransactionActivity.this);
				dateFragment.setMillis(mTimeInMillis);
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

		// fill values from DB
		// fillData();
		if (transaction.loadFromDB(getContentResolver())) {
			mName.setText(transaction.getName());

			// amount
			double dAmount = transaction.getAmount();
			if (dAmount < 0) {
				inOrOutRadio.check(R.id.edit_transaction_debit);
				dAmount *= -1;
			}
			mAmount.setText(String.valueOf(dAmount));

			// set default type
			long typeId = transaction.getType().getId();
			int listSize = typesList.size();
			for (int i = 0; i < listSize; i++) {
				if (typesList.get(i).getId() == typeId)
					mType.setSelection(i);
			}
			// set date
			mTimeInMillis = transaction.getDate();

		}

		// init default date
		setDate();
	}

	// private void fillData() {
	// if (transaction.loadFromDB(getContentResolver())) {
	// mName.setText(transaction.getName());
	// mAmount.setText(String.valueOf(transaction.getAmount()));
	// //mType
	//
	// }
	// }

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
		// name
		String name = mName.getText().toString();
		if (name.length() == 0)
			return;
		
		// amount
		String amount = mAmount.getText().toString();
		if (!validAmount(amount))
			return;
		double dAmount = Double.parseDouble(amount);
		if (inOrOutRadio.getCheckedRadioButtonId() == R.id.edit_transaction_debit) // neg
			dAmount *= -1;
		
		amount = String.valueOf(dAmount);

		Log.w("editTransaction", "save " + name);
		transaction.setName(name);
		transaction.setAmount(Double.parseDouble(amount));
		transaction.setDate(mTimeInMillis);
		transaction.getType().setId(mType.getSelectedItemId());
		transaction.setParent(new Account(parent_id));
		transaction.saveInDB(getContentResolver());
	}

	public void setDate() {
		final Calendar c = Calendar.getInstance();
		if (mTimeInMillis != 0)
			c.setTimeInMillis(mTimeInMillis);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		mDateTextView.setText(day + "/" + month + "/" + year);
		if (mTimeInMillis == 0)
			mTimeInMillis = c.getTimeInMillis();
	}

	public void setDate(int year, int month, int day) {
		mDateTextView.setText(day + "/" + month + "/" + year);
		final Calendar c = Calendar.getInstance();
		c.set(year, month, day);

		mTimeInMillis = c.getTimeInMillis();
	}

}
