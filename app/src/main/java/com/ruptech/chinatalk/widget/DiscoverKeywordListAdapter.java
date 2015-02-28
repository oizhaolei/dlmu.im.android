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
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.DiscoverTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class DiscoverKeywordListAdapter extends ArrayAdapter<String> {

	class ViewHolder {
		@InjectView(R.id.fullname)
		TextView nameTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	protected GenericTask mTask;

	protected final TaskListener mTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			DiscoverTask discoverTask = (DiscoverTask) task;
			if (result == TaskResult.OK) {
				List<String> list = discoverTask.getKeywordList();
				clear();
				addAll(list);

			} else {
				String msg = task.getMsg();
				onRetrieveUserListFailure(msg);
			}
		}

	};

	private final LayoutInflater viewInflater;
	private static Context mContext;

	public DiscoverKeywordListAdapter(Context context, int resId) {
		super(context, resId);
		mContext = context;
		viewInflater = LayoutInflater.from(getContext());
	}

	public void getList(String keyword) {

		if (mTask != null && mTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mTask = new DiscoverTask(true, DiscoverTask.TYPE_KEYWORD, keyword,
				AppPreferences.ID_IMPOSSIBLE, AppPreferences.ID_IMPOSSIBLE);
		mTask.setListener(mTaskListener);
		mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {

			convertView = viewInflater.inflate(R.layout.item_keyword, parent,
					false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String name = getItem(position);
		holder.nameTextView.setText(name);

		return convertView;
	}

	protected void onRetrieveUserListFailure(String msg) {
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

}