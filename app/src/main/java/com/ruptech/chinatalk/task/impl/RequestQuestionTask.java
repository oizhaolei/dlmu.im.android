package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RequestQuestionTask extends GenericTask {
	private final String text;

	public RequestQuestionTask(String text) {
		this.text = text;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().addQa(text);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { text };
	}
}