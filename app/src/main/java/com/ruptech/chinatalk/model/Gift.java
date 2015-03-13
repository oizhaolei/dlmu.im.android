package com.ruptech.chinatalk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Gift implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8296137653082737018L;

	public String pic_url;
	public String title;
	public int cost_point;
	public int exp_point;
	public int charm_point;
	protected long id;
	public int quantity;

	protected long userid;
	protected String user_pic_url;
	protected String user_fullname;
	public String create_date;

	public String comment_fullname;
	public long to_user_photo_id;

	public Gift() {
	}

	public Gift(JSONObject json) throws JSONException {
		id = json.optLong("id");
		pic_url = json.optString("pic_url");
		title = json.optString("title");
		cost_point = json.optInt("cost_point");
		exp_point = json.optInt("exp_point");
		charm_point = json.optInt("charm_point");
		quantity = json.optInt("quantity");
		userid = json.optLong("from_user_id");
		user_pic_url = json.optString("u_pic_url");
		user_fullname = json.optString("fullname");
		create_date = json.optString("create_date");
		comment_fullname = json.optString("comment_fullname");
		to_user_photo_id = json.optLong("to_user_photo_id");
	}

	public String getPic_url() {
		return pic_url;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCost_point() {
		return cost_point;
	}

	public void setCost_point(int cost_point) {
		this.cost_point = cost_point;
	}

	public int getExp_point() {
		return exp_point;
	}

	public void setExp_point(int exp_point) {
		this.exp_point = exp_point;
	}

	public int getCharm_point() {
		return charm_point;
	}

	public void setCharm_point(int charm_point) {
		this.charm_point = charm_point;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getUser_pic_url() {
		return user_pic_url;
	}

	public void setUser_pic_url(String user_pic_url) {
		this.user_pic_url = user_pic_url;
	}

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public String getUser_fullname() {
		return user_fullname;
	}

	public void setUser_fullname(String user_fullname) {
		this.user_fullname = user_fullname;
	}

	public String getComment_fullname() {
		return comment_fullname;
	}

	public void setComment_fullname(String comment_fullname) {
		this.comment_fullname = comment_fullname;
	}

	@Override
	public String toString() {
		return "Channel [pic_url=" + pic_url + ", title=" + title
				+ ", cost_point=" + cost_point + ", exp_point=" + exp_point
				+ ", charm_point=" + charm_point + ", userid=" + userid
				+ ", user_pic_url=" + user_pic_url + ", user_fullname="
				+ user_fullname + ", quantity=" + quantity + ",create_date="
				+ create_date + ",  comment_fullname=" + comment_fullname
				+ ",  to_user_photo_id=" + to_user_photo_id + ",  id=" + id
				+ "]";
	}

	public long getTo_user_photo_id() {
		return to_user_photo_id;
	}

	public void setTo_user_photo_id(long to_user_photo_id) {
		this.to_user_photo_id = to_user_photo_id;
	}

}
