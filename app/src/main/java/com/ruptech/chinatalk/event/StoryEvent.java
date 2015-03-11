package com.ruptech.chinatalk.event;

public class StoryEvent {
    public final String photo_id;
    public final String title;
    public final String content;
    public final String fullname;

    public StoryEvent(String photo_id, String title, String content, String fullname) {
        this.photo_id = photo_id;
        this.title = title;
        this.content = content;
        this.fullname = fullname;
    }
}
