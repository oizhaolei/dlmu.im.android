package com.ruptech.chinatalk;

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
    public static final String BOOT_COMPLETED_ACTION = "com.ruptech.tttalk.action.BOOT_COMPLETED";
    private static final String TAG = Utils.CATEGORY
            + BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "action = " + action);

        if (App.readUser() != null) {
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                int connectivity = NetUtil.getNetworkState(context);
                App.mBus.post(new NetChangeEvent(connectivity));

            } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Log.d(TAG, "System shutdown, stopping service.");
                Intent xmppServiceIntent = new Intent(context, XMPPService.class);
                context.stopService(xmppServiceIntent);

            } else {
                Intent i = new Intent(context, XMPPService.class);
                i.setAction(BOOT_COMPLETED_ACTION);
                context.startService(i);
            }
        }
    }
}