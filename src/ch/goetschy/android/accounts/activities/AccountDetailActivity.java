package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;
import java.util.Calendar;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AccountDetailActivity extends ListActivity {

	private TransactionsAdapter adapter;
	private Account account;
	private Spinner timeFilter;
	private Filter filter;
	private TextView intervalView;
	private View footer;

	private static final int FILTER_ACTIVITY = 10;

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

		// init filter
		filter = new Filter();
		filter.setLowerBound(Calendar.getInstance().getTimeInMillis());

		// listview
		this.setContentView(R.layout.activity_detail);
		ListView listView = getListView();
		listView.setDividerHeight(2);

		// get views
		ImageButton previous = (ImageButton) findViewById(R.id.activity_detail_previous);
		ImageButton next = (ImageButton) findViewById(R.id.activity_detail_next);
		timeFilter = (Spinner) findViewById(R.id.activity_detail_spinner);
		intervalView = (TextView) findViewById(R.id.activity_detail_interval);

		// ADD footer
		footer = getLayoutInflater().inflate(R.layout.activity_detail_footer,
				null);
		listView.addFooterView(footer);
		footer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeFooterColor(true);
				createTransaction();
			}
		});

		// LISTENERS ************

		// previous
		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filter.previous();
				setDateInterval();
				fillData();
			}
		});

		// next
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filter.next();
				setDateInterval();
				fillData();
			}
		});

		// spinner
		timeFilter
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						filter.setInterval(timeFilter.getSelectedItemPosition());
						setDateInterval();
						fillData();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});
		timeFilter.setSelection(Filter.MONTH);

		// ***********************

	}

	// fill with the transactions
	private void fillData() {
		ArrayList<Transaction> transactions = account.getListTransactions(
				getContentResolver(), filter);

		if (transactions != null) {
			adapter = new TransactionsAdapter(this, transactions);
			this.setListAdapter(adapter);
		} else if (adapter != null)
			adapter.clear();
	}

	// when new transaction is clicked
	private void changeFooterColor(boolean dark) {
		if (dark)
			footer.setBackgroundColor(Color.rgb(210, 210, 210));
		else
			footer.setBackgroundColor(Color.rgb(220, 220, 220));
	}

	// set the text for the time interval
	private void setDateInterval() {
		intervalView.setText(Filter.millisToText(filter.getLowerBound())
				+ " - " + Filter.millisToText(filter.getUpperBound() - 1));
	}

	// start
	private void createTransaction() {
		Intent intent = new Intent(this, EditTransactionActivity.class);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE,
				account.getId());
		startActivity(intent);
	}

	// open the filter dialog
	private void setFilter() {
		// tmp
		Intent intent = new Intent(this, FilterActivity.class);
		intent.putExtra(Filter.class.toString(), filter);
		startActivityForResult(intent, FILTER_ACTIVITY);
		// end tmp
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FILTER_ACTIVITY:
			if(resultCode == Activity.RESULT_OK){
				filter = (Filter) data.getSerializableExtra(Filter.class
						.toString());
			}
		}
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
		if (BuildConfig.DEBUG)
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
		fillData();
		changeFooterColor(false);
		super.onResume();
	}
}
