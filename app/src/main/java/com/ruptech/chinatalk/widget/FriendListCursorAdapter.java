/**
 *
 */
package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class FriendListCursorAdapter extends CursorAdapter {
	static class ViewHolder {
		@InjectView(R.id.item_main_tab_friend_mask)
		View userThumbView;
		@InjectView(R.id.item_main_tab_friend_thumb_imageview)
		ImageView friendThumb;
		@InjectView(R.id.item_main_tab_friend_nickname_textview)
		TextView friendNickName;
		@InjectView(R.id.item_main_tab_friend_tel_textview)
		TextView friendTel;
		@InjectView(R.id.item_main_tab_friend_fullname_textview)
		TextView friendFullName;
		@InjectView(R.id.item_main_tab_friend_sms_imageview)
		ImageView smsImageView;
		@InjectView(R.id.item_main_tab_friend_lang_imageview)
		ImageView langImageView;
		@InjectView(R.id.item_main_tab_friend_user_layout)
		View rightView;
		@InjectView(R.id.item_main_tab_friend_user_one_row_layout)
		View rightOneRowView;
		@InjectView(R.id.item_main_tab_friend_tel_one_row_textview)
		TextView rightOneRowViewFriendTel;
		@InjectView(R.id.item_main_tab_friend_fullname_one_row_textview)
		TextView rightOneRowViewFriendFullName;
		@InjectView(R.id.item_main_tab_friend_one_row_nickname_textview)
		TextView rightOneRowViewfriendNickName;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	static final String TAG = Utils.CATEGORY
			+ FriendListCursorAdapter.class.getSimpleName();

	String color;

	private LayoutInflater mInflater;

	public FriendListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		final User user = UserTable.parseCursor(cursor);
		Friend friend = App.friendDAO.fetchFriend(App.readUser().getId(),
				user.getId());// 考虑到@列表 数据
		// friend
		String fullName = user.getFullname();
		String friendNickname = null;
		String friendMethod = null;
		if (friend != null) {
			friendNickname = friend.getFriend_nickname();
			friendMethod = friend.getFriend_method();
		}

		String mFullName = Utils.isEmpty(fullName) ? "" : Utils.abbrString(
				fullName, 8);
		String mFriendNickname = Utils.isEmpty(friendNickname) ? fullName
				: friendNickname;

		if (Utils.isEmpty(user.getTel())) {// public右侧全部隐藏
			holder.friendNickName.setVisibility(View.GONE);
			holder.rightView.setVisibility(View.GONE);

			holder.rightOneRowView.setVisibility(View.VISIBLE);
			holder.rightOneRowViewfriendNickName.setVisibility(View.VISIBLE);
			holder.rightOneRowViewfriendNickName.setText(mFriendNickname);
			if (fullName.equals(friendNickname)) {// 相同不显示fullname
				holder.rightOneRowViewFriendFullName.setVisibility(View.GONE);
			} else {
				holder.rightOneRowViewFriendFullName
						.setVisibility(View.VISIBLE);
				holder.rightOneRowViewFriendFullName.setText(mFullName);
			}
			holder.rightOneRowViewFriendTel.setVisibility(View.GONE);
		} else {
			if (fullName.equals(friendNickname)) {// 相同不显示fullname
				holder.friendNickName.setVisibility(View.GONE);
				holder.rightView.setVisibility(View.GONE);

				holder.rightOneRowView.setVisibility(View.VISIBLE);
				holder.rightOneRowViewfriendNickName
						.setVisibility(View.VISIBLE);
				holder.rightOneRowViewfriendNickName.setText(mFriendNickname);
				if (friendMethod
						.equals(AppPreferences.FRIEND_ADD_METHOD_BY_SERVICE)) {// public右侧全部隐藏
					holder.rightOneRowViewfriendNickName.setTextColor(Color
							.parseColor("#ff6600"));
				}
				holder.rightOneRowViewFriendFullName.setVisibility(View.GONE);
				holder.rightOneRowViewFriendTel.setVisibility(View.VISIBLE);
				holder.rightOneRowViewFriendTel.setText(Utils.abbrString(
						user.getTel(), 20));

			} else {// 不相同显示fullname
				holder.rightOneRowViewfriendNickName.setVisibility(View.GONE);
				holder.rightOneRowView.setVisibility(View.GONE);

				holder.friendNickName.setVisibility(View.VISIBLE);
				holder.rightView.setVisibility(View.VISIBLE);
				holder.friendNickName.setText(mFriendNickname);
				if (friendMethod
						.equals(AppPreferences.FRIEND_ADD_METHOD_BY_SERVICE)) {// public右侧全部隐藏
					holder.friendNickName.setTextColor(Color
							.parseColor("#ff6600"));
				}
				holder.friendFullName.setText(mFullName);
				holder.friendTel.setText(Utils.abbrString(user.getTel(), 20));
			}
		}

		Utils.setUserPicImage(holder.friendThumb, user.getPic_url());

		holder.userThumbView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FriendProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_USER, user);
				context.startActivity(intent);
			}
		});
		// sms user
		if (user.active == 1) {
			holder.smsImageView.setVisibility(View.GONE);
		} else {
			holder.smsImageView.setVisibility(View.VISIBLE);
		}

		holder.langImageView.setImageResource(Utils.getLanguageFlag(user.lang));

	}

	@Override
	public View newView(final Context context, final Cursor cursor,
			ViewGroup parent) {
		View view = mInflater.inflate(R.layout.item_main_tab_friend, parent,
				false);
		ViewHolder holder = new ViewHolder(view);
		view.setTag(holder);

		return view;
	}

}