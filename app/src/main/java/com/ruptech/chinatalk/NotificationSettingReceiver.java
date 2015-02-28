package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.ui.setting.SettingGeneralActivity;

public class NotificationSettingReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent intent) {
		Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		ctx.sendBroadcast(it);
		Intent settingIntent = new Intent(ctx, SettingGeneralActivity.class);
		settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(settingIntent);
	}
}