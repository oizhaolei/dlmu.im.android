package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkTranslatedExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "translated";
    private String cost = null;
    private String message_id = null;
    private String balance = null;

    public TTTalkTranslatedExtension(String test, String ver, String title, String cost, String message_id, String balance) {
        super(test, ver, title);

        this.cost = cost;
        this.message_id = message_id;
        this.balance = balance;
    }

    public String getCost() {
        return cost;
    }

    public String getBalance() {
        return balance;
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
                .append(' ').append("cost=\"").append(cost).append("\"")
                .append(' ').append("message_id=\"").append(message_id).append("\"")
                .append(' ').append("balance=\"").append(balance).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String cost = parser.getAttributeValue("", "cost");
            String message_id = parser.getAttributeValue("", "message_id");
            String balance = parser.getAttributeValue("", "balance");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkTranslatedExtension(test, ver, title, cost, message_id, balance);
        }
    }
}
