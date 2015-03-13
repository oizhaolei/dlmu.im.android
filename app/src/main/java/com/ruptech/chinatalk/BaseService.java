package com.ruptech.chinatalk;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.event.AnnouncementEvent;
import com.ruptech.chinatalk.event.FriendEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.PresentEvent;
import com.ruptech.chinatalk.event.QAEvent;
import com.ruptech.chinatalk.event.StoryEvent;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.ChatTTTActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseService extends Service {

    private static final String TAG = "BaseService";
    private static final int MAX_TICKER_MSG_LEN = 50;
    protected static int SERVICE_NOTIFICATION = 1;
    protected WakeLock mWakeLock;
    private NotificationManager mNotificationManager;
    private ActivityManager mActivityManager;
    private Notification mNotification;
    private Intent mNotificationIntent;
    private Vibrator mVibrator;
    private Map<String, Integer> mNotificationCount = new HashMap<>(
            2);
    private Map<String, Integer> mNotificationId = new HashMap<>(
            2);
    private int mLastNotificationId = 2;

    @Override
    public void onCreate() {
        Log.i(TAG, "called onCreate()");
        super.onCreate();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.app_name));

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationIntent = new Intent(this, ChatActivity.class);
        mActivityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
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


    protected void notifyClient(String fromJid, String fromUserName,
                                String message, boolean showNotification) {
        if (!showNotification) {
            return;
        }
        mWakeLock.acquire();
        setNotification(fromJid, fromUserName, message);
        setLEDNotification();

        int notifyId = 0;
        if (mNotificationId.containsKey(fromJid)) {
            notifyId = mNotificationId.get(fromJid);
        } else {
            mLastNotificationId++;
            notifyId = mLastNotificationId;
            mNotificationId.put(fromJid, Integer.valueOf(notifyId));
        }

        // If vibration is set to true, add the vibration flag to
        // the notification and let the system decide.
        boolean vibraNotify = PrefUtils.getPrefBoolean(
                PrefUtils.VIBRATIONNOTIFY, true);
        if (vibraNotify) {
            mVibrator.vibrate(400);
        }
        mNotificationManager.notify(notifyId, mNotification);

        mWakeLock.release();
    }

    private void setNotification(String fromJid, String fromUserId,
                                 String message) {

        int mNotificationCounter = 0;
        if (mNotificationCount.containsKey(fromJid)) {
            mNotificationCounter = mNotificationCount.get(fromJid);
        }
        mNotificationCounter++;
        mNotificationCount.put(fromJid, mNotificationCounter);
        String author;
        if (null == fromUserId || fromUserId.length() == 0) {
            author = fromJid;
        } else {
            author = fromUserId;
        }
        String title = author;
        String ticker;
        boolean isTicker = PrefUtils.getPrefBoolean(
                PrefUtils.TICKER, true);
        if (isTicker) {
            int newline = message.indexOf('\n');
            int limit = 0;
            String messageSummary = message;
            if (newline >= 0)
                limit = newline;
            if (limit > MAX_TICKER_MSG_LEN
                    || message.length() > MAX_TICKER_MSG_LEN)
                limit = MAX_TICKER_MSG_LEN;
            if (limit > 0)
                messageSummary = message.substring(0, limit) + " [...]";
            ticker = title + ":\n" + messageSummary;
        } else
            ticker = author;
        mNotification = new Notification(R.drawable.ic_launcher, ticker,
                System.currentTimeMillis());
        Uri userNameUri = Uri.parse(fromJid);
        mNotificationIntent.setData(userNameUri);
        mNotificationIntent.putExtra(ChatActivity.INTENT_EXTRA_USERNAME,
                fromUserId);
        mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // need to set flag FLAG_UPDATE_CURRENT to get extras transferred
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotification.setLatestEventInfo(this, title, message, pendingIntent);
        if (mNotificationCounter > 1)
            mNotification.number = mNotificationCounter;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
    }

    private void setLEDNotification() {
        boolean isLEDNotify = PrefUtils.getPrefBoolean(
                PrefUtils.LEDNOTIFY, true);
        if (isLEDNotify) {
            mNotification.ledARGB = Color.MAGENTA;
            mNotification.ledOnMS = 300;
            mNotification.ledOffMS = 1000;
            mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
        }
    }

    public void resetNotificationCounter(String userJid) {
        mNotificationCount.remove(userJid);
    }

    public void clearNotification(String Jid) {
        int notifyId = 0;
        if (mNotificationId.containsKey(Jid)) {
            notifyId = mNotificationId.get(Jid);
            mNotificationManager.cancel(notifyId);
        }
    }

    public boolean isAppOnForeground() {
        List<ActivityManager.RunningTaskInfo> taskInfos = mActivityManager.getRunningTasks(1);
        if (taskInfos.size() > 0
                && TextUtils.equals(getPackageName(),
                taskInfos.get(0).topActivity.getPackageName())) {
            return true;
        }

        // List<RunningAppProcessInfo> appProcesses = mActivityManager
        // .getRunningAppProcesses();
        // if (appProcesses == null)
        // return false;
        // for (RunningAppProcessInfo appProcess : appProcesses) {
        // // Log.i(TAG, appProcess.processName);
        // // The name of the process that this object is associated with.
        // if (appProcess.processName.equals(mPackageName)
        // && appProcess.importance ==
        // RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
        // return true;
        // }
        // }
        return false;
    }

    private boolean isTranslationSecretary(String fromJid){
        return fromJid.startsWith("tttalk.org@");
    }

    private String getMessageTitle(String fromJid){
        String title = null;
        if (isTranslationSecretary(fromJid)){
            title = getString(R.string.translation_secretary);
        }else{
            long fromUserId = Utils.getTTTalkIDFromOF_JID(fromJid);
            User user = App.userDAO.fetchUser(fromUserId);
            String name;
            if (user != null) {
                name = Utils.getFriendName(fromUserId, user.getFullname());
            } else {
                //TODO: Get group chat name;
                name = "Group Chat";
            }
            title = String.format(GCMIntentService.PUSH_TITLE_PATTERN,
                    name, getString(R.string.push_title_message));
        }
        return title;
    }

    public void displayMessageNotification(NewChatEvent event) {

        String title = null;
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
            notificationIntent = new Intent(this, ChatTTTActivity.class);
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
