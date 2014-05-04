package ch.goetschy.android.accounts.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.database.AccountsDatabaseHelper;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.AppInfosTable;
import ch.goetschy.android.accounts.database.Table;
import ch.goetschy.android.accounts.database.TransactionTable;
import ch.goetschy.android.accounts.database.TypeTable;
import ch.goetschy.android.accounts.objects.AppInfos;

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
	private static final String PATH_APP_INFOS = AppInfosTable.TABLE_NAME;

	public static final String ACCOUNTS_AUTHORITY = AUTHORITY + "/"
			+ PATH_ACCOUNTS;

	public final static Uri CONTENT_URI_ACCOUNTS = Uri.parse("content://"
			+ ACCOUNTS_AUTHORITY);
	public final static Uri CONTENT_URI_TRANSACTIONS = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_TRANSACTIONS);
	public final static Uri CONTENT_URI_TYPES = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_TYPES);
	public final static Uri CONTENT_URI_APP_INFOS = Uri.parse("content://"
			+ AUTHORITY + "/" + PATH_APP_INFOS);
	
	public final static Uri CONTENT_URI_BASE = Uri.parse("content://"
			+ AUTHORITY);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/items";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/item";
	public static final String CONTENT_ACCOUNT_ID_TYPE = ACCOUNTS_AUTHORITY
			+ "/id";

	private static final int ACCOUNTS = 10;
	private static final int ACCOUNT_ID = 20;

	private static final int TRANSACTIONS = 30;
	private static final int TRANSACTION_ID = 40;

	private static final int TYPES = 50;
	private static final int TYPE_ID = 60;

	private static final int APP_INFOS = 70;
	private static final int APP_INFOS_ID = 80;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS, ACCOUNTS);
		sUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/#", ACCOUNT_ID);

		sUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS, TRANSACTIONS);
		sUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS + "/#", TRANSACTION_ID);

		sUriMatcher.addURI(AUTHORITY, PATH_TYPES, TYPES);
		sUriMatcher.addURI(AUTHORITY, PATH_TYPES + "/#", TYPE_ID);

		sUriMatcher.addURI(AUTHORITY, PATH_APP_INFOS, APP_INFOS);
		sUriMatcher.addURI(AUTHORITY, PATH_APP_INFOS + "/#", APP_INFOS_ID);
	}

	private static final int ITEMS = 100;
	private static final int ITEM_ID = 110;
	private static final UriMatcher typeUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		typeUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS, ITEMS);
		typeUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS, ITEMS);
		typeUriMatcher.addURI(AUTHORITY, PATH_TYPES, ITEMS);
		typeUriMatcher.addURI(AUTHORITY, PATH_APP_INFOS, ITEMS);

		typeUriMatcher.addURI(AUTHORITY, PATH_ACCOUNTS + "/#", ITEM_ID);
		typeUriMatcher.addURI(AUTHORITY, PATH_TRANSACTIONS + "/#", ITEM_ID);
		typeUriMatcher.addURI(AUTHORITY, PATH_TYPES + "/#", ITEM_ID);
		typeUriMatcher.addURI(AUTHORITY, PATH_APP_INFOS + "/#", ITEM_ID);

	}

	private AccountsDatabaseHelper database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = typeUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowDeleted = 0;

		String table = getTable(uri);
		
		if (BuildConfig.DEBUG)
			Log.w("contentProvider", "delete in " + table + " : " + selection);

		switch (uriType) {
		case ITEMS:
			rowDeleted = sqlDB.delete(table, selection, selectionArgs);
			break;
		case ITEM_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowDeleted = sqlDB.delete(table, Table.COLUMN_ID + "=" + id,
						null);
			} else {
				rowDeleted = sqlDB.delete(table, Table.COLUMN_ID + "=" + id
						+ " and " + selection, selectionArgs);
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
		int uriType = typeUriMatcher.match(uri);

		if (uriType == ITEMS)
			return CONTENT_TYPE;
		else if (uriType == ITEM_ID)
			return CONTENT_ITEM_TYPE;
		else
			return "unknown type";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		String table = getTable(uri);
		
		if (BuildConfig.DEBUG && values.containsKey(AppInfosTable.COLUMN_VALUE))
			Log.w("contentProvider", "insert in " + table + " : " + values.getAsString(AppInfosTable.COLUMN_VALUE));
		
		id = sqlDB.insertOrThrow(table, null, values);
		this.getContext().getContentResolver().notifyChange(uri, null);
		
		return Uri.parse(CONTENT_URI_BASE + "/" + table + "/" + id);
	}

	@Override
	public boolean onCreate() {
		database = new AccountsDatabaseHelper(this.getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
//		if (BuildConfig.DEBUG)
//			Log.w("contentProvider", "query : " + uri);
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		String table = getTable(uri);

		checkColumns(projection, table);

		queryBuilder.setTables(table);

		int uriType = typeUriMatcher.match(uri);
		switch (uriType) {
		case ITEMS:
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "type : items");
			break;
		case ITEM_ID:
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "type : item_id");
			String id = uri.getLastPathSegment();
			queryBuilder.appendWhere(Table.COLUMN_ID + "=" + id);
			break;
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
		int uriType = typeUriMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowUpdated = 0;

		String table = getTable(uri);

		switch (uriType) {
		case ITEMS:
			rowUpdated = sqlDB.update(table, values, selection, selectionArgs);
			break;
		case ITEM_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowUpdated = sqlDB.update(table, values, Table.COLUMN_ID + "="
						+ id, null);
			} else {
				rowUpdated = sqlDB.update(table, values, Table.COLUMN_ID + "="
						+ id + " and " + selection, selectionArgs);
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
		} else if (table.equals(PATH_TYPES)) {
			available = TypeTable.available;
		} else if (table.equals(PATH_APP_INFOS)) {
			available = AppInfosTable.available;
		}
		if (projection != null) {
			HashSet<String> requestedCols = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableCols = new HashSet<String>(
					Arrays.asList(available));
			if (!availableCols.containsAll(requestedCols)) {
				Log.w("contentProvider", "column error");
				if (BuildConfig.DEBUG) {
					for (String i : requestedCols) 
						Log.w("contentProvider", "requested : " + i);
					for (String i : availableCols) 
						Log.w("contentProvider", "available : " + i);
				}
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

	private String getTable(Uri uri) {
		String table = uri.getPathSegments().get(0);
		if (!table.equals(PATH_ACCOUNTS) && !table.equals(PATH_TRANSACTIONS)
				&& !table.equals(PATH_TYPES) && !table.equals(PATH_APP_INFOS)) {
			if (BuildConfig.DEBUG)
				Log.w("contentProvider", "path : " + table);
			throw new IllegalArgumentException("Unknown URI : " + uri);
		}
		return table;
	}
}
