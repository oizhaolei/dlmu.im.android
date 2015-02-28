package com.ruptech.chinatalk.http;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;

/**
 * for story module. using RESTFul
 * 
 * @author zhaolei
 * 
 */
public class HttpStoryServer extends HttpConnection {
	private final String TAG = Utils.CATEGORY
			+ HttpStoryServer.class.getSimpleName();

	private UserPhoto parseUserPhoto(JSONObject json) throws Exception {
		if (json.has("success")) {
			throw new ServerSideException(Utils.getNoPhotoMessage());
		} else {
			UserPhoto userPhoto = new UserPhoto(json);
			return userPhoto;
		}
	}

	public UserPhoto changePhotoTag(long id, String category) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(id));
		params.put("tag", category);
		params.put("lang", App.readUser().getLang());

		Response res = _post("photo_changetag", params);

		JSONObject result = res.asJSONObject();
		return parseUserPhoto(result);
	}

	public boolean deleteCommentNews(long id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(id));

		Response res = _get("delete_news", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.optBoolean("success");
		return success;
	}

	public boolean deleteUserPhoto(long id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(id));

		Response res = _get("photo_delete", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.optBoolean("success");
		return success;
	}

	public JSONObject discover(String type, String name, long maxId,
			long sinceId) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", name);
		params.put("lang", App.readUser().getLang());
		if (type != null)
			params.put("type", type);

		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}

		Response res = _post("discover", params);

		return res.asJSONObject();
	}

	public List<String> findByKeyword(String type, String keyword)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", type);
		params.put("keyword", keyword);

		Response res = _post("find_by_keyword", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<String> channelList = new ArrayList<String>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);
			String channel = jo.getString("title");
			channelList.add(channel);
		}
		return channelList;
	}

	public Channel followChannel(long channel_id, String follower)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(channel_id));
		params.put("follower", follower);

		Response res = _get("channel_follow", params);
		JSONArray result = res.asJSONArray();
		JSONObject model = result.getJSONObject(0);
		return new Channel(model);
	}

	@Override
	protected String getAppServerUrl() {
		return App.readServerAppInfo().getAppServeRestUrl();
	}

	public List<Gift> getUserGiftList(long user_id, long user_photo_id,
			long maxId, long sinceId) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", String.valueOf(user_id));
		if (user_photo_id > 0) {
			params.put("user_photo_id", String.valueOf(user_photo_id));
		}
		params.put("lang", App.readUser().getLang());
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}

		Response res = _get("get_present_donate_list", params);
		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<Gift> giftList = new ArrayList<Gift>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			Gift gift = new Gift(jo);
			giftList.add(gift);
		}
		return giftList;
	}

	public List<Gift> getUserGiftSumList(long user_id, long user_photo_id)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", String.valueOf(user_id));
		if (user_photo_id > 0) {
			params.put("user_photo_id", String.valueOf(user_photo_id));
		}
		params.put("lang", App.readUser().getLang());

		Response res = _get("get_present_donate_sum_list", params);
		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<Gift> giftList = new ArrayList<Gift>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			Gift gift = new Gift(jo);
			giftList.add(gift);
		}
		return giftList;
	}

	public UserPhoto getUserPhoto(long id, String lang) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(id));
		params.put("lang", lang);

		Response res = _get("photo", params);

		JSONObject result = res.asJSONObject();

		return parseUserPhoto(result);
	}

	public boolean giftDonate(long to_user_id, long present_id, long photo_id,
			int quantity) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("to_user_id", String.valueOf(to_user_id));
		params.put("present_id", String.valueOf(present_id));
		params.put("photo_id", String.valueOf(photo_id));
		params.put("quantity", String.valueOf(quantity));

		Response res = _get("present_donate", params);
		JSONObject result = res.asJSONObject();
		boolean success = result.optInt("success") == 1;
		return success;
	}

	public UserPhoto likePhoto(long id, boolean like) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(id));
		params.put("like", String.valueOf(like));
		params.put("lang", App.readUser().getLang());

		Response res = _post("photo_like", params);

		JSONObject result = res.asJSONObject();
		return parseUserPhoto(result);
	}

	public StoryTranslate likeTranslate(long user_photo_id,
			long story_translate_id, String lang, boolean like)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_photo_id", String.valueOf(user_photo_id));
		params.put("story_translate_id", String.valueOf(story_translate_id));
		params.put("like", String.valueOf(like));
		if (Utils.isEmpty(lang)) {
			lang = App.readUser().getLang();
		}
		params.put("lang", lang);

		Response res = _post("translate_like", params);

		JSONObject result = res.asJSONObject();
		StoryTranslate translate = new StoryTranslate(result);
		return translate;
	}

	public UserPhoto postNewStory(long parent_id, String pic_url,
			String content, int late6, int lnge6, String category,
			String address, long reply_id, int width, int height, String lang)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("parent_id", String.valueOf(parent_id));
		params.put("pic_url", pic_url);
		params.put("content", content);
		params.put("late6", String.valueOf(late6));
		params.put("lnge6", String.valueOf(lnge6));
		params.put("address", address);
		params.put("category", category);
		if (Utils.isEmpty(lang)) {
			lang = App.readUser().getLang();
		}

		params.put("lang", lang);
		params.put("reply_id", String.valueOf(reply_id));
		if (width > 0) {
			params.put("width", String.valueOf(width));
		}
		if (height > 0) {
			params.put("height", String.valueOf(height));
		}
		boolean noTranslate = true;
		if (!Utils.isEmpty(content)) {
			noTranslate = ParseEmojiMsgUtil.isNoNeedTranslate(content,
					App.mContext);
		}

		params.put("no_translate", String.valueOf(noTranslate));

		Response res = _post("photos", params);

		JSONObject result = res.asJSONObject();

		return parseUserPhoto(result);
	}

	public StoryTranslate postTranslate(long user_photo_id, String content,
			String lang) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_photo_id", String.valueOf(user_photo_id));
		params.put("lang", lang);
		params.put("to_content", content);

		Response res = _post("translate", params);

		JSONObject result = res.asJSONObject();

		StoryTranslate translate = new StoryTranslate(result);
		return translate;
	}

	public List<Channel> retrieveChannelList(long maxId, long sinceId)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxId", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceId", String.valueOf(sinceId));
		}
		params.put("lang", App.readUser().getLang());

		Response res = _get("channel_popular", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<Channel> channelList = new ArrayList<Channel>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);
			Channel channel = new Channel(jo);
			channelList.add(channel);
		}
		return channelList;
	}

	public List<Channel> retrieveUserChannelList(long maxId, long sinceId)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("lang", App.readUser().getLang());

		Response res = _get("channel_list", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<Channel> channelList = new ArrayList<Channel>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);
			Channel channel = new Channel(jo);
			channelList.add(channel);
		}
		return channelList;
	}

	public void retrieveChannelPhotoList(long maxId, long sinceId,
			long maxGood, long sinceGood, long channel_id, String type,
			List<Channel> channelList, List<UserPhoto> userPhotoList)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		if (maxGood != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxGood", String.valueOf(maxGood));
		}
		if (sinceGood != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceGood", String.valueOf(sinceGood));
		}
		params.put("id", String.valueOf(channel_id));
		if (!Utils.isEmpty(type)) {
			params.put("type", type);
		}
		params.put("lang", App.readUser().getLang());
		if (App.readUser().getAdditionalLangs() != null
				&& App.readUser().getAdditionalLangs().length > 0) {
			params.put("lang1", App.readUser().getAdditionalLangs()[0]);
		}
		Response res = _get("channel_photo_list", params);
		JSONObject result = res.asJSONObject();
		JSONArray list = result.getJSONArray("data");
		JSONArray channel = result.getJSONArray("channel");
		if (channel != null && channel.length() > 0) {
			JSONObject model = result.getJSONArray("channel").getJSONObject(0);
			channelList.add(new Channel(model));
		}
		int size = list.length();
		for (int i = 0; i < size; i++) {
			JSONObject jo = list.getJSONObject(i);

			UserPhoto userPhoto = new UserPhoto(jo);
			userPhotoList.add(userPhoto);
		}
	}

	public List<CommentNews> retrieveCommentNewsList(long maxId, long sinceId,
			String type) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("type", type);
		Response res = _get("news_list", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<CommentNews> newsList = new ArrayList<CommentNews>(size);
		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			CommentNews news = new CommentNews(jo);
			newsList.add(news);
		}
		return newsList;
	}

	public List<Gift> retrieveGiftList(long maxId, long sinceId)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("lang", App.readUser().getLang());
		Response res = _get("presents", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<Gift> giftList = new ArrayList<Gift>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			Gift gift = new Gift(jo);
			giftList.add(gift);
		}
		return giftList;
	}

	public List<UserPhoto> retrievePopularStoryList(long maxId, long sinceId)
			throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}

		Response res = _get("photos_hot", params);
		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<UserPhoto> popularStoryList = new ArrayList<UserPhoto>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);
			UserPhoto userPhoto = new UserPhoto(jo);
			popularStoryList.add(userPhoto);
		}
		return popularStoryList;
	}

	public List<StoryTranslate> retrieveStoryTranslateList(long maxId,
			long sinceId, long user_photo_id, long user_id) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("user_photo_id", String.valueOf(user_photo_id));
		params.put("user_id", String.valueOf(user_id));

		if (App.readUser() != null && App.readUser().getAllLangs().size() > 1) {
			String user_prefer_langs = "";
			for (String lang : App.readUser().getAllLangs()) {
				user_prefer_langs = user_prefer_langs + "'" + lang + "',";
			}
			user_prefer_langs = user_prefer_langs.substring(0,
					user_prefer_langs.length() - 1);
			params.put("user_prefer_langs", user_prefer_langs);

		}
		Response res = _get("translates", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<StoryTranslate> translateList = new ArrayList<StoryTranslate>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			StoryTranslate userPhoto = new StoryTranslate(jo);
			translateList.add(userPhoto);
		}
		return translateList;
	}

	public List<UserPhoto> retrieveUserCommentPhotoList(long maxId,
			long sinceId, long userid) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("userid", String.valueOf(userid));
		params.put("lang", App.readUser().getLang());

		Response res = _get("photos_comment", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<UserPhoto> userList = new ArrayList<UserPhoto>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			UserPhoto userPhoto = new UserPhoto(jo);
			userList.add(userPhoto);
		}
		return userList;
	}

	public List<User> retrieveUserPhotoLikeList(long photoId, long maxId,
			long sinceId) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", String.valueOf(photoId));
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}

		Response res = _get("photo_like_list", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<User> userList = new ArrayList<User>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			User user = new User(jo);
			userList.add(user);
		}
		return userList;
	}

	public List<UserPhoto> retrieveUserPhotoList(long maxId, long sinceId,
			long userid, long parent_id, String type, int late6, int lnge6,
			String tag, String order) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("userid", String.valueOf(userid));
		params.put("parent_id", String.valueOf(parent_id));
		if (!Utils.isEmpty(type)) {
			params.put("type", type);
		}
		params.put("lang", App.readUser().getLang());
		if (App.readUser().getAdditionalLangs() != null
				&& App.readUser().getAdditionalLangs().length > 0) {
			params.put("lang1", App.readUser().getAdditionalLangs()[0]);
		}
		if (AbstractUserStoryListActivity.STORY_TYPE_LOCATION.equals(type)) {
			params.put("late6", String.valueOf(late6));
			params.put("lnge6", String.valueOf(lnge6));
		}
		if (AbstractUserStoryListActivity.STORY_TYPE_TAG.equals(type)
				&& !Utils.isEmpty(tag)) {
			params.put("tag", String.valueOf(tag));
		}
		if (!Utils.isEmpty(order)) {
			params.put("order", String.valueOf(order));
		}

		Response res = _get("photos", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<UserPhoto> userList = new ArrayList<UserPhoto>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			UserPhoto userPhoto = new UserPhoto(jo);
			userList.add(userPhoto);
		}
		return userList;
	}

	public List<UserPhoto> retrieveUserPopularPhotoList(long maxId,
			long sinceId, String type, boolean isUp) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (maxId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("maxid", String.valueOf(maxId));
		}
		if (sinceId != AppPreferences.ID_IMPOSSIBLE) {
			params.put("sinceid", String.valueOf(sinceId));
		}
		params.put("lang", App.readUser().getLang());
		if (App.readUser().getAdditionalLangs() != null
				&& App.readUser().getAdditionalLangs().length > 0) {
			params.put("lang1", App.readUser().getAdditionalLangs()[0]);
		}
		params.put("type", type);

		Response res = _get("photos_popular", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<UserPhoto> userList = new ArrayList<UserPhoto>(size);

		if (isUp) {
			for (int i = size - 1; i >= 0; i--) {
				JSONObject jo = result.getJSONObject(i);

				UserPhoto userPhoto = new UserPhoto(jo);
				userList.add(userPhoto);
			}
		} else {
			for (int i = 0; i < size; i++) {
				JSONObject jo = result.getJSONObject(i);
				UserPhoto userPhoto = new UserPhoto(jo);
				userList.add(userPhoto);
			}
		}
		return userList;
	}

	public FileUploadInfo uploadFile(File file, UploadProgress uploadProgress)
			throws Exception {

		Response res = _uploadFile(getAppServerUrl() + "upload", file,
				uploadProgress);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			FileUploadInfo fileInfo = new FileUploadInfo();
			fileInfo.fileName = result.getString("msg");
			fileInfo.width = result.optInt("width");
			fileInfo.height = result.optInt("height");

			return fileInfo;
		}
		throw new HttpException();
	}

	public FileUploadInfo uploadSound(File file, UploadProgress uploadProgress)
			throws Exception {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "file:" + file);

		Response res = _uploadFile(getAppServerUrl() + "upload_sound", file,
				uploadProgress);

		JSONObject result = res.asJSONObject();
		boolean success = result.getBoolean("success");
		if (success) {
			FileUploadInfo fileInfo = new FileUploadInfo();
			fileInfo.fileName = result.getString("msg");
			fileInfo.width = result.optInt("width");
			fileInfo.height = result.optInt("height");

			return fileInfo;
		}
		throw new HttpException();
	}

	public List<User> retrieveLeaderboardUsers(String type) throws Exception {

		Map<String, String> params = new HashMap<String, String>();
		params.put("type", type);

		Response res = _get("find_user_leaderboard", params);

		JSONArray result = res.asJSONArray();
		int size = result.length();
		List<User> userList = new ArrayList<User>(size);

		for (int i = 0; i < size; i++) {
			JSONObject jo = result.getJSONObject(i);

			User user = new User(jo);
			userList.add(user);
		}
		return userList;
	}
}
