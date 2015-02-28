package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class GiftDonateTask extends GenericTask {
	long to_user_id;
	long present_id;
	long photo_id;
	int quantity;

	public GiftDonateTask(long to_user_id, long present_id, long photo_id,
			int quantity) {
		this.to_user_id = to_user_id;
		this.present_id = present_id;
		this.photo_id = photo_id;
		this.quantity = quantity;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		boolean result = App.getHttpStoryServer().giftDonate(to_user_id,
				present_id, photo_id, quantity);

		if (result) {
			return TaskResult.OK;
		} else {
			return TaskResult.FAILED;
		}
	}
}