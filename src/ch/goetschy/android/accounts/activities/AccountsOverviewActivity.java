package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;

import android.net.Uri;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountsOverviewActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int DELETE_ID = 10;
	private static final int EDIT_ID = 20;

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

		this.setListAdapter(adapter);
	}

	private void createAccount() {
		Intent intent = new Intent(this, EditAccountActivity.class);
		startActivity(intent);
	}

	// ADD BUTTON ------------------------

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

	// DELETE and EDIT BUTTONs -----------

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.account_overview_edit);
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE,
				R.string.account_overview_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		// get id
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Uri uri = Uri.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS + "/"
				+ info.id);
		
		// delete or edit
		switch (item.getItemId()) {
		case DELETE_ID:
			this.getContentResolver().delete(uri, null, null);
			fillData();
			return true;
		case EDIT_ID:
			Intent intent = new Intent(this, EditAccountActivity.class);
			intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE, uri);
			startActivity(intent);
		}
		
		return super.onContextItemSelected(item);
	}

	// ACCOUNT DETAILS -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, AccountDetailActivity.class);
		Uri todoUri = Uri.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS
				+ "/" + id);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE, todoUri);

		startActivity(intent);
	}

	// -----------------------------------

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
