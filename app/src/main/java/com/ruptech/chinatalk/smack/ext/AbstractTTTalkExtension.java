package com.ruptech.chinatalk.smack.ext;

import org.jivesoftware.smack.packet.PacketExtension;

public abstract class AbstractTTTalkExtension implements PacketExtension {
    public static final String NAMESPACE = "http://tttalk.org/protocol/tttalk";
    public static final String VALUE_TEST = "test";
    public static final String VALUE_VER = "1.0";
    public static final String VALUE_TITLE = "title";

    private String test = VALUE_TEST;
    private String ver = VALUE_VER;
    private String title = VALUE_TITLE;

    public AbstractTTTalkExtension(String test, String ver, String title) {
        this.test = test;
        this.ver = ver;
        this.title = title;
    }

    public String getTest() {
        return test;
    }

    public String getVer() {
        return ver;
    }

    public String getTitle() {
        return title;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

}
