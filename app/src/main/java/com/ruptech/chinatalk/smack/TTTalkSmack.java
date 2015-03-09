package com.ruptech.chinatalk.smack;


import com.ruptech.chinatalk.exception.XMPPException;

public interface TTTalkSmack {
    public boolean login(String account, String password) throws XMPPException;

    public boolean logout();

    public boolean isAuthenticated();

    byte[] getAvatar(String jid) throws XMPPException;

    String getUser();

    public boolean createAccount(String username, String password);
    public void sendMessage(String user, String message, String type);
    public String getNameForJID(String jid);
}
