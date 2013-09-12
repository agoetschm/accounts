package ch.goetschy.android.accounts.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TypeTable extends Table {

	public static final String TABLE_NAME = "types";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_ORDER = "number";

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_NAME + " text not null, " + COLUMN_ORDER + " integer );";

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATBASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TypeTable.class.toString(), "Upgrading database...");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
