package com.ruptech.chinatalk.task.impl;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.AppPreferences;

import java.io.File;
import java.io.Serializable;

public class FileUploadTask extends GenericTask {
	public static class FileUploadInfo implements Serializable {
		private static final long serialVersionUID = -8015410963965453887L;
		public String fileName;
		public int width = 0;
		public int height = 0;
	}

	private FileUploadInfo fileInfo;
	private final UploadProgress uploadProgress;

	private final File uploadFile;

	private final String fileType;

	public FileUploadTask(File uploadFile, String fileType,
			UploadProgress listener) {
		this.uploadFile = uploadFile;
		this.fileType = fileType;
		this.uploadProgress = listener;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		Log.v(TAG, fileType);
		Log.v(TAG, uploadFile.getPath());

		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(fileType)) {
			fileInfo = App.getHttpStoryServer().uploadFile(uploadFile,
					uploadProgress);
		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
				.equals(fileType)) {
			fileInfo = App.getHttpStoryServer().uploadSound(uploadFile,
					uploadProgress);
		}
		return TaskResult.OK;
	}

	public FileUploadInfo getFileInfo() {
		return fileInfo;
	}
}