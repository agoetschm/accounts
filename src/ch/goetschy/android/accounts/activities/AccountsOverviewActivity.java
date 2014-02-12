package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.AppInfos;
import ch.goetschy.android.accounts.objects.Type;

import android.R.color;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountsOverviewActivity extends ListActivity {

	private static final int DELETE_ID = 10;
	private static final int EDIT_ID = 20;

	private AccountsAdapter adapter;
	private View footer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		this.getListView().setDividerHeight(2);
		ListView listview = getListView();
		this.registerForContextMenu(listview);
		
		
		Button addBut = (Button) findViewById(R.id.activity_overview_add);
		Button returnBut = (Button) findViewById(R.id.activity_overview_return);

		// ADD footer
//		footer = getLayoutInflater().inflate(R.layout.activity_overview_footer,
//				null);
//		listview.addFooterView(footer);
//		footer.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				changeFooterColor(true);
//				createAccount();
//			}
//		});

		// AppInfos infos = new AppInfos(getContentResolver());
		// // infos.saveInDB(AppInfos.TEST, "test2");
		// Log.w("overview", "infos : " + infos.loadFromDB(AppInfos.TEST));
		// infos.delete(AppInfos.TEST);

		// ArrayList<String> array = new ArrayList<String>();
		// array.add("test");

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
		
		
		// LISTENERS -----------------------
		
		addBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createAccount();
			}
		});
		

		returnBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// -----------------------------

	}

	private void changeFooterColor(boolean dark) {
		if (dark)
			footer.setBackgroundColor(Color.rgb(210, 210, 210));
		else
			footer.setBackgroundColor(Color.rgb(220, 220, 220));
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
//		changeFooterColor(false);
		super.onResume();
	}

	private void createAccount() {
		Intent intent = new Intent(this, EditAccountActivity.class);
		startActivity(intent);
	}

	// ADD and TYPES BUTTON ------------------------

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.activity_overview, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_add:
//			createAccount();
//			return true;
//		case R.id.menu_types:
//			manageTypes();
//			return true;
//		}
//
//		return super.onOptionsItemSelected(item);
//	}

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
		Account account = new Account();

		// get id
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		account.setUri(Uri.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS
				+ "/" + info.id));

		// delete or edit
		switch (item.getItemId()) {
		case DELETE_ID:
			account.delete(getContentResolver());
			fillData();
			return true;
		case EDIT_ID:
			Intent intent = new Intent(this, EditAccountActivity.class);
			intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
					account.getUri());
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
}
