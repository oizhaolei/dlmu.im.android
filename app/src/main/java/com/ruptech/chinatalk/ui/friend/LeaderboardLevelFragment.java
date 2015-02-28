package com.ruptech.chinatalk.ui.friend;

import static butterknife.ButterKnife.findById;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveLeaderboardUserTask;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.ui.user.ScrollTabHolderFragment;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendLeaderBoardListArrayAdapter;
import com.ruptech.chinatalk.widget.ImageProgressBar;

public class LeaderboardLevelFragment extends ScrollTabHolderFragment {

	public static Fragment newInstance() {
		LeaderboardLevelFragment fragment = new LeaderboardLevelFragment();
		return fragment;
	}

	private FriendLeaderBoardListArrayAdapter friendLeaderBoardListArrayAdapter;

	@InjectView(R.id.activity_friend_leaderboard_listView)
	ListView leaderVoardFriendList;

	View headerView;
	ImageProgressBar imageProgressBar1;
	ImageProgressBar imageProgressBar2;
	ImageProgressBar imageProgressBar3;
	ImageProgressBar imageProgressBar4;
	ImageProgressBar imageProgressBar5;

	ImageView userPicImageView1;
	ImageView userPicImageView2;
	ImageView userPicImageView3;
	ImageView userPicImageView4;
	ImageView userPicImageView5;

	TextView userFullnameTextView1;
	TextView userFullnameTextView2;
	TextView userFullnameTextView3;
	TextView userFullnameTextView4;
	TextView userFullnameTextView5;

	ImageView userGenderImageview1;
	ImageView userGenderImageview2;
	ImageView userGenderImageview3;
	ImageView userGenderImageview4;
	ImageView userGenderImageview5;

	ImageView userLangImageview1;
	ImageView userLangImageview2;
	ImageView userLangImageview3;
	ImageView userLangImageview4;
	ImageView userLangImageview5;

	TextView userLevelImageview1;
	TextView userLevelImageview2;
	TextView userLevelImageview3;
	TextView userLevelImageview4;
	TextView userLevelImageview5;

	private static GenericTask mRetrieveLeaderboardUserTask;

