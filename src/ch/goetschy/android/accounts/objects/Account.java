package ch.goetschy.android.accounts.objects;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;

public class Account extends Item {
	private ArrayList<Transaction> listTransactions;
	private int order;

	public Account() {
		super();
		listTransactions = new ArrayList<Transaction>();
		setOrder(0);
	}

	public Account(long p_id) {
		super();
		listTransactions = new ArrayList<Transaction>();
		setOrder(0);
		setId(p_id);
	}

	public Account(int p_id, String p_name, Item p_parent) {
		super(p_id, 0, p_name, p_parent);
		listTransactions = new ArrayList<Transaction>();
		setOrder(0);
	}

	// for the "all accounts" item in save-restore
	public Account(int p_id, String p_name, int p_amount) {
		super(p_id, p_amount, p_name, null);
		setOrder(0);
	}

	public Account(Cursor cursor) {
		super();
		listTransactions = new ArrayList<Transaction>();
		loadFromCursor(cursor);
	}

	public ArrayList<Transaction> getListTransactions() {
		return listTransactions;
	}

	// also updates amount
	public ArrayList<Transaction> getListTransactions(
			ContentResolver contentResolver) {
		if (uri != null) {
			double tmpAmount = 0;

			// clear actual list
			listTransactions.clear();

			Cursor cursor = contentResolver.query(
					MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS, null,
					TransactionTable.COLUMN_PARENT + "=" + id, null,
					TransactionTable.COLUMN_DATE + " ASC");
			if (BuildConfig.DEBUG)
				Log.w("account", "cursor movetofirst");
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					if (BuildConfig.DEBUG)
						Log.w("account", "cursor line");
					Transaction newTrans = new Transaction(cursor,
							contentResolver);
					listTransactions.add(newTrans);

					tmpAmount += newTrans.getAmount();

					cursor.moveToNext();
				}
				if (BuildConfig.DEBUG)
					Log.w("account", "cursor close");
				cursor.close();

				// save new amount
				amount = tmpAmount;
				saveAmountInDB(contentResolver);
			} else {
				// save zero amount
				amount = 0;
				saveAmountInDB(contentResolver);
			}
		}
		return listTransactions;
	}

	// return only the transactions within the filter's bounds
	public ArrayList<Transaction> getListTransactions(
			ContentResolver contentResolver, Filter filter) {

		if (getListTransactions(contentResolver) == null)
			return null;

		ArrayList<Transaction> filteredTransactions = new ArrayList<Transaction>();
		if (BuildConfig.DEBUG)
			Log.w("account", "filter bounds : " + filter.getLowerBound()
					+ " - " + filter.getUpperBound());
		// filter
		for (Transaction trans : listTransactions) {
			if (filter.isSelected(trans)) {
				filteredTransactions.add(trans);
			}
		}
		return filteredTransactions;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public void delete(ContentResolver contentResolver) {
		if (uri != null) {
			// delete all transactions
			getListTransactions(contentResolver);
			for (Transaction trans : listTransactions)
				trans.delete(contentResolver);

			// delete account itself
			contentResolver.delete(uri, null, null);
		}
	}

	@Override
	public boolean loadFromDB(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(uri, null, null, null, null);

		if (cursor.moveToFirst()) {
			loadFromCursor(cursor);
			cursor.close();
			return true;
		}
		return false;
	}

	@Override
	protected void loadFromCursor(Cursor cursor) {
		this.setId(cursor.getLong(cursor
				.getColumnIndex(AccountsTable.COLUMN_ID)));
		this.setAmount(cursor.getDouble(cursor
				.getColumnIndex(AccountsTable.COLUMN_AMOUNT)));
		this.setName(cursor.getString(cursor
				.getColumnIndex(AccountsTable.COLUMN_NAME)));
		this.setOrder(cursor.getInt(cursor
				.getColumnIndex(AccountsTable.COLUMN_ORDER)));
		if (parent != null)
			parent.setId(cursor.getLong(cursor
					.getColumnIndex(AccountsTable.COLUMN_PARENT)));
		uri = Uri.parse(MyAccountsContentProvider.CONTENT_URI_ACCOUNTS + "/"
				+ id);
	}

	@Override
	public void saveInDB(ContentResolver contentResolver) {
		if (BuildConfig.DEBUG)
			Log.w("account", "saveInDB");

		ContentValues values = new ContentValues();
		values.put(AccountsTable.COLUMN_NAME, name);

		values.put(AccountsTable.COLUMN_AMOUNT, amount);
		values.put(AccountsTable.COLUMN_ORDER, order);
		if (parent != null)
			values.put(AccountsTable.COLUMN_PARENT, parent.getId());

		if (uri == null){
			uri = contentResolver.insert(
					MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
			id = Integer.valueOf(uri.getLastPathSegment());
		}
		else
			contentResolver.update(uri, values, null, null);

	}

	public void saveAmountInDB(ContentResolver contentResolver) {

		ContentValues values = new ContentValues();

		values.put(AccountsTable.COLUMN_AMOUNT, amount);
		
		Log.w("account", "save amount; uri = " + uri);
		if (uri != null)
			contentResolver.update(uri, values, null, null);

	}

	public static ArrayList<Account> getListAccounts(
			ContentResolver contentResolver) {
		ArrayList<Account> listAccounts = new ArrayList<Account>();

		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, null, null,
				null, null);
		if (BuildConfig.DEBUG)
			Log.w("account", "cursor movetofirst");
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				if (BuildConfig.DEBUG)
					Log.w("account", "cursor line");

				listAccounts.add(new Account(cursor));

				cursor.moveToNext();
			}
			if (BuildConfig.DEBUG)
				Log.w("account", "cursor close");
			cursor.close();

		}

		return listAccounts;
	}

	// checks if an account name exists in the db
	public static int accountNameExists(ContentResolver contentResolver,
			String name) {
		String[] projection = new String[] { AccountsTable.COLUMN_NAME };
		// query for account
		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, projection,
				AccountsTable.COLUMN_NAME + "='" + name + "'", null, null);
		// if already exists
		if (cursor.moveToFirst()) {
			return cursor.getColumnCount();
		}
		// else
		return 0;
	}

	@Override
	public String toString() {
		// for the list adapter
		return getName();
	}

}
