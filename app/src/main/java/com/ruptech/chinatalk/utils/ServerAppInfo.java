package com.ruptech.chinatalk.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAppInfo extends AppVersion {

	public class LangTrans implements Serializable {
		private static final long serialVersionUID = 7315049503696688631L;
		String lang1;
		String lang2;
		int translateCount;

		@Override
		public String toString() {
			return "LangTrans [lang1=" + lang1 + ", lang2=" + lang2
					+ ", translateCount=" + translateCount + "]";
		}
	}
	public class Server implements Serializable {
		private static final long serialVersionUID = 3040651635748361478L;
		public String appServerUrl;
		public String appServer2Url;
		public String appServerRestUrl;
		public String fileServerUrl;

		@Override
		public String toString() {
			return "Server [appServerUrl=" + appServerUrl
					+ ", appServer2Url=" + appServer2Url
					+ ", appServerRestUrl=" + appServerRestUrl
					+ ", fileServerUrl=" + fileServerUrl + "]";
		}
	}

	public static final long DEFAULT_CLIENT_PERIOD_SECONDS = 10 * 60;

	/**
	 * @param verInfo
	 * @param excludeUrls
	 *            事先已经检测出来的不好用的url
	 * @return
	 * @throws JSONException
	 */
	public static ServerAppInfo parse(JSONObject verInfo,
			List<String> excludeUrls) throws JSONException {
		ServerAppInfo info = new ServerAppInfo();

		info.apkname = verInfo.getString("apkname");
		info.verName = verInfo.getString("verName");
		info.verCode = verInfo.getInt("verCode");
		info.fileSize = verInfo.getInt("fileSize");
		info.point_by_photo_like = verInfo.optInt("point_by_photo_like");
		info.point_by_translate_like = verInfo
				.optInt("point_by_translate_like");
		info.point_by_invite_sns_friend = verInfo
				.optInt("point_by_invite_sns_friend");
		info.client_period_timer = verInfo.optLong("client_period_timer",
				DEFAULT_CLIENT_PERIOD_SECONDS);
		// server
		if (verInfo.has("server_list")) {
			JSONArray server_list = verInfo.getJSONArray("server_list");
			int size = server_list.length();
			List<ServerAppInfo.Server> serverArray = new ArrayList<>();
			Server s = null;
			for (int i = 0; i < size; i++) {
				JSONObject server = server_list.getJSONObject(i);
				String appServerUrl = server.getString("appServerUrl");
				String appServer2Url = server.getString("appServer2Url");
				String appServerRestUrl = server.getString("appServerRestUrl");
				String fileServerUrl = server.getString("fileServerUrl");

				s = info.new Server();
				s.appServerUrl = appServerUrl;
				s.appServer2Url = appServer2Url;
				s.appServerRestUrl = appServerRestUrl;
				s.fileServerUrl = fileServerUrl;
				
				if (!excludeUrls.contains(appServerUrl)
						&& !excludeUrls.contains(fileServerUrl)) {
					serverArray.add(s);
				}
			}
			if (serverArray.size() == 0) {
				serverArray.add(s);
			}
			info.serverArray = serverArray;
		}
		// langs
		if (verInfo.has("langs")) {
			JSONArray list = verInfo.getJSONArray("langs");
			int size = list.length();
			List<LangTrans> langsArray = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				JSONArray lang = list.getJSONArray(i);
				String lang1 = lang.getString(0);
				String lang2 = lang.getString(1);
				int translateCount = lang.getInt(2);

				LangTrans lt = info.new LangTrans();
				lt.lang1 = lang1;
				lt.lang2 = lang2;
				lt.translateCount = translateCount;

				langsArray.add(lt);
			}
			info.langsArray = langsArray;
		}
		// viatel
		if (verInfo.has("viatel")) {
			JSONArray viatelList = verInfo.getJSONArray("viatel");
			int size = viatelList.length();
			String viatelArray[] = new String[size];
			for (int y = 0; y < size; y++) {
				JSONArray viatel = viatelList.getJSONArray(y);
				String region = viatel.getString(0);
				viatelArray[y] = region;
			}
		}
		info.signup_give_balance = verInfo.getInt("signup_give_balance");
		info.good_min_count = verInfo.getInt("good_min_count");
		info.share_to_micro_blog_point = verInfo
				.getInt("share_to_micro_blog_point");
		info.friend_add_from_contact_give_balance = verInfo
				.getInt("friend_add_from_contact_give_balance");
		info.thirdparty_share_interval_time = verInfo
				.getInt("thirdparty_share_interval_time");

		return info;
	}
	public List<Server> serverArray;

	public List<LangTrans> langsArray;

	// 新用户注册奖励点数
	public int signup_give_balance;

	// story 的 popular 数
	public int good_min_count;

	// 第三方微博分享奖励点数
	public int share_to_micro_blog_point;

	// 从通讯录邀请用户奖励点数
	public int friend_add_from_contact_give_balance;

	// 第三方分享奖励点数的时间间隔
	public int thirdparty_share_interval_time;

	// 给图片点赞时送的积分
	public int point_by_photo_like;

	// 给译文点赞时送的积分
	public int point_by_translate_like;

	// 邀请SNS好友时送的积分
	public int point_by_invite_sns_friend;

	// 客户端自动请求服务器频率
	public long client_period_timer = DEFAULT_CLIENT_PERIOD_SECONDS;

	/**
	 *
	 */
	private static final long serialVersionUID = 7846953512025127354L;


	private ServerAppInfo() {
		super();
	}

	private Server _getServer() {
		if (serverArray != null) {
			for (Server server : serverArray) {
				return server;
			}
		}
		return new Server();
	}

	public String getAppServer2Url() {
		return _getServer().appServer2Url;
	}

	public String getAppServeRestUrl() {
		return _getServer().appServerRestUrl;
	}

	public String getAppServerUrl() {
		return _getServer().appServerUrl;
	}

	private String getServerUrl(String prefix, String uri) {
		if (prefix.startsWith("http://") || prefix.startsWith("https://")) {
			return prefix;
		} else {
			return String.format("%s"+uri+"/%s", _getServer().fileServerUrl,
					prefix);
		}
	}
	
	public String getServerMiddle(String prefix) {
		return getServerUrl(prefix, "bmiddle");
	}

	public String getServerOriginal(String prefix) {
		return getServerUrl(prefix, "original");
	}

	public String getServerThumbnail(String prefix) {
		return getServerUrl(prefix, "thumbnail");
	}

	public String getServerPresent(String prefix) {
		return getServerUrl(prefix, "present");
	}

	public String getServerVoice(String suffix) {
		return String.format("%svoice/%s", _getServer().fileServerUrl, suffix);
	}

	@Override
	public String toString() {
		return "ServerAppInfo " + _getServer() + "]";
	}

}