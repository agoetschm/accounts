package ch.goetschy.android.accounts.contentprovider;

import ch.goetschy.android.accounts.database.AccountsDatabaseHelper;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.database.TypeTable;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MyAccountsContentProvider extends ContentProvider {

	private static final String AUTHORITY = "ch.goetschy.android.accounts.contentprovider";

	private static final String BASE_PATH_ACCOUNTS = AccountsTable.TABLE_NAME;
	private static final String BASE_PATH_TRANSACTIONS = TransactionTable.TABLE_NAME;
	private static final String BASE_PATH_TYPES = TypeTable.TABLE_NAME;

	public final static Uri CONTENT_URI_ACCOUNTS = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_ACCOUNTS);
	public final static Uri CONTENT_URI_TRANSACTIONS = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_TRANSACTIONS);
	public final static Uri CONTENT_URI_TYPES = Uri.parse("content://"
			+ AUTHORITY + "/" + BASE_PATH_TYPES);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/items";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/item";

	private static final int ACCOUNTS = 10;
	private static final int ACCOUNT_ID = 20;

	private static final int TRANSACTIONS = 30;
	private static final int TRANSACTION_ID = 40;

	private static final int TYPES = 50;
	private static final int TYPE_ID = 60;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_ACCOUNTS, ACCOUNTS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_ACCOUNTS + "/#", ACCOUNT_ID);

		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TRANSACTIONS, TRANSACTIONS);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TRANSACTIONS + "/#",
				TRANSACTION_ID);

		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TYPES, TYPES);
		sUriMatcher.addURI(AUTHORITY, BASE_PATH_TYPES + "/#", TYPE_ID);
	}

	private AccountsDatabaseHelper database;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		int uriType = sUriMatcher.match(uri);

		if (uriType == ACCOUNTS || uriType == TRANSACTIONS || uriType == TYPES)
			return CONTENT_TYPE;
		else if (uriType == ACCOUNT_ID || uriType == TRANSACTION_ID
				|| uriType == TYPE_ID)
			return CONTENT_ITEM_TYPE;
		else
			return "unknown type";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		String table;

		switch (uriType) {
		case ACCOUNTS:
			table = BASE_PATH_ACCOUNTS;
			break;
		case TRANSACTIONS:
			table = BASE_PATH_TRANSACTIONS;
			break;
		case TYPES:
			table = BASE_PATH_TYPES;
			break;
		default:
			table = "error";
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}
		id = sqlDB.insert(table, null, values);
		this.getContext().getContentResolver().notifyChange(uri, null);

		return Uri.parse(table + "/" + id);
	}

	@Override
	public boolean onCreate() {
		database = new AccountsDatabaseHelper(this.getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
