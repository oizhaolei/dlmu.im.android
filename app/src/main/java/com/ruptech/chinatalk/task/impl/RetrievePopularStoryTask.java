package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrievePopularStoryTask extends GenericTask {

	private List<UserPhoto> userPhotoList;
	private final long maxId;
	private final long sinceId;
	private final boolean top;

	public RetrievePopularStoryTask(boolean top, long maxId, long sinceId) {
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhotoList = App.getHttpStoryServer().retrievePopularStoryList(
				maxId, sinceId);

		return TaskResult.OK;
	}

	public List<UserPhoto> getPopularStoryList() {
		return userPhotoList;
	}

	public boolean isTop() {
		return top;
	}
}