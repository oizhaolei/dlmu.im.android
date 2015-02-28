package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class DeleteUserTask extends GenericTask {
	@Override
	protected TaskResult _doInBackground() throws Exception {
		App.getHttpServer().deleteUser();

		return TaskResult.OK;
	}
}