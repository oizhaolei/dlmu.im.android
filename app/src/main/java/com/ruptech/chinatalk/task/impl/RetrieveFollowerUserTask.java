package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveFollowerUserTask extends GenericTask {

	private List<User> followerUserList;
	private long sinceId = 0;

	public RetrieveFollowerUserTask(long sinceId) {
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		long[] sinceIdArray = new long[1];
		followerUserList = App.getHttpServer().retrieveFollowerUsers(sinceId,
				sinceIdArray);
		sinceId = sinceIdArray[0];

		return TaskResult.OK;
	}

	public List<User> getFollowerList() {
		return followerUserList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {sinceId};
	}

	public long getSinceId() {
		return sinceId;
	}
}