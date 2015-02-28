package com.ruptech.chinatalk.sqlite;

import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class MessageDAO {

	private static final String TAG = Utils.CATEGORY
			+ MessageDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private final RowMapper<Message> mRowMapper = new RowMapper<Message>() {

		@Override
		public Message mapRow(Cursor cursor, int rowNum) {
			Message message = MessageTable.parseCursor(cursor);
			return message;
		}

	};

	public MessageDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());
	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + MessageTable.getName();

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void deleteByMessageId(Long messageId) {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + MessageTable.getName()
				+ " where messageid=" + messageId;

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	public void deleteByUserId(Long userId) {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + MessageTable.getName()
				+ " where userid=" + userId + " or to_userid=" + userId;

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	/**
	 * Check if message exists
	 *
	 *
	 * @param message
	 * @return
	 */
	private boolean exists(Message message) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(*) FROM ").append(MessageTable.getName())
				.append(" WHERE _ID!=0 AND (").append(MessageTable.Columns.ID)
				.append(" =? OR ").append(MessageTable.Columns.MESSAGEID)
				.append(" =?  )");

		return mSqlTemplate.existsBySQL(
				sql.toString(),
				new String[] { String.valueOf(message.getId()),
						String.valueOf(message.messageid) });
	}

	/**
	 * Find a message by message ID
	 *
	 * @param id
	 * @return
	 */
	public Message fetchMessage(Long id) {
		return mSqlTemplate.queryForObject(mRowMapper, MessageTable.getName(),
				null, MessageTable.Columns.ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, "_id DESC",
				"1");
	}

	/**
	 * Find user's messages
	 *
	 * @param categoryIndex
	 *            user id
	 *
	 * @return list of messages 按照本地数据库倒序检索，然后再修改为正序排列，增加参数页数，使用共通变量每页显示的数量
	 */
	public Cursor fetchMessages(Long userId, Long friendId, int page) {
		String filter = "";
		String table = "";

		filter += "((" + MessageTable.Columns.USERID + " =  " + userId
				+ " and " + MessageTable.Columns.TO_USERID + " =  " + friendId
				+ ") or (" + MessageTable.Columns.USERID + " =  " + friendId
				+ " and " + MessageTable.Columns.TO_USERID + " =  " + userId
				+ "))";

		table = "(SELECT * FROM " + MessageTable.getName() + " WHERE "
				+ filter + " ORDER BY " + MessageTable.Columns.MESSAGEID
				+ " DESC LIMIT " + page * AppPreferences.PAGE_COUNT_20
				+ ") messageTable";

		Cursor cursor = mSqlTemplate.getDb(false).query(table, null, null,
				null, null, null, MessageTable.Columns.MESSAGEID, null);

		return cursor;
	}

	public Cursor fetchMessages(Long userId, Long friendId, String fromLang,
			String toLang) {
		String filter = "((" + MessageTable.Columns.USERID + " =  " + userId
				+ " and " + MessageTable.Columns.TO_USERID + " =  " + friendId
				+ ") or (" + MessageTable.Columns.USERID + " =  " + friendId
				+ " and " + MessageTable.Columns.TO_USERID + " =  " + userId
				+ ")) AND " + MessageTable.Columns.FROM_LANG + " in ( '"
				+ fromLang + "','" + toLang + "') AND "
				+ MessageTable.Columns.TO_LANG + " in ('" + toLang + "','"
				+ fromLang + "')";

		Cursor cursor = mSqlTemplate.getDb(false).query(
				MessageTable.getName(), null, filter, null, null, null,
				MessageTable.Columns.MESSAGEID, null);

		return cursor;
	}

	public Message fetchNewestMessageByUser(Long userId, Long friendId) {
		String[] args = new String[] { userId.toString(), friendId.toString(),
				friendId.toString(), userId.toString() };
		return mSqlTemplate
				.queryForObject(
						mRowMapper,
						MessageTable.getName(),
						null,
						MessageTable.Columns.MESSAGEID
								+ " in (select max(messageid) from tbl_message where (userid=? and to_userid=?) or (userid=? and to_userid=?))",
						args, null, null, "messageid DESC", "1");

	}

	/**
	 * 1，删除超过MAX_MSG_GC_NUM垃圾数据 2，清理无需数据
	 */
	public void gc() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + MessageTable.getName() + " WHERE "
				+ MessageTable.Columns.ID
				+ " NOT IN "
				+ " (SELECT "// 子句
				+ MessageTable.Columns.ID + " FROM " + MessageTable.getName()
				+ " ORDER BY " + MessageTable.Columns.ID + " DESC LIMIT "
				+ AppPreferences.MAX_MSG_GC_NUM + ")";
		mDb.execSQL(sql);
		// 发送中的message变成发送失败
		sql = "UPDATE " + MessageTable.getName()
				+ " set message_status = -1 where message_status = 0";
		mDb.execSQL(sql);
	}

	public String getMaxLastUpdatedate() {
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(
				"select max(update_date) from tbl_message ", null);
		String maxUpdateDate = null;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				maxUpdateDate = cursor.getString(0);
			}
		} finally {
			cursor.close();
		}

		return maxUpdateDate;

	}

	// 获取本地当前和该好友的最小message id，以便去服务器端获取本地不存在历史记录
	public long getMinMessageId(long friendId) {
		long userId = App.readUser().getId();
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(
				"select min(messageid) from tbl_message where (userid = "
						+ userId + " and to_userid = " + friendId
						+ ") or (userid = " + friendId + " and to_userid = "
						+ userId + ");", null);
		long minMessageId = 0;
		try {

			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				minMessageId = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}

		return minMessageId;

	}

	/**
	 * 合并或者事先清空数据表再插入
	 *
	 * @param messageList
	 * @param deleteAll
	 * @return
	 */
	public synchronized int insertMessages(List<Message> messageList,
			boolean deleteAll) {
		int result = 0;
		SQLiteDatabase db = mSqlTemplate.getDb(true);

		try {
			db.beginTransaction();
			if (deleteAll)
				deleteAll();

			if (messageList != null)
				for (int i = messageList.size() - 1; i >= 0; i--) {
					Message message = messageList.get(i);
					mergeMessage(message);
				}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Insert a Message
	 *
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @param message
	 * @return
	 */
	public long mergeMessage(Message message) {
		if (message.getId() <= 0) {// patch : valid local id
			message.setId(System.currentTimeMillis());
		}
		if (!exists(message)) {
			// insert
			SQLiteDatabase db = mSqlTemplate.getDb(true);

			return db.insert(MessageTable.getName(), null,
					MessageTable.toContentValues(message));
		} else {
			if (message.getId() > 0) {
				// use id
				mSqlTemplate.updateById(MessageTable.getName(),
						String.valueOf(message.getId()),
						MessageTable.toContentValues(message));
			} else {
				try {
					// use message id
					return mSqlTemplate.getDb(true).update(
							MessageTable.getName(),
							MessageTable.toContentValues(message),
							"messageid=?",
							new String[] { String.valueOf(message.messageid) });
				} catch (Exception e) {
					Utils.sendClientException(e, message);
				}
			}
			// update
			return -1;
		}
	}

	public List<Message> fetchNoMessageIdMessage() {
		List<Message> list = mSqlTemplate.queryForList(mRowMapper,
				MessageTable.getName(), null, " messageid=_id ", null, null,
				null, null, null);
		if (BuildConfig.DEBUG)
			Log.i(TAG, "fetchNoMessageIdMessage : " + list);
		return list;
	}
}
