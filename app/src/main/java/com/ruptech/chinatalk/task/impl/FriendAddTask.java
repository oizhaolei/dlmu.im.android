package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FriendAddTask extends GenericTask {
	private final List<User> users = new ArrayList<User>();
	private final List<Friend> friends = new ArrayList<Friend>();
	// newfollowCount + alreadyFollowCount - unfollowCount
	private int[] followFriendCountArray = new int[3];
	private final String tel;
	private final String nickname;
	private final String memo;
	private final String lang;
	private final String friend_id;
	private final boolean isContact;

	private String lastUpdatedate;

	public FriendAddTask(String tel, String friend_id, String nickname,
			String memo, String lang, boolean isContact) {
		this.tel = tel;
		this.friend_id = friend_id;
		this.nickname = nickname;
		this.memo = memo;
		this.lang = lang;
		this.isContact = isContact;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {

		lastUpdatedate = Utils.getFriendLastUpdatedate();
		App.getHttpServer()
				.addFriend(tel, friend_id, nickname, memo, lastUpdatedate,
						lang, users, friends, String.valueOf(isContact));

		followFriendCountArray = Utils.getFollowFriendCountArray(
				followFriendCountArray, friends);
		// add to db
		App.userDAO.insertUsers(users, false);

		App.friendDAO.insertFriends(friends, false);

		Utils.updateFriendLastUpdatedate();

		return TaskResult.OK;
	}

	public int[] getFollowFriendCountArray() {
		return followFriendCountArray;
	}

	public String getFriendId() {
		return friend_id;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel, nickname, memo, lang, lastUpdatedate,
				friend_id };
	}
}
