package ch.goetschy.android.accounts.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public abstract class Table {
	public String TABLE_NAME;
	public static final String COLUMN_ID = "_id";

	public abstract void onCreate(SQLiteDatabase database);

	public abstract void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion);
}
