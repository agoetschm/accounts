package ch.goetschy.android.accounts.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountsDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "accounts.db";
	private static final int DATABASE_VERSION = 1;

	public AccountsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		new AccountsTable().onCreate(arg0);
		new TransactionTable().onCreate(arg0);
		new TypeTable().onCreate(arg0);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		new AccountsTable().onUpgrade(db, oldVersion, newVersion);
		new TransactionTable().onUpgrade(db, oldVersion, newVersion);
		new TypeTable().onUpgrade(db, oldVersion, newVersion);
	}

}
