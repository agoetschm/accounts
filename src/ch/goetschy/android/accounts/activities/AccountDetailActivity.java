package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.objects.Account;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class AccountDetailActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private TransactionsAdapter adapter;
	private Account account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// data from parent activity
		account = new Account();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			account.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			account.loadFromDB(getContentResolver());
			this.setTitle(account.getName());
		} else {
			Toast.makeText(AccountDetailActivity.this, "No uri in bundle",
					Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// listview
		this.setContentView(R.layout.activity_detail);
		this.getListView().setDividerHeight(2);
		fillData();

	}

	private void fillData() {
		// TODO Auto-generated method stub

	}
	

	private void createTransaction() {
		// TODO Auto-generated method stub
		
	}
	
	private void setFilter() {
		// TODO Auto-generated method stub
		
	}

	// ADD and FILTER BUTTON ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_account_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_detail_add:
			createTransaction();
			return true;
		case R.id.menu_detail_filter:
			setFilter();
			return true; 
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	// ---------------------------------------------


	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { TransactionTable.COLUMN_ID,
				TransactionTable.COLUMN_NAME, TransactionTable.COLUMN_AMOUNT,
				TransactionTable.COLUMN_TYPE, TransactionTable.COLUMN_DATE };
		CursorLoader cursorLoader = new CursorLoader(this,
				MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, projection,
				"parent = " + account.getName(), null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

}
