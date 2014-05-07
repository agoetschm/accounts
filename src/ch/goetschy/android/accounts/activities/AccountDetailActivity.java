package ch.goetschy.android.accounts.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import org.opencv.core.Mat;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.engine.DetectCoinsTask;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountDetailActivity extends SherlockListActivity implements
		ToolTipView.OnToolTipViewClickedListener {

	private TransactionsAdapter mAdapter;
	private Account mAccount;
	private Spinner timeFilter;
	private Filter mFilter;
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

	// default interval for the filter
	private static final int DEFAULT_TIME_INTERVAL = Filter.MONTH;

	// key for saved value
	private static final String FIRST_FILL_KEY = "firstFill";

	// task object
	private FillDataTask mFillDataTask = null;

	// tooltips
	private ToolTipView mTooltipAddTrans = null;
	private ToolTipView mTooltipFilter = null;
	private ToolTipView mTooltipArrows = null;
	private ToolTipView mTooltipInterval = null;
	private ToolTipView mTooltipTotal = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get firstFill if is one
		if (savedInstanceState != null) {
			String savedVal = savedInstanceState.getString(FIRST_FILL_KEY);
			if (savedVal != null)
				firstFill = Boolean.parseBoolean(savedVal);
		}

		// data from parent activity
		mAccount = new Account();
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			mAccount.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			mAccount.loadFromDB(getContentResolver());
			this.setTitle(mAccount.getName());
		} else {
			Toast.makeText(AccountDetailActivity.this, "No uri in bundle",
					Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// init filter
		mFilter = new Filter();
		mFilter.setLowerBound(Calendar.getInstance().getTimeInMillis());
		mFilter.setInterval(DEFAULT_TIME_INTERVAL);

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

		// tooltip layout
		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_detail_tooltiplayout);

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// force overflow
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
		}

		// first fill
		// fillData();
		// TODO optimize the filling of the data : not three times when creating
		// the activity !

		// LISTENERS ************

		// previous
		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFilter.previous();
				setDateInterval();
				fillData();
			}
		});

		// next
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFilter.next();
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
						mFilter.setInterval(position);
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

					}

				});

		// ***********************

	}

	//
	private void enableNavigation(boolean b) {
		mFilter.setDateFilter(b);
		updateNavigator();
	}

	/*
	 * Fill the listview with transactions. If the actual filter doesn't select
	 * any transaction, adapt it.
	 */
	private void fillData() {
		Log.w("fill data", "called");
		// only one filling
		if (mFillDataTask == null) {
			Log.w("fill data", "executed");
			mFillDataTask = new FillDataTask(this, mAccount, mFilter, mAdapter,
					firstFill);
			mFillDataTask.execute();
		}
	}

	private void afterFillDataTask(ArrayList<Transaction> transactions,
			double total, boolean shouldAdapt) {
		Log.w("fill data", "finished");

		// update first fill
		firstFill = shouldAdapt;

		// hide nav if no date filter
		if (mFilter.isDateFilter() == false)
			enableNavigation(false);

		// set adapter
		if (transactions != null) {
			Log.w("fill data", "trans not null");
			mAdapter = new TransactionsAdapter(this, transactions);
			this.setListAdapter(mAdapter);
		} else {
			mAdapter = new TransactionsAdapter(this,
					new ArrayList<Transaction>());
		}

		// set total footer
		if (total < 0)
			totalView.setTextColor(Color.RED);
		else
			totalView.setTextColor(Color.GREEN);
		totalView.setText(TransactionsAdapter.amountFormat.format(total));

		// set task to null
		mFillDataTask = null;
	}

	private static class FillDataTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private double tTotal = 0;
		private ArrayList<Transaction> tTransactions; // t for task
		private AccountDetailActivity tContext;

		// copy all vars to keep task independant
		private Account tAccount;
		private Filter tFilter;
		private TransactionsAdapter tAdapter;
		private boolean tShouldAdapt;

		public FillDataTask(AccountDetailActivity context, Account account,
				Filter filter, TransactionsAdapter adapter, boolean shouldAdapt) {
			tContext = context;
			tAccount = account;
			tFilter = filter;
			tAdapter = adapter;
			tShouldAdapt = shouldAdapt;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(tContext,
					ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Log.w("accountDetail", "fill data");

			// get transactions
			tTransactions = tAccount.getListTransactions(
					tContext.getContentResolver(), tFilter);

			if (tTransactions != null) {

				// debug
				Log.w("accountDetail", "num of trans " + tTransactions.size());
				for (Transaction trans : tTransactions)
					Log.w("accountDetail", "trans " + trans.getName());

				// end debug

				// no trans
				if (tTransactions.size() == 0) {
					// adapt filter if no trans
					if (tShouldAdapt) {
						Log.w("accountDetail", "first fill");
						tFilter.adaptToAccount(tAccount);
						tTransactions = tAccount.getListTransactions(
								tContext.getContentResolver(), tFilter);

						// tShouldAdapt = false;
					}
				}

				// allow adaptation only on first fill
				tShouldAdapt = false;

			}

			// set amount of the selected transactions
			// tmp TODO
			if (tTransactions != null) {
				for (Transaction i : tTransactions)
					tTotal += i.getAmount();
			}
			// end tmp

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			tContext.afterFillDataTask(tTransactions, tTotal, tShouldAdapt);
		}

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
		intervalView.setText(Filter.millisToText(mFilter.getLowerBound())
				+ " - " + Filter.millisToText(mFilter.getUpperBound() - 1));
	}

	// update the time navigator
	private void updateNavigator() {
		if (mFilter.isDateFilter()) {
			// visible navigator
			navigator.setVisibility(View.VISIBLE);

			timeFilter.setSelection(mFilter.getInterval());
			setDateInterval();

			boolean custom = (mFilter.getInterval() == Filter.CUSTOM);
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
				mAccount.getId());
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
				mAccount.getId());

		startActivity(intent);
	}

	// open the filter activity
	private void setFilter() {
		Intent intent = new Intent(this, FilterActivity.class);
		intent.putExtra(Filter.class.toString(), mFilter);
		startActivityForResult(intent, FILTER_ACTIVITY);
	}

	/*
	 * starts the graph activity
	 */
	private void createGraph() {
		Intent intent = new Intent(this, GraphActivity.class);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
				mAccount.getUri());
		startActivity(intent);
	}

	// transfer
	private void transferTransactions(final ArrayList<Transaction> transList) {
		// account spinner
		final ArrayList<Account> accountsList = Account
				.getListAccounts(getContentResolver());
		if (accountsList == null) {
			Log.w("transfer transaction", "accounts list is null");
			return;
		}
		ArrayAdapter<Account> accountsAdapter = new AccountsAdapter(this,
				accountsList);

		// dialog to choose the target account
		MyDialog.chooseInList(this, R.string.activity_detail_transfer_question,
				accountsAdapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						// confirm transfer
						MyDialog.confirm(AccountDetailActivity.this,
								R.string.activity_detail_transfer_confirm,
								new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										// if yes, transfer
										for (Transaction trans : transList) {
											trans.transfer(
													getContentResolver(),
													accountsList.get(which));
										}
										// update actual view
										fillData();
										return null;
									}
								}, null);

					}
				});
	}

	// set the type of a list of trans
	private void changeType(final ArrayList<Transaction> transList) {
		// type spinner
		final ArrayList<Type> typesList = Type.getTypes(getContentResolver());
		if (typesList == null) {
			Log.w("change type", "types list is null");
			return;
		}
		ArrayAdapter<Type> typesAdapter = new TypesAdapter(this, typesList);

		// dialog to choose the target type
		MyDialog.chooseInList(this,
				R.string.activity_detail_change_type_question, typesAdapter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						// confirm change
						MyDialog.confirm(AccountDetailActivity.this,
								R.string.activity_detail_change_type_confirm,
								new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										// if yes, change type
										for (Transaction trans : transList) {
											trans.setParent(AccountDetailActivity.this.mAccount); // TODO
																									// do
																									// better
											trans.changeType(
													getContentResolver(),
													typesList.get(which));
										}
										// update actual view
										fillData();
										return null;
									}
								}, null);

					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FILTER_ACTIVITY:
			if (resultCode == Activity.RESULT_OK) {
				mFilter = (Filter) data.getSerializableExtra(Filter.class
						.toString());
			}
		}
	}

	// ADD, FILTER, TRANSFER and HELP BUTTONS IN ACTION BAR
	// ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_account_detail, menu);
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
		case R.id.menu_detail_change_type:
			changeType(mAccount.getListTransactions(getContentResolver(),
					mFilter));
			return true;
		case R.id.menu_detail_transfer:
			transferTransactions(mAccount.getListTransactions(
					getContentResolver(), mFilter));
			return true;
		case R.id.menu_detail_graph:
			createGraph();
			return true;
		case R.id.menu_detail_help:
			if (isNoTooltip())
				nextTooltip(null);
			return true;
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// CONTEXT MENU : DELETE, EDIT and TRANSFER BUTTONS -----------

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE,
				R.string.activity_detail_delete);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.activity_detail_edit);
		menu.add(Menu.NONE, MOVE_TO_ID, Menu.NONE,
				R.string.activity_detail_transfer);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
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
			MyDialog.confirm(this, R.string.activity_detail_delete_question,
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
			// create list with one trans
			ArrayList<Transaction> trans = new ArrayList<Transaction>();
			trans.add(actTransaction);
			// transfer it
			transferTransactions(trans);
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

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addTransTooltip();
		} else if (toolTipView == mTooltipAddTrans) {
			mTooltipAddTrans = null;
			addFilterTooltip();
		} else if (toolTipView == mTooltipFilter) {
			mTooltipFilter = null;
			addIntervalTooltip();
		} else if (toolTipView == mTooltipInterval) {
			mTooltipInterval = null;
			addArrowsTooltip();
		} else if (toolTipView == mTooltipArrows) {
			mTooltipArrows = null;
			addTotalTooltip();
		} else if (toolTipView == mTooltipTotal) {
			mTooltipTotal = null;
		}
	}

	// returns true if no tooltip is displayed
	private boolean isNoTooltip() {
		return (mTooltipAddTrans == null && mTooltipFilter == null
				&& mTooltipArrows == null && mTooltipInterval == null && mTooltipTotal == null);
	}

	private void addTransTooltip() {

		mTooltipAddTrans = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Add a new transactions, which \nrepresents a new income or expense.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_detail_add));
		mTooltipAddTrans.setOnToolTipViewClickedListener(this);
	}

	private void addFilterTooltip() {
		mTooltipFilter = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Set a filter to display only a \nselection of this account's \ntransactions.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_detail_filter));
		mTooltipFilter.setOnToolTipViewClickedListener(this);
	}

	private void addArrowsTooltip() {
		mTooltipArrows = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Display the transactions for \nthe previous interval.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true), previous);
		mTooltipArrows.setOnToolTipViewClickedListener(this);
	}

	private void addIntervalTooltip() {
		mTooltipInterval = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Change the interval of time for \nthe transactions displayed.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true), timeFilter);
		mTooltipInterval.setOnToolTipViewClickedListener(this);
	}

	private void addTotalTooltip() {
		if (totalView != null) {
			mTooltipTotal = mTooltipLayout.showToolTipForView(new ToolTip()
					.withText("The total of the displayed transactions.")
					.withColor(getResources().getColor(R.color.holo_orange))
					.withShadow(true), totalView);
			mTooltipTotal.setOnToolTipViewClickedListener(this);
		} else
			mTooltipTotal = null;
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}

	// ---------------------------------------------

	@Override
	protected void onResume() {
		Log.w("accountDetail", "on resume");
		Log.w("accountDetail", "first fill : " + this.firstFill);
		fillData();
		changeFooterColor(false);
		updateNavigator();
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(FIRST_FILL_KEY, String.valueOf(firstFill));
		super.onSaveInstanceState(outState);
	}
}
