package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.R.layout;
import ch.goetschy.android.accounts.R.menu;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class AccountsOverviewActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	// test
	private ArrayAdapter<String> adapter;
	private ArrayList<String> list;

	// end test

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		fillData();
		this.registerForContextMenu(getListView());
	}

	private void fillData() {
		// test adapter
		String[] values = new String[] { "one", "two", "three" };
		list = new ArrayList<String>();
		for (int i = 0; i < values.length; i++) {
			list.add(values[i]);
		}
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		// end test
		this.getListView().setAdapter(adapter);
	}

	private void createAccount() {
		// test
		adapter.add("new one");
		// end test

		ContentValues values = new ContentValues();
		values.put(AccountsTable.COLUMN_AMOUNT, "amount");
		values.put(AccountsTable.COLUMN_NAME, "name");
		values.put(AccountsTable.COLUMN_ORDER, 1);
		values.put(AccountsTable.COLUMN_PARENT, 0);

		this.getContentResolver().insert(
				MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			createAccount();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}
