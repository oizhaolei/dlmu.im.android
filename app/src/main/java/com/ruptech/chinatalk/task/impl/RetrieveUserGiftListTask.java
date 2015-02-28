package com.ruptech.chinatalk.task.impl;

import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveUserGiftListTask extends GenericTask {

	private List<Gift> giftList;
	private final boolean top;
	private final long userId;
	private final long userPhotoId;
	private final long maxId;
	private final long sinceId;

	public RetrieveUserGiftListTask(boolean isTop, long userId,
			long userPhotoId, long maxId, long sinceId) {
		this.top = isTop;
		this.userId = userId;
		this.userPhotoId = userPhotoId;
		this.maxId = maxId;
		this.sinceId = sinceId;
	}

	public RetrieveUserGiftListTask(long userId, long userPhotoId) {
		this.top = false;
		this.userId = userId;
		this.userPhotoId = userPhotoId;
		this.maxId = -1;
		this.sinceId = -1;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		if (maxId == -1 && sinceId == -1) {
			giftList = App.getHttpStoryServer().getUserGiftSumList(userId,
					userPhotoId);
		} else {
			giftList = App.getHttpStoryServer().getUserGiftList(userId,
					userPhotoId, maxId, sinceId);
		}
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
		return new Object[] { userId, userPhotoId, maxId, sinceId };
	}
}