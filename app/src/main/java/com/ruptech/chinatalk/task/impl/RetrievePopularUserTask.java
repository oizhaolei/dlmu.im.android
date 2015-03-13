package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrievePopularUserTask extends GenericTask {

	private List<User> popularUserList;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		popularUserList = App.getHttpServer().retrievePopularUsers();

		return TaskResult.OK;
	}

	public List<User> getPopularUserList() {
		return popularUserList;
	}
}