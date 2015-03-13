package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkAnnouncementExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "announcement";
    private String announcement_id = null;

    public TTTalkAnnouncementExtension(String test, String ver, String title, String announcement_id) {
        super(test, ver, title);

        this.announcement_id = announcement_id;
    }



    public String get_announcement_id() {
        return announcement_id;
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
                .append(' ').append("announcement_id=\"").append(announcement_id).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String announcement_id = parser.getAttributeValue("", "announcement_id");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkAnnouncementExtension(test, ver, title, announcement_id);
        }
    }
}
