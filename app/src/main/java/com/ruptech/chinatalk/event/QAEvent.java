package com.ruptech.chinatalk.event;

public class QAEvent {
    public final String content;
    public final long qaId;

    public QAEvent(String content, long qaId) {
        this.content = content;
        this.qaId = qaId;
    }
}
