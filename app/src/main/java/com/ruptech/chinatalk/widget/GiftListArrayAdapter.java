/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.utils.Utils;

public class GiftListArrayAdapter extends ArrayAdapter<Gift> {
	static class ViewHolder {
		@InjectView(R.id.item_gift_thumb_imageview)
		ImageView giftThumb;
		@InjectView(R.id.item_gift_cost_textview)
		TextView costTextView;
		@InjectView(R.id.item_gift_title_textview)
		TextView titleTextView;
		@InjectView(R.id.item_gift_charm_textview)
		TextView charmTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
			setItemSize(giftThumb);
		}

		private void setItemSize(View itemView) {
			int gapSize = itemView.getContext().getResources()
					.getDimensionPixelSize(R.dimen.grid_gap);
			int displayWidth = Utils.getDisplayWidth(itemView.getContext());
			int cellSize = (displayWidth - gapSize * 3) / 4;
			RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
					cellSize, cellSize);
			itemView.setLayoutParams(imageParams);
		}
	}

	private static final int mResource = R.layout.item_gift; // xml布局文件

	private final LayoutInflater mInflater;
	private final Context mContext;

	public GiftListArrayAdapter(Context context) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View view;
		final ViewHolder holder;

		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();

		}

		final Gift gift = getItem(position);

		Utils.setGiftPicImage(holder.giftThumb, gift.getPic_url());

		holder.costTextView.setText(gift.getCost_point()
				+ mContext.getString(R.string.point));
		holder.titleTextView.setText(gift.getTitle());
		holder.charmTextView.setText(mContext.getString(R.string.gift_charm,
				gift.getCharm_point()));
		return view;
	}
}