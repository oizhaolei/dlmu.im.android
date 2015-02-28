package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RequestVerifyTask extends GenericTask {
	private final long messageId;

	public RequestVerifyTask(long messageId) {
		this.messageId = messageId;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().requestVerifyMessage(messageId);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { messageId };
	}
}
