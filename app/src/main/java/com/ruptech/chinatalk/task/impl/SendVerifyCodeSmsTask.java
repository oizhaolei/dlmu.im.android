package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class SendVerifyCodeSmsTask extends GenericTask {
	private final String tel;

	public SendVerifyCodeSmsTask(String tel) {
		this.tel = tel;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().sendUserVerifyCode(tel);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel };
	}
}
