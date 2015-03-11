package com.ruptech.chinatalk.event;

public class PresentEvent {
    public final long present_id;
    public final long to_user_photo_id;
    public final String fullname;
    public final String present_name;
    public final String pic_url;

    public PresentEvent(long present_id, long to_user_photo_id, String fullname, String present_name, String pic_url ) {
        this.present_id = present_id;
        this.to_user_photo_id = to_user_photo_id;
        this.fullname = fullname;
        this.present_name = present_name;
        this.pic_url = pic_url;
    }
}
