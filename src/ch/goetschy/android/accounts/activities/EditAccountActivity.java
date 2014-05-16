package ch.goetschy.android.accounts.activities;

import java.util.concurrent.Callable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditAccountActivity extends SherlockActivity {

	private EditText mName;
	private Account account;

	private boolean mDelete = false;
	private boolean editing;

	private String baseName = null;

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
			editing = true;
		} else
			editing = false;

		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mName.getText().toString())) {
					makeToast("Please enter a name");
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
				MyDialog.confirm(EditAccountActivity.this,
						R.string.edit_account_delete_question,
						new Callable<Object>() {
							@Override
							public Object call() throws Exception {
								// if yes, delete
								mDelete = true; // do not save
								setResult(RESULT_CANCELED);
								if (account.getUri() == null) {
									finish();
								} else {
									account.delete(getContentResolver());
									finish();
								}
								return null;
							}
						}, null);

			}
		});

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void fillData() {
		if (account.loadFromDB(getContentResolver())) {
			mName.setText(account.getName());

			// detecting name changing
			baseName = account.getName();
		}
	}
	

	private void makeToast(String message) {
		Toast.makeText(EditAccountActivity.this, message, Toast.LENGTH_LONG)
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
				account.getUri());
	}

	private void save() {
		if (!mDelete) {
			String name = mName.getText().toString();

			if (name.length() == 0)
				return;

			// complexe condition
			boolean conditionAlreadyExits = false;
			int occurences = Account.accountNameExists(getContentResolver(),
					name);
			if (occurences > 0) {
				if (editing && name.equals(baseName)) { // editing + no change
														// -> already exists
														// once in db
					if (occurences > 1) // more than once
						conditionAlreadyExits = true;
				} else
					conditionAlreadyExits = true;
			}
			if (conditionAlreadyExits) { // if name exists in DB
				makeToast("This account name already exists");
				return; // do not save
			}

			account.setName(name);
			account.saveInDB(getContentResolver());
		}
	}

	// RETURN IN ACTION BAR ------------------------
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

}
