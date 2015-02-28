package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendWalletPriorityTask extends GenericTask {
	private final int wallet_priority;
	private final Long friend_id;

	public FriendWalletPriorityTask(Long friend_id, int wallet_priority) {
		this.friend_id = friend_id;
		this.wallet_priority = wallet_priority;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		Friend friend = App.getHttpServer().friendWalletPriority(friend_id,
				wallet_priority);
		App.friendDAO.mergeFriend(friend);

		return TaskResult.OK;
	}
}