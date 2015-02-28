package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class PhotoChangeTagTask extends GenericTask {

	private UserPhoto userPhoto;
	private final long id;
	private final String tag;

	public PhotoChangeTagTask(long id, String tag) {
		this.id = id;
		this.tag = tag;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhoto = App.getHttpStoryServer().changePhotoTag(id, tag);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { id, tag };
	}

	public UserPhoto getUserPhoto() {
		return userPhoto;
	}

}