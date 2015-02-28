package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class AcceptInviteTask extends GenericTask {
	public AcceptInviteTask(String invite_user_id) {
		super();
		this.invite_user_id = invite_user_id;
	}

	private User user;
	private String invite_user_id;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		user = App.getHttpServer().acceptInvite(invite_user_id);
		if (user != null) {
			App.userDAO.mergeUser(user);
			if (App.readUser() != null
					&& user.getId() == App.readUser().getId()) {
				App.writeUser(user);
			}
		}

		return TaskResult.OK;
	}

	public User getUser() {
		return user;
	}
}
