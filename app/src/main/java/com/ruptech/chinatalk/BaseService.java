package com.ruptech.chinatalk;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;
import com.ruptech.dlmu.im.R;

public abstract class BaseService extends Service {

    private static final String TAG = "BaseService";
    protected WakeLock mWakeLock;

    public static MyNotificationBuilder createNotificationBuilder(
            Context context, String title, String content, Bitmap icon) {

        MyNotificationBuilder mBuilder = new MyNotificationBuilder(context, title, content, icon);

        return mBuilder;
    }

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

    private String getMessageTitle(String fromJid) {
        String title;
        String name = fromJid;
        title = name;
        return title;
    }

    public void displayMessageNotification(NewChatEvent event) {

        String title;
        String fromJid = event.fromJID;
        String content = event.chatMessage;


        if (Utils.isEmpty(content))
            return;

        title = getMessageTitle(fromJid);

        int notiId = fromJid.hashCode();

        Intent notificationIntent;
        notificationIntent = new Intent(this, ChatActivity.class);
        notificationIntent.putExtra(ChatActivity.EXTRA_JID, fromJid);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                notiId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        MyNotificationBuilder mBuilder = createNotificationBuilder(this, title, content, null);
        mBuilder.setTicker(content);
        mBuilder.setContentIntent(contentIntent);

        App.notificationManager.cancel(notiId);
        App.notificationManager.notify(notiId, mBuilder.build());
    }
}
