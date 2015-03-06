package com.ruptech.chinatalk.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.utils.Utils;

/**
 * for story module. using RESTFul
 *
 * @author zhaolei
 *
 */
public class Http2Server extends HttpConnection {
	private final String TAG = Utils.CATEGORY
			+ Http2Server.class.getSimpleName();

	public UserPhoto autoTranslatePhoto(long id, String lang) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(id));
		params.put("lang", lang);

		Response res = _get("photo_auto_translate", params);

		JSONObject result = res.asJSONObject();
		UserPhoto userPhoto = new UserPhoto(result);
		return userPhoto;
	}

	public String getAddress(double latitude, double longitude) {

		String address = null;

		Map<String, String> params = new HashMap<>();
		params.put("language", Locale.getDefault().getLanguage());
		params.put("latlng", String.format("%f,%f", latitude, longitude));

		try {
			Response res = _get(getAppServerUrl() + "geocode", params);
			String temp = res.getBody();

			if (!Utils.isEmpty(temp))
				address = temp;

		} catch (Exception e) {
			Utils.sendClientException(e);
			Log.v(TAG, "params:" + e.getMessage());
		}

		return address;

	}

	@Override
	protected String getAppServerUrl() {
		return App.readServerAppInfo().getAppServer2Url();
	}

	public FileUploadInfo uploadUrl(String url ) throws Exception {
		Map<String, String> params = new HashMap<>();
			params.put("url", url);
		Response res = _post(getAppServerUrl() + "upload", params );

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			FileUploadInfo fileInfo = new FileUploadInfo();
			fileInfo.fileName = result.getString("msg");
			fileInfo.width = result.optInt("width");
			fileInfo.height = result.optInt("height");

			return fileInfo;
		}
		throw new HttpException();
	}


}
