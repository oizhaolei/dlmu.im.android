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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

public class WebViewActivity extends ActionBarActivity implements
		OnRefreshListener {
	private final String TAG = Utils.CATEGORY
			+ WebViewActivity.class.getSimpleName();
	public static final String EXTRA_WEBVIEW_URL = "EXTRA_WEBVIEW_URL";
	public static final String EXTRA_WEBVIEW_TITLE = "EXTRA_WEBVIEW_TITLE";
	private String mTitle = "";

	private String mUrl;

	@InjectView(R.id.activity_webview_content_view)
	WebView webview;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private void getExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mUrl = extras.getString(EXTRA_WEBVIEW_URL);
			mTitle = extras.getString(EXTRA_WEBVIEW_TITLE);
		}
	}

	@Override
	public void onBackPressed() {
		if (webview.canGoBack()) {
			webview.goBack();
		} else {
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		ButterKnife.inject(this);
		getExtras();
		getSupportActionBar().setTitle(mTitle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		if (!Utils.checkNetwork(this)) {
			return;
		}

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				swypeLayout.setRefreshing(false);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				swypeLayout.setRefreshing(true);
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				swypeLayout.setRefreshing(true);
				view.loadUrl(url, Utils.additionalHeaders());
				return true;
			}
		});
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setLoadWithOverviewMode(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setDomStorageEnabled(true);// 解决加载出现空白
		webview.clearCache(true);

		webview.loadUrl(mUrl, Utils.additionalHeaders());
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
}
