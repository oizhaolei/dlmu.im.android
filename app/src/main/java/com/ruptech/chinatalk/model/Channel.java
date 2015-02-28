package com.ruptech.chinatalk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Channel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8296137653082737018L;

	public long title_translate_id;

	public String pic_url;

	public String lang;

	public String title;
	public int follower_count;
	public int popular_count;
	public int is_follower;
	public int recommend;
	protected long id;

	public long popular_id;

	public Channel() {
	}

	public Channel(JSONObject json) throws JSONException {
		id = json.optLong("id");
		popular_id = json.optLong("popular_id");
		title_translate_id = json.optLong("title_translate_id");
		pic_url = json.optString("pic_url");
		lang = json.optString("lang");
		title = json.optString("title");
		follower_count = json.optInt("follower_count");
		popular_count = json.optInt("popular_count");
		is_follower = json.optInt("is_follower");
		recommend = json.optInt("recommend");
	}

	public int getFollower_count() {
		return follower_count;
	}

	public long getId() {
		return id;
	}

	public int getIs_follower() {
		return is_follower;
	}

	public String getLang() {
		return lang;
	}

	public String getPic_url() {
		return pic_url;
	}

	public int getPopular_count() {
		return popular_count;
	}

	public long getPopular_id() {
		return popular_id;
	}

	public String getTitle() {
		return title;
	}

	public long getTitle_translate_id() {
		return title_translate_id;
	}

	public void setFollower_count(int follower_count) {
		this.follower_count = follower_count;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setIs_follower(int is_follower) {
		this.is_follower = is_follower;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public void setPopular_count(int popular_count) {
		this.popular_count = popular_count;
	}

	public void setPopular_id(long popular_id) {
		this.popular_id = popular_id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTitle_translate_id(long title_translate_id) {
		this.title_translate_id = title_translate_id;
	}

	@Override
	public String toString() {
		return "Channel [title_translate_id=" + title_translate_id
				+ ", pic_url=" + pic_url + ", lang=" + lang + ", title="
				+ title + ", follower_count=" + follower_count
				+ ", popular_count=" + popular_count + ", is_follower="
				+ is_follower + ", popular_id=" + popular_id + ", recommend="
				+ recommend + ", id=" + id + "]";
	}

	public int getRecommend() {
		return recommend;
	}

	public void setRecommend(int recommend) {
		this.recommend = recommend;
	}

}
