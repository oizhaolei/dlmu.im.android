package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkFriendExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "friend";
    private String friend_id = null;
    private String fullname = null;

    public TTTalkFriendExtension(String test, String ver, String title, String friend_id, String fullname) {
        super(test, ver, title);

        this.friend_id = friend_id;
        this.fullname = fullname;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public String getFullname(){
        return fullname;
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(getNamespace())
                .append(' ').append("test=\"").append(getTest()).append("\"")
                .append(' ').append("ver=\"").append(getVer()).append("\"")
                .append(' ').append("title=\"").append(getTitle()).append("\"")
                .append(' ').append("friend_id=\"").append(friend_id).append("\"")
                .append(' ').append("fullname=\"").append(fullname).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String friend_id = parser.getAttributeValue("", "friend_id");
            String fullname = parser.getAttributeValue("", "fullname");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkFriendExtension(test, ver, title, friend_id, fullname);
        }
    }
}
