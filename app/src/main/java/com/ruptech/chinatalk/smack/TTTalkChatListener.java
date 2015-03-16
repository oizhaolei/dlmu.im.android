package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.smack.ext.TTTalkExtension;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.packet.Message;

/**
 * TTTalk translated
 */
public class TTTalkChatListener extends PacketExtensionListener<TTTalkExtension> {
	private final ContentResolver mContentResolver;
	protected final String TAG = Utils.CATEGORY + TTTalkChatListener.class.getSimpleName();

	protected TTTalkChatListener(Class<TTTalkExtension> extClass, ContentResolver contentResolver) {
		super(extClass);
		this.mContentResolver = contentResolver;
	}

	@Override
	public void processExtension(Message msg, TTTalkExtension ext) {

		String body = msg.getBody();
		if (body == null) {
			return;
		}

		String fromJID = XMPPUtils.getJabberID(msg.getFrom());

		Log.e(TAG, msg.toXML());

		String type = ext.getType();
		String file_path = ext.getFilePath();
		int content_length = ext.getContentLength();

		long ts = System.currentTimeMillis();

		Chat chat = new Chat();
		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setContent(body);
		chat.setType(type);
		chat.setFilePath(file_path);
		chat.setFromContentLength(content_length);
		chat.setToJid(fromJID);
		chat.setPid(msg.getPacketID());
		chat.setStatus(ChatProvider.DS_NEW);
		chat.setCreated_date(ts);

		ChatProvider.insertChat(mContentResolver, chat);

		App.mBus.post(new NewChatEvent(fromJID, body, type));
	}


}