package com.ruptech.chinatalk.http;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpServer extends HttpConnection {
	private final String TAG = Utils.CATEGORY
			+ HttpServer.class.getSimpleName();

	private User _getUser(String[] prop, Map<String, String> params)
			throws Exception {
		Response res = _get("user/user.php", params);
		JSONObject result = res.asJSONObject();
		if (result.getBoolean("success")) {
			if (prop != null && result.has("username")) {
				prop[0] = result.optString("username");
				prop[1] = result.optString("lang");
			}
			if (!"null".equals(result.getString("data"))) {
				JSONObject data0 = (JSONObject) result.getJSONArray("data")
						.get(0);
				return new User(data0);
			}

		} else {
			throw new ServerSideException(result.getString("msg"));
		}
		return null;
	}


	private User _parseUser(JSONObject result, Response res) throws Exception {
		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new User(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}


	public User changeUserProfile(String mFunc, String mKey, String mValue)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("func", mFunc);
		params.put("userid", String.valueOf(App.readUser().getId()));
		if ("change_prop".equals(mFunc)) {
			params.put("key", mKey);
			params.put("value", mValue);
		} else {
			if (mKey.equals("username")) {
				params.put("username", mValue);
			} else if (mKey.equals("fullname")) {
				params.put("fullname", mValue);
			} else if (mKey.equals("lang")) {
				params.put("lang", mValue);
			} else if (mKey.equals("gender")) {
				params.put("gender", mValue);
			} else if (mKey.equals("photo_name")) {
				params.put("photo_name", mValue);
			} else if (mKey.equals("user_memo")) {
				params.put("user_memo", mValue);
			}
		}

		Response res = _get("user/user_change_profile.php", params);
		JSONObject result = res.asJSONObject();
		return _parseUser(result, res);
	}


	private Map<String, String> convertJsonItem(JSONObject jo)
			throws JSONException {
		Map<String, String> map = new HashMap<>();
		Iterator<String> keys = jo.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			map.put(key, jo.getString(key));
		}

		return map;
	}

	private void convertUserList(List<User> userList, JSONArray user_list)
			throws JSONException {
		int size = user_list.length();

		userList.clear();
		for (int i = 0; i < size; i++) {
			JSONObject jo = user_list.getJSONObject(i);

			User friend = new User(jo);
			userList.add(friend);
		}
	}


	public boolean friendReport(Long friendId, String report_content)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", String.valueOf(friendId));
		params.put("report_content", report_content);
		Response res = _get("friend/friend_report.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			return success;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}

	}

	@Override
	protected String getAppServerUrl() {
		return App.properties.getProperty("SERVER_BASE_URL");
	}

	public User getUser(long userid) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(userid));

		return _getUser(null, params);
	}


	public List<User> retrieveBlockedFriends(long sinceId, long[] sinceIdArray)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("since_id", String.valueOf(sinceId));

		Response res = _get("friend/retrieve_blocked_friends.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> blockedFriendsList = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);
				User user = new User(jo);
				blockedFriendsList.add(user);
			}
			if (size > 0) {
				sinceIdArray[0] = list.getJSONObject(size - 1).getLong("fid");
			}
			return blockedFriendsList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<User> retrieveFollowerUsers(long sinceId, long[] sinceIdArray)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("since_id", String.valueOf(sinceId));

		Response res = _get("timeline/follower_friend_timeline.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> followerUserList = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);
				User user = new User(jo);
				followerUserList.add(user);
			}
			if (size > 0) {
				sinceIdArray[0] = list.getJSONObject(size - 1).getLong("fid");
			}
			return followerUserList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	/**
	 * @throws Exception
	 */

	public List<String> sendGroupMessage(String fromJid, String toGroupJid, String subject, String body)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("from_jid", fromJid);
		params.put("to_group", toGroupJid);
		params.put("subject", subject);
		params.put("body", body);

		Response res = _get("send", params);
		JSONArray result = res.asJSONArray();
		List<String> sends= new ArrayList<>(result.length());
		for (int i = 0; i < result.length(); i++) {
			String memberId = result.getString(i);
			sends.add(memberId);
		}
		return sends;
	}

	/**
	 * @throws Exception
	 */

	public User login(String username, String password, boolean encrypt)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("username", username);
		params.put("password", password);
		params.put("encrypt", String.valueOf(encrypt));
		params.put("serial", String.valueOf(android.os.Build.SERIAL));

		Response res = _get("login", params);
		JSONObject result = res.asJSONObject();


			return new User(result);
	}

}
