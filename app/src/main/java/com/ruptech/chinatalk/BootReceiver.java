package com.ruptech.chinatalk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.event.NetChangeEvent;
import com.ruptech.chinatalk.utils.NetUtil;
import com.ruptech.chinatalk.utils.Utils;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = Utils.CATEGORY
			+ BootReceiver.class.getSimpleName();
    public static final String BOOT_COMPLETED_ACTION = "com.ruptech.tttalk.action.BOOT_COMPLETED";
    private PendingIntent versionCheckPendingIntent;

    @Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action = " + action);

        cancelVersionCheck(context);
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            int connectivity = NetUtil.getNetworkState(context);
            App.mBus.post(new NetChangeEvent(connectivity));


            if (connectivity != NetUtil.NETWORK_NONE) {
                //version check
                startVersionCheck(context);
            }
        } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            Log.d(TAG, "System shutdown, stopping service.");
            Intent xmppServiceIntent = new Intent(context, XMPPService.class);
            context.stopService(xmppServiceIntent);

        } else {
            Intent i = new Intent(context, XMPPService.class);
            i.setAction(BOOT_COMPLETED_ACTION);
            context.startService(i);

            startVersionCheck(context);
        }
	}

    private void startVersionCheck(Context context) {
        //version check
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        Intent versionCheckIntent = new Intent(context, VersionCheckReceiver.class);
//        versionCheckPendingIntent = PendingIntent.getBroadcast(context, 0, versionCheckIntent, 0);
//        alarmManager.setRepeating(AlarmManager.RTC, 0, 60 * 60 * 1000, versionCheckPendingIntent);
    }

    private void cancelVersionCheck(Context context) {
        if (versionCheckPendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(versionCheckPendingIntent);
        }
    }
}