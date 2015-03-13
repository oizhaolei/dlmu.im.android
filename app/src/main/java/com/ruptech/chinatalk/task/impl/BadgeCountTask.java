package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import org.json.JSONObject;

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
