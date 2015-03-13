package com.ruptech.chinatalk.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ApplyTranslatorActivity extends ActionBarActivity {

	private final String TAG = Utils.CATEGORY
			+ ApplyTranslatorActivity.class.getSimpleName();

	@OnClick(R.id.activity_apply_translator_dowlond_button)
	public void gotoAppDownload(View v) {
		String downloadUrl = App.readServerAppInfo().getAppServerUrl() + "../"
				+ "android_volunteer_v3.apk";
		Uri uri = Uri.parse(downloadUrl);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}

	@OnClick(R.id.activity_apply_translator_link_button)
	public void gotoAppStore(View v) {
		String downloadUrl = "market://details?id=com.ruptech.volunteer";
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
		startActivity(i);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apply_translator);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.about_translator);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}
}
