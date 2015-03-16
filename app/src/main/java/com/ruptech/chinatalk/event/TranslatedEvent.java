package com.ruptech.chinatalk.event;

public class TranslatedEvent {
    public String fromJID;
    public String chatMessage;

    public TranslatedEvent(String fromJID, String chatMessage) {
        this.fromJID = fromJID;
        this.chatMessage = chatMessage;
    }
}
