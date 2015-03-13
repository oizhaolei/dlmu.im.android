package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;

import static butterknife.ButterKnife.findById;

public class Gallery extends LinearLayout {
	public abstract interface OnGalleryItemClickListener {
		public void onGalleryItemClick(Gallery gallery, int position);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private OnGalleryItemClickListener listener;
	Context mContext;

	public Gallery(Context context) {
		super(context);
		mContext = context;
	}

	public Gallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	ImageView getImageView(String picUrl, int width, int height, int margin,
			boolean isFirst) {
		ImageView imageView = new ImageView(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
				height);
		if (isFirst) {
			params.setMargins(margin, margin, margin, margin);
		} else {
			params.setMargins(0, margin, margin, margin);
		}
		imageView.setLayoutParams(params);
		imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		if (!Utils.isEmpty(picUrl)) {
			ImageManager.imageLoader.displayImage(picUrl, imageView,
					ImageManager.getOptionsLandscape());
		}
		return imageView;
	}

	public void setImageList(ArrayList<String> mImageUrlList, int width,
			int height, int margin) {
		removeAllViews();
		LayoutInflater mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < mImageUrlList.size(); i++) {
			final int position = i;
			View cell = mInflater.inflate(R.layout.item_gallery, null);
			ImageView imageView = (ImageView) findById(cell,
					R.id.item_imageview);
			ImageView imageMaskView = (ImageView) findById(cell, R.id.item_mask);
			addView(cell);

			if (listener != null) {
				imageMaskView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						listener.onGalleryItemClick(Gallery.this, position);
					}

				});
			}
			ViewGroup.LayoutParams params = cell.getLayoutParams();
			params.width = width;
			params.height = height;
			if (i == 0) {
				((MarginLayoutParams) params).setMargins(margin, margin,
						margin, margin);
			} else {
				((MarginLayoutParams) params).setMargins(0, margin, margin,
						margin);
			}
			cell.setLayoutParams(params);

			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			if (!Utils.isEmpty(mImageUrlList.get(i))) {
				ImageManager.imageLoader.displayImage(mImageUrlList.get(i),
						imageView, ImageManager.getOptionsLandscape());
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
