package com.ruptech.chinatalk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A data class representing Basic user information element
 */
public class User extends Item implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1311784597606188334L;

	public String password;
	public String username;
	private String fullname;
	private boolean teacher;

	public User() {
	}

	public User(JSONObject json) throws JSONException {
		id = System.currentTimeMillis();
		password = json.optString("password");
		username = json.optString("username");
		teacher = true;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOF_JabberID() {
		return username+"@im.dlmu.edu.cn";
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}


	public static String getOF_username(User user) {
		if (user.teacher)
			return String.format("teacher_%s", user.getUsername());
		else
			return
					String.format("student_%s", user.getUsername());
	}



	public static String getTTTalkIDFromOF_JID(String jid) {
		return jid.substring( jid.indexOf("_"), jid.indexOf("@"));
	}
}