	private final TaskListener mRetrieveLeaderboardUserTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveLeaderboardUserTask popularUserTask = (RetrieveLeaderboardUserTask) task;
			if (result == TaskResult.OK) {
				List<User> popularUserList = popularUserTask
						.getPopularUserList();
				if (popularUserList.size() > 0) {
					displayHeaderView(popularUserList.get(0),
							userPicImageView1, imageProgressBar1,
							userFullnameTextView1, userGenderImageview1,
							userLangImageview1, userLevelImageview1);
					displayHeaderView(popularUserList.get(1),
							userPicImageView2, imageProgressBar2,
							userFullnameTextView2, userGenderImageview2,
							userLangImageview2, userLevelImageview2);
					displayHeaderView(popularUserList.get(2),
							userPicImageView3, imageProgressBar3,
							userFullnameTextView3, userGenderImageview3,
							userLangImageview3, userLevelImageview3);
					displayHeaderView(popularUserList.get(3),
							userPicImageView4, imageProgressBar4,
							userFullnameTextView4, userGenderImageview4,
							userLangImageview4, userLevelImageview4);
					displayHeaderView(popularUserList.get(4),
							userPicImageView5, imageProgressBar5,
							userFullnameTextView5, userGenderImageview5,
							userLangImageview5, userLevelImageview5);

					friendLeaderBoardListArrayAdapter.clear();

					for (int i = 5; i < popularUserList.size(); i++) {
						friendLeaderBoardListArrayAdapter.add(popularUserList
								.get(i));
					}
					friendLeaderBoardListArrayAdapter.notifyDataSetChanged();
					leaderVoardFriendList.setSelection(0);
				} else {
					goneHedderView();
				}
			} else {
				goneHedderView();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private void goneHedderView() {
		userPicImageView1.setVisibility(View.INVISIBLE);
		imageProgressBar1.setVisibility(View.GONE);
		userFullnameTextView1.setVisibility(View.INVISIBLE);
		userGenderImageview1.setVisibility(View.INVISIBLE);
		userLangImageview1.setVisibility(View.INVISIBLE);
		userLevelImageview1.setVisibility(View.INVISIBLE);

		userPicImageView2.setVisibility(View.INVISIBLE);
		imageProgressBar2.setVisibility(View.GONE);
		userFullnameTextView2.setVisibility(View.INVISIBLE);
		userGenderImageview2.setVisibility(View.INVISIBLE);
		userLangImageview2.setVisibility(View.INVISIBLE);
		userLevelImageview2.setVisibility(View.INVISIBLE);

		userPicImageView3.setVisibility(View.INVISIBLE);
		imageProgressBar3.setVisibility(View.GONE);
		userFullnameTextView3.setVisibility(View.INVISIBLE);
		userGenderImageview3.setVisibility(View.INVISIBLE);
		userLangImageview3.setVisibility(View.INVISIBLE);
		userLevelImageview3.setVisibility(View.INVISIBLE);

		userPicImageView4.setVisibility(View.INVISIBLE);
		imageProgressBar4.setVisibility(View.GONE);
		userFullnameTextView4.setVisibility(View.INVISIBLE);
		userGenderImageview4.setVisibility(View.INVISIBLE);
		userLangImageview4.setVisibility(View.INVISIBLE);
		userLevelImageview4.setVisibility(View.INVISIBLE);

		userPicImageView5.setVisibility(View.INVISIBLE);
		imageProgressBar5.setVisibility(View.GONE);
		userFullnameTextView5.setVisibility(View.INVISIBLE);
		userGenderImageview5.setVisibility(View.INVISIBLE);
		userLangImageview5.setVisibility(View.INVISIBLE);
		userLevelImageview5.setVisibility(View.INVISIBLE);
	}

	private void displayHeaderView(final User user, ImageView userPicImageView,
			ImageProgressBar imageProgressBar, TextView userFullnameTextView,
			ImageView userGenderImageview, ImageView userLangImageview,
			TextView userLevelImageview) {
		if (user != null) {
			String url = App.readServerAppInfo().getServerOriginal(
					user.getPic_url());
			if (!Utils.isEmpty(url)) {
				if (!url.equals(userPicImageView.getTag())) {
					ImageManager.imageLoader
							.displayImage(
									url,
									userPicImageView,
									ImageManager.getImageOptionsPortrait(),
									ImageViewActivity
											.createImageLoadingListener(imageProgressBar),
									ImageViewActivity
											.createLoadingProgresListener(imageProgressBar));
				}
				userPicImageView.setTag(url);
				userPicImageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						gotoProfileActivity(getActivity(), user);
					}
				});
			} else {
				imageProgressBar.setVisibility(View.GONE);
			}

			String fullName = Utils.getFriendName(user.getId(),
					user.getFullname());
			if (Utils.isEmpty(fullName)) {
				userFullnameTextView.setText("");
			} else {
				userFullnameTextView.setText(Utils.abbrString(fullName, 8));
			}
			if (user.getGender() < 1) {
				userGenderImageview.setVisibility(View.INVISIBLE);
			} else if (user.getGender() == AppPreferences.USERS_GENDER_MALE) {
				userGenderImageview.setImageResource(R.drawable.male);
			} else if (user.getGender() == AppPreferences.USERS_GENDER_FEMALE) {
				userGenderImageview.setImageResource(R.drawable.female);
			}

