package com.ruptech.chinatalk.model;

import com.ruptech.chinatalk.utils.AppPreferences;

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
		fullname = json.optString("fullname");
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
		return getOF_username()+"@" + AppPreferences.IM_SERVER_RESOURCE;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}


	public   String getOF_username( ) {
		if ( teacher)
			return String.format(AppPreferences.TEACHER_PREFIX +"%s",  getUsername());
		else
			return
					String.format(AppPreferences.STUDENT_PREFIX+"%s",  getUsername());
	}


}
