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

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.utils.Utils;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;
import static com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import static com.ruptech.chinatalk.sqlite.TableContent.CommentNewsTable;
import static com.ruptech.chinatalk.sqlite.TableContent.FriendTable;
import static com.ruptech.chinatalk.sqlite.TableContent.HotUserPhotoTable;
import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;
import static com.ruptech.chinatalk.sqlite.TableContent.RosterTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPropTable;
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
                    udpateTable57to64(db);
                } catch (Exception e) {
                    Utils.sendClientException(e);
                }
            }else if (oldVersion == 62) {
                try {
                    udpateTable62to63(db);
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
	public static final int DATABASE_VERSION = 64;

	/**
	 * self instance
	 */
	private static ChinaTalkDatabase sInstance = null;

	// indexes
	private static void createAllIndexes(SQLiteDatabase db) {
		db.execSQL(UserTable.getCreateIndexSQL());
		db.execSQL(UserPhotoTable.getCreateIndexSQL());
		db.execSQL(UserPropTable.getCreateIndexSQL());

		db.execSQL(FriendTable.getCreateIndexSQL());
		db.execSQL(MessageTable.getCreateIndexSQL());

		db.execSQL(ChannelTable.getCreateIndexSQL());

		db.execSQL(HotUserPhotoTable.getCreateIndexSQL());
		db.execSQL(CommentNewsTable.getCreateIndexSQL());
        db.execSQL(ChatTable.getCreateIndexSQL());
        db.execSQL(RosterTable.getCreateIndexSQL());
	}

	// Create All tables
	private static void createAllTables(SQLiteDatabase db) {
		db.execSQL(UserTable.getCreateSQL());
		db.execSQL(UserPhotoTable.getCreateSQL());
		db.execSQL(UserPropTable.getCreateSQL());

		db.execSQL(FriendTable.getCreateSQL());
		db.execSQL(MessageTable.getCreateSQL());

		db.execSQL(ChannelTable.getCreateSQL());

		db.execSQL(HotUserPhotoTable.getCreateSQL());

		db.execSQL(CommentNewsTable.getCreateSQL());
        db.execSQL(ChatTable.getCreateSQL());
        db.execSQL(RosterTable.getCreateSQL());
	}

	private static void dropAllTables(SQLiteDatabase db) {
		db.execSQL(UserTable.getDropSQL());
		db.execSQL(UserPhotoTable.getDropSQL());
		db.execSQL(UserPropTable.getDropSQL());

		db.execSQL(FriendTable.getDropSQL());
		db.execSQL(MessageTable.getDropSQL());

		db.execSQL(ChannelTable.getDropSQL());

		db.execSQL(HotUserPhotoTable.getDropSQL());

		db.execSQL(CommentNewsTable.getDropSQL());
        db.execSQL(ChatTable.getDropSQL());
        db.execSQL(RosterTable.getDropSQL());
	}

	/**
	 * Get Database
	 *
	 * @param context
	 * @return
	 */
	public static synchronized ChinaTalkDatabase getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new ChinaTalkDatabase(context);
		}
		return sInstance;
	}

	private static void udpateTable57to64(SQLiteDatabase db) {
        db.execSQL(ChatTable.getCreateSQL());
        db.execSQL(RosterTable.getCreateSQL());
        db.execSQL(ChatTable.getBulkInsertSQL());
        db.execSQL("ALTER TABLE " + UserTable.getName() + " ADD "
                + UserTable.Columns.TERMINAL_TYPE + "    TEXT;");
	}

    private static void udpateTable62to63(SQLiteDatabase db) {
        db.execSQL(ChatTable.getBulkInsertSQL());
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
	 *
	 * @param context
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
	 *
	 * @param writeable
	 * @return
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
	 *
	 * @return
	 */
	public SQLiteOpenHelper getSQLiteOpenHelper() {
		return mOpenHelper;
	}
}