			userLangImageview
					.setImageResource(Utils.getLanguageFlag(user.lang));
			userLevelImageview.setText(String.valueOf(user.getLevel()));
		} else {
			imageProgressBar.setVisibility(View.GONE);
		}
	}

	public void doLeaderboardFriends() {
		mRetrieveLeaderboardUserTask = new RetrieveLeaderboardUserTask(
				getLeaderBoardType());
		mRetrieveLeaderboardUserTask
				.setListener(mRetrieveLeaderboardUserTaskListener);

		mRetrieveLeaderboardUserTask.execute();
	}

	protected String getLeaderBoardType() {
		return FriendsLeaderboardListActivity.LEADERBOARD_TYPE_LEVEL;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.sub_tab_leaderboard_list, container,
				false);
		ButterKnife.inject(this, v);

		return v;
	}

	private View setupComponentHeaderView() {
		View view = View.inflate(getActivity(),
				R.layout.item_leaderboard_header, null);
		imageProgressBar1 = findById(view, R.id.image_progress_bar1);
		imageProgressBar2 = findById(view, R.id.image_progress_bar2);
		imageProgressBar3 = findById(view, R.id.image_progress_bar3);
		imageProgressBar4 = findById(view, R.id.image_progress_bar4);
		imageProgressBar5 = findById(view, R.id.image_progress_bar5);

		userPicImageView1 = findById(view,
				R.id.item_leaderboard_header_pic_imageview1);
		userPicImageView2 = findById(view,
				R.id.item_leaderboard_header_pic_imageview2);
		userPicImageView3 = findById(view,
				R.id.item_leaderboard_header_pic_imageview3);
		userPicImageView4 = findById(view,
				R.id.item_leaderboard_header_pic_imageview4);
		userPicImageView5 = findById(view,
				R.id.item_leaderboard_header_pic_imageview5);

		userFullnameTextView1 = findById(view,
				R.id.item_leaderboard_header_user_nickname_textview1);
		userFullnameTextView2 = findById(view,
				R.id.item_leaderboard_header_user_nickname_textview2);
		userFullnameTextView3 = findById(view,
				R.id.item_leaderboard_header_user_nickname_textview3);
		userFullnameTextView4 = findById(view,
				R.id.item_leaderboard_header_user_nickname_textview4);
		userFullnameTextView5 = findById(view,
				R.id.item_leaderboard_header_user_nickname_textview5);

		userGenderImageview1 = findById(view,
				R.id.item_leaderboard_header_user_gender_imageview1);
		userGenderImageview2 = findById(view,
				R.id.item_leaderboard_header_user_gender_imageview2);
		userGenderImageview3 = findById(view,
				R.id.item_leaderboard_header_user_gender_imageview3);
		userGenderImageview4 = findById(view,
				R.id.item_leaderboard_header_user_gender_imageview4);
		userGenderImageview5 = findById(view,
				R.id.item_leaderboard_header_user_gender_imageview5);

		userLangImageview1 = findById(view,
				R.id.item_leaderboard_header_user_lang_imageview1);
		userLangImageview2 = findById(view,
				R.id.item_leaderboard_header_user_lang_imageview2);
		userLangImageview3 = findById(view,
				R.id.item_leaderboard_header_user_lang_imageview3);
		userLangImageview4 = findById(view,
				R.id.item_leaderboard_header_user_lang_imageview4);
		userLangImageview5 = findById(view,
				R.id.item_leaderboard_header_user_lang_imageview5);

		userLevelImageview1 = findById(view,
				R.id.item_leaderboard_header_user_level_textview1);
		userLevelImageview2 = findById(view,
				R.id.item_leaderboard_header_user_level_textview2);
		userLevelImageview3 = findById(view,
				R.id.item_leaderboard_header_user_level_textview3);
		userLevelImageview4 = findById(view,
				R.id.item_leaderboard_header_user_level_textview4);
		userLevelImageview5 = findById(view,
				R.id.item_leaderboard_header_user_level_textview5);
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		leaderVoardFriendList.addHeaderView(setupComponentHeaderView());
		friendLeaderBoardListArrayAdapter = new FriendLeaderBoardListArrayAdapter(
				getActivity());
		leaderVoardFriendList.setAdapter(friendLeaderBoardListArrayAdapter);
		leaderVoardFriendList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				User user = friendLeaderBoardListArrayAdapter
						.getItem(position - 1);
				gotoProfileActivity(getActivity(), user);
			}
		});

		doLeaderboardFriends();
	}

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	private void gotoProfileActivity(Activity context, User user) {
		Intent intent = new Intent(context, FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		context.startActivity(intent);
	}
}