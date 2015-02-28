/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserPhotoRemoveTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.UserStoryListCursorAdapter.ViewHolder;

public class UserStoryListArrayAdapter extends ArrayAdapter<UserPhoto> {

	protected final LayoutInflater mInflater;

	protected final Context mContext;
	protected int mResId;

	protected final SwipeRefreshLayout mSwypeLayout;

	public UserStoryListArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout, int resId) {
		super(context, resId);
		mResId = resId;
		mContext = context;
		mSwypeLayout = swypeLayout;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void changeUserPhoto(UserPhoto userPhoto) {
		for (int i = 0; i < getCount(); i++) {
			UserPhoto item = getItem(i);
			if (item.getId() == userPhoto.getId()) {
				item.mergeFrom(userPhoto);
				notifyDataSetChanged();
				break;
			}
		}
	}

	private void doDeletePhotoComment(final UserPhoto userPhoto,
			Context context) {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				GenericTask mUserPhotoRemoveTask = new UserPhotoRemoveTask(
						userPhoto.getId());

				mUserPhotoRemoveTask.setListener(new TaskAdapter() {
					@Override
					public void onPostExecute(GenericTask task,
							TaskResult result) {
						//
						remove(userPhoto);
						mSwypeLayout.setRefreshing(false);
					}

					@Override
					public void onPreExecute(GenericTask task) {
						mSwypeLayout.setRefreshing(true);
					}
				});
				mUserPhotoRemoveTask.execute();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(mContext, positiveListener, negativeListener,
				mContext.getString(R.string.delete_selected),
				mContext.getString(R.string.are_you_sure_delete_message));

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final UserPhoto userPhoto = getItem(position);

		View view;
		final UserStoryListCursorAdapter.ViewHolder holder;

		if (convertView == null) {
			view = mInflater.inflate(mResId, parent, false);

			holder = UserStoryListCursorAdapter.getViewHolder(view);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		if (userPhoto.getUserid() == App.readUser().getId()) {
			holder.deleteView.setVisibility(View.VISIBLE);
		} else {
			holder.deleteView.setVisibility(View.GONE);
		}
		OnClickListener onDeleteClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				doDeletePhotoComment(userPhoto, mContext);
			}
		};
		UserStoryListCursorAdapter.bindUserPhoto(mContext, holder, userPhoto,
				this, onDeleteClickListener, mSwypeLayout, isOriginal());
		return view;
	}

	protected boolean isOriginal() {
		return true;
	}

}