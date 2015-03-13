package com.ruptech.chinatalk.ui.user;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserGiftListTask;
import com.ruptech.chinatalk.ui.gift.GiftDonateActivity;
import com.ruptech.chinatalk.widget.UserGiftListArrayAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfilePresentFragment extends ScrollTabHolderFragment {
	private UserGiftListArrayAdapter mUserGiftListArrayAdapter;

	private static GenericTask mRetrieveUserGiftListTask;

	@InjectView(R.id.activity_user_gift_grid_view)
	HeaderGridView uesrGiftGridView;

	@InjectView(R.id.emptyview_text)
	TextView emptyTextView;

	private final TaskListener mRetrieveUserGiftListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserGiftListTask retrieveUserGiftTask = (RetrieveUserGiftListTask) task;
			if (result == TaskResult.OK) {
				if (result == TaskResult.OK) {
					List<Gift> giftList = retrieveUserGiftTask.getGiftList();
					mUserGiftListArrayAdapter.clear();
					if (giftList.size() > 0) {
						emptyTextView.setVisibility(View.GONE);
						mUserGiftListArrayAdapter.addAll(giftList);
					} else {
						emptyTextView.setVisibility(View.VISIBLE);
					}
				} else {
					Toast.makeText(getActivity(),
							retrieveUserGiftTask.getMsg(), Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	protected long mUserPhotoId;

	protected long mUserId;

	@Override
	public void adjustScroll(int scrollHeight) {
	}

	public void doRetrieveGiftList(long userId, long userPhotoId) {
		if (mRetrieveUserGiftListTask != null
				&& mRetrieveUserGiftListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveUserGiftListTask = new RetrieveUserGiftListTask(userId,
				userPhotoId);
		mRetrieveUserGiftListTask
				.setListener(mRetrieveUserGiftListTaskListener);

		mRetrieveUserGiftListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public User getUserFromExtras() {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ProfileActivity.EXTRA_USER);
			return user;
		}
		return null;
	}

	public long getUserIdFromExtras() {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(ProfileActivity.EXTRA_USER_ID);
			return userId;
		}
		return 0;
	}

	public long getUserPhotoIdFromExtras() {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			long userId = extras
					.getLong(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID);
			return userId;
		}
		return 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.profile_gift_tab, container, false);
		ButterKnife.inject(this, v);
		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		User mUser = getUserFromExtras();
		if (mUser == null) {
			mUserId = getUserIdFromExtras();
		} else {
			mUserId = mUser.getId();
		}

		mUserPhotoId = getUserPhotoIdFromExtras();

		View placeHolderView = createPlaceHolderView(R.dimen.header_height);
		uesrGiftGridView.addHeaderView(placeHolderView);
		uesrGiftGridView.setOnScrollListener(this);
		mUserGiftListArrayAdapter = new UserGiftListArrayAdapter(getActivity());
		uesrGiftGridView.setAdapter(mUserGiftListArrayAdapter);
		if (mUserGiftListArrayAdapter.getCount() <= 0) {
			doRetrieveGiftList(mUserId, mUserPhotoId);
		}
	}
}