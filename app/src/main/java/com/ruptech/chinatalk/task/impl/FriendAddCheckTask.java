package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class FriendAddCheckTask extends GenericTask {
	private final User user = new User();
	private String tel;
	private String lang;
	private final String friend_id;
	public FriendAddCheckTask(String tel, String friend_id){
		this.tel= tel;
		this.friend_id = friend_id;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		String[] array = App.getHttpServer().friendAddCheck(tel, friend_id,
				user);
		tel = array[0];
		lang = array[1];
		return TaskResult.OK;
	}

	public String getLang() {
		return lang;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { tel };
	}

	public String getTel() {
		return tel;
	}

	public User getUser() {
		return user;
	}
}