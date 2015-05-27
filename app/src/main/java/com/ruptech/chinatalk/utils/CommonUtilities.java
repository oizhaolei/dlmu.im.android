package com.ruptech.chinatalk.utils;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

    public static final String EXTRA_MESSAGE = "message";
    public static final String CHAT_LIST_ACTION = "com.ruptech.chinatalk.CHAT_LIST_ACTION";
    public static final String CONTENT_MESSAGE_ACTION = "com.ruptech.chinatalk.CONTENT_MESSAGE";
    public static final String REMOVE_FRIEND_ACTION = "com.ruptech.dlmu.im.REMOVE_FRIEND_ACTION";
    public static final String REFERSH_NEW_MARK_ACTION = "com.ruptech.dlmu.im.REFRESH_NEW_MARK_ACTION";

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

}
