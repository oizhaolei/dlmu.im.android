package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveBlockedFriendsTask extends GenericTask {

	private List<User> blockedFriendsList;
	private long sinceId = 0;

	public RetrieveBlockedFriendsTask(long sinceId) {
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		long[] sinceIdArray = new long[1];
		blockedFriendsList = App.getHttpServer().retrieveBlockedFriends(
				sinceId, sinceIdArray);
		sinceId = sinceIdArray[0];
		return TaskResult.OK;
	}

	public List<User> getBlockedFriendsList() {
		return blockedFriendsList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {sinceId};
	}

	public long getSinceId() {
		return sinceId;
	}
}