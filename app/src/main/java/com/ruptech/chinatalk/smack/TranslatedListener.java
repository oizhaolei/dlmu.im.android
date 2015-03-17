package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.sqlite.MessageProvider;
import com.ruptech.chinatalk.event.TranslatedEvent;
import com.ruptech.chinatalk.smack.ext.TTTalkTranslatedExtension;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.packet.Message;

/**
 * TTTalk translated
 */
public class TranslatedListener extends PacketExtensionListener<TTTalkTranslatedExtension> {
	private final ContentResolver mContentResolver;

	protected TranslatedListener(Class<TTTalkTranslatedExtension> extClass,ContentResolver contentResolver) {
		super(extClass);
		this .mContentResolver =contentResolver;
	}

	@Override
	public void processExtension(Message msg, TTTalkTranslatedExtension ext) {
		String messageId = ext.getMessage_id();
		String body = msg.getBody();

		MessageProvider.saveTranslatedContent(mContentResolver, messageId, body);

		String fromJID = XMPPUtils.getJabberID(msg.getFrom());
		App.mBus.post(new TranslatedEvent(fromJID, body));
	}

}