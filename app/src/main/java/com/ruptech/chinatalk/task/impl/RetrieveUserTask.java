package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class RetrieveUserTask extends GenericTask {
	private final String username;

	public User getUser() {
		return user;
	}

	private User user;

	public RetrieveUserTask(String username) {
		this.username = username;
	}


	@Override
	protected TaskResult _doInBackground() throws Exception {
		user = App.getHttpServer().retrieveUser(username);


		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[]{username};
	}
}
