package com.ruptech.chinatalk.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.utils.Utils;

import static com.ruptech.chinatalk.sqlite.TableContent.HotUserPhotoTable;

public class HotUserPhotoDAO {

	private static final String TAG = Utils.CATEGORY
			+ HotUserPhotoDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	public HotUserPhotoDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());
	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + HotUserPhotoTable.getName();

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	// TIMELINE_ID 代表热门贴图的ID
	public Cursor fetchHotPopularCursor() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select * ");

		sqlB.append(" from " + HotUserPhotoTable.getName());

		sqlB.append(" order by " + HotUserPhotoTable.Columns.TIMELINE_ID
				+ " desc");
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);

		return cursor;
	}

	public long getMaxId() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select max(" + HotUserPhotoTable.Columns.TIMELINE_ID
				+ ") ");
		sqlB.append(" from " + HotUserPhotoTable.getName());

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);
		long sinceId = 0;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				sinceId = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}

		return sinceId;
	}

	public long getMinId() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select min(" + HotUserPhotoTable.Columns.TIMELINE_ID
				+ ") ");

		sqlB.append(" from " + HotUserPhotoTable.getName());

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);
		long maxId = 0;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				maxId = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}

		return maxId;
	}

	/**
	 * Insert a UserPhoto
	 *
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @param UserPhoto
	 * @param isUnread
	 * @return
	 */
	public long insertHotUserPhotoTable(UserPhoto userPhoto) {
		try {
			return mSqlTemplate.getDb(true).insertOrThrow(
					HotUserPhotoTable.getName(), null,
					HotUserPhotoTable.toContentValues(userPhoto, true));
		} catch (SQLException e) {
		}
		return -1;
	}
}
