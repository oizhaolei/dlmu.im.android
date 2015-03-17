package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkOldVersionTranslatedExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "old_version_translated";
    private String message_id = null;
    private String userid = null;
    private String from_lang = null;
    private String to_lang = null;
    private String file_path = null;
    private String file_type = null;
    private String file_length = null;
    private String from_content = null;
    private String to_content = null;
    private String create_date = null;

    public TTTalkOldVersionTranslatedExtension(String test, String ver, String title, String userid, String message_id, String from_lang,
                                               String to_lang, String file_path, String file_type, String file_length, String from_content, String to_content, String create_date) {
        super(test, ver, title);
        this.userid = userid;
        this.message_id = message_id;
        this.from_lang = from_lang;
        this.to_lang = to_lang;
        this.file_path = file_path;
        this.file_type = file_type;
        this.file_length = file_length;
        this.from_content = from_content;
        this.to_content = to_content;
        this.create_date = create_date;
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
                .append(' ').append("message_id=\"").append(getMessage_id()).append("\"")
                .append(' ').append("userid=\"").append(getUserid()).append("\"")
                .append(' ').append("from_lang=\"").append(getFrom_lang()).append("\"")
                .append(' ').append("to_lang=\"").append(getTo_lang()).append("\"")
                .append(' ').append("file_path=\"").append(getFilePath()).append("\"")
                .append(' ').append("file_type=\"").append(getFileType()).append("\"")
                .append(' ').append("file_length=\"").append(getFileLength()).append("\"")
                .append(' ').append("from_content=\"").append(getFrom_content()).append("\"")
                .append(' ').append("to_content=\"").append(getTo_content()).append("\"")
                .append(' ').append("create_date=\"").append(getCreate_date()).append("\"")
                .append("/>");
        return buf.toString();
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getMessage_id() {
        return message_id;
    }

    public String getUserid() {
        return userid;
    }

    public String getFrom_lang() {
        return from_lang;
    }

    public String getTo_lang() {
        return to_lang;
    }

    public String getFilePath() {
        return file_path;
    }

    public String getFileType() {
        return file_type;
    }

    public String getFileLength() {
        return file_length;
    }

    public String getFrom_content() {
        return from_content;
    }

    public String getTo_content() {
        return to_content;
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String userid = parser.getAttributeValue("", "userid");
            String message_id = parser.getAttributeValue("", "message_id");
            String from_lang = parser.getAttributeValue("", "from_lang");
            String to_lang = parser.getAttributeValue("", "to_lang");
            String file_path = parser.getAttributeValue("", "file_path");
            String file_type = parser.getAttributeValue("", "file_type");
            String file_length = parser.getAttributeValue("", "file_length");
            String from_content = parser.getAttributeValue("", "from_content");
            String to_content = parser.getAttributeValue("", "to_content");
            String create_date = parser.getAttributeValue("", "create_date");

            while (parser.getEventType() != 3) {
                parser.next();
            }

            return new TTTalkOldVersionTranslatedExtension(test, ver, title, userid, message_id, from_lang,
                    to_lang, file_path, file_type, file_length, from_content, to_content, create_date);
        }
    }
}
