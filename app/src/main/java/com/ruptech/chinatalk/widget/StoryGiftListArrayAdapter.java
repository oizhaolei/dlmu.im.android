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
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class StoryGiftListArrayAdapter extends ArrayAdapter<Gift> {
	static class ViewHolder {
		@InjectView(R.id.item_item_story_gift_user_thumb_imageview)
		ImageView userThumb;
		@InjectView(R.id.item_item_story_gift_user_nickname_textview)
		TextView userNickName;
		@InjectView(R.id.item_item_story_gift_user_content_textview)
		TextView userGiftContent;
		@InjectView(R.id.item_item_story_gift_user_gift_date_textview)
		TextView userGiftDateView;
		@InjectView(R.id.item_item_story_gift_thumb_imageview)
		ImageView giftThumb;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_story_gift; // xml布局文件

	private final LayoutInflater mInflater;
	private final Context mContext;

	public StoryGiftListArrayAdapter(Context context) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void bindView(View view, final Gift gift) {
		ViewHolder holder = (ViewHolder) view.getTag();
		Utils.setUserPicImage(holder.userThumb, gift.getUser_pic_url());

		String fullName = gift.getUser_fullname();
		String friendNickname = Utils.getFriendName(gift.getUserid(), fullName);

		String mFullName = Utils.isEmpty(fullName) ? "" : Utils.abbrString(
				fullName, 8);
		String mFriendNickname = Utils.isEmpty(friendNickname) ? mFullName
				: Utils.abbrString(friendNickname, 8);
		holder.userNickName.setText(mFriendNickname);
		holder.userGiftContent.setText(mContext.getString(
				R.string.gift_donate_count, gift.getQuantity()));
		holder.userGiftDateView.setText(DateCommonUtils
				.formatConvUtcDateString(gift.getCreate_date(), false, false));

		Utils.setGiftPicImage(holder.giftThumb, gift.getPic_url());

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

		Gift gift = getItem(position);
		bindView(view, gift);
		return view;
	}

}