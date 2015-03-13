package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.PrefUtils;

import java.util.List;

public class RetrieveNewMessagesTask extends GenericTask {

	List<Message> messageList;
	private final long userId;

	private String lastUpdatedate;

	public RetrieveNewMessagesTask(long userId) {
		this.userId = userId;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		lastUpdatedate = PrefUtils.getPrefMessageLastUpdateDate(userId);
		messageList = App.getHttpServer().retrieveNewMessage(userId,
				lastUpdatedate);

		// save lastupdatedate
		PrefUtils.savePrefMessageLastUpdatedate(userId, lastUpdatedate);

		return TaskResult.OK;
	}

	public List<Message> getMessageList() {
		return messageList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { userId, lastUpdatedate };
	}

	public long getUserId() {
		return userId;
	}
}