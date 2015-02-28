package com.ruptech.chinatalk.ui.friend;

import android.support.v4.app.Fragment;

public class LeaderboardWeekCharmFragment extends LeaderboardLevelFragment {
	public static Fragment newInstance() {
		LeaderboardWeekCharmFragment fragment = new LeaderboardWeekCharmFragment();
		return fragment;
	}

	@Override
	protected String getLeaderBoardType() {
		return FriendsLeaderboardListActivity.LEADERBOARD_TYPE_CHARM_WEEK;
	}
}