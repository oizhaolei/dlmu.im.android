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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class IntroduceActivity extends ActionBarActivity implements
		OnRefreshListener {
	private final String TAG = Utils.CATEGORY
			+ IntroduceActivity.class.getSimpleName();
	private static IntroduceActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	@InjectView(R.id.activity_introduce_content_view)
	WebView webview;

	private String mUrl;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

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
		setContentView(R.layout.activity_introduce);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.introduce);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;

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
		webview.getSettings().setBuiltInZoomControls(true);// 隐藏缩放按钮的控件
		webview.getSettings().setDomStorageEnabled(true);// 解决加载出现空白
		webview.clearCache(true);

		mUrl = getResources().getString(R.string.introduce_url);
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
