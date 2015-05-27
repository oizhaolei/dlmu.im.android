package com.ruptech.chinatalk.smack;


import com.ruptech.chinatalk.model.Chat;

import org.jivesoftware.smack.SmackException;

public interface TTTalkSmack {
    boolean login(String account, String password) throws Exception;

    boolean logout();

    boolean isAuthenticated();

    void sendMessage(String toJid, Chat chat) throws SmackException.NotConnectedException;
}
