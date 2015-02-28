package com.ruptech.chinatalk.ui.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.ui.story.ChannelPopularListActivity;
import com.ruptech.chinatalk.widget.DiscoverChannelArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

public class SubDiscoverChannelFragment extends ListFragment implements
		OnRefreshListener {

	public static Fragment newInstance() {
		SubDiscoverChannelFragment fragment = new SubDiscoverChannelFragment();
		return fragment;
	}

	private String keyword;
	private DiscoverFragment parentFragment;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.emptyTextView)
	View emptyTextView;

	DiscoverChannelArrayAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.sub_tab_discover, container, false);
		ButterKnife.inject(this, v);
		return v;
	}

	@Override
	public void onRefresh(boolean isUp) {
		mAdapter.getChannelList(keyword, isUp);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getListView().setEmptyView(emptyTextView);
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mAdapter = new DiscoverChannelArrayAdapter(getActivity(), swypeLayout,
				R.layout.item_sub_tab_channel);
		getListView().setAdapter(mAdapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Channel channel = mAdapter.getItem(position);
				Intent intent = new Intent(getActivity(),
						ChannelPopularListActivity.class);
				intent.putExtra(ChannelPopularListActivity.EXTRA_CHNANEL,
						channel);
				startActivity(intent);
			}
		});

		parentFragment = (DiscoverFragment) getParentFragment();

		keyword = parentFragment.getKeyword();

	}

	public void setChannelList(List<Channel> list) {
		mAdapter.clear();
		mAdapter.addAll(list);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void clear() {
		mAdapter.clear();
	}
}