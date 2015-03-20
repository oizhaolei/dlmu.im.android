package com.ruptech.chinatalk.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

public class UserDAO {
	private static final String TAG = Utils.CATEGORY
			+ UserDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private final RowMapper<User> mRowMapper = new RowMapper<User>() {

		@Override
		public User mapRow(Cursor cursor, int rowNum) {
			User user = UserTable.parseCursor(cursor);
			return user;
		}

	};

	public UserDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase
				.getInstance(context).getSQLiteOpenHelper());

	}

	public void deleteAll() {
		SQLiteDatabase mDb = mSqlTemplate.getDb(true);
		String sql = "DELETE FROM " + UserTable.getName();

		if (BuildConfig.DEBUG)
			Log.v(TAG, sql);
		mDb.execSQL(sql);
	}

	private void deleteUser(SQLiteDatabase db, Long userId) {
		String sql = "DELETE FROM " + UserTable.getName() + " where "
				+ UserTable.Columns.ID + " = ?";

		db.execSQL(sql, new String[]{String.valueOf(userId)});
	}

	// 根据messageId进行排序
	public Cursor fetchChats(Long userId) {
		String[] args = new String[]{userId.toString(), userId.toString(),
				userId.toString(), userId.toString(), userId.toString(),
				userId.toString()};
		StringBuilder sqlB = new StringBuilder();

		sqlB.append("select messageid, ifnull(friend.is_top, 0) is_top, ");
		sqlB.append("(CASE WHEN friend.friend_nickname ISNULL or friend.friend_nickname='null' THEN user.fullname ELSE friend.friend_nickname END) friend_name, ");
		sqlB.append("user.* from ( ");
		// Union 1 -- start ： 获取存在聊天历史的好友列表
		sqlB.append("select u.*, m.messageid messageid from tbl_user u left join ( ");
		sqlB.append("select max(a.messageid) messageid, a.userid from ( ");
		sqlB.append("select _id messageid, userid from tbl_message where to_userid = ? ");
		sqlB.append("union  ");
		sqlB.append("select _id messageid, to_userid userid from tbl_message where userid = ? ");
		sqlB.append(") a ");
		sqlB.append("group by userid) m  ");
		sqlB.append("on u._id = m.userid  ");
		sqlB.append("where m.messageid is not null ");
		// Union 1 -- end
		sqlB.append("union  ");
		// Union 2 -- start ：获取关注列表当中没有聊天历史的好友列表
		sqlB.append("select u.*, 0 messageid from tbl_user u  ");
		sqlB.append("left join tbl_friend f on f.friend_id=u._id  ");
		sqlB.append("where f.user_id=? and f.done =1  ");
		sqlB.append("and u._id not in (select to_userid from tbl_message where userid=?)  ");
		sqlB.append("and u._id not in (select userid from tbl_message where to_userid=?) ");
		// Union 2 -- end
		sqlB.append(") user ");
		sqlB.append("left join tbl_friend friend on friend.friend_id=user._id and friend.user_id=?  ");
		sqlB.append("order by is_top desc, messageid desc, friend_name; ");

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				args);

		return cursor;
	}

	// 根据messageId进行排序
	public Cursor fetchChats(Long userId, String keyword) {

		String[] args;
		StringBuilder sqlB = new StringBuilder();

		sqlB.append("select messageid, ifnull(friend.is_top, 0) is_top, ");
		sqlB.append("(CASE WHEN friend.friend_nickname ISNULL or friend.friend_nickname='null' THEN user.fullname ELSE friend.friend_nickname END) friend_name, ");
		sqlB.append("user.* from ( ");
		// Union 1 -- start ： 获取存在聊天历史的好友列表
		sqlB.append("select u.*, m.messageid messageid from tbl_user u left join ( ");
		sqlB.append("select max(a.messageid) messageid, a.userid from ( ");
		sqlB.append("select _id messageid, userid from tbl_message where to_userid = ? ");
		sqlB.append("union  ");
		sqlB.append("select _id messageid, to_userid userid from tbl_message where userid = ? ");
		sqlB.append(") a ");
		sqlB.append("group by userid) m  ");
		sqlB.append("on u._id = m.userid  ");
		sqlB.append("where m.messageid is not null ");
		// Union 1 -- end
		sqlB.append("union  ");
		// Union 2 -- start ：获取关注列表当中没有聊天历史的好友列表
		sqlB.append("select u.*, 0 messageid from tbl_user u  ");
		sqlB.append("left join tbl_friend f on f.friend_id=u._id  ");
		sqlB.append("where f.user_id=? and f.done =1  ");
		sqlB.append("and u._id not in (select to_userid from tbl_message where userid=?)  ");
		sqlB.append("and u._id not in (select userid from tbl_message where to_userid=?) ");
		// Union 2 -- end
		sqlB.append(") user ");
		sqlB.append("left join tbl_friend friend on friend.friend_id=user._id and friend.user_id=?  ");

		if (keyword != null && keyword.length() > 0) {
			keyword = "%" + keyword + "%";
			args = new String[]{userId.toString(), userId.toString(),
					userId.toString(), userId.toString(), userId.toString(),
					userId.toString(), keyword, keyword};
			sqlB.append("where friend.friend_nickname like ? or user.fullname like ? ");
		} else {
			args = new String[]{userId.toString(), userId.toString(),
					userId.toString(), userId.toString(), userId.toString(),
					userId.toString()};
		}

		sqlB.append("order by is_top desc, messageid desc, friend_name; ");

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sqlB.toString(),
				args);

		return cursor;
	}

	public Cursor fetchFriends(Long userId) {
		String[] args = new String[]{userId.toString()};
		Cursor cursor = mSqlTemplate
				.getDb(false)
				.rawQuery(
						"select u._id, f.friend_nickname, f.friend_memo, f.friend_method, password, username, fullname, lang, user_memo, balance, "
								+ "terminal_type, create_id, u.create_date, update_id, update_date, active, gender, pic_url, point "
								+ "from tbl_user u, tbl_friend f "
								+ "where u._id=f.friend_id and f.done =1 and f.user_id=? "
								+ "order by f.friend_nickname", args);

		return cursor;
	}

	public Cursor fetchFriends(Long userId, String keyword) {
		keyword = "%" + keyword + "%";
		String[] args = new String[]{userId.toString(), keyword, keyword};
		Cursor cursor = mSqlTemplate
				.getDb(false)
				.rawQuery(
						"select u._id, f.friend_nickname, f.friend_memo, f.friend_method, password, username, fullname, lang, user_memo, balance, "
								+ "create_id, u.create_date, update_id, update_date, active, gender, pic_url, point "
								+ "from tbl_user u, tbl_friend f "
								+ "where u._id=f.friend_id and f.done =1 and f.user_id=? and (f.friend_nickname like ? or u.fullname like ?) "
								+ "order by f.friend_nickname", args);

		return cursor;
	}

	// 检索新的好友加入done = 2的条件 +时间排序
	public Cursor fetchRequestingFriends(Long userId) {
		String[] args = new String[]{userId.toString(), userId.toString(),
				userId.toString()};
		String sql = "select _id, password, username, fullname, lang, user_memo, balance, create_id, create_date, update_id, update_date, active, gender, pic_url, point   "
				+ " from (select a.* from (select u._id, f.create_date f_create_date, password, username, fullname, lang, user_memo, balance, create_id, u.create_date, update_id, update_date, active, gender, pic_url, point  "
				+ " from tbl_user u"
				+ " inner join tbl_friend f on u._id=f.user_id and f.friend_id=? and f.done=1"
				+ " where not exists (select * from tbl_friend f2 where u._id=f2.friend_id and f2.user_id=?) "
				+ " union "
				+ " select u2._id, f2.create_date f_create_date, password, username, fullname, lang, user_memo, balance, create_id, u2.create_date, update_id, update_date, active, gender, pic_url, point  "
				+ " from tbl_user u2"
				+ " inner join tbl_friend f2 on u2._id=f2.user_id and f2.friend_id=? and f2.done=2) a  order by f_create_date)";

		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sql, args);

		return cursor;
	}

	/**
	 * 添加新朋友个数
	 */
	public int fetchRequestingFriendsCount(Long userId, String clickDate) {
		String[] args = new String[]{userId.toString(), userId.toString(),
				clickDate};
		String sql = "select *"
				+ " from tbl_user u"
				+ " inner join tbl_friend f on u._id=f.user_id and f.friend_id=? and f.done=1"
				+ " where not exists (select * from tbl_friend f2 where u._id=f2.friend_id and f2.user_id=?) and f.create_date>?";
		Cursor cursor = mSqlTemplate.getDb(false).rawQuery(sql, args);
		int count = 0;
		try {

			count = cursor.getCount();

		} finally {
			cursor.close();
		}
		return count;

	}

	/**
	 * Find a user by user ID
	 *
	 * @param userId
	 * @return
	 */
	public User fetchUser(Long userId) {
		return mSqlTemplate.queryForObject(mRowMapper, UserTable.getName(),
				null, UserTable.Columns.ID + " = ?",
				new String[]{String.valueOf(userId)}, null, null,
				"_id DESC", "1");
	}

	/**
	 * Find a user by user username
	 *
	 * @param tel
	 * @return
	 */
	public User fetchUserByTel(String tel) {
		return mSqlTemplate.queryForObject(mRowMapper, UserTable.getName(),
				null, UserTable.Columns.USERNAME + " = ?", new String[]{tel},
				null, null, "_id DESC", "1");
	}

	/**
	 * 合并或者事先清空数据表再插入
	 *
	 * @param userList
	 * @param deleteAll
	 * @return
	 */
	public synchronized int insertUsers(List<User> userList, boolean deleteAll) {
		int result = 0;
		SQLiteDatabase db = mSqlTemplate.getDb(true);

		try {
			db.beginTransaction();
			if (deleteAll)
				deleteAll();
			if (userList != null)
				for (int i = userList.size() - 1; i >= 0; i--) {
					User user = userList.get(i);
					mergeUser(user);
				}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Insert a user
	 * <p/>
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 */
	public long mergeUser(User user) {
		SQLiteDatabase db = mSqlTemplate.getDb(true);
		deleteUser(db, user.getId());
		// insert

		return db.insert(UserTable.getName(), null,
				UserTable.toContentValues(user));
	}
}
