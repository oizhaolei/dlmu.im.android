package com.ruptech.chinatalk.utils;

import org.json.JSONObject;

import java.util.Iterator;

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

	public void addNewMessageCount(String fromJID) {
		int oldCount = getNewMessageCount(fromJID);
		oldCount++;
		try {
			newMessageCountJson.put(fromJID, oldCount);
			saveToPref();
		} catch (Exception e) {

		}
	}

    public void addNewMessageCount(Long friendID) {
        addNewMessageCount(Utils.getOF_JIDFromTTTalkId(friendID));
    }

	public void removeNewChatCount(String fromJID) {
		newMessageCountJson.remove(fromJID);
		saveToPref();
	}

    public void removeNewMessageCount(Long friendID) {
        removeNewChatCount(Utils.getOF_JIDFromTTTalkId(friendID));
    }

	public int getNewMessageCount(String fromJID) {
		return newMessageCountJson.optInt(fromJID);
	}

    public int getNewMessageCount(Long friendID) {
        return getNewMessageCount(Utils.getOF_JIDFromTTTalkId(friendID));
    }

	public void loadBadgeCount() {
		newMessageCountJson = PrefUtils.getPrefNewMessageCount();
	}

	private void saveToPref() {
		PrefUtils.savePrefNewMessageCount(newMessageCountJson);
	}

}
