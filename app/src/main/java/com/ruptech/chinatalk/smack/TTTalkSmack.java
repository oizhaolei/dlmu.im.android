package com.ruptech.chinatalk.smack;


import com.ruptech.chinatalk.exception.XMPPException;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import java.util.List;

public interface TTTalkSmack {
	boolean login(String account, String password) throws Exception;

	boolean logout();

	boolean isAuthenticated();
	
	void sendMessage(String toJid, Chat chat) throws SmackException.NotConnectedException ;
}
