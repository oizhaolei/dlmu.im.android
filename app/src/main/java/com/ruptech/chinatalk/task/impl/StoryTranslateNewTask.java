package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class StoryTranslateNewTask extends GenericTask {

	private StoryTranslate storyTranslate;
	private final String content;
	private final String lang;
	private final long user_photo_id;

	public StoryTranslateNewTask(String content, String lang, long user_photo_id) {
		this.content = content;
		this.lang = lang;
		this.user_photo_id = user_photo_id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		storyTranslate = App.getHttpStoryServer().postTranslate(user_photo_id,
				content, lang);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { user_photo_id, content, lang };
	}

	public StoryTranslate getStoryTranslate() {
		return storyTranslate;
	}

}