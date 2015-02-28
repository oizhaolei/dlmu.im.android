package com.ruptech.chinatalk.ui.setting;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.alipay.sdk.app.PayTask;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RechargePriceTask;
import com.ruptech.chinatalk.utils.AlipayResult;
import com.ruptech.chinatalk.utils.Utils;

public class RechargeAlipayActivity extends ActionBarActivity implements
		OnItemClickListener, OnRefreshListener {

	private static final String[] STRING_FROM = { "subject", "body" };
	private final static int[] TO_IDS = { R.id.product_subject,
			R.id.product_body };

	private static final int SDK_PAY_FLAG = 1;

	private static final int SDK_CHECK_FLAG = 2;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				String flag = "alipay_result_status_";
				try {
					AlipayResult result = new AlipayResult((String) msg.obj);

					String packageName = getPackageName();
					flag += result.resultStatus;
					int identifier = getResources().getIdentifier(flag,
							"string", packageName);
					String dealResult = getResources().getString(identifier);

					Toast.makeText(RechargeAlipayActivity.this, dealResult,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					Utils.sendClientException(e, flag);
				}
				break;
			}
			case SDK_CHECK_FLAG: {
				Toast.makeText(RechargeAlipayActivity.this, "检查结果为：" + msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			}
			default:
				break;
			}
		};
	};

	private final String TAG = Utils.CATEGORY
			+ RechargeAlipayActivity.class.getSimpleName();

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

	@InjectView(R.id.activity_alipay_listview)
	ListView aliPayRechargeList;

	@InjectView(R.id.activity_alipay_recharge_memo)
	TextView alipayMemoTextview;
	private void doRetrieveRechargePrice() {
		if (mRechargePriceTask != null
				&& mRechargePriceTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRechargePriceTask = new RechargePriceTask();
		mRechargePriceTask.setListener(mRechargePriceTaskListener);

		mRechargePriceTask.execute();
	}

	private void initListView(List<Map<String, String>> rechargePriceList) {
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,
				rechargePriceList, R.layout.item_alipay_product, STRING_FROM,
				TO_IDS);
		aliPayRechargeList.setAdapter(mSimpleAdapter);
		aliPayRechargeList.setOnItemClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recharge_alipay);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.recharge_rmb_pay_demo);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setupComponents();
		doRetrieveRechargePrice();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		try {
			if (BuildConfig.DEBUG)
				Log.i("ExternalPartner", "onItemClick");
			final String orderInfo = AlipayResult.getNewOrderInfo(
					mRechargePriceList, position);
			if (BuildConfig.DEBUG)
				Log.i("ExternalPartner", "start pay");
			// start the pay.
			if (BuildConfig.DEBUG)
				Log.i(TAG, "info = " + orderInfo);

			new Thread() {
				@Override
				public void run() {
					PayTask alipay = new PayTask(RechargeAlipayActivity.this);

					// 设置为沙箱模式，不设置默认为线上环境
					// alipay.setSandBox(true);

					String result = alipay.pay(orderInfo);

					if (BuildConfig.DEBUG)
						Log.i(TAG, "result = " + result);

					Message msg = new Message();
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
			}.start();

		} catch (Exception e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
			Utils.sendClientException(e);
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
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		alipayMemoTextview.setText(Html
				.fromHtml(getString(R.string.alipay_recharge_memo)));
	}
}