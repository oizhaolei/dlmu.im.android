package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.TranslatedEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.smack.ext.TTTalkOldVersionTranslatedExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkTranslatedExtension;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.MessageProvider;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.packet.Message;

/**
 * TTTalk translated
 */
public class OldVersionTranslatedListener extends PacketExtensionListener<TTTalkOldVersionTranslatedExtension> {
    protected final String TAG = Utils.CATEGORY + OldVersionTranslatedListener.class.getSimpleName();
	private final ContentResolver mContentResolver;

	protected OldVersionTranslatedListener(Class<TTTalkOldVersionTranslatedExtension> extClass, ContentResolver contentResolver) {
		super(extClass);
		this .mContentResolver =contentResolver;
	}

	@Override
	public void processExtension(Message msg, TTTalkOldVersionTranslatedExtension ext) {

        String fromJID = XMPPUtils.getJabberID(msg.getFrom());

        Log.e(TAG, msg.toXML());

        long ts = System.currentTimeMillis();

        Chat chat = new Chat();
        chat.setFromJid(App.readUser().getOF_JabberID());
        chat.setContent(ext.getFrom_content());
        String type = AppPreferences.MESSAGE_TYPE_NAME_TEXT;
        if(!Utils.isEmpty(ext.getFilePath())){
            type = ext.getFileType();
        }
        chat.setType(type);
        String file_path = ext.getFilePath();
        chat.setFilePath(file_path);
        String fileLength = ext.getFileLength();
        if(Utils.isEmpty(fileLength))
            fileLength = "0";
        int content_length = Integer.valueOf(fileLength);
        chat.setFromContentLength(content_length);
        chat.setToJid(fromJID);
        chat.setPid(msg.getPacketID());
        chat.setStatus(ChatProvider.DS_NEW);
        chat.setCreated_date(ts);

        String messageId = ext.getMessage_id();
        chat.setMessage_id(Long.valueOf(messageId));

        ChatProvider.insertChat(mContentResolver, chat);

		String body =ext.getTo_content();

		MessageProvider.saveTranslatedContent(mContentResolver, messageId, body);
        App.mBus.post(new NewChatEvent(fromJID, body, type));
	}

}