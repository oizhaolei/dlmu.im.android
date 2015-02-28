package com.ruptech.chinatalk.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

	protected List<PagerItem> mTabs = new ArrayList<PagerItem>();

	TabFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public void addTabItem(PagerItem item) {
		mTabs.add(item);
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public Fragment getItem(int i) {
		return mTabs.get(i).createFragment();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTabs.get(position).getTitle();
	}

}
