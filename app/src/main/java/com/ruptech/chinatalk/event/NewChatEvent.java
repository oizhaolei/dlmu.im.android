package com.ruptech.chinatalk.event;

public class NewChatEvent {
    public String fromJID;
    public String type;
    public String chatMessage;

    public NewChatEvent(String fromJID, String chatMessage, String type) {
        this.fromJID = fromJID;
        this.type = type;
        this.chatMessage = chatMessage;
    }
}
