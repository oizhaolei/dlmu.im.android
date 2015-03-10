package com.ruptech.chinatalk.smack;

import com.ruptech.chinatalk.utils.Utils;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by zhaolei on 15/1/30.
 */
public class TTTalkExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "tttalk";
    private String type = null;
    private String file_path = null;
    private int content_length = 0;

    public TTTalkExtension(String test, String ver, String title, String type, String file_path, String content_length) {
        super(test, ver, title);

        this.type = type;
        this.file_path = file_path;
        if (!Utils.isEmpty(content_length))
            this.content_length = Integer.parseInt(content_length);
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getType() {
        return type;
    }

    public String getFilePath() {
        return file_path;
    }

    public int getContentLength() {
        return content_length;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns=\"").append(getNamespace()).append("\"")
                .append(' ').append("test=\"").append(getTest()).append("\"")
                .append(' ').append("ver=\"").append(getVer()).append("\"")
                .append(' ').append("title=\"").append(getTitle()).append("\"")
                .append(' ').append("type=\"").append(type).append("\"")
                .append(' ').append("file_path=\"").append(file_path).append("\"")
                .append(' ').append("content_length=\"").append(content_length).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {
        public Provider() {
        }

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String type = parser.getAttributeValue("", "type");
            String file_path = parser.getAttributeValue("", "file_path");
            String content_length = parser.getAttributeValue("", "content_length");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkExtension(test, ver, title, type, file_path, content_length);
        }
    }
}
