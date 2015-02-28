package com.ruptech.chinatalk.task.impl;

import java.util.ArrayList;
import java.util.List;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

/**
 * 对tbl_user_photo的翻译
 *
 * @author zhaolei
 *
 */
public class RequestAutoTranslatePhotoTask extends GenericTask {
	private final long id;
	private final String lang;
	private UserPhoto userPhoto;
	private static List<Long> storyAutoRequestTransKeyList;

	public static List<Long> getStoryAutoRequestTransKeyList() {
		if (storyAutoRequestTransKeyList == null) {
			storyAutoRequestTransKeyList = new ArrayList<Long>();
		}
		return storyAutoRequestTransKeyList;
	}

	public RequestAutoTranslatePhotoTask(long id, String lang) {
		this.id = id;
		this.lang = lang;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhoto = App.getHttp2Server().autoTranslatePhoto(id, lang);
		App.userPhotoDAO.updateUserPhoto(userPhoto);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {id, lang};
	}

	public UserPhoto getUserPhoto() {
		return userPhoto;
	}

}
