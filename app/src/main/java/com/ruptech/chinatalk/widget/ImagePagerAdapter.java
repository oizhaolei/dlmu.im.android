/**
 *
 */
package com.ruptech.chinatalk.widget;

import static butterknife.ButterKnife.findById;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public class ImagePagerAdapter extends PagerAdapter {
	private final LayoutInflater inflater;
	private List<String> imageUrlList;
	private final ImageProgressBar imageProgressBar;
	private ImageViewTouch imageView;

	public ImagePagerAdapter(Context context, ImageProgressBar progressBar) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.imageProgressBar = progressBar;
	}

	@Override
	public void destroyItem(ViewGroup view, int position, Object object) {
		((ViewPager) view).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}

	@Override
	public int getCount() {
		return imageUrlList.size();
	}

	public List<String> getImageUrlList() {
		return imageUrlList;
	}

	public ImageView getImageView() {
		return imageView;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	public String getUrl(int pos) {
		return imageUrlList.get(pos);
	}

	@Override
	public Object instantiateItem(ViewGroup view, final int position) {
		View imageLayout = inflater.inflate(R.layout.item_image_view, view,
				false);

		imageView = (ImageViewTouch) findById(imageLayout, R.id.imageView);
		imageView.setDisplayType(DisplayType.FIT_TO_SCREEN);

		String url = imageUrlList.get(position);
		if (!Utils.isEmpty(url)) {
			ImageManager.imageLoader.displayImage(url, imageView, ImageManager
					.getImageOptionsPortrait(), ImageViewActivity
					.createImageLoadingListener(imageProgressBar),
					ImageViewActivity
							.createLoadingProgresListener(imageProgressBar));
		}
		((ViewPager) view).addView(imageLayout, 0);
		return imageLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	public void setImageUrlList(List<String> imageUrlList) {
		this.imageUrlList = imageUrlList;
	}

	@Override
	public void startUpdate(View container) {
	}
}