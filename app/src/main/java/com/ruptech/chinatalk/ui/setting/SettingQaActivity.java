package com.ruptech.chinatalk.ui.setting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestQuestionTask;
import com.ruptech.chinatalk.task.impl.RetrieveQATask;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class SettingQaActivity extends ActionBarActivity implements
		OnRefreshListener {
	private static final String[] STRING_FROM = { "create_date", "question",
			"answer" };

	private final static int[] TO_IDS = { R.id.listitem_qa_create_date,
			R.id.listitem_qa_question, R.id.listitem_qa_answer };

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_qa_emptyview_text)
	TextView emptyTextView;

	@InjectView(R.id.activity_qa_message)
	EditText mQAEditText;

	List<Map<String, String>> mQAList;

	private final TaskListener mQAListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				mQAList = ((RetrieveQATask) task).getmQAList();
				onRetrieveQASuccess();
				doViewSetAdapter(mQAList);
			} else {
				String msg = task.getMsg();
				onRetrieveQAFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveQABegin();
		}
	};

	@InjectView(R.id.activity_qa_listView)
	ListView mQAListView;

	private final TaskListener mRequestQuestionListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onRequestQuestionSuccess();
			} else {
				String msg = task.getMsg();
				onRequestQuestionFailure(msg);
			}
			mQAEditText.setEnabled(true);
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRequestQuestionBegin();
		}

	};

	private GenericTask mRequestQuestionTask;

	// Tasks.
	private GenericTask mRetrieveQATask;

	@InjectView(R.id.activity_qa_btn_send)
	View mSendButton;

	@InjectView(R.id.activity_qa_rl)
	View mVessageView;
	protected final String TAG = Utils.CATEGORY
			+ SettingQaActivity.class.getSimpleName();

	public static final String EXTRA_QA_ID = "EXTRA_QA_ID";

	private int qa_id = 0;

	private void doRequestQuestion(String text) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "doRequestQuestion");

		if (mRequestQuestionTask != null
				&& mRequestQuestionTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRequestQuestionTask = new RequestQuestionTask(text);
		mRequestQuestionTask.setListener(mRequestQuestionListener);
		mRequestQuestionTask.execute();
	}

	private void doRetrieveQA() {

		if (mRetrieveQATask != null
				&& mRetrieveQATask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveQATask = new RetrieveQATask(qa_id);
		mRetrieveQATask.setListener(mQAListener);
		mRetrieveQATask.execute();
	}

	private void doViewSetAdapter(List<Map<String, String>> mQAList) {
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < mQAList.size(); i++) {
			Map<String, String> map = new HashMap<>();
			Date date = DateCommonUtils.parseToDateFromString(mQAList.get(i)
					.get("create_date"));
			if (date != null) {
				map.put("create_date",
						DateCommonUtils.dateFormat(date,
								DateCommonUtils.DF_yyyyMMddHHmmss).substring(0,
								11));
			}
			map.put("question", mQAList.get(i).get("question"));
			map.put("answer", mQAList.get(i).get("answer"));
			mapList.add(map);
		}

		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this, mapList,
				R.layout.item_qa_full, STRING_FROM, TO_IDS);
		mQAListView.setAdapter(mSimpleAdapter);

		if (mQAList.size() == 0) {
			emptyTextView.setText(getString(R.string.no_data_found));
		}
	}

	private void getExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String extra_qa_id = extras.getString(EXTRA_QA_ID);
			if (!Utils.isEmpty(extra_qa_id)) {
				qa_id = Integer.valueOf(extra_qa_id);
			}
		}
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@OnClick(R.id.activity_qa_btn_send)
	public void onClick(View v) {
		String text = mQAEditText.getText().toString().trim();
		if (!Utils.isEmpty(text)) {
			doRequestQuestion(text);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}
		getExtras();
		setContentView(R.layout.activity_setting_qa);
		ButterKnife.inject(this);

		getSupportActionBar().setTitle(R.string.qa);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
		doRetrieveQA();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

	@Override
	public void onRefresh() {
		swypeLayout.setRefreshing(false);
	}

	private void onRequestQuestionBegin() {
		swypeLayout.setRefreshing(true);
		mSendButton.setEnabled(false);
		mQAEditText.setEnabled(false);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mQAEditText.getWindowToken(), 0);
		mQAEditText.setText("");
	}

	private void onRequestQuestionFailure(String msg) {
		swypeLayout.setRefreshing(false);
		mSendButton.setEnabled(true);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRequestQuestionSuccess() {
		mQAEditText.setText("");
		swypeLayout.setRefreshing(false);
		mSendButton.setEnabled(true);
		Toast.makeText(this, R.string.qa_add_ok, Toast.LENGTH_SHORT).show();
		doRetrieveQA();
	}

	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(R.string.qa);
	}

	private void onRetrieveQABegin() {
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveQAFailure(String msg) {
		swypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRetrieveQASuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mQAListView.setEmptyView(emptyTextView);
		if (App.readUser() != null) {
			mVessageView.setVisibility(View.VISIBLE);
		} else {
			mVessageView.setVisibility(View.GONE);
		}

	}
}
