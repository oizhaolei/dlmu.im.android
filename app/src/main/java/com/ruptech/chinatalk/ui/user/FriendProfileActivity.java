package com.ruptech.chinatalk.ui.user;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.FriendOperate;
import com.ruptech.chinatalk.ui.FriendOperate.UserType;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.dialog.ChangeFriendMemoActivity;
import com.ruptech.chinatalk.ui.dialog.ChangeNickNameActivity;
import com.ruptech.chinatalk.ui.dialog.ChangePhotoActivity;
import com.ruptech.chinatalk.ui.fragment.SlidingTabLayout;
import com.ruptech.chinatalk.ui.gift.GiftDonateActivity;
import com.ruptech.chinatalk.ui.gift.GiftListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.TapPagerAdapter;
import com.ruptech.chinatalk.widget.UserPhotoListArrayAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendProfileActivity extends ActionBarActivity implements
		ScrollTabHolder, ViewPager.OnPageChangeListener {

	static final String TAG = Utils.CATEGORY
			+ FriendProfileActivity.class.getSimpleName();
	public static final String EXTRA_PAGE_ITEM = "EXTRA_PAGE_ITEM";

	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private ProgressDialog progressDialog;

	TapPagerAdapter mPagerAdapter;

	RetrieveUserTask mRetrieveUserByTelTask;

	@InjectView(R.id.activity_friend_profile_user_background_layout)
	ImageView mBackgroundImageview;
	@InjectView(R.id.activity_friend_profile_user_thumb_imageview)
	ImageView mThumbImageview;
	@InjectView(R.id.activity_friend_profile_user_sms_imageview)
	ImageView mSmsImageview;
	@InjectView(R.id.activity_friend_profile_user_nickname_textview)
	TextView mNicknameTextView;
	@InjectView(R.id.activity_friend_profile_user_memo_textview)
	TextView mMemoTextView;
	@InjectView(R.id.activity_friend_profile_user_lang_imageview)
	ImageView mLanguageImageView;
	@InjectView(R.id.activity_friend_profile_user_gender_imageview)
	ImageView mGenderImageView;
	@InjectView(R.id.activity_friend_profile_user_level_imageview)
	TextView mLevelImageView;

	@InjectView(R.id.activity_friend_profile_user_follow_textview)
	TextView mFollowTextView;
	@InjectView(R.id.activity_friend_profile_user_fans_textview)
	TextView mFansTextView;
	@InjectView(R.id.activity_friend_profile_user_charm_textview)
	TextView mCharmTextView;

	@InjectView(R.id.pager)
	ViewPager mViewPager;

	@InjectView(R.id.item_user_story_follow_view)
	View mFollowerView;
	@InjectView(R.id.item_user_story_follow_textview)
	TextView mFollowerTextView;
	@InjectView(R.id.item_user_story_follow_image)
	ImageView mFollowerImageView;

	@InjectView(R.id.item_user_story_message_view)
	View mMessageView;
	@InjectView(R.id.item_user_story_message_textview)
	TextView mMessageTextView;
	@InjectView(R.id.item_user_story_gift_view)
	View mGiftView;
	@InjectView(R.id.item_user_story_gift_textview)
	TextView mGiftTextView;

	@InjectView(R.id.sliding_tabs)
	SlidingTabLayout mTabLayout;

	@InjectView(R.id.header)
	View headerView;
	@InjectView(R.id.footer)
	View footerView;

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

	MenuItem menuItemChatHistory;

	MenuItem menuItemCleanChatHistory;

	protected GenericTask mRetrieveUserPhotoListTask;

	protected UserPhotoListArrayAdapter mUserStoryListArrayAdapter;

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

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	public void changeFriendMemo() {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangeFriendMemoActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
		startActivityForResult(intent,
				ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_FRIEND);
	}

	public void changeUserBackground(View v) {
		if (mUser != null && mUser.getId() == App.readUser().getId()) {
			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {

					Intent intent = new Intent(FriendProfileActivity.this,
							ChangePhotoActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
					intent.putExtra(ChangePhotoActivity.EXTRA_CHANGE_TASK,
							"changeUserBackground");
					startActivityForResult(intent,
							ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_USER);
				}
			};
			DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			};

			Utils.AlertDialog(this, positiveListener, negativeListener,
					this.getString(R.string.tips),
					this.getString(R.string.change_profile_bg_tips));
		}
	}

	public void changeUserNickName() {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangeNickNameActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
		startActivityForResult(intent,
				ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_FRIEND);
	}

	private void displayUser() {
		try {
			if (mUser != null && App.readUser() != null) {
				String bgThumb = mUser.getBackground_url();
				if (!Utils.isEmpty(bgThumb)) {
					if (!bgThumb.equals(mBackgroundImageview.getTag())) {
						ImageManager.imageLoader.displayImage(
								App.readServerAppInfo().getServerOriginal(
										bgThumb), mBackgroundImageview,
								ImageManager.getProfileOptionsPortrait());
					}
					mBackgroundImageview.setTag(bgThumb);
				} else {
					mBackgroundImageview
							.setImageResource(R.drawable.profile_default_bg);
					mBackgroundImageview.setTag(null);
				}
				String thumb = mUser.getPic_url();
				Utils.setUserPicImage(mThumbImageview, thumb);

				// sms user
				if (mUser.active == 1) {
					mSmsImageview.setVisibility(View.GONE);
				} else {
					mSmsImageview.setVisibility(View.VISIBLE);
				}

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

				mLanguageImageView.setImageResource(Utils
						.getLanguageFlag(mUser.lang));

				mGenderImageView.setVisibility(View.VISIBLE);
				if (mUser.getGender() < 1) {
					mGenderImageView.setVisibility(View.INVISIBLE);
				} else if (mUser.getGender() == AppPreferences.USERS_GENDER_MALE) {
					mGenderImageView.setImageResource(R.drawable.male);
				} else if (mUser.getGender() == AppPreferences.USERS_GENDER_FEMALE) {
					mGenderImageView.setImageResource(R.drawable.female);
				}

				mFollowTextView
						.setText(String.valueOf(mUser.getFollow_count()));
				mFansTextView
						.setText(String.valueOf(mUser.getFollower_count()));
				mCharmTextView.setText(String.valueOf(mUser.getCharm_point()));
				friendOperate = new FriendOperate(this, mUser, mUserType);
				mGenderImageView
						.setBackgroundResource(R.drawable.background_round_button_gender);
				mLevelImageView.setText(String.valueOf(mUser.getLevel()));
			}
		} catch (Exception e) {

			Utils.sendClientException(e);
		}
	}

	public void displayUserOriginal(View v) {
		if (!Utils.isEmpty(mUser.getPic_url())) {
			UserPhoto userPhoto = new UserPhoto();
			userPhoto.setPic_url(mUser.getPic_url());
			ArrayList<String> extraPhotos = new ArrayList<String>();
			extraPhotos.add(App.readServerAppInfo().getServerOriginal(
					mUser.getPic_url()));
			Intent intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra(ImageViewActivity.EXTRA_POSITION, 0);
			intent.putExtra(ImageViewActivity.EXTRA_IMAGE_URLS, extraPhotos);
			startActivityForResult(intent,
					ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_USER);
		}
	}

	public void friendAddOrRemoveButton(View v) {
		if (friendOperate != null) {
			if (mUserType == UserType.FRIEND) {
				friendOperate.settingRemoveFriend();
			} else {
				friendOperate.settingFriendAdd(mFriendAddListener);
			}
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

	public int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop() + 3;

		int headerHeight = 0;
		if (firstVisiblePosition >= 1) {
			headerHeight = headerView.getHeight();
		}

		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_USER) {// modify
				if (null != data.getExtras()) {
					Bundle extras = data.getExtras();
					mUser = (User) extras.get(ProfileActivity.EXTRA_USER);
					if (!Utils.isEmpty(mUser.getBackground_url())) {
						String userThumbUrl = mUser.getBackground_url();
						if (!userThumbUrl.equals(mBackgroundImageview.getTag())) {
							ImageManager.imageLoader
									.displayImage(App.readServerAppInfo()
											.getServerOriginal(userThumbUrl),
											mBackgroundImageview);
						}
						mBackgroundImageview.setTag(userThumbUrl);
					} else {
						mBackgroundImageview.setImageBitmap(null);
						mBackgroundImageview.setTag(null);
					}

					Utils.setUserPicImage(mThumbImageview, mUser.getPic_url());
				}
			} else if (requestCode == ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_FRIEND) {// modify
				if (null != data.getExtras()) {
					Bundle extras = data.getExtras();
					Friend friend = (Friend) extras
							.get(ProfileActivity.EXTRA_FRIEND);
					if (Utils.isEmpty(friend.getFriend_nickname().trim())) {
						mNicknameTextView.setText("");
					} else {
						if (friend.getFriend_nickname().equals(
								mUser.getFullname())) {
							mNicknameTextView.setText(friend
									.getFriend_nickname());
						} else {
							mNicknameTextView.setText(friend
									.getFriend_nickname()
									+ "("
									+ mUser.getFullname() + ")");
						}
					}

					if (Utils.isEmpty(friend.getFriend_memo().trim())) {
						mMemoTextView.setText("");
						mMemoTextView.setVisibility(View.GONE);
					} else {
						mMemoTextView.setText(friend.getFriend_memo());
						mMemoTextView.setVisibility(View.VISIBLE);
					}
				}
			}
		}
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
		menuItemChatHistory = mMenu
				.findItem(R.id.friend_menu_more_chat_history);
		menuItemCleanChatHistory = mMenu
				.findItem(R.id.friend_menu_clean_chat_history);
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
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
		if (currentHolder != null && headerView != null) {
			currentHolder
					.adjustScroll((int) (headerView.getHeight() + headerView
							.getTranslationY()));
		}
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

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {

		if (mViewPager.getCurrentItem() == pagePosition) {

			int scrollY = getScrollY(view);
			headerView.setTranslationY(Math.max(-scrollY, -mMinHeaderHeight));

			float ratio = clamp(-headerView.getTranslationY()
					/ mMinHeaderHeight, 0.0f, 1.0f);
			int newAlpha = (int) (ratio * 255);
			mActionBarBackgroundDrawable.setAlpha(newAlpha);
			getSupportActionBar().setBackgroundDrawable(
					mActionBarBackgroundDrawable);

			if (newAlpha > 250) {
				String name = mNicknameTextView.getText().toString();
				getSupportActionBar().setTitle(name);
			} else {
				getSupportActionBar().setTitle(null);
			}
		}
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

	public void sendGiftButton(View v) {
		Intent intent = new Intent(this, GiftListActivity.class);
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_ID, mUser.getId());
		this.startActivity(intent);
	}

	public void sendMessageButton(View v) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_FRIEND, mUser);
		this.startActivity(intent);
	}

	public void setting_blockFriend(MenuItem item) {
		if (friendOperate != null) {
			friendOperate.settingBlockFriend();
		}
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

	public void setting_get_historyFriend(MenuItem item) {
		if (friendOperate != null) {
			friendOperate.settingGetHistoryFriend(mUser.getId(),
					App.messageDAO.getMinMessageId(mUser.getId()));
		}
	}

	public void setting_memoFriend(MenuItem item) {
		changeFriendMemo();
	}

	public void setting_nickNameFriend(MenuItem item) {
		changeUserNickName();
	}

	public void setting_reportFriend(MenuItem item) {
		if (friendOperate != null) {
			friendOperate.settingReportFriend();
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

		mPagerAdapter = new TapPagerAdapter(this, getSupportFragmentManager());
		mPagerAdapter.setTabHolderScrollingContent(this);
		mPagerAdapter.addTab(R.string.popular, ProfilePopularFragment.class);
		mPagerAdapter.addTab(R.string.favorite, ProfileLikeFragment.class);
		mPagerAdapter.addTab(R.string.present, ProfilePresentFragment.class);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setCurrentItem(pagerCurrent);

		mTabLayout.setViewPager(mViewPager);
		mTabLayout.setOnPageChangeListener(this);

	}

	private void showMenuButton() {
		if (mUserType == UserType.MYSELF) {
			footerView.setVisibility(View.GONE);
		} else if (mUserType == UserType.FRIEND) {
			footerView.setVisibility(View.VISIBLE);
			mFollowerTextView.setText(R.string.friend_menu_delete);
			mFollowerImageView
					.setBackgroundResource(R.drawable.quxiao_guanzhu_icon);
			mMessageView.setVisibility(View.VISIBLE);
		} else {
			footerView.setVisibility(View.VISIBLE);
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
			menuItemChatHistory.setVisible(false);
			menuItemCleanChatHistory.setVisible(false);
		} else if (mUserType == UserType.FRIEND) {
			menuItemChangeProfile.setVisible(false);
			menuItemChangeNickname.setVisible(true);
			menuItemChangeMemo.setVisible(true);
			menuItemFriendBlock.setVisible(true);
			menuItemFriendReport.setVisible(true);
			menuItemChatHistory.setVisible(true);
			menuItemCleanChatHistory.setVisible(true);
		} else {
			menuItemChangeProfile.setVisible(false);
			menuItemChangeNickname.setVisible(false);
			menuItemChangeMemo.setVisible(false);
			menuItemFriendBlock.setVisible(false);
			menuItemFriendReport.setVisible(true);
			menuItemChatHistory.setVisible(false);
			menuItemCleanChatHistory.setVisible(false);
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
