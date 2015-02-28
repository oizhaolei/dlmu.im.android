/**
 *
 */
package com.ruptech.chinatalk.widget;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FriendAddTask;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.processbutton.ActionProcessButton;

public class FriendListArrayBaseAdapter extends ArrayAdapter<User> {
	static class ViewHolder {
		@InjectView(R.id.item_main_tab_friend_request_thumb_imageview)
		ImageView friendThumb;
		@InjectView(R.id.item_main_tab_friend_request_sms_imageview)
		ImageView smsImageView;
		@InjectView(R.id.item_main_tab_friend_request_lang_imageview)
		ImageView langImageView;
		@InjectView(R.id.item_main_tab_friend_content_one_gender_imageview)
		ImageView genderImageView;
		@InjectView(R.id.item_main_tab_friend_content_one_level_textview)
		TextView levelImageView;
		@InjectView(R.id.item_main_tab_friend_content_one_title_textview)
		TextView firstTextView;
		@InjectView(R.id.item_main_tab_friend_content_two_title_textview)
		TextView secondTextView;
		@InjectView(R.id.item_main_tab_friend_content_three_title_textview)
		TextView thirdTextView;
		@InjectView(R.id.item_main_tab_add_new_friend_btn)
		ActionProcessButton addFriendButton;
		@InjectView(R.id.item_main_tab_friend_content_one_layout)
		View oneRowView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_main_tab_friend_request; // xml布局文件

	private final LayoutInflater mInflater;
	private final ArrayList<User> inProcessUserList = new ArrayList<User>();
	private final ArrayList<ActionProcessButton> btnList = new ArrayList<ActionProcessButton>();
	private final Context mContext;
	public static final int EXTRA_FRIEND_POPULAR_LIST = 1;
	public static final int EXTRA_FRIEND_ONLINE_LIST = 2;
	public static final int EXTRA_FRIEND_LBS_LIST = 3;
	public static final int EXTRA_FRIEND_ADD_RECOMMENDED = 4;
	public static final int EXTRA_FRIEND_FOLLOWER_LIST = 5;
	public static final int EXTRA_FRIEND_BLOCKED_LIST = 6;
	public static final int EXTRA_FRIEND_LIKE_STORY_LIST = 7;
	private final TextPaint tpFullname = new TextPaint();

	private int mFlag = 0;

	private static GenericTask mFriendAddTask;

	public static void gotoFollowUser(final User user, Context context,
			final TaskListener friendAddListener) {
		mFriendAddTask = new FriendAddTask(user.getTel(), String.valueOf(user
				.getId()), "", "", "", false);
		mFriendAddTask.setListener(friendAddListener);
		mFriendAddTask.execute();
	}

	public FriendListArrayBaseAdapter(Context context, int flag,
			ProgressDialog progressDialog) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFlag = flag;
	}

