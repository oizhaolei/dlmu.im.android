package com.ruptech.chinatalk.task.impl;

import java.util.List;
import java.util.Map;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RechargePriceTask extends GenericTask {
	private String msg;
	public List<Map<String, String>> rechargePriceList;

	@Override
	protected TaskResult _doInBackground() throws Exception {
		rechargePriceList = App.getHttpServer().getRechargePrice();

		return TaskResult.OK;
	}

	@Override
	public String getMsg() {
		return msg;
	}

	public List<Map<String, String>> getRechargePriceList() {
		return rechargePriceList;
	}
}
