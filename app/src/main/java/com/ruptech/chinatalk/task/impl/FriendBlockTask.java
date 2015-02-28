package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendBlockTask extends GenericTask {

	private final long friendId;

	public FriendBlockTask(long friendId) {
		this.friendId = friendId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		Friend friend = App.getHttpServer().blockFriend(friendId);
		App.messageDAO.deleteByUserId(friendId);
		App.friendDAO.mergeFriend(friend);
		App.mBadgeCount.removeNewMessageCount(friendId);
		// PrefUtils.removePrefNewMessageCount(friendId);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { friendId };
	}
}