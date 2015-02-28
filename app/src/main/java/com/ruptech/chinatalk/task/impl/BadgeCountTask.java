package com.ruptech.chinatalk.task.impl;

import org.json.JSONObject;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class BadgeCountTask extends GenericTask {

	@Override
	protected TaskResult _doInBackground() throws Exception {
		JSONObject data = App.getHttpServer().retrieveBadgeCount();

		if (data != null) {
			App.mBadgeCount.followCount = data.optInt("follow");
			// badgeCount.chatCount = data.optInt("chat");
			App.mBadgeCount.commentCount = data.optInt("comment");
			App.mBadgeCount.newsCount = data.optInt("news");
			App.mBadgeCount.friendCount = data.optInt("friend");
		}
		return TaskResult.OK;
	}

}
