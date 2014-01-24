package ch.goetschy.android.accounts.objects;

import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AppInfosTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class AppInfos {
	/**
	 * class allows acces to app infos in the DB
	 */
	
	static final public String TEST = "test";
	static final public String CONTROL_DEFAULT_TYPES = "control_default_types";

	private ContentResolver contentResolver;

	public AppInfos(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
	}

	public void saveInDB(String name, String value) {

		ContentValues values = new ContentValues();
		values.put(AppInfosTable.COLUMN_NAME, name);
		values.put(AppInfosTable.COLUMN_VALUE, value);
		
		// test if info already exists
		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_APP_INFOS, null,
				AppInfosTable.COLUMN_NAME + " = '" + name + "'", null, null);
		
		if (!cursor.moveToFirst()) // if doesn't exist
			contentResolver.insert(MyAccountsContentProvider.CONTENT_URI_APP_INFOS,
					values);
		else
			contentResolver.update(MyAccountsContentProvider.CONTENT_URI_APP_INFOS,
					values, AppInfosTable.COLUMN_NAME + "='" + name + "'", null);

		cursor.close();
	}

	public void delete(String name) {
		contentResolver.delete(MyAccountsContentProvider.CONTENT_URI_APP_INFOS,
				AppInfosTable.COLUMN_NAME + "='" + name + "'", null);
	}

	public String loadFromDB(String name) {
		String[] projection = new String[] { AppInfosTable.COLUMN_VALUE };
		Cursor cursor = contentResolver.query(
				MyAccountsContentProvider.CONTENT_URI_APP_INFOS, projection,
				AppInfosTable.COLUMN_NAME + "='" + name + "'", null, null);
		if (cursor.moveToFirst()) {
			return loadFromCursor(cursor);
		}
		return null;
	}

	private String loadFromCursor(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(AppInfosTable.COLUMN_VALUE));
	}

}
