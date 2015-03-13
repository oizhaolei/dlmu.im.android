/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveChannelPhotoListTask;
import com.ruptech.chinatalk.ui.story.ChannelPopularListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;

import java.util.List;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;

public class ChannelPhotoListArrayAdapter extends UserStoryListArrayAdapter {
	public static void changeLocalChannel(Channel channel) {
		ContentValues v = new ContentValues();
		v.put(ChannelTable.Columns.POPULAR_COUNT, channel.getPopular_count());
		v.put(ChannelTable.Columns.FOLLOWER_COUNT, channel.getFollower_count());
		v.put(ChannelTable.Columns.IS_FOLLOWER, channel.getIs_follower());
		v.put(ChannelTable.Columns.PIC_URL, channel.getPic_url());
		if (App.channelDAO.isExists(channel)) {
			App.channelDAO.updateChannel(channel.getId(), v);
		} else {
			App.channelDAO.insertChannel(channel);
		}
		CommonUtilities.broadcastChannel(App.mContext);
		CommonUtilities.broadcastChannelList(App.mContext);
	}

	private final String channelType;

	private Channel channel;
	protected GenericTask mRetrieveChannelPhotoListTask;

	private final TaskListener mRetrieveChannelPhotoListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveChannelPhotoListTask channelPhotoListTask = (RetrieveChannelPhotoListTask) task;
			mSwypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				channel = channelPhotoListTask.getChannel();
				if (channel != null) {
					changeLocalChannel(channel);
					List<UserPhoto> mChannelPhotoList = channelPhotoListTask
							.getChannelPhotoList();

					if (mChannelPhotoList.size() == 0) {
						if (channelPhotoListTask.isTop()) {
							Toast.makeText(mContext, R.string.no_new_data,
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(mContext, R.string.no_more_data,
									Toast.LENGTH_SHORT).show();
						}
					}
					if (mChannelPhotoList != null
							&& mChannelPhotoList.size() > 0) {
						int pos = 0;
						for (UserPhoto userPhoto : mChannelPhotoList) {
							if (channelPhotoListTask.isTop()) {
								insert(userPhoto, pos++);
							} else {
								add(userPhoto);
							}
						}
					}
				} else {
					Toast.makeText(mContext, R.string.channel_is_not_exist,
							Toast.LENGTH_SHORT).show();
					App.channelDAO.deleteChannel(channelPhotoListTask
							.getChannelId());
					CommonUtilities.broadcastChannelList(mContext);
					ChannelPopularListActivity.close();
				}
			} else {
				Toast.makeText(mContext, channelPhotoListTask.getMsg(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			mSwypeLayout.setRefreshing(true);
		}

	};

	private static Context mContext;

	public ChannelPhotoListArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout, int resId, String channelType,
			Channel channel) {
		super(context, swypeLayout, resId);
		this.channelType = channelType;
		this.channel = channel;
		mContext = context;
	}

	public void doRetrieveUserPhotoList(boolean isTop) {
		long sinceId;
		long maxId;
		long sinceGood;
		long maxGood;
		if (isTop) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
			sinceGood = getSinceGood();
			maxGood = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
			maxGood = getMaxGood();
			sinceGood = AppPreferences.ID_IMPOSSIBLE;
		}

		if (mRetrieveChannelPhotoListTask != null
				&& mRetrieveChannelPhotoListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveChannelPhotoListTask = new RetrieveChannelPhotoListTask(isTop,
				maxId, sinceId, maxGood, sinceGood, channel.getId(),
				channelType);
		mRetrieveChannelPhotoListTask
				.setListener(mRetrieveChannelPhotoListTaskListener);
		mRetrieveChannelPhotoListTask.execute();
	}

	protected long getMaxGood() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return super.getItem(super.getCount() - 1).getGood();
		}
	}

	protected long getMaxId() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return super.getItem(super.getCount() - 1).getId();
		}
	}

	protected long getSinceGood() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return super.getItem(0).getGood();
		}
	}

	protected long getSinceId() {
		if (super.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return super.getItem(0).getId();
		}
	}
}