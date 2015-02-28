package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UserMemoChangeTask extends GenericTask {

	private Friend friend;
	private final String memo;
	private final long friendid;

	public UserMemoChangeTask(long friendid, String memo) {
		this.friendid = friendid;
		this.memo = memo;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		friend = App.getHttpServer().changeFriendMemo(friendid, memo);
		App.friendDAO.mergeFriend(friend);

		return TaskResult.OK;
	}

	public Friend getFriend() {
		return friend;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {friendid, memo};
	}
}
