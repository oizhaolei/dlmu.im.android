package com.ruptech.chinatalk.ui.setting;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;

public class RechargeWebpayActivity extends ActionBarActivity implements
		OnRefreshListener {
	// JavaScript Interface
	private class AndroidBridge {

		@SuppressWarnings("unused")
		public void callClose() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(RechargeWebpayActivity.this,
							R.string.recharge_process_finished,
							Toast.LENGTH_SHORT).show();
					doRetrieveUser();
				}
			});
		}
	}

	private final class WebViewClientExtension extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			if (BuildConfig.DEBUG)
				Log.v(TAG, "onPageFinished(" + url + ")");
			swypeLayout.setRefreshing(false);
		}

		@SuppressLint("JavascriptInterface")
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			swypeLayout.setRefreshing(true);
			if (BuildConfig.DEBUG)
				Log.v(TAG, "onPageStarted(" + url + ")");
			view.addJavascriptInterface(new AndroidBridge(), "androidInterface");

		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed(); // 接受所有网站的证书
		}

		private void promptDownload(final String downloadUrl) {
			try {
				Intent i = Intent.parseUri(downloadUrl,
						Intent.URI_INTENT_SCHEME);
				startActivity(i);
			} catch (Exception e) {
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "shouldOverrideUrlLoading(" + url + ")");

			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				try {
					Intent i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
					startActivityForResult(i, REQUEST_ONLINE_PAYMENT);
					Log.e(TAG, "Intent.parse");
					// 삼성카드 안심클릭을 위해 추가
					// finish();
				} catch (URISyntaxException e) {
					Utils.sendClientException(e, url);
				} catch (ActivityNotFoundException e) {
					try {
						Intent i = new Intent(Intent.ACTION_VIEW,
								Uri.parse(url));
						startActivity(i);
						Log.e(TAG, "Uri.parse");
					} catch (ActivityNotFoundException e1) {
						// url prefix가 ispmobile 일겨우만 alert를 띄움
						if (BuildConfig.DEBUG)
							Log.e(TAG, "ActivityNotFoundException:(" + url
									+ ")");
						Utils.sendClientException(e1, url);

						final String downloadUrl = uriDownload(url);
						if (downloadUrl != null) {
							promptDownload(downloadUrl);
						} else {
							view.loadData("<html><body></body></html>",
									"text/html", "euc-kr");
							DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							};
							CustomDialog alertIsp = new CustomDialog(
									RechargeWebpayActivity.this)
									.setTitle(getString(R.string.notice))
									.setMessage(
											getString(R.string.undefined_uri,
													url))
									.setPositiveButton(R.string.ok,
											positiveListener);
							alertIsp.show();

						}
						swypeLayout.setRefreshing(false);
						return false;
					}
				}
			} else {
				view.loadUrl(url, Utils.additionalHeaders());
			}
			swypeLayout.setRefreshing(false);
			return true;
		}
	}

	private final String TAG = Utils.CATEGORY
			+ RechargeWebpayActivity.class.getSimpleName();

	private static final int REQUEST_ONLINE_PAYMENT = 10;

	private final Handler handler = new Handler();

	@InjectView(R.id.activity_recharge_webview)
	WebView wv;

	String serialNo;

	private int payType;

	Map<String, String> m = new HashMap<>();

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private void doRetrieveUser() {

		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(App
				.readUser().getId());
		mRetrieveUserTask.setListener(new TaskAdapter() {

			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				RetrieveUserTask fsTask = (RetrieveUserTask) task;
				if (result == TaskResult.OK) {
					settingRechargeSuccess();
				} else {
					String msg = fsTask.getMsg();
					settingRechargeFailure(msg);
				}
			}

			@Override
			public void onPreExecute(GenericTask task) {
				settingRechargeBegin();
			}

		});
		mRetrieveUserTask.execute();
	}

	private void extras() {
		Bundle extras = getIntent().getExtras();
		payType = extras.getInt(MyWalletActivity.EXIST_ONLINE_PAY_TYPE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ONLINE_PAYMENT) {
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				Toast.makeText(RechargeWebpayActivity.this,
						"extras:" + extras.toString(), Toast.LENGTH_LONG)
						.show();

				Map<String, String> params = new HashMap<>();
				params.put("P_OID", App.readUser().getTel());
				params.put("serialno", String.valueOf(serialNo));
				params = HttpConnection.genParams(params);

				String returnUrl = App.getHttpServer().genRequestURL(
						"recharge_return.php", params);
				wv.loadUrl(returnUrl, Utils.additionalHeaders());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recharge_webpay);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.online_recharge);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		serialNo = DateCommonUtils.dateFormat(new Date(),
				DateCommonUtils.DF_yyyyMMddHHmmss2);

		extras();
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

	private void settingRechargeBegin() {
		swypeLayout.setRefreshing(true);
	}

	private void settingRechargeFailure(String msg) {
		swypeLayout.setRefreshing(false);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		this.finish();
	}

	private void settingRechargeSuccess() {
		swypeLayout.setRefreshing(true);
		this.finish();
	}

	@SuppressLint("JavascriptInterface")
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		if (!Utils.checkNetwork(this)) {
			return;
		}
		WebViewClient webViewClient = new WebViewClientExtension();
		wv.setWebViewClient(webViewClient);
		wv.clearCache(true);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.addJavascriptInterface(new AndroidBridge(), "androidInterface");

		String url = "";
		Map<String, String> params = new HashMap<>();
		switch (payType) {
		case MyWalletActivity.ONLINE_PAY_TYPE_INIPAY:
			getSupportActionBar().setTitle(R.string.recharge_inipay_demo);
			params.put("tel", App.readUser().getTel());
			params.put("serialno", String.valueOf(serialNo));
			params = HttpConnection.genParams(params);

			url = App.getHttpServer().genRequestURL("recharge_start1.php",
					params);
			break;
		case MyWalletActivity.ONLINE_PAY_TYPE_PAYPAL:
			getSupportActionBar().setTitle(R.string.recharge_paypal_demo);
			params.put("userid", String.valueOf(App.readUser().getId()));
			params.put("serialno", String.valueOf(serialNo));
			params = HttpConnection.genParams(params);

			url = App.getHttpServer().genRequestURL(
					"paypal_recharge_start.php", params);
			break;
		default:
			break;
		}

		if (BuildConfig.DEBUG)
			Log.d(TAG, url);

		wv.loadUrl(url, Utils.additionalHeaders());
	}

	private String uriDownload(String uri) {
		if (uri.indexOf("kr.co.shiftworks.vguardweb")>=0) {
			return "market://details?id=kr.co.shiftworks.vguardweb";
		} else if (uri.indexOf("com.TouchEn.mVaccine.webs")>=0) {
			return "market://details?id=com.TouchEn.mVaccine.webs";
		} else if (uri.indexOf("com.lotte.lottesmartpay")>=0) {
			return "market://details?id=com.lotte.lottesmartpay";
		} else if (uri.indexOf("com.lcacApp")>=0) {
			return "market://details?id=com.lcacApp";
		} else if (uri.indexOf("com.kbcard.cxh.appcard")>=0) {
			return "market://details?id=com.kbcard.cxh.appcard";
		} else if (uri.indexOf("kr.co.samsungcard.mpocket")>=0) {
			return "market://details?id=kr.co.samsungcard.mpocket";
		}
		if (m.isEmpty()) {
			m.put("droidx://antivirusweb", "market://details?id=net.nshc.droidxantivirus");
			m.put("droidxantivirusweb:", "market://details?id=net.nshc.droidxantivirus");
			m.put("intent://cardName=SAMSUNG",
					"market://kr.co.shiftworks.vguardweb");
			m.put("intent://antivirusweb", "market://com.ilk.visa3d");
			m.put("intent://inicis",
					"market://details?id=com.inicis.pay.android");
			m.put("intent:hdcardappcardansimclick",
					"market://details?id=com.hyundaicard.appcard");
			m.put("intent://hdcardappcardansimclick",
					"market://details?id=com.hyundaicard.appcard");
			m.put("hdcardappcardansimclick://",
					"market://details?id=com.hyundaicard.appcard");
			m.put("kftc-bankpay://eftpay",
					"market://details?id=com.kftc.bankpay.android");
			m.put("kpay://inicis", "market://details?id=com.inicis.kpay");
			m.put("mpocket.online.ansimclick://",
					"market://details?id=net.ib.android.smcard");
			m.put("mvaccinestart",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("smartvaccineexit",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("smartvaccinecheck",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("mvaccinecheck",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("intent://mvaccine",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("mvaccine://",
					"market://details?id=com.TouchEn.mVaccine.webs");
			m.put("ispmobile://", "market://details?id=kvp.jjy.MispAndroid320");
			m.put("vguardstart://",
					"market://details?id=kr.co.shiftworks.vguardweb");
			m.put("vguardend://",
					"market://details?id=kr.co.shiftworks.vguardweb");
			m.put("vguardcheck://", "market://details?id=kr.co.shiftworks.bank");
			m.put("mpocketansimclick://",
					"market://details?id=net.ib.android.smcard");
			m.put("ahnlabv3mobileplus",
					"market://details?id=com.ahnlab.v3mobileplus");
			m.put("shinhan-sr-ansimclick://",
					"market://details?id=com.shcard.smartpay");
			m.put("intent://pay", "market://details?id=com.shcard.smartpay");
			m.put("lotteappcard://lottecard", "market://details?id=com.lotte.lottesmartpay");
			m.put("intent://lottecard", "market://details?id=com.lcacApp");
			m.put("lottecard://", "market://details?id=com.lcacApp");
			m.put("intent://lottesmartpay",
					"market://details?id=com.lotte.lottesmartpay");
			m.put("lottesmartpay://",
					"market://details?id=com.lotte.lottesmartpay");
			m.put("kb-acp://", "market://details?id=com.kbcard.cxh.appcard");
			m.put("kb-homeplus-acp://",
					"market://details?id=com.kbcard.cxh.appcard");
			m.put("kbappcard://", "market://details?id=com.kbcard.cxh.appcard");
			m.put("samsungcardipin://",
					"market://details?id=net.ib.android.smcard");
			m.put("samsungcard://", "market://details?id=net.ib.android.smcard");
		}
		for (String key : m.keySet()) {
			if (uri.indexOf(key) == 0) {
				return m.get(key);
			}
		}
		return null;
	}

}