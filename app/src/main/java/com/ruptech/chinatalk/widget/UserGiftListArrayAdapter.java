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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserGiftListArrayAdapter extends ArrayAdapter<Gift> {
	static class ViewHolder {
		@InjectView(R.id.item_user_gift_thumb_imageview)
		ImageView userGiftThumb;
		@InjectView(R.id.item_user_gift_title_textview)
		TextView userTitleTextView;
		@InjectView(R.id.item_user_gift_quantity_textview)
		TextView userQuantityTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
			setItemSize(userGiftThumb);
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

	private static final int mResource = R.layout.item_user_gift; // xml布局文件

	private final LayoutInflater mInflater;
	private final Context mContext;

	public UserGiftListArrayAdapter(Context context) {
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
		Utils.setGiftPicImage(holder.userGiftThumb, gift.getPic_url());

		holder.userQuantityTextView.setText(mContext.getString(
				R.string.story_title_gift, gift.getQuantity()));
		holder.userTitleTextView.setText(gift.getTitle());
		return view;
	}
}