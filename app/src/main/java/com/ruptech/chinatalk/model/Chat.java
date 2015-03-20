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
	protected String pid;
	protected int status;

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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}