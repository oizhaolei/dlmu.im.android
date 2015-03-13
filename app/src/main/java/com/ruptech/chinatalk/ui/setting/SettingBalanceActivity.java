package com.ruptech.chinatalk.ui.setting;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingBalanceActivity extends ActionBarActivity implements
OnRefreshListener {

	private final String TAG = Utils.CATEGORY
			+ SettingBalanceActivity.class.getSimpleName();

	private static final String USER_FEE_HISTORY_LIST = "user_fee_history_list_webview";

	private static String getBalanceUrl(String url, Object searchMonth) {
		String loginid;
		if (App.readUser() != null) {
			loginid = String.valueOf(App.readUser().getId());
		} else {
			return "";
		}
		Map<String, String> params = new HashMap<>();
		params.put("month", String.valueOf(searchMonth.toString()));
		params.put("minute_offset",
				String.valueOf(DateCommonUtils.getTimezoneMinuteOffset()));

		String source = "an-" + Utils.getAppVersionCode();
		params.put("source", source);
		params.put("loginid", loginid);
		String sign = Utils.genSign(params, loginid);

		return String
				.format("%s%s.php?loginid=%s&month=%s&minute_offset=%s&sign=%s&source=%s",
						App.readServerAppInfo().getAppServerUrl(), url,
						loginid, searchMonth.toString(),
						DateCommonUtils.getTimezoneMinuteOffset(), sign, source);
	}

	String[] arrayMonthly = null;

	@InjectView(R.id.activity_feehistory_balance)
	TextView mBalanceTextView;

	private String mUrl;

	@InjectView(R.id.activity_setting_balance_content)
	WebView webview;

	private MenuItem selectMonthMenu;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private void displayUserBalance() {
		mBalanceTextView.setText(Html.fromHtml(getString(
				R.string.curr_user_balance, NumberFormat.getNumberInstance()
				.format(App.readUser().getBalance())))
				+ getString(R.string.point));
	}

	private String[] getMonthly() {
		String[] monthly = new String[12];
		Calendar c = Calendar.getInstance();
		for (int i = 0; i < 12; i++) {
			monthly[i] = DateCommonUtils.dateFormat(c.getTime(),
					DateCommonUtils.DF_yyyyMM);
			c.add(Calendar.MONTH, -1);
		}
		return monthly;
	}

	private void loadWebViewByMonth(String searchMonth) {

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

		mUrl = getBalanceUrl(USER_FEE_HISTORY_LIST, searchMonth);

		webview.loadUrl(mUrl, Utils.additionalHeaders());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_setting_balance);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.consumption_experience);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		selectMonthMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.empty);

		selectMonthMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		selectMonthMenu.setTitleCondensed(arrayMonthly[0]);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == selectMonthMenu.getItemId()) {
			searchFeeHisByMonth();
		}
		return true;
	}
	@Override
	public void onRefresh() {
		swypeLayout.setRefreshing(false);
	}


	private void searchFeeHisByMonth() {
		new CustomDialog(this).setTitle(getString(R.string.select_month))
		.setItems(arrayMonthly, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				loadWebViewByMonth(arrayMonthly[which]);
				selectMonthMenu.setTitleCondensed(arrayMonthly[which]);
			}
		}).show();
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		arrayMonthly = getMonthly();

		loadWebViewByMonth(arrayMonthly[0]);// 第一次加载

		displayUserBalance();
	}
}
