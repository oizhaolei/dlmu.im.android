package com.ruptech.chinatalk.task.impl;

import org.json.JSONObject;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.http.Response;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.ui.LoginSignupActivity;

public class WeChatGetAccessTokenTask extends GenericTask {
	;
	private final String code;
	private String grant_type;
	private String url;

	public WeChatGetAccessTokenTask(String code) {
		this.code = code;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		String appid = WeChatUtil.APP_ID;
		String secret = WeChatUtil.APP_SECRET;
		grant_type = WeChatUtil.GRANT_TYPE_AUTHORIZATION_CODE;
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
				+ appid + "&secret=" + secret + "&code=" + code
				+ "&grant_type=" + grant_type;

		Response res = App.getHttpServer().get(url);
		JSONObject result = res.asJSONObject();

		String access_token = result.getString("access_token");
		String openid = result.getString("openid");
		if (LoginSignupActivity.instance != null) {
			LoginSignupActivity.instance.wechat_access_token = access_token;
			LoginSignupActivity.instance.wechat_openid = openid;
			App.wechatAccessToken = access_token;

			url = "https://api.weixin.qq.com/sns/userinfo?access_token="
					+ access_token + "&openid=" + openid;
			Response res2 = App.getHttpServer().get(url);
			JSONObject result2 = res2.asJSONObject();
			LoginSignupActivity.instance.wechat_nickname = result2
					.getString("nickname");
			LoginSignupActivity.instance.wechat_headimgurl = result2
					.getString("headimgurl");
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { code, grant_type, url };
	}
}
