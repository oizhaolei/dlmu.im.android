package com.ruptech.chinatalk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.ruptech.chinatalk.http.Response;
import com.ruptech.chinatalk.utils.DateCommonUtils;

public class UserPhoto extends Item implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -4629555631696325429L;

	public static boolean isAutoTranslated(UserPhoto userPhoto) {
		return userPhoto.getTranslator_id() >= -1 ? true : false;
	}

	public long userid;
	public long translator_id;
	public String translator_fullname;
	public long parent_id;
	public long timeline_id;
	public long timeline_userid;
	public String timeline_fullname;
	public String timeline_create_date;

	public String status;// timeline status: create, like, comment
	public String pic_url;
	public String content;
	public String lang;
	public String address;
	public int late6;
	public int lnge6;
	public int width;
	public int height;
	public String category;
	public int good;
	public int comment;

	public int favorite;
	public boolean checked;

	public boolean oldAddress = true;
	/***/
	public String fullname;
	public String user_pic;
	public String to_content;

	public String to_lang;
	public String comment_fullname;

	public long channel_id;
	public String channel_title;
	public String channel_pic;

	public int present_count;

	public UserPhoto() {
	}

	public UserPhoto(JSONObject json) throws JSONException {
		id = json.getLong("id");
		userid = json.getLong("userid");
		pic_url = json.getString("pic_url");
		content = json.getString("content");
		lang = json.getString("lang");
		late6 = json.optInt("late6", 0);
		lnge6 = json.optInt("lnge6", 0);
		width = json.optInt("width", 0);
		height = json.optInt("height", 0);
		category = json.getString("category");
		good = json.getInt("good");
		comment = json.getInt("comment");
		address = json.optString("address");
		favorite = json.optInt("favorite");
		create_date = DateCommonUtils.parseToDateFromString(json
				.getString("create_date"));

		translator_id = json.optLong("translator_id", -2);
		translator_fullname = json.optString("translator_fullname");
		parent_id = json.optLong("parent_id", -1);
		timeline_id = json.optLong("timeline_id", id);
		timeline_fullname = json.optString("timeline_fullname");
		timeline_userid = json.optLong("timeline_userid");
		timeline_create_date = json.optString("timeline_create_date");

		status = json.optString("status");
		to_content = json.optString("to_content");
		to_lang = json.optString("to_lang");
		fullname = json.optString("fullname");
		user_pic = json.optString("user_pic");
		comment_fullname = json.optString("comment_fullname");

		channel_id = json.optLong("channel_id");
		channel_title = json.optString("channel_title");
		channel_pic = json.optString("channel_pic");

		present_count = json.optInt("present_count", 0);

	}

	public UserPhoto(Response res) throws Exception {
		this(res.asJSONObject());
	}

	public String getAddress() {
		return address;
	}

	public String getCategory() {
		return category;
	}

	public long getChannel_id() {
		return channel_id;
	}

	public String getChannel_pic() {
		return channel_pic;
	}

	public String getChannel_title() {
		return channel_title;
	}

	public boolean getChecked() {
		return checked;
	}

	public int getComment() {
		return comment;
	}

	public String getComment_fullname() {
		return comment_fullname;
	}

	public String getContent() {
		return content;
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

	public int getHeight() {
		return height;
	}

	public String getLang() {
		return lang;
	}

	public int getLate6() {
		return late6;
	}

	public int getLnge6() {
		return lnge6;
	}

	public long getParent_id() {
		return parent_id;
	}

	public String getPic_url() {
		return pic_url;
	}

	public int getPresent_count() {
		return present_count;
	}

	public String getStatus() {
		return status;
	}

	public String getTimeline_Create_date() {
		return this.timeline_create_date;
	}

	public long getTimeline_id() {
		return timeline_id;
	}

	public String getTimelineFullname() {
		return timeline_fullname;
	}

	public long getTimelineUserId() {
		return timeline_userid;
	}

	public String getTo_content() {
		return to_content;
	}

	public String getTo_lang() {
		return to_lang;
	}

	public String getTranslator_fullname() {
		return translator_fullname;
	}

	public long getTranslator_id() {
		return translator_id;
	}

	public String getUser_pic() {
		return user_pic;
	}

	public long getUserid() {
		return userid;
	}

	public int getWidth() {
		return width;
	}

	public boolean isOldAddress() {
		return oldAddress;
	}

	public void mergeFrom(UserPhoto userPhoto) {
		id = userPhoto.getId();
		userid = userPhoto.getUserid();
		pic_url = userPhoto.getPic_url();
		content = userPhoto.getContent();
		lang = userPhoto.getLang();
		late6 = userPhoto.getLate6();
		lnge6 = userPhoto.getLnge6();
		width = userPhoto.getWidth();
		height = userPhoto.getHeight();
		category = userPhoto.getCategory();

		if (oldAddress) {
			address = userPhoto.getAddress();
		}

		good = userPhoto.getGood();
		comment = userPhoto.getComment();
		favorite = userPhoto.getFavorite();
		create_date = userPhoto.getCreate_date();

		translator_id = userPhoto.getTranslator_id();
		translator_fullname = userPhoto.getTranslator_fullname();
		parent_id = userPhoto.getParent_id();
		if (userPhoto.getTimeline_id() != -1)
			timeline_id = userPhoto.getTimeline_id();

		status = userPhoto.getStatus();
		to_content = userPhoto.getTo_content();
		to_lang = userPhoto.getTo_lang();
		fullname = userPhoto.getFullname();
		user_pic = userPhoto.getUser_pic();
		comment_fullname = userPhoto.getComment_fullname();

		channel_id = userPhoto.getChannel_id();
		channel_title = userPhoto.getChannel_title();
		channel_pic = userPhoto.getChannel_pic();

		present_count = userPhoto.getPresent_count();
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setChannel_id(long channel_id) {
		this.channel_id = channel_id;
	}

	public void setChannel_pic(String channel_pic) {
		this.channel_pic = channel_pic;
	}

	public void setChannel_title(String channel_title) {
		this.channel_title = channel_title;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setComment(int comment) {
		this.comment = comment;
	}

	public void setComment_fullname(String comment_fullname) {
		this.comment_fullname = comment_fullname;
	}

	public void setContent(String content) {
		this.content = content;
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

	public void setHeight(int height) {
		this.height = height;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setLate6(int late6) {
		this.late6 = late6;
	}

	public void setLnge6(int lnge6) {
		this.lnge6 = lnge6;
	}

	public void setOldAddress(boolean isRemote) {
		this.oldAddress = isRemote;
	}

	public void setParent_id(long parent_id) {
		this.parent_id = parent_id;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	public void setPresent_count(int present_count) {
		this.present_count = present_count;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTimelineFullname(String fullname) {
		this.timeline_fullname = fullname;
	}

	public void setTimelineId(long timeline_id) {
		this.timeline_id = timeline_id;
	}

	public void setTimelineUserId(long userid) {
		this.timeline_userid = userid;
	}

	public void setTo_content(String transltedContent) {
		this.to_content = transltedContent;
	}

	public void setTo_lang(String to_lang) {
		this.to_lang = to_lang;
	}

	public void setTranslator_fullname(String translator_fullname) {
		this.translator_fullname = translator_fullname;
	}

	public void setTranslator_id(long translator_id) {
		this.translator_id = translator_id;
	}

	public void setUser_pic(String user_pic) {
		this.user_pic = user_pic;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public String toString() {
		return "UserPhoto [userid=" + userid + ", translator_id="
				+ translator_id + ", parent_id=" + parent_id + ", pic_url="
				+ pic_url + ", content=" + content + ", lang=" + lang
				+ ", address=" + address + ", late6=" + late6 + ", lnge6="
				+ lnge6 + ", width=" + width + ", height=" + height
				+ ", category=" + category + ", good=" + good + ", comment="
				+ comment + ", favorite=" + favorite + ", checked=" + checked
				+ ", is_remote_address=" + oldAddress + ", fullname="
				+ fullname + ", user_pic=" + user_pic + ", to_content="
				+ to_content + ", to_lang=" + to_lang + ", comment_fullname="
				+ comment_fullname + ", channel_id=" + channel_id
				+ ", channel_title=" + channel_title + ", channel_pic="
				+ channel_pic + ", present_count=" + present_count + "]";
	}
}
