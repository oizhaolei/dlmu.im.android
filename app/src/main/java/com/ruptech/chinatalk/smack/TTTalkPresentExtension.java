package com.ruptech.chinatalk.smack;

import com.ruptech.chinatalk.utils.Utils;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class TTTalkPresentExtension extends AbstractTTTalkExtension {
    public static final String ELEMENT_NAME = "present";
    private String present_id;
    private String to_user_photo_id;
    private String fullname;
    private String present_name;
    private String pic_url;

    public TTTalkPresentExtension(String test, String ver, String title, String present_id, String to_user_photo_id, String fullname, String present_name, String pic_url) {
        super(test, ver, title);

        this.present_id = present_id;
        this.to_user_photo_id = to_user_photo_id;
        this.fullname = fullname;
        this.present_name = present_name;
        this.pic_url = pic_url;
    }

    public String getPresent_id(){
        return present_id;
    }

    public  String getTo_user_photo_id(){
        return to_user_photo_id;
    }

    public String getPresent_name(){
        return present_name;
    }

    public String getPic_url(){
        return pic_url;
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
                .append(' ').append("present_id=\"").append(present_id).append("\"")
                .append(' ').append("to_user_photo_id=\"").append(to_user_photo_id).append("\"")
                .append(' ').append("fullname=\"").append(fullname).append("\"")
                .append(' ').append("present_name=\"").append(present_name).append("\"")
                .append(' ').append("pic_url=\"").append(pic_url).append("\"")
                .append("/>");
        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            parser.next();
            String test = parser.getAttributeValue("", "test");
            String ver = parser.getAttributeValue("", "ver");
            String title = parser.getAttributeValue("", "title");
            String present_id = parser.getAttributeValue("", "present_id");
            String to_user_photo_id = parser.getAttributeValue("", "to_user_photo_id");
            String fullname = parser.getAttributeValue("", "fullname");
            String present_name = parser.getAttributeValue("", "present_name");
            String pic_url = parser.getAttributeValue("", "pic_url");

            while (parser.getEventType() != 3) {
                parser.next();
            }
            if(Utils.isEmpty(to_user_photo_id)){
                to_user_photo_id = "0";
            }

            return new TTTalkPresentExtension(test, ver, title, present_id, to_user_photo_id, fullname, present_name, pic_url);
        }
    }
}
