package com.ruptech.chinatalk.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.sqlite.ChinaTalkDatabase;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;

public class ChatProvider extends ContentProvider {

    public static final String AUTHORITY = "com.ruptech.chinatalk.provider.Chats";
    public static final String TABLE_NAME = ChatTable.getName();
    public static final String QUERY_URI = "chats";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + QUERY_URI);

    private static final UriMatcher URI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int MESSAGES = 1;
    private static final int MESSAGE_ID = 2;

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yaxim.chat";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yaxim.chat";
    public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by
    public static final String PACKET_ID = "pid";
    // boolean mappings
    public static final int DS_NEW = 0; // < this message has not been
    // sent/displayed yet
    public static final int DS_SENT_OR_READ = 1; // < this message was sent
    // but not yet acked, or
    // it was received and
    // read
    public static final int DS_ACKED = 2; // < this message was XEP-0184

    static {
        URI_MATCHER.addURI(AUTHORITY, QUERY_URI, MESSAGES);
        URI_MATCHER.addURI(AUTHORITY, QUERY_URI + "/#", MESSAGE_ID);
    }

    private static final String TAG = "ChatProvider";
	private SQLiteOpenHelper mOpenHelper;

    public ChatProvider() {
    }

    private static void infoLog(String data) {
        Log.i(TAG, data);
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(url)) {

            case MESSAGES:
                count = db.delete(TABLE_NAME, where, whereArgs);
                break;
            case MESSAGE_ID:
                String segment = url.getPathSegments().get(1);

                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + segment;
                } else {
                    where = "_id=" + segment + " AND (" + where + ")";
                }

                count = db.delete(TABLE_NAME, where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }

        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public String getType(Uri url) {
        int match = URI_MATCHER.match(url);
        switch (match) {
            case MESSAGES:
                return CONTENT_TYPE;
            case MESSAGE_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (URI_MATCHER.match(url) != MESSAGES) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        for (String colName : ChatTable.getRequiredColumns()) {
            if (values.containsKey(colName) == false) {
                throw new IllegalArgumentException("Missing column: " + colName);
            }
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(TABLE_NAME, ChatTable.Columns.CREATED_DATE, values);

        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }

        Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(noteUri, null);
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

        switch (match) {
            case MESSAGES:
                qBuilder.setTables(TABLE_NAME);
                break;
            case MESSAGE_ID:
                qBuilder.setTables(TABLE_NAME);
                qBuilder.appendWhere("_id=");
                qBuilder.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qBuilder.query(db, projectionIn, selection, selectionArgs,
                null, null, orderBy);

        if (ret == null) {
            infoLog("ChatProvider.query: failed");
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
            case MESSAGES:
                count = db.update(TABLE_NAME, values, where, whereArgs);
                break;
            case MESSAGE_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update(TABLE_NAME, values, "_id=" + rowId, null);
                break;
            default:
                throw new UnsupportedOperationException("Cannot update URL: " + url);
        }

        infoLog("*** notifyChange() rowId: " + rowId + " url " + url);

        getContext().getContentResolver().notifyChange(url, null);
        return count;

    }

}
