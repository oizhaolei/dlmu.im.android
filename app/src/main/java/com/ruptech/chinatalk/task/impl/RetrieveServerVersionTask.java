package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.ServerAppInfo;

public class RetrieveServerVersionTask extends GenericTask {

	private String error;
	private ServerAppInfo serverAppInfo;

	public ServerAppInfo getServerAppInfo() {
		return serverAppInfo;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		// check version
		serverAppInfo = App.getHttpServer().ver();
		if (serverAppInfo != null) {
			App.writeServerAppInfo(serverAppInfo);
		}
		return TaskResult.OK;
	}

	public String getError() {
		return error;
	}

}
