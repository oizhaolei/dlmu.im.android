package com.ruptech.chinatalk.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class SegmentTabLayout extends LinearLayout implements OnClickListener {

	public interface OnSegmentClickListener {
		public void onSegmentClick(int viewId);
	}

	private OnSegmentClickListener tabClickListener;
	private View selectedView;
	static final String LOG_TAG = "SegmentTabLayout";

	private final List<Integer> mTabs = new ArrayList<Integer>();

	public SegmentTabLayout(Context context) {
		super(context);
	}

	public SegmentTabLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SegmentTabLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void clickTab(int viewId) {
		View view = this.findViewById(viewId);
		if (view != null)
			onClick(view);
	}

	public void addTab(int resId) {
		mTabs.add(Integer.valueOf(resId));
	}

	private View createTabView(int textID) {

		View tab = LayoutInflater.from(getContext()).inflate(
				R.layout.item_segment_btn, null);
		TextView textView = (TextView) findById(tab, R.id.tab_textview);

		textView.setText(textID);
		Utils.setTextColor(textView);
		return tab;
	}

	@Override
	public void onClick(View v) {
		if (selectedView != v && tabClickListener != null)
			tabClickListener.onSegmentClick(v.getId());

		if (selectedView != null)
			selectedView.setSelected(false);
		v.setSelected(true);
		selectedView = v;

	}

	public void populateTab() {
		for (int i = 0; i < mTabs.size(); i++) {
			int resId = mTabs.get(i).intValue();
			View tabView = createTabView(resId);
			tabView.setId(resId);
			tabView.setOnClickListener(this);
			int width = getResources().getDimensionPixelSize(R.dimen.tab_width);
			int oneDP = getResources().getDimensionPixelSize(R.dimen.tab_line);
			LayoutParams params = new LinearLayout.LayoutParams(width,
					LayoutParams.MATCH_PARENT);

			params.setMargins(0, 0, -oneDP, 0);
			tabView.setLayoutParams(params);

			if (i == 0)
				tabView.setBackgroundResource(R.drawable.segment_bg_left);
			else if (i == (mTabs.size() - 1))
				tabView.setBackgroundResource(R.drawable.segment_bg_right);
			else
				tabView.setBackgroundResource(R.drawable.segment_bg_middle);
			addView(tabView);
		}
	}

	public void setNewCountForTab(int count, int tabIndex) {
		if (tabIndex < 0 || tabIndex >= mTabs.size())
			return;

		View tabView = getChildAt(tabIndex);

		if (tabView != null) {
			TextView countTextView = (TextView) tabView
					.findViewById(R.id.tab_number_icon);
			ImageView smallNewMark = (ImageView) tabView
					.findViewById(R.id.tab_new_icon);

			if (countTextView == null || smallNewMark == null)
				return;

			if (count == 0) {
				countTextView.setVisibility(View.INVISIBLE);
				smallNewMark.setVisibility(View.INVISIBLE);
			} else if (count < 10) {
				countTextView.setText(String.valueOf(count));
				countTextView.setVisibility(View.VISIBLE);
				smallNewMark.setVisibility(View.INVISIBLE);
			} else {
				countTextView.setVisibility(View.INVISIBLE);
				smallNewMark.setVisibility(View.VISIBLE);
			}
		}

		invalidate();
		getParent().requestLayout();
	}

	public void setOnSegmentClickListener(OnSegmentClickListener listener) {
		tabClickListener = listener;
	}

}