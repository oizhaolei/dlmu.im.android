package com.ruptech.chinatalk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.story.UserStoryGiftListActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 *
 * @author Administrator
 *
 */
public class PresentDonateReceiver {
	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[2 * 1024];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	public static void doRetrieveUserPhoto(final long photoId,
			final String notificationTitle, final String comment_fullname,
			final long presentId, final String presentPicUrl,
			final boolean isSound) {
		RetrieveUserPhotoTask retrieveUserPhotoTask = new RetrieveUserPhotoTask(
				photoId, App.readUser().lang);
		retrieveUserPhotoTask.setListener(new TaskAdapter() {

			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
				if (result == TaskResult.OK) {
					UserPhoto userPhoto = retrieveUserPhotoTask.getUserPhoto();

					sendPresentDonteNotice(App.mContext, notificationTitle,
							comment_fullname, presentId, presentPicUrl,
							userPhoto, isSound);
				}
			}
		});
		retrieveUserPhotoTask.execute();
	}

	private static Bitmap getPresentLocalOrNetBitmap(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			String photoUri = App.readServerAppInfo().getServerPresent(url);
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

	private static boolean isPresentDonateNotification(Context context,
			String title) {
		if (title != null) {
			String pattern = context.getString(R.string.present);
			if (title.contains(pattern)) {
				return true;
			}
		}

		return false;
	}

	public static void sendPresentDonteNotice(final Context context,
			final String content, final String title, final long presentId,
			final String presentPicUrl, final UserPhoto userPhoto,
			final boolean isSound) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				String newTitle = title;
				int notificationId = R.layout.activity_gift_list;
				int notiId = notificationId + (int) presentId;
				if (isPresentDonateNotification(context, title)) {
					count_of_gift_notification++;
					if (count_of_gift_notification > 1)
						newTitle = String.format("[%d] %s",
								count_of_gift_notification, title);

					notificationId = R.string.present;
					notiId = notificationId;
				}

				Bitmap pic_url = getPresentLocalOrNetBitmap(presentPicUrl);

				Intent notificationIntent = null;
				if (userPhoto != null) {
					notificationIntent = new Intent(context,
							UserStoryCommentActivity.class);
					notificationIntent.putExtra(
							UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);
				} else {
					notificationIntent = new Intent(context,
							UserStoryGiftListActivity.class);
					notificationIntent.putExtra(ProfileActivity.EXTRA_USER_ID,
							App.readUser().getId());
				}

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

	static final String TAG = Utils.CATEGORY
			+ PresentDonateReceiver.class.getSimpleName();

	public static int count_of_gift_notification = 0;

}