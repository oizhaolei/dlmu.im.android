package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class SendGroupTask extends GenericTask {
	private final String fromJid;
	private final String toGroupJid;
	private final String subject;
	private final String body;

	public List<String> getSendList() {
		return sendList;
	}

	private List<String> sendList;

	public SendGroupTask(String fromJid, String toGroupJid, String subject, String body) {
		this.fromJid = fromJid;
		this.toGroupJid = toGroupJid;
		this.subject = subject;
		this.body = body;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		sendList = App.getHttpServer().sendGroupMessage(fromJid, toGroupJid, subject, body);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[]{fromJid, toGroupJid, subject, body};
	}
}
