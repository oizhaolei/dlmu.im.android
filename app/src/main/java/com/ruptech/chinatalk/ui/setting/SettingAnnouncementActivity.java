package com.ruptech.chinatalk.ui.setting;

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

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveAnnouncementTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.AnnouncementListArrayAdapter;

public class SettingAnnouncementActivity extends ActionBarActivity implements
		OnRefreshListener {

	// Tasks.
	private static GenericTask mRetrieveAnnouncementTask;

	public static void doRetrieveAnnouncement(TaskListener announcementListener) {

		if (mRetrieveAnnouncementTask != null
				&& mRetrieveAnnouncementTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveAnnouncementTask = new RetrieveAnnouncementTask();
		mRetrieveAnnouncementTask.setListener(announcementListener);
		mRetrieveAnnouncementTask.execute();
	}

	@InjectView(R.id.activity_announcement_emptyview_text)
	TextView emptyTextView;

	private AnnouncementListArrayAdapter mAnnouncementListArrayAdapter;

	private final TaskListener mAnnouncementListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onRetrieveAnnouncementSuccess();
				mAnnouncementListArrayAdapter.clear();

				for (Map<String, String> item : RetrieveAnnouncementTask.announcementList) {
					mAnnouncementListArrayAdapter.add(item);
				}

				if (mAnnouncementListArrayAdapter.getCount() == 0) {
					emptyTextView.setText(getString(R.string.no_data_found));
				}
			} else {
				String msg = task.getMsg();
				onRetrieveAnnouncementFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveAnnouncementBegin();
		}

	};
	@InjectView(R.id.activity_announcement_listView)
	ListView mAnnouncementListView;

	private final static String TAG = Utils.CATEGORY
			+ SettingAnnouncementActivity.class.getSimpleName();
	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_announcement);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.announcement);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setupComponents();

		doRetrieveAnnouncement(mAnnouncementListener);
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

	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(R.string.announcement);
	}

	private void onRetrieveAnnouncementBegin() {
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveAnnouncementFailure(String msg) {
		swypeLayout.setRefreshing(false);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	private void onRetrieveAnnouncementSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		mAnnouncementListView.setEmptyView(emptyTextView);

		mAnnouncementListArrayAdapter = new AnnouncementListArrayAdapter(this);
		mAnnouncementListView.setAdapter(mAnnouncementListArrayAdapter);

	}
}
