package com.ruptech.chinatalk.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.TranslatorRankListTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.TranslatorListAdapter;

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TranslatorRankListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;
	private final String TAG = Utils.CATEGORY
			+ TranslatorRankListActivity.class.getSimpleName();

	@InjectView(R.id.activity_translator_listView)
	ListView mRankedListView;
	@InjectView(R.id.activity_translator_emptyview_text)
	TextView emptyTextView;
	private TranslatorListAdapter mRankListAdapter;
	private GenericTask mTranslatorRankListTask;
	List<Map<String, String>> mRankList;
	private final TaskListener mRankListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			TranslatorRankListTask translatorRankListTask = (TranslatorRankListTask) task;
			if (result == TaskResult.OK) {
				mRankList = translatorRankListTask.getmRankedList();
				onTranslatorRankSuccess();

				for (Map<String, String> item : mRankList) {
					mRankListAdapter.add(item);
				}
				if (!translatorRankListTask.isTop()
						&& mRankList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (mRankListAdapter.getCount() == 0) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(getString(R.string.no_translator));
				} else {
					emptyTextView.setVisibility(View.GONE);
				}
			} else {
				String msg = task.getMsg();
				onTranslatorRankFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			TranslatorRankListTask translatorRankListTask = (TranslatorRankListTask) task;
			onTranslatorRankBegin(translatorRankListTask.isTop());
		}
	};

	private MenuItem doTranslatorMenu;
	private boolean notMoreDataFound = false;

	private void doApplyTranslator() {
		Intent i = new Intent(this, ApplyTranslatorActivity.class);
		startActivity(i);
	}

	private void doTranslatorRankList(String startNumber,
			boolean top) {
		if (notMoreDataFound
				|| (mTranslatorRankListTask != null && mTranslatorRankListTask
						.getStatus() == GenericTask.Status.RUNNING)) {
			return;
		}

		mTranslatorRankListTask = new TranslatorRankListTask(top, App
				.readUser().getLang(), startNumber);
		mTranslatorRankListTask.setListener(mRankListener);
		mTranslatorRankListTask.execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_translator_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.within_tree_month);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponent();

		doTranslatorRankList(null, true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		doTranslatorMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.apply_do_translator);
		doTranslatorMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == doTranslatorMenu.getItemId()) {
			doApplyTranslator();
		}
		return true;
	}

	@Override
	public void onRefresh(boolean isTop) {
		swypeLayout.setRefreshing(false);
		if (notMoreDataFound && !isTop) {
			Toast.makeText(this, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void onTranslatorRankBegin(boolean isTop) {
		swypeLayout.setProgressTop(isTop);
		swypeLayout.setRefreshing(true);
	}

	private void onTranslatorRankFailure(String msg) {
		swypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	private void onTranslatorRankSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void setupComponent() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mRankedListView.setEmptyView(emptyTextView);

		mRankListAdapter = new TranslatorListAdapter(this);
		mRankedListView.setAdapter(mRankListAdapter);

		OnScrollListener onScrollListener = new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount > visibleItemCount) {
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == view.getCount() - 1
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					doTranslatorRankList(
							String.valueOf(mRankListAdapter.getCount()), false);
				}
			}
		};
		mRankedListView.setOnScrollListener(onScrollListener);
	}
}
