package com.ruptech.chinatalk.smack;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkBalanceExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "balance";
    private String balance = null;

    public TTTalkBalanceExtension(String test, String ver, String title, String balance) {
        super(test, ver, title);
        this.balance = balance;
    }

    public String getBalance() {
        return balance;
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
            String balance = parser.getAttributeValue("", "balance");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkBalanceExtension(test, ver, title,balance);
        }
    }
}
