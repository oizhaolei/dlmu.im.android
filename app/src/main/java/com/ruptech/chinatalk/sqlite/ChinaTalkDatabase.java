package com.ruptech.chinatalk.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.chinatalk.utils.Utils;

import static com.ruptech.chinatalk.sqlite.TableContent.ChatRoomTable;
import static com.ruptech.chinatalk.sqlite.TableContent.ChatRoomUserTable;
import static com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import static com.ruptech.chinatalk.sqlite.TableContent.FriendTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

public class ChinaTalkDatabase {
	/**
	 * SQLiteOpenHelper
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		// Construct
		public DatabaseHelper(Context context, CursorFactory cf) {
			super(context, DATABASE_NAME, cf, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Create Database.");
			createAllTables(db);
			createAllIndexes(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Upgrade Database.");
			if (oldVersion == 57) {
				try {
					udpateTable57to67(db);
				} catch (Exception e) {
					Utils.sendClientException(e);
				}
			} else {
				dropAllTables(db);
				createAllTables(db);
				createAllIndexes(db);
			}
		}

	}

	private static final String TAG = Utils.CATEGORY
			+ ChinaTalkDatabase.class.getSimpleName();

	/**
	 * SQLite Database file name
	 */
	private static final String DATABASE_NAME = "chinatalk.db";

	/**
	 * Database Version
	 */
	public static final int DATABASE_VERSION = 67;

	/**
	 * self instance
	 */
	private static ChinaTalkDatabase sInstance = null;

	// indexes
	private static void createAllIndexes(SQLiteDatabase db) {
		db.execSQL(UserTable.getCreateIndexSQL());

		db.execSQL(FriendTable.getCreateIndexSQL());
		db.execSQL(ChatTable.getCreateIndexSQL());
		db.execSQL(ChatRoomTable.getCreateIndexSQL());
		db.execSQL(ChatRoomUserTable.getCreateIndexSQL());
	}

	// Create All tables
	private static void createAllTables(SQLiteDatabase db) {
		db.execSQL(UserTable.getCreateSQL());
		db.execSQL(FriendTable.getCreateSQL());
		db.execSQL(ChatTable.getCreateSQL());
		db.execSQL(ChatRoomTable.getCreateSQL());
		db.execSQL(ChatRoomUserTable.getCreateSQL());
	}

	private static void dropAllTables(SQLiteDatabase db) {
		db.execSQL(UserTable.getDropSQL());
		db.execSQL(FriendTable.getDropSQL());
		db.execSQL(ChatTable.getDropSQL());
		db.execSQL(ChatRoomTable.getDropSQL());
		db.execSQL(ChatRoomUserTable.getDropSQL());
	}

	/**
	 * Get Database
	 */
	public static synchronized ChinaTalkDatabase getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new ChinaTalkDatabase(context);
		}
		return sInstance;
	}

	private static void udpateTable57to67(SQLiteDatabase db) {
		db.execSQL(ChatTable.getCreateSQL());
		db.execSQL(ChatRoomTable.getCreateSQL());
		db.execSQL(ChatRoomUserTable.getCreateSQL());
		db.execSQL("ALTER TABLE " + UserTable.getName() + " ADD "
				+ UserTable.Columns.TERMINAL_TYPE + "    TEXT;");

	}

	/**
	 * SQLiteDatabase Open Helper
	 */
	private DatabaseHelper mOpenHelper = null;

	public static CursorFactory mCursorFactory = new CursorFactory() {
		@Override
		public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
		                        String editTable, SQLiteQuery query) {
			if (BuildConfig.DEBUG)
				Log.i(TAG, query.toString());
			return new SQLiteCursor(db, driver, editTable, query);
		}
	};

	/**
	 * Construct
	 */
	private ChinaTalkDatabase(Context context) {
		mOpenHelper = new DatabaseHelper(context, mCursorFactory);
	}

	/**
	 * Close Database
	 */
	public void close() {
		if (null != sInstance) {
			mOpenHelper.close();
			sInstance = null;
		}
	}

	/**
	 * Get Database Connection
	 */
	public SQLiteDatabase getDb(boolean writeable) {
		if (writeable) {
			return mOpenHelper.getWritableDatabase();
		} else {
			return mOpenHelper.getReadableDatabase();
		}
	}

	/**
	 * Get SQLiteDatabase Open Helper
	 */
	public SQLiteOpenHelper getSQLiteOpenHelper() {
		return mOpenHelper;
	}
}
