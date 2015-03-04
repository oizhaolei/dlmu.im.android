package com.ruptech.chinatalk.smack;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkRequestExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "request";
    private String fee = null;
    private String message_id = null;
    private String create_date = null;

    public TTTalkRequestExtension(String test, String ver, String title, String fee, String message_id, String create_date) {
        super(test, ver, title);

        this.fee = fee;
        this.message_id = message_id;
        this.create_date = create_date;
    }

    public String getFee() {
        return fee;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getMessage_id() {
        return message_id;
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
                .append(' ').append("fee=\"").append(fee).append("\"")
                .append(' ').append("message_id=\"").append(message_id).append("\"")
                .append(' ').append("create_date=\"").append(create_date).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String fee = parser.getAttributeValue("", "fee");
            String message_id = parser.getAttributeValue("", "message_id");
            String create_date = parser.getAttributeValue("", "create_date");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkRequestExtension(test, ver, title, fee, message_id, create_date);
        }
    }
}
