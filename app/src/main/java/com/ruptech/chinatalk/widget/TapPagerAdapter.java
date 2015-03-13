/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;

import com.ruptech.chinatalk.ui.user.ScrollTabHolder;
import com.ruptech.chinatalk.ui.user.ScrollTabHolderFragment;

import java.util.ArrayList;
import java.util.List;

public class TapPagerAdapter extends FragmentPagerAdapter {

	class PagerItem {
		public CharSequence mTitle;
		public Class<?> mClss;

		PagerItem(CharSequence title, Class<?> clss) {
			mTitle = title;
			mClss = clss;
		}
	}

	private final SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
	private ScrollTabHolder mListener;
	private final Activity mContext;
	protected final List<PagerItem> mTabs = new ArrayList<PagerItem>();

	public TapPagerAdapter(Activity context, FragmentManager fragmentManager) {
		super(fragmentManager);
		mContext = context;
		mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
	}

	public void addTab(int titleId, final Class<?> clss) {
		mTabs.add(new PagerItem(mContext.getString(titleId), clss));
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public Fragment getItem(int i) {
		PagerItem item = mTabs.get(i);
		ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) Fragment
				.instantiate(mContext, item.mClss.getName(), null);
		fragment.setPosition(i);
		mScrollTabHolders.put(i, fragment);
		if (mListener != null) {
			fragment.setScrollTabHolder(mListener);
		}

		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mTabs.get(position).mTitle;
	}

	public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
		return mScrollTabHolders;
	}

	public void setTabHolderScrollingContent(ScrollTabHolder listener) {
		mListener = listener;
	}
}