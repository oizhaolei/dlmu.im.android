package com.ruptech.chinatalk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentNews implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8296137653082737018L;

	protected long id;
	public long user_id;
	public String user_fullname;
	public long from_user_id;
	public long relation_id;
	public long news_id;
	public String news_title;
	public String pic_url;
	public String user_pic_url;
	public String content;
	public String create_date;

	public String type;

	public CommentNews() {
	}

	public CommentNews(JSONObject json) throws JSONException {
		id = json.optLong("id");
		user_id = json.optLong("user_id");
		from_user_id = json.optLong("from_user_id");
		relation_id = json.optLong("relation_id");
		news_id = json.optLong("news_id");
		pic_url = json.optString("pic_url");
		user_pic_url = json.optString("user_pic_url");
		news_title = json.optString("news_title");
		content = json.optString("content");
		type = json.optString("type");
		user_fullname = json.optString("user_fullname");
		create_date = json.optString("create_date");
	}

	public String getContent() {
		return content;
	}

	public String getCreate_date() {
		return create_date;
	}

	public long getFrom_user_id() {
		return from_user_id;
	}

	public long getId() {
		return id;
	}

	public long getNews_id() {
		return news_id;
	}

	public String getNews_title() {
		return news_title;
	}

	public String getPic_url() {
		return pic_url;
	}

	public long getRelation_id() {
		return relation_id;
	}

	public String getType() {
		return type;
	}

	public String getUser_fullname() {
		return user_fullname;
	}

	public long getUser_id() {
		return user_id;
	}

	public String getUser_pic_url() {
		return user_pic_url;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public void setFrom_user_id(long from_user_id) {
		this.from_user_id = from_user_id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setNews_id(long news_id) {
		this.news_id = news_id;
	}

	public void setNews_title(String news_title) {
		this.news_title = news_title;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public void setRelation_id(long relation_id) {
		this.relation_id = relation_id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUser_fullname(String user_fullname) {
		this.user_fullname = user_fullname;
	}

	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	public void setUser_pic_url(String user_pic_url) {
		this.user_pic_url = user_pic_url;
	}

	@Override
	public String toString() {
		return "Channel [user_id=" + user_id + ", from_user_id=" + from_user_id
				+ ", relation_id=" + relation_id + ", user_pic_url="
				+ user_pic_url + ", news_id=" + news_id + ", pic_url="
				+ pic_url + ", news_title=" + news_title + ", user_fullname="
				+ user_fullname + ", content=" + content + ", type=" + type
				+ ", create_date=" + create_date
				+ ", id=" + id + "]";
	}

}
