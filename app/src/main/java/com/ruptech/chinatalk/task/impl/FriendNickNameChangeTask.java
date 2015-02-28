package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendNickNameChangeTask extends GenericTask {

	private Friend friend;
	private final String nickname;
	private final long friendid;

	public FriendNickNameChangeTask(String nickname, long friendid) {
		this.nickname = nickname;
		this.friendid = friendid;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		friend = App.getHttpServer().changeFriendNickName(friendid, nickname);
		App.friendDAO.mergeFriend(friend);
		return TaskResult.OK;
	}

	public Friend getFriend() {
		return friend;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {friendid, nickname};
	}
}
