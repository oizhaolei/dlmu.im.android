package com.ruptech.chinatalk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.story.UserStoryListActivity;
import com.ruptech.chinatalk.ui.story.UserStorySaveActivity;
import com.ruptech.chinatalk.ui.story.UserStorySaveActivity.UploadStatus;
import com.ruptech.chinatalk.ui.story.UserStoryTranslateActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

/**
 *
 * @author Administrator
 *
 */
public class UserStoryReceiver {
	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[2 * 1024];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	public static void doRetrieveNewComment(final long photoId,
			final String notificationTitle, final String comment_fullname,
			final boolean isSound) {
		RetrieveUserPhotoTask retrieveUserPhotoTask = new RetrieveUserPhotoTask(
				photoId, App.readUser().lang);
		retrieveUserPhotoTask.setListener(new TaskAdapter() {

			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
				if (result == TaskResult.OK) {
					UserPhoto userPhoto = retrieveUserPhotoTask.getUserPhoto();

					sendStoryPhotoCommentNotice(App.mContext,
							notificationTitle, comment_fullname, userPhoto,
							isSound);
				}
			}
		});
		retrieveUserPhotoTask.execute();
	}

	public static void doRetrieveNewTranslate(final long photoId,
			final String content, final String fullname, final boolean isSound) {
		RetrieveUserPhotoTask retrieveUserPhotoTask = new RetrieveUserPhotoTask(
				photoId, App.readUser().lang);
		retrieveUserPhotoTask.setListener(new TaskAdapter() {

			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
				if (result == TaskResult.OK) {
					UserPhoto userPhoto = retrieveUserPhotoTask.getUserPhoto();

					sendStoryTranslateNotice(App.mContext, content, userPhoto,
							fullname, isSound);
				}
			}
		});
		retrieveUserPhotoTask.execute();
	}

	private static Bitmap getLocalOrNetBitmap(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			String photoUri = App.readServerAppInfo().getServerMiddle(url);
			in = new BufferedInputStream(new URL(photoUri).openStream(),
					2 * 1024);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, 2 * 1024);
			copy(in, out);
			out.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean isStoryNewNotification(Context context, String title) {
		if (title != null) {
			String pattern = context.getString(R.string.push_title_story_new);
			if (title.contains(pattern)) {
				return true;
			}
		}

		return false;
	}

	public static void sendStoryPhotoCommentNotice(final Context context,
			final String content, final String title,
			final UserPhoto userPhoto, final boolean isSound) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				String newTitle = title;
				int notificationId = R.layout.activity_story_comment_list;
				int notiId = notificationId + (int) userPhoto.getId();
				if (isStoryNewNotification(context, title)) {
					count_of_story_new_notification++;
					if (count_of_story_new_notification > 1)
						newTitle = String.format("[%d] %s",
								count_of_story_new_notification, title);

					notificationId = R.string.push_title_story_new;
					notiId = notificationId;
				}

				Bitmap pic_url = getLocalOrNetBitmap(userPhoto.getPic_url());

				Intent notificationIntent = new Intent(context,
						UserStoryCommentActivity.class);
				notificationIntent.putExtra(
						UserStoryCommentActivity.EXTRA_USER_PHOTO, userPhoto);
				notificationIntent.putExtra(
						UserStoryCommentActivity.EXTRA_IS_NOTIFICATION, true);

				PendingIntent contentIntent = PendingIntent.getActivity(
						context, notiId, notificationIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				NotificationCompat.Builder mBuilder = MessageReceiver
						.createNotificationBuilder(context, newTitle, content,
								pic_url, isSound);
				mBuilder.setTicker(title);
				mBuilder.setContentIntent(contentIntent);
				App.notificationManager.cancel(notiId);
				App.notificationManager.notify(notiId, mBuilder.build());
			}
		}).start();

	}

	public static Notification sendStoryPhotoNotice(Context context,
			Builder mBuilder, UploadStatus uploadStatus) {

		final int iconRes = com.ruptech.chinatalk.R.drawable.ic_tttalk_gray_light;
		long when = System.currentTimeMillis();

		Intent notificationIntent = new Intent();
		if (uploadStatus == UploadStatus.Upload_Fail) {
			notificationIntent = new Intent(context,
					UserStorySaveActivity.class);
			notificationIntent
					.setAction(UserStorySaveActivity.ACTION_RE_UPLOAD);

			mBuilder.setTicker(context.getString(R.string.uplaod_photo_failure));
			mBuilder.setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
			mBuilder.setContentText(context
					.getString(R.string.uplaod_photo_failure));
			mBuilder.setProgress(0, 0, false);

		} else if (uploadStatus == UploadStatus.Upload_Success) {
			notificationIntent = new Intent(context,
					UserStoryListActivity.class);
			notificationIntent.putExtra(ProfileActivity.EXTRA_USER_ID, App
					.readUser().getId());
			String name = App.readUser().getFullname();
			notificationIntent.putExtra(UserStoryListActivity.EXTRA_TITLE, name
					+ "-" + context.getString(R.string.popular));
			notificationIntent.putExtra(
					AbstractUserStoryListActivity.EXTRA_STORY_TYPE,
					AbstractUserStoryListActivity.STORY_TYPE_USER);

			mBuilder.setTicker(context.getString(R.string.picture_send_success));
			mBuilder.setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
			mBuilder.setContentText(context
					.getString(R.string.picture_send_success));
			mBuilder.setProgress(0, 0, false);
		} else {

		}

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setWhen(when);
		mBuilder.setAutoCancel(true);

		mBuilder.setContentIntent(resultPendingIntent);

		Notification notif = mBuilder.build();
		App.notificationManager.notify(iconRes, notif);
		// close after 60 seconds
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				App.notificationManager.cancel(iconRes);
			}
		}, 1000 * 60);

		return notif;
	}

	public static void sendStoryTranslateNotice(Context context,
			String content, UserPhoto photo, String title, boolean isSound) {

		int notiId = R.layout.activity_story_translate_list;

		Intent notificationIntent = new Intent(context,
				UserStoryTranslateActivity.class);
		notificationIntent.putExtra(
				UserStoryTranslateActivity.EXTRA_USER_PHOTO, photo);

		PendingIntent contentIntent = PendingIntent.getActivity(context,
				notiId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = MessageReceiver
				.createNotificationBuilder(context, title, content, null,
						isSound);
		mBuilder.setTicker(title);
		mBuilder.setContentIntent(contentIntent);
		App.notificationManager.cancel(notiId);
		App.notificationManager.notify(notiId, mBuilder.build());
	}

	static final String TAG = Utils.CATEGORY
			+ UserStoryReceiver.class.getSimpleName();

	public static int count_of_story_new_notification = 0;

}