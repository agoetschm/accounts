package ch.goetschy.android.accounts.database;

import ch.goetschy.android.accounts.BuildConfig;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TypeTable extends Table {

	public static final String TABLE_NAME = "types";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_ORDER = "number";
	public static final String COLUMN_COLOR = "color";

	public static final String[] available = { COLUMN_ID, COLUMN_NAME,
			COLUMN_ORDER, COLUMN_COLOR };

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_NAME + " text not null, " + COLUMN_ORDER + " integer, "
			+ COLUMN_COLOR + " integer );";

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATBASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		if (BuildConfig.DEBUG)
			Log.w(TypeTable.class.toString(), "Upgrading database...");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
