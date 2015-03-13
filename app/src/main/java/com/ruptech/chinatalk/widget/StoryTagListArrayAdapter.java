/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruptech.chinatalk.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class StoryTagListArrayAdapter extends BaseAdapter {
	static class ViewHolder {
		@InjectView(R.id.item_story_tag_textview)
		TextView storyTagTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_story_tag; // xml布局文件
	private List<String> storyTagArray;

	private LayoutInflater mInflater;
	private boolean isLocal = false;

	public StoryTagListArrayAdapter(Context context) {
        this.storyTagArray = new ArrayList<>();
        if (context != null) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

	@Override
	public int getCount() {
		return storyTagArray.size();
	}

	@Override
	public Object getItem(int position) {
		return storyTagArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);

			holder = new ViewHolder(view);

			if (isLocal)
				holder.storyTagTextView.setTextColor(parent.getContext()
						.getResources().getColor(R.color.text_gray));

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();

		}

		String tagName = storyTagArray.get(position);
		holder.storyTagTextView.setText(tagName);
		return view;
	}

	public void setData(List<String> data) {
		storyTagArray = data;
		this.notifyDataSetChanged();
	}

	public void setLocal() {
		this.isLocal = true;
	}

}