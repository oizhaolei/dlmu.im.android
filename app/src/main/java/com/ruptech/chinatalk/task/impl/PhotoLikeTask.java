package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

/**
 * 接收者接收到message后，请求翻译。因为发送者费用不足支付翻译费用了。
 *
 * @author zhaolei
 *
 */
public class PhotoLikeTask extends GenericTask {

	private final long id;
	private final boolean like;
	private UserPhoto userPhoto;

	public PhotoLikeTask(long id, boolean like) {
		this.id = id;
		this.like = like;
	}
	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhoto = App.getHttpStoryServer().likePhoto(id, like);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {id, like};
	}

	public UserPhoto getUserPhoto() {
		return userPhoto;
	}
}
