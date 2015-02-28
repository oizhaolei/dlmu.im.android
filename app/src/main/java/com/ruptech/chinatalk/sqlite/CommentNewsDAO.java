package com.ruptech.chinatalk.sqlite;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.sqlite.TableContent.CommentNewsTable;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class CommentNewsDAO {
	private static final String TAG = Utils.CATEGORY
			+ CommentNewsDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private static final RowMapper<CommentNews> mRowMapper = new RowMapper<CommentNews>() {

		@Override
		public CommentNews mapRow(Cursor cursor, int rowNum) {
			CommentNews commentNews = CommentNewsTable.parseCursor(cursor);
			return commentNews;
		}

	};

	public CommentNewsDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());
	}

	public void deleteAll(String type) {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + CommentNewsTable.TABLE_NAME + " WHERE "
				+ CommentNewsTable.Columns.TYPE + " = '" + type + "'";
		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + CommentNewsTable.TABLE_NAME;
		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void deleteCommentNews(Long commentNewsId) {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + CommentNewsTable.TABLE_NAME + " where "
				+ CommentNewsTable.Columns.ID + " = ?";

		mDb.execSQL(sql, new String[] { String.valueOf(commentNewsId) });
	}

	/**
	 * Find a CommentNews by CommentNews ID
	 *
	 * @param CommentNewsId
	 * @return
	 */
	public CommentNews fetchCommentNews(Long commentNewsId) {
		return mSqlTemplate.queryForObject(mRowMapper,
				CommentNewsTable.TABLE_NAME, null, CommentNewsTable.Columns.ID
						+ " = ?",
				new String[] { String.valueOf(commentNewsId) }, null, null,
				"created_at DESC", "1");
	}

	public Cursor fetchCommentNewsCursor(String type) {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select * ");
		sqlB.append(" from " + CommentNewsTable.TABLE_NAME + " where "
				+ CommentNewsTable.Columns.TYPE + " = '" + type + "' order by "
				+ CommentNewsTable.Columns.ID + " desc");
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);

		return cursor;
	}

	public void gc() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + CommentNewsTable.TABLE_NAME
				+ " WHERE "
				+ CommentNewsTable.Columns.ID
				+ " NOT IN "
				+ " (SELECT "// 子句
				+ CommentNewsTable.Columns.ID + " FROM "
				+ CommentNewsTable.TABLE_NAME + " ORDER BY "
				+ CommentNewsTable.Columns.ID + " DESC LIMIT "
				+ AppPreferences.MAX_USER_PHOTO_GC_NUM + ")";
		mDb.execSQL(sql);
	}

	public long getMaxId(String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select max(")
				.append(CommentNewsTable.Columns.ID)
				.append(") ")
				.append(" from ")
				.append(CommentNewsTable.TABLE_NAME + " where "
						+ CommentNewsTable.Columns.TYPE + " = '" + type + "'");

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sb.toString(), null);
		long maxCount = 0;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				maxCount = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}

		return maxCount;
	}

	public long getMinId(String type) {
		StringBuilder sb = new StringBuilder();
		sb.append("select min(")
				.append(CommentNewsTable.Columns.ID)
				.append(") ")
				.append(" from ")
				.append(CommentNewsTable.TABLE_NAME + " where "
						+ CommentNewsTable.Columns.TYPE + " = '" + type + "'");

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sb.toString(), null);
		long minCount = 0;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				minCount = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}

		return minCount;
	}

	/**
	 * Insert a CommentNews
	 *
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @param CommentNews
	 * @param isUnread
	 * @return
	 */
	public long insertCommentNews(CommentNews CommentNews) {
		if (!isExists(CommentNews)) {
			return mSqlTemplate.getDb(true).insert(CommentNewsTable.TABLE_NAME,
					null, CommentNewsTable.toContentValues(CommentNews));
		} else {
			if (BuildConfig.DEBUG)
				Log.e(TAG, CommentNews.getId() + " is exists.");
			return -1;
		}
	}

	public int insertCommentNews(List<CommentNews> CommentNewss,
			SQLiteDatabase db) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "insertCommentNewss : " + CommentNewss);
		int result = 0;
		deleteAll();
		for (int i = CommentNewss.size() - 1; i >= 0; i--) {
			CommentNews CommentNews = CommentNewss.get(i);

			long id = -1;
			try {
				id = db.insertOrThrow(CommentNewsTable.TABLE_NAME, null,
						CommentNewsTable.toContentValues(CommentNews));
			} catch (SQLException e) {
				Utils.sendClientException(e);
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}
			if (-1 == id) {
				if (BuildConfig.DEBUG)
					Log.e(TAG,
							"cann't insert the CommentNews : "
									+ CommentNews.toString());
			} else {
				++result;
				if (BuildConfig.DEBUG)
					Log.v(TAG, String.format(
							"Insert a CommentNews into database : %d, %s", id,
							CommentNews.toString()));
			}
		}

		return result;
	}

	/**
	 * Check if CommentNews exists
	 *
	 *
	 * @param CommentNews
	 * @return
	 */
	public boolean isExists(CommentNews CommentNews) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(CommentNewsTable.TABLE_NAME)
				.append(" WHERE ").append(CommentNewsTable.Columns.ID)
				.append(" =? ");

		return mSqlTemplate.existsBySQL(sql.toString(),
				new String[] { String.valueOf(CommentNews.getId()) });
	}

	/**
	 * Update by using {@link CommentNews}
	 *
	 * @param CommentNews
	 * @return
	 */
	public int updateCommentNews(CommentNews CommentNews) {
		return updateCommentNews(CommentNews.getId(),
				CommentNewsTable.toContentValues(CommentNews));
	}

	/**
	 * Update by using {@link ContentValues}
	 *
	 * @param CommentNewsId
	 * @param newValues
	 * @return
	 */
	public int updateCommentNews(Long CommentNewsId, ContentValues values) {
		return mSqlTemplate.updateById(CommentNewsTable.TABLE_NAME,
				String.valueOf(CommentNewsId), values);
	}
}
