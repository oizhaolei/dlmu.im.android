package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class SignupTask extends GenericTask {

	private final User tempUser;

	public SignupTask(User tempUser) {
		this.tempUser = tempUser;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		User user = App.getHttpServer().userSignup(tempUser.getTel(),
				tempUser.getPassword(), tempUser.getFullname(),
				tempUser.getPic_url(), String.valueOf(tempUser.getGender()),
				tempUser.getLang());
		App.writeUser(user);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tempUser.getTel(), tempUser.getPassword(),
				tempUser.getFullname(), tempUser.getPic_url(),
				String.valueOf(tempUser.getGender()), tempUser.getLang() };
	}
}
