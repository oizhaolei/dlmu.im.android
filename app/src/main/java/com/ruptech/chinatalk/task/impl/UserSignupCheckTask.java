package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UserSignupCheckTask extends GenericTask {
	private User user;
	private final String tel;
	private boolean isCheckOk;
	private final boolean isThirdParty;

	public UserSignupCheckTask(String tel, boolean isThirdParty) {
		this.tel = tel;
		this.isThirdParty = isThirdParty;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		String[] prop = new String[1];
		user = App.getHttpServer().getUserSignupCheck(tel, prop);
		isCheckOk = Boolean.parseBoolean(prop[0]);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel };
	}

	public String getTel() {
		return tel;
	}

	public User getUser() {
		return user;
	}

	public boolean isCheckOk(){
		return isCheckOk;
	}

	public boolean isThirdParty() {
		return isThirdParty;
	}
}
