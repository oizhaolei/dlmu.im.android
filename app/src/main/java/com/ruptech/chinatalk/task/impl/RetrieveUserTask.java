package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveUserTask extends GenericTask {
	private User user;
	private final long userid;
	public RetrieveUserTask(long userid) {
		this.userid = userid;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		user = App.getHttpServer().getUser(userid);

		if (user != null) {
			App.userDAO.mergeUser(user);

			if (App.readUser() != null
					&& user.getId() == App.readUser().getId()) {
				App.writeUser(user);
			}
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { userid };
	}

	public User getUser() {
		return user;
	}
}
