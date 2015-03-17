package com.ruptech.chinatalk.smack;


import com.ruptech.chinatalk.exception.XMPPException;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import java.util.List;

public interface TTTalkSmack {
    public boolean login(String account, String password) throws XMPPException;

    public boolean logout();

    public boolean isAuthenticated();

	String getUser();

    public boolean createAccount(String username, String password);
    public void sendMessage(String toJid,Chat chat);
    public void sendGroupMessage(MultiUserChat chatRoom,Chat chat);
    public String getNameForJID(String jid);

    public MultiUserChat createChatRoom(List<User> inviteUserList);
    public MultiUserChat createChatRoomByRoomName(String roomName);
    public RoomInfo getChatRoomInfo(String roomName);
}
