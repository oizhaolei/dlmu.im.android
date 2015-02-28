package com.ruptech.chinatalk.utils;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.UserPhoto;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

	public static void broadcastAddressUpdate(Context context) {
		Intent intent = new Intent(ADDRESS_UPDATE_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastBalance(Context context) {
		Intent intent = new Intent(BALANCE_UPDATE_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastChannel(Context context) {
		Intent intent = new Intent(CHANNEL_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastChannelList(Context context) {
		Intent intent = new Intent(CHANNEL_LIST_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastChatList(Context context) {
		Intent intent = new Intent(CHAT_LIST_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastMessage(Context context, Serializable message) {
		Intent intent = new Intent(CONTENT_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

	public static void broadcastRefreshNewMark(Context context) {
		Intent intent = new Intent(REFERSH_NEW_MARK_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastRemoveFriend(Context context) {
		Intent intent = new Intent(REMOVE_FRIEND_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastStoryMessage(Context context,
			UserPhoto userPhoto) {
		Intent intent = new Intent(STORY_POPULAR_CONTENT_MESSAGE_ACTION);
		context.sendBroadcast(intent);
		if (userPhoto != null) {
			intent = new Intent(STORY_CONTENT_MESSAGE_ACTION);
			intent.putExtra(EXTRA_MESSAGE, userPhoto);
			context.sendBroadcast(intent);
			intent = new Intent(STORY_COMMENT_MESSAGE_ACTION);
			intent.putExtra(EXTRA_MESSAGE, userPhoto);
			context.sendBroadcast(intent);
		}
	}

	public static void broadcastStoryTranslate(Context context,
			StoryTranslate storyTranslate) {

		if (storyTranslate != null) {
			Intent intent = new Intent(STORY_TRANSLATE_ACTION);
			intent.putExtra(EXTRA_MESSAGE, storyTranslate);
			context.sendBroadcast(intent);
		}
	}

	public static void broadcastStoryGift(Context context) {
		Intent intent = new Intent(STORY_GIFT_ACTION);
		context.sendBroadcast(intent);
	}

	public static void broadcastUserPhotoList(Context context) {
		Intent intent = new Intent(USER_PHOTO_LIST_ACTION);
		context.sendBroadcast(intent);
	}

	public static void displaySystemMessage(Context context, String message) {
		Intent intent = new Intent(SYSTEM_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}

	public static void messageNotification(Context context, Message message) {
		if (App.readUser() == null) {
			return;
		}
		MessageReceiver.messageCommonAction(context, message);
		// 刷新页面
		broadcastMessage(context, message);
	}

	public static final String EXTRA_MESSAGE = "message";
	public static final String SENDER_ID = "25249242234";
	public static final String CHAT_LIST_ACTION = "com.ruptech.chinatalk.CHAT_LIST_ACTION";

	public static final String CONTENT_MESSAGE_ACTION = "com.ruptech.chinatalk.CONTENT_MESSAGE";

	public static final String STORY_CONTENT_MESSAGE_ACTION = "com.ruptech.chinatalk.STORY_CONTENT_MESSAGE_ACTION";

	public static final String STORY_POPULAR_CONTENT_MESSAGE_ACTION = "com.ruptech.chinatalk.STORY_POPULAR_CONTENT_MESSAGE_ACTION";

	public static final String STORY_COMMENT_MESSAGE_ACTION = "com.ruptech.chinatalk.STORY_COMMENT_MESSAGE_ACTION";

	public static final String SYSTEM_MESSAGE_ACTION = "com.ruptech.chinatalk.SYSTEM_MESSAGE";

	public static final String REMOVE_FRIEND_ACTION = "com.ruptech.chinatalk.REMOVE_FRIEND_ACTION";

	public static final String ADDRESS_UPDATE_ACTION = "com.ruptech.chinatalk.ADDRESS_UPDATE";

	public static final String STORY_TRANSLATE_ACTION = "com.ruptech.chinatalk.STORY_TRANSLATE_ACTION";

	public static final String BALANCE_UPDATE_ACTION = "com.ruptech.chinatalk.BALANCE_UPDATE";

	public static final String CHANNEL_LIST_ACTION = "com.ruptech.chinatalk.CHANNEL_LIST_ACTION";

	public static final String CHANNEL_ACTION = "com.ruptech.chinatalk.CHANNEL_ACTION";

	public static final String USER_PHOTO_LIST_ACTION = "com.ruptech.chinatalk.USER_PHOTO_LIST_ACTION";

	public static final String REFERSH_NEW_MARK_ACTION = "com.ruptech.chinatalk.REFRESH_NEW_MARK_ACTION";

	public static final String STORY_GIFT_ACTION = "com.ruptech.chinatalk.STORY_GIFT_ACTION";
}
