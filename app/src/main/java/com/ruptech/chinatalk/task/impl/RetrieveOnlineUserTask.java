package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveOnlineUserTask extends GenericTask {

	private List<User> onlineUserList;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		onlineUserList = App.getHttpServer().retrieveOnlineUsers();

		return TaskResult.OK;
	}

	public List<User> getOnlineUserList() {
		return onlineUserList;
	}
}