package com.ruptech.chinatalk.utils;

import com.ruptech.chinatalk.App;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ServerAppInfo extends AppVersion {


	/**
	 * @param verInfo
	 * @return
	 * @throws JSONException
	 */
	public static ServerAppInfo parse(JSONObject verInfo) throws JSONException {
		ServerAppInfo info = new ServerAppInfo();

		info.appname = verInfo.getString("appname");
		info.verName = verInfo.getString("verName");
		info.verCode = verInfo.getInt("verCode");

		return info;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 7846953512025127354L;


	private String getServerUrl(String prefix, String uri) {
		if (prefix.startsWith("http://") || prefix.startsWith("https://")) {
			return prefix;
		} else {
			return String.format("%s" + uri + "/%s", App.properties.getProperty("SERVER_BASE_URL"),
					prefix);
		}
	}


	public String getServerOriginal(String prefix) {
		return getServerUrl(prefix, "original");
	}

	public String getServerThumbnail(String prefix) {
		return getServerUrl(prefix, "thumbnail");
	}


	public String getAppServerUrl() {
		return App.properties.getProperty("SERVER_BASE_URL");
	}
}