	private String getFullname(User friendUser, Friend userFriendInfo) {
		String fullName = Utils.isEmpty(friendUser.getFullname()) ? ""
				: friendUser.getFullname();
		if (userFriendInfo != null
				&& !Utils.isEmpty(userFriendInfo.getFriend_nickname())) {
			fullName = userFriendInfo.getFriend_nickname();
		}
		return fullName;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View view;
		final ViewHolder holder;

		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);
			holder = new ViewHolder(view);
			btnList.add(holder.addFriendButton);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();

		}

		tpFullname.setTextSize(holder.firstTextView.getTextSize());
		tpFullname.setTypeface(holder.firstTextView.getTypeface());

		final User friendUser = getItem(position);
		Friend userFriendInfo = App.friendDAO.fetchFriend(App.readUser()
				.getId(), friendUser.getId());

		final String fullName = getFullname(friendUser, userFriendInfo);

		OnClickListener onFollowUserListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoFollowUser(friendUser, mContext,
						new TaskAdapter() {
							private long friendId;

							@Override
							public void onPostExecute(GenericTask task,
									TaskResult result) {

								FriendAddTask friendAddTask = (FriendAddTask) task;
								if (result == TaskResult.OK) {

									for (int i = 0; i < btnList.size(); i++) {
										ActionProcessButton temp = btnList
												.get(i);
										long btnFriendId = (Long) temp.getTag();
										if (btnFriendId == friendId) {
											if (mFlag == EXTRA_FRIEND_BLOCKED_LIST) {
												temp.setText(R.string.already_cancel_block);
											} else {
												temp.setText(R.string.add_ok);
											}
											temp.setEnabled(false);
											temp.setProgress(100);
										}
									}
								} else {
									String msg = friendAddTask.getMsg();
									Toast.makeText(mContext, msg,
											Toast.LENGTH_SHORT).show();
									for (int i = 0; i < btnList.size(); i++) {
										ActionProcessButton temp = btnList
												.get(i);
										long btnFriendId = (Long) temp.getTag();
										if (btnFriendId == friendId) {
											temp.setEnabled(true);
											temp.setProgress(0);
										}
									}
								}
								inProcessUserList.remove(friendUser);

							}

							@Override
							public void onPreExecute(GenericTask task) {
								inProcessUserList.add(friendUser);
								friendId = friendUser.getId();
								holder.addFriendButton.setTag(Long
										.valueOf(friendUser.getId()));
								holder.addFriendButton.setEnabled(false);
								holder.addFriendButton.setProgress(50);

							}
						});
			}
		};

		holder.addFriendButton.setOnClickListener(onFollowUserListener);
		holder.addFriendButton.setMode(ActionProcessButton.Mode.ENDLESS);

		if (userFriendInfo == null || userFriendInfo.getDone() != 1) {
			if (inProcessUserList.contains(friendUser)) {
				holder.addFriendButton.setProgress(50);
				holder.addFriendButton.setEnabled(false);
			} else {
				holder.addFriendButton.setProgress(0);
				holder.addFriendButton.setEnabled(true);
			}

			holder.addFriendButton.setText(R.string.add_as_friend);

		} else {
			holder.addFriendButton.setProgress(100);
			holder.addFriendButton.setText(R.string.add_ok);
			holder.addFriendButton.setEnabled(false);
		}

		if (mFlag == EXTRA_FRIEND_BLOCKED_LIST
				&& (userFriendInfo == null || userFriendInfo.getDone() == -1)) {
			holder.addFriendButton.setVisibility(View.INVISIBLE);
		}

		holder.addFriendButton.setTag(Long.valueOf(friendUser.getId()));

		Utils.setUserPicImage(holder.friendThumb, friendUser.getPic_url());

		// sms user
		if (friendUser.active == 1) {
			holder.smsImageView.setVisibility(View.GONE);
		} else {
			holder.smsImageView.setVisibility(View.VISIBLE);
		}

		holder.langImageView.setImageResource(Utils
				.getLanguageFlag(friendUser.lang));

		holder.genderImageView.setVisibility(View.VISIBLE);
		if (friendUser.getGender() == 1) {
			holder.genderImageView.setImageResource(R.drawable.male);
		} else if (friendUser.getGender() == 2) {
			holder.genderImageView.setImageResource(R.drawable.female);
		} else {
			holder.genderImageView.setVisibility(View.GONE);
		}

		if (friendUser.getLevel() > 0) {
			holder.levelImageView
					.setText(String.valueOf(friendUser.getLevel()));
		} else {
			holder.levelImageView.setVisibility(View.GONE);
		}

		holder.firstTextView.post(new Runnable() {
			@Override
			public void run() {
				float maxWidth = holder.oneRowView.getMeasuredWidth() * 0.8f;
				String name = (String) TextUtils.ellipsize(fullName,
						tpFullname, maxWidth, TextUtils.TruncateAt.END);
				holder.firstTextView.setText(name);
			}

		});

		if (mFlag == EXTRA_FRIEND_POPULAR_LIST
				|| mFlag == EXTRA_FRIEND_ONLINE_LIST) {// popular、online
			if (!Utils.isEmpty(friendUser.getUser_memo())) {
				holder.secondTextView.setVisibility(View.VISIBLE);
				holder.secondTextView.setText(friendUser.getUser_memo());
			} else {
				holder.secondTextView.setVisibility(View.GONE);
			}
			holder.thirdTextView.setVisibility(View.GONE);
		} else if (mFlag == EXTRA_FRIEND_LBS_LIST) {// lbs
			holder.firstTextView.setText(fullName);
			if (!Utils.isEmpty(friendUser.getUser_memo())) {
				holder.secondTextView.setVisibility(View.VISIBLE);
				holder.secondTextView.setText(friendUser.getUser_memo());
			} else {
				holder.secondTextView.setVisibility(View.GONE);
			}
			if (Utils.isValidLocation6(friendUser.getLnge6(),
					friendUser.getLate6())
					&& MyLocation.recentLocation != null) {
				holder.thirdTextView.setVisibility(View.VISIBLE);
				holder.thirdTextView.setText(Utils.FomartDistance(Utils
						.GetDistance(friendUser.getLnge6() / 1E6,
								friendUser.getLate6() / 1E6,
								MyLocation.recentLocation.getLongitude(),
								MyLocation.recentLocation.getLatitude())));
			} else {
				holder.thirdTextView.setVisibility(View.GONE);
			}

		} else if (mFlag == EXTRA_FRIEND_ADD_RECOMMENDED) {// ADD_RECOMMENDED
			holder.secondTextView.setText(friendUser.getTel());
			holder.thirdTextView.setVisibility(View.GONE);
		} else if (mFlag == EXTRA_FRIEND_FOLLOWER_LIST) {// follower
			String createDate = DateCommonUtils.dateFormat(
					friendUser.getCreate_date(),
					DateCommonUtils.DF_yyyyMMddHHmmssSSS);
			String formateCreateDate = DateCommonUtils.formatConvUtcDateString(
					createDate, false, true);
			holder.secondTextView.setText(formateCreateDate);
			holder.thirdTextView.setVisibility(View.GONE);
		} else if (mFlag == EXTRA_FRIEND_LIKE_STORY_LIST) {// follower
			String likeDate = DateCommonUtils.formatConvUtcDateString(
					friendUser.getLike_date(), true, false);
			holder.secondTextView.setText(likeDate);
			holder.thirdTextView.setVisibility(View.GONE);
		} else if (mFlag == EXTRA_FRIEND_BLOCKED_LIST) {// online
			holder.secondTextView.setVisibility(View.GONE);
			holder.thirdTextView.setVisibility(View.GONE);
		}
		return view;
	}

}