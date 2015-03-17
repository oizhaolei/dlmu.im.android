package com.ruptech.chinatalk;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.MessageProvider;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveNewMessagesTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.TTTActivity;
import com.ruptech.chinatalk.ui.setting.SettingAnnouncementActivity;
import com.ruptech.chinatalk.ui.setting.SettingQaActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 */
@Deprecated
public class MessageReceiver {

	static final String TAG = Utils.CATEGORY
			+ MessageReceiver.class.getSimpleName();

	private static GenericTask mRetrieveNewMessagesTask;

	public static NotificationCompat.Builder createNormalNotificationBuilder(
			Context context, String title, String content, Bitmap icon,
			boolean isSound) {

		if (Utils.isEmpty(title)) {
			title = context.getString(R.string.app_name);
		}

		int defaults = Notification.DEFAULT_LIGHTS;
		if (isSound) {
			defaults |= Notification.DEFAULT_SOUND;
		}
		long when = System.currentTimeMillis();
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setContentTitle(title)
				.setSmallIcon(R.drawable.ic_tttalk_gray_light).setWhen(when)
				.setDefaults(defaults).setAutoCancel(true)
				.setContentText(content);

		if (icon == null) {
			mBuilder.setLargeIcon(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ic_launcher));
		} else {
			mBuilder.setLargeIcon(icon);
		}

