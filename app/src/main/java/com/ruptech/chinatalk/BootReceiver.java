package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ruptech.chinatalk.utils.Utils;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = Utils.CATEGORY
			+ BootReceiver.class.getSimpleName();


	@Override
	public void onReceive(Context ctx, Intent intent) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "system boot completed");
	}
}