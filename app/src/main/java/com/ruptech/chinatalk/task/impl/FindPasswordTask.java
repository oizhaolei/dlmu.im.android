package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FindPasswordTask extends GenericTask {

	private final String tel;

	public FindPasswordTask(String tel) {
		this.tel = tel;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().sendUserPasswordSms(tel);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel };
	}
}