package com.ruptech.chinatalk.task.impl;

import java.util.Map;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveAnnouncementByIdTask extends GenericTask {
	private Map<String, String> announcement;
	private final long id;

	public RetrieveAnnouncementByIdTask(long id) {
		this.id = id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		announcement = App.getHttpServer().getAnnouncementById(id);
		return TaskResult.OK;
	}

	public Map<String, String> getAnnouncement() {
		return announcement;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { id };
	}
}
