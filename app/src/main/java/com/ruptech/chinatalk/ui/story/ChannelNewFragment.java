package com.ruptech.chinatalk.ui.story;

import android.support.v4.app.Fragment;

import com.ruptech.chinatalk.utils.Utils;

public class ChannelNewFragment extends ChannelPopularFragment {

	private static final String TAG = Utils.CATEGORY
			+ ChannelNewFragment.class.getSimpleName();

	public static Fragment newInstance() {
		ChannelNewFragment fragment = new ChannelNewFragment();
		return fragment;
	}

	@Override
	protected String getChannelType() {
		return ChannelPopularListActivity.CHANNEL_TYPE_NEW;
	}
}