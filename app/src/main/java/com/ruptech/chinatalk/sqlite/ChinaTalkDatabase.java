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

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.BuildConfig;

import static com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

public class ChinaTalkDatabase {
    /**
     * Database Version
     */
    public static final int DATABASE_VERSION = 70;
    private static final String TAG = Utils.CATEGORY
            + ChinaTalkDatabase.class.getSimpleName();

    /**
     * SQLite Database file name
     */
    private static final String DATABASE_NAME = "chinatalk.db";
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
     * self instance
     */
    private static ChinaTalkDatabase sInstance = null;
    /**
     * SQLiteDatabase Open Helper
     */
    private DatabaseHelper mOpenHelper = null;

    /**
     * Construct
     */
    private ChinaTalkDatabase(Context context) {
        mOpenHelper = new DatabaseHelper(context, mCursorFactory);
    }

    // indexes
    private static void createAllIndexes(SQLiteDatabase db) {
        db.execSQL(UserTable.getCreateIndexSQL());

        db.execSQL(ChatTable.getCreateIndexSQL());
    }

    // Create All tables
    private static void createAllTables(SQLiteDatabase db) {
        db.execSQL(UserTable.getCreateSQL());
        db.execSQL(ChatTable.getCreateSQL());
    }

    private static void dropAllTables(SQLiteDatabase db) {
        db.execSQL(UserTable.getDropSQL());
        db.execSQL(ChatTable.getDropSQL());
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

            dropAllTables(db);
            createAllTables(db);
            createAllIndexes(db);
        }

    }
}
