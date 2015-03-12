package com.ruptech.chinatalk.http;

import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
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
			if (prop != null && result.has("tel")) {
				prop[0] = result.optString("tel");
				prop[1] = result.optString("lang");
			}
			if (!"null".equals(result.getString("data"))) {
				JSONObject data0 = (JSONObject) result.getJSONArray("data")
						.get(0);
				User user = new User(data0);
				return user;
			}

		} else {
			throw new ServerSideException(result.getString("msg"));
		}
		return null;
	}

	private void _parseFriends(Response res, List<User> userList,
			List<Friend> friendList) throws Exception {
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONObject data = result.getJSONObject("data");

			// users
			JSONArray user_list = data.getJSONArray("users");
			convertUserList(userList, user_list);
			// friends
			JSONArray friend_list = data.getJSONArray("friends");
			convertFriendList(friendList, friend_list);
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	private List<Message> _parseMessage(Response res) throws Exception {
		JSONObject result = res.asJSONObject();

		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Message> messageList = new ArrayList<Message>(size);
			convertMessageList(messageList, list);
			return messageList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	private User _parseUser(JSONObject result, Response res) throws Exception {
		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new User(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public List<Message> acceptTranslateMessage(long message_id)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("message_id", String.valueOf(message_id));

		Response res = _get("message/message_accept_translate.php", params);

		return _parseMessage(res);
	}

	public void addFriend(String tel, String friendId, String nickname,
			String memo, String lastUpdatedate, String lang,
			List<User> userList, List<Friend> friendList, String isContact)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", friendId);
		params.put("tel", tel);
		params.put("nickname", nickname);
		params.put("memo", memo);
		params.put("lang", lang);
		params.put("is_contact", isContact);
		params.put("update_date", lastUpdatedate);

		Response res = _get("friend/friend_add.php", params);

		_parseFriends(res, userList, friendList);
	}

	public void addQa(String question) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("question", question);
		if (BuildConfig.DEBUG)
			Log.v(TAG, "addQa");

		Response res = _get("help/qa_add.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (!success) {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	public Friend blockFriend(long friend_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", String.valueOf(friend_id));

		Response res = _get("friend/friend_block.php", params);

		JSONObject result = res.asJSONObject();
		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new Friend(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public Friend changeFriendMemo(long friendid, String memo) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friendid", String.valueOf(friendid));
		params.put("memo", memo);
		params.put("func", "change_friend_memo");

		Response res = _get("user/user_change_profile.php", params);
		JSONObject result = res.asJSONObject();
		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new Friend(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public Friend changeFriendNickName(long friendid, String nickName)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friendid", String.valueOf(friendid));
		params.put("nickname", nickName);
		params.put("func", "change_friend_nickname");

		Response res = _get("user/user_change_profile.php", params);
		JSONObject result = res.asJSONObject();
		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new Friend(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public User changePassword(String oldPassword, String password)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("oldpassword", oldPassword);
		params.put("password", password);
		params.put("func", "change_pwd");

		Response res = _get("user/user_change_profile.php", params);
		JSONObject result = res.asJSONObject();
		return _parseUser(result, res);
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
			if (mKey.equals("tel")) {
				params.put("tel", mValue);
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

	private void convertAnnouncementList(
			List<Map<String, String>> announcementList,
			JSONArray announcement_list) throws JSONException {
		int size;
		size = announcement_list.length();

		announcementList.clear();
		for (int i = 0; i < size; i++) {
			JSONObject jo = announcement_list.getJSONObject(i);
			announcementList.add(convertJsonItem(jo));
		}
	}

	// Low-level interface

	private void convertFriendList(List<Friend> friendList,
			JSONArray friend_list) throws JSONException {
		int size;
		size = friend_list.length();

		friendList.clear();
		for (int i = 0; i < size; i++) {
			JSONObject jo = friend_list.getJSONObject(i);

			Friend friend = new Friend(jo);
			friendList.add(friend);
		}
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

	private void convertMessageList(List<Message> messageList,
			JSONArray message_list) throws JSONException {
		int size = message_list.length();

		messageList.clear();
		for (int i = 0; i < size; i++) {
			JSONObject jo = message_list.getJSONObject(i);

			Message message = new Message(jo);
			messageList.add(message);
		}
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

	public boolean deleteUser() throws Exception {
		Map<String, String> params = new HashMap<>();
		Response res = _get("user/user_delete.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			return success;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public int freeRecharge(String type) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("type", type);
		Response res = _get("recharge/recharge_free.php", params);
		JSONObject result = res.asJSONObject();
		if (result.getBoolean("success")) {
			String msg = result.getString("msg");
			if (msg == null || "".equals(msg) || "null".equals(msg)) {
				// 充值成功
				return AppPreferences.FREE_RECHARGE_SUCEESS;
			} else {
				// 时间间隔不够，不需要充值
				return AppPreferences.FREE_RECHARGE_INTERVAL_TIME;
			}
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	public String[] friendAddCheck(String tel, String friend_id, User existUser)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);
		params.put("friend_id", friend_id);
		Response res = _get("friend/friend_add_check.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {

			if (!"null".equals(result.getString("data"))) {
				JSONObject data0 = (JSONObject) result.getJSONArray("data")
						.get(0);
				User user = new User(data0);
				existUser.setId(user.getId());
				existUser.setFullname(user.getFullname());
				existUser.setTel(user.getTel());
				existUser.setLang(user.getLang());
				existUser.setActive(user.getActive());
			}
			tel = result.getString("tel");
			String searchNoFriendInfoUsedLang = result.getString("lang");

			String[] array = new String[] { tel, searchNoFriendInfoUsedLang };
			return array;
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
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

	public Friend friendWalletPriority(Long friendId, int wallet_priority)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", String.valueOf(friendId));
		params.put("wallet_priority", String.valueOf(wallet_priority));
		Response res = _get("friend/friend_wallet_priority.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new Friend(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}

	}

	public Map<String, String> getAnnouncementById(long id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(id));

		Response res = _get("help/announcement.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			Map<String, String> message = convertJsonItem(data0);
			return message;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	@Override
	protected String getAppServerUrl() {
		return App.readServerAppInfo().getAppServerUrl();
	}

	public JSONObject getGuide() throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("language", Utils.getUserLanguage());
		Response res = _get("help/guide.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			return result.getJSONObject("data");
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public void getInfoPeriodTimeline(String message_last_update_date,
			String friend_last_update_date,
			String announcement_last_update_date, List<User> userList,
			List<Friend> friendList, List<Message> messageList,
			List<Map<String, String>> announcementList) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("message_last_update_date", message_last_update_date);
		params.put("friend_last_update_date", friend_last_update_date);
		params.put("announcement_last_update_date",
				announcement_last_update_date);
		Response res = _get("timeline/info_period_timeline.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONObject data = result.getJSONObject("data");
			// users
			JSONArray user_list = data.getJSONArray("users");
			convertUserList(userList, user_list);
			// friends
			JSONArray friend_list = data.getJSONArray("friends");
			convertFriendList(friendList, friend_list);
			// messages
			JSONArray message_list = data.getJSONArray("messages");
			convertMessageList(messageList, message_list);
			// announcement
			JSONArray announcement_list = data.getJSONArray("announcements");
			convertAnnouncementList(announcementList, announcement_list);
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	public Message getMessageById(long message_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(message_id));

		Response res = _get("message/message.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			return new Message(data0);
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public List<Message> getMessageByLocalIds(String localIds) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("localIds", String.valueOf(localIds));

		Response res = _get("message/message.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Message> messageList = new ArrayList<Message>(size);
			convertMessageList(messageList, list);
			return messageList;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public Map<String, String> getQaById(long qa_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(qa_id));

		Response res = _get("help/qa.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
			Map<String, String> message = convertJsonItem(data0);
			return message;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public List<Map<String, String>> getRechargePrice() throws Exception {
		Response res = _get("recharge/recharge_price.php", null);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Map<String, String>> rechargePriceList = new ArrayList<Map<String, String>>(
					size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				Map<String, String> rechargePric = convertJsonItem(jo);
				rechargePriceList.add(rechargePric);
			}
			return rechargePriceList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<User> getRecommendedFriendList(String contacts)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("contacts", contacts);
		if (BuildConfig.DEBUG)
			Log.v(TAG, "params:" + params);

		Response res = _post("friend_recommended_list.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> recommendedUserList = new ArrayList<User>(size);

			for (int i = 0; i < size; i++) {

				JSONObject jo = list.getJSONObject(i);

				User user = new User(jo);
				user.setPoint(AppPreferences.RECOMMENDED_FRIENDS_TO_NEW_USER_FLAG);
				recommendedUserList.add(user);
			}
			return recommendedUserList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public User getUser(long userid) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(userid));

		return _getUser(null, params);
	}

	// Test
	public User getUserByTel(String tel, String[] prop) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);

		return _getUser(prop, params);
	}

	public User getUserSignupCheck(String tel, String[] prop) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);

		Response res = _get("user/user_signup_check.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			prop[0] = String.valueOf(result.getBoolean("result"));
			if (!Utils.isEmpty(result.getString("data"))) {
				JSONObject data0 = (JSONObject) result.getJSONArray("data")
						.get(0);
				User user = new User(data0);
				return user;
			}
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
		return null;
	}

	public Friend removeFriend(long friend_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", String.valueOf(friend_id));

		Response res = _get("friend/friend_delete.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONObject data = result.getJSONObject("data");
			Friend friend = new Friend(data);
			return friend;
		} else {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	public List<Message> requestTranslate(long localId, long toUserId,
			String fromLang, String toLang, String text, int contentLength,
			String filetype, String lastUpdatedate, String filePath)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("local_id", String.valueOf(localId));
		params.put("to_userid", String.valueOf(toUserId));
		params.put("from_lang", fromLang);
		params.put("to_lang", toLang);
		if (text == null)
			text = "";
		params.put("text", text);
		params.put("content_length", String.valueOf(contentLength));
		params.put("filetype", filetype);
		params.put("update_date", lastUpdatedate);
		params.put("file_path", filePath);
		if (BuildConfig.DEBUG)
			Log.v(TAG, "params:" + params);

		Response res = _get("message/message_request_translate.php", params);

		return _parseMessage(res);
	}

	public void requestVerifyMessage(long message_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("message_id", String.valueOf(message_id));

		Response res = _get("message/message_request_verify.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (!success) {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);

		}
	}

	public List<Map<String, String>> retrieveAnnouncementList()
			throws Exception {
		Map<String, String> params = new HashMap<>();

		Response res = _get("help/announcement_list.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Map<String, String>> announcementMapList = new ArrayList<Map<String, String>>(
					size);

			convertAnnouncementList(announcementMapList, list);
			return announcementMapList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public JSONObject retrieveBadgeCount() throws Exception {
		Map<String, String> params = new HashMap<>();
		Response res = _get("utils/badge_count.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			return result.optJSONObject("data");
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
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
			List<User> blockedFriendsList = new ArrayList<User>(size);
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
			List<User> followerUserList = new ArrayList<User>(size);
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

	public List<User> retrieveLbsUsers(int late6, int lnge6) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("late6", String.valueOf(late6));
		params.put("lnge6", String.valueOf(lnge6));

		Response res = _get("timeline/lbs_user_timeline.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> popularUserList = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);
				User user = new User(jo);
				popularUserList.add(user);
			}
			return popularUserList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<Message> retrieveMessageHistory(long friendId, long minMessageId)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("friend_id", String.valueOf(friendId));
		params.put("min_message_id", String.valueOf(minMessageId));

		Response res = _get("message/message_history.php", params);

		return _parseMessage(res);
	}

	public void retrieveNewFriends(String lastUpdatedate, List<User> userList,
			List<Friend> friendList, long userId) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("userid", String.valueOf(userId));
		params.put("update_date", lastUpdatedate);

		Response res = _get("timeline/friend_timeline.php", params);

		_parseFriends(res, userList, friendList);
	}

	public List<Message> retrieveNewMessage(long userId, String updateDate)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("userid", String.valueOf(userId));
		params.put("update_date", updateDate);

		Response res = _get("timeline/message_timeline.php", params);

		return _parseMessage(res);
	}

	public List<User> retrieveOnlineUsers() throws Exception {
		Map<String, String> params = new HashMap<>();
		Response res = _get("timeline/online_user_timeline.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> popularUserList = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);
				User user = new User(jo);
				popularUserList.add(user);
			}
			return popularUserList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<User> retrievePopularUsers() throws Exception {
		Map<String, String> params = new HashMap<>();
		Response res = _get("timeline/popular_user_timeline.php", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<User> popularUserList = new ArrayList<User>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);
				User user = new User(jo);
				popularUserList.add(user);
			}
			return popularUserList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<Map<String, String>> retrieveQAList(int qa_id) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("qa_id", String.valueOf(qa_id));
		Response res = _get("help/qa_list.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Map<String, String>> qaMapList = new ArrayList<Map<String, String>>(
					size);

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				Map<String, String> message = convertJsonItem(jo);
				qaMapList.add(message);
			}
			return qaMapList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<Map<String, String>> retrieveTranslatorList(String lang,
			String start_number) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("lang", lang);
		params.put("start_number", start_number);
		Response res = _get("help/translator_rank_list.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Map<String, String>> rankedMapList = new ArrayList<Map<String, String>>(
					size);

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				Map<String, String> message = convertJsonItem(jo);
				rankedMapList.add(message);
			}
			return rankedMapList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public List<Map<String, String>> retrieveVerifyMessage() throws Exception {
		Map<String, String> params = new HashMap<>();

		Response res = _get("user/user_verify_message_list.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			JSONArray list = result.getJSONArray("data");
			int size = list.length();
			List<Map<String, String>> messageMapList = new ArrayList<Map<String, String>>(
					size);

			for (int i = 0; i < size; i++) {
				JSONObject jo = list.getJSONObject(i);

				Map<String, String> message = convertJsonItem(jo);
				messageMapList.add(message);
			}
			return messageMapList;
		}
		String msg = result.getString("msg");
		throw new ServerSideException(msg);
	}

	public boolean sendClientMessage(String msg) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("message", msg);
		if (App.readUser() != null) {
			params.put("tel", App.readUser().getTel());
		}
		Response res = _get("utils/logging_client_message.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");

		return success;
	}

	public void sendUserPasswordSms(String tel) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);

		Response res = _get("user/send_sms_forget_password.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (!success) {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);

		}
	}

	public void uploadUserLocation(int late6, int lnge6) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("late6", String.valueOf(late6));
		params.put("lnge6", String.valueOf(lnge6));

		Response res = _get("user/user_upload_location.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (!success) {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
	}

	/**
	 *
	 * @param username
	 * @param password
     * @param encrypt
	 * @return
	 * @throws Exception
	 */

	public User userLogin(String username, String password, boolean encrypt)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("username", username);
		params.put("password", password);
		params.put("encrypt", String.valueOf(encrypt));
        params.put("serial", String.valueOf(android.os.Build.SERIAL));

		Response res = _get("user/user_login.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
			JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);

			User user = new User(data0);
			// IMPORTANT:get server utc time as start time
			if (PrefUtils.getPrefMessageLastUpdateDate(user.getId()) == null) {
				PrefUtils.savePrefMessageLastUpdatedate(user.getId(),
						data0.getString("server_utc_timestamp"));
			}

			return user;
		} else {
			throw new ServerSideException(result.getString("msg"));
		}
	}

	public User userSignup(String tel, String password, String fullname,
			String file_path, String gender, String lang) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);
		params.put("password", password);
		params.put("fullname", fullname);
		params.put("file_path", file_path);
		params.put("gender", gender);
		params.put("lang", lang);
        params.put("serial", String.valueOf(android.os.Build.SERIAL));

		Response res = _get("user/user_signup.php", params);
		JSONObject result = res.asJSONObject();

		if (result.getBoolean("success")) {
		}
		return _parseUser(result, res);
	}

	public boolean verifyCodeVerify(String tel, String verify_code)
			throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("tel", tel);
		params.put("verify_code", verify_code);

		Response res = _get("user/sms_verify_code_verify.php", params);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (!success) {
			String msg = result.getString("msg");
			throw new ServerSideException(msg);
		}
		return success;
	}

    public Message xmpp_requestTranslate(Chat chat)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("local_id", String.valueOf(System.currentTimeMillis()));
        params.put("to_userid", String.valueOf(0));
        params.put("from_lang", chat.getFromLang());
        params.put("to_lang", chat.getToLang());
        if (chat.getMessage() == null)
            chat.setMessage("");
        params.put("text", chat.getMessage());
        params.put("content_length", String.valueOf(chat.getFromContentLength()));
        params.put("filetype", chat.getType());
        params.put("update_date", DateCommonUtils.getUtcDate(new Date(),
                DateCommonUtils.DF_yyyyMMddHHmmssSSS));
        params.put("file_path", chat.getFilePath());
        params.put("callback_id",String.valueOf(chat.getId()));
        if (BuildConfig.DEBUG)
            Log.v(TAG, "params:" + params);

        Response res = _get("message/xmpp_translate.php", params);
        JSONObject result = res.asJSONObject();

        boolean success = result.getBoolean("success");
        if (success) {
            JSONObject jo = result.getJSONObject("data");
            Message message = new Message(jo);
            return message;
        }
        String msg = result.getString("msg");
        throw new ServerSideException(msg);

    }
}
