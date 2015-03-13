package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RetrieveInfoPeriodTask extends GenericTask {

	List<User> userList = new ArrayList<User>();
	List<Friend> friendList = new ArrayList<Friend>();
	List<Message> messageList = new ArrayList<Message>();
	List<Map<String, String>> announcementList = new ArrayList<Map<String, String>>();
	// newfollowCount + alreadyFollowCount - unfollowCount
	private int[] followFriendCountArray = new int[3];

	@Override
	protected TaskResult _doInBackground() throws Exception {
		String messageLastUpdateDate = PrefUtils
				.getPrefMessageLastUpdateDate(App.readUser().getId());
		String friendLastUpdatedate = Utils.getFriendLastUpdatedate();
		String announcementLastUpdateDate = PrefUtils
				.getPrefShowAnnouncementDialogLastUpdateDate();

		App.getHttpServer().getInfoPeriodTimeline(messageLastUpdateDate,
				friendLastUpdatedate, announcementLastUpdateDate, userList,
				friendList, messageList, announcementList);
		followFriendCountArray = Utils.getFollowFriendCountArray(
				followFriendCountArray, friendList);

		App.friendDAO.insertFriends(friendList, false);
		App.userDAO.insertUsers(userList, false);
		for (Message message : messageList) {
			CommonUtilities.messageNotification(App.mContext, message);
		}
		if (messageList.size() > 0) {
			PrefUtils.savePrefMessageLastUpdatedate(App.readUser().getId(),
					null);
		}
		if (friendList.size() > 0) {
			Utils.updateFriendLastUpdatedate();
		}

		return TaskResult.OK;
	}

	public List<Map<String, String>> getAnnouncementList() {
		return announcementList;
	}

	public int[] getFollowFriendCountArray() {
		return followFriendCountArray;
	}

	public List<Friend> getFriendList() {
		return friendList;
	}

	public List<Message> getMessageList() {
		return messageList;
	}

	public List<User> getUserList() {
		return userList;
	}
}