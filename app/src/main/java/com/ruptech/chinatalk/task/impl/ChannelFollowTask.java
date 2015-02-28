package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class ChannelFollowTask extends GenericTask {
	private final long channelId;
	private final String follower;

	private Channel channel;

	public ChannelFollowTask(Long channelId, String follower) {
		this.channelId = channelId;
		this.follower = follower;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		channel = App.getHttpStoryServer().followChannel(channelId, follower);
		return TaskResult.OK;
	}

	public Channel getChannel() {
		return channel;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { channelId };
	}
}