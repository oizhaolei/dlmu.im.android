package com.ruptech.chinatalk.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Item;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.Utils;

public class ApiTestCase extends TestCase {
	private final String TAG = Utils.CATEGORY
			+ ApiTestCase.class.getSimpleName();

	private void _testUser(User user, JSONObject jo) throws Exception {
		jo.put("user_id", user.getId());
		// ------------------------------------------------
		// 3. 个人信息修改
		// ------------------------------------------------
		profile(jo);

		// 5. Friend
		friend(jo);

		// ------------------------------------------------
		// 6. 聊天
		// ------------------------------------------------
		message(jo);

		story();
		story2();

		// recharge
		freeRecharge();

		etc();
	}

	protected void AcceptTranslateMessage() throws Exception {
		List<Message> messageList = App.getHttpServer().acceptTranslateMessage(
				10000);
		Assert.assertTrue(messageList.size() > 0);
	}

	protected void changeUserAlbum(JSONObject jo) throws Exception {
		User user = App.readUser();
		String photo_name = "Test.jpg";

		File mPhotoFile = new File(Environment.getExternalStorageDirectory(),
				photo_name);
		if (mPhotoFile.exists()) {

			mPhotoFile = ImageManager.compressImage(mPhotoFile, 75,
					App.mContext, true);

			FileUploadInfo msg = App.getHttpStoryServer().uploadFile(
					mPhotoFile, null);
			Assert.assertTrue(msg.width > 0);
			Assert.assertTrue(msg.height > 0);

			Assert.assertNotSame(msg.fileName, user.getPic_url());
			user = App.getHttpServer().changeUserProfile("change_column",
					"photo_name", msg.fileName);

			Assert.assertEquals(msg.fileName, user.getPic_url());

			jo.put("pic", msg.fileName);
		}
	}

	protected int CountFriends(Item user) throws Exception {
		List<User> userList = new ArrayList<User>();
		List<Friend> friendList = new ArrayList<Friend>();

		String lastUpdatedate = "2014-10-14 14:03:17";
		long userId = user.getId();
		App.getHttpServer().retrieveNewFriends(lastUpdatedate, userList,
				friendList, userId);
		Assert.assertTrue(userList.size() > 0);
		int countFriends = friendList.size();
		Assert.assertTrue(countFriends > 0);
		return countFriends;
	}

	private void etc() throws Exception {

		// 8.1 Add QA, RetrieveQaList
		qa();

		// 8.2 Announcement** list*
		retrieveAnnouncementList();

		retrieveTranslatorList();

		// RequestVerifyMessage();
	}

