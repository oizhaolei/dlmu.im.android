package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * TTTalk translated
 */
public class TTTalkChatListener implements PacketListener {
	private final ContentResolver mContentResolver;
	protected final String TAG = Utils.CATEGORY + TTTalkChatListener.class.getSimpleName();

	protected TTTalkChatListener(ContentResolver contentResolver) {
		this.mContentResolver = contentResolver;
	}

	@Override
	public void processPacket(Packet packet) {
		Message msg = (Message) packet;

		String body = msg.getBody();
		if (body == null) {
			return;
		}

		String fromJID = XMPPUtils.getJabberID(msg.getFrom());

		Log.e(TAG, msg.toXML());


		long ts = System.currentTimeMillis();

		Chat chat = new Chat();
		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setContent(body);
		chat.setToJid(fromJID);
		chat.setPid(msg.getPacketID());
		chat.setStatus(ChatProvider.DS_NEW);
		chat.setCreated_date(ts);

		ChatProvider.insertChat(mContentResolver, chat);

		App.mBus.post(new NewChatEvent(fromJID, body));
	}


}