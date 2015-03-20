package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class RetrieveFriendsTask extends GenericTask {

	private static void retrieveFriends(List<User> users, List<Friend> friends,
	                                    int[] followFriendCountArray,
	                                    long userId, boolean isNeedNotify) throws Exception {

		String lastUpdatedate = Utils.getFriendLastUpdatedate();
		App.getHttpServer().retrieveNewFriends(lastUpdatedate, users, friends,
				userId);
		// add to db
		App.userDAO.insertUsers(users, false);

		// check exist friends first
		if (isNeedNotify) {
			followFriendCountArray = Utils.getFollowFriendCountArray(
					followFriendCountArray, friends);
		}
		// merge insert
		App.friendDAO.insertFriends(friends, false);

		Utils.updateFriendLastUpdatedate();
	}

	private final List<User> users = new ArrayList<User>();
	private final List<Friend> friends = new ArrayList<Friend>();
	private final long userId;
	private final boolean isNeedNotify;
	// newfollowCount + alreadyFollowCount - unfollowCount
	private final int[] followFriendCountArray = new int[3];

	public RetrieveFriendsTask(long userId, boolean isNeedNotify) {
		this.userId = userId;
		this.isNeedNotify = isNeedNotify;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		retrieveFriends(users, friends, followFriendCountArray, userId,
				isNeedNotify);
		return TaskResult.OK;
	}

	public int[] getFollowFriendCountArray() {
		return followFriendCountArray;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[]{users, friends, followFriendCountArray,
				userId, isNeedNotify};
	}
}