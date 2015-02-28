package com.ruptech.chinatalk.ui.friend;

import android.support.v4.app.Fragment;

public class LeaderboardMonthCharmFragment extends LeaderboardLevelFragment {
	public static Fragment newInstance() {
		LeaderboardMonthCharmFragment fragment = new LeaderboardMonthCharmFragment();
		return fragment;
	}

	@Override
	protected String getLeaderBoardType() {
		return FriendsLeaderboardListActivity.LEADERBOARD_TYPE_CHARM_MONTH;
	}
}