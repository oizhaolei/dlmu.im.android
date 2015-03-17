package com.ruptech.chinatalk.sqlite;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.sqlite.TableContent.MessageTable;
import com.ruptech.chinatalk.utils.AppPreferences;

public class MessageProvider extends ContentProvider {

	public static final String AUTHORITY = "com.ruptech.chinatalk.provider.Messages";
	public static final String TABLE_NAME = MessageTable.getName();
	public static final String QUERY_URI = "messages";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + QUERY_URI);

	private static final UriMatcher URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int MESSAGES = 1;
	private static final int MESSAGE_ID = 2;

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yaxim.message";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yaxim.message";
	public static final String DEFAULT_SORT_ORDER = "_id ASC"; // sort by

	static {
		URI_MATCHER.addURI(AUTHORITY, QUERY_URI, MESSAGES);
		URI_MATCHER.addURI(AUTHORITY, QUERY_URI + "/#", MESSAGE_ID);
	}

	private static final String TAG = MessageProvider.class.getName();
	private SQLiteOpenHelper mOpenHelper;

	public MessageProvider() {
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

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		long rowId = db.insert(TABLE_NAME, null, values);

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
			Log.i(TAG, "ChatProvider.query: failed");
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

		Log.i(TAG, "*** notifyChange() rowId: " + rowId + " url " + url);

		getContext().getContentResolver().notifyChange(url, null);
		return count;

	}

	public static void saveTranslatedContent(ContentResolver contentResolver,String messageId, String to_content) {
		ContentValues cv = new ContentValues();
		cv.put(MessageTable.Columns.TO_CONTENT, to_content);

		contentResolver.update(MessageProvider.CONTENT_URI, cv, MessageTable.Columns.ID
				+ " = ?  ", new String[]{messageId});
		contentResolver.notifyChange(MessageProvider.CONTENT_URI, null);
	}

    public static void mergeMessage(ContentResolver contentResolver, Message message){
        ContentValues cv = new ContentValues();
        cv.put(TableContent.MessageTable.Columns.MESSAGEID, message.getMessageid());
        cv.put(TableContent.MessageTable.Columns.FROM_VOICE_ID, message.getFrom_voice_id());
        cv.put(TableContent.MessageTable.Columns.TO_CONTENT, message.getTo_content());
        cv.put(TableContent.MessageTable.Columns.STATUS_TEXT, message.getStatus_text());
        cv.put(TableContent.MessageTable.Columns.FEE, message.getFee());
        cv.put(TableContent.MessageTable.Columns.AUTO_TRANSLATE, message.getAuto_translate());
        cv.put(TableContent.MessageTable.Columns.TO_USER_FEE, message.getTo_user_fee());
        cv.put(TableContent.MessageTable.Columns.ACQUIRE_DATE, message.getAcquire_date());
        cv.put(TableContent.MessageTable.Columns.TRANSLATED_DATE, message.getTranslated_date());
        cv.put(TableContent.MessageTable.Columns.VERIFY_STATUS, message.getVerify_status());
        cv.put(TableContent.MessageTable.Columns.MESSAGE_STATUS, message.getMessage_status());
        cv.put(TableContent.MessageTable.Columns.CREATE_ID, message.getCreate_id());
        cv.put(TableContent.MessageTable.Columns.CREATE_DATE, message.getCreate_date());
        cv.put(TableContent.MessageTable.Columns.UPDATE_ID, message.getUpdate_id());
        cv.put(TableContent.MessageTable.Columns.UPDATE_DATE, message.getUpdate_date());
        cv.put(TableContent.MessageTable.Columns.FILE_PATH, message.getFile_path());
        cv.put(TableContent.MessageTable.Columns.FILE_TYPE, message.getFile_type());

        contentResolver.update(MessageProvider.CONTENT_URI, cv, TableContent.MessageTable.Columns.ID
                + " = ?  ", new String[]{String.valueOf(message.getId())});
    }

    public static void insertMessage(ContentResolver contentResolver, Message message){
        ContentValues values = new ContentValues();
        values.put(TableContent.MessageTable.Columns.ID, message.getId());
        values.put(TableContent.MessageTable.Columns.MESSAGEID, message.getMessageid());
        values.put(TableContent.MessageTable.Columns.USERID, message.getUserid());
        values.put(TableContent.MessageTable.Columns.TO_USERID, message.getTo_userid());
        values.put(TableContent.MessageTable.Columns.FROM_LANG, message.from_lang);
        values.put(TableContent.MessageTable.Columns.TO_LANG, message.to_lang);
        values.put(TableContent.MessageTable.Columns.FROM_CONTENT, message.from_content);
        values.put(TableContent.MessageTable.Columns.MESSAGE_STATUS, message.getMessage_status());
        values.put(TableContent.MessageTable.Columns.STATUS_TEXT, message.getStatus_text());
        values.put(TableContent.MessageTable.Columns.FILE_PATH, message.getFile_path());
        values.put(TableContent.MessageTable.Columns.FROM_CONTENT, message.getFrom_content());
        values.put(TableContent.MessageTable.Columns.FILE_TYPE, message.getFile_type());
        values.put(TableContent.MessageTable.Columns.CREATE_DATE, message.getCreate_date());
        values.put(TableContent.MessageTable.Columns.UPDATE_DATE, message.getUpdate_date());

        contentResolver.insert(MessageProvider.CONTENT_URI, values);
    }

    public static void changeMessageStatus(Context context,Message message){
        ContentValues cv = new ContentValues();
        cv.put(TableContent.MessageTable.Columns.MESSAGE_STATUS, message.getMessage_status());
        cv.put(TableContent.MessageTable.Columns.STATUS_TEXT, message.getStatus_text());
        context.getContentResolver().update(MessageProvider.CONTENT_URI, cv, TableContent.MessageTable.Columns.ID
                + " = ?  " , new String[]{String.valueOf(message.getId())});
    }
}
