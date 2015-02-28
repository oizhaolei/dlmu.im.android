package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveStoryLikeListTask extends GenericTask {

	private List<User> userList;
	private final boolean top;
	private final long id;
	private final long maxId;
	private final long sinceId;

	public RetrieveStoryLikeListTask(boolean top, long id, long maxId, long sinceId) {
		super();
		this.top = top;
		this.id = id;
		this.maxId =maxId;
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userList = App.getHttpStoryServer().retrieveUserPhotoLikeList(id,
				maxId, sinceId);
		return TaskResult.OK;
	}

	public List<User> getLikeUserList() {
		return userList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {id, maxId,sinceId};
	}

	public boolean isTop() {
		return top;
	}

}