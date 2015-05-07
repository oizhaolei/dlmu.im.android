package com.ruptech.chinatalk.task.impl;


import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.smack.TTTalkSmack;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class XMPPLogoutTask extends GenericTask {


	private final TTTalkSmack smack;

	public XMPPLogoutTask(TTTalkSmack smack) {
		this.smack = smack;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		boolean result = smack.logout();
		if (!result) {
			throw new Exception("failed.");
		}
		return TaskResult.OK;
	}
}