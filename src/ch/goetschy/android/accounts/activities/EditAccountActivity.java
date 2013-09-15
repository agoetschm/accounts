package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditAccountActivity extends Activity {

	private EditText mName;

	private Uri accountUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_account);

		mName = (EditText) findViewById(R.id.edit_account_name);
		Button confirmButton = (Button) findViewById(R.id.edit_account_confirm);

		Bundle extras = getIntent().getExtras();

		// if saved instance
		accountUri = (savedInstanceState == null) ? null
				: (Uri) savedInstanceState
						.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE);
		// edit or add
		if (extras != null) {
			accountUri = extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE);
			fillData(accountUri);
		}

		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mName.getText().toString())) {
					makeToast();
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		});
	}

	private void fillData(Uri uri) {
		String[] projection = { AccountsTable.COLUMN_NAME };
		Cursor cursor = this.getContentResolver().query(uri, projection, null,
				null, null);
		
		if (cursor.moveToFirst()) {

			mName.setText(cursor.getString(cursor
					.getColumnIndexOrThrow(AccountsTable.COLUMN_NAME)));
			cursor.close();
		}
	}

	private void makeToast() {
		Toast.makeText(EditAccountActivity.this, "Please enter a name",
				Toast.LENGTH_LONG).show();
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
				accountUri);
	}

	private void save() {
		String name = mName.getText().toString();

		if (name.length() == 0)
			return;

		ContentValues values = new ContentValues();
		values.put(AccountsTable.COLUMN_NAME, name);

		if (accountUri == null) { 	// insert new account
			values.put(AccountsTable.COLUMN_AMOUNT, 0);
			values.put(AccountsTable.COLUMN_ORDER, 0);
			values.put(AccountsTable.COLUMN_PARENT, 0);

			this.getContentResolver().insert(
					MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
		} else { 					// update account
			this.getContentResolver().update(accountUri, values, null, null);
		}
	}
}
