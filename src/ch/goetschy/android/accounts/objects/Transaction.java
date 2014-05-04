package ch.goetschy.android.accounts.objects;

import java.util.HashMap;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.database.TypeTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Transaction extends Item implements Savable {
	private Type type;
	private String description;
	private long date;

	public Transaction() {
		super();
		setType(new Type());
		setDescription("");
		setDate(0);
	}

	public Transaction(Cursor cursor, ContentResolver contentResolver) {
		super();
		setType(new Type());
		setDescription("");
		setDate(0);
		if (cursor != null) {
			loadFromCursor(cursor);
			type.loadNameAndColorFromDB(contentResolver);
		} else
			throw new NullPointerException("Null cursor");
	}

	public Transaction(int p_id, double p_amount, String p_name,
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

	/*
	 * transfert this transaction to the selected account
	 */
	public void transfer(ContentResolver contentResolver, Account targetAccount) {
		// security
		if (loadFromDB(contentResolver) == false)
			return;

		// change parent
		this.setParent(targetAccount);
		// save change
		saveInDB(contentResolver);
	}
	
	
	/*
	 * change the type and save
	 */
	public void changeType(ContentResolver contentResolver, Type targetType){
		// security
		if (loadFromDB(contentResolver) == false)
			return;

		// change type
		this.setType(targetType);
		// save change
		saveInDB(contentResolver);
	}

	@Override
	public void delete(ContentResolver contentResolver) {
		Log.w("transaction", "delete " + name);
		if (uri != null)
			contentResolver.delete(uri, null, null);
		else
			Log.w("transaction", "delete failed : no uri");
	}

	@Override
	public void saveInDB(ContentResolver contentResolver) {
		if (BuildConfig.DEBUG)
			Log.w("transaction", "saveInDB");

		ContentValues values = new ContentValues();
		values.put(TransactionTable.COLUMN_NAME, name);
		values.put(TransactionTable.COLUMN_AMOUNT, amount);

		values.put(TransactionTable.COLUMN_DESCRIPTION, description);
		values.put(TransactionTable.COLUMN_DATE, date);
		if (type != null)
			values.put(TransactionTable.COLUMN_TYPE, type.getId());
		if (parent != null) {
			values.put(TransactionTable.COLUMN_PARENT, parent.getId());
			Log.w("transaction", "parent id : " + parent.getId());
		} else {
			Log.w("transaction", "no parent");
			return;
		}

		if (uri == null)
			uri = contentResolver.insert(
					MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS, values);
		else
			contentResolver.update(uri, values, null, null);

		Log.w("transaction", "saveInDB end");
	}

	@Override
	public boolean loadFromDB(ContentResolver contentResolver) {
		if (uri != null) {
			Cursor cursor = contentResolver.query(uri, null, null, null, null);

			if (cursor.moveToFirst()) {
				loadFromCursor(cursor);
				cursor.close();
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadFromCursor(Cursor cursor) {
		this.setId(cursor.getLong(cursor
				.getColumnIndex(TransactionTable.COLUMN_ID)));
		this.setAmount(cursor.getDouble(cursor
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

		uri = Uri.parse(MyAccountsContentProvider.CONTENT_URI_TRANSACTIONS
				+ "/" + id);
	}

	public Long getTypeIdFromDB(ContentResolver contentResolver) {
		if (uri != null) {
			String[] projection = new String[] { TransactionTable.COLUMN_TYPE };
			Cursor cursor = contentResolver.query(uri, projection, null, null,
					null);

			if (cursor.moveToFirst()) {
				return cursor.getLong(cursor
						.getColumnIndex(TransactionTable.COLUMN_TYPE));
			}
		}
		return DEFAULT_ID;
	}

	// methods from Savable --------------
	@Override
	public HashMap<String, String> getFields() {
		HashMap<String, String> fields = new HashMap<String, String>();

		fields.put("name", getName());
		fields.put("amount", String.valueOf(getAmount()));
		fields.put("description", getDescription());
		fields.put("date", String.valueOf(getDate()));
		fields.put("typeName", getType().getName()); // can be null

		return fields;
	}

	@Override
	public boolean setFields(HashMap<String, String> fields) {
		Log.w("transaction", "setFields");

		Log.w("transaction", "name");
		if (fields.get("name") == null)
			return false;
		setName(fields.get("name"));

		Log.w("transaction", "amount");
		if (fields.get("amount") == null)
			return false;
		Log.w("transaction", fields.get("amount"));
		setAmount(Double.valueOf(fields.get("amount")));

		Log.w("transaction", "description");
		if (fields.get("description") != null) {
			Log.w("transaction", fields.get("description"));
			setDescription(fields.get("description"));
		}

		Log.w("transaction", "date");
		if (fields.get("date") == null)
			return false;
		Log.w("transaction", fields.get("date"));
		setDate(Long.valueOf(fields.get("date")));

		Log.w("transaction", "typeName");
		if (fields.get("typeName") == null)
			return false;
		setType(null); // temp

		return true;
	}
	// ----------------------------------

}
