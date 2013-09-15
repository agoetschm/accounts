package ch.goetschy.android.accounts.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TransactionTable extends Table {

	public static final String TABLE_NAME = "transactions";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_PARENT = "parent";

	public static final String[] available = { COLUMN_AMOUNT, COLUMN_DATE,
			COLUMN_DESCRIPTION, COLUMN_ID, COLUMN_NAME, COLUMN_PARENT,
			COLUMN_TYPE };

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_AMOUNT + " integer, " + COLUMN_NAME + " text not null, "
			+ COLUMN_DESCRIPTION + " text not null, " + COLUMN_DATE
			+ " integer, " + COLUMN_TYPE + " integer, " + COLUMN_PARENT
			+ " integer );";

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
