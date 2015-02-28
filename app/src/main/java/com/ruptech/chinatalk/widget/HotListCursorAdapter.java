/**
 *
 */
package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrievePopularStoryTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public class HotListCursorAdapter extends CursorAdapter {

	public static class ViewHolder {
		@InjectView(R.id.item_hot_grid_image)
		ImageView picImgView;
		@InjectView(R.id.tem_hot_grid_image_mask)
		ImageView maskiew;
		@InjectView(R.id.item_hot_grid_lang_imageview)
		ImageView userLangImgView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
			setItemSize(view);
		}

		private void setItemSize(View gridItemView) {

			int gapSize = gridItemView.getContext().getResources()
					.getDimensionPixelSize(R.dimen.grid_gap);
			int displayWidth = Utils.getDisplayWidth(gridItemView.getContext());
			int cellSize = (displayWidth - gapSize * 2) / 3;
			AbsListView.LayoutParams imageParams = new AbsListView.LayoutParams(
					cellSize, cellSize);
			gridItemView.setLayoutParams(imageParams);
		}

	}

	private static final int mResource = R.layout.item_sub_tab_hot; // xml布局文件

	private GenericTask mRetrieveHotListTask;
	protected Context mContext;
	private SwipeRefreshLayout swypeLayout;
	private GridView mHotListView;

	protected LayoutInflater mInflater;

	private final TaskListener mRetrieveHotListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrievePopularStoryTask hotListTask = (RetrievePopularStoryTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<UserPhoto> hotList = hotListTask.getPopularStoryList();
				if (hotList.size() == 0) {
					if (hotListTask.isTop()) {
						Toast.makeText(mContext, R.string.no_new_data,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, R.string.no_more_data,
								Toast.LENGTH_SHORT).show();
					}
				}
				if (hotList != null && hotList.size() > 0) {
					for (UserPhoto hotUserPhoto : hotList) {
						App.hotUserPhotoDAO
								.insertHotUserPhotoTable(hotUserPhoto);
					}
					doChangeAdapterCursor();
				}

				if (hotListTask.isTop() && mHotListView != null) {
					mHotListView.setSelection(0);
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrievePopularStoryTask hotListTask = (RetrievePopularStoryTask) task;
			swypeLayout.setProgressTop(hotListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	public HotListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}

	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final UserPhoto userPhoto = UserPhotoTable.parseCursor(cursor);
		String picUrl = userPhoto.getPic_url();
		if (!picUrl.equals(holder.picImgView.getTag())) {
			ImageManager.imageLoader.displayImage(App.readServerAppInfo()
					.getServerMiddle(userPhoto.getPic_url()),
					holder.picImgView, ImageManager.getOptionsLandscape());
			holder.picImgView.setTag(userPhoto.getPic_url());
		}

		holder.userLangImgView.setImageResource(Utils.getLanguageFlag(userPhoto
				.getLang()));

	}

	public void doChangeAdapterCursor() {
		Cursor cursor = fetchUserPhotoListCursor();
		if (cursor == null || cursor.getCount() == 0) {
			doRetrieveHotList(true, mHotListView);
		} else {
			changeCursor(cursor);
		}
	}

	public void doRetrieveHotList(boolean top, GridView hotListView) {
		if (mRetrieveHotListTask != null
				&& mRetrieveHotListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mHotListView = hotListView;
		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}

		mRetrieveHotListTask = new RetrievePopularStoryTask(top, maxId, sinceId);
		mRetrieveHotListTask.setListener(mRetrieveHotListTaskListener);

		mRetrieveHotListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private Cursor fetchUserPhotoListCursor() {
		Cursor hotUserPhotoListCursor = App.hotUserPhotoDAO
				.fetchHotPopularCursor();
		return hotUserPhotoListCursor;
	}

	private long getMaxId() {
		long maxId = App.hotUserPhotoDAO.getMinId();
		if (maxId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return maxId;
		}
	}

	public long getSinceId() {
		long sinceId = App.hotUserPhotoDAO.getMaxId();
		if (sinceId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return sinceId;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(mResource, parent, false);

		ViewHolder holder = new ViewHolder(view);

		view.setTag(holder);

		return view;
	}

	public void setSwypeLayout(SwipeRefreshLayout swypeLayout) {
		this.swypeLayout = swypeLayout;
	}
}