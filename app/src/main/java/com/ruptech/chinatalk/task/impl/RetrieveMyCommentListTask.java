package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;

public class RetrieveMyCommentListTask extends GenericTask {

	private List<UserPhoto> myCommentList;
	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private final long userid;

	public RetrieveMyCommentListTask(boolean top, long maxId, long sinceId,
			long userid) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.sinceId = sinceId;
		this.userid = userid;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		myCommentList = App.getHttpStoryServer().retrieveUserCommentPhotoList(
				maxId, sinceId, userid);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { maxId, sinceId, userid };
	}

	public List<UserPhoto> getMyCommentList() {
		return myCommentList;
	}

	public boolean isTop() {
		return top;
	}

}