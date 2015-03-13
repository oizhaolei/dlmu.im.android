package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkQaExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "qa";
    private String qa_id = null;

    public TTTalkQaExtension(String test, String ver, String title, String qa_id) {
        super(test, ver, title);

        this.qa_id = qa_id;
    }



    public String getqa_id() {
        return qa_id;
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
                .append(' ').append("qa_id=\"").append(qa_id).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String qa_id = parser.getAttributeValue("", "qa_id");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkQaExtension(test, ver, title, qa_id);
        }
    }
}
