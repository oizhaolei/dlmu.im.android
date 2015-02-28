package com.ruptech.chinatalk.ui.user;

import android.support.v4.app.Fragment;

import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.utils.Utils;

public class ProfileLikeFragment extends ProfilePopularFragment {

	public static Fragment newInstance() {
		ProfileLikeFragment fragment = new ProfileLikeFragment();
		return fragment;
	}

	private static final String TAG = Utils.CATEGORY
			+ ProfileLikeFragment.class.getSimpleName();

	@Override
	protected String getStoryType() {
		return AbstractUserStoryListActivity.STORY_TYPE_FAVORITE;
	}

}