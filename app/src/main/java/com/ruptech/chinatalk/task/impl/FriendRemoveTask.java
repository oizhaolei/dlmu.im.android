package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendRemoveTask extends GenericTask {
	private final Long friendId;

	public FriendRemoveTask(long friendId) {
		this.friendId = friendId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		Friend removedFriend = App.getHttpServer().removeFriend(friendId);
		App.friendDAO.mergeFriend(removedFriend);
		App.messageDAO.deleteByUserId(friendId);

		App.readUser().setFollow_count(App.readUser().getFollow_count() - 1);
		App.mBadgeCount.removeNewMessageCount(friendId);
		// PrefUtils.removePrefNewMessageCount(friendId);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { friendId };
	}
}