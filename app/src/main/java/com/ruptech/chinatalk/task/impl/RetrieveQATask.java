package com.ruptech.chinatalk.task.impl;

import java.util.List;
import java.util.Map;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveQATask extends GenericTask {

	private List<Map<String, String>> mQAList;
	private final int qa_id;

	public RetrieveQATask(int qa_id) {
		this.qa_id = qa_id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		mQAList = App.getHttpServer().retrieveQAList(qa_id);

		return TaskResult.OK;
	}

	public List<Map<String, String>> getmQAList() {
		return mQAList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {qa_id};
	}
}