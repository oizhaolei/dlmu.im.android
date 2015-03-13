package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveMessageHistoryTask extends GenericTask {
	List<Message> messageList;
	private final long friendId;
	private final long minMessageId;

	public RetrieveMessageHistoryTask(long friendId, long minMessageId) {
		this.friendId = friendId;
		this.minMessageId = minMessageId;

	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		messageList = App.getHttpServer().retrieveMessageHistory(friendId,
				minMessageId);
		// add to db
		App.messageDAO.insertMessages(messageList, false);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { friendId, minMessageId };
	}
}