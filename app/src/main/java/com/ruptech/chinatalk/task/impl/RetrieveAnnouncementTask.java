package com.ruptech.chinatalk.task.impl;

import java.util.List;
import java.util.Map;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveAnnouncementTask extends GenericTask {
	public static List<Map<String, String>> announcementList;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		announcementList = App.getHttpServer().retrieveAnnouncementList();

		return TaskResult.OK;
	}
}