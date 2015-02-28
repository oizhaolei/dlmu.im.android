package com.ruptech.chinatalk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Friend implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 84474430729754859L;
	public long user_id;
	public long friend_id;
	protected long id;
	public String create_date;

	public String friend_nickname;

	public String friend_memo;

	public String friend_method;

	public int done;

	public int wallet_priority;

	public int is_top;

	public Friend() {
	}

	public Friend(JSONObject json) throws JSONException {

		id = json.getLong("id");
		user_id = json.getLong("user_id");
		friend_id = json.getLong("friend_id");
		done = json.getInt("done");
		wallet_priority = json.getInt("wallet_priority");
		friend_nickname = json.getString("friend_nickname");
		friend_memo = json.getString("friend_memo");
		friend_method = json.getString("friend_method");
		create_date = json.getString("create_date");
	}

	public String getCreate_date() {
		return create_date;
	}

	public int getDone() {
		return done;
	}

	public long getFriend_id() {
		return friend_id;
	}

	public String getFriend_memo() {
		return friend_memo;
	}

	public String getFriend_method() {
		return friend_method;
	}

	public String getFriend_nickname() {
		return friend_nickname;
	}

	public long getId() {
		return id;
	}

	public int getIs_top() {
		return is_top;
	}

	public long getUser_id() {
		return user_id;
	}

	public int getWallet_priority() {
		return wallet_priority;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public void setDone(int done) {
		this.done = done;
	}

	public void setFriend_id(long friend_id) {
		this.friend_id = friend_id;
	}

	public void setFriend_memo(String friend_memo) {
		this.friend_memo = friend_memo;
	}

	public void setFriend_method(String friend_method) {
		this.friend_method = friend_method;
	}

	public void setFriend_nickname(String friend_nickname) {
		this.friend_nickname = friend_nickname;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setIs_top(int is_top) {
		this.is_top = is_top;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public void setWallet_priority(int wallet_priority) {
		this.wallet_priority = wallet_priority;
	}

	@Override
	public String toString() {
		return "Friend [user_id=" + user_id + ", friend_id=" + friend_id
				+ ", friend_nickname=" + friend_nickname + ", friend_memo="
				+ friend_memo + ", friend_method=" + friend_method + ", done="
				+ done + ", wallet_priority=" + wallet_priority
				+ ", create_date=" + create_date + "]";
	}

}
