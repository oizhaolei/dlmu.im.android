package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ruptech.chinatalk.R;

public class UpdateVersionServiceActivity extends Activity {

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, UpdateVersionServiceActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FILL_IN_DATA);
		intent.setAction(String.valueOf(System.currentTimeMillis()));
		return intent;
	}

	/**
	 * 点击通知栏，启动service来进行更新
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_version);

		Intent updateIntent = new Intent(this, UpdateVersionService.class);
		startService(updateIntent);
		finish();
	}
}
