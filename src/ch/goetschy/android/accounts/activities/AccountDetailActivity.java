package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountDetailActivity extends ListActivity {

	private TransactionsAdapter adapter;
	private Account account;
	private Spinner timeFilter;
	private Filter filter;
	private TextView intervalView;
	private View addFooter;
	private ImageButton previous;
	private ImageButton next;
	private TextView totalView;

	private LinearLayout navigator;

	boolean firstFill = true;

	private static final int FILTER_ACTIVITY = 10;

	private static final int DELETE_ID = 20;
	private static final int EDIT_ID = 30;
	private static final int MOVE_TO_ID = 40;

	private static final int DEFAULT_TIME_INTERVAL = Filter.MONTH;

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
		filter.setInterval(DEFAULT_TIME_INTERVAL);

		// listview
		this.setContentView(R.layout.activity_detail);
		ListView listView = getListView();
		listView.setDividerHeight(2);
		this.registerForContextMenu(listView); // register for context menu

		// get views
		navigator = (LinearLayout) findViewById(R.id.activity_detail_footer);
		previous = (ImageButton) findViewById(R.id.activity_detail_previous);
		next = (ImageButton) findViewById(R.id.activity_detail_next);
		timeFilter = (Spinner) findViewById(R.id.activity_detail_spinner);
		intervalView = (TextView) findViewById(R.id.activity_detail_interval);

		// total footer
		View totalFooter = getLayoutInflater().inflate(
				R.layout.activity_detail_total_footer, null);
		listView.addFooterView(totalFooter);
		totalView = (TextView) findViewById(R.id.activity_detail_total_amount);

		// ADD footer
		addFooter = getLayoutInflater().inflate(
				R.layout.activity_detail_add_footer, null);
		listView.addFooterView(addFooter);
		addFooter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeFooterColor(true);
				createTransaction();
			}
		});

		// first fill
		// fillData();
		// TODO optimize the filling of the data : not three times when creating
		// the activity !

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
						Log.w("accountDetail", "time filter selected = "
								+ position);
						filter.setInterval(position);
						updateNavigator();
						if (position == Filter.CUSTOM)
							setFilter();

						// if no filter, no navigation
						if (position == Filter.NONE) {
							enableNavigation(false);
						} else
							enableNavigation(true);

						fillData();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});

		// ***********************

	}

	//
	private void enableNavigation(boolean b) {
		filter.setDateFilter(b);
		updateNavigator();
	}

	// fill with the transactions
	private void fillData() {
		Log.w("accountDetail", "fill data");

		ArrayList<Transaction> transactions = account.getListTransactions(
				getContentResolver(), filter);

		if (transactions != null) {

			Log.w("accountDetail", "num of trans " + transactions.size());
			for (Transaction trans : transactions)
				Log.w("accountDetail", "trans " + trans.getName());

			// adapt filter if no trans
			if (firstFill) {
				Log.w("accountDetail", "first fill");
				if (transactions.size() == 0) {
					Log.w("accountDetail",
							"num of trans " + transactions.size());
					filter.setInterval(Filter.NONE);
					transactions = account.getListTransactions(
							getContentResolver(), filter);
				}
				firstFill = false;
			}

			// set adapter
			adapter = new TransactionsAdapter(this, transactions);
			this.setListAdapter(adapter);

		} else if (adapter != null)
			adapter.clear();

		// set amount of the selected transactions
		// tmp
		double total = 0;
		if (transactions != null) {
			for (Transaction i : transactions)
				total += i.getAmount();
		}
		// end tmp
		if (total < 0)
			totalView.setTextColor(Color.RED);
		else
			totalView.setTextColor(Color.GREEN);
		totalView.setText(TransactionsAdapter.amountFormat.format(total));

	}

	// when new transaction is clicked
	private void changeFooterColor(boolean dark) {
		if (dark)
			addFooter.setBackgroundColor(Color.rgb(210, 210, 210));
		else
			addFooter.setBackgroundColor(Color.rgb(220, 220, 220));
	}

	// set the text for the time interval
	private void setDateInterval() {
		intervalView.setText(Filter.millisToText(filter.getLowerBound())
				+ " - " + Filter.millisToText(filter.getUpperBound() - 1));
	}

	// update the time navigator
	private void updateNavigator() {
		if (filter.isDateFilter()) {
			// visible navigator
			navigator.setVisibility(View.VISIBLE);

			timeFilter.setSelection(filter.getInterval());
			setDateInterval();

			boolean custom = (filter.getInterval() == Filter.CUSTOM);
			next.setEnabled(!custom);
			previous.setEnabled(!custom);

			if (custom) {
				next.setVisibility(View.INVISIBLE);
				previous.setVisibility(View.INVISIBLE);
			} else {
				next.setVisibility(View.VISIBLE);
				previous.setVisibility(View.VISIBLE);
			}
		} else {
			// invisible navigator
			navigator.setVisibility(View.GONE);
		}
	}

	// create trans
	private void createTransaction() {
		Intent intent = new Intent(this, EditTransactionActivity.class);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ACCOUNT_ID_TYPE,
				account.getId());
		startActivity(intent);
	}

	// edit trans
	private void editTransaction(long id) {
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
			if (resultCode == Activity.RESULT_OK) {
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

	// CONTEXT MENU : DELETE and EDIT BUTTONs -----------

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE,
				R.string.activity_detail_delete);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.activity_detail_edit);
		menu.add(Menu.NONE, MOVE_TO_ID, Menu.NONE,
				R.string.activity_detail_move_to);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final Transaction actTransaction = new Transaction();

		// get id
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		actTransaction.setUri(Uri
				.parse(MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS + "/"
						+ info.id));

		// delete or edit
		switch (item.getItemId()) {
		case DELETE_ID:
			// show confirm dialog
			MyDialog.confirm(this, R.string.edit_account_delete_question, // TODO modify text
					new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							// if yes, delete
							actTransaction.delete(getContentResolver());
							fillData(); // and refresh
							return null;
						}
					}, null);
			return false;
		case EDIT_ID:
			editTransaction(info.id);
			return false;
		case MOVE_TO_ID:
			// TODO implement moving to an other account
			return false;
		}

		return super.onContextItemSelected(item);
	}

	// TRANSACTION DETAILS -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		editTransaction(id);

	}

	// ---------------------------------------------

	@Override
	protected void onResume() {
		Log.w("accountDetail", "on resume");
		fillData();
		changeFooterColor(false);
		updateNavigator();
		super.onResume();
	}
}
