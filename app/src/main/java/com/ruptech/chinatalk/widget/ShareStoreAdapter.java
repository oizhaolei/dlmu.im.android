package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ShareStoreAdapter extends ArrayAdapter<Map<String, String>> {
	static class ViewHolder {
		@InjectView(R.id.item_share_story_image)
		ImageView thumb;
		@InjectView(R.id.item_share_story_text)
		TextView title;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	static final String TAG = Utils.CATEGORY
			+ TranslatorListAdapter.class.getSimpleName();
	private static final int mResource = R.layout.item_share_story;
	private final LayoutInflater mInflater;
	public ShareStoreAdapter(Context context) {
		super(context, mResource);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, null);
			holder = new ViewHolder(view);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		view.setTag(holder);

		final Map<String, String> shareMap = getItem(position);

		holder.thumb.setBackgroundResource(Integer.valueOf(shareMap
				.get("background")));
		holder.thumb.setImageResource(Integer.valueOf(shareMap.get("thumb")));
		holder.title.setText(shareMap.get("title"));

		return view;
	}

}
