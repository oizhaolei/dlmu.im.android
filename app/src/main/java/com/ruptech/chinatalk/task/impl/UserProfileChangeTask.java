package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UserProfileChangeTask extends GenericTask {
	private User user;
	private final String mFunc;
	private final String mKey;
	private final String mValue;

	public UserProfileChangeTask(String mFunc, String mKey, String mValue) {
		this.mFunc = mFunc;
		this.mKey = mKey;
		this.mValue = mValue;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		user = App.getHttpServer().changeUserProfile(mFunc, mKey, mValue);

		App.userDAO.mergeUser(user);
		if (user.getId() == App.readUser().getId()) {
			App.writeUser(user);
			// 变换语言清理频道数据
			App.channelDAO.deleteAll();
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { mFunc, mKey, mValue };
	}

	public User getUser() {
		return user;
	}
}
