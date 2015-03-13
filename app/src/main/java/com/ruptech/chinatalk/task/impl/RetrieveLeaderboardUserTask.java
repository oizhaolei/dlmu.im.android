package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveLeaderboardUserTask extends GenericTask {

	private List<User> popularUserList;
	private final String type;

	public RetrieveLeaderboardUserTask(String type) {
		this.type = type;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		popularUserList = App.getHttpStoryServer().retrieveLeaderboardUsers(
				type);

		return TaskResult.OK;
	}

	public List<User> getPopularUserList() {
		return popularUserList;
	}
}