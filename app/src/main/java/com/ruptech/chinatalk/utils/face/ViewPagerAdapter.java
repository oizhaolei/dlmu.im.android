package com.ruptech.chinatalk.utils.face;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

	private final List<View> pageViews;

	public ViewPagerAdapter(List<View> pageViews) {
		super();
		this.pageViews = pageViews;
	}

	@Override
	public void destroyItem(ViewGroup view, int position, Object object) {
		((ViewPager) view).removeView(pageViews.get(position));
	}

	// 显示数目
	@Override
	public int getCount() {
		return pageViews.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public Object instantiateItem(ViewGroup view, final int position) {
		((ViewPager) view).addView(pageViews.get(position));
		return pageViews.get(position);
	}


	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}