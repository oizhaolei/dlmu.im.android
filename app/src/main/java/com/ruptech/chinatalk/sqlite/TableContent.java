package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.BuildConfig;

public abstract class TableContent {

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
			v.put(Columns.FULLNAME, user.getFullname());
			return v;
		}
	}

	public static class ChatTable {
		public static class Columns {
			public final String ID = "_id";
			public final String CREATED_DATE = "created_date";
			public final String FROM_JID = "from_jid";
			public final String TO_JID = "to_jid";
			public final String FROM_FULLNAME = "from_fullname";
			public final String TO_FULLNAME= "to_fullname";
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
			create.append(Columns.FROM_JID + " TEXT, ");
			create.append(Columns.TO_JID + " TEXT, ");
			create.append(Columns.FROM_FULLNAME + " TEXT, ");
			create.append(Columns.TO_FULLNAME + " TEXT, ");
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
			chat.setFromFullname(cursor.getString(cursor
					.getColumnIndex(Columns.FROM_FULLNAME)));// 消息来自
			chat.setToFullname(cursor.getString(cursor
					.getColumnIndex(Columns.TO_FULLNAME)));
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
			v.put(Columns.FROM_FULLNAME, chat.getFromFullname());
			v.put(Columns.TO_FULLNAME, chat.getToFullname());
			v.put(Columns.CONTENT, chat.getContent());
			v.put(Columns.DELIVERY_STATUS, chat.getRead());
			v.put(Columns.PACKET_ID, chat.getPid());
			return v;
		}

	}


	private static final String TAG = Utils.CATEGORY
			+ TableContent.class.getSimpleName();
	public static UserTable UserTable = new UserTable();
	public static ChatTable ChatTable = new ChatTable();
}