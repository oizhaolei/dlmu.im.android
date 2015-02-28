package com.ruptech.chinatalk.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.thirdparty.model.Share;

public class PrefUtils {

	private static final String TAG = PrefUtils.class.getSimpleName();

	final public static String PREF_TOP_ALREADY_REMINDER_WINDOW = "pref_top_already_reminder_window";

	final public static String PREF_USERINFO = "USER_INFO";

	final public static String PREF_SERVERAPP_INFO = "SERVER_APP_INFO";

	final public static String PREF_USERINFO_PASS = "USER_INFO_PASSWORD";

	final public static String PREF_NOTIFICATION_EXTRAS = "NOTIFICATION_EXTRAS";

	final public static String PREF_MESSAGE_LAST_UPDATE = "pref_message_last_update";

	final public static String PREF_NEW_MESSAGE_COUNT = "pref_new_message_count";

	final public static String PREF_POPULAR_TYPE = "pref_popular_type";

	final public static String PREF_TTT_LAST_SELECTED_LANG = "pref_ttt_old_select_lang";

	final public static String PREF_GUIDE_LAST_UPDATE = "pref_guide_last_update";

	// 朋友推荐
	final public static String PREF_RECOMMENDED_FRIENDS = "PREF_RECOMMENDED_FRIEND_LIST";

	final public static String PREF_RECOMMENDED_FRIENDS_LAST_UPDATE = "PREF_RECOMMENDED_UPLOAD_FRIENDS_LAST_UPDATE";

	final public static String PREF_POPULAR_LAST_UPDATE = "pref_popular_last_update";

	final public static String PREF_MESSAGE_LIST = "pref_message_list";

	public static final String PREF_SHARED_PREF_NAME = "com.ruptech.chinatalk.share";

	final public static String PREF_USER_STORY_PHOTO_TAG = "USER_STORY_PHOTO_TAG";

	final public static String PREF_USER_STORY_CHANNEL_LIST = "USER_STORY_CHANNEL_LIST";

	// dialog显示上次announce时间
	final public static String PREF_SHOW_ANNOUNCEMENT_DIALOG_LAST_UPDATE = "pref_show_announcement_dialog_last_update";

	final public static String PREF_THIRD_PARTY_SHARE = "THIRD_PARTY_SHARE";

	final public static String PREF_OPEN_QA = "OPEN_QA";

	final public static String PREF_NETWORK_IMAGE = "NETWORK_IMAGE";

	final public static String PREF_TRANSLATED_NOTICE_TTS = "pref_translated_notice_tts";

	final public static String PREF_TRANSLATED_NOTICE_REPLY = "pref_translated_notice_reply";

	final public static String PREF_TRANSLATED_NOTICE_LIKE = "pref_translated_notice_like";

	final public static String PREF_TRANSLATED_NOTICE_COMMENT = "pref_translated_notice_comment";

	final public static String PREF_TRANSLATED_NOTICE_FRIEND = "pref_translated_notice_friend";

	final public static String PREF_TRANSLATED_NOTICE_MESSAGE = "pref_translated_notice_message";

	final public static String PREF_TRANSLATED_NOTICE_TRANSLATE = "pref_translated_notice_translate";

	final public static String PREF_TRANSLATED_NOTICE_INTERRUPT_SWITCH = "pref_translated_notice_interrupt_switch";

	final public static String PREF_TRANSLATED_NOTICE_INTERRUPT_START_HOUR = "pref_translated_notice_interrupt_start_hour";

	final public static String PREF_TRANSLATED_NOTICE_INTERRUPT_DURATION = "pref_translated_notice_interrupt_start_duration";

	final public static String PREF_VERSION_CHECKED_TIME = "PREF_VERSION_CHECKED_TIME";

	final public static String PREF_LAST_APK_VERSION = "pref_last_apk_version";

	// 第一次注册登录，提示系统免费赠送1000PT
	final public static String PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM = "SHOW_SYS_FREE_RECHARGE_POINT_INFORM";

