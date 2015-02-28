package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class VerifyCodeVerifyTask extends GenericTask {
	private final String verifyCode;
	private final String tel;

	public VerifyCodeVerifyTask(String tel, String verifyCode) {
		this.tel = tel;
		this.verifyCode = verifyCode;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {

		App.getHttpServer().verifyCodeVerify(tel, verifyCode);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel, verifyCode };
	}
}
