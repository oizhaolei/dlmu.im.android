package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveMyChannelListTask extends GenericTask {

	private List<Channel> myChannelList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;

	public RetrieveMyChannelListTask(boolean top, long maxId, long sinceId) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		myChannelList = App.getHttpStoryServer().retrieveUserChannelList(maxId,
				sinceId);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { maxId, sinceId };
	}

	public List<Channel> getmyChannelList() {
		return myChannelList;
	}

	public boolean isTop() {
		return top;
	}

}