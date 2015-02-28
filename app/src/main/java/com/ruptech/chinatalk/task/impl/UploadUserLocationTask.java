package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UploadUserLocationTask extends GenericTask {
	private String msg;
	private final int late6;
	private final int lnge6;

	public UploadUserLocationTask(int late6, int lnge6) {
		this.late6 = late6;
		this.lnge6 = lnge6;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		App.getHttpServer().uploadUserLocation(late6, lnge6);

		return TaskResult.OK;
	}
	@Override
	public String getMsg() {
		return msg;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {late6, lnge6};
	}
}
