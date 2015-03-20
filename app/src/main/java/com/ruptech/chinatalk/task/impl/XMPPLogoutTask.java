package com.ruptech.chinatalk.task.impl;


import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class XMPPLogoutTask extends GenericTask {


	@Override
	protected TaskResult _doInBackground() throws Exception {
		boolean result = App.mSmack.logout();
		if (!result) {
			throw new Exception("failed.");
		}
		return TaskResult.OK;
	}
}