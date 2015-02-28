package com.ruptech.chinatalk.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import android.os.Environment;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.utils.ServerAppInfo;

public class HttpServerTestCase extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Thread.sleep(1000);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAcceptTranslateMessage() throws Exception {
		List<Message> messageList = App.getHttpServer().acceptTranslateMessage(
				10000);
		Assert.assertTrue(messageList.size() > 0);
	}

	public void testAddFriend() throws Exception {
		String tel = "15942616353";
		String nickname = "test";
		String memo = "TEST";
		String lastUpdatedate = "2014-01-28 01:03:17";
		String lang = "KR";
		List<User> userList = new ArrayList<User>();
		List<Friend> friendList = new ArrayList<Friend>();
		App.getHttpServer().addFriend(tel, "", nickname, memo,
				lastUpdatedate, lang, userList, friendList, "false`");
		Assert.assertTrue(friendList.size() >= 0);
	}

	// public void testAddUserAlbum() throws Exception {
	// String photo_name = Environment.getExternalStorageDirectory()
	// + "/Fire.png";
	// File mfile = new File(photo_name);
	// if (mfile.exists()) {
	// User user = App.getHttpServer().addUserAlbum(photo_name);
	// Assert.assertEquals(user.getTel(), "15942616353");
	// } else {
	// Assert.assertTrue(false);
	// }
	//
	// }

	public void testChangeFriendMemo() throws Exception {
		long friendid = 4636;
		String memo = "test";
		Friend friend = App.getHttpServer().changeFriendMemo(friendid, memo);
		Assert.assertEquals(friend.friend_memo, "test");
	}

	public void testChangeFriendNickName() throws Exception {
		long friendid = 13695;
		String nickName = "TEST123";
		Friend friend = App.getHttpServer().changeFriendNickName(friendid,
				nickName);
		Assert.assertEquals(friend.getFriend_nickname(), "TEST123");
	}

	public void testChangePassword() throws Exception {
		String oldPassword = "111";
		String password = "222";
		User user = App.getHttpServer().changePassword(oldPassword, password);
		Assert.assertEquals(user.getPassword(),
				"*899ECD04E40F745BD52A4C552BE4A818AC65FAF8");
		user = App.getHttpServer().changePassword(password, oldPassword);
		Assert.assertEquals(user.getPassword(),
				"*832EB84CB764129D05D498ED9CA7E5CE9B8F83EB");
	}


	public void testChangeUserProfile() throws Exception {
		String mFullname = "ZHENGBOYU";
		User user = App.getHttpServer().changeUserProfile("change_column",
				"fullname", mFullname);
		Assert.assertEquals(user.getFullname(), "ZHENGBOYU");
	}

	public void testFriendAddCheck() throws Exception {
		User existUser = new User();
		String[] str = App.getHttpServer().friendAddCheck("15942616353", "",
				existUser);
		Assert.assertTrue(str.length > 0);
	}

	public void testGetApkVersionOfServer() throws Exception {
		ServerAppInfo serverAppInfo = App.getHttpServer()
				.ver();

		Assert.assertTrue(App.mApkVersionOfClient.verCode >= serverAppInfo.verCode);
		System.out.println(serverAppInfo);
	}

	public void testGetMessageById() throws Exception {
		Message message = App.getHttpServer().getMessageById(10000);
		Assert.assertEquals(10000, message.getMessageid());
	}

	public void testGetRecommendedFriendList() throws Exception {
		List<User> list = App.getHttpServer().getRecommendedFriendList(
				"15942616353");
		Assert.assertTrue(list.size() > 0);
	}

	public void testGetUser() throws Exception {
		User user = App.getHttpServer().getUser(13695);
		Assert.assertEquals(user.getTel(), "15942616353");
	}

	public void testGetUser_1() throws Exception {
		String[] prop = new String[2];

		User user = App.getHttpServer().getUserByTel("15942616353", prop);
		Assert.assertTrue(prop.length > 0);
		Assert.assertEquals("15942616353", prop[0]);
		Assert.assertEquals(user.getTel(), "15942616353");
	}

	public void testRemoveFriend() throws Exception {
		Long friend_id = (long) 19086;
		new ArrayList<Friend>();
		App.getHttpServer().removeFriend(friend_id);
	}

	// public void testRemoveUserAlbums() throws Exception {
	// String photo_ids = "124";
	// User user = App.getHttpServer().removeUserAlbums(photo_ids);
	// Assert.assertEquals(user.getTel(), "15942616353");
	// }

	public void testRequestVerifyMessage() throws Exception {
		long message_id = 101;
		App.getHttpServer().requestVerifyMessage(message_id);
		Assert.assertTrue(true);
	}

	public void testRetrieveAnnouncementList() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveAnnouncementList();
		Assert.assertTrue(list.size() > 0);
	}

	public void testRetrieveNewFriends() throws Exception {
		String lastUpdatedate = "2014-01-27 07:21:26";
		List<User> userList = new ArrayList<User>();
		List<Friend> friendList = new ArrayList<Friend>();
		long userId = 13695;
		App.getHttpServer().retrieveNewFriends(lastUpdatedate, userList,
				friendList, userId);
		Assert.assertTrue(userList.size() > 0);
		Assert.assertTrue(friendList.size() > 0);
	}

	public void testRetrieveNewMessage() throws Exception {
		long userId = 13695;
		String updateDate = "2014-01-27 07:21:26";
		List<Message> list = App.getHttpServer().retrieveNewMessage(userId,
				updateDate);
		Assert.assertTrue(list.size() > 0);
	}

	public void testRetrieveQAList() throws Exception {
		List<Map<String, String>> list = App.getHttpServer().retrieveQAList(0);
		Assert.assertTrue(list.size() >= 0);
	}

	public void testRetrieveTranslatorList() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveTranslatorList("CN",  "0");
		Assert.assertTrue(list.size() >= 0);
	}

	public void testRetrieveVerifyMessage() throws Exception {
		List<Map<String, String>> list = App.getHttpServer()
				.retrieveVerifyMessage();
		Assert.assertTrue(list.size() >= 0);
	}

	// ------------------------------------------------------------------------------
	public void testSendClientMessage() throws Exception {
		String msg = "timed out";
		boolean result = App.getHttpServer().sendClientMessage(msg);
		Assert.assertTrue(result);
	}

	public void testSendUserPasswordSms() throws Exception {
		String tel = "15942616353";
		App.getHttpServer().sendUserPasswordSms(tel);
		Assert.assertTrue(true);
	}

	public void testSendUserVerifyCode() throws Exception {
		String msg = "15942616353";
		boolean result = App.getHttpServer().sendUserVerifyCode(msg);
		Assert.assertTrue(result);
	}

	public void testUploadPhoto() throws Exception {
		File file = new File(Environment.getExternalStorageDirectory(),"Fire.jpg");
		UploadProgress uploadProgress = new UploadProgress() {
			@Override
			public void onUpload(long uploaded, long total) {
				Log.d("testUploadPhoto", uploaded + " - " + total);
			}
		};
		FileUploadInfo msg = App.getHttpStoryServer().uploadFile(file, uploadProgress);
		Assert.assertEquals(msg.fileName, "Fire.jpg");
	}

	public void testUploadPhoto2() throws Exception {
		CharSequence url = "http://app.tttalk.org:4000/upload?loginid=4638&sign=438ea2fae66f377249db526f54362fb3731f48ab&source=an-2014122318";
		File file = new File(Environment.getExternalStorageDirectory(),
				"Fire.jpg");

		String body = HttpRequest.post(url).part("file", file.getName(), file)
				.body();
		Log.d("testUploadPhoto2", body);
	}

	public void testUserLogin() throws Exception {
		String username = "15942616353";
		String password = "*6BF1EB348A13D6DC8E414260E681272320B2C514";
		boolean encrypt = false;
		User user = App.getHttpServer().userLogin(username, password, encrypt);
		Assert.assertTrue(user.getId() == 13695);
	}

	public void testUserSignup() throws Exception {
		String tel = "15912345678";
		String password = "111";
		String fullname = "zheng";
		String lang = "CN";
		String file_path = null;
		String gender = "1";
		User user = App.getHttpServer().userSignup(tel, password, fullname,
				file_path, gender, lang);
		Assert.assertTrue(user.getId() > 0);
	}
}
