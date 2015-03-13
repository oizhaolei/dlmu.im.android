package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import static butterknife.ButterKnife.findById;

public class TopGallery extends LinearLayout {
	public abstract interface OnGalleryItemClickListener {
		public void onGalleryItemClick(TopGallery gallery, int position);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private OnGalleryItemClickListener listener;
	Context mContext;

	private List<User> mUserList;

	public TopGallery(Context context) {
		super(context);
		mContext = context;
	}

	public TopGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public User getData(int position) {
		if (mUserList == null || mUserList.size() <= position)
			return null;
		else
			return mUserList.get(position);
	}

	public void setDataList(List<User> userList) {
		mUserList = userList;
		removeAllViews();
		LayoutInflater mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < mUserList.size(); i++) {
			final int position = i;
			View cell = mInflater.inflate(R.layout.item_top_gallery, this,
					false);
			ImageView imageView = (ImageView) findById(cell,
					R.id.item_imageview);
			ImageView imageMaskView = (ImageView) findById(cell, R.id.item_mask);
			ImageView langImageView = (ImageView) findById(cell,
					R.id.item_lang_imageview);
			addView(cell);

			if (listener != null) {
				imageMaskView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						listener.onGalleryItemClick(TopGallery.this, position);
					}

				});
			}

			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			if (!Utils.isEmpty(mUserList.get(i).getPic_url())) {
				String imageUrl = App.readServerAppInfo().getServerThumbnail(
						mUserList.get(i).getPic_url());
				ImageManager.imageLoader.displayImage(imageUrl, imageView,
						ImageManager.getOptionsPortrait());
			}
			if (!Utils.isEmpty(mUserList.get(i).getLang())) {
				langImageView.setImageResource(Utils.getLanguageFlag(mUserList
						.get(i).getLang()));
			}

			cell.setBackgroundResource(R.drawable.preference_item);
			cell.setPadding(1, 1, 1, 1);

		}

	}

	public void setOnGalleryItemClickListener(
			OnGalleryItemClickListener listener) {
		this.listener = listener;
	}
}
