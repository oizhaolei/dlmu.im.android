package com.ruptech.chinatalk.http;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.Utils;

import org.apache.http.HttpException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class HttpConnection {
	private static final String ANONYMOS_USER_ID = "3637";

	private final String TAG = Utils.CATEGORY
			+ HttpConnection.class.getSimpleName();

	/**
	 * 纪录最近n次url访问
	 */
	private static LimitedQueue<String> urlHistory = new LimitedQueue<String>(
			5);

	public static String getUrlHistory() {
		StringBuffer sb = new StringBuffer();

		for (String url : urlHistory) {
			sb.append(url).append('\n');
		}
		return sb.toString();
	}

	static class LimitedQueue<E> extends LinkedList<E> {
		private static final long serialVersionUID = -654911332280856680L;
		private int limit;

		public LimitedQueue(int limit) {
			this.limit = limit;
		}

		@Override
		public boolean add(E o) {
			super.add(o);
			while (size() > limit) {
				super.remove();
			}
			return true;
		}
	}

	public HttpConnection() {
		super();
	}

	public Response get(String url) {
		if (BuildConfig.DEBUG) {
			Log.i(TAG, url);
		}
		urlHistory.add(url);

		String body = HttpRequest.get(url).body();

		return new Response(body);
	}

	public Response post(String url, Map<String, String> postParams) {
		if (BuildConfig.DEBUG) {
			Log.i(TAG, url + ", " + postParams);
		}
		urlHistory.add(url + ", " + postParams);

		String body = HttpRequest.post(url).form(postParams).body();

		return new Response(body);
	}

	public Response uploadFile(String url, File file,
			final UploadProgress uploadProgress) {
		if (BuildConfig.DEBUG) {
			Log.i(TAG, url + ", " + file.getName());
		}
		urlHistory.add(url + ", " + file.getName());

		String body = HttpRequest.post(url).part("file", file.getName(), file)
				.progress(uploadProgress).body();

		return new Response(body);
	}

	/**
	 * Returns the no sign base URL
	 *
	 * @param ifPage
	 *            业务接口名
	 * @return the base URL
	 */

	public String genRequestNoSignURL(String ifPage, Map<String, String> params) {
		// check ServerAppInfo
		if (App.readServerAppInfo() == null) {
			ServerAppInfo serverAppInfo = null;
			try {
				serverAppInfo = ver();
			} catch (Exception e) {
			}
			App.writeServerAppInfo(serverAppInfo);
		}
		if (App.readServerAppInfo() == null) {
			return null;
		}

		//
		if (params == null) {
			params = new HashMap<>();
		}

		String url = ifPage;
		if (!url.startsWith("http")) {
			url = getAppServerUrl() + url;
		}
		url += "?" + encodeParameters(params);
		return url;
	}

	/**
	 * Returns the base URL
	 *
	 * @param ifPage
	 *            业务接口名
	 * @return the base URL
	 */

	public String genRequestURL(String ifPage, Map<String, String> params) {
		// check ServerAppInfo
		if (App.readServerAppInfo() == null) {
			ServerAppInfo serverAppInfo = null;
			try {
				serverAppInfo = ver();
			} catch (Exception e) {
			}
			App.writeServerAppInfo(serverAppInfo);
		}
		if (App.readServerAppInfo() == null) {
			return null;
		}

		String url = ifPage;
		if (!url.startsWith("http")) {
			url = getAppServerUrl() + url;
		}
		url += "?" + encodeParameters(params);
		return url;
	}

	private static String encodeParameters(Map<String, String> params)
			throws RuntimeException {
		StringBuffer buf = new StringBuffer();
		String[] keyArray = params.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);
		int j = 0;
		for (String key : keyArray) {
			String value = params.get(key);
			if (j++ != 0) {
				buf.append("&");
			}
			if (!Utils.isEmpty(value)) {
				try {
					buf.append(URLEncoder.encode(key, "UTF-8")).append("=")
							.append(URLEncoder.encode(value, "UTF-8"));
				} catch (java.io.UnsupportedEncodingException neverHappen) {
					// throw new RuntimeException(neverHappen.getMessage(),
					// neverHappen);
				}
			}
		}

		return buf.toString();
	}

	/**
	 * Issues an HTTP GET request.
	 * 
	 * @return the response
	 * @throws HttpException
	 */
	protected Response _get(String ifPage, Map<String, String> params)
			throws Exception {
		params = genParams(params);

		String url = "";
		for (int i = 0; i < 2; i++) {
			url = genRequestURL(ifPage, params);
			try {
				Response response = get(url);
				return response;
			} catch (HttpRequestException e1) {
				// App.setServerAppInfo(null);
				// Utils.sendClientException(e1);
			}
		}

		throw new NetworkException(
				App.mContext.getString(R.string.network_is_bad));
	}

	public static Map<String, String> genParams(Map<String, String> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		String loginid = ANONYMOS_USER_ID;
		if (App.readUser() != null) {
			loginid = String.valueOf(App.readUser().getId());
		}
		params.put("source", getSource());
		params.put("loginid", loginid);
		String sign = Utils.genSign(params, loginid);
		params.put("sign", sign);

		return params;
	}

	private static String getSource() {
		return "an-" + Utils.getAppVersionCode();
	}

	public ServerAppInfo ver() throws Exception {
		Response res = null;
		List<String> excludeUrls = new ArrayList<>();
        String[] SERVER_BASE_URL = new String[]{
                App.properties.getProperty("SERVER_BASE_URL1"),App.properties.getProperty("SERVER_BASE_URL2")
        };
		for (int i = 0; res == null
				&& i < SERVER_BASE_URL.length; i++) {
			String url = SERVER_BASE_URL[i] + "utils/ver.php";
			try {
				res = get(url);
				JSONObject verInfo = res.asJSONObject();
				ServerAppInfo info = ServerAppInfo.parse(verInfo, excludeUrls);
				return info;
			} catch (Exception e) {
				excludeUrls.add(SERVER_BASE_URL[i]);
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}
		}
		return null;
	}

	abstract protected String getAppServerUrl();

	protected Response _post(String ifPage, Map<String, String> form)
			throws Exception {
		Map<String, String> params = genParams(null);

		String url = null;
		for (int i = 0; i < 5; i++) {
			url = genRequestURL(ifPage, params);
			try {
				Response response = post(url, form);
				return response;
			} catch (HttpRequestException e1) {
				// App.setServerAppInfo(null);
				Utils.sendClientException(e1);
			}
		}
		throw new NetworkException(
				App.mContext.getString(R.string.network_is_bad));
	}

	protected Response _uploadFile(String ifPage, File file,
			UploadProgress uploadProgress) throws Exception {
		Map<String, String> params = genParams(null);

		for (int i = 0; i < 5; i++) {
			String url = genRequestURL(ifPage, params);
			try {
				Response response = uploadFile(url, file, uploadProgress);
				return response;
			} catch (HttpRequestException e1) {
				// App.setServerAppInfo(null);
				Utils.sendClientException(e1);
			}
		}
		throw new NetworkException(
				App.mContext.getString(R.string.network_is_bad));
	}
}
