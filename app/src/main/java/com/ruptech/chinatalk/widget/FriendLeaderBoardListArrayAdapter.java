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
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class FriendLeaderBoardListArrayAdapter extends ArrayAdapter<User> {
	static class ViewHolder {
		@InjectView(R.id.item_leaderboard_rank_textview)
		TextView friendRank;
		@InjectView(R.id.item_leaderboard_thumb_imageview)
		ImageView friendThumb;
		@InjectView(R.id.item_leaderboard_content_fullname_textview)
		TextView friendFullName;
		@InjectView(R.id.item_leaderboard_content_gender_imageview)
		ImageView genderImageView;
		@InjectView(R.id.item_leaderboard_content_lang_imageview)
		ImageView langImageView;
		@InjectView(R.id.item_leaderboard_content_level_textview)
		TextView levelImageView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_leaderboard; // xml布局文件

	protected LayoutInflater mInflater;

	public FriendLeaderBoardListArrayAdapter(Context context) {
		super(context, mResource);

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

		User user = getItem(position);
		holder.friendRank.setText(String.valueOf(position + 6));

		String fullName = Utils.getFriendName(user.getId(), user.getFullname());
		if (Utils.isEmpty(fullName)) {
			holder.friendFullName.setText("");
		} else {
			holder.friendFullName.setText(Utils.abbrString(fullName, 10));
		}

		Utils.setUserPicImage(holder.friendThumb, user.getPic_url());

		if (user.getGender() < 1) {
			holder.genderImageView.setVisibility(View.INVISIBLE);
		} else if (user.getGender() == AppPreferences.USERS_GENDER_MALE) {
			holder.genderImageView.setImageResource(R.drawable.male);
		} else if (user.getGender() == AppPreferences.USERS_GENDER_FEMALE) {
			holder.genderImageView.setImageResource(R.drawable.female);
		}

		holder.langImageView.setImageResource(Utils.getLanguageFlag(user.lang));
		holder.levelImageView.setText(String.valueOf(user.getLevel()));
		return view;
	}

}