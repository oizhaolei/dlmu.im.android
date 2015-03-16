package com.ruptech.chinatalk;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ruptech.chinatalk.event.AnnouncementEvent;
import com.ruptech.chinatalk.event.FriendEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.PresentEvent;
import com.ruptech.chinatalk.event.QAEvent;
import com.ruptech.chinatalk.event.StoryEvent;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.TTTActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

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


	private boolean isTranslationSecretary(String fromJid){
        return fromJid.startsWith("tttalk.org@");
    }

    private String getMessageTitle(String fromJid){
        String title;
        if (isTranslationSecretary(fromJid)){
            title = getString(R.string.translation_secretary);
        }else{
            boolean isGroupChat = Utils.isGroupChat(fromJid);
            String name;
            if (isGroupChat){
                name = "Group Chat:" + fromJid.substring(0, fromJid.indexOf("@"));
            }else{
                long fromUserId = Utils.getTTTalkIDFromOF_JID(fromJid);
                User user = App.userDAO.fetchUser(fromUserId);
                if (user != null) {
                    name = Utils.getFriendName(fromUserId, user.getFullname());
                } else {
                    name = "No name";
                }
            }
            title = String.format(GCMIntentService.PUSH_TITLE_PATTERN,
                    name, getString(R.string.push_title_message));
        }
        return title;
    }

    public void displayMessageNotification(NewChatEvent event) {

        String title ;
        String fromJid = event.fromJID;
        String type = event.type;
        String content = event.chatMessage;

        long fromUserId = Utils.getTTTalkIDFromOF_JID(fromJid);

        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(type)) {
            content = getString(R.string.notification_picture);
        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(type)) {
            content = getString(R.string.notification_voice);
        }
        if (Utils.isEmpty(content))
            return;

        title = getMessageTitle(fromJid);

        int notiId = fromJid.hashCode();

        if (fromUserId > 0) {
            App.mBadgeCount.addNewMessageCount(fromJid);
        }
        Intent notificationIntent;
        if (!isTranslationSecretary(fromJid)) {
            notificationIntent = new Intent(this, ChatActivity.class);
            notificationIntent.putExtra(ChatActivity.EXTRA_JID, fromJid);
        } else {
            notificationIntent = new Intent(this, TTTActivity.class);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                notiId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationBuilder mBuilder = MessageReceiver
                .createNotificationBuilder(this, title, content, null,
                        PrefUtils.getPrefTranslatedNoticeMessage());
        mBuilder.setTicker(content);
        mBuilder.setContentIntent(contentIntent);

        if (title.equalsIgnoreCase(GCMIntentService.getQATitle())
                || title.equalsIgnoreCase(GCMIntentService
                .getAnnouncementTitle()))
            mBuilder.setShowSetting(false);

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

    public void displayQaNotification(QAEvent event) {
        String notifyTitle = getString(R.string.qa);
        String content = event.content;
        MessageReceiver.notify(App.mContext, notifyTitle, content,
                R.string.qa, -1, true);
    }

    public void displayAnnouncementNotification(AnnouncementEvent event) {
        String notifyTitle = getString(R.string.announcement);
        String content = event.content;
        MessageReceiver.notify(App.mContext, notifyTitle, content,
                R.string.announcement, -1, true);
    }

    public void displayFrirendNotification(FriendEvent event) {
        FriendsRequestReceiver.doRetrieveNewFriend(App.readUser().getId(), false);
    }

    public void displayPresentNotification(PresentEvent event) {
        String PUSH_TITLE_PATTERN = "%s %s";
        String notificationTitle = String.format(PUSH_TITLE_PATTERN,
                event.fullname, getString(R.string.push_title_present_donate));
        String notificationContent = getString(R.string.news_title_gift_donate);
        if (event.to_user_photo_id > 0) {
            PresentDonateReceiver.doRetrieveUserPhoto(
                    event.to_user_photo_id, notificationContent,
                    notificationTitle, event.present_id, event.pic_url,
                    PrefUtils.getPrefTranslatedNoticeLike());
        } else {
            PresentDonateReceiver.sendPresentDonteNotice(this,
                    notificationContent, notificationTitle, event.present_id,
                    event.pic_url, null,
                    PrefUtils.getPrefTranslatedNoticeLike());
        }
    }

	public static void displayTranslatedNotice(Context context, String msg,
	                                        boolean close) {
		final int iconRes = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		int defaults = Notification.DEFAULT_LIGHTS;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(iconRes)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(msg);
		Intent notificationIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntent(notificationIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, 0);
		mBuilder.setWhen(when);
		mBuilder.setAutoCancel(true);
		mBuilder.setDefaults(defaults);
		mBuilder.setTicker(msg);
		mBuilder.setContentIntent(resultPendingIntent);
		App.notificationManager.notify(iconRes, mBuilder.build());

		if (close) {
			// close after 10 seconds
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					App.notificationManager.cancel(iconRes);
				}
			}, 1000 * 10);
		}
	}

    public void displayStoryNotification(StoryEvent event) {
        String PUSH_TITLE_PATTERN = "%s %s";
        String title = event.title;
        String photoId = event.photo_id;
        String fullName = event.fullname;
        String content = event.content;
        if ("story_new_comment".equals(title)) {
            String fullname = event.fullname;
            if (!Utils.isEmpty(fullname)) {
                String notificationTitle = getString(R.string.new_story_comment);
                String comment_fullname = String
                        .format(PUSH_TITLE_PATTERN,
                                fullname, getString(R.string.push_title_story_new_comment));

                UserStoryReceiver.doRetrieveNewComment(
                        Long.valueOf(photoId), notificationTitle,
                        comment_fullname,
                        PrefUtils.getPrefTranslatedNoticeComment());
            }
        } else if ("story_like".equals(title)) {
            String notificationTitle = getString(R.string.new_story_like);
            String fullname = String.format(
                    GCMIntentService.PUSH_TITLE_PATTERN,
                    fullName,
                    getString(R.string.push_title_story_like));

            UserStoryReceiver.doRetrieveNewComment(
                    Long.valueOf(photoId), notificationTitle, fullname,
                    PrefUtils.getPrefTranslatedNoticeLike());
        } else if ("story_new".equals(title)) {
            String notificationTitle = getString(R.string.new_story);
            if (!Utils.isEmpty(content))
                notificationTitle = content;
            String fullname = String.format(
                    GCMIntentService.PUSH_TITLE_PATTERN,
                    fullName,
                    getString(R.string.push_title_story_new));
            UserStoryReceiver.doRetrieveNewComment(
                    Long.valueOf(photoId), notificationTitle, fullname,
                    PrefUtils.getPrefTranslatedNoticeFriend());
        } else if ("story_new_translate".equals(title)) {
            String fullname = String
                    .format(GCMIntentService.PUSH_TITLE_PATTERN,
                            fullName,
                            getString(R.string.push_title_story_new_translate));
            UserStoryReceiver.doRetrieveNewTranslate(
                    Long.valueOf(photoId), content, fullname,
                    PrefUtils.getPrefTranslatedNoticeTranslate());
        } else if ("story_like_translate".equals(title)) {
            String fullname = String
                    .format(GCMIntentService.PUSH_TITLE_PATTERN,
                            fullName,
                            getString(R.string.push_title_tranlate_like));
            UserStoryReceiver.doRetrieveNewTranslate(
                    Long.valueOf(photoId), content, fullname,
                    PrefUtils.getPrefTranslatedNoticeLike());
        } else if ("story_new_comment_reply".equals(title)) {
            String notificationTitle = getString(R.string.new_story_comment_reply);
            String fullname = String
                    .format(GCMIntentService.PUSH_TITLE_PATTERN,
                            fullName,
                            getString(R.string.push_title_story_new_comment_reply));

            if (!Utils.isEmpty(content))
                notificationTitle = content;
            UserStoryReceiver.doRetrieveNewComment(
                    Long.valueOf(photoId), notificationTitle, fullname,
                    PrefUtils.getPrefTranslatedNoticeReply());
        }
    }
}
