package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendReportTask extends GenericTask {
	private final Long friendId;
	private final String report_content;

	public FriendReportTask(long friendId, String report_content) {
		this.friendId = friendId;
		this.report_content = report_content;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().friendReport(friendId, report_content);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { friendId };
	}
}