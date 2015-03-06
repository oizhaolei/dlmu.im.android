package com.ruptech.chinatalk.ui.setting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class RechargeRmbBankTransferActivity extends ActionBarActivity
		implements OnRefreshListener {

	// JavaScript Interface
	private class AndroidBridge {

		@SuppressWarnings("unused")
		public void callClose() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					RechargeRmbBankTransferActivity.close();
				}
			});
		}
	}

	private static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private final String TAG = Utils.CATEGORY
			+ RechargeRmbBankTransferActivity.class.getSimpleName();

	private static RechargeRmbBankTransferActivity instance;

	@InjectView(R.id.activity_recharge_rmb_bank_transfer_content_view)
	WebView webview;

	private final Handler handler = new Handler();

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
		setContentView(R.layout.activity_recharge_rmb_bank_transfer);
		ButterKnife.inject(this);
		instance = this;
		getSupportActionBar().setTitle(
				R.string.recharge_no_alipay_account_card_pay);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	@SuppressLint("JavascriptInterface")
	private void setupComponents() {
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
		webview.getSettings().setJavaScriptEnabled(true);// 支持js
		webview.clearCache(true);
		webview.addJavascriptInterface(new AndroidBridge(), "androidInterface");

		String serialNo = DateCommonUtils.dateFormat(new Date(),
				DateCommonUtils.DF_yyyyMMddHHmmss2);
		String fullname = App.readUser().getFullname();
		try {
			fullname = URLEncoder.encode(fullname, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		Map<String, String> params = new HashMap<>();
		params.put("userid", String.valueOf(App.readUser().getId()));
		params.put("tel", App.readUser().getTel());
		params.put("fullname", fullname);
		params.put("serialno", String.valueOf(serialNo));
		params = HttpConnection.genParams(params);
 
		mUrl = App.getHttpServer().genRequestURL("recharge_rmb_bank_transfer.php", params);

		if (BuildConfig.DEBUG)
			Log.i(TAG, mUrl);
		webview.loadUrl(mUrl, Utils.additionalHeaders());
	}

}
