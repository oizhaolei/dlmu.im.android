package com.ruptech.chinatalk.model;

import java.io.Serializable;

public class ChatRoom implements Serializable {
	public int id;
	public String jid;
	public long accountUserId;
	public String title;
	public Long[] participantIds;

	public String create_date;

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}


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

	public Long[] getParticipantIds() {
		return participantIds;
	}

	public void setParticipantIds(Long[] participantIds) {
		this.participantIds = participantIds;
	}
}