package com.ruptech.chinatalk.smack;

import org.jivesoftware.smack.packet.PacketExtension;

public abstract class AbstractTTTalkExtension implements PacketExtension {
    public static final String NAMESPACE = "http://tttalk.org/protocol/tttalk";
    private String test = null;
    private String ver = null;
    private String title = null;

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
