package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AccountDetailActivity extends ListActivity {

	private TransactionsAdapter adapter;
	private Account account;
	private Spinner timeFilter;
	private Filter filter;

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
		ListView listView = getListView();
		listView.setDividerHeight(2);
		fillData();

		// get views
		ImageButton previous = (ImageButton) findViewById(R.id.activity_detail_previous);
		ImageButton next = (ImageButton) findViewById(R.id.activity_detail_next);
		timeFilter = (Spinner) findViewById(R.id.activity_detail_spinner);
		
		// init filter
		filter = new Filter();

		// previous
		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filter.previous();
			}
		});

		Log.w("detail", "after");

		// next
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filter.next();
			}
		});

		// spinner
		timeFilter
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});

	}

	private void fillData() {
		ArrayList<Transaction> transactions = account
				.getListTransactions(getContentResolver());
		if (transactions != null) {
			adapter = new TransactionsAdapter(this, transactions);
			this.setListAdapter(adapter);
		}
		else if (adapter != null)
			adapter.clear();
	}

	private void createTransaction() {
		Intent intent = new Intent(this, EditTransactionActivity.class);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE,
				account.getId());
		startActivity(intent);
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

	// TRANSACTION DETAILS -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, EditTransactionActivity.class);
		Uri transactionUri = Uri
				.parse(MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS + "/"
						+ id);
		Log.w("accountDetail", "transactionUri : " + transactionUri);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
				transactionUri);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE,
				account.getId());

		startActivity(intent);

	}

	// ---------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}
}
