package com.example.contentprovidersample;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * A sample Content Provider.
 * @author Ashwini Shahapurkar, http://androidcookbook.com/Recipe.seam?recipeId=1558
 * @author Ian Darwin - fleshed out
 */
public class MyContentProvider extends ContentProvider {

	MyDatabaseHelper mDatabase;
	public static final String AUTHORITY = "com.example.contentprovidersample";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public static final String TABLE_NAME = "mydata";
	
	public static final String[] COLUMNS = { "_id", "content" };

	private static final UriMatcher matcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int ITEM = 0;
	private static final int ITEMS = 1;
	static {
		matcher.addURI(AUTHORITY, "items/#", ITEM);
		matcher.addURI(AUTHORITY, "items", ITEMS);
	}
	
	@Override
	public boolean onCreate() {
		mDatabase = new MyDatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		int matchType = matcher.match(uri);
		switch (matchType) {
		case ITEM:
			return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/item";
		case ITEMS:
			return ContentResolver.CURSOR_DIR_BASE_TYPE + "/item";
		default:
			throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
		}
	}

	/** The C of CRUD */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(Constants.TAG, "MyContentProvider.insert()");
		if (matcher.match(uri) == ITEM) {
			throw new RuntimeException("Cannot specify ID when inserting");
		}
		long id = mDatabase.getWritableDatabase().insert(
				TABLE_NAME, null, values);
		uri = Uri.withAppendedPath(uri, "/" + id);
		return uri;
	}
	
	/** The R of CRUD */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(Constants.TAG, "MyContentProvider.query()");
		// build the query with SQLiteQueryBuilder
		SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
		qBuilder.setTables(TABLE_NAME);

		// query the database and get result in cursor
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		Cursor resultCursor = qBuilder.query(db,
				projection, selection, selectionArgs, null, null, sortOrder,
				null);
		resultCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return resultCursor;

	}

	/** The U of CRUD */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(Constants.TAG, "MyContentProvider.update()");
		return mDatabase.getWritableDatabase().update(
				TABLE_NAME, values, selection, selectionArgs);
	}
	
	/** The D of CRUD */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(Constants.TAG, "MyContentProvider.delete()");
		return mDatabase.getWritableDatabase().delete(TABLE_NAME, selection, selectionArgs);
	}
	
	/**
	 * Typical Android SQLite DB Helper class
	 */
	final static class MyDatabaseHelper extends SQLiteOpenHelper {

		public static final String DBNAME = "data_db.sqlite";
		public static final int VERSION = 1;
		
		public MyDatabaseHelper(Context context) {
			// Super's constructor arguments:
			// Context, database name, CursorFactory object (may be null), 
			// database schema version number (used to decide when to run 
			// the onUpdate() method.
			super(context, DBNAME, null, VERSION);
		}

		// CREATE TABLE <table-name> (column1 INTEGER PRIMARY KEY AUTOINCREMENT
		// NOT NULL, column2 TEXT);

		public void onCreate(SQLiteDatabase db) {
			createDatabase(db);
		}

		private void createDatabase(SQLiteDatabase db) {
			db.execSQL("create table " + TABLE_NAME + "(" +
					COLUMNS[0] + " integer primary key autoincrement not null, " + 
					COLUMNS[1] + " varchar " + 
					");");
			for (int i = 0; i < 3; i++) {
				db.execSQL(
					"insert into " + TABLE_NAME + "(" + COLUMNS[1] + ") values ('" + "Item " + i + "')");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			throw new IllegalStateException(
					"No versions exist yet, this should not get called.");
		}
	}

}