package ch.goetschy.android.accounts.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AccountsTable extends Table {

	public static final String TABLE_NAME = "accounts";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ORDER = "order";
	public static final String COLUMN_PARENT = "parent";

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_AMOUNT + " integer, " + COLUMN_NAME + " text not null, "
			+ COLUMN_ORDER + " integer, " + COLUMN_PARENT + " integer );";

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATBASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(AccountsTable.class.toString(), "Upgrading database...");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
