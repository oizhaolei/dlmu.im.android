/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.DiscoverTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DiscoverUserArrayAdapter extends ArrayAdapter<User> {

	public interface OnDiscoverListener {
		public void onDiscover(long userCount, long channelCount,
				List<Channel> channelList);
	}

	class ViewHolder {
		@InjectView(R.id.fullname)
		TextView nameTextView;
		@InjectView(R.id.icon)
		ImageView iconView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private OnDiscoverListener listener;

	protected boolean notMoreDataFound = false;
	protected GenericTask mRetrieveUserListTask;

	protected final TaskListener mRetrieveUserListTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			DiscoverTask discoverTask = (DiscoverTask) task;
			if (result == TaskResult.OK) {
				List<User> mUserList = discoverTask.getUserList();
				if (!discoverTask.isTop()
						&& mUserList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (discoverTask.getName().equals(keyword)) {
					addAllUsers(mUserList, discoverTask.isTop());
				} else {
					clear();
					addAllUsers(mUserList, discoverTask.isTop());
					if (listener != null) {
						listener.onDiscover(discoverTask.getUserCount(),
								discoverTask.getChannelCount(),
								discoverTask.getChannelList());
					}
				}
				keyword = discoverTask.getName();
				onRetrieveUserListSuccess();
			} else {
				String msg = task.getMsg();
				onRetrieveUserListFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			DiscoverTask retrieveUserListTask = (DiscoverTask) task;
			onRetrieveUserListBegin(retrieveUserListTask.isTop());
		}

	};

	private final SwipeRefreshLayout mSwypeLayout;

	private final LayoutInflater viewInflater;

	private String keyword;

	private static Context mContext;

	public DiscoverUserArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout, int resId) {
		super(context, resId);
		mContext = context;
		this.mSwypeLayout = swypeLayout;
		viewInflater = LayoutInflater.from(getContext());
	}

	private void addAllUsers(List<User> mUserList, boolean up) {
		int pos = 0;
		for (User user : mUserList) {
			if (up) {
				insert(user, pos++);
			} else {
				add(user);
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

	public void getUserList(String keyword, boolean isTop) {
		if (keyword == null || keyword.length() == 0)
			return;

		if (!isTop && notMoreDataFound) {
			mSwypeLayout.setRefreshing(false);
			Toast.makeText(mContext, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mRetrieveUserListTask != null
				&& mRetrieveUserListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		long sinceId;
		long maxId;

		String type;
		if (keyword.equals(this.keyword)) {
			if (isTop) {
				sinceId = getSinceId();
				maxId = AppPreferences.ID_IMPOSSIBLE;
			} else {
				maxId = getMaxId();
				sinceId = AppPreferences.ID_IMPOSSIBLE;
			}
			type = DiscoverTask.TYPE_USER;
		} else {
			maxId = AppPreferences.ID_IMPOSSIBLE;
			sinceId = AppPreferences.ID_IMPOSSIBLE;
			type = DiscoverTask.TYPE_NONE;
		}

		mRetrieveUserListTask = new DiscoverTask(isTop, type, keyword, maxId,
				sinceId);
		mRetrieveUserListTask.setListener(mRetrieveUserListTaskListener);
		mRetrieveUserListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {

			convertView = viewInflater.inflate(R.layout.item_sub_tab_user, parent,
					false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		User user = getItem(position);
		holder.nameTextView.setText(user.getFullname());

		String picUrl = user.getPic_url();
		if (!picUrl.equals(holder.iconView.getTag())) {
			ImageManager.imageLoader.displayImage(App.readServerAppInfo()
					.getServerThumbnail(picUrl), holder.iconView, ImageManager
					.getOptionsPortrait());
			holder.iconView.setTag(picUrl);
		}
		return convertView;
	}

	protected void onRetrieveUserListBegin(boolean isTop) {
		mSwypeLayout.setProgressTop(isTop);
		mSwypeLayout.setRefreshing(true);
	}

	protected void onRetrieveUserListFailure(String msg) {
		mSwypeLayout.setRefreshing(false);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	protected void onRetrieveUserListSuccess() {
		mSwypeLayout.setRefreshing(false);
	}

	public void setOnDiscoverListener(OnDiscoverListener l) {
		this.listener = l;
	}
}