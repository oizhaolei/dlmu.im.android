package com.ruptech.chinatalk.task.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class DiscoverTask extends GenericTask {

	public static final String TYPE_KEYWORD = "keyword";
	public static final String TYPE_USER = "user";
	public static final String TYPE_CHANNEL = "channel";
	public static final String TYPE_NONE = null;

	private List<User> mUserList;
	private List<Channel> mChannelList;
	private List<String> mKeywordList;
	private long mUserCount;
	private long mChannelCount;
	private final boolean top;
	private final long maxId;
	private final long sinceId;
	private final String name;
	private final String type;

	public DiscoverTask(boolean top, String type, String name, long maxId,
			long sinceId) {
		super();
		this.top = top;
		this.maxId = maxId;
		this.type = type;
		this.sinceId = sinceId;
		this.name = name;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		JSONObject result = App.getHttpStoryServer().discover(type, name,
				maxId, sinceId);

		mUserCount = result.optLong("user_count");
		mChannelCount = result.optLong("channel_count");
		mUserList = new ArrayList<User>();

		JSONArray list = result.optJSONArray("user");
		if (list != null) {
			int size = list.length();

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				User user = new User(jo);
				mUserList.add(user);
			}
		}
		mChannelList = new ArrayList<Channel>();

		list = result.optJSONArray("channel");
		if (list != null) {
			int size = list.length();

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				Channel channel = new Channel(jo);
				mChannelList.add(channel);
			}
		}

		mKeywordList = new ArrayList<String>();

		list = result.optJSONArray("keyword");
		if (list != null) {
			int size = list.length();

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				String name = jo.optString("name");
				mKeywordList.add(name);
			}
		}

		return TaskResult.OK;
	}

	public long getChannelCount() {
		return mChannelCount;
	}

	public List<Channel> getChannelList() {
		return mChannelList;
	}

	public List<String> getKeywordList() {
		return mKeywordList;
	}

	public String getName() {
		return name;
	}

	public long getUserCount() {
		return mUserCount;
	}

	public List<User> getUserList() {
		return mUserList;
	}

	public boolean isTop() {
		return top;
	}

}