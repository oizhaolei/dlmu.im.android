package com.ruptech.chinatalk.model;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.utils.AppPreferences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Serializable {
	public int id = -1;
	public String jid;
	public long accountUserId;
	public String title;
	public List<Long> participantIds = new ArrayList<>();

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

	public List<Long> getParticipantIds() {
		return participantIds;
	}

	public List<User> getParticipantUsers() {
		List<User> users = new ArrayList<>();
		for (Long id : participantIds) {
			users.add(App.userDAO.fetchUser(id));
		}
		return users;
	}

	public void setParticipantIds(List<Long> participantIds) {
		this.participantIds = participantIds;
	}

	public boolean isGroupChat() {
		boolean flag = participantIds.size() > 1 || jid.contains(AppPreferences.GROUP_CHAT_SUFFIX);
		return flag;
	}

	public User getFirstPaticipantUser() {
		User user = null;
		if (participantIds.size() > 0)
			user = App.userDAO.fetchUser(participantIds.get(0));

		return user;
	}

	public void addPaticipant(Long userid) {
		participantIds.add(userid);
	}
}