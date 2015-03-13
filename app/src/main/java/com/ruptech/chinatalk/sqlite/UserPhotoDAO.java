package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

public class UserPhotoDAO {

	private static final String TAG = Utils.CATEGORY
			+ UserPhotoDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private static final RowMapper<UserPhoto> mRowMapper = new RowMapper<UserPhoto>() {

		@Override
		public UserPhoto mapRow(Cursor cursor, int rowNum) {
			UserPhoto userPhoto = UserPhotoTable.parseCursor(cursor);
			return userPhoto;
		}

	};

	public UserPhotoDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());
	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + UserPhotoTable.getName();

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}


	public int deleteUserPhotosById(Long id) {
		return mSqlTemplate.deleteByField(UserPhotoTable.getName(),
				UserPhotoTable.Columns.ID, String.valueOf(id));
	}


	public Cursor fetchPopularCursor() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select * ");

		sqlB.append(" from " + UserPhotoTable.getName()
				+ " where parent_id=0 and  good >= "
				+ App.readServerAppInfo().good_min_count);

		sqlB.append(" order by " + UserPhotoTable.Columns.TIMELINE_ID + " desc");
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);

		return cursor;
	}

	/**
	 * Find user's UserPhotos
	 *
	 * @param categoryIndex
	 *            user id
	 * @return list of UserPhotos
	 */
	public List<UserPhoto> fetchUserPhotosByUserId(Long userid) {
		return mSqlTemplate.queryForList(mRowMapper, UserPhotoTable.getName(),
				null, UserPhotoTable.Columns.USERID + " = ?",
				new String[] { String.valueOf(userid) }, null, null,
				" _id DESC", null);
	}

	public void gc() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + UserPhotoTable.getName()
				+ " WHERE "
				+ UserPhotoTable.Columns.ID
				+ " NOT IN "
				+ " (SELECT "// 子句
				+ UserPhotoTable.Columns.ID + " FROM "
				+ UserPhotoTable.getName() + " ORDER BY "
				+ UserPhotoTable.Columns.ID + " DESC LIMIT "
				+ AppPreferences.MAX_USER_PHOTO_GC_NUM + ")";
		mDb.execSQL(sql);
	}

	public long getMaxId() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select max(" + UserPhotoTable.Columns.TIMELINE_ID + ") ");
		sqlB.append(" from " + UserPhotoTable.getName());

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
		sqlB.append("select min(" + UserPhotoTable.Columns.TIMELINE_ID + ") ");

		sqlB.append(" from " + UserPhotoTable.getName());

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

	public UserPhoto getStoryByUserPhotoId(long userPhotoId) {
		String sqlWhere = UserPhotoTable.Columns.ID + " = ?";

		return mSqlTemplate.queryForObject(mRowMapper,
				UserPhotoTable.getName(), null, sqlWhere,
				new String[] { String.valueOf(userPhotoId) }, null, null, null,
				"1");

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
	public long insertUserPhoto(UserPhoto userPhoto) {
		return insertUserPhoto(userPhoto, true);
	}

	public long insertUserPhoto(UserPhoto userPhoto, boolean isNew) {
		try {
			if (isNew) {
				deleteUserPhotosById(userPhoto.getId());
			}
			return mSqlTemplate.getDb(true).insertOrThrow(
					UserPhotoTable.getName(), null,
					UserPhotoTable.toContentValues(userPhoto, true));
		} catch (SQLException e) {
			// updateUserPhoto(userPhoto);
		}
		return -1;
	}

	/**
	 * Update by using {@link ContentValues}
	 *
	 * @param UserPhotoId
	 * @param newValues
	 * @return
	 */
	public int updateUserPhoto(Long UserPhotoId, ContentValues values) {
		return mSqlTemplate.updateById(UserPhotoTable.getName(),
				String.valueOf(UserPhotoId), values);
	}

	/**
	 * Update by using {@link UserPhoto}
	 *
	 * @param UserPhoto
	 * @return
	 */
	public int updateUserPhoto(UserPhoto userPhoto) {
		return updateUserPhoto(userPhoto.getId(),
				UserPhotoTable.toContentValues(userPhoto, false));
	}
}
