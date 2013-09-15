package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class AccountsOverviewActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		this.getListView().setDividerHeight(2);
		fillData();
		this.registerForContextMenu(getListView());
	}

	private void fillData() {

		String[] from = new String[] { AccountsTable.COLUMN_NAME,
				AccountsTable.COLUMN_AMOUNT };
		int[] to = new int[] { R.id.activity_overview_name,
				R.id.activity_overview_amount };

		this.getLoaderManager().initLoader(0, null, this);
		adapter = new SimpleCursorAdapter(this,
				R.layout.activity_overview_item, null, from, to, 0);
		// adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
		//
		// @Override
		// public boolean setViewValue(View view, Cursor cursor,
		// int columnIndex) {
		// if (columnIndex == 2)
		// ((TextView) view).setText(cursor.getString(columnIndex));
		// else if (columnIndex == 1)
		// ((TextView) view).setText(cursor.getInt(columnIndex));
		// return true;
		// }
		//
		// });

		this.setListAdapter(adapter);
	}

	private void createAccount() {
		// test
		// adapter.add("new one");

		// ContentValues values = new ContentValues();
		// values.put(AccountsTable.COLUMN_AMOUNT, 3);
		// values.put(AccountsTable.COLUMN_NAME, "name");
		// values.put(AccountsTable.COLUMN_ORDER, 1);
		// values.put(AccountsTable.COLUMN_PARENT, 0);
		//
		// this.getContentResolver().insert(
		// MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
		// end test

		Intent intent = new Intent(this, EditAccountActivity.class);
		Log.w("accountOverview", "startintent");
		startActivity(intent);

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
		String[] projection = { AccountsTable.COLUMN_ID,
				AccountsTable.COLUMN_NAME, AccountsTable.COLUMN_AMOUNT };
		CursorLoader cursorLoader = new CursorLoader(this,
				MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, projection,
				null, null, null);
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
