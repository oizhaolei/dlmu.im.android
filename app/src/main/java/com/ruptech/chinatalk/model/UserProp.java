package com.ruptech.chinatalk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserProp implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 53971816464569463L;

	public long userid;

	public String propKey;
	public String propValue;

	protected long id;
	public String create_date;

	public UserProp() {
	}

	public UserProp(JSONObject json) throws JSONException {
		id = json.getLong("id");
		userid = json.getLong("userid");
		propKey = json.getString("prop_key");
		propValue = json.getString("prop_value");
		create_date = json.getString("create_date");
	}

	public String getCreate_date() {
		return create_date;
	}

	public long getId() {
		return id;
	}

	public String getPropKey() {
		return propKey;
	}

	public String getPropValue() {
		return propValue;
	}

	public long getUserid() {
		return userid;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setPropKey(String propKey) {
		this.propKey = propKey;
	}

	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	@Override
	public String toString() {
		return "UserProp [userid=" + userid + ", id=" + id + ", propKey="
				+ propKey + ", propValue=" + propValue + ", create_date="
				+ create_date + "]";
	}

}
