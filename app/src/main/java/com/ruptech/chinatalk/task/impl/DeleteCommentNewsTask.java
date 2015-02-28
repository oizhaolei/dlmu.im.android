package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class DeleteCommentNewsTask extends GenericTask {
	private final long newsId;

	public DeleteCommentNewsTask(long newsId) {
		this.newsId = newsId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		App.getHttpStoryServer().deleteCommentNews(newsId);
		App.commentNewsDAO.deleteCommentNews(newsId);
		return TaskResult.OK;
	}
}