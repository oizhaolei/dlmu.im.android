package com.ruptech.chinatalk.task.impl;

import org.json.JSONObject;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.http.Response;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.ui.setting.RechargeWeChatActivity;

public class WeChatGetAppAccessTokenTask extends GenericTask {

	@Override
	protected TaskResult _doInBackground() throws Exception {
		String appid = WeChatUtil.APP_ID;
		String secret = WeChatUtil.APP_SECRET;

		String url = String
				.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
						appid, secret);

		Response res = App.getHttpServer().get(url);
		JSONObject result = res.asJSONObject();

		RechargeWeChatActivity.accessToken = result.getString("access_token");
		// String expires_in = result.getString("expires_in");

		return TaskResult.OK;
	}
}
