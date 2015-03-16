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
    protected String filePath;
    protected String fromLang;
    protected String toLang;
    protected int fromContentLength;
    protected String to_content;
    protected long message_id;
    protected String pid;
    protected int status;

	public int getVerify_status() {
		return verify_status;
	}

	public void setVerify_status(int verify_status) {
		this.verify_status = verify_status;
	}

	protected int verify_status;

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
        return fromLang;
    }

    public void setFromLang(String fromLang) {
        this.fromLang = fromLang;
    }

    public String getToLang() {
        return toLang;
    }

    public void setToLang(String toLang) {
        this.toLang = toLang;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFromContentLength() {
        return fromContentLength;
    }

    public void setFromContentLength(int fromContentLength) {
        this.fromContentLength = fromContentLength;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}