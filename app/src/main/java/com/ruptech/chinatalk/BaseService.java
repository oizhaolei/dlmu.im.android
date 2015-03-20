package com.ruptech.chinatalk;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.ruptech.chinatalk.event.FriendEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.impl.RetrieveFriendsTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;
import com.ruptech.dlmu.im.R;

public abstract class BaseService extends Service {

	private static final String TAG = "BaseService";
	protected WakeLock mWakeLock;

	@Override
	public void onCreate() {
		Log.i(TAG, "called onCreate()");
		super.onCreate();
		mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.app_name));

	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "called onDestroy()");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "called onStartCommand()");
		return START_STICKY;
	}


	private boolean isTranslationSecretary(String fromJid) {
		return fromJid.startsWith("tttalk.org@");
	}

	private String getMessageTitle(String fromJid) {
		String title;
		if (isTranslationSecretary(fromJid)) {
			title = getString(R.string.translation_secretary);
		} else {
			boolean isGroupChat = Utils.isGroupChat(fromJid);
			String name;
			if (isGroupChat) {
				name = "Group Chat:" + fromJid.substring(0, fromJid.indexOf("@"));
			} else {
				long fromUserId = Utils.getTTTalkIDFromOF_JID(fromJid);
				User user = App.userDAO.fetchUser(fromUserId);
				if (user != null) {
					name = user.getUsername();
				} else {
					name = "No name";
				}
			}
			title = name;
		}
		return title;
	}

	public void displayMessageNotification(NewChatEvent event) {

		String title;
		String fromJid = event.fromJID;
		String content = event.chatMessage;

		long fromUserId = Utils.getTTTalkIDFromOF_JID(fromJid);

		if (Utils.isEmpty(content))
			return;

		title = getMessageTitle(fromJid);

		int notiId = fromJid.hashCode();

		Intent notificationIntent;
		notificationIntent = new Intent(this, ChatActivity.class);
		notificationIntent.putExtra(ChatActivity.EXTRA_JID, fromJid);

		PendingIntent contentIntent = PendingIntent.getActivity(this,
				notiId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		MyNotificationBuilder mBuilder = createNotificationBuilder(this, title, content, null,
				PrefUtils.getPrefTranslatedNoticeMessage());
		mBuilder.setTicker(content);
		mBuilder.setContentIntent(contentIntent);

		App.notificationManager.cancel(notiId);
		App.notificationManager.notify(notiId, mBuilder.build());

//        // 如果选择TTS播放，处理
//        if (PrefUtils.getPrefTranslatedNoticeTts()) {
//            // TTS
//            if (!Utils.tts(context, message.getTo_lang(),
//                    message.getFrom_lang(), content)) {
//                Toast.makeText(
//                        context,
//                        context.getString(R.string.tts_no_supported_language),
//                        Toast.LENGTH_SHORT).show();
//            }
//        }

	}

	public static MyNotificationBuilder createNotificationBuilder(
			Context context, String title, String content, Bitmap icon,
			boolean isSound) {

		MyNotificationBuilder mBuilder = new MyNotificationBuilder(context,
				isSound, title, content, icon);

		return mBuilder;
	}

	public static void doRetrieveNewFriend(final long userId,
	                                       final boolean isNeedNotify) {

		RetrieveFriendsTask mRetrieveFriendsTask = new RetrieveFriendsTask(
				userId, isNeedNotify);
		mRetrieveFriendsTask.execute();
	}

	public void displayFrirendNotification(FriendEvent event) {
		doRetrieveNewFriend(App.readUser().getId(), false);
	}


}
