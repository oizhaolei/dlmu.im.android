package com.ruptech.chinatalk.event;

public class AnnouncementEvent {
    public final String content;
    public final long announcementId;

    public AnnouncementEvent(String content, long announcementId) {
        this.content = content;
        this.announcementId = announcementId;
    }
}