	public static final String PREF_PREFER_LANG = "PREF_PREFER_LANG";
	public static final String PREF_EXCEPTION = "PREF_EXCEPTION";

	// 贴图翻译tips
	final public static String PREF_STORY_TRANSLATION_SHOW_TIPS_DIALOG = "pref_story_translation_show_tips_dialog";

	// 贴图默认是“朋友圈”，取不到数据时候切换到“精选”图片
	final public static String PREF_DEFAULT_FRIEND_CHANGE_TO_CHOSEN_MENU = "PREF_DEFAULT_FRIEND_CHANGE_TO_CHOSEN_MENU";

	final public static String PREF_NEW_USER_FANS_COUNT = "PREF_NEW_USER_FANS_COUNT";

	private static SharedPreferences mPref;

	final public static String PREF_INIT_DELETED_HOT_PHOTOS = "PREF_INIT_DELETED_HOT_PHOTOS";

	public static boolean existsMessage(Message message) {
		String[] messages = getPrefMessages();
		if (messages != null && messages.length > 0) {
			List<String> messageList = Arrays.asList(messages);
			return messageList.contains(String.valueOf(message.getMessageid()));
		} else {
			return false;
		}
	}

	public static boolean existsPrefUserInfo() {
		return getmPref().contains(PREF_USERINFO);
	}

	private static SharedPreferences getmPref() {
		if (mPref == null) {
			mPref = PreferenceManager.getDefaultSharedPreferences(App.mContext);
		}
		return mPref;
	}

	private static String getNewMessageCountKey() {
		String key;
		if (App.readUser() != null) {
			key = PREF_NEW_MESSAGE_COUNT + "_" + App.readUser().getId();
		} else {
			key = PREF_NEW_MESSAGE_COUNT + "_";
		}
		return key;
	}

	public static String getNotificationExtrasStr() {
		String str = getmPref().getString(PREF_NOTIFICATION_EXTRAS, "");
		return str;
	}

	public static boolean getPrefDefaultFriendChangeToChosenMenu() {
		boolean isChecked = getmPref().getBoolean(
				PREF_DEFAULT_FRIEND_CHANGE_TO_CHOSEN_MENU, true);
		return isChecked;
	}

	public static long getPrefGuideLastUpdate() {
		long lastUpdatedate = getmPref().getLong(PREF_GUIDE_LAST_UPDATE, 0);
		return lastUpdatedate;
	}

	public static boolean getPrefInitDeletedHotPhotos() {
		boolean isInitDelete = getmPref().getBoolean(
				PREF_INIT_DELETED_HOT_PHOTOS, false);
		return isInitDelete;
	}

	public static long getPrefLastApkVersion() {
		long lastApkVersion = getmPref().getLong(PREF_LAST_APK_VERSION, 0);
		return lastApkVersion;
	}

	public static String getPrefMessageLastUpdateDate(long userId) {
		String lastUpdate = getmPref().getString(
				userId + PREF_MESSAGE_LAST_UPDATE, null);
		return lastUpdate;
	}

	public static String[] getPrefMessages() {
		String messageStr = getmPref().getString(PREF_MESSAGE_LIST, "");
		String[] messageList = messageStr.split(",");
		return messageList;
	}

	public static JSONObject getPrefNewMessageCount() {

		JSONObject jsonObject = new JSONObject();
		try {
			String jsonString = getmPref().getString(getNewMessageCountKey(),
					null);
			jsonObject = new JSONObject(jsonString);
		} catch (Exception e) {
			removePrefNewMessageCount();
		}
		return jsonObject;
	}

	public static String getPrefOpenedQAStr() {
		String str = getmPref().getString(PREF_OPEN_QA, "");
		return str;
	}

	public static String getPrefPerferLang() {
		String str = getmPref().getString(PREF_PREFER_LANG,
				App.readUser().getLang());
		return str;
	}

