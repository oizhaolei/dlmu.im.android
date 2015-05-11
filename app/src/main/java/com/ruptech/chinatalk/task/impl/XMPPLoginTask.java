package com.ruptech.chinatalk.task.impl;


import com.ruptech.chinatalk.smack.TTTalkSmack;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class XMPPLoginTask extends GenericTask {
	private final TTTalkSmack smack;
	String account;
	String password;

	public XMPPLoginTask(TTTalkSmack smack,String username, String password) {
		this.smack = smack;
		this.account = username;
		this.password = password;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		boolean result =  smack.login(account, password);
		if (!result) {
			throw new Exception("failed.");
		}
		return TaskResult.OK;
	}
}