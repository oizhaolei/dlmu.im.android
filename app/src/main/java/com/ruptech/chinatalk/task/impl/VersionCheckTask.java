package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.ServerAppInfo;

public class VersionCheckTask extends GenericTask {

	private String error;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		// check version
		ServerAppInfo serverAppInfo = App.getHttpServer().ver();
		if (serverAppInfo != null) {
			App.writeServerAppInfo(serverAppInfo);
		}
		return TaskResult.OK;
	}

	public String getError() {
		return error;
	}

}