	private void freeRecharge() throws Exception {
		User user = App.readUser();

		double oldBal;
		double newBal;
		// 4.2.1 第三方分享
		// 4.2.1.1 新浪微博
		// passed
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_SINA_WEIBO);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// 4.2.1.2 FACEBOOK
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_FACEBOOK);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// 4.2.1.3 WECHAT
		// passed
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer()
				.freeRecharge(AppPreferences.SHARE_TO_WECHAT_MEMENTS);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// 4.2.1.4 QQ
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_QQ);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// 4.2.1.5 QQ空间
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_QZONE);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// 腾讯微博
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_TQQ);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

		// GOOGLE +
		oldBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		App.getHttpServer().freeRecharge(AppPreferences.SHARE_TO_GOOGLE_PLUS);
		newBal = App.getHttpServer().getUser(user.getId())
				.getBalance();
		Assert.assertEquals(App.readServerAppInfo().share_to_micro_blog_point,
				Double.valueOf(newBal - oldBal).intValue());

	}

	private void friend(JSONObject jo) throws Exception {
		User user = App.readUser();

		List<User> userList = new ArrayList<User>();
		List<Friend> friendList = new ArrayList<Friend>();

		// / no friend
		App.getHttpServer().retrieveNewFriends(null, userList, friendList,
				user.getId());
		Assert.assertEquals(0, userList.size());
		Assert.assertEquals(0, friendList.size());

		// / add first friend
		long friendUserId1 = 4638;
		User fu4638 = App.getHttpServer()
				.getUser(friendUserId1);
		String nickname = "zhaolei email";
		String memo = "memo";
		String lang = "KR";
		App.getHttpServer().addFriend("", String.valueOf(friendUserId1),
				nickname, memo, "", lang, userList, friendList, "false");
		Assert.assertEquals(1, userList.size());
		Assert.assertEquals(1, friendList.size());
		User fuser = userList.get(0);
		Assert.assertEquals(friendUserId1, fuser.getId());
		Assert.assertEquals(fu4638.getBalance(), fuser.getBalance());

		Friend friend = friendList.get(0);
		Assert.assertEquals(1, friend.getDone());

		// 关注的人
		user = App.getHttpServer().getUser(App.readUser().getId());
		User new_fuser = App.getHttpServer().getUser(friendUserId1);
		Assert.assertEquals(1, user.getFollow_count());
		Assert.assertEquals(1,
				new_fuser.getFollower_count() - fu4638.getFollower_count());

		// /change memo
		String newMemo = "new memo";
		friend = App.getHttpServer().changeFriendMemo(friendUserId1, newMemo);
		Assert.assertEquals(newMemo, friend.friend_memo);
		// /change nickname
		String newNickname = "new nickname";
		friend = App.getHttpServer().changeFriendNickName(friendUserId1,
				newNickname);
		Assert.assertEquals(newNickname, friend.friend_nickname);

		// 重复添加好友
		try {
			App.getHttpServer().addFriend("", String.valueOf(friendUserId1),
					nickname, memo, "", lang, userList, friendList, "false");
			Assert.assertTrue(userList.size() > 0);
			Assert.assertTrue(friendList.size() > 0);
		} catch (Exception e) {
		}
		// 添加中韩以外的手机号码
		try {
			App.getHttpServer().addFriend("1234567890", "", nickname, memo, "",
					lang, userList, friendList, "false");
			Assert.assertEquals(0, userList.size());
			Assert.assertEquals(0, friendList.size());
		} catch (Exception e) {
		}

		// / add second friend
		String friendUser2Tel = "18624357886";
		String[] prop = new String[2];
		User friendUser2 = App.getHttpServer().getUserByTel(friendUser2Tel,
				prop);
		User user1 = App.getHttpServer().getUser(user.getId());
		nickname = "zhaolei tel";
		App.getHttpServer().addFriend(friendUser2Tel, "", nickname, memo, "",
				lang, userList, friendList, "true");
		Assert.assertEquals(2, userList.size());
		Assert.assertEquals(2, friendList.size());
		// 关注的人
		user = App.getHttpServer().getUser(App.readUser().getId());
		Assert.assertEquals(2, user.getFollow_count());

		App.getHttpServer().retrieveNewFriends(null, userList, friendList,
				user.getId());
		Assert.assertEquals(2, userList.size());
		Assert.assertEquals(2, friendList.size());

		User user2 = App.getHttpServer().getUser(user.getId());
		Assert.assertEquals(user1.getBalance()
				+ App.readServerAppInfo().friend_add_from_contact_give_balance,
				user2.getBalance());

		JSONArray messagesJson = new JSONArray();
		for (User u : userList) {
			messagesJson.put(u.getId());
		}
		jo.put("friends", messagesJson);

		// / remove a friend
		friendUser2 = App.getHttpServer().getUser(friendUser2.getId());
		Friend removedFriend = App.getHttpServer().removeFriend(
				friendUser2.getId());
		Assert.assertEquals(0, removedFriend.getDone());

		App.getHttpServer().retrieveNewFriends(null, userList, friendList,
				user.getId());
		Assert.assertEquals(2, userList.size());
		Assert.assertEquals(2, friendList.size());
		// 关注的人
		user = App.getHttpServer().getUser(App.readUser().getId());
		new_fuser = App.getHttpServer().getUser(friendUser2.getId());
		Assert.assertEquals(1, user.getFollow_count());
		Assert.assertEquals(1,
				friendUser2.getFollower_count() - new_fuser.getFollower_count());

		// / block Friend
		Friend blockedFriend = App.getHttpServer().blockFriend(
				friendUser2.getId());
		Assert.assertEquals(AppPreferences.FRIEND_BLOCK_STATUS,
				blockedFriend.getDone());

		// /
		List<User> recommendlist = App.getHttpServer()
				.getRecommendedFriendList(friendUser2Tel);
		Assert.assertEquals(0, recommendlist.size());
		recommendlist = App.getHttpServer().getRecommendedFriendList(
				"18940895301");
		Assert.assertEquals(1, recommendlist.size());

		// 互相加为好友
		long friendUserId3 = 6248;
		String nickname3 = "liu email";
		String memo3 = "memo";
		String lang3 = "KR";
		App.getHttpServer().addFriend("", String.valueOf(friendUserId3),
				nickname3, memo3, "", lang3, userList, friendList, "false");

		long friendUserId4 = App.readUser().getId();
		User fu6248 = App.getHttpServer()
				.getUser(friendUserId3);
		App.writeUser(fu6248);
		String nickname4 = "testcase";
		String lang4 = "CN";
		App.getHttpServer().addFriend("", String.valueOf(friendUserId4),
				nickname4, "", "", lang4, userList, friendList, "false");
		// 打开钱包
		App.getHttpServer().friendWalletPriority(friendUserId4, 1);
		// 回复appuser
		App.writeUser(user);
	}

	// protected void DeleteUser() throws Exception {
	// Assert.assertTrue( App.getHttpServer().deleteUser());
	// }

	protected void FriendAddCheck(String tel) throws Exception {
		User existUser = new User();
		String[] str = App.getHttpServer().friendAddCheck(tel, "", existUser);
		Assert.assertTrue(str.length > 0);
	}

	protected void GetApkVersionOfServer() throws Exception {
		ServerAppInfo serverAppInfo = App.getHttpServer()
				.ver();

		Assert.assertTrue(App.mApkVersionOfClient.verCode >= serverAppInfo.verCode);
		System.out.println(serverAppInfo);
	}

	protected void GetMessageById(long msg_id) throws Exception {
		Message message = App.getHttpServer().getMessageById(msg_id);
		Assert.assertEquals(msg_id, message.getMessageid());
	}

	protected void GetUser() throws Exception {
		// TODO:
		User user = App.getHttpServer().getUser(7860);
		Assert.assertEquals(user.getTel(), "15942616353");
	}

	protected void GetUser_1() throws Exception {
		String[] prop = new String[2];

		User user = App.getHttpServer().getUserByTel("15942616353", prop);
		Assert.assertTrue(prop.length > 0);
		Assert.assertEquals("15942616353", prop[0]);
		Assert.assertEquals(user.getTel(), "15942616353");
	}

	private void message(JSONObject jo) throws Exception {
		User user = App.readUser();

		// 翻译秘书
		Message tttMessage1 = requestTTTTranslate();
		Message tttMessage2 = requestTTTTranslate2();
		// chat
		Message chatMessage1 = requestChatTranslate();
		Message chatMessage2 = requestChatTranslate2();

		JSONArray messagesJson = new JSONArray();
		messagesJson.put(tttMessage1.getMessageid());
		messagesJson.put(chatMessage1.getMessageid());
		jo.put("messages", messagesJson);
		messagesJson = new JSONArray();
		messagesJson.put(tttMessage2.getMessageid());
		messagesJson.put(chatMessage2.getMessageid());
		jo.put("fee_messages", messagesJson);

		// Message timeline
		String updateDate = tttMessage1.getUpdate_date();
		List<Message> messageList = App.getHttpServer().retrieveNewMessage(
				user.getId(), updateDate);
		Assert.assertTrue(messageList.size() > 0);
		Message message3 = null;
		for (Message message : messageList) {
			if (chatMessage1.getMessageid() == message.getMessageid()) {
				message3 = message;
			}
		}
		Assert.assertNotNull(message3);
	}

	private void profile(JSONObject jo) throws Exception {
		User user = App.readUser();

		// 3.1 Change full name
		String otherfullname = "a test user " + user.getId();
		user = App.getHttpServer().changeUserProfile("change_column",
				"fullname", otherfullname);
		Assert.assertEquals(otherfullname, user.getFullname());

		// 3.2 Change language
		String oldLang = user.getLang();
		String otherLang = "KR";
		user = App.getHttpServer().changeUserProfile("change_column", "lang",
				otherLang);
		Assert.assertEquals(otherLang, user.getLang());
		// change back
		user = App.getHttpServer().changeUserProfile("change_column", "lang",
				oldLang);
		Assert.assertEquals(oldLang, user.getLang());

		// 3.3 Change password
		String oldPassword = "111";
		String oldPasswordEncrype = user.getPassword();
		String password = "222";
		// 改密码
		user = App.getHttpServer().changePassword(oldPassword, password);
		// change back
		user = App.getHttpServer().changePassword(password, oldPassword);
		Assert.assertEquals(oldPasswordEncrype, user.getPassword());

		// 3.4 Change gender
		String gender = String.valueOf(AppPreferences.USERS_GENDER_FEMALE);
		user = App.getHttpServer().changeUserProfile("change_column", "gender",
				gender);
		Assert.assertEquals(AppPreferences.USERS_GENDER_FEMALE,
				user.getGender());

		// 3.4 Change usermemo
		String usermemo = "test case";
		user = App.getHttpServer().changeUserProfile("change_column",
				"user_memo", usermemo);
		Assert.assertEquals(usermemo, user.getUser_memo());

		// 3.4 Change Album
		changeUserAlbum(jo);
	}

	protected void qa() throws Exception {
		int orgVal, newVal;

		orgVal = App.getHttpServer().retrieveQAList(0).size();

		String question = "daily test, from zhaolei";
		App.getHttpServer().addQa(question);
		newVal = App.getHttpServer().retrieveQAList(0).size();

		Assert.assertEquals(newVal, orgVal + 1);
	}

	private Message requestChatTranslate() throws Exception {
		// request
		long localId = System.currentTimeMillis();
		long toUserId = 4638;// oizhaolei@gmail.com CN
		// long toUserId = 31946;// huaxijin@gmail.com JP
		// long toUserId = 30154;// 18624357886 CN
		String fromLang = "CN";
		String toLang = "JP";// no use
		String text = "你好";
		String file_path = null;
		int contentLength = Utils.textLength(text);
		String filetype = null;
		String lastUpdatedate = "";
		List<Message> list = App.getHttpServer().requestTranslate(localId,
				toUserId, fromLang, toLang, text, contentLength, filetype,
				lastUpdatedate, file_path);
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals(AppPreferences.MESSAGE_STATUS_TRANSLATED,
				message.getMessage_status());
		Assert.assertEquals(text, message.getFrom_content());
		Assert.assertTrue(message.getTo_user_fee() == 0);
		Assert.assertTrue(message.getFee() == 0);

		return message;
	}

	private Message requestChatTranslate2() throws Exception {

		long localId = System.currentTimeMillis();
		int toUserId = 6248;
		String fromLang = "CN";
		String toLang = "kR";// no use
		String text = "你好" + localId;
		String file_path = null;
		int contentLength = Utils.textLength(text);
		String filetype = null;
		String lastUpdatedate = "";
		List<Message> list = App.getHttpServer().requestTranslate(localId,
				toUserId, fromLang, toLang, text, contentLength, filetype,
				lastUpdatedate, file_path);
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals(AppPreferences.MESSAGE_STATUS_REQUEST_TRANS,
				message.getMessage_status());
		Assert.assertEquals(text, message.getFrom_content());
		Assert.assertTrue(message.getTo_user_fee() > 0);//
		Assert.assertTrue(message.getFee() == 0);

		// accquire

		// translate

		return message;
	}

	// free ttt
	private Message requestTTTTranslate() throws Exception {
		long localId = System.currentTimeMillis();
		long toUserId = 0;// 翻译秘书
		String fromLang = "CN";
		String toLang = "JP";
		String text = "你好";
		String file_path = null;
		int contentLength = 2;
		String filetype = null;
		String lastUpdatedate = "";
		List<Message> list = App.getHttpServer().requestTranslate(localId,
				toUserId, fromLang, toLang, text, contentLength, filetype,
				lastUpdatedate, file_path);
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals(AppPreferences.MESSAGE_STATUS_TRANSLATED,
				message.getMessage_status());
		Assert.assertEquals(text, message.getFrom_content());

		Assert.assertTrue(message.getFee() == 0);
		Assert.assertTrue(message.getTo_user_fee() == 0);

		return message;
	}

	// no free
	private Message requestTTTTranslate2() throws Exception {
		long localId = System.currentTimeMillis();
		long toUserId = 0;// 翻译秘书
		String fromLang = "CN";
		String toLang = "JP";
		String text = "你好" + localId;
		String file_path = null;
		int contentLength = 2;
		String filetype = null;
		String lastUpdatedate = "";
		List<Message> list = App.getHttpServer().requestTranslate(localId,
				toUserId, fromLang, toLang, text, contentLength, filetype,
				lastUpdatedate, file_path);
		Assert.assertEquals(1, list.size());
		Message message = list.get(0);
		Assert.assertEquals(AppPreferences.MESSAGE_STATUS_REQUEST_TRANS,
				message.getMessage_status());
		Assert.assertEquals(text, message.getFrom_content());

		return message;
	}

	protected void RequestVerifyMessage() throws Exception {
		long message_id = 101;
		App.getHttpServer().requestVerifyMessage(message_id);
		Assert.assertTrue(true);
	}

	protected void retrieveAnnouncementList() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveAnnouncementList();
		Assert.assertTrue(list.size() > 0);
	}

	protected void RetrieveNewFriends(String lastUpdateDate) throws Exception {
		List<User> userList = new ArrayList<User>();
		List<Friend> friendList = new ArrayList<Friend>();
		long userId = 7860;
		App.getHttpServer().retrieveNewFriends(lastUpdateDate, userList,
				friendList, userId);
		Assert.assertTrue(userList.size() > 0);
		Assert.assertTrue(friendList.size() > 0);
	}

	protected void retrieveTranslatorList() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveTranslatorList("CN", "0");
		Assert.assertTrue(list.size() >= 0);
	}

	protected void RetrieveVerifyMessage() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveVerifyMessage();
		Assert.assertTrue(list.size() >= 0);
	}

	// ------------------------------------------------------------------------------

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Thread.sleep(1000);
	}

	private void story() throws Exception {
		// friend
		List<UserPhoto> popularList = App.getHttpStoryServer()
				.retrieveUserPopularPhotoList(AppPreferences.ID_IMPOSSIBLE, 0,
						AbstractUserStoryListActivity.STORY_TYPE_FRIENDS, true);
		Assert.assertTrue(popularList.size() > 0);
		// timeline
		popularList = App.getHttpStoryServer().retrieveUserPopularPhotoList(
				AppPreferences.ID_IMPOSSIBLE, 0,
				AbstractUserStoryListActivity.STORY_TYPE_TIMELINE, true);
		Assert.assertTrue(popularList.size() > 0);
		// photo
		List<UserPhoto> photoList = App.getHttpStoryServer()
				.retrieveUserPhotoList(AppPreferences.ID_IMPOSSIBLE, 0, -1, 0,
						AbstractUserStoryListActivity.STORY_TYPE_PHOTO, 0, 0,
						null, null);
		Assert.assertTrue(photoList.size() > 0);
		// chosen
		photoList = App.getHttpStoryServer().retrieveUserPhotoList(
				AppPreferences.ID_IMPOSSIBLE, 0, -1, 0,
				AbstractUserStoryListActivity.STORY_TYPE_CHOSEN, 0, 0, null,
				null);
		Assert.assertTrue(photoList.size() > 0);

		//
		List<User> retrievePopularUsers = App.getHttpServer()
				.retrievePopularUsers();
		Assert.assertTrue(retrievePopularUsers.size() > 0);
		List<User> retrieveOnlineUsers = App.getHttpServer()
				.retrieveOnlineUsers();
		Assert.assertTrue(retrieveOnlineUsers.size() > 0);

		int late6 = 38882883;
		int lnge6 = 121519607;
		List<User> retrieveLbsUsers = App.getHttpServer().retrieveLbsUsers(
				late6, lnge6);
		Assert.assertTrue(retrieveLbsUsers.size() > 0);

	}

	private void story2() throws Exception {
		double latitude = 38.882883;
		double longitude = 121.519607;
		String cityName = App.getHttp2Server().getAddress(latitude, longitude);
		if (!Utils.isEmpty(cityName)) {
			Assert.assertTrue(cityName.indexOf("大连") >= 0);
		}

		int userPhotoId = 85369;// 晚安
		UserPhoto userPhoto = App.getHttp2Server().autoTranslatePhoto(
				userPhotoId, "EN");
		Assert.assertEquals("Good night", userPhoto.getTo_content());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRetrieveUserPopularPhotoList() throws Exception {
		story();
		story2();
	}

	public void testUserSession() throws Exception {
		long userId = 75681;
		User _savedUser = App.readUser();
		JSONObject jo = new JSONObject();
		try {
			// ------------------------------------------------
			// 2. 登录
			// ------------------------------------------------
			User user = App.getHttpServer().getUser(userId);
			App.writeUser(user);
			// ------------------------------------------------
			// 3. 个人信息修改
			// ------------------------------------------------
			changeUserAlbum(jo);
			// ------------------------------------------------
			// 4. 支付
			// ------------------------------------------------
			// 4.1 payment: TODO:
			//

			// ------------------------------------------------
			// 8. 其他
			// ------------------------------------------------

		} finally {
			App.writeUser(_savedUser);
			Log.e(TAG, jo.toString());
		}
	}

	// ------------------------------------------------
	// 1. 注册
	// ------------------------------------------------
	public void testUserSignup() throws Exception {
		User _savedUser = App.readUser();
		JSONObject jo = new JSONObject();
		try {
			String tel = String.valueOf(System.currentTimeMillis())
					+ "@tttalk.org";
			String passwd = "111";
			String fullname = "testcase";
			String lang = "CN";
			String file_path = null;
			String gender = "1";
			boolean[] return_sign_up = new boolean[1];

			// 1.新用户signup
			User user = App.getHttpServer().userSignup(tel, passwd, fullname,
					file_path, gender, lang);
			Assert.assertEquals(tel, user.getTel());
			Assert.assertTrue(user.balance - AppPreferences.INI_BALANCE < 1);

			User loginUser = App.getHttpServer().userLogin(tel, passwd, false);
			Assert.assertEquals(loginUser.getTel(), user.getTel());

			// 2.删除
			Assert.assertEquals(0, user.getDelete_flag());
			App.writeUser(user);
			App.getHttpServer().deleteUser();
			user = App.getHttpServer().getUser(user.getId());
			Assert.assertEquals(1, user.getDelete_flag());

			Thread.sleep(2000);
			try {
				loginUser = App.getHttpServer().userLogin(tel, passwd, false);
				fail("you can not login using a deleted user.");
			} catch (Exception e) {
			}

			// 3.重新signup
			User user2 = App.getHttpServer().userSignup(tel, passwd, fullname,
					file_path, gender, lang);
			Assert.assertEquals(0, user2.getDelete_flag());
			Assert.assertTrue(return_sign_up[0]);
			Assert.assertTrue(user2.balance - AppPreferences.INI_BALANCE < 1);

			Thread.sleep(2000);
			loginUser = App.getHttpServer().userLogin(tel, passwd, false);
			Assert.assertEquals(loginUser.getTel(), user2.getTel());

			// 4.business logic test
			App.writeUser(loginUser);

			_testUser(loginUser, jo);

			// 5.删除
			// Assert.assertEquals(0, loginUser.getDelete_flag());
			// App.writeUser(loginUser);
			// App.getHttpServer().deleteUser();
			// User user3 = App.getHttpServer().getUser(
			// String.valueOf(loginUser.getId()));
			// Assert.assertEquals(1, user3.getDelete_flag());

		} finally {
			Log.e(TAG, jo.toString());
			App.getHttpServer().sendClientMessage(jo.toString());

			App.writeUser(_savedUser);
		}

		// TODO 定期删除tel='testcase'的数据
		// delete from tbl_user where fullname = 'testcase';
	}

	protected User UserLogin(String username, String passwd, boolean encrypt)
			throws Exception {

		User user = App.getHttpServer().userLogin(username, passwd, encrypt);
		Assert.assertTrue(user.getId() == 7860);
		return user;
	}

}
