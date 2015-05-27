package com.ruptech.chinatalk.event;

public class NewChatEvent {
    public String fromJID;
    public String chatMessage;

    public NewChatEvent(String fromJID, String chatMessage) {
        this.fromJID = fromJID;
        this.chatMessage = chatMessage;
    }
}
