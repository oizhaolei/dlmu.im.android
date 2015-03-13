package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;

import java.util.List;

public class RetrieveUserPhotoListTask extends GenericTask {

	private List<UserPhoto> userPhotoList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private long userid = -1;
	private long parent_id = 0;
	private final String type;
	private int late6 = 0;
	private int lnge6 = 0;
	private String order = "";
	private String tag = "";

	public RetrieveUserPhotoListTask(boolean top, long maxId, long sinceId,
			long userid, long parent_id, String type, int late6, int lnge6,
			String tag, String order) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.userid = userid;
		this.parent_id = parent_id;
		this.type = type;
		this.late6 = late6;
		this.lnge6 = lnge6;
		this.tag = tag;
		this.order = order;
	}

	public RetrieveUserPhotoListTask(boolean top, long maxId, long sinceId,
			String type) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.type = type;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		if (AbstractUserStoryListActivity.STORY_TYPE_TIMELINE.equals(type)) {
			userPhotoList = App.getHttpStoryServer()
					.retrieveUserPopularPhotoList(maxId, sinceId, type, top);
		} else {
			userPhotoList = App.getHttpStoryServer().retrieveUserPhotoList(
					maxId, sinceId, userid, parent_id, type, late6, lnge6, tag,
					order);
		}

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { top, maxId, sinceId, userid, parent_id, type,
				late6, lnge6, tag, order };
	}

	public List<UserPhoto> getUserPhotoList() {
		return userPhotoList;
	}

	public boolean isTop() {
		return top;
	}

}