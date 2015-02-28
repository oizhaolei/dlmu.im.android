package com.ruptech.chinatalk.ui.setting;

import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveVerifyMessageTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.VerifyMessageListArrayAdapter;

public class SettingVerifyActivity extends ActionBarActivity implements
		OnRefreshListener {
	private final String TAG = Utils.CATEGORY
			+ SettingVerifyActivity.class.getSimpleName();

	List<Map<String, String>> mMessageList;

	// Tasks.
	private GenericTask mRetrieveMessageTask;

	private final TaskListener mRetrieveMessageListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				mMessageList = ((RetrieveVerifyMessageTask) task)
						.getmMessageList();
				onRetrieveMessageSuccess();

				for (Map<String, String> item : mMessageList) {
					mMessageListArrayAdapter.add(item);
				}
				mMessageListArrayAdapter.notifyDataSetChanged();

				if (mMessageListArrayAdapter.getCount() == 0) {
					emptyTextView.setText(getString(R.string.no_data_found));
				}

			} else {
				String msg = task.getMsg();
				onRetrieveMessageFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveMessageBegin();
		}

	};

	@InjectView(R.id.activity_request_verify_listView)
	ListView mMessageListView;
	private VerifyMessageListArrayAdapter mMessageListArrayAdapter;

	@InjectView(R.id.activity_request_verify_emptyview_text)
	TextView emptyTextView;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private void doRetrieveMessage() {

		if (mRetrieveMessageTask != null
				&& mRetrieveMessageTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveMessageTask = new RetrieveVerifyMessageTask();
		mRetrieveMessageTask.setListener(mRetrieveMessageListener);

		mRetrieveMessageTask.execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_verify);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.validate);
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

	@Override
	public void onResume() {
		super.onResume();

		mMessageListArrayAdapter.clear();
		doRetrieveMessage();
	}

	private void onRetrieveMessageBegin() {
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveMessageFailure(String msg) {
		swypeLayout.setRefreshing(false);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRetrieveMessageSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		mMessageListView.setEmptyView(emptyTextView);
		mMessageListArrayAdapter = new VerifyMessageListArrayAdapter(this);
		mMessageListView.setAdapter(mMessageListArrayAdapter);
	}
}
