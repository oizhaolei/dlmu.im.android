package com.ruptech.chinatalk.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import com.ruptech.chinatalk.sqlite.TableContent.FriendTable;

public class FriendDAO {
	private static final String TAG = Utils.CATEGORY
			+ FriendDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private final RowMapper<Friend> mRowMapper = new RowMapper<Friend>() {

		@Override
		public Friend mapRow(Cursor cursor, int rowNum) {
			Friend friend = FriendTable.parseCursor(cursor);
			return friend;
		}

	};

	public FriendDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());

	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + FriendTable.getName();

		mDb.execSQL(sql);
	}

	private void deleteFriendByFriendId(Long userId, Long friendId) {

		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + FriendTable.getName() + " where user_id="
				+ userId + " and friend_id=" + friendId;
		mDb.execSQL(sql);
	}

	public Friend fetchFriend(Long userId, Long friendId) {
		String selection = FriendTable.Columns.USER_ID + " = " + userId
				+ " AND " + FriendTable.Columns.FRIEND_ID + " = " + friendId;
		return mSqlTemplate.queryForObject(mRowMapper, FriendTable.getName(),
				null, selection, null, null, null, "_id DESC", "1");
	}

	/**
	 * 添加新朋友获取当前本地最新时间
	 */
	public String getLastUpdatedate() {
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(
				"select max(create_date) from " + FriendTable.getName(), null);
		String maxUpdatedate = null;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				maxUpdatedate = cursor.getString(0);
			}
		} finally {
			cursor.close();
		}
		return maxUpdatedate;

	}

	public synchronized int insertFriends(List<Friend> friends,
			boolean deleteAll) {
		int result = 0;
		SQLiteDatabase db = mSqlTemplate.getDb(true);

		try {
			db.beginTransaction();
			if (deleteAll)
				deleteAll();
			if (friends != null)
				for (int i = friends.size() - 1; i >= 0; i--) {
					Friend friend = friends.get(i);
					mergeFriend(friend);

				}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Insert a user
	 *
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @return
	 */
	public long mergeFriend(Friend friend) {
		deleteFriendByFriendId(friend.user_id, friend.friend_id);
		// insert
		SQLiteDatabase db = mSqlTemplate.getDb(true);

		return db.insert(FriendTable.getName(), null,
				FriendTable.toContentValues(friend));
	}

	public void removeFriendByFriendId(Long userId, Long friendId) {

		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "UPDATE " + FriendTable.getName()
				+ " SET DONE = 0 where user_id=" + userId + " and friend_id="
				+ friendId;
		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void updateFriendIsTop(Long userId, Long friendId, int isTop) {

		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "UPDATE " + FriendTable.getName() + " SET IS_TOP = "
				+ isTop + " where user_id=" + userId + " and friend_id="
				+ friendId;
		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}
}