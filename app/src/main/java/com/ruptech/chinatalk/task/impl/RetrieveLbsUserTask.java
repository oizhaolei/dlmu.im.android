package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveLbsUserTask extends GenericTask {

	private List<User> lbsUserList;
	private final int late6;
	private final int lnge6;

	public RetrieveLbsUserTask(int late6, int lnge6) {
		this.late6 = late6;
		this.lnge6 = lnge6;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		lbsUserList = App.getHttpServer().retrieveLbsUsers(late6, lnge6);
		return TaskResult.OK;
	}

	public List<User> getLbsUserList() {
		return lbsUserList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { late6, lnge6 };
	}
}