package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class RetrieveServiceListTask extends GenericTask {
	private List<Map<String, Object>> serviceList;

	public List<Map<String, Object>> getServiceList() {
		return serviceList;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		serviceList = App.getHttpServer().retrieveServiceList();

		return TaskResult.OK;
	}

}
