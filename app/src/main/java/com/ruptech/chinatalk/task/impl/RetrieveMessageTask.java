package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveMessageTask extends GenericTask {
	private Message message;
	private final Long messageId;

	public RetrieveMessageTask(long messageId) {
		this.messageId = messageId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		message = App.getHttpServer().getMessageById(messageId);
		return TaskResult.OK;
	}

	public Message getMessage() {
		return message;
	}
	@Override
	public Object[] getMsgs() {
		return new Object[] {messageId};
	}
}
