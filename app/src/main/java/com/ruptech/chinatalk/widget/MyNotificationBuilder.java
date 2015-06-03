package com.ruptech.chinatalk.widget;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

public class MyNotificationBuilder extends NotificationCompat.Builder {
    private final Context mContext;
    private RemoteViews contentView;

    public MyNotificationBuilder(Context context) {
        super(context);
        mContext = context;
        createRemoteViews(context, "", "");
        setContent(contentView);
        setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
    }

    public MyNotificationBuilder(Context context, String title, String content, Bitmap icon) {
        super(context);
        mContext = context;
        int defaults = Notification.DEFAULT_LIGHTS;
        defaults |= Notification.DEFAULT_SOUND;
        setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
        if (Utils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }
        createRemoteViews(context, title, content);

        setContent(contentView);
        setDefaults(defaults);
        setSmallIcon(R.drawable.tt_logo2);
        setAutoCancel(true);

        if (icon == null) {
            setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.tt_logo2));
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
    public MyNotificationBuilder setWhen(long when) {
        String time = (String) android.text.format.DateFormat.format("kk:mm",
                new java.util.Date(when));
        contentView.setTextViewText(R.id.time, time);
        return this;
    }

}
