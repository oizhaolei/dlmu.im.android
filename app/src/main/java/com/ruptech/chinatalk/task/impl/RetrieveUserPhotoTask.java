package com.ruptech.chinatalk.task.impl;

import android.util.Base64;

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
public class RetrieveUserPhotoTask extends GenericTask {
	private long id;
	private final String lang;
	private UserPhoto userPhoto;

	public RetrieveUserPhotoTask(long id, String lang) {
		this.id = id;
		this.lang = lang;
	}

	public RetrieveUserPhotoTask(String encodedId, String lang) {
		try {
			id = Long.parseLong(encodedId);
		} catch (Exception e) {
			byte[] b = Base64.decode(encodedId, 0);
			id = Long.parseLong(new String(b));
		}
		this.lang = lang;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhoto = App.getHttpStoryServer().getUserPhoto(id, lang);

		// 更新本地数据库
		App.userPhotoDAO.updateUserPhoto(userPhoto);

		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { id, lang };
	}

	public UserPhoto getUserPhoto() {
		return userPhoto;
	}

	public long getUserPhotoId() {
		return id;
	}

}
