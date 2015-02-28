package com.ruptech.chinatalk.ui.setting;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

public class SettingFeeIntroduceActivity extends ActionBarActivity implements
		OnRefreshListener {
	private final String TAG = Utils.CATEGORY
			+ SettingFeeIntroduceActivity.class.getSimpleName();
	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;
	@InjectView(R.id.activity_fee_introduction_webview)
	WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_fee_introduce);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.charges_introduction);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponent();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onRefresh() {
		swypeLayout.setRefreshing(false);
	}
	private void setupComponent() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		if (!Utils.checkNetwork(this)) {
			return;
		}

		String url = String.format("%smessage_fee_memo.php", App
				.readServerAppInfo().getAppServerUrl());
		wv.clearCache(true);
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				swypeLayout.setRefreshing(false);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				swypeLayout.setRefreshing(true);
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				swypeLayout.setRefreshing(true);
				view.loadUrl(url, Utils.additionalHeaders());
				return true;
			}
		});
		wv.loadUrl(url, Utils.additionalHeaders());
	}

}
