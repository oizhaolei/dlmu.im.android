package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class TranslatorRankListTask extends GenericTask {

	private List<Map<String, String>> mRankedList;
	private final boolean top;
	private final String lang;
	private final String startNumber;

	public TranslatorRankListTask(boolean top, String lang, String startNumber) {
		super();
		this.top = top;
		this.lang = lang;
		this.startNumber = startNumber;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		mRankedList = App.getHttpServer().retrieveTranslatorList(lang,
				startNumber);

		return TaskResult.OK;
	}

	public List<Map<String, String>> getmRankedList() {
		return mRankedList;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {lang,
				startNumber};
	}

	public boolean isTop() {
		return top;
	}
}