package com.ruptech.chinatalk.thirdparty.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Share implements Parcelable, Serializable {

	private static final long serialVersionUID = -267348128925172924L;

	private String lang;
	private String thirdparty_share_appname;
	private String thirdparty_share_downloadurl;
	private String thirdparty_share_text;
	private String thirdparty_share_title;
	private String thirdparty_share_imgurl;
	private String thirdparty_share_sina_officalaccount;
	private String thirdparty_share_targeturl;

	public Share(JSONObject obj) {

		try {
			this.lang = obj.getString("lang");
			this.thirdparty_share_appname = obj
					.getString("thirdparty_share_appname");
			if (obj.has("thirdparty_share_downloadurl")) {
				this.thirdparty_share_downloadurl = obj
						.getString("thirdparty_share_downloadurl");
			}
			this.thirdparty_share_imgurl = obj
					.getString("thirdparty_share_imgurl");
			this.thirdparty_share_sina_officalaccount = obj
					.getString("thirdparty_share_sina_officalaccount");
			this.thirdparty_share_targeturl = obj
					.getString("thirdparty_share_targeturl");
			this.thirdparty_share_text = obj.getString("thirdparty_share_text");
			this.thirdparty_share_title = obj
					.getString("thirdparty_share_title");
		} catch (JSONException e) {
		}

	}

	public Share(Parcel in) {
		lang = in.readString();
		thirdparty_share_appname = in.readString();
		thirdparty_share_downloadurl = in.readString();
		thirdparty_share_imgurl = in.readString();
		thirdparty_share_sina_officalaccount = in.readString();
		thirdparty_share_targeturl = in.readString();
		thirdparty_share_text = in.readString();
		thirdparty_share_title = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getLang() {
		return lang;
	}

	public String getThirdparty_share_appname() {
		return thirdparty_share_appname;
	}

	public String getThirdparty_share_downloadurl() {
		return thirdparty_share_downloadurl;
	}

	public String getThirdparty_share_imgurl() {
		return thirdparty_share_imgurl;
	}

	public String getThirdparty_share_sina_officalaccount() {
		return thirdparty_share_sina_officalaccount;
	}

	public String getThirdparty_share_targeturl() {
		return thirdparty_share_targeturl;
	}

	public String getThirdparty_share_text() {
		return thirdparty_share_text;
	}

	public String getThirdparty_share_title() {
		return thirdparty_share_title;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setThirdparty_share_appname(String thirdparty_share_appname) {
		this.thirdparty_share_appname = thirdparty_share_appname;
	}

	public void setThirdparty_share_downloadurl(
			String thirdparty_share_downloadurl) {
		this.thirdparty_share_downloadurl = thirdparty_share_downloadurl;
	}

	public void setThirdparty_share_imgurl(String thirdparty_share_imgurl) {
		this.thirdparty_share_imgurl = thirdparty_share_imgurl;
	}

	public void setThirdparty_share_sina_officalaccount(
			String thirdparty_share_sina_officalaccount) {
		this.thirdparty_share_sina_officalaccount = thirdparty_share_sina_officalaccount;
	}

	public void setThirdparty_share_targeturl(String thirdparty_share_targeturl) {
		this.thirdparty_share_targeturl = thirdparty_share_targeturl;
	}

	public void setThirdparty_share_text(String thirdparty_share_text) {
		this.thirdparty_share_text = thirdparty_share_text;
	}

	public void setThirdparty_share_title(String thirdparty_share_title) {
		this.thirdparty_share_title = thirdparty_share_title;
	}

	@Override
	public String toString() {
		return "ThirdPartyShare [lang=" + lang + ", thirdparty_share_appname="
				+ thirdparty_share_appname + ", thirdparty_share_downloadurl="
				+ thirdparty_share_downloadurl + ", thirdparty_share_imgurl="
				+ thirdparty_share_imgurl
				+ ", thirdparty_share_sina_officalaccount="
				+ thirdparty_share_sina_officalaccount
				+ ", thirdparty_share_targeturl=" + thirdparty_share_targeturl
				+ ", thirdparty_share_text=" + thirdparty_share_text
				+ ", thirdparty_share_title=" + thirdparty_share_title + "]";
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(lang);
		out.writeString(thirdparty_share_appname);
		out.writeString(thirdparty_share_downloadurl);
		out.writeString(thirdparty_share_imgurl);
		out.writeString(thirdparty_share_sina_officalaccount);
		out.writeString(thirdparty_share_targeturl);
		out.writeString(thirdparty_share_text);
		out.writeString(thirdparty_share_title);

	}

}
