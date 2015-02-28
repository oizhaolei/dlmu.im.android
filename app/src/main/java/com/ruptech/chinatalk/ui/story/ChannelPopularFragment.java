package com.ruptech.chinatalk.ui.story;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.ui.user.ScrollTabHolderFragment;
import com.ruptech.chinatalk.widget.ChannelPhotoListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

public class ChannelPopularFragment extends ScrollTabHolderFragment implements
		OnRefreshListener {

	public static Fragment newInstance() {
		ChannelPopularFragment fragment = new ChannelPopularFragment();
		return fragment;
	}

	protected ChannelPhotoListArrayAdapter mChannelPhotoListArrayAdapter;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_popular_listView)
	ListView mListView;

	protected Channel channel;

	@Override
	public void adjustScroll(int scrollHeight) {
		if (scrollHeight == 0 && mListView.getFirstVisiblePosition() >= 1) {
			return;
		}

		mListView.setSelectionFromTop(1, scrollHeight);

	}

	public Channel getChannelFromExtras() {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			Channel channel = (Channel) extras
					.get(ChannelPopularListActivity.EXTRA_CHNANEL);
			return channel;
		}
		return null;
	}

	protected String getChannelType() {
		return ChannelPopularListActivity.CHANNEL_TYPE_POPULAR;
	}

	/**
	 * Create the view for this fragment, using the arguments given to it.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.channel_tab, container, false);
		ButterKnife.inject(this, v);

		return v;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onRefresh(boolean isUp) {
		mChannelPhotoListArrayAdapter.doRetrieveUserPhotoList(isUp);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		channel = getChannelFromExtras();

		View placeHolderView = createPlaceHolderView(R.dimen.channel_header_height);
		mListView.addHeaderView(placeHolderView);
		mListView.setOnScrollListener(this);

		mChannelPhotoListArrayAdapter = new ChannelPhotoListArrayAdapter(
				getActivity(), swypeLayout, R.layout.item_story_user_photo,
				getChannelType(), channel);
		mListView.setAdapter(mChannelPhotoListArrayAdapter);

		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mChannelPhotoListArrayAdapter.doRetrieveUserPhotoList(true);
	}
}