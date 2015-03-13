package com.ruptech.chinatalk.ui.story;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestAutoTranslatePhotoTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoListTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.UserStoryListArrayAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class AbstractUserStoryListActivity extends ActionBarActivity
		implements SwipeRefreshLayout.OnRefreshListener {

	protected static final int ADD_STORY = 0;
	public static final String EXTRA_STORY_ADDRESS = "EXTRA_STORY_ADDRESS";
	public static final String EXTRA_STORY_LATE6 = "EXTRA_STORY_LATE6";
	public static final String EXTRA_STORY_LNGE6 = "EXTRA_STORY_LNGE6";
	public static final String EXTRA_STORY_TAG = "EXTRA_STORY_TAG";
	public static final String EXTRA_STORY_TYPE = "EXTRA_STORY_TYPE";
	public static AbstractUserStoryListActivity instance = null;

	public static final String STORY_TYPE_CHOSEN = "chosen";
	public static final String STORY_TYPE_FAVORITE = "favorite";
	public static final String STORY_TYPE_FRIENDS = "friend";
	public static final String STORY_TYPE_TIMELINE = "timeline";
	public static final String STORY_TYPE_LOCATION = "location";
	public static final String STORY_TYPE_PHOTO = "photo";
	public static final String STORY_TYPE_POPULAR = "popular";
	public static final String STORY_TYPE_TAG = "tag";
	public static final String STORY_TYPE_USER = "me";
	public static final String STORY_TYPE_CHANNELS = "channels";

	public static boolean isTranslatorLang(String lang) {
		if (lang.equals(App.readUser().getLang1())
				|| lang.equals(App.readUser().getLang2())) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean isLocalDataDisplay = true;

	protected final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UserPhoto story = (UserPhoto) intent.getExtras().getSerializable(
					CommonUtilities.EXTRA_MESSAGE);
			if (story != null) {
				mUserStoryListArrayAdapter.changeUserPhoto(story);
			}
		}
	};
	protected int mLate6 = 0;
	protected int mLnge6 = 0;

	protected final TaskListener mRequestAutoTranslatePhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				RequestAutoTranslatePhotoTask requestAutoTranslatePhotoTask = ((RequestAutoTranslatePhotoTask) task);
				mUserStoryListArrayAdapter
						.changeUserPhoto(requestAutoTranslatePhotoTask
								.getUserPhoto());
			}
		}
	};

	protected GenericTask mRetrieveUserPhotoListTask;

	protected final TaskListener mRetrieveUserPhotoListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			if (result == TaskResult.OK) {
				List<UserPhoto> mUserPhotoList = retrieveUserPhotoListTask
						.getUserPhotoList();
				if (isLocalDataDisplay && mUserPhotoList.size() > 0) {
					mUserStoryListArrayAdapter.clear();
				}
				isLocalDataDisplay = false;
				if (!retrieveUserPhotoListTask.isTop()
						&& mUserPhotoList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (retrieveUserPhotoListTask.isTop()
						&& mUserPhotoList.size() == 0) {
					Toast.makeText(AbstractUserStoryListActivity.this,
							R.string.no_new_data,
							Toast.LENGTH_SHORT).show();
				}
				addAllStoryListArrayAdapter(mUserPhotoList,
						retrieveUserPhotoListTask.isTop());
				onRetrieveUserPhotoListSuccess();
			} else {
				String msg = task.getMsg();
				onRetrieveUserPhotoListFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			onRetrieveUserPhotoListBegin(retrieveUserPhotoListTask.isTop());
		}

	};

	protected String mStoryType;

	protected String mTag;
	protected long mUserId;

	protected UserStoryListArrayAdapter mUserStoryListArrayAdapter;
	protected boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	protected final String TAG = Utils.CATEGORY
			+ AbstractUserStoryListActivity.class.getSimpleName();

	@InjectView(R.id.activity_story_listView_emptyview_text)
	TextView emptyTextView;

	@InjectView(R.id.activity_story_listView)
	ListView userStoryListView;

	private void addAllStoryListArrayAdapter(List<UserPhoto> mUserPhotoList,
			boolean up) {
		int pos = 0;
		String lang = App.readUser().getLang();
		String lang1 = null;
		if (App.readUser().getAdditionalLangs() != null
				&& App.readUser().getAdditionalLangs().length > 0) {
			lang1 = App.readUser().getAdditionalLangs()[0];
		}

		for (UserPhoto userPhoto : mUserPhotoList) {
			// request auto translate 去掉重复请求自动翻译
			long requestKey = userPhoto.getId();
			List<Long> storyAutoRequestTransKeyList = RequestAutoTranslatePhotoTask
					.getStoryAutoRequestTransKeyList();
			if (!Utils.isEmpty(userPhoto.getLang())
					&& !Utils.isEmpty(userPhoto.getContent())
					&& (!lang.equals(userPhoto.getLang()) || (lang1 != null && !lang1
							.equals(userPhoto.getLang())))
					&& Utils.isEmpty(userPhoto.getTo_content())
					&& !UserPhoto.isAutoTranslated(userPhoto)
					&& !storyAutoRequestTransKeyList.contains(requestKey)) {
				// 不等于登陆者的语言才去请求自动翻译
				if (lang.equals(userPhoto.getLang())
						|| (lang1 != null && !lang1.equals(userPhoto.getLang()))) {
					requestAutotranslate(userPhoto, lang1);
				} else {
					requestAutotranslate(userPhoto, lang);
				}
				storyAutoRequestTransKeyList.add(requestKey);
			}

			if (up) {
				mUserStoryListArrayAdapter.insert(userPhoto, pos++);
			} else {
				mUserStoryListArrayAdapter.add(userPhoto);
			}
		}
	}

	public void doPostNew(MenuItem item) {
		Intent intent = new Intent(this, PhotoAlbumActivity.class);
		startActivity(intent);
	}

	protected void doRetrieveUserPhotoList(boolean isTop) {
		if (notMoreDataFound
				|| (mRetrieveUserPhotoListTask != null && mRetrieveUserPhotoListTask
						.getStatus() == GenericTask.Status.RUNNING)) {
			return;
		}

		long sinceId;
		long maxId;
		if (isTop) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		mRetrieveUserPhotoListTask = new RetrieveUserPhotoListTask(isTop,
				maxId, sinceId, mUserId, 0, mStoryType, mLate6, mLnge6, mTag,
				"");
		mRetrieveUserPhotoListTask
				.setListener(mRetrieveUserPhotoListTaskListener);
		mRetrieveUserPhotoListTask.execute();
	}

	@SuppressLint("NewApi")
	protected void extractExtras() {
		Bundle extras = getIntent().getExtras();

		mUserId = extras.getLong(ProfileActivity.EXTRA_USER_ID, -1);
		mStoryType = extras.getString(EXTRA_STORY_TYPE, "");
	}

	protected int getContentViewRes() {
		return R.layout.activity_story_list;
	}

	protected long getMaxId() {
		if (mUserStoryListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mUserStoryListArrayAdapter.getItem(
					mUserStoryListArrayAdapter.getCount() - 1).getId() - 1;
		}
	}

	protected long getSinceId() {
		if (mUserStoryListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mUserStoryListArrayAdapter.getItem(0).getId() + 1;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(getContentViewRes());
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.story);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;
		setSupportProgressBarIndeterminateVisibility(false);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				CommonUtilities.STORY_CONTENT_MESSAGE_ACTION));
	}

	@Override
	protected void onDestroy() {
		instance = null;
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onRefresh(boolean isTop) {
		if (isTop) {
			doRetrieveUserPhotoList(isTop);
		} else if (notMoreDataFound && !isTop) {
			swypeLayout.setRefreshing(false);
			Toast.makeText(this, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
		} else {
			swypeLayout.setRefreshing(false);
		}
	}

	protected void onRetrieveUserPhotoListBegin(boolean isTop) {
		swypeLayout.setProgressTop(isTop);
		swypeLayout.setRefreshing(true);
	}

	protected void onRetrieveUserPhotoListFailure(String msg) {
		swypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	protected void onRetrieveUserPhotoListSuccess() {
		swypeLayout.setRefreshing(false);
		if (mUserStoryListArrayAdapter.getCount() == 0) {
			emptyTextView.setText(R.string.popular_no_data);
			emptyTextView.setVisibility(View.VISIBLE);
		} else {
			emptyTextView.setVisibility(View.GONE);
		}
	}

	protected void requestAutotranslate(UserPhoto userPhoto, String lang) {
		UserStoryCommentActivity.requestAutotranslate(userPhoto, lang,
				mRequestAutoTranslatePhotoTaskListener);
	}

	protected void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		emptyTextView.setVisibility(View.GONE);

		// userStoryListView.addFooterView(footerView);
		mUserStoryListArrayAdapter = new UserStoryListArrayAdapter(this,
				swypeLayout, R.layout.item_story_user_photo);

		if (mStoryType == STORY_TYPE_USER) {
			List<UserPhoto> localUserPhotoList = App.userPhotoDAO
					.fetchUserPhotosByUserId(mUserId);
			addAllStoryListArrayAdapter(localUserPhotoList, false);
		}

		userStoryListView.setAdapter(mUserStoryListArrayAdapter);
		userStoryListView.setRecyclerListener(new RecyclerListener() {
			@Override
			public void onMovedToScrapHeap(View view) {
				// final ImageView imageView = (ImageView) view
				// .findViewById(R.id.item_user_story_pic_imageview);
				// imageView.setImageBitmap(null);
			}
		});

		/** View poppyView = */
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
					// 加载数据代码
					doRetrieveUserPhotoList(false);
				}
			}
		};
		userStoryListView.setOnScrollListener(onScrollListener);
	}
}
