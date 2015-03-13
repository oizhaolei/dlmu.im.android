package com.ruptech.chinatalk.task.impl;

import android.util.Log;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.http.ServerSideException;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.ui.setting.RechargeWeChatActivity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.security.KeyStore;

public class WeChatGetPrepayIdTask extends GenericTask {

	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	private final String entity;
	private final String accessToken;
	private String url;

	public WeChatGetPrepayIdTask(String entity, String accessToken) {
		this.entity = entity;
		this.accessToken = accessToken;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		url = String.format(
				"https://api.weixin.qq.com/pay/genprepay?access_token=%s",
				accessToken);

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "doInBackground, url = " + url);
			Log.d(TAG, "doInBackground, entity = " + entity);
		}

		HttpClient httpClient = getNewHttpClient();

		HttpPost httpPost = new HttpPost(url);

		httpPost.setEntity(new StringEntity(entity));
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		HttpResponse resp = httpClient.execute(httpPost);
		if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			return null;
		}

		byte[] buf = EntityUtils.toByteArray(resp.getEntity());

		if (buf == null || buf.length == 0) {
			return TaskResult.FAILED;
		}

		String content = new String(buf);
		if (content == null || content.length() <= 0) {
			return TaskResult.FAILED;
		}

		JSONObject json = new JSONObject(content);

		if (json.has("prepayid")) { // success case
			RechargeWeChatActivity.prepayId = json.getString("prepayid");
		} else {
			throw new ServerSideException(json.getString("errmsg"));
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { entity, accessToken, url };
	}
}
