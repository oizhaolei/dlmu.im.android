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

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AgreementActivity extends ActionBarActivity implements
		OnRefreshListener {
	public static final String AGREEMENT_PRIVACY_POLICY = "agreement_privacy_policy";
	public static final String AGREEMENT_TERMS_AND_CONDITIONS = "agreement_terms_and_conditions";
	public static final String EXTRA_AGREEMENT_KEY = "agreement.key";

	private static String getAgreementUrl(String agreementName) {
		return String.format("%s/%s.php", App.readServerAppInfo()
				.getAppServerUrl(), agreementName);
	}

	private String mUrl;

	private String mAgreementName;

	private static AgreementActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_agreement_content)
	WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agreement);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.agreement);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		mAgreementName = extras.getString(EXTRA_AGREEMENT_KEY);
		mUrl = getAgreementUrl(mAgreementName);

		setupComponents();
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
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		if (!Utils.checkNetwork(this)) {
			return;
		}

		if (AGREEMENT_TERMS_AND_CONDITIONS.equals(mAgreementName)) {
			getSupportActionBar().setTitle(
					R.string.prompt_agreement_terms_and_conditions);
		} else {
			getSupportActionBar().setTitle(
					R.string.prompt_agreement_privacy_policy);
		}
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
		wv.loadUrl(mUrl, Utils.additionalHeaders());

	}
}
