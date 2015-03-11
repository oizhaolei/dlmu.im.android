package com.ruptech.chinatalk.smack;

import com.ruptech.chinatalk.utils.Utils;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkStoryExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "story";
    private String photo_id;
    private String content;
    private String fullname;

    public TTTalkStoryExtension(String test, String ver, String title, String photo_id, String content, String fullname) {
        super(test, ver, title);

        this.photo_id = photo_id;
        this.content = content;
        this.fullname = fullname;
    }
    public String getPhoto_id(){
        return photo_id;
    }

    public String getContent(){
        return content;
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
                .append(' ').append("photo_id=\"").append(photo_id).append("\"")
                .append(' ').append("content=\"").append(content).append("\"")
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
            String photo_id = parser.getAttributeValue("", "photo_id");
            String content = parser.getAttributeValue("", "content");
            String fullname = parser.getAttributeValue("", "fullname");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkStoryExtension(test, ver, title, photo_id, content, fullname);
        }
    }
}
