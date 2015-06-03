package com.ruptech.chinatalk.event;

public class VerificationEvent {
    public String fromJID;
    public String chatMessage;

    public VerificationEvent(String fromJID, String chatMessage) {
        this.fromJID = fromJID;
        this.chatMessage = chatMessage;
    }
}
