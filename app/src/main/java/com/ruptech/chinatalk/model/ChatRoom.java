package com.ruptech.chinatalk.model;

import java.io.Serializable;

public class ChatRoom implements Serializable {
    protected int id;
    protected String jid;
    protected long accountUserId;
    protected String title;
	protected long[] participantIds;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public long getAccountUserId() {
		return accountUserId;
	}

	public void setAccountUserId(long accountUserId) {
		this.accountUserId = accountUserId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long[] getParticipantIds() {
		return participantIds;
	}

	public void setParticipantIds(long[] participantIds) {
		this.participantIds = participantIds;
	}
}