package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class SendClientMessageTask extends GenericTask {
	private final String msg;

	public SendClientMessageTask(String msg) {
		this.msg = msg;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		//App.getHttpServer().sendClientMessage(msg);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[]{msg};
	}
}