/**
 *
 */
package com.ruptech.chinatalk.widget;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.DiscoverTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ChannelListCursorAdapter.ViewHolder;

public class DiscoverChannelArrayAdapter extends ArrayAdapter<Channel> {

	protected boolean notMoreDataFound = false;
	protected GenericTask mTask;

	protected final TaskListener mTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			DiscoverTask channelTask = (DiscoverTask) task;
			if (result == TaskResult.OK) {
				List<Channel> mUserList = channelTask.getChannelList();
				if (!channelTask.isTop()
						&& mUserList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (channelTask.getName().equals(keyword)) {
					addAllChannels(mUserList, channelTask.isTop());
				} else {
					clear();
					addAllChannels(mUserList, channelTask.isTop());
				}
				keyword = channelTask.getName();
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

	public DiscoverChannelArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout, int resId) {
		super(context, resId);
		mContext = context;
		this.mSwypeLayout = swypeLayout;
		viewInflater = LayoutInflater.from(getContext());
	}

	private void addAllChannels(List<Channel> list, boolean up) {
		int pos = 0;
		for (Channel channel : list) {
			if (up) {
				insert(channel, pos++);
			} else {
				add(channel);
			}
		}
	}

	public void getChannelList(String keyword, boolean isTop) {
		if (keyword == null || keyword.length() == 0)
			return;

		if (!isTop && notMoreDataFound) {
			mSwypeLayout.setRefreshing(false);
			Toast.makeText(mContext, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (mTask != null && mTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		long sinceId;
		long maxId;

		if (keyword.equals(this.keyword)) {
			if (isTop) {
				sinceId = getSinceId();
				maxId = AppPreferences.ID_IMPOSSIBLE;
			} else {
				maxId = getMaxId();
				sinceId = AppPreferences.ID_IMPOSSIBLE;
			}
		} else {
			maxId = AppPreferences.ID_IMPOSSIBLE;
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}

		mTask = new DiscoverTask(isTop, DiscoverTask.TYPE_CHANNEL, keyword,
				maxId, sinceId);
		mTask.setListener(mTaskListener);
		mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChannelListCursorAdapter.ViewHolder holder;
		if (convertView == null) {

			convertView = viewInflater.inflate(R.layout.item_sub_tab_channel,
					parent, false);
			holder = new ChannelListCursorAdapter.ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Channel channel = getItem(position);
		String picUrl = channel.getPic_url();
		if (!picUrl.equals(holder.picImgView.getTag())) {
			ImageManager.imageLoader.displayImage(App.readServerAppInfo()
					.getServerThumbnail(channel.getPic_url()),
					holder.picImgView, ImageManager.getOptionsLandscape());
			holder.picImgView.setTag(picUrl);
		}

		holder.titleTextView.setText(channel.getTitle());
		holder.popularTextView.setText(String.valueOf(channel
				.getPopular_count()));
		holder.fansTextView
				.setText(String.valueOf(channel.getFollower_count()));

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

}