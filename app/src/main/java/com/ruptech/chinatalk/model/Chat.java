package com.ruptech.chinatalk.model;

import java.io.Serializable;

public class Chat implements Serializable {
    private static final long serialVersionUID = -850853231465927885L;
    protected int id;
    protected long created_date;
    protected String fromJid;
    protected int read;
    protected String toJid;
    protected String content;
    protected String type;
    protected String filePath;
    protected String fromLang;
    protected String toLang;
    protected int fromContentLength;
	@Deprecated
    protected String to_content;
    protected long message_id;
    protected String pid;
    protected int status;

	@Deprecated
    public String getTo_content() {
        return to_content;
    }

	@Deprecated
    public void setTo_content(String to_content) {
        this.to_content = to_content;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreated_date() {
        return created_date;
    }

    public void setCreated_date(long created_date) {
        this.created_date = created_date;
    }

    public String getFromJid() {
        return fromJid;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

	public void setFromJid(String fromJid) {
		this.fromJid = fromJid;
	}

	public String getToJid() {
		return toJid;
	}

	public void setToJid(String toJid) {
		this.toJid = toJid;
	}

	public long getMessage_id() {
		return message_id;
	}

	public void setMessage_id(long message_id) {
		this.message_id = message_id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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