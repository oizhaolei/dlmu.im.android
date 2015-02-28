package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class UserStoryNewTask extends GenericTask {

	private UserPhoto userPhoto;
	private String lang="";
	private String pic_url="";
	private int width = 0;
	private int height = 0;
	private String content="";
	private long parent_id = 0;
	private long reply_id = 0;
	private int late6 = 0;
	private int lnge6 = 0;
	private String address="";
	private String category="";
	public UserStoryNewTask(String lang,String pic_url, int width, int height, String content,long parent_id,long reply_id, int late6, int lnge6, String address, String category){
		this.lang = lang;
		this.pic_url =pic_url;
		this.width = width;
		this.height = height;
		this.content = content;
		this.parent_id  = parent_id;
		this.reply_id = reply_id;
		this.late6 = late6;
		this.lnge6 = lnge6;
		this.address = address;
		this.category = category;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		userPhoto = App.getHttpStoryServer().postNewStory(parent_id, pic_url,
				content, late6, lnge6, category, address, reply_id, width,
				height, lang);
		if (parent_id == 0) {
			App.getHttpServer().changeUserProfile("change_prop", "album_count",
					String.valueOf(App.readUser().getAlbum_count() + 1));
			App.readUser().setAlbum_count(App.readUser().getAlbum_count() + 1);
		}
		return TaskResult.OK;

	}

	@Override
	public Object[] getMsgs() {
		return new Object[] { parent_id, pic_url, content, late6, lnge6,
				category, address, reply_id, width, height, lang };
	}

	public UserPhoto getUserPhoto() {
		return userPhoto;
	}

}