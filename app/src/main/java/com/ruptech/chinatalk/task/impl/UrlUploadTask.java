package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;

public class UrlUploadTask extends GenericTask {

	private FileUploadInfo fileInfo;
	private final String url;

	public UrlUploadTask(String url) {
		this.url = url;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		fileInfo = App.getHttp2Server().uploadUrl(url);
		return TaskResult.OK;
	}

	public FileUploadInfo getFileInfo() {
		return fileInfo;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {url};
	}
}