package com.ruptech.chinatalk.sqlite;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class ChannelDAO {
	private static final String TAG = Utils.CATEGORY
			+ ChannelDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private static final RowMapper<Channel> mRowMapper = new RowMapper<Channel>() {

		@Override
		public Channel mapRow(Cursor cursor, int rowNum) {
			Channel Channel = ChannelTable.parseCursor(cursor);
			return Channel;
		}

	};

	public ChannelDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());
	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + ChannelTable.getName();

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void deleteChannel(Long channelId) {
		SQLiteDatabase db = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + ChannelTable.getName() + " where "
				+ ChannelTable.Columns.ID + " = ?";

		db.execSQL(sql, new String[] { String.valueOf(channelId) });
	}

	/**
	 * Find a Channel by Channel ID
	 * 
	 * @param channelId
	 * @return
	 */
	public Channel fetchChannel(Long channelId) {
		return mSqlTemplate.queryForObject(mRowMapper, ChannelTable.getName(),
				null, ChannelTable.Columns.ID + " = ?",
				new String[] { String.valueOf(channelId) }, null, null,
				"_id DESC", "1");
	}

	public Cursor fetchPopularCursor() {
		StringBuilder sqlB = new StringBuilder();
		sqlB.append("select * ");
		sqlB.append(" from " + ChannelTable.getName() + " order by "
				+ ChannelTable.Columns.RECOMMEND + " desc ,"
				+ ChannelTable.Columns.FOLLOWER_COUNT + " desc");
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				null);

		return cursor;
	}

	public void gc() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + ChannelTable.getName() + " WHERE "
				+ ChannelTable.Columns.ID
				+ " NOT IN "
				+ " (SELECT "// 子句
				+ ChannelTable.Columns.ID + " FROM " + ChannelTable.getName()
				+ " ORDER BY " + ChannelTable.Columns.ID + " DESC LIMIT "
				+ AppPreferences.MAX_USER_PHOTO_GC_NUM + ")";
		mDb.execSQL(sql);
	}

	public long getMaxId() {
		StringBuilder sb = new StringBuilder();
		sb.append("select max(").append(ChannelTable.Columns.POPULAR_ID)
				.append(") ").append(" from ").append(ChannelTable.getName());

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

	public long getMinId() {
		StringBuilder sb = new StringBuilder();
		sb.append("select min(").append(ChannelTable.Columns.POPULAR_ID)
				.append(") ").append(" from ").append(ChannelTable.getName());

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
	 * Insert a Channel
	 * 
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 * 
	 * @param channel
	 * @param isUnread
	 * @return
	 */
	public long insertChannel(Channel channel) {
		if (!isExists(channel)) {
			return mSqlTemplate.getDb(true).insert(ChannelTable.getName(),
					null, ChannelTable.toContentValues(channel));
		} else {
			if (BuildConfig.DEBUG)
				Log.e(TAG, channel.getId() + " is exists.");
			return -1;
		}
	}

	public int insertChannels(List<Channel> channels, SQLiteDatabase db) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "insertChannels : " + channels);
		int result = 0;
		deleteAll();
		for (int i = channels.size() - 1; i >= 0; i--) {
			Channel channel = channels.get(i);

			long id = -1;
			try {
				id = db.insertOrThrow(ChannelTable.getName(), null,
						ChannelTable.toContentValues(channel));
			} catch (SQLException e) {
				Utils.sendClientException(e);
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}
			if (-1 == id) {
				if (BuildConfig.DEBUG)
					Log.e(TAG,
							"cann't insert the Channel : " + channel.toString());
			} else {
				++result;
				if (BuildConfig.DEBUG)
					Log.v(TAG, String.format(
							"Insert a Channel into database : %d, %s", id,
							channel.toString()));
			}
		}

		return result;
	}

	/**
	 * Check if Channel exists
	 * 
	 * 
	 * @param Channel
	 * @return
	 */
	public boolean isExists(Channel Channel) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(ChannelTable.getName())
				.append(" WHERE ").append(ChannelTable.Columns.ID)
				.append(" =? ");

		return mSqlTemplate.existsBySQL(sql.toString(),
				new String[] { String.valueOf(Channel.getId()) });
	}

	/**
	 * Update by using {@link Channel}
	 * 
	 * @param Channel
	 * @return
	 */
	public int updateChannel(Channel Channel) {
		return updateChannel(Channel.getId(),
				ChannelTable.toContentValues(Channel));
	}

	/**
	 * Update by using {@link ContentValues}
	 * 
	 * @param ChannelId
	 * @param newValues
	 * @return
	 */
	public int updateChannel(Long ChannelId, ContentValues values) {
		return mSqlTemplate.updateById(ChannelTable.getName(),
				String.valueOf(ChannelId), values);
	}
}
