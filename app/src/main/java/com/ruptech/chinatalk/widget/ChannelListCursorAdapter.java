/**
 *
 */
package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveChannelListTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public class ChannelListCursorAdapter extends CursorAdapter {

	public static class ViewHolder {
		@InjectView(R.id.item_sub_tab_channel_title_textview)
		TextView titleTextView;
		@InjectView(R.id.item_sub_tab_channel_popular_textview)
		TextView popularTextView;
		@InjectView(R.id.item_sub_tab_channel_popular_fans_textview)
		TextView fansTextView;
		@InjectView(R.id.item_sub_tab_channel_thumb_imageview)
		ImageView picImgView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_sub_tab_channel; // xml布局文件

	protected Context mContext;
	private SwipeRefreshLayout swypeLayout;
	private ListView mChannelListView;

	protected LayoutInflater mInflater;
	private GenericTask mRetrieveChannelListTask;

	private final TaskListener mRetrieveChannelListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveChannelListTask channelListTask = (RetrieveChannelListTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<Channel> channelList = channelListTask.getChannelList();
				if (channelList.size() == 0) {
					if (channelListTask.isTop()) {
						Toast.makeText(mContext, R.string.no_new_data,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, R.string.no_more_data,
								Toast.LENGTH_SHORT).show();
					}
				}
				if (channelList != null && channelList.size() > 0) {
					if (channelListTask.isTop()
							&& channelList.size() >= AppPreferences.PAGE_COUNT_20) {
						App.channelDAO.deleteAll();
					}

					for (Channel channel : channelList) {
						App.channelDAO.insertChannel(channel);
					}
					doChangeAdapterCursor();
				}
				if (mChannelListView != null && channelListTask.isTop()) {
					mChannelListView.setSelection(0);
				}
			} else {
				String msg = task.getMsg();
				if (!Utils.isEmpty(msg)) {
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveChannelListTask channelListTask = (RetrieveChannelListTask) task;
			swypeLayout.setProgressTop(channelListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	public void doChangeAdapterCursor() {
		Cursor cursor = fetchChannelListCursor();
		if (cursor == null || cursor.getCount() == 0) {
			doRetrieveChannelList(true, mChannelListView);
		} else {
			changeCursor(cursor);
		}
	}

	private Cursor fetchChannelListCursor() {
		Cursor channelListCursor = App.channelDAO.fetchPopularCursor();
		return channelListCursor;
	}

	public void setSwypeLayout(SwipeRefreshLayout swypeLayout) {
		this.swypeLayout = swypeLayout;
	}

	public void doRetrieveChannelList(boolean top, ListView channelListView) {
		this.mChannelListView = channelListView;
		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		if (mRetrieveChannelListTask != null
				&& mRetrieveChannelListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mRetrieveChannelListTask = new RetrieveChannelListTask(top, maxId,
				sinceId);
		mRetrieveChannelListTask.setListener(mRetrieveChannelListTaskListener);
		mRetrieveChannelListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	protected long getMaxId() {
		long maxCount = App.channelDAO.getMinId();
		if (maxCount == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return maxCount;
		}
	}

	protected long getSinceId() {
		long sinceId = App.channelDAO.getMaxId();
		if (sinceId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return sinceId;
		}
	}

	public ChannelListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Channel channel = ChannelTable.parseCursor(cursor);
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
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(mResource, parent, false);
		ViewHolder holder = new ViewHolder(view);

		view.setTag(holder);
		return view;
	}
}