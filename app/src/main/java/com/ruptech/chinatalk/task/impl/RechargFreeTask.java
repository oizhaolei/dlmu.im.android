package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RechargFreeTask extends GenericTask {

	private int rechargeResult;
	private final String type;

	public RechargFreeTask(String type) {
		this.type = type;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		rechargeResult = App.getHttpServer().freeRecharge(type);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {type};
	}

	public int getRechargeResult() {
		return rechargeResult;
	}

}
