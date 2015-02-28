package com.ruptech.chinatalk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService {
    private static void displayGcmNotification(Context context, String title,
                                               String content) throws JSONException {

        User readUser = App.readUser();
        if (readUser == null) {
            return;
        }
        if ("balance".equals(title)) {
            try {
                JSONObject jo = new JSONObject(content);
                String body = jo.optString("body");
                if (!Utils.isEmpty(jo.optString("body"))) {
                    String notificationTitle = context
                            .getString(R.string.recharge_process_finished);
                    MessageReceiver.notify(App.mContext, notificationTitle,
                            body, R.string.balance, -1, true);
                }
                readUser.setBalance(Double.valueOf(jo.optString("content")));
            } catch (JSONException e) {
                readUser.setBalance(Double.valueOf(content));
            }
            App.writeUser(readUser);
            CommonUtilities.broadcastBalance(context);
        } else if ("announcement".equals(title)) {
            String notifyTitle = getAnnouncementTitle();

            MessageReceiver.notify(context, notifyTitle, content,
                    R.string.announcement, -1, true);
        } else if ("qa".equals(title)) {
            String notifyTitle = getQATitle();

            MessageReceiver.notify(context, notifyTitle, content, R.string.qa,
                    -1, true);
        } else if ("friend".equals(title)) {
            FriendsRequestReceiver.doRetrieveNewFriend(readUser.getId(), false);
        } else if ("message".equals(title)) {
            JSONObject jo = new JSONObject(content);
            Message _message = new Message(jo);

            CommonUtilities.messageNotification(context, _message);
        } else if ("story_new_comment".equals(title)) {

            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String message = jo.optString("comment_content");
            String fullname = String.format(PUSH_TITLE_PATTERN,
                    userPhoto.getComment_fullname(),
                    context.getString(R.string.push_title_story_new_comment));
            if (Utils.isEmpty(message))
                message = context.getString(R.string.new_story_comment);
            UserStoryReceiver.sendStoryPhotoCommentNotice(App.mContext,
                    message, fullname, userPhoto,
                    PrefUtils.getPrefTranslatedNoticeComment());
        } else if ("story_new_comment_reply".equals(title)) {
            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String fullname = String.format(PUSH_TITLE_PATTERN, userPhoto
                    .getComment_fullname(), context
                    .getString(R.string.push_title_story_new_comment_reply));
            String message = jo.optString("comment_content");
            if (Utils.isEmpty(message))
                message = context.getString(R.string.new_story_comment_reply);
            UserStoryReceiver.sendStoryPhotoCommentNotice(App.mContext,
                    message, fullname, userPhoto,
                    PrefUtils.getPrefTranslatedNoticeReply());
        } else if ("story_like".equals(title)) {
            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String fullname = String.format(PUSH_TITLE_PATTERN,
                    userPhoto.getComment_fullname(),
                    context.getString(R.string.push_title_story_like));
            String message = context.getString(R.string.new_story_like);
            UserStoryReceiver.sendStoryPhotoCommentNotice(App.mContext,
                    message, fullname, userPhoto,
                    PrefUtils.getPrefTranslatedNoticeLike());
        } else if ("story_new".equals(title)) {
            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String message = userPhoto.getContent();
            String fullname = String.format(PUSH_TITLE_PATTERN,
                    userPhoto.getFullname(),
                    context.getString(R.string.push_title_story_new));
            if (Utils.isEmpty(message))
                message = context.getString(R.string.new_story);
            UserStoryReceiver.sendStoryPhotoCommentNotice(App.mContext,
                    message, fullname, userPhoto,
                    PrefUtils.getPrefTranslatedNoticeFriend());
        } else if ("story_new_translate".equals(title)) {
            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String fullname = String.format(PUSH_TITLE_PATTERN,
                    jo.getString("translate_user"),
                    context.getString(R.string.push_title_story_new_translate));
            String message = jo.optString("translate_content");
            UserStoryReceiver.sendStoryTranslateNotice(App.mContext, message,
                    userPhoto, fullname,
                    PrefUtils.getPrefTranslatedNoticeTranslate());
        } else if ("story_like_translate".equals(title)) {
            JSONObject jo = new JSONObject(content);
            UserPhoto userPhoto = new UserPhoto(jo);
            String fullname = String.format(PUSH_TITLE_PATTERN,
                    jo.getString("translate_user"),
                    context.getString(R.string.push_title_tranlate_like));
            String message = jo.optString("translate_content");
            UserStoryReceiver.sendStoryTranslateNotice(App.mContext, message,
                    userPhoto, fullname,
                    PrefUtils.getPrefTranslatedNoticeLike());
        } else if ("present_donate".equals(title)) {
            JSONObject jo = new JSONObject(content);
            Gift gift = new Gift(jo);
            String notificationTitle = String.format(PUSH_TITLE_PATTERN,
                    gift.getComment_fullname(),
                    context.getString(R.string.push_title_present_donate));
            String notificationContent = context
                    .getString(R.string.news_title_gift_donate);

            if (gift.getTo_user_photo_id() > 0) {
                PresentDonateReceiver.doRetrieveUserPhoto(
                        gift.getTo_user_photo_id(), notificationContent,
                        notificationTitle, gift.getId(), gift.getPic_url(),
                        PrefUtils.getPrefTranslatedNoticeLike());
            } else {
                PresentDonateReceiver.sendPresentDonteNotice(context,
                        notificationContent, notificationTitle, gift.getId(),
                        gift.getPic_url(), null,
                        PrefUtils.getPrefTranslatedNoticeLike());
            }
        } else if ("delete".equals(title)) {
            if (readUser.getId() == Long.valueOf(content)) {
                Utils.doLogout(App.mContext);
                System.exit(0);
            }
        }
    }

    public static String getAnnouncementTitle() {
        return String.format(PUSH_TITLE_PATTERN,
                App.mContext.getString(R.string.app_name),
                App.mContext.getString(R.string.push_title_announcement));
    }

    public static String getQATitle() {
        return String.format(PUSH_TITLE_PATTERN,
                App.mContext.getString(R.string.app_name),
                App.mContext.getString(R.string.push_title_qa));
    }

    public static final String PUSH_TITLE_PATTERN = "%s %s";

    protected final String TAG = Utils.CATEGORY
            + GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
//		super(SENDER_ID);
    }

    private void dispatchMessage(Context context, String message) {
        try {
            JSONObject json = new JSONObject(message);

            String title = json.getString("title");
            String content = json.getString("content");
            displayGcmNotification(context, title, content);
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, e.getMessage(), e);
            Utils.sendClientException(e, message);
        }
    }


    protected void onDeletedMessages(Context context, int total) {
//		String message = getString(R.string.gcm_deleted, total);
//		if (BuildConfig.DEBUG)
//			Log.i(TAG, "Received deleted messages notification:" + message);
    }


    public void onError(Context context, String errorId) {
//		if (BuildConfig.DEBUG)
//			Log.i(TAG, "Received error: " + errorId);
//		displaySystemMessage(context, getString(R.string.gcm_error, errorId));
    }


    protected void onMessage(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Received message. Extras: " + extras);
        if (App.readUser() == null)
            return;

        if (extras != null) {
            String message = extras.getString("message");
            dispatchMessage(context, message);
        }
    }


//	protected boolean onRecoverableError(Context context, String errorId) {
//		// log message
//		if (BuildConfig.DEBUG)
//			Log.i(TAG, "Received recoverable error: " + errorId);
//		displaySystemMessage(context,
//				getString(R.string.gcm_recoverable_error, errorId));
//		return super.onRecoverableError(context, errorId);
//	}


//	protected void onRegistered(Context context, String registrationId) {
//		if (BuildConfig.DEBUG)
//			Log.i(TAG, "Device registered: regId = " + registrationId);
//		displaySystemMessage(context, getString(R.string.gcm_registered));
//		ServerUtilities.registerGCMOnServer(this);
//	}
//
//
//	protected void onUnregistered(Context context, String registrationId) {
//		if (BuildConfig.DEBUG)
//			Log.i(TAG, "Device unregistered");
//		displaySystemMessage(context, getString(R.string.gcm_unregistered));
//	}

}