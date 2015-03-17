package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.ChatRoom;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.model.UserProp;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public abstract class TableContent {
	public static class ChannelTable {
		public static class Columns {
			public final String ID = "_id";
			public final String TITLE_TRANSLATE_ID = "title_translate_id";
			public final String PIC_URL = "pic_url";
			public final String LANG = "lang";
			public final String TITLE = "title";
			public final String FOLLOWER_COUNT = "follower_count";
			public final String POPULAR_COUNT = "popular_count";
			public final String IS_FOLLOWER = "is_follower";
			public final String POPULAR_ID = "popular_id";
			public final String RECOMMEND = "recommend";
		}

		public final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String createIndexSQL = "CREATE INDEX " + getName() + "_idx ON "
					+ getName() + " ( " + getIndexColumns()[1] + " );";
			return createIndexSQL;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.TITLE_TRANSLATE_ID + " LONG, ");
			create.append(Columns.PIC_URL + " TEXT, ");
			create.append(Columns.LANG + " TEXT, ");
			create.append(Columns.TITLE + " TEXT, ");
			create.append(Columns.FOLLOWER_COUNT + " INT, ");
			create.append(Columns.POPULAR_COUNT + " INT, ");
			create.append(Columns.IS_FOLLOWER + " INT, ");
			create.append(Columns.RECOMMEND + " INT, ");
			create.append(Columns.POPULAR_ID + " INT ");
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
			return new String[] { Columns.ID, Columns.TITLE_TRANSLATE_ID,
					Columns.PIC_URL, Columns.LANG, Columns.TITLE,
					Columns.FOLLOWER_COUNT, Columns.POPULAR_COUNT,
					Columns.IS_FOLLOWER, Columns.RECOMMEND, Columns.POPULAR_ID };
		}

		public String getName() {
			return "tbl_channel";
		}

		public Channel parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			Channel channel = new Channel();
			channel.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			channel.title_translate_id = cursor.getLong(cursor
					.getColumnIndex(Columns.TITLE_TRANSLATE_ID));
			channel.pic_url = cursor.getString(cursor
					.getColumnIndex(Columns.PIC_URL));
			channel.lang = cursor
					.getString(cursor.getColumnIndex(Columns.LANG));
			channel.title = cursor.getString(cursor
					.getColumnIndex(Columns.TITLE));
			channel.follower_count = cursor.getInt(cursor
					.getColumnIndex(Columns.FOLLOWER_COUNT));
			channel.popular_count = cursor.getInt(cursor
					.getColumnIndex(Columns.POPULAR_COUNT));
			channel.is_follower = cursor.getInt(cursor
					.getColumnIndex(Columns.IS_FOLLOWER));
			channel.popular_id = cursor.getLong(cursor
					.getColumnIndex(Columns.POPULAR_ID));
			channel.recommend = cursor.getInt(cursor
					.getColumnIndex(Columns.RECOMMEND));
			return channel;
		}

		/** * Channel -> ContentValues * * @param channel * @param isUnread * @return */
		public ContentValues toContentValues(Channel channel) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, channel.getId());
			v.put(Columns.TITLE_TRANSLATE_ID, channel.title_translate_id);
			v.put(Columns.PIC_URL, channel.pic_url);
			v.put(Columns.LANG, channel.lang);
			v.put(Columns.TITLE, channel.title);
			v.put(Columns.FOLLOWER_COUNT, channel.follower_count);
			v.put(Columns.POPULAR_COUNT, channel.popular_count);
			v.put(Columns.POPULAR_ID, channel.getPopular_id());
			v.put(Columns.RECOMMEND, channel.getRecommend());
			return v;
		}
	}

	public static class CommentNewsTable {
		public static class Columns {
			public static final String ID = "_id";
			public static final String USER_ID = "user_id";
			public static final String FROM_USER_ID = "from_user_id";
			public static final String RELATION_ID = "relation_id";
			public static final String NEWS_ID = "news_id";
			public static final String NEWS_TITLE = "news_title";
			public static final String PIC_URL = "pic_url";
			public static final String USER_PIC_URL = "user_pic_url";
			public static final String CONTENT = "content";
			public static final String TYPE = "type";
			public static final String USER_FULLNAME = "user_fullname";
			public static final String CREATE_DATE = "create_date";
		}

		public static final String TABLE_NAME = "tbl_news_timeline";

		public static String[] getIndexColumns() {
			return new String[] { Columns.ID, Columns.USER_ID,
					Columns.FROM_USER_ID, Columns.RELATION_ID, Columns.NEWS_ID,
					Columns.NEWS_TITLE, Columns.PIC_URL, Columns.USER_PIC_URL,
					Columns.CONTENT, Columns.TYPE, Columns.USER_FULLNAME,
					Columns.CREATE_DATE };
		}

		public static CommentNews parseCursor(Cursor cursor) {

			if (null == cursor || 0 == cursor.getCount()) {
				if (BuildConfig.DEBUG)
					Log.w(TAG,
							"Cann't parse Cursor, bacause cursor is null or empty.");
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}

			CommentNews commentNews = new CommentNews();
			commentNews
					.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			commentNews.user_id = cursor.getLong(cursor
					.getColumnIndex(Columns.USER_ID));
			commentNews.from_user_id = cursor.getLong(cursor
					.getColumnIndex(Columns.FROM_USER_ID));
			commentNews.relation_id = cursor.getLong(cursor
					.getColumnIndex(Columns.RELATION_ID));
			commentNews.news_id = cursor.getLong(cursor
					.getColumnIndex(Columns.NEWS_ID));
			commentNews.news_title = cursor.getString(cursor
					.getColumnIndex(Columns.NEWS_TITLE));
			commentNews.pic_url = cursor.getString(cursor
					.getColumnIndex(Columns.PIC_URL));
			commentNews.user_pic_url = cursor.getString(cursor
					.getColumnIndex(Columns.USER_PIC_URL));
			commentNews.content = cursor.getString(cursor
					.getColumnIndex(Columns.CONTENT));
			commentNews.type = cursor.getString(cursor
					.getColumnIndex(Columns.TYPE));
			commentNews.user_fullname = cursor.getString(cursor
					.getColumnIndex(Columns.USER_FULLNAME));
			commentNews.create_date = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_DATE));

			return commentNews;
		}

		/**
		 * commentNews -> ContentValues
		 *
		 * @param commentNews
		 * @return
		 */
		public static ContentValues toContentValues(CommentNews commentNews) {
			final ContentValues v = new ContentValues();

			v.put(Columns.ID, commentNews.getId());
			v.put(Columns.USER_ID, commentNews.getUser_id());
			v.put(Columns.FROM_USER_ID, commentNews.getFrom_user_id());
			v.put(Columns.RELATION_ID, commentNews.getRelation_id());
			v.put(Columns.NEWS_ID, commentNews.getNews_id());
			v.put(Columns.NEWS_TITLE, commentNews.getNews_title());
			v.put(Columns.PIC_URL, commentNews.getPic_url());
			v.put(Columns.USER_PIC_URL, commentNews.getUser_pic_url());
			v.put(Columns.CONTENT, commentNews.getContent());
			v.put(Columns.TYPE, commentNews.getType());
			v.put(Columns.USER_FULLNAME, commentNews.getUser_fullname());
			v.put(Columns.CREATE_DATE, commentNews.getCreate_date());

			return v;
		}

		public String getCreateIndexSQL() {
			String createIndexSQL = "CREATE INDEX " + TABLE_NAME + "_idx ON "
					+ TABLE_NAME + " ( " + getIndexColumns()[1] + " );";
			return createIndexSQL;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(TABLE_NAME).append("( ");
			create.append(Columns.ID + "                 LONG PRIMARY KEY, ");
			create.append(Columns.USER_ID + " LONG, ");
			create.append(Columns.FROM_USER_ID + " LONG, ");
			create.append(Columns.RELATION_ID + " LONG, ");
			create.append(Columns.NEWS_ID + "            LONG, ");
			create.append(Columns.NEWS_TITLE + "               TEXT, ");
			create.append(Columns.PIC_URL + "              TEXT, ");
			create.append(Columns.USER_PIC_URL + "              TEXT, ");
			create.append(Columns.CONTENT + "     TEXT, ");
			create.append(Columns.TYPE + "     TEXT, ");
			create.append(Columns.USER_FULLNAME + "     TEXT, ");
			create.append(Columns.CREATE_DATE + "     TEXT ");
			create.append(");");

			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + create.toString());

			return create.toString();
		}

		public String getDropSQL() {
			String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql);
			return sql;
		}
	}

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
			return new String[] { Columns.ID, Columns.USER_ID,
					Columns.FRIEND_ID, Columns.DONE, Columns.WALLET_PRIORITY,
					Columns.CREATE_DATE };
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

		/** * Friend -> ContentValues * * @param friend * @return */
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

	public static class HotUserPhotoTable extends UserPhotoTable {
		@Override
		public String getName() {
			return "tbl_hot_user_photo";
		}
	}

	public static class MessageTable {
		public static class Columns {
			public final String ID = "_id";
			public final String MESSAGEID = "messageid";
			public final String USERID = "userid";
			public final String VIA_TEL = "via_tel";
			public final String TO_USERID = "to_userid";
			public final String TRANSLATOR_ID = "translator_id";
			public final String FROM_LANG = "from_lang";
			public final String TO_LANG = "to_lang";
			public final String FROM_VOICE_ID = "from_voice_id";
			public final String FROM_CONTENT = "from_content";
			public final String FROM_CONTENT_LENGTH = "from_content_length";
			public final String TO_CONTENT = "to_content";
			public final String STATUS_TEXT = "status_text";
			public final String FEE = "fee";
			public final String TRANSLATE_FEE = "translate_fee";
			public final String AUTO_TRANSLATE = "auto_translate";
			public final String TO_USER_FEE = "to_user_fee";
			public final String ACQUIRE_DATE = "acquire_date";
			public final String TRANSLATED_DATE = "translated_date";
			public final String VERIFY_STATUS = "verify_status";
			public final String MESSAGE_STATUS = "message_status";
			public final String CREATE_ID = "create_id";
			public final String CREATE_DATE = "create_date";
			public final String UPDATE_ID = "update_id";
			public final String UPDATE_DATE = "update_date";
			public final String FILE_PATH = "file_path";
			public final String FILE_TYPE = "file_type";
			public final String DETECT_LANGUAGE = "detect_language";
		}

		public static final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
					+ " ( " + Columns.TO_USERID + " );";
			sql = sql + "CREATE INDEX " + getName() + "_userid ON " + getName()
					+ " ( " + Columns.USERID + " );";
			sql = sql + "CREATE INDEX " + getName() + "_to_userid ON "
					+ getName() + " ( " + Columns.TO_USERID + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.MESSAGEID + " LONG, ");
			create.append(Columns.USERID + " LONG, ");
			create.append(Columns.VIA_TEL + " TEXT, ");
			create.append(Columns.TO_USERID + " LONG, ");
			create.append(Columns.TRANSLATOR_ID + " LONG, ");
			create.append(Columns.FROM_LANG + " TEXT, ");
			create.append(Columns.TO_LANG + " TEXT, ");
			create.append(Columns.FROM_VOICE_ID + " LONG, ");
			create.append(Columns.FROM_CONTENT + " TEXT, ");
			create.append(Columns.FROM_CONTENT_LENGTH + " TEXT, ");
			create.append(Columns.TO_CONTENT + " TEXT, ");
			create.append(Columns.STATUS_TEXT + " TEXT, ");
			create.append(Columns.FEE + " DOUBLE, ");
			create.append(Columns.TRANSLATE_FEE + " DOUBLE, ");
			create.append(Columns.AUTO_TRANSLATE + " INT, ");
			create.append(Columns.TO_USER_FEE + " DOUBLE, ");
			create.append(Columns.ACQUIRE_DATE + " TEXT, ");
			create.append(Columns.TRANSLATED_DATE + " TEXT, ");
			create.append(Columns.VERIFY_STATUS + " INT, ");
			create.append(Columns.MESSAGE_STATUS + " INT, ");
			create.append(Columns.CREATE_ID + " TEXT, ");
			create.append(Columns.CREATE_DATE + " TEXT, ");
			create.append(Columns.UPDATE_ID + " TEXT, ");
			create.append(Columns.UPDATE_DATE + " TEXT, ");
			create.append(Columns.FILE_PATH + " TEXT, ");
			create.append(Columns.FILE_TYPE + " TEXT, ");
			create.append(Columns.DETECT_LANGUAGE + " INT ");
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
			return new String[] { Columns.ID, Columns.USERID, Columns.VIA_TEL,
					Columns.TO_USERID, Columns.TRANSLATOR_ID,
					Columns.FROM_LANG, Columns.TO_LANG, Columns.FROM_VOICE_ID,
					Columns.FROM_CONTENT, Columns.FROM_CONTENT_LENGTH,
					Columns.TO_CONTENT, Columns.STATUS_TEXT, Columns.FEE,
					Columns.TRANSLATE_FEE, Columns.AUTO_TRANSLATE,
					Columns.TO_USER_FEE, Columns.ACQUIRE_DATE,
					Columns.TRANSLATED_DATE, Columns.VERIFY_STATUS,
					Columns.MESSAGE_STATUS, Columns.CREATE_ID,
					Columns.CREATE_DATE, Columns.UPDATE_ID,
					Columns.UPDATE_DATE, Columns.FILE_PATH, Columns.FILE_TYPE,
					Columns.DETECT_LANGUAGE };
		}

		public static String getName() {
			return "tbl_message";
		}

		public Message parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			Message message = new Message();
			message.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			message.messageid = cursor.getLong(cursor
					.getColumnIndex(Columns.MESSAGEID));
			message.userid = cursor.getLong(cursor
					.getColumnIndex(Columns.USERID));
			message.to_userid = cursor.getLong(cursor
					.getColumnIndex(Columns.TO_USERID));
			message.from_lang = cursor.getString(cursor
					.getColumnIndex(Columns.FROM_LANG));
			message.to_lang = cursor.getString(cursor
					.getColumnIndex(Columns.TO_LANG));
			message.from_voice_id = cursor.getLong(cursor
					.getColumnIndex(Columns.FROM_VOICE_ID));
			message.from_content = cursor.getString(cursor
					.getColumnIndex(Columns.FROM_CONTENT));
			message.from_content_length = cursor.getInt(cursor
					.getColumnIndex(Columns.FROM_CONTENT_LENGTH));
			message.to_content = cursor.getString(cursor
					.getColumnIndex(Columns.TO_CONTENT));
			message.status_text = cursor.getString(cursor
					.getColumnIndex(Columns.STATUS_TEXT));
			message.fee = cursor.getDouble(cursor.getColumnIndex(Columns.FEE));
			message.to_user_fee = cursor.getDouble(cursor
					.getColumnIndex(Columns.TO_USER_FEE));
			message.acquire_date = cursor.getString(cursor
					.getColumnIndex(Columns.ACQUIRE_DATE));
			message.translated_date = cursor.getString(cursor
					.getColumnIndex(Columns.TRANSLATED_DATE));
			message.verify_status = cursor.getInt(cursor
					.getColumnIndex(Columns.VERIFY_STATUS));
			message.message_status = cursor.getInt(cursor
					.getColumnIndex(Columns.MESSAGE_STATUS));
			message.create_id = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_ID));
			message.create_date = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_DATE));
			message.update_id = cursor.getString(cursor
					.getColumnIndex(Columns.UPDATE_ID));
			message.update_date = cursor.getString(cursor
					.getColumnIndex(Columns.UPDATE_DATE));
			message.file_path = cursor.getString(cursor
					.getColumnIndex(Columns.FILE_PATH));
			message.file_type = cursor.getString(cursor
					.getColumnIndex(Columns.FILE_TYPE));
			message.auto_translate = cursor.getInt(cursor
					.getColumnIndex(Columns.AUTO_TRANSLATE));
			return message;
		}

		/** * Message -> ContentValues * * @param message * @param isUnread * @return */
		public ContentValues toContentValues(Message message) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, message.getId());
			v.put(Columns.MESSAGEID, message.messageid);
			v.put(Columns.USERID, message.userid);
			v.put(Columns.TO_USERID, message.to_userid);
			v.put(Columns.FROM_LANG, message.from_lang);
			v.put(Columns.TO_LANG, message.to_lang);
			v.put(Columns.FROM_VOICE_ID, message.from_voice_id);
			v.put(Columns.FROM_CONTENT, message.from_content);
			v.put(Columns.FROM_CONTENT_LENGTH, message.from_content_length);
			v.put(Columns.TO_CONTENT, message.getTo_content());
			v.put(Columns.STATUS_TEXT, message.getStatus_text());
			v.put(Columns.FEE, message.fee);
			v.put(Columns.TO_USER_FEE, message.to_user_fee);
			v.put(Columns.ACQUIRE_DATE, message.acquire_date);
			v.put(Columns.TRANSLATED_DATE, message.translated_date);
			v.put(Columns.VERIFY_STATUS, message.verify_status);
			v.put(Columns.MESSAGE_STATUS, message.message_status);
			v.put(Columns.CREATE_ID, message.create_id);
			v.put(Columns.CREATE_DATE, message.create_date);
			v.put(Columns.UPDATE_ID, message.update_id);
			v.put(Columns.UPDATE_DATE, message.update_date);
			v.put(Columns.FILE_PATH, message.file_path);
			v.put(Columns.FILE_TYPE, message.file_type);
			v.put(Columns.AUTO_TRANSLATE, message.auto_translate);
			return v;
		}
	}

	public static class UserPhotoTable {
		public static class Columns {
			public final String ID = "_id";
			public final String PARENT_ID = "parent_id";
			public final String TIMELINE_ID = "timeline_id";
			public final String TIMELINE_USERID = "timeline_userid";
			public final String TIMELINE_FULLNAME = "timeline_fullname";
			public final String TIMELINE_CREATE_DATE = "timeline_create_date";
			public final String USERID = "userid";
			public final String STATUS = "status";
			public final String PIC_URL = "pic_url";
			public final String TRANSLATOR_ID = "translator_id";
			public final String TRANSLATOR_FULLNAME = "translator_fullname";
			public final String CONTENT = "content";
			public final String LANG = "lang";
			public final String TO_CONTENT = "to_content";
			public final String TO_LANG = "to_lang";
			public final String ADDRESS = "address";
			public final String LATE6 = "late6";
			public final String LNGE6 = "lnge6";
			public final String WIDTH = "width";
			public final String HEIGHT = "height";
			public final String CATEGORY = "category";
			public final String GOOD = "good";
			public final String COMMENT = "comment";
			public final String FAVORITE = "favorite";
			public final String FULLNAME = "fullname";
			public final String USER_PIC = "user_pic";
			public final String PRESENT_COUNT = "present_count";
			public final String CHANNEL_ID = "channel_id";
			public final String CHANNEL_TITLE = "channel_title";
			public final String CHANNEL_PIC = "channel_pic";
			public final String CREATE_DATE = "create_date";
		}

		public Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String createIndexSQL = "CREATE INDEX " + getName() + "_idx ON "
					+ getName() + " ( " + getIndexColumns()[1] + " );";
			return createIndexSQL;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.PARENT_ID + " LONG, ");
			create.append(Columns.TIMELINE_ID + " LONG, ");
			create.append(Columns.TIMELINE_USERID + " LONG, ");
			create.append(Columns.TIMELINE_FULLNAME + " TEXT, ");
			create.append(Columns.TIMELINE_CREATE_DATE + " TEXT, ");
			create.append(Columns.USERID + " LONG, ");
			create.append(Columns.STATUS + " TEXT, ");
			create.append(Columns.PIC_URL + " TEXT, ");
			create.append(Columns.TRANSLATOR_ID + " LONG, ");
			create.append(Columns.TRANSLATOR_FULLNAME + " TEXT, ");
			create.append(Columns.CONTENT + " TEXT, ");
			create.append(Columns.LANG + " TEXT, ");
			create.append(Columns.TO_CONTENT + " TEXT, ");
			create.append(Columns.TO_LANG + " TEXT, ");
			create.append(Columns.ADDRESS + " TEXT, ");
			create.append(Columns.LATE6 + " INT, ");
			create.append(Columns.LNGE6 + " INT, ");
			create.append(Columns.WIDTH + " INT, ");
			create.append(Columns.HEIGHT + " INT, ");
			create.append(Columns.CATEGORY + " TEXT, ");
			create.append(Columns.GOOD + " INT, ");
			create.append(Columns.COMMENT + " INT, ");
			create.append(Columns.FAVORITE + " INT, ");
			create.append(Columns.FULLNAME + " TEXT, ");
			create.append(Columns.USER_PIC + " TEXT, ");
			create.append(Columns.CHANNEL_ID + " LONG, ");
			create.append(Columns.CHANNEL_PIC + " TEXT, ");
			create.append(Columns.CHANNEL_TITLE + " TEXT, ");
			create.append(Columns.PRESENT_COUNT + " INT, ");
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
			return new String[] { Columns.ID, Columns.PARENT_ID,
					Columns.TIMELINE_ID, Columns.TIMELINE_USERID,
					Columns.TIMELINE_FULLNAME, Columns.TIMELINE_CREATE_DATE,
					Columns.USERID, Columns.STATUS, Columns.PIC_URL,
					Columns.TRANSLATOR_ID, Columns.TRANSLATOR_FULLNAME,
					Columns.CONTENT, Columns.LANG, Columns.TO_CONTENT,
					Columns.TO_LANG, Columns.ADDRESS, Columns.LATE6,
					Columns.LNGE6, Columns.WIDTH, Columns.HEIGHT,
					Columns.CATEGORY, Columns.GOOD, Columns.COMMENT,
					Columns.FAVORITE, Columns.FULLNAME, Columns.USER_PIC,
					Columns.CHANNEL_ID, Columns.CHANNEL_TITLE,
					Columns.CHANNEL_PIC, Columns.PRESENT_COUNT,
					Columns.CREATE_DATE };
		}

		public String getName() {
			return "tbl_user_photo";
		}

		public UserPhoto parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			UserPhoto userPhoto = new UserPhoto();
			userPhoto.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			userPhoto.parent_id = cursor.getLong(cursor
					.getColumnIndex(Columns.PARENT_ID));
			userPhoto.timeline_id = cursor.getLong(cursor
					.getColumnIndex(Columns.TIMELINE_ID));
			userPhoto.timeline_userid = cursor.getLong(cursor
					.getColumnIndex(Columns.TIMELINE_USERID));
			userPhoto.timeline_fullname = cursor.getString(cursor
					.getColumnIndex(Columns.TIMELINE_FULLNAME));
			userPhoto.timeline_create_date = cursor.getString(cursor
					.getColumnIndex(Columns.TIMELINE_CREATE_DATE));
			userPhoto.userid = cursor.getLong(cursor
					.getColumnIndex(Columns.USERID));
			userPhoto.status = cursor.getString(cursor
					.getColumnIndex(Columns.STATUS));
			userPhoto.pic_url = cursor.getString(cursor
					.getColumnIndex(Columns.PIC_URL));
			userPhoto.translator_id = cursor.getLong(cursor
					.getColumnIndex(Columns.TRANSLATOR_ID));
			userPhoto.translator_fullname = cursor.getString(cursor
					.getColumnIndex(Columns.TRANSLATOR_FULLNAME));
			userPhoto.content = cursor.getString(cursor
					.getColumnIndex(Columns.CONTENT));
			userPhoto.lang = cursor.getString(cursor
					.getColumnIndex(Columns.LANG));
			userPhoto.to_content = cursor.getString(cursor
					.getColumnIndex(Columns.TO_CONTENT));
			userPhoto.to_lang = cursor.getString(cursor
					.getColumnIndex(Columns.TO_LANG));
			userPhoto.address = cursor.getString(cursor
					.getColumnIndex(Columns.ADDRESS));
			userPhoto.late6 = cursor.getInt(cursor
					.getColumnIndex(Columns.LATE6));
			userPhoto.lnge6 = cursor.getInt(cursor
					.getColumnIndex(Columns.LNGE6));
			userPhoto.width = cursor.getInt(cursor
					.getColumnIndex(Columns.WIDTH));
			userPhoto.height = cursor.getInt(cursor
					.getColumnIndex(Columns.HEIGHT));
			userPhoto.category = cursor.getString(cursor
					.getColumnIndex(Columns.CATEGORY));
			userPhoto.good = cursor.getInt(cursor.getColumnIndex(Columns.GOOD));
			userPhoto.comment = cursor.getInt(cursor
					.getColumnIndex(Columns.COMMENT));
			userPhoto.favorite = cursor.getInt(cursor
					.getColumnIndex(Columns.FAVORITE));
			userPhoto.fullname = cursor.getString(cursor
					.getColumnIndex(Columns.FULLNAME));
			userPhoto.user_pic = cursor.getString(cursor
					.getColumnIndex(Columns.USER_PIC));
			userPhoto.present_count = cursor.getInt(cursor
					.getColumnIndex(Columns.PRESENT_COUNT));
			userPhoto.channel_id = cursor.getInt(cursor
					.getColumnIndex(Columns.CHANNEL_ID));
			userPhoto.channel_title = cursor.getString(cursor
					.getColumnIndex(Columns.CHANNEL_TITLE));
			userPhoto.channel_pic = cursor.getString(cursor
					.getColumnIndex(Columns.CHANNEL_PIC));
			userPhoto.create_date = DateCommonUtils
					.parseToDateFromString(cursor.getString(cursor
							.getColumnIndex(Columns.CREATE_DATE)));
			return userPhoto;
		}

		/** * UserPhoto -> ContentValues * * @param userPhoto * @param isUnread * @return */
		public ContentValues toContentValues(UserPhoto userPhoto,
				boolean isInsert) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, userPhoto.getId());
			v.put(Columns.PARENT_ID, userPhoto.parent_id);
			if (isInsert) {
				v.put(Columns.TIMELINE_ID, userPhoto.timeline_id);
				v.put(Columns.TIMELINE_USERID, userPhoto.timeline_userid);
				v.put(Columns.TIMELINE_FULLNAME, userPhoto.timeline_fullname);
				v.put(Columns.TIMELINE_CREATE_DATE,
						userPhoto.timeline_create_date);
				v.put(Columns.STATUS, userPhoto.status);
			}
			v.put(Columns.USERID, userPhoto.userid);
			v.put(Columns.PIC_URL, userPhoto.pic_url);
			v.put(Columns.TRANSLATOR_ID, userPhoto.translator_id);
			v.put(Columns.TRANSLATOR_FULLNAME, userPhoto.translator_fullname);
			v.put(Columns.CONTENT, userPhoto.content);
			v.put(Columns.LANG, userPhoto.lang);
			v.put(Columns.TO_CONTENT, userPhoto.to_content);
			v.put(Columns.TO_LANG, userPhoto.to_lang);
			v.put(Columns.ADDRESS, userPhoto.address);
			v.put(Columns.LATE6, userPhoto.late6);
			v.put(Columns.LNGE6, userPhoto.lnge6);
			v.put(Columns.WIDTH, userPhoto.width);
			v.put(Columns.HEIGHT, userPhoto.height);
			v.put(Columns.CATEGORY, userPhoto.category);
			v.put(Columns.GOOD, userPhoto.good);
			v.put(Columns.PRESENT_COUNT, userPhoto.present_count);
			v.put(Columns.COMMENT, userPhoto.comment);
			v.put(Columns.FAVORITE, userPhoto.favorite);
			v.put(Columns.FULLNAME, userPhoto.fullname);
			v.put(Columns.USER_PIC, userPhoto.user_pic);
			v.put(Columns.CHANNEL_ID, userPhoto.channel_id);
			v.put(Columns.CHANNEL_TITLE, userPhoto.channel_title);
			v.put(Columns.CHANNEL_PIC, userPhoto.channel_pic);
			v.put(Columns.CREATE_DATE, DateCommonUtils
					.dateFormat(userPhoto.create_date,
							DateCommonUtils.DF_yyyyMMddHHmmssSSS));
			return v;
		}
	}

	public static class UserPropTable {
		public static class Columns {
			public final String ID = "_id";
			public final String USERID = "userid";
			public final String PROP_KEY = "name";
			public final String PROP_VALUE = "content";
			public final String CREATE_DATE = "create_date";
		}

		public final Columns Columns = new Columns();

		public String getCreateIndexSQL() {
			String createIndexSQL = "CREATE INDEX " + getName() + "_idx ON "
					+ getName() + " ( " + getIndexColumns()[1] + " );";
			return createIndexSQL;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.USERID + " LONG, ");
			create.append(Columns.PROP_KEY + " TEXT, ");
			create.append(Columns.PROP_VALUE + " TEXT, ");
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
			return new String[] { Columns.ID, Columns.USERID, Columns.PROP_KEY,
					Columns.PROP_VALUE, Columns.CREATE_DATE };
		}

		public String getName() {
			return "tbl_user_prop";
		}

		public UserProp parseCursor(Cursor cursor) {
			if (null == cursor || 0 == cursor.getCount()) {
				return null;
			} else if (-1 == cursor.getPosition()) {
				cursor.moveToFirst();
			}
			UserProp userProp = new UserProp();
			userProp.setId(cursor.getLong(cursor.getColumnIndex(Columns.ID)));
			userProp.userid = cursor.getLong(cursor
					.getColumnIndex(Columns.USERID));
			userProp.propKey = cursor.getString(cursor
					.getColumnIndex(Columns.PROP_KEY));
			userProp.propValue = cursor.getString(cursor
					.getColumnIndex(Columns.PROP_VALUE));
			userProp.create_date = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_DATE));
			return userProp;
		}

		/** * UserProp -> ContentValues * * @param userProp * @param isUnread * @return */
		public ContentValues toContentValues(UserProp userProp) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, userProp.getId());
			v.put(Columns.USERID, userProp.userid);
			v.put(Columns.PROP_KEY, userProp.propKey);
			v.put(Columns.PROP_VALUE, userProp.propValue);
			v.put(Columns.CREATE_DATE, userProp.create_date);
			return v;
		}
	}

	public static class UserTable {
		public static class Columns {
			public final String ID = "_id";
			public final String PASSWORD = "password";
			public final String TEL = "tel";
			public final String FULLNAME = "fullname";
			public final String LANG = "lang";
			public final String ACTIVE = "active";
			public final String GENDER = "gender";
			public final String USER_MEMO = "user_memo";
			public final String BALANCE = "balance";
			public final String POINT = "point";
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
					+ " ( " + Columns.TEL + " );";
			if (BuildConfig.DEBUG)
				Log.w(TAG, "sql:" + sql.toString());
			return sql;
		}

		public String getCreateSQL() {
			StringBuffer create = new StringBuffer(512);
			create.append("CREATE TABLE ").append(getName()).append("( ");
			create.append(Columns.ID + " LONG PRIMARY KEY, ");
			create.append(Columns.PASSWORD + " TEXT, ");
			create.append(Columns.TEL + " TEXT, ");
			create.append(Columns.FULLNAME + " TEXT, ");
			create.append(Columns.LANG + " TEXT, ");
			create.append(Columns.ACTIVE + " INT, ");
			create.append(Columns.GENDER + " INT, ");
			create.append(Columns.USER_MEMO + " TEXT, ");
			create.append(Columns.BALANCE + " DOUBLE, ");
			create.append(Columns.POINT + " INT, ");
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
			return new String[] { Columns.ID, Columns.PASSWORD, Columns.TEL,
					Columns.FULLNAME, Columns.LANG, Columns.ACTIVE,
					Columns.GENDER, Columns.USER_MEMO, Columns.BALANCE,
					Columns.POINT, Columns.PIC_URL, Columns.TERMINAL_TYPE,
                    Columns.CREATE_ID,
					Columns.CREATE_DATE, Columns.UPDATE_ID, Columns.UPDATE_DATE };
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
			user.tel = cursor.getString(cursor.getColumnIndex(Columns.TEL));
			user.fullname = cursor.getString(cursor
					.getColumnIndex(Columns.FULLNAME));
			user.lang = cursor.getString(cursor.getColumnIndex(Columns.LANG));
			user.active = cursor.getInt(cursor.getColumnIndex(Columns.ACTIVE));
			user.gender = cursor.getInt(cursor.getColumnIndex(Columns.GENDER));
			user.user_memo = cursor.getString(cursor
					.getColumnIndex(Columns.USER_MEMO));
			user.balance = cursor.getLong(cursor
					.getColumnIndex(Columns.BALANCE));
			user.point = cursor.getInt(cursor.getColumnIndex(Columns.POINT));
			user.pic_url = cursor.getString(cursor
					.getColumnIndex(Columns.PIC_URL));
            user.terminal_type = cursor.getString(cursor
		            .getColumnIndex(Columns.TERMINAL_TYPE));
			user.create_id = cursor.getString(cursor
					.getColumnIndex(Columns.CREATE_ID));
			user.create_date = DateCommonUtils.parseToDateFromString(cursor
					.getString(cursor.getColumnIndex(Columns.CREATE_DATE)));
			user.update_id = cursor.getString(cursor
					.getColumnIndex(Columns.UPDATE_ID));
			user.update_date = DateCommonUtils.parseToDateFromString(cursor
					.getString(cursor.getColumnIndex(Columns.UPDATE_DATE)));
			return user;
		}

		/** * User -> ContentValues * * @param user * @return */
		public ContentValues toContentValues(User user) {
			final ContentValues v = new ContentValues();
			v.put(Columns.ID, user.getId());
			v.put(Columns.PASSWORD, user.password);
			v.put(Columns.TEL, user.tel);
			v.put(Columns.FULLNAME, user.fullname);
			v.put(Columns.LANG, user.lang);
			v.put(Columns.ACTIVE, user.active);
			v.put(Columns.GENDER, user.gender);
			v.put(Columns.USER_MEMO, user.user_memo);
			v.put(Columns.BALANCE, user.balance);
			v.put(Columns.POINT, user.point);
			v.put(Columns.PIC_URL, user.pic_url);
            v.put(Columns.TERMINAL_TYPE, user.terminal_type);
			v.put(Columns.CREATE_ID, user.create_id);
			v.put(Columns.CREATE_DATE, DateCommonUtils.dateFormat(
					user.create_date, DateCommonUtils.DF_yyyyMMddHHmmssSSS));
			v.put(Columns.UPDATE_ID, user.update_id);
			v.put(Columns.UPDATE_DATE, DateCommonUtils.dateFormat(
					user.update_date, DateCommonUtils.DF_yyyyMMddHHmmssSSS));
			return v;
		}
	}

    public static class ChatTable {
        public static class Columns {
            public final String ID = "_id";
            public final String CREATED_DATE = "created_date";
            public final String FROM_JID = "from_user";
            public final String TO_JID = "to_user";
            public final String CONTENT = "content";
            public final String CONTENT_TYPE = "content_type";
            public final String FILE_PATH = "file_path";
            public final String VOICE_SECOND = "voice_second";
	        @Deprecated
	        public final String TO_MESSAGE = "to_content";
            public final String MESSAGE_ID = "message_id";
            public final String DELIVERY_STATUS = "read";
            public final String PACKET_ID = "pid";
        }

        public static final Columns Columns = new Columns();

        public String getCreateIndexSQL() {
            String sql = "CREATE INDEX " + getName() + "_idx ON " + getName()
                    + " ( " + Columns.MESSAGE_ID + " );";
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
            create.append(Columns.CONTENT_TYPE + " TEXT, ");
            create.append(Columns.FILE_PATH + " TEXT, ");
            create.append(Columns.VOICE_SECOND + " INTEGER, ");
            create.append(Columns.DELIVERY_STATUS + " INTEGER, ");
            create.append(Columns.MESSAGE_ID + " TEXT, ");
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

        public String getBulkInsertSQL() {
            long userid = App.readUser().getId();
            StringBuffer bulkInsertSql = new StringBuffer(512);
            bulkInsertSql.append("INSERT INTO ").append(getName()).append("( ");
            bulkInsertSql.append(Columns.CREATED_DATE + ", ");
            bulkInsertSql.append(Columns.FROM_JID + ", ");
            bulkInsertSql.append(Columns.TO_JID + ", ");
            bulkInsertSql.append(Columns.CONTENT + ", ");
            bulkInsertSql.append(Columns.CONTENT_TYPE + ", ");
            bulkInsertSql.append(Columns.FILE_PATH + ", ");
            bulkInsertSql.append(Columns.VOICE_SECOND + ", ");
            bulkInsertSql.append(Columns.MESSAGE_ID + ", ");
            bulkInsertSql.append(Columns.DELIVERY_STATUS + ", ");
            bulkInsertSql.append(Columns.PACKET_ID);
            bulkInsertSql.append(") ");
            bulkInsertSql.append("SELECT ");
            bulkInsertSql.append("strftime('%s', " + MessageTable.Columns.CREATE_DATE + ") * 1000, ");
            bulkInsertSql.append("CASE " + MessageTable.Columns.USERID + " WHEN " + userid + " THEN 1 ELSE 0 END, ");
            bulkInsertSql.append("'chinatalk_' || CASE " + MessageTable.Columns.USERID + " WHEN " + userid + " THEN " + MessageTable.Columns.TO_USERID  + " ELSE " + MessageTable.Columns.USERID + " END || '@tttalk.org', ");
            bulkInsertSql.append(MessageTable.Columns.FROM_CONTENT + ", ");
            bulkInsertSql.append(MessageTable.Columns.FILE_TYPE + ", ");
            bulkInsertSql.append(MessageTable.Columns.FILE_PATH + ", ");
            bulkInsertSql.append(MessageTable.Columns.FROM_CONTENT_LENGTH + ", ");
            bulkInsertSql.append(MessageTable.Columns.MESSAGEID + ", ");
            bulkInsertSql.append(ChatProvider.DS_SENT_OR_READ + ", ");
            bulkInsertSql.append("'' ");
            bulkInsertSql.append("FROM " + MessageTable.getName() + " ");
            bulkInsertSql.append("ORDER BY " + MessageTable.Columns.ID + " ASC");


            if (BuildConfig.DEBUG)
                Log.w(TAG, "sql:" + bulkInsertSql.toString());
            return bulkInsertSql.toString();
        }

        public String[] getIndexColumns() {
            return new String[] { Columns.ID, Columns.CREATED_DATE, Columns.FROM_JID, Columns.TO_JID,
                    Columns.CONTENT, Columns.CONTENT_TYPE,Columns.FILE_PATH,Columns.VOICE_SECOND,
                    Columns.DELIVERY_STATUS, Columns.MESSAGE_ID, Columns.PACKET_ID };
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
            chat.setMessageId(cursor.getLong(cursor
		            .getColumnIndex(Columns.MESSAGE_ID)));
            chat.setId(cursor.getInt(cursor
		            .getColumnIndex(Columns.ID)));
            chat.setContent(cursor.getString(cursor
		            .getColumnIndex(Columns.CONTENT)));
            chat.setType(cursor.getString(cursor
		            .getColumnIndex(Columns.CONTENT_TYPE)));
            chat.setFilePath(cursor.getString(cursor
		            .getColumnIndex(Columns.FILE_PATH)));
            chat.setFromContentLength(cursor.getInt(cursor
		            .getColumnIndex(Columns.VOICE_SECOND)));
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

        /** * Message -> ContentValues * * @param message * @param isUnread * @return */
        public ContentValues toContentValues(Chat chat) {
            final ContentValues v = new ContentValues();
            v.put(Columns.ID, chat.getId());
            v.put(Columns.CREATED_DATE, chat.getCreated_date());
            v.put(Columns.FROM_JID, chat.getFromJid());
            v.put(Columns.TO_JID, chat.getToJid());
            v.put(Columns.CONTENT, chat.getContent());
            v.put(Columns.CONTENT_TYPE, chat.getType());
            v.put(Columns.FILE_PATH, chat.getFilePath());
            v.put(Columns.VOICE_SECOND, chat.getFromContentLength());
            v.put(Columns.DELIVERY_STATUS, chat.getRead());
            v.put(Columns.MESSAGE_ID, chat.getMessageId());
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
            return new String[] { Columns.ID, Columns.JID,
		            Columns.ACCOUNT_USERID,  Columns.TITLE, Columns.CREATE_DATE  };
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

        public static String getCreateIndexSQL() {
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
            return new String[] { Columns.ID, Columns.CHATROOM_ID,
		            Columns.PARTICIPANT_ID  };
        }

        public static String getName() {
            return "tbl_chatroom_user";
        }
    }

	private static final String TAG = Utils.CATEGORY
			+ TableContent.class.getSimpleName();
	public static ChannelTable ChannelTable = new ChannelTable();
	public static FriendTable FriendTable = new FriendTable();
	public static HotUserPhotoTable HotUserPhotoTable = new HotUserPhotoTable();
	public static MessageTable MessageTable = new MessageTable();
	public static UserTable UserTable = new UserTable();
	public static UserPhotoTable UserPhotoTable = new UserPhotoTable();
	public static UserPropTable UserPropTable = new UserPropTable();
	public static CommentNewsTable CommentNewsTable = new CommentNewsTable();
    public static ChatTable ChatTable = new ChatTable();
    public static ChatRoomTable ChatRoomTable = new ChatRoomTable();
    public static ChatRoomUserTable ChatRoomUserTable = new ChatRoomUserTable();
}