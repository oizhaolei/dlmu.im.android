package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.widget.DiscoverUserArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SubDiscoverUserFragment extends ListFragment implements
		OnRefreshListener {

	public static Fragment newInstance() {
		SubDiscoverUserFragment fragment = new SubDiscoverUserFragment();
		return fragment;
	}

	private String keyword;
	private DiscoverFragment parentFragment;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.emptyTextView)
	View emptyTextView;

	DiscoverUserArrayAdapter mAdapter;

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
		mAdapter.getUserList(keyword, isUp);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getListView().setEmptyView(emptyTextView);
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mAdapter = new DiscoverUserArrayAdapter(getActivity(), swypeLayout,
				R.layout.item_sub_tab_user);
		getListView().setAdapter(mAdapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				User user = mAdapter.getItem(position);
				Intent intent = new Intent(getActivity(),
						FriendProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_USER, user);
				getActivity().startActivity(intent);
			}
		});

		parentFragment = (DiscoverFragment) getParentFragment();
		mAdapter.setOnDiscoverListener(parentFragment);

		keyword = parentFragment.getKeyword();
		mAdapter.getUserList(keyword, true);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void clear() {
		mAdapter.clear();
	}
}