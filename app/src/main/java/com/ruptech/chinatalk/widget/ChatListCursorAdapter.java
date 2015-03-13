/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

public class ChatListCursorAdapter extends CursorAdapter {
	static class ViewHolder {
		@InjectView(R.id.item_main_tab_chat_friend_mask)
		View userThumbView;
		@InjectView(R.id.item_main_tab_chat_friend_thumb_imageview)
		ImageView friendThumb;
		@InjectView(R.id.item_main_tab_chat_friend_nickname_textview)
		TextView friendNickName;
		@InjectView(R.id.item_main_tab_chat_message_content_textview)
		TextView messageContent;
		@InjectView(R.id.item_main_tab_chat_message_createddate_textview)
		TextView messageCreate;
		@InjectView(R.id.item_main_tab_chat_friend_sms_imageview)
		ImageView smsImageView;
		@InjectView(R.id.item_main_tab_chat_friend_lang_imageview)
		ImageView langImageView;
		@InjectView(R.id.item_main_tab_chat_message_count_textview)
		TextView mCountNewMessageTextView;
		@InjectView(R.id.item_main_tab_chat_friend_towrow_layout)
		View rightTowrowView;
		@InjectView(R.id.item_main_tab_chat_friend_onerow_layout)
		View rightOnerowView;
		@InjectView(R.id.item_main_tab_chat_friend_nickname_onerow_textview)
		TextView nickNameOnerowTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	static final String TAG = Utils.CATEGORY
			+ ChatListCursorAdapter.class.getSimpleName();
	private LayoutInflater mInflater;

	public ChatListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final User friendUser = UserTable.parseCursor(cursor);

		Friend friend = App.friendDAO.fetchFriend(App.readUser().getId(),
				friendUser.getId());
		// newest message to display
		Message message = App.messageDAO.fetchNewestMessageByUser(App
				.readUser().getId(), friendUser.getId());
		if (message != null) {
			holder.rightTowrowView.setVisibility(View.VISIBLE);
			holder.rightOnerowView.setVisibility(View.GONE);

			if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
					.equals(message.file_type)) {
				holder.messageContent.setText(R.string.picture);
			} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
					.equals(message.file_type)) {
				holder.messageContent.setText(R.string.voice);
			} else if (message.userid == App.readUser().getId()
					|| Utils.isEmpty(message.getTo_content())) {
				holder.messageContent.setText(message.getFrom_content());
			} else {
				holder.messageContent.setText(message.getTo_content());
			}
			String messageCreateDate = message.getCreate_date();
			holder.messageCreate.setText(DateCommonUtils
					.formatConvUtcDateString(messageCreateDate, false, false));

			holder.messageContent.setVisibility(View.VISIBLE);
			holder.messageCreate.setVisibility(View.VISIBLE);
			view.setBackgroundColor(App.mContext.getResources().getColor(
					R.color.chat_friend_message_background));

			// count
			int count = App.mBadgeCount.getNewMessageCount(friendUser.getId());

			if (count > 0) {
				holder.mCountNewMessageTextView.setVisibility(View.VISIBLE);
				holder.mCountNewMessageTextView.setText(String.valueOf(count));
			} else {
				holder.mCountNewMessageTextView.setVisibility(View.GONE);
			}
		} else {
			holder.rightOnerowView.setVisibility(View.VISIBLE);
			holder.rightTowrowView.setVisibility(View.GONE);

			holder.messageContent.setVisibility(View.GONE);
			holder.messageCreate.setVisibility(View.GONE);
			view.setBackgroundColor(App.mContext.getResources().getColor(
					R.color.content_background));
		}

		if (friend != null
				&& !Utils.isEmpty(friend.getFriend_nickname())
				&& !friend.getFriend_nickname()
						.equals(friendUser.getFullname())) {
			String name = friend.getFriend_nickname() + "   "
					+ friendUser.getFullname();
			holder.nickNameOnerowTextView.setText(name);
			holder.friendNickName.setText(name);
		} else {
			holder.nickNameOnerowTextView.setText(friendUser.getFullname());
			holder.friendNickName.setText(friendUser.getFullname());
		}

		if (friend == null || friend.getDone() == 0) {
			int colorId = App.mContext.getResources().getColor(
					R.color.text_gray);
			holder.nickNameOnerowTextView.setTextColor(colorId);
			holder.friendNickName.setTextColor(colorId);
		} else {
			int colorid = App.mContext.getResources().getColor(
					R.color.text_black);
			holder.nickNameOnerowTextView.setTextColor(colorid);
			holder.friendNickName.setTextColor(colorid);
		}

		Utils.setUserPicImage(holder.friendThumb, friendUser.getPic_url());

		holder.userThumbView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, FriendProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_USER, friendUser);
				context.startActivity(intent);
			}
		});

		// sms user
		if (friendUser.active == 1) {
			holder.smsImageView.setVisibility(View.GONE);
		} else {
			holder.smsImageView.setVisibility(View.VISIBLE);
		}

		holder.langImageView.setImageResource(Utils
				.getLanguageFlag(friendUser.lang));

		Friend to_friend = App.friendDAO.fetchFriend(friendUser.getId(), App
				.readUser().getId());
		if ((friend != null && friend.getIs_top() == 1)
				|| (to_friend != null && to_friend.getIs_top() == 1)) {
			view.setBackgroundColor(App.mContext.getResources().getColor(
					R.color.orange_softlight));
		}

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(R.layout.item_main_tab_chat, parent,
				false);

		ViewHolder holder = new ViewHolder(view);
		view.setTag(holder);

		return view;
	}
}