/**
 *
 */
package com.ruptech.chinatalk.widget;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoListTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class UserPhotoListArrayAdapter extends UserStoryListArrayAdapter {

	protected boolean notMoreDataFound = false;

	protected boolean isLocalDataDisplay = true;

	protected GenericTask mRetrieveUserPhotoListTask;
	private final long mUserId;

	protected final TaskListener mRetrieveUserPhotoListTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			if (result == TaskResult.OK) {
				List<UserPhoto> mUserPhotoList = retrieveUserPhotoListTask
						.getUserPhotoList();
				if (isLocalDataDisplay && mUserPhotoList.size() > 0) {
					clear();
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
				}
				addAllStoryListArrayAdapter(mUserPhotoList,
						retrieveUserPhotoListTask.isTop());
				if (getCount() == 0) {
					mEmptyTextView.setVisibility(View.VISIBLE);
					mEmptyTextView.setText(R.string.popular_no_data);
				} else {
					mEmptyTextView.setVisibility(View.GONE);
				}
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

	private final String storyType;
	private final TextView mEmptyTextView;
	private static Context mContext;

	public UserPhotoListArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout, int resId, long userId,
			String storyType, TextView emptyTextView) {
		super(context, swypeLayout, resId);
		mContext = context;
		this.mUserId = userId;
		this.storyType = storyType;
		this.mEmptyTextView = emptyTextView;
	}

	private void addAllStoryListArrayAdapter(List<UserPhoto> mUserPhotoList,
			boolean up) {
		int pos = 0;
		for (UserPhoto userPhoto : mUserPhotoList) {
			if (up) {
				insert(userPhoto, pos++);
			} else {
				add(userPhoto);
			}
		}
	}

	protected long getMaxId() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return getItem(super.getCount() - 1).getId() - 1;
		}
	}

	protected long getSinceId() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return getItem(0).getId() + 1;
		}
	}

	public void getUserPhotoList(boolean isTop) {
		if (notMoreDataFound) {
			mSwypeLayout.setRefreshing(false);
			Toast.makeText(mContext, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mRetrieveUserPhotoListTask != null
				&& mRetrieveUserPhotoListTask.getStatus() == GenericTask.Status.RUNNING) {
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
				maxId, sinceId, mUserId, 0, storyType, 0, 0, "", "");
		mRetrieveUserPhotoListTask
				.setListener(mRetrieveUserPhotoListTaskListener);
		mRetrieveUserPhotoListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	protected boolean isOriginal() {
		return false;
	}

	protected void onRetrieveUserPhotoListBegin(boolean isTop) {
		mSwypeLayout.setProgressTop(isTop);
		mSwypeLayout.setRefreshing(true);
	}

	protected void onRetrieveUserPhotoListFailure(String msg) {
		mSwypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	protected void onRetrieveUserPhotoListSuccess() {
		mSwypeLayout.setRefreshing(false);
	}

}