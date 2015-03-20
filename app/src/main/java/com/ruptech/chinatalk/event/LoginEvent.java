package com.ruptech.chinatalk.event;


import com.ruptech.chinatalk.task.TaskResult;

public class LoginEvent {
	public final String msg;
	public final TaskResult result;

	public LoginEvent(String msg, TaskResult result) {
		this.msg = msg;
		this.result = result;
	}
}