		return mBuilder;
	}

	public static MyNotificationBuilder createNotificationBuilder(
			Context context, String title, String content, Bitmap icon,
			boolean isSound) {

		MyNotificationBuilder mBuilder = new MyNotificationBuilder(context,
				isSound, title, content, icon);

		return mBuilder;
	}

	private static boolean displayMessageNotification(Context context,
	                                                  Message message, long userId) {

		String title = null;
		String content = null;
		User user = null;
		int status = message.getMessage_status();
		if (isMessageStatusEnd(status)) {
			if (AppPreferences.MESSAGE_STATUS_GIVEUP == status) {
				if (message.getUserid() == userId) {
					content = message.getStatus_text();
				}
			} else if (AppPreferences.MESSAGE_STATUS_TRANSLATED == status) {
				content = message.getTo_content();
			} else if (AppPreferences.MESSAGE_STATUS_NO_TRANSLATE == status) {
				if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
						.equals(message.file_type)) {
					content = context.getString(R.string.notification_picture);
				} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
						.equals(message.file_type)) {
					content = context.getString(R.string.notification_voice);
				} else if (message.getFrom_lang().equals(message.getTo_lang())) {
					content = message.getFrom_content();
				} else {
					content = message.getStatus_text();
				}
			}
			if (Utils.isEmpty(content))
				return false;

			if (message.getTo_userid() == 0) {
				title = context.getString(R.string.translation_secretary);
			} else {
				user = App.userDAO.fetchUser(message.getUserid());
				if (user != null) {
					String name = Utils.getFriendName(message.getUserid(),
							user.getFullname());
					title = String.format(GCMIntentService.PUSH_TITLE_PATTERN,
							name,
							context.getString(R.string.push_title_message));
				} else {
					return false;// user不存在就不通知了
				}
			}

			// 新消息个数
			// PrefUtils.savePrefNewMessageCount(message.userid,
			// PrefUtils.getPrefNewMessageCount(message.userid) + 1);
			int notiId;
			long messageId = -1;
			if (message.getTo_userid() == AppPreferences.STORY_REQUEST_TO_USERID) {
				notiId = Long.valueOf(message.getTo_userid()).intValue();
				messageId = message.getMessageid();
			} else if (message.getUserid() == userId) {
				notiId = Long.valueOf(message.getTo_userid()).intValue();
				if (notiId == AppPreferences.TTT_REQUEST_TO_USERID) {// 翻译秘书
					messageId = message.getId();
				}
			} else {
				notiId = Long.valueOf(message.getUserid()).intValue();
				App.mBadgeCount.addNewMessageCount(message.userid);
			}
			// 如果选择消息通知，处理
			notify(context, title, content, notiId, messageId,
					PrefUtils.getPrefTranslatedNoticeMessage());

			// 如果选择TTS播放，处理
			if (PrefUtils.getPrefTranslatedNoticeTts()) {
				// TTS
				if (!Utils.tts(context, message.getTo_lang(),
						message.getFrom_lang(), content)) {
					Toast.makeText(
							context,
							context.getString(R.string.tts_no_supported_language),
							Toast.LENGTH_SHORT).show();
				}
			}

			// 刷新chat
			CommonUtilities.broadcastChatList(context);
			return true;
		}
		return false;
	}

	public static void doRetrieveNewMessage(final Context context,
	                                        final long userId, final boolean dispNotification) {
		if (mRetrieveNewMessagesTask != null
				&& mRetrieveNewMessagesTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveNewMessagesTask = new RetrieveNewMessagesTask(userId);

		mRetrieveNewMessagesTask.setListener(new TaskAdapter() {

			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				RetrieveNewMessagesTask retrieveMessageTask = (RetrieveNewMessagesTask) task;
				List<Message> newMessageList = retrieveMessageTask
						.getMessageList();
				long userId = retrieveMessageTask.getUserId();
				boolean existTranslatedMessage = false;

				if (newMessageList != null && newMessageList.size() > 0) {
					boolean noticeFlag = false;
					for (Message message : newMessageList) {
						if (AppPreferences.MESSAGE_STATUS_TRANSLATED == message
								.getMessage_status()) {
							existTranslatedMessage = true;
						}

						// 显示通知的判断
						if (!noticeFlag
								&& dispNotification
								|| message.getUserid() == AppPreferences.SYSTEM_REQUEST_TO_USERID) {
							noticeFlag = MessageReceiver.messageCommonAction(
									context, message);
						} else {
							App.messageDAO.mergeMessage(message);
						}
					}
//                    if (AbstractChatActivity.instance != null) {
//                        // AbstractChatActivity.doRefresh();
//                        CommonUtilities.broadcastMessage(context, null);
//                    }

					if (existTranslatedMessage && isPushRegistered()) {
						doRetrieveUser(userId);
					}
				}
			}

		});
		mRetrieveNewMessagesTask.execute();
	}

	private static void doRetrieveUser(long userId) {

		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(userId);
		mRetrieveUserTask.execute();
	}

	// 推送的TTT message lang与当前TTT画面语言是否不一致。
	private static boolean isDifferentTTTLang(String lang, String lang2) {
		if ((lang.equals(PrefUtils.getPrefTTTLastSelectedLang(1)) || lang
				.equals(PrefUtils.getPrefTTTLastSelectedLang(2)))
				&& (lang2.equals(PrefUtils.getPrefTTTLastSelectedLang(1)) || lang2
				.equals(PrefUtils.getPrefTTTLastSelectedLang(2)))) {
			return false;
		}
		return true;
	}

	public static boolean isMessageStatusEnd(int message_status) {
		return AppPreferences.MESSAGE_STATUS_TRANSLATED == message_status
				|| AppPreferences.MESSAGE_STATUS_NO_TRANSLATE == message_status
				|| AppPreferences.MESSAGE_STATUS_GIVEUP == message_status;
	}

	public static boolean isNowAvailableNotifiy() {
		if (PrefUtils.getPrefTranslatedNoticeInterruptSwitch()) {
			if (isNowInNotInterruptTimeSetting())
				return false;
			else
				return true;
		} else {
			return true;
		}
	}

	private static boolean isNowInNotInterruptTimeSetting() {
		int duration = PrefUtils.getPrefTranslatedNoticeInterruptDuration();
		if (duration == 0)
			return false;
		else if (duration == 24)
			return true;
		else {
			int startHour = PrefUtils
					.getPrefTranslatedNoticeInterruptStartHour();
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("kk");
			String formattedTime = sdf.format(now);
			int hour = Integer.parseInt(formattedTime);

			return startHour <= hour && hour < (startHour + duration);

		}

	}

	private static boolean isPushRegistered() {
//		return (GCMRegistrar.isRegistered(App.mContext) && GCMRegistrar
//				.isRegisteredOnServer(App.mContext))
//				|| (BaiduPushMessageReceiver.isRegistered() && BaiduPushMessageReceiver
//						.isRegisteredOnServer());
		return true;
	}

	// 新message的共通处理
	public static boolean messageCommonAction(Context context, Message message) {
		Message localMessage = App.messageDAO.fetchMessage(message.getId());
		if (localMessage != null
				&& isMessageStatusEnd(localMessage.getMessage_status())) {
			// 翻译完毕或者不翻译或者放弃不进行处理

			return false;
		} else if (PrefUtils.existsMessage(message)) {
			return false;
		} else {
//            App.messageDAO.mergeMessage(message);
            MessageProvider.mergeMessage(App.mContext.getContentResolver(),message);

            // 推送的TTT message lang与当前TTT画面语言不一致也需要提醒。
            int message_status = message.getMessage_status();
            if (message.getTo_userid() == AppPreferences.TTT_REQUEST_TO_USERID && isDifferentTTTLang(
                    message.getFrom_lang(), message.getTo_lang())
                    && isMessageStatusEnd(message_status)) {
                PrefUtils.writeMessageList(message);
                return MessageReceiver.displayMessageNotification(context,
                        message, App.readUser().getId());
            } else {
                // 如果和这个人正在聊天，并且消息是自动翻译，但是本地余额足的时候刷新下这个人的信息
//                if ((message.getTo_userid() == App.readUser().getId() && message
//                        .getUserid() != AppPreferences.TTT_REQUEST_TO_USERID)
//                        && message.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
//                    User friendUser = App.userDAO.fetchUser(message.userid);
//                    if (friendUser.getBalance() > AppPreferences.MINI_BALANCE) {
//                        doRetrieveUser(message.getUserid());
//                    }
//                }
            }
			return false;
		}
	}

	public static void notify(Context context, String title, String content,
	                          int notiId, long messageId, boolean isSound) {

		Intent notificationIntent = null;
		switch (notiId) {
			case R.string.balance:
				notificationIntent = new Intent(context, MyWalletActivity.class);
				break;
			case R.string.qa:
				notificationIntent = new Intent(context, SettingQaActivity.class);
				break;
			case R.string.announcement:
				notificationIntent = new Intent(context,
						SettingAnnouncementActivity.class);
				break;
			case (int) AppPreferences.TTT_REQUEST_TO_USERID:
				Message message = App.messageDAO.fetchMessage(messageId);
				if (message != null) {
					PrefUtils
							.savePrefTTTLastSelectedLang(1, message.getFrom_lang());
					PrefUtils.savePrefTTTLastSelectedLang(2, message.getTo_lang());

				}
				notificationIntent = new Intent(context, TTTActivity.class);
				break;
			default:
				User user = App.userDAO.fetchUser(Long.valueOf(notiId));
				if (user != null) {
					notificationIntent = new Intent(context, ChatActivity.class);
					notificationIntent.putExtra(ChatActivity.EXTRA_JID, user.getOF_JabberID());
				}
		}

		if (notificationIntent == null)
			return;

		PendingIntent contentIntent = PendingIntent.getActivity(context,
				notiId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		MyNotificationBuilder mBuilder = MessageReceiver
				.createNotificationBuilder(context, title, content, null,
						isSound);
		mBuilder.setTicker(content);
		mBuilder.setContentIntent(contentIntent);

		if (title.equalsIgnoreCase(GCMIntentService.getQATitle())
				|| title.equalsIgnoreCase(GCMIntentService
				.getAnnouncementTitle()))
			mBuilder.setShowSetting(false);

		App.notificationManager.cancel(notiId);
		App.notificationManager.notify(notiId, mBuilder.build());
	}
}