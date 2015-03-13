package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.PrefUtils;

import org.json.JSONObject;

public class GuideTask extends GenericTask {
	private String msg;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		JSONObject data = App.getHttpServer().getGuide();
		PrefUtils.writeGuide(data);
		PrefUtils.savePrefGuideLastUpdate(System.currentTimeMillis());

		return TaskResult.OK;
	}

	@Override
	public String getMsg() {
		return msg;
	}
}
