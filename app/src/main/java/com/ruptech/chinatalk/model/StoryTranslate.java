package com.ruptech.chinatalk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.ruptech.chinatalk.http.Response;

public class StoryTranslate implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8641923449396812152L;
	public int favorite;
	public long user_id;
	public long user_photo_id;
	public String lang;
	public String from_lang;
	public int good;
	public String fullname;
	public String user_pic;

	public String to_content;
	public String from_content;
	protected long id;
	public String create_date;

	public StoryTranslate() {
	}

	public StoryTranslate(JSONObject json) throws JSONException {
		id = json.getLong("id");
		user_id = json.getLong("user_id");
		lang = json.getString("lang");
		from_lang = json.optString("from_lang");
		good = json.getInt("good");
		favorite = json.optInt("favorite", 0);
		create_date = json.getString("create_date");
		user_photo_id = json.getLong("user_photo_id");
		to_content = json.optString("to_content");
		from_content = json.optString("content");
		user_pic = json.getString("user_pic");
		fullname = json.getString("fullname");
	}

	public StoryTranslate(Response res) throws Exception {
		this(res.asJSONObject());
	}

	public String getCreate_date() {
		return create_date;
	}

	public int getFavorite() {
		return favorite;
	}

	public String getFullname() {
		return fullname;
	}

	public int getGood() {
		return good;
	}

	public long getId() {
		return id;
	}

	public String getLang() {
		return lang;
	}

	public String getTo_content() {
		return to_content;
	}

	public long getUser_id() {
		return user_id;
	}

	public long getUser_photo_id() {
		return user_photo_id;
	}

	public String getUser_pic() {
		return user_pic;
	}

	public void mergeFrom(StoryTranslate userPhoto) {
		id = userPhoto.getId();
		user_id = userPhoto.getUser_id();
		lang = userPhoto.getLang();

		good = userPhoto.getGood();
		create_date = userPhoto.getCreate_date();

		user_photo_id = userPhoto.getUser_photo_id();

		to_content = userPhoto.getTo_content();
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public void setFavorite(int favorite) {
		this.favorite = favorite;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public void setGood(int good) {
		this.good = good;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setTo_content(String transltedContent) {
		this.to_content = transltedContent;
	}

	public void setUser_id(long userid) {
		this.user_id = userid;
	}

	public void setUser_photo_id(long user_photo_id) {
		this.user_photo_id = user_photo_id;
	}

	public void setUser_pic(String user_pic) {
		this.user_pic = user_pic;
	}

	@Override
	public String toString() {
		return "StoryTranslate [userid=" + user_id + ", user_photo_id="
				+ user_photo_id + ", lang=" + lang + ", good=" + good
				+ ", to_content=" + to_content + "]";
	}

}
