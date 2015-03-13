package com.ruptech.chinatalk.ui.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;
import com.ruptech.chinatalk.widget.UserPhotoListArrayAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfilePopularFragment extends ScrollTabHolderFragment implements
		OnRefreshListener {

	public static Fragment newInstance() {
		ProfilePopularFragment fragment = new ProfilePopularFragment();
		return fragment;
	}

	protected final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UserPhoto story = (UserPhoto) intent.getExtras().getSerializable(
					CommonUtilities.EXTRA_MESSAGE);
			if (story != null) {
				mUserPhotoAdapter.changeUserPhoto(story);
			}
		}
	};

	private UserPhotoListArrayAdapter mUserPhotoAdapter;

	private static final String TAG = Utils.CATEGORY
			+ ProfilePopularFragment.class.getSimpleName();

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.emptyview_text)
	TextView emptyTextView;

	@InjectView(R.id.activity_popular_listView)
	ListView mListView;

	protected User mUser;
	private LayoutInflater mInflater;
	protected long mUserId;

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0 && mListView.getFirstVisiblePosition() >= 1) {
			return;
		}

		mListView.setSelectionFromTop(1, scrollHeight);

	}

	protected String getStoryType() {
		return AbstractUserStoryListActivity.STORY_TYPE_USER;
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

	/**
	 * Create the view for this fragment, using the arguments given to it.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mInflater = inflater;
		View v = inflater.inflate(R.layout.profile_tab, container, false);
		ButterKnife.inject(this, v);

		this.getActivity().registerReceiver(mHandleMessageReceiver,
				new IntentFilter(CommonUtilities.STORY_CONTENT_MESSAGE_ACTION));

		return v;
	}

	@Override
	public void onDestroy() {
		try {
			this.getActivity().unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	public void onRefresh(boolean isUp) {
		mUserPhotoAdapter.getUserPhotoList(isUp);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mUser = getUserFromExtras();
		if (mUser == null) {
			mUserId = getUserIdFromExtras();
		} else {
			mUserId = mUser.getId();
		}

		View placeHolderView = createPlaceHolderView(R.dimen.header_height);
		mListView.addHeaderView(placeHolderView);
		mListView.setOnScrollListener(this);

		mUserPhotoAdapter = new UserPhotoListArrayAdapter(this.getActivity(),
				swypeLayout, R.layout.item_story_user_photo_middle, mUserId,
				getStoryType(), emptyTextView);
		mListView.setAdapter(mUserPhotoAdapter);

		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mUserPhotoAdapter.getUserPhotoList(true);

	}
}