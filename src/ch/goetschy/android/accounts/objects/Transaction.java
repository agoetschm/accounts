package ch.goetschy.android.accounts.objects;

import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

public class Transaction extends Item {
	private Type type;
	private String description;
	private long date;

	public Transaction() {
		super();
		setType(new Type());
		setDescription("");
		setDate(0);
	}

	public Transaction(Cursor cursor) {
		super();
		if (cursor != null)
			loadFromCursor(cursor);
		else
			throw new NullPointerException("Null cursor");
	}

	public Transaction(int p_id, int p_amount, String p_name,
			String p_description, long p_date, Type p_type, Account p_parent) {
		super(p_id, p_amount, p_name, p_parent);
		setType(p_type);
		setDescription(p_description);
		setDate(p_date);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public void delete(ContentResolver contentResolver) {
		if (uri != null)
			contentResolver.delete(uri, null, null);
	}

	@Override
	public void saveInDB(ContentResolver contentResolver) {

		ContentValues values = new ContentValues();
		values.put(TransactionTable.COLUMN_NAME, name);
		values.put(TransactionTable.COLUMN_AMOUNT, amount);

		values.put(TransactionTable.COLUMN_DESCRIPTION, description);
		values.put(TransactionTable.COLUMN_DATE, date);
		values.put(TransactionTable.COLUMN_TYPE, type.getId());
		if (parent != null)
			values.put(TransactionTable.COLUMN_PARENT, parent.getId());

		if (uri == null)
			contentResolver.insert(
					MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS, values);
		else
			contentResolver.update(uri, values, null, null);
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

	private void loadFromCursor(Cursor cursor) {
		this.setId(cursor.getLong(cursor
				.getColumnIndex(TransactionTable.COLUMN_ID)));
		this.setAmount(cursor.getInt(cursor
				.getColumnIndex(TransactionTable.COLUMN_AMOUNT)));
		this.setName(cursor.getString(cursor
				.getColumnIndex(TransactionTable.COLUMN_NAME)));
		this.setDescription(cursor.getString(cursor
				.getColumnIndex(TransactionTable.COLUMN_DESCRIPTION)));
		this.setDate(cursor.getLong(cursor
				.getColumnIndex(TransactionTable.COLUMN_DATE)));
		if (type != null)
			type.setId(cursor.getLong(cursor
					.getColumnIndex(TransactionTable.COLUMN_TYPE)));
		if (parent != null)
			parent.setId(cursor.getLong(cursor
					.getColumnIndex(TransactionTable.COLUMN_PARENT)));
	}

}
