package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.UserProp;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPropTable;

class UserPropDAO {
	private static final String TAG = Utils.CATEGORY + UserPropDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private static final RowMapper<UserProp> mRowMapper = new RowMapper<UserProp>() {

		@Override
		public UserProp mapRow(Cursor cursor, int rowNum) {
			UserProp userProp = UserPropTable.parseCursor(cursor);
			return userProp;
		}

	};

	public UserPropDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase.getInstance(context).getSQLiteOpenHelper());
	}

	// /**
	// * @see UserPropDAO#fetchUserProps(String, int)
	// */
	// public List<UserProp> fetchUserProps(String userId, String
	// UserPropType) {
	// return fetchUserProps(userId, Integer.parseInt(UserPropType));
	// }
	public int deleteUserPropsByuserid(Long userid) {
		return mSqlTemplate.deleteByField(UserPropTable.getName(), UserPropTable.Columns.USERID,
				String.valueOf(userid));
	}

	public long fetchMinUserPropId(Long userid) {
		String sql = "SELECT min(" + UserPropTable.Columns.ID + ") FROM " + UserPropTable.getName() + " WHERE "
				+ UserPropTable.Columns.USERID + " = ?";
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sql + " LIMIT 1", new String[] { String.valueOf(userid) });

		long result = -1;

		if (cursor == null) {
			return result;
		}

		cursor.moveToFirst();
		if (cursor.getCount() == 0) {
			result = -1;
		} else {
			result = cursor.getLong(0);
		}
		cursor.close();

		return result;
	}

	/**
	 * Find a UserProp by UserProp ID
	 *
	 * @param UserPropId
	 * @return
	 */
	public UserProp fetchUserProp(Long UserPropId) {
		return mSqlTemplate.queryForObject(mRowMapper, UserPropTable.getName(), null, UserPropTable.Columns.ID
				+ " = ?", new String[] { String.valueOf(UserPropId) }, null, null, "created_at DESC", "1");
	}

	/**
	 * Find user's UserProps
	 *
	 * @param categoryIndex
	 *            user id
	 * @return list of UserProps
	 */
	public List<UserProp> fetchUserProps(Long userid) {
		List<UserProp> list = mSqlTemplate.queryForList(mRowMapper, UserPropTable.getName(), null,
				UserPropTable.Columns.USERID + " = ?", new String[] { String.valueOf(userid) }, null, null,
				"created_at DESC", null);
		if (BuildConfig.DEBUG)
			Log.v(TAG, "fetchUserProps : " + list);
		return list;
	}

	/**
	 * Insert a UserProp
	 *
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @param UserProp
	 * @param isUnread
	 * @return
	 */
	public long insertUserProp(UserProp userProp) {
		if (!isExists(userProp)) {
			return mSqlTemplate.getDb(true).insert(UserPropTable.getName(), null,
					UserPropTable.toContentValues(userProp));
		} else {
			if (BuildConfig.DEBUG)
				Log.e(TAG, userProp.getId() + " is exists.");
			return -1;
		}
	}

	// TODO:
	public int insertUserProps(Long userid, List<UserProp> userProps, SQLiteDatabase db) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "insertUserProps : " + userProps);
		int result = 0;
		deleteUserPropsByuserid(userid);
		for (int i = userProps.size() - 1; i >= 0; i--) {
			UserProp userProp = userProps.get(i);

			long id = -1;
			try {
				id = db.insertOrThrow(UserPropTable.getName(), null, UserPropTable.toContentValues(userProp));
			} catch (SQLException e) {
				Utils.sendClientException(e);
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}
			if (-1 == id) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, "cann't insert the UserProp : " + userProp.toString());
			} else {
				++result;
				if (BuildConfig.DEBUG)
					Log.v(TAG, String.format("Insert a UserProp into database : %d, %s", id, userProp.toString()));
			}
		}

		return result;
	}

	/**
	 * Check if UserProp exists
	 *
	 * FIXME: 取消使用Query
	 *
	 * @param UserProp
	 * @return
	 */
	public boolean isExists(UserProp userProp) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(UserPropTable.getName()).append(" WHERE ")
				.append(UserPropTable.Columns.ID).append(" =? ");

		return mSqlTemplate.existsBySQL(sql.toString(), new String[] { String.valueOf(userProp.getId()) });
	}

	/**
	 * Update by using {@link ContentValues}
	 *
	 * @param UserPropId
	 * @param newValues
	 * @return
	 */
	public int updateUserProp(Long UserPropId, ContentValues values) {
		return mSqlTemplate.updateById(UserPropTable.getName(), String.valueOf(UserPropId), values);
	}

	/**
	 * Update by using {@link UserProp}
	 *
	 * @param UserProp
	 * @return
	 */
	public int updateUserProp(UserProp userProp) {
		return updateUserProp(userProp.getId(), UserPropTable.toContentValues(userProp));
	}

}
