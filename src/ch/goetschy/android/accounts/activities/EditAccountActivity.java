package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditAccountActivity extends Activity {

	private EditText mName;
	private Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_account);

		mName = (EditText) findViewById(R.id.edit_account_name);
		Button confirmButton = (Button) findViewById(R.id.edit_account_confirm);
		Button deleteButton = (Button) findViewById(R.id.edit_account_delete);

		// data from parent activity
		Bundle extras = getIntent().getExtras();

		// account object
		account = new Account();

		// if saved instance
		account.setUri((savedInstanceState == null) ? null
				: (Uri) savedInstanceState
						.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
		// edit or add
		if (extras != null) {
			account.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			fillData();
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

		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// show confirm dialog
				new AlertDialog.Builder(EditAccountActivity.this)
						.setMessage(R.string.edit_account_delete_question)
						.setCancelable(false)
						.setPositiveButton(R.string.edit_account_yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if yes, delete
										setResult(RESULT_CANCELED);
										if (account.getUri() == null) {
											finish();
										} else {
											account.delete(getContentResolver());
											finish();
										}
									}
								})
						.setNegativeButton(R.string.edit_account_no, null)
						.show();

			}
		});
	}

	private void fillData() {
		if (account.loadFromDB(getContentResolver()))
			mName.setText(account.getName());
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
				account.getUri());
	}

	private void save() {
		String name = mName.getText().toString();

		if (name.length() == 0)
			return;

		account.setName(name);
		account.saveInDB(getContentResolver());
	}
}
