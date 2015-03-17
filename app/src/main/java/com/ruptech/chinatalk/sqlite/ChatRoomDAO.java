package com.ruptech.chinatalk.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.ruptech.chinatalk.model.ChatRoom;
import com.ruptech.chinatalk.model.UserProp;
import com.ruptech.chinatalk.sqlite.SQLiteTemplate.RowMapper;
import com.ruptech.chinatalk.sqlite.TableContent.ChatRoomTable;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import   com.ruptech.chinatalk.sqlite.TableContent.ChatRoomUserTable;
import   com.ruptech.chinatalk.sqlite.TableContent.UserPropTable;

public class ChatRoomDAO {
	private static final String TAG = Utils.CATEGORY + ChatRoomDAO.class.getSimpleName();

	private final SQLiteTemplate mSqlTemplate;

	private static final RowMapper<ChatRoom> mRowMapper = new RowMapper<ChatRoom>() {

		@Override
		public ChatRoom mapRow(Cursor cursor, int rowNum) {
			ChatRoom ChatRoom = ChatRoomTable.parseCursor(cursor);
			return ChatRoom;
		}

	};

	public ChatRoomDAO(Context context) {
		mSqlTemplate = new SQLiteTemplate(ChinaTalkDatabase.getInstance(context).getSQLiteOpenHelper());
	}

	public int deleteChatRoomsByJid(String jid) {
		return mSqlTemplate.deleteByField(ChatRoomTable.getName(), ChatRoomTable.Columns.JID, jid);
	}


	/**
	 * Find a ChatRoom by ChatRoom ID
	 *
	 * @param jid
	 * @return
	 */
	public ChatRoom fetchChatRoomByJid(String jid) {
		ChatRoom chatRoom =  mSqlTemplate.queryForObject(mRowMapper, ChatRoomTable.getName(), null, ChatRoomTable.Columns.JID
				+ " = ?", new String[]{jid}, null, null, null, "1");
		Long[] participantIds = fetchParticipantIdsFromChatRoom(chatRoom.getId());
		chatRoom.setParticipantIds(participantIds);
		return chatRoom;
	}

	private Long[] fetchParticipantIdsFromChatRoom(int chatRoomId) {
		List<Long> list = new ArrayList<>();

		final Cursor c = mSqlTemplate.getDb(false).query(ChatRoomUserTable.getName(),
				new String[]{ChatRoomUserTable.Columns.PARTICIPANT_ID},
				ChatRoomUserTable.Columns.CHATROOM_ID,
				new String[]{String.valueOf(chatRoomId)}, null, null, null, null);
		try {
			while (c.moveToNext()) {
				list.add(c.getLong(c
						.getColumnIndex(ChatRoomUserTable.Columns.PARTICIPANT_ID)));
			}
		} finally {
			c.close();
		}
		Long[] participantIds = new Long[list.size()];
		 list.toArray(participantIds);
		return participantIds;
	}


	/**
	 * Insert a ChatRoom
	 * <p/>
	 * 若报 SQLiteconstraintexception 异常, 检查是否某not null字段为空
	 *
	 * @return
	 */
	public long insertChatRoom(ChatRoom chatRoom) {
		return mSqlTemplate.getDb(true).insert(ChatRoomTable.getName(), null,
				ChatRoomTable.toContentValues(chatRoom));
	}

	public int updateChatRoom(int ChatRoomId, ContentValues values) {
		return mSqlTemplate.updateById(ChatRoomTable.getName(), String.valueOf(ChatRoomId), values);
	}

	/**
	 * Update by using {@link com.ruptech.chinatalk.model.ChatRoom}
	 *
	 * @param chatRoom
	 * @return
	 */
	public int updateChatRoom(ChatRoom chatRoom) {
		return updateChatRoom(chatRoom.getId(), ChatRoomTable.toContentValues(chatRoom));
	}

}
