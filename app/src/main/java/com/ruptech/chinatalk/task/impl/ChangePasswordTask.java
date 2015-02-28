package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class ChangePasswordTask extends GenericTask {
	private final String oldPassword;
	private final String password;

	public ChangePasswordTask(String oldPassword, String password) {
		this.oldPassword = oldPassword;
		this.password = password;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		User user = App.getHttpServer().changePassword(oldPassword, password);
		App.writeUser(user);
		if (user != null) {
			App.userDAO.mergeUser(App.readUser());
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { oldPassword, password };
	}
}