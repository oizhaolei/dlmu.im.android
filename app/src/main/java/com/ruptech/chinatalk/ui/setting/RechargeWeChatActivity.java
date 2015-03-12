package com.ruptech.chinatalk.ui.setting;

import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RechargePriceTask;
import com.ruptech.chinatalk.task.impl.WeChatGetAppAccessTokenTask;
import com.ruptech.chinatalk.task.impl.WeChatGetPrepayIdTask;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class RechargeWeChatActivity extends ActionBarActivity implements
		OnItemClickListener, OnRefreshListener {

	private final static String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	private final String TAG = Utils.CATEGORY
			+ RechargeWeChatActivity.class.getSimpleName();
	private IWXAPI api;

	public static String accessToken;

	public static String prepayId;
	private String entity;

	private String total_fee;

	private long timeStamp;

	private String nonceStr, packageValue;
	private ProgressDialog dialog;

	private final TaskListener weChatGetAppAccessTokenTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			// WeChatGetAppAccessTokenTask weChatGetAppAccessTokenTask =
			// (WeChatGetAppAccessTokenTask) task;

			if (result == TaskResult.OK) {
				GenericTask weChatGetPrepayIdTask = new WeChatGetPrepayIdTask(entity,accessToken);
				weChatGetPrepayIdTask
						.setListener(weChatGetPrepayIdTaskListener);
				weChatGetPrepayIdTask.execute();
			} else {
				if (dialog != null) {
					dialog.dismiss();
				}
				String msg = task.getMsg();
				Toast.makeText(RechargeWeChatActivity.this,
						getString(R.string.wechat_get_prepayid_fail, msg),
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			accessToken = "";
			prepayId = "";

			dialog = ProgressDialog.show(RechargeWeChatActivity.this,
					getString(R.string.wechat_app_tip),
					getString(R.string.wechat_getting_prepayid));
		}

	};

	private final TaskListener weChatGetPrepayIdTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			// WeChatGetPrepayIdTask weChatGetPrepayIdTask =
			// (WeChatGetPrepayIdTask) task;
			if (dialog != null) {
				dialog.dismiss();
			}
			if (result == TaskResult.OK) {
				Toast.makeText(RechargeWeChatActivity.this,
						R.string.wechat_get_prepayid_success, Toast.LENGTH_LONG)
						.show();
				sendPayReq();
			} else {
				String msg = task.getMsg();
				Toast.makeText(RechargeWeChatActivity.this,
						getString(R.string.wechat_get_prepayid_fail, msg),
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private static final String[] STRING_FROM = { "subject", "body" };

	private final static int[] TO_IDS = { R.id.wechat_product_subject,
			R.id.wechat_product_body };

	private GenericTask mRechargePriceTask;

	List<Map<String, String>> mRechargePriceList;

	private final TaskListener mRechargePriceTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				mRechargePriceList = ((RechargePriceTask) task)
						.getRechargePriceList();
				initListView(mRechargePriceList);
				onRetrieveSuccess();
			} else {
				String msg = task.getMsg();
				onRetrieveFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveBegin();
		}
	};

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_wechat_listview)
	ListView tenPayRechargeList;

	@InjectView(R.id.activity_wechat_recharge_memo)
	TextView tenpayMemoTextview ;

	private void doRetrieveRechargePrice() {
		if (mRechargePriceTask != null
				&& mRechargePriceTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRechargePriceTask = new RechargePriceTask();
		mRechargePriceTask.setListener(mRechargePriceTaskListener);

		mRechargePriceTask.execute();
	}

	private String genNonceStr() {
		Random random = new Random();
		return getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}
	/**
	 * 注意：商户系统内部的订单号,32个字符内、可包含字母,确保在商户系统唯一
	 */
	private String genOutTradNo() {
		return App.readUser().getId() + "_" + genTimeStamp();
		// Random random = new Random();
		// return getMessageDigest(String.valueOf(random.nextInt(10000))
		// .getBytes());
	}

	private String genPackage(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(WeChatUtil.PARTNER_KEY); // 注意：不能hardcode在客户端，建议genPackage这个过程都由服务器端完成

		// 进行md5摘要前，params内容为原始内容，未经过url encode处理
		String packageSign = getMessageDigest(sb.toString().getBytes())
				.toUpperCase();

		return URLEncodedUtils.format(params, "utf-8") + "&sign=" + packageSign;
	}

	private String genProductArgs(List<Map<String, String>> sProductsMapList,
			int position) {
		Map<String, String> map = sProductsMapList.get(position);
		JSONObject json = new JSONObject();

		try {
			json.put("appid", WeChatUtil.APP_ID);
			String traceId = getTraceId(); // traceId
											// 由开发者自定义，可用于订单的查询与跟踪，建议根据支付用户信息生成此id
			json.put("traceid", traceId);
			nonceStr = genNonceStr();
			json.put("noncestr", nonceStr);

			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("bank_type", "WX"));// 银行渠道
			packageParams
					.add(new BasicNameValuePair("body", map.get("subject")));// 商品描述
			packageParams.add(new BasicNameValuePair("fee_type", "1"));// 币种，1人民币
			packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));// 字符编码
			String notify_url = App.readServerAppInfo().getAppServerUrl()
					+ "recharge/wechatpay.php";
			// String notify_url =
			packageParams.add(new BasicNameValuePair("notify_url", notify_url));// 接收财付通通知的URL
			packageParams.add(new BasicNameValuePair("out_trade_no",
					genOutTradNo()));// 商家订单号
			packageParams.add(new BasicNameValuePair("partner",
					WeChatUtil.PARTNER_ID));// 商户号
			packageParams.add(new BasicNameValuePair("spbill_create_ip",
					"196.168.1.1"));// 订单生成的机器IP，指用户浏览器端IP
			total_fee = String.valueOf(Integer.valueOf(map.get("price")) * 100);
			packageParams.add(new BasicNameValuePair("total_fee", total_fee));// 商品金额,以分为单位
			packageValue = genPackage(packageParams);

			json.put("package", packageValue);
			timeStamp = genTimeStamp();
			json.put("timestamp", timeStamp);

			List<NameValuePair> signParams = new LinkedList<NameValuePair>();
			signParams.add(new BasicNameValuePair("appid", WeChatUtil.APP_ID));
			signParams
					.add(new BasicNameValuePair("appkey", WeChatUtil.APP_KEY));
			signParams.add(new BasicNameValuePair("noncestr", nonceStr));
			signParams.add(new BasicNameValuePair("package", packageValue));
			signParams.add(new BasicNameValuePair("timestamp", String
					.valueOf(timeStamp)));
			signParams.add(new BasicNameValuePair("traceid", traceId));
			json.put("app_signature", genSign(signParams));

			json.put("sign_method", "sha1");
		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}

		return json.toString();
	}

	private String genSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());

		String sha1 = WeChatUtil.sha1(sb.toString());
		Log.d(TAG, "genSign, sha1 = " + sha1);
		return sha1;
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	/**
	 * 建议 traceid 字段包含用户信息及订单信息，方便后续对订单状态的查询和跟踪
	 */
	private String getTraceId() {
		return "crestxu_" + genTimeStamp();
	}
	private void getWeChatAppAccessToken() {
		GenericTask weChatGetAppAccessTokenTask = new WeChatGetAppAccessTokenTask();
		weChatGetAppAccessTokenTask
				.setListener(weChatGetAppAccessTokenTaskListener);
		weChatGetAppAccessTokenTask.execute();
		// new GetAccessTokenTask().execute();
	}

	private void initListView(List<Map<String, String>> rechargePriceList) {
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
				rechargePriceList, R.layout.item_wechat_product, STRING_FROM,
				TO_IDS);
		tenPayRechargeList.setAdapter(mSimpleAdapter);
		tenPayRechargeList.setOnItemClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recharge_wechat);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.recharge_rmb_pay_demo);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		api = WXAPIFactory.createWXAPI(this, WeChatUtil.APP_ID);

		setupComponents();
		doRetrieveRechargePrice();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
		if (isPaySupported) {
			entity = genProductArgs(mRechargePriceList, position);
			getWeChatAppAccessToken();
		} else {
			Toast.makeText(RechargeWeChatActivity.this,
					getString(R.string.wechat_check_pay_nosupport),
					Toast.LENGTH_SHORT).show();
		}
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
		if (mRechargePriceList != null) {
			onRetrieveSuccess();
		} else {
			doRetrieveRechargePrice();
		}
	}

	private void onRetrieveBegin() {
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveFailure(String msg) {
		swypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRetrieveSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void sendPayReq() {

		PayReq req = new PayReq();
		req.appId = WeChatUtil.APP_ID;
		req.partnerId = WeChatUtil.PARTNER_ID;
		req.prepayId = prepayId;
		req.nonceStr = nonceStr;
		req.timeStamp = String.valueOf(timeStamp);
		req.packageValue = "Sign=" + packageValue;
		// req.extData = "trade_no=" + prepayId + "&total_fee=" + total_fee
		// + "&buyer_id=" + App.readUser().getId();

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("appkey", WeChatUtil.APP_KEY));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		req.sign = genSign(signParams);

		boolean register = api.registerApp(WeChatUtil.APP_ID);
		if (register) {
			api.sendReq(req);
		} else {
			Log.e("WeChat", "appId have not been registed. ");
		}
	}
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		tenpayMemoTextview.setText(Html
				.fromHtml(getString(R.string.wechat_recharge_memo)));
	}
}