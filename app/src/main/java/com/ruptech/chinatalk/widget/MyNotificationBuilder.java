package com.ruptech.chinatalk.widget;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.NotificationSettingReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class MyNotificationBuilder extends NotificationCompat.Builder {

	private static PendingIntent pendingIntent;
	private RemoteViews contentView;
	private final Context mContext;
	private static final long MIN_SOUND_INTERVAL = 5000; // 5000ms
	public static long lastSoundTime = 0;

	public MyNotificationBuilder(Context context) {
		super(context);
		mContext = context;
		createRemoteViews(context, "", "");
		setContent(contentView);
		setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
	}

	public MyNotificationBuilder(Context context, boolean isSound,
			String title, String content, Bitmap icon) {
		super(context);
		mContext = context;
		int defaults = Notification.DEFAULT_LIGHTS;

		if (isSound && soundTimeDiff() > MIN_SOUND_INTERVAL) {
			defaults |= Notification.DEFAULT_SOUND;
			setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
		} else {
			defaults &= ~Notification.DEFAULT_SOUND;
			setVibrate(null);
		}
		if (Utils.isEmpty(title)) {
			title = context.getString(R.string.app_name);
		}
		createRemoteViews(context, title, content);

		setContent(contentView);
		setDefaults(defaults);
		setSmallIcon(R.drawable.ic_tttalk_gray_light);
		setAutoCancel(true);

		if (icon == null) {
			setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_launcher));
		} else {
			setLargeIcon(icon);
		}
	}

	private RemoteViews createRemoteViews(Context context, String title,
			String text) {
		contentView = new RemoteViews(context.getPackageName(),
				R.layout.item_notification);
		contentView.setTextViewText(R.id.item_notification_title, title);
		contentView.setTextViewText(R.id.item_notification_text, text);

		String time = (String) android.text.format.DateFormat.format("kk:mm",
				new java.util.Date(System.currentTimeMillis()));
		contentView.setTextViewText(R.id.item_notification_time, time);

		setShowSetting(true);
		return contentView;
	}

	@Override
	public MyNotificationBuilder setAutoCancel(boolean autoCancel) {
		super.setAutoCancel(autoCancel);
		return this;
	}

	@Override
	public MyNotificationBuilder setContent(RemoteViews views) {
		if (views != null) {
			super.setContent(views);
		}
		return this;
	}

	@Override
	public MyNotificationBuilder setContentIntent(PendingIntent intent) {
		super.setContentIntent(intent);
		return this;
	}

	@Override
	public MyNotificationBuilder setContentText(CharSequence text) {
		contentView.setTextViewText(R.id.item_notification_text, text);
		return this;
	}

	@Override
	public MyNotificationBuilder setContentTitle(CharSequence title) {
		contentView.setTextViewText(R.id.item_notification_title, title);
		return this;
	}

	@Override
	public MyNotificationBuilder setDefaults(int defaults) {
		if (!MessageReceiver.isNowAvailableNotifiy())
			defaults &= ~Notification.DEFAULT_SOUND;

		super.setDefaults(defaults);
		return this;
	}

	@Override
	public MyNotificationBuilder setLargeIcon(Bitmap icon) {
		contentView.setImageViewBitmap(R.id.item_notification_icon, icon);
		return this;
	}

	@Override
	public MyNotificationBuilder setProgress(int max, int progress,
			boolean indeterminate) {
		if (max == 0) {
			contentView.setViewVisibility(R.id.item_notification_progressBar,
					View.GONE);
		} else {
			contentView.setViewVisibility(R.id.item_notification_progressBar,
					View.VISIBLE);
			contentView.setProgressBar(R.id.item_notification_progressBar, max,
					progress, indeterminate);
		}
		return this;
	}

	public MyNotificationBuilder setShowSetting(boolean isShow) {
		if (pendingIntent == null) {
			Intent intent = new Intent(mContext,
					NotificationSettingReceiver.class);

			pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
		}
		if (isShow) {
			contentView.setViewVisibility(R.id.item_notification_setting,
					View.VISIBLE);
			contentView.setOnClickPendingIntent(R.id.item_notification_setting,
					pendingIntent);
		} else {
			contentView.setViewVisibility(R.id.item_notification_setting,
					View.INVISIBLE);
		}
		return this;
	}

	@Override
	public MyNotificationBuilder setSmallIcon(int icon) {
		super.setSmallIcon(icon);
		return this;
	}

	@Override
	public MyNotificationBuilder setTicker(CharSequence ticker) {
		super.setTicker(ticker);
		return this;
	}

	@Override
	public MyNotificationBuilder setVibrate(long[] pattern) {
		if (MessageReceiver.isNowAvailableNotifiy() && pattern != null
				&& soundTimeDiff() > MIN_SOUND_INTERVAL) {
			super.setVibrate(pattern);
			lastSoundTime = System.currentTimeMillis();
		} else
			super.setVibrate(null);
		return this;
	}

	@Override
	public MyNotificationBuilder setWhen(long when) {
		String time = (String) android.text.format.DateFormat.format("kk:mm",
				new java.util.Date(when));
		contentView.setTextViewText(R.id.time, time);
		return this;
	}

	private long soundTimeDiff() {
		return (System.currentTimeMillis() - lastSoundTime);
	}

}
