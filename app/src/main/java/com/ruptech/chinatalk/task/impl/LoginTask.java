package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class LoginTask extends GenericTask {
	private final String username;
	private final String password;
	private final boolean encrypt;

	public LoginTask(String username, String password, boolean encrypt) {
		this.username = username;
		this.password = password;
		this.encrypt = encrypt;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		User me = App.getHttpServer().userLogin(username, password, encrypt);
		App.writeUser(me);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { username, password, encrypt };
	}
}
