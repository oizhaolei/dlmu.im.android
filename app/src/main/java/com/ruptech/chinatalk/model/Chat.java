package com.ruptech.chinatalk.model;

import java.io.Serializable;

public class Chat implements Serializable {
    private static final long serialVersionUID = -850853231465927885L;
    protected int id;
    protected long date;
    protected int fromMe;
    protected int read;
    protected String jid;
    protected String message;
    protected String type;
    public String file_path;
    protected String fileType;
    protected String from_lang;
    protected String to_lang;
    public int from_content_length;
    protected String to_content;
    protected long message_id;
    protected String pid;

    public String getTo_content() {
        return to_content;
    }

    public void setTo_content(String to_content) {
        this.to_content = to_content;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getFromMe() {
        return fromMe;
    }

    public void setFromMe(int fromMe) {
        this.fromMe = fromMe;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public long getMessageId() {
        return message_id;
    }

    public void setMessageId(long messageId){
        message_id = messageId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromLang() {
        return from_lang;
    }

    public void setFromLang(String from_lang) {
        this.from_lang = from_lang;
    }

    public String getToLang() {
        return to_lang;
    }

    public void setToLang(String to_lang) {
        this.to_lang = to_lang;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}