package com.ruptech.chinatalk.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.FriendOperate;
import com.ruptech.chinatalk.ui.FriendOperate.UserType;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendProfileActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ FriendProfileActivity.class.getSimpleName();
	public static final String EXTRA_PAGE_ITEM = "EXTRA_PAGE_ITEM";

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private ProgressDialog progressDialog;


	RetrieveUserTask mRetrieveUserByTelTask;

	@InjectView(R.id.activity_friend_profile_user_background_layout)
	ImageView mBackgroundImageview;
	@InjectView(R.id.activity_friend_profile_user_thumb_imageview)
	ImageView mThumbImageview;
	@InjectView(R.id.activity_friend_profile_user_nickname_textview)
	TextView mNicknameTextView;
	@InjectView(R.id.activity_friend_profile_user_memo_textview)
	TextView mMemoTextView;
	@InjectView(R.id.activity_friend_profile_user_gender_imageview)
	ImageView mGenderImageView;

	@InjectView(R.id.activity_friend_profile_user_follow_textview)
	TextView mFollowTextView;
	@InjectView(R.id.activity_friend_profile_user_fans_textview)
	TextView mFansTextView;


	@InjectView(R.id.activity_friend_profile_follow_view)
	View mFollowerView;
	@InjectView(R.id.activity_friend_profile_follow_textview)
	TextView mFollowerTextView;
	@InjectView(R.id.activity_friend_profile_follow_image)
	ImageView mFollowerImageView;

	@InjectView(R.id.activity_friend_profile_message_view)
	View mMessageView;
	@InjectView(R.id.activity_friend_profile_message_textview)
	TextView mMessageTextView;


	private FriendOperate friendOperate;

	private UserType mUserType;

	private User mUser;

	private long mUserId;

	private Drawable mActionBarBackgroundDrawable;

	protected boolean notMoreDataFound = false;
	protected boolean isLocalDataDisplay = true;

	private static FriendProfileActivity instance = null;

	private final TextPaint tpNickName = new TextPaint();

	private final TextPaint tpMemo = new TextPaint();

	private boolean isCreateOptionsMenu = false;

	private int mMinHeaderHeight;

	private int mActionBarHeight;

	private final TypedValue mTypedValue = new TypedValue();

	MenuItem menuItemChangeNickname;

	MenuItem menuItemChangeMemo;

	MenuItem menuItemChangeProfile;

	MenuItem menuItemFriendBlock;

	MenuItem menuItemFriendReport;



	private final TaskListener mRetrieveUserListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserTask retrieveUserTask = (RetrieveUserTask) task;
			if (result == TaskResult.OK) {
				User user = retrieveUserTask.getUser();
				if (user != null) {
					mUser = user;
				}
				onRetrieveUserTaskSuccess();
			} else {
				String msg = retrieveUserTask.getMsg();
				onRetrieveUserTaskFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private final TaskListener mFriendAddListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				addFriendSuccess();
			} else {
				String msg = task.getMsg();
				addFriendFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			addFriendBegin();
		}
	};

	private int pagerCurrent = 0;

	private void addFriendBegin() {
		mFollowerView.setEnabled(false);
		startProgress();
	}

	private void addFriendFailure(String msg) {
		stopProgress();
		mFollowerView.setEnabled(true);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void addFriendSuccess() {
		stopProgress();
		mFollowerView.setEnabled(true);
		mUserType = UserType.FRIEND;
		showMenuItem();
		showMenuButton();

		PrefUtils.removePreRecommendedFriendById(mUserId);
		Toast.makeText(this, R.string.friend_add_ok, Toast.LENGTH_SHORT).show();
	}



	private void displayUser() {
		try {
			if (mUser != null && App.readUser() != null) {


				Friend friend = App.friendDAO.fetchFriend(App.readUser()
						.getId(), mUserId);

				if (friend != null && !Utils.isEmpty(friend.getFriend_memo())) {
					final String friendMemo = friend.getFriend_memo();
					mMemoTextView.post(new Runnable() {
						@Override
						public void run() {
							float maxWidth = mBackgroundImageview
									.getMeasuredWidth() * 0.5f;
							String name = (String) TextUtils.ellipsize(
									friendMemo, tpMemo, maxWidth,
									TextUtils.TruncateAt.END);
							mMemoTextView.setText(name);
						}

					});
					mMemoTextView.setVisibility(View.VISIBLE);
				} else {
					mMemoTextView.setVisibility(View.GONE);
				}

				final String friendName = friend == null
						|| Utils.isEmpty(friend.getFriend_nickname()) ? mUser
						.getFullname() : friend.getFriend_nickname();

				if (mUser.getId() == App.readUser().getId()) {
					mUserType = UserType.MYSELF;
				} else {
					if (friend == null || friend.getDone() != 1) {
						mUserType = UserType.STRANGER;
					} else {
						mUserType = UserType.FRIEND;
					}
				}
				showMenuItem();
				showMenuButton();

				mNicknameTextView.post(new Runnable() {
					@Override
					public void run() {
						float maxWidth = mBackgroundImageview
								.getMeasuredWidth() * 0.5f;
						String name = (String) TextUtils.ellipsize(friendName
										+ "(" + mUser.getFullname() + ")", tpNickName,
								maxWidth, TextUtils.TruncateAt.END);
						if (friendName.equals(mUser.getFullname())) {
							name = (String) TextUtils.ellipsize(friendName,
									tpNickName, maxWidth,
									TextUtils.TruncateAt.END);
						}
						mNicknameTextView.setText(name);
					}

				});



				friendOperate = new FriendOperate(this, mUser, mUserType);
				mGenderImageView
						.setBackgroundResource(R.drawable.background_round_button_gender);
			}
		} catch (Exception e) {

			Utils.sendClientException(e);
		}
	}



	public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}

		getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

		mActionBarHeight = TypedValue.complexToDimensionPixelSize(
				mTypedValue.data, getResources().getDisplayMetrics());

		return mActionBarHeight;
	}


	private User getUserFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ProfileActivity.EXTRA_USER);
			pagerCurrent = extras.getInt(FriendProfileActivity.EXTRA_PAGE_ITEM);
			return user;
		}
		return null;
	}

	private long getUserIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(ProfileActivity.EXTRA_USER_ID);
			return userId;
		}
		return 0;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.activity_friend_profile);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		instance = this;

		mUser = getUserFromExtras();
		if (mUser == null) {
			mUserId = getUserIdFromExtras();
		} else {
			mUserId = mUser.getId();
		}

		setupComponents(savedInstanceState);
		if (mUser != null) {
			displayUser();
		}
		if (mUser == null && mUserId <= 0) {
			Toast.makeText(this, R.string.user_infomation_is_invalidate,
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.friend_profile_actions, mMenu);
		menuItemChangeProfile = mMenu.findItem(R.id.change_profile);
		menuItemChangeNickname = mMenu.findItem(R.id.change_nickname);
		menuItemChangeMemo = mMenu.findItem(R.id.change_friend_memo);
		menuItemFriendBlock = mMenu.findItem(R.id.friend_menu_block);
		menuItemFriendReport = mMenu.findItem(R.id.friend_menu_report);
		isCreateOptionsMenu = true;
		showMenuItem();

		return true;
	}

	@Override
	protected void onDestroy() {
		instance = null;
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}


	@Override
	public void onResume() {
		super.onResume();
		retrieveUser();
	}

	private void onRetrieveUserTaskFailure(String msg) {
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRetrieveUserTaskSuccess() {
		displayUser();
	}


	private void retrieveUser() {
		if (mRetrieveUserByTelTask != null
				&& mRetrieveUserByTelTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveUserByTelTask = new RetrieveUserTask(mUserId);
		mRetrieveUserByTelTask.setListener(mRetrieveUserListener);

		mRetrieveUserByTelTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}


	public void sendMessageButton(View v) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_JID, mUser.getOF_JabberID());
		this.startActivity(intent);
	}


	public void setting_changeProfile(MenuItem item) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
		this.startActivity(intent);
	}

	public void setting_clean_message(MenuItem item) {
		if (friendOperate != null) {
			friendOperate.settingCleanMessage();
		}
	}


	private void setupComponents(Bundle savedInstanceState) {
		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.min_header_height)
				- getActionBarHeight();

		mActionBarBackgroundDrawable = getResources().getDrawable(
				R.color.action_bar_background);
		mActionBarBackgroundDrawable = mActionBarBackgroundDrawable
				.getConstantState().newDrawable();

		tpNickName.setTextSize(mNicknameTextView.getTextSize());
		tpNickName.setTypeface(mNicknameTextView.getTypeface());
		tpMemo.setTextSize(mMemoTextView.getTextSize());
		tpMemo.setTypeface(mMemoTextView.getTypeface());


	}

	private void showMenuButton() {
		if (mUserType == UserType.MYSELF) {
		} else if (mUserType == UserType.FRIEND) {
			mFollowerTextView.setText(R.string.friend_menu_delete);
			mFollowerImageView
					.setBackgroundResource(R.drawable.quxiao_guanzhu_icon);
			mMessageView.setVisibility(View.VISIBLE);
		} else {
			mMessageView.setVisibility(View.GONE);
		}
	}

	private void showMenuItem() {
		if (!isCreateOptionsMenu) {
			return;
		}
		if (mUserType == UserType.MYSELF) {
			menuItemChangeProfile.setVisible(true);
			menuItemChangeNickname.setVisible(false);
			menuItemChangeMemo.setVisible(false);
			menuItemFriendBlock.setVisible(false);
			menuItemFriendReport.setVisible(false);
		} else if (mUserType == UserType.FRIEND) {
			menuItemChangeProfile.setVisible(false);
			menuItemChangeNickname.setVisible(true);
			menuItemChangeMemo.setVisible(true);
			menuItemFriendBlock.setVisible(true);
			menuItemFriendReport.setVisible(true);
		} else {
			menuItemChangeProfile.setVisible(false);
			menuItemChangeNickname.setVisible(false);
			menuItemChangeMemo.setVisible(false);
			menuItemFriendBlock.setVisible(false);
			menuItemFriendReport.setVisible(true);
		}
	}

	private void startProgress() {
		progressDialog = Utils.showDialog(this,
				getString(R.string.please_waiting));
	}

	private void stopProgress() {
		Utils.dismissDialog(progressDialog);
	}
}
