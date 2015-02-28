package com.ruptech.chinatalk.utils;

import java.util.Iterator;

import org.json.JSONObject;

public class BadgeCount {
	private JSONObject newMessageCountJson = new JSONObject();
	public int followCount = 0;
	public int commentCount = 0;
	public int newsCount = 0;
	public int friendCount = 0;
	public int versionCount = 0;

	public int getChatNewCount() {
		return getNewMessageTotalCount() + commentCount + newsCount;
	}

	public int getMySelfNewCount() {
		return friendCount + versionCount;
	}

	public int getPopularNewCount() {
		return followCount;
	}

	public int getNewMessageTotalCount() {
		int totalCount = 0;
		Iterator<String> iter = newMessageCountJson.keys();
		while (iter.hasNext()) {
			String key = iter.next();
			totalCount += newMessageCountJson.optInt(String.valueOf(key));
		}

		return totalCount;
	}

	public void addNewMessageCount(Long friendId) {
		int oldCount = getNewMessageCount(friendId);
		oldCount++;
		try {
			newMessageCountJson.put(String.valueOf(friendId), oldCount);
			saveToPref();
		} catch (Exception e) {

		}
	}

	public void removeNewMessageCount(Long friendId) {
		newMessageCountJson.remove(String.valueOf(friendId));
		saveToPref();
	}

	public int getNewMessageCount(Long friendId) {
		return newMessageCountJson.optInt(String.valueOf(friendId));
	}

	public void loadBadgeCount() {
		newMessageCountJson = PrefUtils.getPrefNewMessageCount();
	}

	private void saveToPref() {
		PrefUtils.savePrefNewMessageCount(newMessageCountJson);
	}

}
