package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveGiftListTask extends GenericTask {

	private List<Gift> giftList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;

	public RetrieveGiftListTask(boolean isTop, long maxId, long sinceId) {
		this.top = isTop;
		this.maxId = maxId;
		this.sinceId = sinceId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		giftList = App.getHttpStoryServer().retrieveGiftList(maxId, sinceId);
		return TaskResult.OK;
	}

	public List<Gift> getGiftList() {
		return giftList;
	}

	public boolean isTop() {
		return top;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { maxId, sinceId };
	}
}