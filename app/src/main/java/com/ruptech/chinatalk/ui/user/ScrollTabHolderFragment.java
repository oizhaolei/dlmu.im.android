package com.ruptech.chinatalk.ui.user;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;

import com.ruptech.chinatalk.R;

public abstract class ScrollTabHolderFragment extends Fragment implements
		ScrollTabHolder, OnScrollListener {

	protected int mPosition;

	protected ScrollTabHolder mScrollTabHolder;

	public View createPlaceHolderView(int dimenId) {
		int height = getResources().getDimensionPixelSize(dimenId);

		LinearLayout placeHolder = new LinearLayout(this.getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				height);
		placeHolder.setLayoutParams(params);
		placeHolder.setBackgroundResource(R.color.main_background);
		return placeHolder;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (mScrollTabHolder != null)
			mScrollTabHolder.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount, mPosition);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		// nothing
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	public void setPosition(int pos) {
		mPosition = pos;
	}

	public void setScrollTabHolder(ScrollTabHolder scrollTabHolder) {
		mScrollTabHolder = scrollTabHolder;
	}

}