	public static String getPrefException() {
		String str = getmPref().getString(PREF_EXCEPTION, null);
		return str;
	}

	public static long getPrefPopularLastUpdate() {
		long lastUpdatedate = getmPref().getLong(PREF_POPULAR_LAST_UPDATE, 0);
		return lastUpdatedate;
	}

	public static int getPrefPopularType() {
		int type = getmPref().getInt(PREF_POPULAR_TYPE, 0);
		return type;
	}

	public static long getPrefRecommendedFriendsLastUpdate() {
		long lastUpdatedate = getmPref().getLong(
				PREF_RECOMMENDED_FRIENDS_LAST_UPDATE, 0);
		return lastUpdatedate;
	}

	public static String getPrefShowAnnouncementDialogLastUpdateDate() {
		String lastUpdate = getmPref().getString(
				PREF_SHOW_ANNOUNCEMENT_DIALOG_LAST_UPDATE, null);
		return lastUpdate;
	}

	public static int getPrefStoryTranslationShowTipsDialog() {
		int flag = getmPref()
				.getInt(PREF_STORY_TRANSLATION_SHOW_TIPS_DIALOG, 0);
		return flag;
	}

	public static String getPrefThirdPartyType() {
		String third_party_type = getmPref().getString(
				AppPreferences.THIRD_PARTY_TYPE, "");
		return third_party_type;
	}

