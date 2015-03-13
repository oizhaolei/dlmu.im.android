package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

public class RecommendedFriendTask extends GenericTask {

	private static List<User> recommendedFriendsUserList;

	@Override
	protected TaskResult _doInBackground() throws Exception {

		List<String> contacts = Utils.getMobileContactList("");
		if (contacts != null && contacts.size() > 0) {
			StringBuffer sb = new StringBuffer(contacts.size() * 20);
			for (String c : contacts) {
				sb.append(c).append(",");
			}
			if (contacts.size() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}

			if (!Utils.isEmpty(sb.toString())) {
				recommendedFriendsUserList = App.getHttpServer()
						.getRecommendedFriendList(sb.toString());
				PrefUtils
						.writeRecommendedFriendUser(recommendedFriendsUserList);
				PrefUtils.savePrefRecommendedFriendsLastUpdate(System
						.currentTimeMillis());
			}

		}
		return TaskResult.OK;
	}
}