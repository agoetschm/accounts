package ch.goetschy.android.accounts.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

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
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyAccountsContentProvider extends ContentProvider {

	private static final String AUTHORITY = "ch.goetschy.android.accounts.contentprovider";

	private static final String PATH_ACCOUNTS = AccountsTable.TABLE_NAME;
	private static final String PATH_TRANSACTIONS = TransactionTable.TABLE_NAME;
	private static final String PATH_TYPES = TypeTable.TABLE_NAME;

	public final static Uri CONTENT_URI_ACCOUNTS = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_ACCOUNTS);
	public final static Uri CONTENT_URI_TRANSACTIONS = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_TRANSACTIONS);
	public final static Uri CONTENT_URI_TYPES = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_TYPES);

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
		sUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS, ACCOUNTS);
		sUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/#", ACCOUNT_ID);

		sUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS, TRANSACTIONS);
		sUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS + "/#", TRANSACTION_ID);

		sUriMatcher.addURI(AUTHORITY, PATH_TYPES, TYPES);
		sUriMatcher.addURI(AUTHORITY, PATH_TYPES + "/#", TYPE_ID);
	}

	// private static final int ITEMS = 70;
	// private static final int ITEM_ID = 80;
	// private static final UriMatcher typeUriMatcher = new UriMatcher(
	// UriMatcher.NO_MATCH);
	// static {
	// typeUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS, ACCOUNTS);
	// typeUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/#", ACCOUNT_ID);
	// }

	private AccountsDatabaseHelper database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowDeleted = 0;

		switch (uriType) {
		case ACCOUNTS:
			rowDeleted = sqlDB.delete(AccountsTable.TABLE_NAME, selection,
					selectionArgs);
			break;
		case ACCOUNT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowDeleted = sqlDB.delete(AccountsTable.TABLE_NAME,
						AccountsTable.COLUMN_ID + "=" + id, null);
			} else {
				rowDeleted = sqlDB.delete(AccountsTable.TABLE_NAME,
						AccountsTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}
		this.getContext().getContentResolver().notifyChange(uri, null);

		return rowDeleted;
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
			table = PATH_ACCOUNTS;
			break;
		case TRANSACTIONS:
			table = PATH_TRANSACTIONS;
			break;
		case TYPES:
			table = PATH_TYPES;
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
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		String table = uri.getPathSegments().get(0);
		if (!table.equals(PATH_ACCOUNTS) && !table.equals(PATH_TRANSACTIONS)
				&& !table.equals(PATH_TYPES)) {
			Log.w("contentProvider", "path : " + table);
			throw new IllegalArgumentException("Unknown table : " + table);
		}

		checkColumns(projection, table);

		queryBuilder.setTables(table);

		int uriType = sUriMatcher.match(uri);
		switch (uriType) {
		case ACCOUNTS:
			break;
		case ACCOUNT_ID:
			String id = uri.getLastPathSegment();
			queryBuilder.appendWhere(AccountsTable.COLUMN_ID + "=" + id);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}

		SQLiteDatabase sqlDB = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(sqlDB, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(this.getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowUpdated = 0;

		switch (uriType) {
		case ACCOUNTS:
			rowUpdated = sqlDB.update(AccountsTable.TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case ACCOUNT_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowUpdated = sqlDB.update(AccountsTable.TABLE_NAME, values,
						AccountsTable.COLUMN_ID + "=" + id, null);
			} else {
				rowUpdated = sqlDB.update(AccountsTable.TABLE_NAME, values,
						AccountsTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}

			break;
		default:
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}
		this.getContext().getContentResolver().notifyChange(uri, null);

		return rowUpdated;
	}

	private void checkColumns(String[] projection, String table) {
		String[] available = {};
		if (table.equals(PATH_ACCOUNTS)) {
			available = AccountsTable.available;
		} else if (table.equals(PATH_TRANSACTIONS)) {
			available = TransactionTable.available;
		} else if (table == PATH_TYPES) {
			available = TypeTable.available;
		}
		if (projection != null) {
			HashSet<String> requestedCols = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableCols = new HashSet<String>(
					Arrays.asList(available));
			if (!availableCols.containsAll(requestedCols)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
