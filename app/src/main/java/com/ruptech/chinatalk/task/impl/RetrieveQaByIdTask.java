package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.Map;

public class RetrieveQaByIdTask extends GenericTask {
	private Map<String, String> qa;
	private final Long id;

	public RetrieveQaByIdTask(long id) {
		this.id = id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		qa = App.getHttpServer().getQaById(id);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {id};
	}

	public Map<String, String> getQa() {
		return qa;
	}
}
