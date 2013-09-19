package ch.goetschy.android.accounts.objects;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;

public class Account extends Item {
	private ArrayList<Transaction> list_transactions;
	private int order;

	public Account() {
		super();
	}

	public Account(int p_id, String p_name, Item p_parent) {
		super(p_id, 0, p_name, p_parent);
		list_transactions = new ArrayList<Transaction>();
	}

	public void addTransaction(Transaction transaction) {
		getList_transactions().add(transaction);
	}

	public ArrayList<Transaction> getList_transactions() {
		return list_transactions;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
	
	public void delete(ContentResolver contentResolver){
		if(uri != null)
			contentResolver.delete(uri, null, null);
	}

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

	public void saveInDB(ContentResolver contentResolver) {

		ContentValues values = new ContentValues();
		values.put(AccountsTable.COLUMN_NAME, name);
		
		if (uri == null) {
			values.put(AccountsTable.COLUMN_AMOUNT, 0);
			values.put(AccountsTable.COLUMN_ORDER, 0);
			values.put(AccountsTable.COLUMN_PARENT, 0);
			
			contentResolver.insert(
					MyAccountsContentProvider.CONTENT_URI_ACCOUNTS, values);
		} else 
			contentResolver.update(uri, values, null, null);
	}
	

	
	public static SimpleCursorAdapter getAdapter(Context context, int layout){
		String[] from = new String[] { AccountsTable.COLUMN_NAME,
				AccountsTable.COLUMN_AMOUNT };
		int[] to = new int[] { R.id.activity_overview_name,
				R.id.activity_overview_amount };
		
		return new SimpleCursorAdapter(context,
				layout, null, from, to, 0);
	}
}
