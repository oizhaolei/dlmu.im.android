package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

/**
 * 接收者接收到message后，请求翻译。因为发送者费用不足支付翻译费用了。
 *
 * @author zhaolei
 *
 */
public class StoryTranslateLikeTask extends GenericTask {

	private final long user_photo_id;
	private final long story_translate_id;
	private final String lang;
	private final boolean like;
	private StoryTranslate storyTranslate;

	public StoryTranslateLikeTask(long user_photo_id, String lang,
			long story_translate_id, boolean like) {
		this.user_photo_id = user_photo_id;
		this.lang = lang;
		this.story_translate_id = story_translate_id;
		this.like = like;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		storyTranslate = App.getHttpStoryServer().likeTranslate(user_photo_id,
				story_translate_id, lang, like);
		return TaskResult.OK;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { user_photo_id, story_translate_id, lang, like };
	}
	public StoryTranslate getStoryTranslate() {
		return storyTranslate;
	}
}
