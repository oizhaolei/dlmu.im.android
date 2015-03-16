package com.ruptech.chinatalk.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.sqlite.ChinaTalkDatabase;
import com.ruptech.chinatalk.sqlite.TableContent.RosterTable;

public class RosterProvider extends ContentProvider {

    public static final String AUTHORITY = "com.ruptech.chinatalk.provider.Roster";
    public static final String TABLE_ROSTER = RosterTable.getName();
    public static final String QUERY_URI = "roster";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + QUERY_URI);
    public static final String TABLE_GROUPS = "groups";
    public static final Uri GROUPS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_GROUPS);
    public static final String QUERY_ALIAS = "main_result";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yaxim.roster";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yaxim.roster";
    public static final String DEFAULT_SORT_ORDER = RosterTable.Columns.STATUS_MODE + " DESC, "
            + RosterTable.Columns.ALIAS + " COLLATE NOCASE";

    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    private static final int GROUPS = 3;
    private static final int GROUP_MEMBERS = 4;

    static {
        URI_MATCHER.addURI(AUTHORITY, "roster", CONTACTS);
        URI_MATCHER.addURI(AUTHORITY, "roster/#", CONTACT_ID);
        URI_MATCHER.addURI(AUTHORITY, "groups", GROUPS);
        URI_MATCHER.addURI(AUTHORITY, "groups/*", GROUP_MEMBERS);
    }

    private static final String TAG = "RosterProvider";

    private Runnable mNotifyChange = new Runnable() {
        public void run() {
            Log.d(TAG, "notifying change");
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(GROUPS_URI, null);
        }
    };
	/*
	 * delay change notification, cancel previous attempts. this implements rate
	 * throttling on fast update sequences
	 */
    long last_notify = 0;
    private Handler mNotifyHandler = new Handler();
    private SQLiteOpenHelper mOpenHelper;

    public RosterProvider() {
    }

    private static void infoLog(String data) {
        Log.i(TAG, data);
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(url)) {

            case CONTACTS:
                count = db.delete(TABLE_ROSTER, where, whereArgs);
                break;

            case CONTACT_ID:
                String segment = url.getPathSegments().get(1);

                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }

                count = db.delete(TABLE_ROSTER, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(GROUPS_URI, null);
        notifyChange();

        return count;
    }

    @Override
    public String getType(Uri url) {
        int match = URI_MATCHER.match(url);
        switch (match) {
            case CONTACTS:
                return CONTENT_TYPE;
            case CONTACT_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (URI_MATCHER.match(url) != CONTACTS) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        for (String colName : RosterTable.getRequiredColumns()) {
            if (values.containsKey(colName) == false) {
                throw new IllegalArgumentException("Missing column: " + colName);
            }
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(TABLE_ROSTER, RosterTable.Columns.JID, values);

        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }

        Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);

        notifyChange();

        return noteUri;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = ChinaTalkDatabase
                .getInstance(getContext()).getSQLiteOpenHelper();
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        int match = URI_MATCHER.match(url);
        String groupBy = null;

        switch (match) {

            case GROUPS:
                qBuilder.setTables(TABLE_ROSTER + " " + QUERY_ALIAS);
                groupBy = RosterTable.Columns.GROUP;
                break;

            case GROUP_MEMBERS:
                qBuilder.setTables(TABLE_ROSTER + " " + QUERY_ALIAS);
                qBuilder.appendWhere(RosterTable.Columns.GROUP + "=");
                qBuilder.appendWhere(url.getPathSegments().get(1));
                break;

            case CONTACTS:
                qBuilder.setTables(TABLE_ROSTER + " " + QUERY_ALIAS);
                break;

            case CONTACT_ID:
                qBuilder.setTables(TABLE_ROSTER + " " + QUERY_ALIAS);
                qBuilder.appendWhere("_id=");
                qBuilder.appendWhere(url.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder) && match == CONTACTS) {
            orderBy = DEFAULT_SORT_ORDER;//默认按在线状态排序
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs,
                groupBy, null, orderBy);

        if (ret == null) {
            infoLog("RosterProvider.query: failed");
        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public int update(Uri url, ContentValues values, String where,
                      String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = URI_MATCHER.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (match) {
            case CONTACTS:
                count = db.update(TABLE_ROSTER, values, where, whereArgs);
                break;
            case CONTACT_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(TABLE_ROSTER, values, "_id=" + rowId, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }

        notifyChange();

        return count;

    }

    private void notifyChange() {
        mNotifyHandler.removeCallbacks(mNotifyChange);
        long ts = System.currentTimeMillis();
        if (ts > last_notify + 500)
            mNotifyChange.run();
        else
            mNotifyHandler.postDelayed(mNotifyChange, 200);
        last_notify = ts;
    }





}
