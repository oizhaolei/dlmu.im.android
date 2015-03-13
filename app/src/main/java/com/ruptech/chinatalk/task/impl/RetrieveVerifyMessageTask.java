package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class RetrieveVerifyMessageTask extends GenericTask {

	private List<Map<String, String>> mMessageList;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		mMessageList = App.getHttpServer().retrieveVerifyMessage();

		return TaskResult.OK;
	}

	public List<Map<String, String>> getmMessageList() {
		return mMessageList;
	}

}