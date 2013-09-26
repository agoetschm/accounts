package ch.goetschy.android.accounts.objects;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
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

	//
	// public void addTransaction(Transaction transaction) {
	// listTransactions.add(transaction);
	// }


	public ArrayList<Transaction> getListTransactions() {
		return listTransactions;
	}

	public ArrayList<Transaction> getListTransactions(
			ContentResolver contentResolver) {
		if (uri != null) {
			Cursor cursor = contentResolver.query(
					MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS, null,
					TransactionTable.COLUMN_PARENT + "=" + id, null, null);
			Log.w("account", "cursor movetofirst");
			if (cursor.moveToFirst()) {
				// clear actual list
				listTransactions.clear();
				while(!cursor.isAfterLast()){
					Log.w("account", "cursor line");
					listTransactions.add(new Transaction(cursor));
					cursor.moveToNext();
				}
				Log.w("account", "cursor close");
				cursor.close();
			} else
				return null;
		}
		return listTransactions;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public void delete(ContentResolver contentResolver) {
		if (uri != null)
			contentResolver.delete(uri, null, null);
	}

	@Override
	public boolean loadFromDB(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(uri, null, null, null, null);

		if (cursor.moveToFirst()) {
			this.setId(cursor.getLong(cursor
					.getColumnIndex(AccountsTable.COLUMN_ID)));
			this.setAmount(cursor.getInt(cursor
					.getColumnIndex(AccountsTable.COLUMN_AMOUNT)));
			this.setName(cursor.getString(cursor
					.getColumnIndex(AccountsTable.COLUMN_NAME)));
			this.setOrder(cursor.getInt(cursor
					.getColumnIndex(AccountsTable.COLUMN_ORDER)));
			if (parent != null)
				parent.setId(cursor.getLong(cursor
						.getColumnIndex(AccountsTable.COLUMN_PARENT)));

			cursor.close();
			return true;
		}
		return false;
	}

	@Override
	public void saveInDB(ContentResolver contentResolver) {

		ContentValues values = new ContentValues();
		values.put(AccountsTable.COLUMN_NAME, name);

		values.put(AccountsTable.COLUMN_AMOUNT, amount);
		values.put(AccountsTable.COLUMN_ORDER, order);
		if (parent != null)
			values.put(AccountsTable.COLUMN_PARENT, parent.getId());

		if (uri == null)
			contentResolver.insert(
					MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
		else

			contentResolver.update(uri, values, null, null);

	}

	public static SimpleCursorAdapter getAdapter(Context context, int layout) {
		String[] from = new String[] { AccountsTable.COLUMN_NAME,
				AccountsTable.COLUMN_AMOUNT };
		int[] to = new int[] { R.id.activity_overview_name,
				R.id.activity_overview_amount };

		return new SimpleCursorAdapter(context, layout, null, from, to, 0);
	}
}
