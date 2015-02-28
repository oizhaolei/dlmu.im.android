package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveStoryTranslateListTask extends GenericTask {

	private List<StoryTranslate> storyTranslateList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private long user_photo_id = -1;
	private long user_id = -1;

	public RetrieveStoryTranslateListTask(boolean top, long maxId,
			long sinceId, long user_photo_id, long user_id) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.user_photo_id = user_photo_id;
		this.user_id = user_id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		storyTranslateList = App.getHttpStoryServer()
				.retrieveStoryTranslateList(maxId, sinceId, user_photo_id,
						user_id);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { maxId, sinceId, user_photo_id, user_id };
	}

	public List<StoryTranslate> getStoryTranslateList() {
		return storyTranslateList;
	}

	public boolean isTop() {
		return top;
	}

}