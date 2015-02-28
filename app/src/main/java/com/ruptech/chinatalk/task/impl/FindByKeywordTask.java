package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FindByKeywordTask extends GenericTask {
	private List<String> channels;
	private final String keyword;
	private final String type = "channel";

	public FindByKeywordTask(String keyword) {
		this.keyword = keyword;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		channels = App.getHttpStoryServer().findByKeyword(type, keyword);

		return TaskResult.OK;
	}

	public List<String> getChannels() {
		return channels;
	}
}
