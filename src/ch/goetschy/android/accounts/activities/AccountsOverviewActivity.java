package ch.goetschy.android.accounts.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.AppInfos;
import ch.goetschy.android.accounts.objects.Type;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ViewConfiguration;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;

public class AccountsOverviewActivity extends SherlockListActivity implements
		ToolTipView.OnToolTipViewClickedListener {

	private static final int DELETE_ID = 10;
	private static final int EDIT_ID = 20;

	private AccountsAdapter adapter;
	private Account actAccount; // actual account -> delete

	private ToolTipView mTooltipAddAccount = null;
	private ToolTipView mTooltipTypes = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		ListView listview = getListView();
		this.registerForContextMenu(listview);

		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_overview_tooltiplayout);

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

		// default types ------------
		AppInfos infos = new AppInfos(getContentResolver());
		Log.w("overview",
				"infos : -" + infos.loadFromDB(AppInfos.CONTROL_DEFAULT_TYPES)
						+ "-");

		String strInfos = (infos.loadFromDB(AppInfos.CONTROL_DEFAULT_TYPES) == null ? "true"
				: "false");
		if (!(strInfos.equals("false")) // if "ask again" wasn't checked and
										// absent default types
				&& !Type.controlDefault(this.getContentResolver())) {

			// dialog - load defaults ----------------
			new AlertDialog.Builder(AccountsOverviewActivity.this)
					.setTitle(R.string.activity_overview_default_types_question)
					.setCancelable(false)
					// checkable - ask again ?
					.setMultiChoiceItems(R.array.askagain, null,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									if (isChecked) {
										new AppInfos(getContentResolver())
												.saveInDB(
														AppInfos.CONTROL_DEFAULT_TYPES,
														"false");
									} else {
										new AppInfos(getContentResolver())
												.saveInDB(
														AppInfos.CONTROL_DEFAULT_TYPES,
														"true");
									}
								}
							})
					.setPositiveButton(
							R.string.activity_overview_default_types_yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// add the default types
									Type.addDefault(getContentResolver());
								}
							})
					.setNegativeButton(
							R.string.activity_overview_default_types_no, null)
					.create().show();
			// ----------------

			if (BuildConfig.DEBUG)
				Log.w("overview", "not all default");
		}

	}

	private void fillData() {
		ArrayList<Account> accounts = Account
				.getListAccounts(getContentResolver());

		if (accounts != null) {
			adapter = new AccountsAdapter(this, accounts);
			this.setListAdapter(adapter);
		} else if (adapter != null)
			adapter.clear();
	}

	@Override
	protected void onResume() {
		fillData();
		// changeFooterColor(false);
		super.onResume();
	}

	private void createAccount() {
		Intent intent = new Intent(this, EditAccountActivity.class);
		startActivity(intent);
	}

	private void manageTypes() {
		Intent intent = new Intent(this, ManageTypesActivity.class);
		startActivity(intent);
	}

	// ADD and TYPES BUTTON IN ACTION BAR ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_overview_add:
			createAccount();
			return true;
		case R.id.menu_overview_types:
			manageTypes();
			return true;
		case R.id.menu_overview_help:
			if (mTooltipAddAccount == null && this.mTooltipTypes == null)
				nextTooltip(null);
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	// CONTEXT MENU : DELETE and EDIT BUTTONs -----------

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.account_overview_edit);
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE,
				R.string.account_overview_delete);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		actAccount = new Account();

		// get id
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		actAccount.setUri(Uri
				.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS + "/"
						+ info.id));

		// delete or edit
		switch (item.getItemId()) {
		case DELETE_ID:
			// show confirm dialog
			MyDialog.confirm(this, R.string.edit_account_delete_question,
					new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							// if yes, delete
							actAccount.delete(getContentResolver());
							fillData(); // and refresh
							return null;
						}
					}, null);
			return true;
		case EDIT_ID:
			Intent intent = new Intent(this, EditAccountActivity.class);
			intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
					actAccount.getUri());
			startActivity(intent);
		}

		return super.onContextItemSelected(item);
	}

	// ACCOUNT DETAILS -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, AccountDetailActivity.class);
		Uri accountUri = Uri
				.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS + "/"
						+ id);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE, accountUri);

		startActivity(intent);
	}

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addTypesTooltip();
		} else if (toolTipView == mTooltipTypes) {
			mTooltipTypes = null;
			addAccountTooltip();
		} else if (toolTipView == mTooltipAddAccount) {
			mTooltipAddAccount = null;
		}
	}

	private void addAccountTooltip() {
		mTooltipAddAccount = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Add a new account, which\nis a group of transactions.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_overview_add));
		mTooltipAddAccount
				.setOnToolTipViewClickedListener(AccountsOverviewActivity.this);
	}

	private void addTypesTooltip() {
		mTooltipTypes = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Create and modify your own\ntypes of transactions to manage\nyour accounts.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_overview_types));
		mTooltipTypes
				.setOnToolTipViewClickedListener(AccountsOverviewActivity.this);
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}
}
