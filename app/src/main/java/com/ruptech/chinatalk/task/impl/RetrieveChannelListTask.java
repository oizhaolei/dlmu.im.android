package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveChannelListTask extends GenericTask {

	private List<Channel> channelList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;

	public RetrieveChannelListTask(boolean top, long maxId, long sinceId) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		channelList = App.getHttpStoryServer().retrieveChannelList(maxId,
				sinceId);
		return TaskResult.OK;
	}

	public List<Channel> getChannelList() {
		return channelList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { top, maxId, sinceId };
	}

	public boolean isTop() {
		return top;
	}

}