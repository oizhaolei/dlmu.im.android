package com.ruptech.chinatalk.ui.gift;

import java.text.NumberFormat;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveGiftListTask;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.GiftListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class GiftListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	private final String TAG = Utils.CATEGORY
			+ GiftListActivity.class.getSimpleName();
	@InjectView(R.id.activity_gift_list_grid_view)
	GridView giftListView;
	@InjectView(R.id.activity_gift_list_balance_textview)
	TextView giftListBalanceTextView;

	private long mToUserId;
	private long mToPhotoId;

	private GiftListArrayAdapter mGiftListArrayAdapter;

	private static GenericTask mRetrieveGiftTask;

	private final TaskListener mRetrieveGiftTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveGiftListTask retrieveGiftTask = (RetrieveGiftListTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<Gift> giftList = retrieveGiftTask.getGiftList();
				addToAdapter(giftList, retrieveGiftTask.isTop());
			} else {
				Toast.makeText(GiftListActivity.this,
						retrieveGiftTask.getMsg(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}

	};

	private void addToAdapter(List<Gift> mGiftList, boolean up) {
		int pos = 0;
		for (Gift gift : mGiftList) {
			// request auto translate
			if (up) {
				mGiftListArrayAdapter.insert(gift, pos++);
			} else {
				mGiftListArrayAdapter.add(gift);
			}
		}
	}

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	public void doGiftList(boolean top) {
		if (mRetrieveGiftTask != null
				&& mRetrieveGiftTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		mRetrieveGiftTask = new RetrieveGiftListTask(top, maxId, sinceId);
		mRetrieveGiftTask.setListener(mRetrieveGiftTaskListener);

		mRetrieveGiftTask.execute();
	}

	protected long getMaxId() {
		if (mGiftListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mGiftListArrayAdapter.getItem(
					mGiftListArrayAdapter.getCount() - 1).getId();
		}
	}

	protected long getSinceId() {
		if (mGiftListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mGiftListArrayAdapter.getItem(0).getId();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gift_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.present);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mToUserId = getToUserIdFromExtras();
		mToPhotoId = getToPhototIdFromExtras();
		// 加载画面
		setupComponents();

		doGiftList(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onRefresh(boolean isUp) {
		swypeLayout.setRefreshing(false);
		doGiftList(isUp);
	}

	private long getToUserIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(GiftDonateActivity.EXTRA_TO_USER_ID);
			return userId;
		}
		return 0;
	}

	private long getToPhototIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras
					.getLong(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID);
			return userId;
		}
		return 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		setBalance();
	}

	public void myWalletButton(View v) {
		Intent intent = new Intent(this, MyWalletActivity.class);
		startActivity(intent);

	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mGiftListArrayAdapter = new GiftListArrayAdapter(this);
		giftListView.setAdapter(mGiftListArrayAdapter);
		giftListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Gift gift = mGiftListArrayAdapter.getItem(position);
				if (gift != null) {
					gotoGiftDonate(gift);
				}

			}
		});
		setBalance();
	}

	private void gotoGiftDonate(Gift gift) {
		Intent intent = new Intent(this, GiftDonateActivity.class);
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_ID, mToUserId);
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID, mToPhotoId);
		intent.putExtra(GiftDonateActivity.EXTRA_GIFT, gift);
		this.startActivity(intent);
	}

	private void setBalance() {
		if (giftListBalanceTextView != null) {
			giftListBalanceTextView.setText(Html.fromHtml(getString(
					R.string.curr_user_balance,
					NumberFormat.getNumberInstance().format(
							App.readUser().getBalance())))
					+ getString(R.string.point));
		}
	}
}