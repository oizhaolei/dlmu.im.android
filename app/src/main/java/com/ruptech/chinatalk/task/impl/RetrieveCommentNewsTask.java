package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveCommentNewsTask extends GenericTask {
	public List<CommentNews> commentNewsList;

	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private final String type;

	public RetrieveCommentNewsTask(boolean top, long maxId, long sinceId, String type) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.type = type;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		commentNewsList = App.getHttpStoryServer().retrieveCommentNewsList(
				maxId,
				sinceId, type);
		return TaskResult.OK;
	}

	public List<CommentNews> getCommentNewsList() {
		return commentNewsList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {
				maxId, sinceId, type};
	}

	public boolean isTop() {
		return top;
	}
}