package com.ruptech.chinatalk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveAnnouncementByIdTask;
import com.ruptech.chinatalk.task.impl.RetrieveMessageTask;
import com.ruptech.chinatalk.task.impl.RetrieveQaByIdTask;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class BaiduPushMessageReceiver {
    public static boolean isRegistered() {
        return registered;
    }

    public static boolean isRegisteredOnServer() {
        return registeredOnServer;
    }

    public static void setRegisteredOnServer(boolean registeredOnServer) {
        BaiduPushMessageReceiver.registeredOnServer = registeredOnServer;
    }

    // 本机唯一标示
    public static String mUserlId;

    private static boolean registered = false;

    private static boolean registeredOnServer = false;

    /**
     * TAG to Log
     */
    public static final String TAG = BaiduPushMessageReceiver.class
            .getSimpleName();

    private GenericTask baiduReceiverTask;

    private final TaskListener retrieveAnnouncementByIdTaskListener = new TaskAdapter() {


        public void onPostExecute(GenericTask task, TaskResult result) {
            RetrieveAnnouncementByIdTask retrieveAnnouncementByIdTask = (RetrieveAnnouncementByIdTask) task;
            if (result == TaskResult.OK) {
                Map<String, String> announcement = retrieveAnnouncementByIdTask
                        .getAnnouncement();
                // notifies user
                String message = announcement.get("title");
                String title = GCMIntentService.getAnnouncementTitle();
                MessageReceiver.notify(App.mContext, title, message,
                        R.string.announcement, -1, true);
            } else {
                String msg = retrieveAnnouncementByIdTask.getMsg();
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "retrieveAnnouncementByIdTask:" + msg);
            }
        }


        public void onPreExecute(GenericTask task) {
        }

    };

    private final TaskListener retrieveMessageByIdTaskListener = new TaskAdapter() {


        public void onPostExecute(GenericTask task, TaskResult result) {
            RetrieveMessageTask retrieveMessageTask = (RetrieveMessageTask) task;
            if (result == TaskResult.OK) {
                Message message = retrieveMessageTask.getMessage();

                CommonUtilities.messageNotification(App.mContext, message);
            } else {
                String msg = retrieveMessageTask.getMsg();
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "retrieveMessageTask:" + msg);
            }
        }


        public void onPreExecute(GenericTask task) {
        }

    };

    private final TaskListener retrieveQaByIdTaskListener = new TaskAdapter() {


        public void onPostExecute(GenericTask task, TaskResult result) {
            RetrieveQaByIdTask retrieveQaByIdTask = (RetrieveQaByIdTask) task;
            if (result == TaskResult.OK) {
                Map<String, String> qa = retrieveQaByIdTask.getQa();

                // notifies user
                String message = qa.get("answer");
                String title = GCMIntentService.getQATitle();
                MessageReceiver.notify(App.mContext, title, message,
                        R.string.qa, -1, true);

            } else {
                String msg = retrieveQaByIdTask.getMsg();
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "retrieveQaByIdTask:" + msg);
            }
        }


        public void onPreExecute(GenericTask task) {
        }

    };

    /**
     * 调用PushManager.startWork后，sdk将对push
     * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
     * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     *
     * @param context   BroadcastReceiver的执行Context
     * @param errorCode 绑定接口返回值，0 - 成功
     * @param appid     应用id。errorCode非0时为null
     * @param userId    应用user id。errorCode非0时为null
     * @param channelId 应用channel id。errorCode非0时为null
     * @param requestId 向服务端发起的请求id。在追查问题时有用；
     * @return none
     */

    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        Log.d(TAG, responseString);
        // 绑定成功，设置已绑定flag，可以有效的减少不必要的绑定请求
        if (errorCode == 0) {
            mUserlId = userId;
            registered = true;
//            ServerUtilities.registerBaiduPushOnServer(context);
        }
    }

    /**
     * delTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param failTags  删除失败的tag
     * @param requestId 分配给对云推送的请求的id
     */

    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
    }

    /**
     * listTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示列举tag成功；非0表示失败。
     * @param tags      当前应用设置的所有tag。
     * @param requestId 分配给对云推送的请求的id
     */

    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {
    }

    /**
     * 接收透传消息的函数。
     *
     * @param context             上下文
     * @param message             推送的消息
     * @param customContentString 自定义内容,为空或者json字符串
     */

    public void onMessage(Context context, String message,
                          String customContentString) {
        String messageString = "baidu push: message=\"" + message
                + "\" customContentString=" + customContentString;
        Log.d(TAG, messageString);
        User readUser = App.readUser();
        if (readUser == null)
            return;

        if (!TextUtils.isEmpty(message)) {
            try {
                JSONObject json = new JSONObject(message);

                String title = json.getString("title");
                if ("announcement".equals(title)) {
                    long id = Long.valueOf(json.getString("id"));
                    baiduReceiverTask = new RetrieveAnnouncementByIdTask(id);
                    baiduReceiverTask
                            .setListener(retrieveAnnouncementByIdTaskListener);
                    baiduReceiverTask.execute();
                } else if ("qa".equals(title)) {
                    long id = Long.valueOf(json.getString("id"));
                    baiduReceiverTask = new RetrieveQaByIdTask(id);
                    baiduReceiverTask.setListener(retrieveQaByIdTaskListener);
                    baiduReceiverTask.execute();
                } else if ("friend".equals(title)) {
                    FriendsRequestReceiver.doRetrieveNewFriend(
                            readUser.getId(), false);
                } else if ("message".equals(title)) {
                    long id = Long.valueOf(json.getString("id"));
                    baiduReceiverTask = new RetrieveMessageTask(id);
                    baiduReceiverTask
                            .setListener(retrieveMessageByIdTaskListener);
                    baiduReceiverTask.execute();
                } else if ("balance".equals(title)) {
                    // balance消息，长度固定，所以不需要重新执行RetrieveUserTask
                    String content = json.optString("content");
                    String body = json.optString("body");
                    if (!Utils.isEmpty(content)) {
                        double balance = Double.valueOf(content);
                        readUser.setBalance(balance);
                        App.writeUser(readUser);
                        CommonUtilities.broadcastBalance(context);
                    }
                    if (!Utils.isEmpty(body)) {
                        String notificationTitle = context
                                .getString(R.string.recharge_process_finished);
                        MessageReceiver.notify(App.mContext, notificationTitle,
                                body, R.string.balance, -1, true);
                    }
                } else if ("story_new_comment".equals(title)) {
                    String fullname = json.optString("fullname");
                    if (!Utils.isEmpty(fullname)) {
                        String photoId = json.getString("id");
                        String notificationTitle = context
                                .getString(R.string.new_story_comment);

                        String comment_fullname = String
                                .format(GCMIntentService.PUSH_TITLE_PATTERN,
                                        fullname,
                                        context.getString(R.string.push_title_story_new_comment));

                        UserStoryReceiver.doRetrieveNewComment(
                                Long.valueOf(photoId), notificationTitle,
                                comment_fullname,
                                PrefUtils.getPrefTranslatedNoticeComment());
                    }
                } else if ("story_like".equals(title)) {
                    String photoId = json.getString("id");
                    String notificationTitle = context
                            .getString(R.string.new_story_like);
                    String fullname = String.format(
                            GCMIntentService.PUSH_TITLE_PATTERN,
                            json.getString("fullname"),
                            context.getString(R.string.push_title_story_like));

                    UserStoryReceiver.doRetrieveNewComment(
                            Long.valueOf(photoId), notificationTitle, fullname,
                            PrefUtils.getPrefTranslatedNoticeLike());
                } else if ("story_new".equals(title)) {
                    String photoId = json.getString("id");
                    String notificationTitle = context
                            .getString(R.string.new_story);
                    String temp = json.optString("content");
                    if (!Utils.isEmpty(temp))
                        notificationTitle = temp;
                    String fullname = String.format(
                            GCMIntentService.PUSH_TITLE_PATTERN,
                            json.getString("fullname"),
                            context.getString(R.string.push_title_story_new));

                    UserStoryReceiver.doRetrieveNewComment(
                            Long.valueOf(photoId), notificationTitle, fullname,
                            PrefUtils.getPrefTranslatedNoticeFriend());
                } else if ("story_new_translate".equals(title)) {
                    String photoId = json.getString("id");
                    String content = json.optString("content");
                    String fullname = String
                            .format(GCMIntentService.PUSH_TITLE_PATTERN,
                                    json.getString("fullname"),
                                    context.getString(R.string.push_title_story_new_translate));
                    UserStoryReceiver.doRetrieveNewTranslate(
                            Long.valueOf(photoId), content, fullname,
                            PrefUtils.getPrefTranslatedNoticeTranslate());
                } else if ("story_like_translate".equals(title)) {
                    String photoId = json.getString("id");
                    String content = json.optString("content");
                    String fullname = String
                            .format(GCMIntentService.PUSH_TITLE_PATTERN,
                                    json.getString("fullname"),
                                    context.getString(R.string.push_title_tranlate_like));
                    UserStoryReceiver.doRetrieveNewTranslate(
                            Long.valueOf(photoId), content, fullname,
                            PrefUtils.getPrefTranslatedNoticeLike());
                } else if ("story_new_comment_reply".equals(title)) {
                    String photoId = json.getString("id");
                    String notificationTitle = context
                            .getString(R.string.new_story_comment_reply);
                    String temp = json.optString("content");
                    String fullname = String
                            .format(GCMIntentService.PUSH_TITLE_PATTERN,
                                    json.getString("fullname"),
                                    context.getString(R.string.push_title_story_new_comment_reply));

                    if (!Utils.isEmpty(temp))
                        notificationTitle = temp;

                    UserStoryReceiver.doRetrieveNewComment(
                            Long.valueOf(photoId), notificationTitle, fullname,
                            PrefUtils.getPrefTranslatedNoticeReply());
                } else if ("present_donate".equals(title)) {
                    String photoId = json.optString("to_user_photo_id");
                    String notificationContent = context
                            .getString(R.string.news_title_gift_donate);
                    String notificationTitle = String
                            .format(GCMIntentService.PUSH_TITLE_PATTERN,
                                    json.getString("fullname"),
                                    context.getString(R.string.push_title_present_donate));

                    if (!Utils.isEmpty(photoId) && Integer.valueOf(photoId) > 0) {
                        PresentDonateReceiver.doRetrieveUserPhoto(
                                Long.valueOf(photoId), notificationContent,
                                notificationTitle, json.optLong("id"),
                                json.optString("pic_url"),
                                PrefUtils.getPrefTranslatedNoticeLike());
                    } else {
                        PresentDonateReceiver.sendPresentDonteNotice(context,
                                notificationContent, notificationTitle,
                                json.optLong("id"), json.optString("pic_url"),
                                null, PrefUtils.getPrefTranslatedNoticeLike());
                    }
                } else if ("delete".equals(title)) {// 来自服务器端的删除指令
                    String id = json.getString("id");
                    if (Long.valueOf(id) == readUser.getId()) {
                        Utils.doLogout(App.mContext);
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                Utils.sendClientException(e);
            }
        }
    }

    /**
     * 接收通知点击的函数。注：推送通知被用户点击前，应用无法通过接口获取通知的内容。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */

    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
    }

    /**
     * setTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param failTags  设置失败的tag
     * @param requestId 分配给对云推送的请求的id
     */

    public void onSetTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
    }

    /**
     * PushManager.stopWork() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId 分配给对云推送的请求的id
     */

    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        Log.d(TAG, responseString);

        // 解绑定成功，设置未绑定flag，
        if (errorCode == 0) {
            registered = false;
        }
    }
}