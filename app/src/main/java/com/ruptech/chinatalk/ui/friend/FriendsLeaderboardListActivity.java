package com.ruptech.chinatalk.ui.friend;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.fragment.SlidingTabLayout;
import com.ruptech.chinatalk.ui.user.ScrollTabHolder;
import com.ruptech.chinatalk.widget.TapPagerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendsLeaderboardListActivity extends ActionBarActivity implements
		ScrollTabHolder, ViewPager.OnPageChangeListener {

	public static final String LEADERBOARD_TYPE_LEVEL = "level";

	public static final String LEADERBOARD_TYPE_CHARM_WEEK = "charm_week";

	public static final String LEADERBOARD_TYPE_CHARM_MONTH = "charm_month";

	private LayoutInflater mInflater;
	TapPagerAdapter mPagerAdapter;
	@InjectView(R.id.pager)
	ViewPager mViewPager;

	@InjectView(R.id.sliding_tabs)
	SlidingTabLayout mTabLayout;

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_leaderboard_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.leaderboard);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 加载画面
		setupComponents();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageSelected(int position) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
	}

	private void setupComponents() {
		mPagerAdapter = new TapPagerAdapter(this, getSupportFragmentManager());
		mPagerAdapter.setTabHolderScrollingContent(this);
		mPagerAdapter.addTab(R.string.leaderboard_level,
				LeaderboardLevelFragment.class);
		mPagerAdapter.addTab(R.string.leaderboard_week_charm,
				LeaderboardWeekCharmFragment.class);
		mPagerAdapter.addTab(R.string.leaderboard_month_charm,
				LeaderboardMonthCharmFragment.class);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);

		mTabLayout.setViewPager(mViewPager);
		mTabLayout.setOnPageChangeListener(this);

	}
}