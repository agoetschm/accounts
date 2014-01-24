package ch.goetschy.android.accounts.database;

import ch.goetschy.android.accounts.BuildConfig;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AppInfosTable extends Table {

	public static final String TABLE_NAME = "app_infos";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_VALUE = "value";

	public static final String[] available = { COLUMN_ID, COLUMN_NAME,
			COLUMN_VALUE };

	private static final String DATATBASE_CREATE = "create table " + TABLE_NAME
			+ "( " + COLUMN_ID + " integer primary key autoincrement,"
			+ COLUMN_NAME + " text not null, " + COLUMN_VALUE  + " text not null );";

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATATBASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		if (BuildConfig.DEBUG)
			Log.w(AppInfosTable.class.toString(), "Upgrading database...");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}