	public static boolean getPrefTranslatedNoticeComment() {
		boolean isChecked = getmPref().getBoolean(
				PREF_TRANSLATED_NOTICE_COMMENT, true);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeFriend() {
		boolean isChecked = getmPref().getBoolean(
				PREF_TRANSLATED_NOTICE_FRIEND, true);
		return isChecked;
	}

	public static int getPrefTranslatedNoticeInterruptDuration() {
		int duration = getmPref().getInt(
				PREF_TRANSLATED_NOTICE_INTERRUPT_DURATION, 0);
		return duration;
	}

	public static int getPrefTranslatedNoticeInterruptStartHour() {
		int start = getmPref().getInt(
				PREF_TRANSLATED_NOTICE_INTERRUPT_START_HOUR, 0);
		return start;
	}

	public static boolean getPrefTranslatedNoticeInterruptSwitch() {
		boolean isChecked = getmPref().getBoolean(
				PREF_TRANSLATED_NOTICE_INTERRUPT_SWITCH, false);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeLike() {
		boolean isChecked = getmPref().getBoolean(PREF_TRANSLATED_NOTICE_LIKE,
				true);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeMessage() {
		boolean isChecked = getmPref().getBoolean(
				PREF_TRANSLATED_NOTICE_MESSAGE, true);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeReply() {
		boolean isChecked = getmPref().getBoolean(PREF_TRANSLATED_NOTICE_REPLY,
				true);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeTranslate() {
		boolean isChecked = getmPref().getBoolean(
				PREF_TRANSLATED_NOTICE_TRANSLATE, true);
		return isChecked;
	}

	public static boolean getPrefTranslatedNoticeTts() {
		boolean isChecked = getmPref().getBoolean(PREF_TRANSLATED_NOTICE_TTS,
				false);
		return isChecked;
	}

	public static String getPrefTTTLastSelectedLang(int number) {
		String tttOldSelectLang = getmPref().getString(
				PREF_TTT_LAST_SELECTED_LANG + "_" + number, "");
		return tttOldSelectLang;
	}

	public static int getPrefUserNewFansCount(Long userId) {
		int fansCount = getmPref().getInt(
				PREF_NEW_USER_FANS_COUNT + "_" + userId, 0);
		return fansCount;
	}

	public static long getPrefVersionCheckedDate() {
		long lastUpdatedate = getmPref().getLong(PREF_VERSION_CHECKED_TIME, 0);
		return lastUpdatedate;
	}

	public static String getUserChannelList() {
		String str = getmPref().getString(PREF_USER_STORY_CHANNEL_LIST, "");
		return str;
	}

	public static String getUserStoryPhotoTagCode() {
		String tag = getmPref().getString(PREF_USER_STORY_PHOTO_TAG, "");
		return tag;
	}

	public static boolean hasPrefThirdPartyAccess() {
		String third_party_type = getPrefThirdPartyType();
		String access = getmPref().getString(
				AppPreferences.THIRD_PARTY_ACCESS_TOKEN, "");
		if (AppPreferences.THIRD_PARTY_TYPE_GOOGLE.equals(third_party_type)) {
			return true;
		} else {
			return !Utils.isEmpty(third_party_type) && !Utils.isEmpty(access);
		}
	}

	public static boolean isShowSystemFreeRechargePointInform(Long userId) {
		int result = getmPref().getInt(
				PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + userId, -1);
		return result == 1 ? true : false;
	}

	public static boolean isShowSystemFreeRechargePointInform(String tel) {
		String result = getmPref().getString(
				PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + tel, "");
		return Utils.isEmpty(result) ? false : true;
	}

	public static boolean isThirdPartyShareInfoEmpty() {
		if (readThirdParyShare() == null) {
			return true;
		} else {
			return readThirdParyShare().isEmpty();
		}
	}

	public static Map<String, Object> readNotificationExtras() {
		return (Map<String, Object>) readObject(PREF_NOTIFICATION_EXTRAS);
	}

	private static Object readObject(String key) {
		String str = readStr(key);

		byte[] bytes = str.getBytes();
		if (bytes.length == 0) {
			return null;
		}
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
			Base64InputStream base64InputStream = new Base64InputStream(
					byteArray, Base64.DEFAULT);
			ObjectInputStream in = new ObjectInputStream(base64InputStream);
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, String>> readOpenedQA() {
		return (List<Map<String, String>>) readObject(PREF_OPEN_QA);
	}

	@SuppressWarnings("unchecked")
	public static List<User> readRecommendedFriendUserList() {
		return (List<User>) readObject(PREF_RECOMMENDED_FRIENDS);
	}

	public static ServerAppInfo readServerAppInfo() {
		return (ServerAppInfo) readObject(PREF_SERVERAPP_INFO);
	}

	private static String readStr(String key) {
		String str = getmPref().getString(key, "");
		return str;
	}

	@SuppressWarnings("unchecked")
	public static List<Share> readThirdParyShare() {
		return (List<Share>) readObject(PREF_THIRD_PARTY_SHARE);
	}

	public static User readUser() {
		return (User) readObject(PREF_USERINFO);
	}

	private static void remove(String key) {
		getmPref().edit().remove(key).commit();
	}

	public static void removeGuide() {
		remove(PREF_THIRD_PARTY_SHARE);
		remove(PREF_OPEN_QA);
	}

	public static void removeNotificationExtras() {
		remove(PREF_NOTIFICATION_EXTRAS);
	}

	public static void removePrefDefaultFriendChangeToChosenMenu() {
		remove(PREF_DEFAULT_FRIEND_CHANGE_TO_CHOSEN_MENU);
	}

	public static void removePrefNewMessageCount() {
		remove(getNewMessageCountKey());
	}

	public static void removePrefPopularType() {
		App.userPhotoDAO.deleteAll();
		remove(PREF_POPULAR_TYPE);
	}

	public static void removePrefPreferLang() {
		remove(PREF_PREFER_LANG);
	}

	public static void removePrefException() {
		remove(PREF_EXCEPTION);
	}

	public static void removePrefRecommendedFriendsLastUpdate() {
		remove(PREF_RECOMMENDED_FRIENDS_LAST_UPDATE);
	}

	public static void removePrefShowAnnouncementDialogLastUpdateDate() {
		remove(PREF_SHOW_ANNOUNCEMENT_DIALOG_LAST_UPDATE);
	}

	public static void removePrefThirdPartyAccess() {
		remove(AppPreferences.THIRD_PARTY_ACCESS_TOKEN);
		remove(AppPreferences.THIRD_PARTY_TYPE);
		remove(AppPreferences.THIRD_PARTY_USER_ID);
	}

	public static void removePrefTTTLastSelectedLangs() {
		remove(PREF_TTT_LAST_SELECTED_LANG + "_1");
		remove(PREF_TTT_LAST_SELECTED_LANG + "_2");
	}

	public static void removePrefUser() {
		remove(PREF_USERINFO);
	}

	public static void removePrefUserNewFansCount(Long userId) {
		remove(PREF_NEW_USER_FANS_COUNT + "_" + userId);
	}

	public static void removePreRecommendedFriendById(Long userId) {
		List<User> recommendedFriendUserList = readRecommendedFriendUserList();
		if (null != recommendedFriendUserList) {
			for (int i = 0; i < recommendedFriendUserList.size(); i++) {
				User user = recommendedFriendUserList.get(i);
				if (userId == user.getId()) {
					recommendedFriendUserList.remove(i);
					writeRecommendedFriendUser(recommendedFriendUserList);
					break;
				}
			}
		}
	}

	public static void removePreRecommendedFriendByTel(String tel) {
		List<User> recommendedFriendUserList = readRecommendedFriendUserList();
		if (null != recommendedFriendUserList) {
			for (int i = 0; i < recommendedFriendUserList.size(); i++) {
				User user = recommendedFriendUserList.get(i);
				if (tel.equals(user.getTel())) {
					recommendedFriendUserList.remove(i);
					writeRecommendedFriendUser(recommendedFriendUserList);
					break;
				}

			}
		}
	}

	public static void removeShowSystemFreeRechargePointInform(Long userId) {
		getmPref()
				.edit()
				.putInt(PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + userId,
						0).commit();

	}

	public static void removeShowSystemFreeRechargePointInform(String tel) {
		getmPref().edit()
				.remove(PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + tel)
				.commit();
	}

	public static void removeUserStoryPhotoTagCode() {
		remove(PREF_USER_STORY_PHOTO_TAG);
	}

	public static void savePrefDefaultFriendChangeToChosenMenu(boolean isDefault) {
		getmPref()
				.edit()
				.putBoolean(PREF_DEFAULT_FRIEND_CHANGE_TO_CHOSEN_MENU,
						isDefault).commit();
	}

	public static void savePrefGuideLastUpdate(long lastTime) {
		getmPref().edit().putLong(PREF_GUIDE_LAST_UPDATE, lastTime).commit();
	}

	public static void savePrefInitDeletedHotPhotos() {
		getmPref().edit().putBoolean(PREF_INIT_DELETED_HOT_PHOTOS, true)
				.commit();
	}

	public static void savePrefLastApkVersion() {
		getmPref()
				.edit()
				.putLong(PREF_LAST_APK_VERSION, App.mApkVersionOfClient.verCode)
				.commit();
	}

	public static void savePrefMessageLastUpdatedate(long userId,
			String prefDate) {
		String dbLastUpdatedate = App.messageDAO.getMaxLastUpdatedate();// DB时间
		if (prefDate == null
				|| (dbLastUpdatedate != null && dbLastUpdatedate
						.compareTo(prefDate) > 0)) {// DB时间大于pref时间
			prefDate = dbLastUpdatedate;
		}
		getmPref().edit()
				.putString(userId + PREF_MESSAGE_LAST_UPDATE, prefDate)
				.commit();
	}

	public static void savePrefNewMessageCount(JSONObject json) {
		getmPref().edit().putString(getNewMessageCountKey(), json.toString())
				.commit();
	}

	public static void savePrefPopularLastUpdate(long lastTime) {
		getmPref().edit().putLong(PREF_POPULAR_LAST_UPDATE, lastTime).commit();
	}

	public static void savePrefPopularType(int type) {
		getmPref().edit().putInt(PREF_POPULAR_TYPE, type).commit();
	}

	public static void savePrefPreferLang(String lang) {
		getmPref().edit().putString(PREF_PREFER_LANG, lang).commit();
	}

	public static void savePrefRecommendedFriendsLastUpdate(long lastTime) {
		getmPref().edit()
				.putLong(PREF_RECOMMENDED_FRIENDS_LAST_UPDATE, lastTime)
				.commit();
	}

	public static void savePrefShowAccouncementDialogLastUpdatedate(
			String lastDate) {
		getmPref().edit()
				.putString(PREF_SHOW_ANNOUNCEMENT_DIALOG_LAST_UPDATE, lastDate)
				.commit();
	}

	public static void savePrefStoryTranslateShowTipsDialog() {
		getmPref().edit().putInt(PREF_STORY_TRANSLATION_SHOW_TIPS_DIALOG, 1)
				.commit();
	}

	public static void savePrefThirdPartyAccess(String third_party_type,
			String access_token, String third_party_user_id) {
		getmPref()
				.edit()
				.putString(AppPreferences.THIRD_PARTY_ACCESS_TOKEN,
						access_token).commit();
		getmPref().edit()
				.putString(AppPreferences.THIRD_PARTY_TYPE, third_party_type)
				.commit();
		getmPref()
				.edit()
				.putString(AppPreferences.THIRD_PARTY_USER_ID,
						third_party_user_id).commit();
	}

	public static void savePrefTranslatedNoticeComment(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_COMMENT, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeFriend(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_FRIEND, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeInterruptDuration(int duration) {
		getmPref().edit()
				.putInt(PREF_TRANSLATED_NOTICE_INTERRUPT_DURATION, duration)
				.commit();
	}

	public static void savePrefTranslatedNoticeInterruptStartHour(int start) {
		getmPref().edit()
				.putInt(PREF_TRANSLATED_NOTICE_INTERRUPT_START_HOUR, start)
				.commit();
	}

	public static void savePrefTranslatedNoticeInterruptSwitch(boolean isChecked) {
		getmPref().edit()
				.putBoolean(PREF_TRANSLATED_NOTICE_INTERRUPT_SWITCH, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeLike(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_LIKE, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeMessage(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_MESSAGE, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeReply(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_REPLY, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeTranslate(boolean isChecked) {
		getmPref().edit()
				.putBoolean(PREF_TRANSLATED_NOTICE_TRANSLATE, isChecked)
				.commit();
	}

	public static void savePrefTranslatedNoticeTts(boolean isChecked) {
		getmPref().edit().putBoolean(PREF_TRANSLATED_NOTICE_TTS, isChecked)
				.commit();
	}

	public static void savePrefTTTLastSelectedLang(int number, String lang) {
		getmPref().edit()
				.putString(PREF_TTT_LAST_SELECTED_LANG + "_" + number, lang)
				.commit();
	}

	public static void savePrefUserNewFansCount(Long userId, int newCount) {
		int totalCount = getPrefUserNewFansCount(userId) + newCount;
		getmPref().edit()
				.putInt(PREF_NEW_USER_FANS_COUNT + "_" + userId, totalCount)
				.commit();
	}

	public static void saveShowSystemFreeRechargePointInform(Long userId) {
		int result = getmPref().getInt(
				PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + userId, -1);
		if (result == -1) {
			getmPref()
					.edit()
					.putInt(PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_"
							+ userId, 1).commit();
		}
	}

	public static void saveShowSystemFreeRechargePointInform(String tel) {
		getmPref()
				.edit()
				.putString(
						PREF_SHOW_SYS_FREE_RECHARGE_POINT_INFORM + "_" + tel,
						tel).commit();
	}

	public static void saveUserChannelList(String list) {
		getmPref().edit().putString(PREF_USER_STORY_CHANNEL_LIST, list)
				.commit();
	}

	public static void saveUserStoryPhotoTagCode(String tagCode) {
		getmPref().edit().putString(PREF_USER_STORY_PHOTO_TAG, tagCode)
				.commit();
	}

	public static void saveVersionCheckedTime() {
		getmPref().edit()
				.putLong(PREF_VERSION_CHECKED_TIME, System.currentTimeMillis())
				.commit();
	}

	public static void savePrefException(String message) {
		getmPref().edit().putString(PREF_EXCEPTION, message).commit();
	}

	public static void writeGuide(JSONObject guide_data) {
		if (guide_data == null) {
			removeGuide();
		} else {
			if (guide_data.has("third_party_share_list")) {
				JSONArray third_party_share_list = new JSONArray();
				try {
					third_party_share_list = guide_data
							.getJSONArray("third_party_share_list");
				} catch (JSONException e1) {
				}
				// App.readServerAppInfo().setThirdPartyShareList(
				// third_party_share_list);

				List<Share> thirdPartyShareArray = new ArrayList<Share>();
				int size = third_party_share_list.length();
				for (int i = 0; i < size; i++) {
					try {
						JSONObject third_party_share = third_party_share_list
								.getJSONObject(i);
						Share thirdPartyShare = new Share(third_party_share);

						thirdPartyShareArray.add(thirdPartyShare);
					} catch (JSONException e) {
					}
				}
				writeObject(thirdPartyShareArray, PREF_THIRD_PARTY_SHARE);
			}

			List<Map<String, String>> qaMapList = new ArrayList<Map<String, String>>();
			if (guide_data.has("qa_list")) {
				try {
					JSONArray qa_list = guide_data.getJSONArray("qa_list");

					int size = qa_list.length();
					qaMapList = new ArrayList<Map<String, String>>(size);

					for (int i = 0; i < size; i++) {
						JSONObject jo = qa_list.getJSONObject(i);

						Map<String, String> message = Utils.convertJsonItem(jo);
						qaMapList.add(message);
					}
				} catch (JSONException e1) {
				}
				writeObject(qaMapList, PREF_OPEN_QA);
			}

		}
	}

	public static void writeMessageList(Message message) {
		if (message != null) {
			String[] messages = getPrefMessages();
			if (messages != null && messages.length > 0) {
				String str = "";
				int start = 0;
				if (messages.length > 500) {
					start = messages.length - 500;
				}
				for (int i = start; i < messages.length; i++) {
					str += messages[i] + ",";
				}
				str += message.getMessageid();
				getmPref().edit().putString(PREF_MESSAGE_LIST, str).commit();
			}
		}
	}

	public static void writeNotificationExtras(Map<String, Object> extras) {
		writeObject(extras, PREF_NOTIFICATION_EXTRAS);
	}

	public static void writeObject(Object obj, String key) {
		if (obj == null) {
			remove(key);
		} else {
			try {
				ByteArrayOutputStream out;
				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

				ObjectOutputStream objectOutput;
				objectOutput = new ObjectOutputStream(arrayOutputStream);
				objectOutput.writeObject(obj);
				byte[] data = arrayOutputStream.toByteArray();
				objectOutput.close();
				arrayOutputStream.close();

				out = new ByteArrayOutputStream();
				Base64OutputStream b64 = new Base64OutputStream(out,
						Base64.DEFAULT);
				b64.write(data);
				b64.close();
				out.close();
				String str = new String(out.toByteArray());
				getmPref().edit().putString(key, str).commit();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);

			}
		}
	}

	public static void writeRecommendedFriendUser(
			List<User> recommendedFriendList) {
		writeObject(recommendedFriendList, PREF_RECOMMENDED_FRIENDS);
	}

	public static void writeServerAppInfo(ServerAppInfo serverAppInfo) {
		writeObject(serverAppInfo, PREF_SERVERAPP_INFO);
	}

	public static void writeUser(User user) {
		writeObject(user, PREF_USERINFO);
	}

}
