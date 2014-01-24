package ch.goetschy.android.accounts.database;

import ch.goetschy.android.accounts.BuildConfig;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AccountsDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "accounts.db";
	private static final int DATABASE_VERSION = 2;

	public AccountsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if (BuildConfig.DEBUG)
			Log.w("DB helper", "constructor " + DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		new AccountsTable().onCreate(arg0);
		new TransactionTable().onCreate(arg0);
		new TypeTable().onCreate(arg0);
		new AppInfosTable().onCreate(arg0);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
			new AppInfosTable().onUpgrade(db, oldVersion, newVersion);
		}
		Log.w("DB helper", "upgrade");
	}

}
