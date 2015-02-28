package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UserPhotoRemoveTask extends GenericTask {
	private final Long userPhotoId;

	public UserPhotoRemoveTask(long userPhotoId) {
		this.userPhotoId = userPhotoId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpStoryServer().deleteUserPhoto(userPhotoId);
		App.userPhotoDAO.deleteUserPhotosById(userPhotoId);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { userPhotoId };
	}
}