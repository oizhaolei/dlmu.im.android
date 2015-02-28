package com.ruptech.chinatalk.task.impl;

import org.json.JSONObject;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.PrefUtils;

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
