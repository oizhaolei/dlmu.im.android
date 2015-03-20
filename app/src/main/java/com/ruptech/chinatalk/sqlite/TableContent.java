package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.ChatRoom;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public abstract class TableContent {

	public static class FriendTable {
		public static class Columns {
			public final String ID = "_id";
			public final String USER_ID = "user_id";
			public final String FRIEND_ID = "friend_id";
			public final String DONE = "done";
			public final String WALLET_PRIORITY = "wallet_priority";
			public final String FRIEND_NICKNAME = "friend_nickname";
			public final String FRIEND_MEMO = "friend_memo";
			public final String FRIEND_METHOD = "friend_method";
			public final String CREATE_DATE = "create_date";
			public final String IS_TOP = "is_top";
		}

		public static final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE UNIQUE INDEX " + getName() + "_idx ON "
					+ getName() + " ( " + Columns.USER_ID + ","
					+ Columns.FRIEND_ID + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.USER_ID + " LONG, ");
			create.append(Columns.FRIEND_ID + " LONG, ");
			create.append(Columns.DONE + " INT, ");
			create.append(Columns.WALLET_PRIORITY + " INT, ");
			create.append(Columns.FRIEND_NICKNAME + " TEXT, ");
			create.append(Columns.FRIEND_MEMO + " TEXT, ");
			create.append(Columns.FRIEND_METHOD + " TEXT, ");
			create.append(Columns.CREATE_DATE + " TEXT, ");
			create.append(Columns.IS_TOP + " INT DEFAULT 0");
			create.append(");");
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());
			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + getName();
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}

		public String[] getIndexColumns() {
			return new String[]{Columns.ID, Columns.USER_ID,
					Columns.FRIEND_ID, Columns.DONE, Columns.WALLET_PRIORITY,
					Columns.CREATE_DATE};
		}

		public static String getName() {
			return "tbl_friend";
		}

		public static Friend parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			Friend friend = new Friend();
			friend.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			friend.user_id = cursor.getLong(cursor
					.getColumnIndex(Columns.USER_ID));
			friend.friend_id = cursor.getLong(cursor
					.getColumnIndex(Columns.FRIEND_ID));
			friend.done = cursor.getInt(cursor.getColumnIndex(Columns.DONE));
			friend.wallet_priority = cursor.getInt(cursor
					.getColumnIndex(Columns.WALLET_PRIORITY));
			friend.friend_nickname = cursor.getString(cursor
					.getColumnIndex(Columns.FRIEND_NICKNAME));
			friend.friend_memo = cursor.getString(cursor
					.getColumnIndex(Columns.FRIEND_MEMO));
			friend.friend_method = cursor.getString(cursor
					.getColumnIndex(Columns.FRIEND_METHOD));
			friend.create_date = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_DATE));
			friend.is_top = cursor
					.getInt(cursor.getColumnIndex(Columns.IS_TOP));
			return friend;
		}

		/**
		 * Friend -> ContentValues * * @param friend * @return
		 */
		public static ContentValues toContentValues(Friend friend) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, friend.getId());
			v.put(Columns.USER_ID, friend.user_id);
			v.put(Columns.FRIEND_ID, friend.friend_id);
			v.put(Columns.DONE, friend.done);
			v.put(Columns.WALLET_PRIORITY, friend.wallet_priority);
			v.put(Columns.FRIEND_NICKNAME, friend.getFriend_nickname());
			v.put(Columns.FRIEND_MEMO, friend.friend_memo);
			v.put(Columns.FRIEND_METHOD, friend.friend_method);
			v.put(Columns.CREATE_DATE, friend.create_date);
			return v;
		}
	}

	public static class UserTable {
		public static class Columns {
			public final String ID = "_id";
			public final String PASSWORD = "password";
			public final String USERNAME = "username";
			public final String FULLNAME = "fullname";
			public final String GENDER = "gender";
			public final String USER_MEMO = "user_memo";
			public final String PIC_URL = "pic_url";
			public final String CREATE_ID = "create_id";
			public final String CREATE_DATE = "create_date";
			public final String UPDATE_ID = "update_id";
			public final String UPDATE_DATE = "update_date";
			public final String TERMINAL_TYPE = "terminal_type";
		}

		public final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
					+ " ( " + Columns.USERNAME + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.PASSWORD + " TEXT, ");
			create.append(Columns.USERNAME + " TEXT, ");
			create.append(Columns.FULLNAME + " TEXT, ");
			create.append(Columns.GENDER + " INT, ");
			create.append(Columns.USER_MEMO + " TEXT, ");
			create.append(Columns.PIC_URL + " TEXT, ");
			create.append(Columns.TERMINAL_TYPE + " TEXT, ");
			create.append(Columns.CREATE_ID + " TEXT, ");
			create.append(Columns.CREATE_DATE + " date, ");
			create.append(Columns.UPDATE_ID + " TEXT, ");
			create.append(Columns.UPDATE_DATE + " date ");
			create.append(");");
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());
			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + getName();
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}

		public String[] getIndexColumns() {
			return new String[]{Columns.ID, Columns.PASSWORD, Columns.USERNAME,
					Columns.FULLNAME, Columns.GENDER, Columns.USER_MEMO, Columns.PIC_URL, Columns.TERMINAL_TYPE,
					Columns.CREATE_ID, Columns.CREATE_DATE, Columns.UPDATE_ID, Columns.UPDATE_DATE};
		}

		public String getName() {
			return "tbl_user";
		}

		public User parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			User user = new User();
			user.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			user.password = cursor.getString(cursor
					.getColumnIndex(Columns.PASSWORD));
			user.username = cursor.getString(cursor.getColumnIndex(Columns.USERNAME));
			return user;
		}

		/**
		 * User -> ContentValues * * @param user * @return
		 */
		public ContentValues toContentValues(User user) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, user.getId());
			v.put(Columns.PASSWORD, user.password);
			v.put(Columns.USERNAME, user.username);
			return v;
		}
	}

	public static class ChatTable {
		public static class Columns {
			public final String ID = "_id";
			public final String CREATED_DATE = "created_date";
			public final String FROM_JID = "from_jid";
			public final String TO_JID = "to_jid";
			public final String CONTENT = "content";
			public final String DELIVERY_STATUS = "read";
			public final String PACKET_ID = "pid";
		}

		public static final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
					+ " ( " + Columns.FROM_JID + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			create.append(Columns.CREATED_DATE + " INTEGER, ");
			create.append(Columns.FROM_JID + " INTEGER, ");
			create.append(Columns.TO_JID + " TEXT, ");
			create.append(Columns.CONTENT + " TEXT, ");
			create.append(Columns.DELIVERY_STATUS + " INTEGER, ");
			create.append(Columns.PACKET_ID + " TEXT ");
			create.append(");");
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());
			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + getName();
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}

		public String[] getIndexColumns() {
			return new String[]{Columns.ID, Columns.CREATED_DATE, Columns.FROM_JID, Columns.TO_JID,
					Columns.CONTENT,
					Columns.DELIVERY_STATUS, Columns.PACKET_ID};
		}

		public static String getName() {
			return "tbl_chat";
		}

		public static Chat parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}

			Chat chat = new Chat();
			chat.setCreated_date(cursor.getLong(cursor
					.getColumnIndex(Columns.CREATED_DATE)));
			chat.setId(cursor.getInt(cursor
					.getColumnIndex(Columns.ID)));
			chat.setContent(cursor.getString(cursor
					.getColumnIndex(Columns.CONTENT)));
			chat.setFromJid(cursor.getString(cursor
					.getColumnIndex(Columns.FROM_JID)));// 消息来自
			chat.setToJid(cursor.getString(cursor
					.getColumnIndex(Columns.TO_JID)));
			chat.setPid(cursor.getString(cursor
					.getColumnIndex(Columns.PACKET_ID)));
			chat.setRead(cursor.getInt(cursor
					.getColumnIndex(Columns.DELIVERY_STATUS)));

			return chat;

		}

		/**
		 * Message -> ContentValues * * @param message * @param isUnread * @return
		 */
		public ContentValues toContentValues(Chat chat) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, chat.getId());
			v.put(Columns.CREATED_DATE, chat.getCreated_date());
			v.put(Columns.FROM_JID, chat.getFromJid());
			v.put(Columns.TO_JID, chat.getToJid());
			v.put(Columns.CONTENT, chat.getContent());
			v.put(Columns.DELIVERY_STATUS, chat.getRead());
			v.put(Columns.PACKET_ID, chat.getPid());
			return v;
		}

	}

	public static class ChatRoomTable {
		public static class Columns {
			public final String ID = "_id";
			public final String JID = "jid";
			public final String ACCOUNT_USERID = "account_userid";
			public final String TITLE = "title";
			public final String CREATE_DATE = "create_date";
		}

		public static final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
					+ " ( " + Columns.ACCOUNT_USERID + " );";
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			create.append(Columns.JID + " TEXT UNIQUE ON CONFLICT REPLACE, ");
			create.append(Columns.ACCOUNT_USERID + " LONG, ");
			create.append(Columns.TITLE + " TEXT, ");
			create.append(Columns.CREATE_DATE + " TEXT ");
			create.append(");");
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());
			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + getName();
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}

		public String[] getIndexColumns() {
			return new String[]{Columns.ID, Columns.JID,
					Columns.ACCOUNT_USERID, Columns.TITLE, Columns.CREATE_DATE};
		}

		public static String getName() {
			return "tbl_chatroom";
		}

		public static ChatRoom parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			ChatRoom chatRoom = new ChatRoom();
			chatRoom.setId(cursor.getInt(cursor.getColumnIndex(Columns.ID)));
			chatRoom.jid = cursor.getString(cursor
					.getColumnIndex(Columns.JID));
			chatRoom.accountUserId = cursor.getLong(cursor
					.getColumnIndex(Columns.ACCOUNT_USERID));
			chatRoom.title = cursor.getString(cursor
					.getColumnIndex(Columns.TITLE));
			chatRoom.create_date = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_DATE));
			return chatRoom;
		}

		public static ContentValues toContentValues(ChatRoom chatRoom) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, chatRoom.getId());
			v.put(Columns.JID, chatRoom.jid);
			v.put(Columns.ACCOUNT_USERID, chatRoom.accountUserId);
			v.put(Columns.TITLE, chatRoom.title);
			v.put(Columns.CREATE_DATE, chatRoom.create_date);
			return v;
		}
	}

	public static class ChatRoomUserTable {
		public static class Columns {
			public final String ID = "_id";
			public final String CHATROOM_ID = "chatroom_id";
			public final String PARTICIPANT_ID = "participant_id";
		}

		public static final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
					+ " ( " + Columns.CHATROOM_ID + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
			create.append(Columns.CHATROOM_ID + " INTEGER, ");
			create.append(Columns.PARTICIPANT_ID + " LONG ");
			create.append(");");
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());
			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + getName();
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}

		public String[] getIndexColumns() {
			return new String[]{Columns.ID, Columns.CHATROOM_ID,
					Columns.PARTICIPANT_ID};
		}

		public static String getName() {
			return "tbl_chatroom_user";
		}
	}

	private static final String TAG = Utils.CATEGORY
			+ TableContent.class.getSimpleName();
	public static FriendTable FriendTable = new FriendTable();
	public static UserTable UserTable = new UserTable();
	public static ChatTable ChatTable = new ChatTable();
	public static ChatRoomTable ChatRoomTable = new ChatRoomTable();
	public static ChatRoomUserTable ChatRoomUserTable = new ChatRoomUserTable();
}