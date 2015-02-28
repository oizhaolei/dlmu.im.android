/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public class ChannelListArrayAdapter extends ArrayAdapter<Channel> {
	static final String TAG = Utils.CATEGORY
			+ TranslatorListAdapter.class.getSimpleName();

	private static final int mResource = R.layout.item_sub_tab_channel;
	private final LayoutInflater mInflater;
	private final Context mContext;

	public ChannelListArrayAdapter(Context context) {
		super(context, mResource);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		final ChannelListCursorAdapter.ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, null);
			holder = new ChannelListCursorAdapter.ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ChannelListCursorAdapter.ViewHolder) view.getTag();
		}

		Channel channel = getItem(position);
		String picUrl = channel.getPic_url();
		if (!picUrl.equals(holder.picImgView.getTag())) {
			ImageManager.imageLoader.displayImage(App.readServerAppInfo()
					.getServerThumbnail(channel.getPic_url()),
					holder.picImgView, ImageManager.getOptionsLandscape());
			holder.picImgView.setTag(picUrl);
		}
		holder.titleTextView.setText(channel.getTitle());
		holder.popularTextView.setText(String.valueOf(channel
				.getPopular_count()));
		holder.fansTextView
				.setText(String.valueOf(channel.getFollower_count()));
		return view;
	}
}