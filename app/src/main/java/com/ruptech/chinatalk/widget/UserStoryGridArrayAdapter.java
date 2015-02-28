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
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.HotListCursorAdapter.ViewHolder;

public class UserStoryGridArrayAdapter extends ArrayAdapter<UserPhoto> {
	protected Context mContext;

	protected LayoutInflater mInflater;

	public UserStoryGridArrayAdapter(Context context) {
		super(context, mResource);
		mContext = context;
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	private static final int mResource = R.layout.item_sub_tab_hot; // xml布局文件

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final UserPhoto userPhoto = getItem(position);

		View view;
		final HotListCursorAdapter.ViewHolder holder;

		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);

			holder = new ViewHolder(view);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		String picUrl = userPhoto.getPic_url();
		if (!picUrl.equals(holder.picImgView.getTag())) {
			ImageManager.imageLoader.displayImage(App.readServerAppInfo()
					.getServerMiddle(userPhoto.getPic_url()),
					holder.picImgView, ImageManager.getOptionsLandscape());
			holder.picImgView.setTag(userPhoto.getPic_url());
		}

		holder.userLangImgView.setImageResource(Utils.getLanguageFlag(userPhoto
				.getLang()));
		return view;
	}
}