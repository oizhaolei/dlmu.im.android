package com.ruptech.chinatalk.test;

import java.util.List;

import junit.framework.Assert;
import android.test.AndroidTestCase;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;

public class HttpStoryServerTestCase extends AndroidTestCase {

	private static final long deletedUserId = 53383; // tel=15940800830 53383

	private static final String INVALID_MESSAGE = "\"message\":\"invalid user\"";

	private static final long normalUserId = 53383; // tel=litieshuai2008@163.com

	public static void assertDeletedUserException(boolean isDeleted, Exception e)
			throws Exception {
		if (isDeleted)
			Assert.assertTrue("Deleted user Exception", true);
		else
			Assert.assertTrue("Deleted user Exception", false);
	}

	public static void assertDeletedUserResponse(boolean isDeleted) {
		if (isDeleted) {
			Assert.assertTrue("Can get response", false);
		} else {
			Assert.assertTrue("Can get response", true);
		}
	}

	public static void assertDeletedUserRestifyException(boolean isDeleted, Exception e)
			throws Exception {
		if (isDeleted) {
			String message = e.getMessage();
			Assert.assertTrue("Found 'Invalid user'", message != null
					&& message.contains(INVALID_MESSAGE));
		} else {
			throw e;
		}
	}

	public static void setTestUserType(boolean isDeleted){
		// for (Server s : App.readServerAppInfo().serverArray) {
		// s.appServer2Url = "http://epg.jccast.com:5001/";
		// s.appServerRestUrl = "http://epg.jccast.com:5000/";
		// s.fileServerUrl = "http://epg.jccast.com:8081/upload/";
		// }

		if (isDeleted)
			App.readUser().setId(deletedUserId);
		else
			App.readUser().setId(normalUserId);
	}
	// 48547
	private final String address = "中国 大连";
	private final long autoTranslatePhotoId = 17100;
	private final String content = "很好吃！！！";
	private final long deletePhotoId = 33771;
	private final boolean isTestDeletedUser = false;
	private final String lang = "CN";
	private final int late6 = 1200882;
	private final int lnge6 = 1200882;
	private final long maxid = AppPreferences.ID_IMPOSSIBLE;
	private final long messageId = 1069679;
	private final long photoId = 33770;
	private final String photoType = AbstractUserStoryListActivity.STORY_TYPE_FRIENDS; // me, friend, favorite,
	// location, tag, chosen
	private final String pic_url = "df211197603b8aa36ed1eecfa6829401";
	private final long replyId = 48547;
	private final long sinceid = 0;
	private final String tag = "food";



	private final long viewUserId = 53383;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Thread.sleep(1000);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testDeleteUserPhoto() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			boolean success = App.getHttpStoryServer()
					.deleteUserPhoto(deletePhotoId);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}

	public void testGetUserPhoto() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			UserPhoto userPhoto = App.getHttpStoryServer().getUserPhoto(photoId,
					lang);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}

	// public void testGetUserPhotoByMessageId() throws Exception {
	// setTestUserType(isTestDeletedUser);
	// try {
	// UserPhoto userPhoto = App.getHttpStoryServer()
	// .getUserPhotoByMessageId(messageId);
	//
	// assertDeletedUserResponse(isTestDeletedUser);
	// } catch (Exception e) {
	// assertDeletedUserRestifyException(isTestDeletedUser, e);
	// }
	// }

	public void testLikePhoto() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			UserPhoto userPhoto = App.getHttpStoryServer().likePhoto(photoId, true);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}

	public void testPostNewStory() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			UserPhoto User = App.getHttpStoryServer().postNewStory(photoId, pic_url,
					content, late6, lnge6, tag, address, replyId, 0, 0, lang);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}

	public void testRetrieveUserCommentPhotoList() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			List<UserPhoto> userList = App.getHttpStoryServer()
					.retrieveUserCommentPhotoList(maxid, sinceid, viewUserId);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}

	public void testRetrieveUserPhotoList() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			List<UserPhoto> userList = App.getHttpStoryServer()
					.retrieveUserPhotoList(maxid, sinceid, viewUserId,
							viewUserId, photoType, 0, 0, "food", "desc");

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}


	public void testUserPhotoLikeList() throws Exception {
		setTestUserType(isTestDeletedUser);
		try {
			List<User> userList = App.getHttpStoryServer()
					.retrieveUserPhotoLikeList(photoId, maxid, sinceid);

			assertDeletedUserResponse(isTestDeletedUser);
		} catch (Exception e) {
			assertDeletedUserRestifyException(isTestDeletedUser, e);
		}
	}
}
