package com.ruptech.chinatalk.task.impl;

import java.util.ArrayList;
import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveChannelPhotoListTask extends GenericTask {

	private final List<UserPhoto> userPhotoList = new ArrayList<UserPhoto>();;
	private final List<Channel> channel = new ArrayList<Channel>();
	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private final long maxGood;
	private final long sinceGood;
	private final long channel_id;
	private final String type;

	public RetrieveChannelPhotoListTask(boolean top, long maxId, long sinceId,
			long maxGood, long sinceGood, long channel_id, String type) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.maxGood = maxGood;
		this.sinceGood = sinceGood;
		this.channel_id = channel_id;
		this.type = type;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpStoryServer().retrieveChannelPhotoList(maxId, sinceId,
				maxGood, sinceGood, channel_id, type, channel, userPhotoList);
		return TaskResult.OK;
	}

	public Channel getChannel() {
		if (channel.size() > 0)
			return channel.get(0);
		else {
			return null;
		}
	}

	public List<UserPhoto> getChannelPhotoList() {
		return userPhotoList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {maxId, sinceId,
				maxGood, sinceGood, channel_id, type, channel,
				userPhotoList};
	}

	public boolean isTop() {
		return top;
	}

	public long getChannelId() {
		return channel_id;
	}
}