package com.ruptech.chinatalk.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.thirdparty.model.Share;
import com.ruptech.chinatalk.ui.friend.FriendListActivity;
import com.ruptech.chinatalk.ui.friend.FriendsFollowerListActivity;
import com.ruptech.chinatalk.ui.setting.SettingSystemInfoActivity;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.ui.story.MyChannelListActivity;
import com.ruptech.chinatalk.ui.story.MyCommentListActivity;
import com.ruptech.chinatalk.ui.story.MyTranslateListActivity;
import com.ruptech.chinatalk.ui.story.UserStoryGiftListActivity;
import com.ruptech.chinatalk.ui.story.UserStoryListActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ThirdPartyUtil;
import com.ruptech.chinatalk.utils.Utils;

import java.text.NumberFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyselfFragment extends Fragment {

	public static Fragment newInstance() {
		MyselfFragment fragment = new MyselfFragment();
		return fragment;
	}

	private final BroadcastReceiver mHandleRefreshNewMarkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshNewMark();
		}
	};

	@InjectView(R.id.main_tab_setting_profile_thumb_imageview)
	ImageView mThumbImageview;

	@InjectView(R.id.main_tab_setting_profile_name_textview)
	TextView mNameTextView;
	@InjectView(R.id.main_tab_setting_profile_lang_imageview)
	ImageView mLanguageImageView;
	@InjectView(R.id.main_tab_setting_profile_gender_imageview)
	ImageView mGenderImageView;
	@InjectView(R.id.main_tab_setting_profile_popular_textview)
	TextView mPopularTextView;
	@InjectView(R.id.main_tab_setting_profile_follow_textview)
	TextView mFollowTextView;
	@InjectView(R.id.main_tab_setting_profile_tel_textview)
	TextView mTelTextView;
	@InjectView(R.id.main_tab_setting_profile_fans_new_text)
	TextView mFasNewTextView;

	private static TextView mFansTextView;
	private static TextView mGiftTextView;
	@InjectView(R.id.main_tab_myself_balance_textview)
	TextView mBalanceTextView;
	@InjectView(R.id.main_tab_setting_profile_top_layout)
	View profileTopView;

	private static ImageView newMarkImgView;
	public static MyselfFragment instance = null;

	private final TextPaint tp = new TextPaint();

	private static ImageView fansNewMarkimageView;

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateBalance();
		}
	};

	private void displayUser() {
		if (App.readUser() != null) {
			String thumb = App.readUser().getPic_url();
			Utils.setUserPicImage(mThumbImageview, thumb);

			profileTopView.post(new Runnable() {
				@Override
				public void run() {
					float maxWidth = profileTopView.getMeasuredWidth() * 0.4f;
					String name = (String) TextUtils.ellipsize(App.readUser()
							.getFullname(), tp, maxWidth,
							TextUtils.TruncateAt.END);
					mNameTextView.setText(name);

					maxWidth = profileTopView.getMeasuredWidth() * 0.8f;
					String tel = (String) TextUtils.ellipsize(App.readUser()
							.getTel(), tp, maxWidth, TextUtils.TruncateAt.END);
					mTelTextView.setText(tel);
				}

			});

			mLanguageImageView.setImageResource(Utils.getLanguageFlag(App
					.readUser().lang));

			mGenderImageView.setVisibility(View.VISIBLE);
			if (App.readUser().getGender() < 1) {
				mGenderImageView.setVisibility(View.INVISIBLE);
			} else if (App.readUser().getGender() == AppPreferences.USERS_GENDER_MALE) {
				mGenderImageView.setImageResource(R.drawable.male);
			} else if (App.readUser().getGender() == AppPreferences.USERS_GENDER_FEMALE) {
				mGenderImageView.setImageResource(R.drawable.female);
			}

			mBalanceTextView.setText(Html.fromHtml(NumberFormat
					.getNumberInstance().format(App.readUser().getBalance()))
					+ " " + getString(R.string.point));

			mPopularTextView.setText(String.valueOf(App.readUser()
					.getAlbum_count()));
			mFollowTextView.setText(String.valueOf(App.readUser()
					.getFollow_count()));
			mFansTextView.setText(String.valueOf(App.readUser()
					.getFollower_count()));
			mGiftTextView.setText(String.valueOf(App.readUser()
					.getPresent_count()));
			mThumbImageview
					.setBackgroundResource(R.drawable.background_round_button_profile);
			mGenderImageView
					.setBackgroundResource(R.drawable.background_round_button_gender);
		}
	}

	@OnClick(R.id.main_tab_myself_setting_layout)
	public void doSystemSettinginfo(View v) {
		Intent intent = new Intent(getActivity(),
				SettingSystemInfoActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_myself_channel_layout)
	public void goto_my_channel(View v) {
		Intent intent = new Intent(getActivity(), MyChannelListActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_myself_comment_layout)
	public void goto_my_comment(View v) {
		Intent intent = new Intent(getActivity(), MyCommentListActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER_ID, App.readUser().getId());
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_myself_like_layout)
	public void goto_my_favorite(View v) {
		Intent intent = new Intent(getActivity(), UserStoryListActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER_ID, App.readUser().getId());
		String name = App.readUser().getFullname();
		intent.putExtra(UserStoryListActivity.EXTRA_TITLE, name + "-" + "-"
				+ getString(R.string.favorite));
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_TYPE,
				AbstractUserStoryListActivity.STORY_TYPE_FAVORITE);

		startActivity(intent);
	}

	@OnClick(R.id.main_tab_myself_translate_layout)
	public void goto_my_translate(View v) {
		Intent intent = new Intent(getActivity(), MyTranslateListActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER_ID, App.readUser().getId());
		startActivity(intent);
	}

	private void gotoProfile() {
		Intent intent = new Intent(getActivity(), FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, App.readUser());
		this.startActivity(intent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		getActivity().registerReceiver(mHandleMessageReceiver,
				new IntentFilter(CommonUtilities.BALANCE_UPDATE_ACTION));

		View v = inflater.inflate(R.layout.main_tab_myself, container, false);
		ButterKnife.inject(this, v);

		return v;
	}

	@Override
	public void onDestroy() {
		try {
			getActivity().unregisterReceiver(mHandleMessageReceiver);
			getActivity().unregisterReceiver(mHandleRefreshNewMarkReceiver);
		} catch (Exception e) {
		}

		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.showNormalActionBar(getActivity());
		displayUser();
		refreshNewMark();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		instance = this;
		mFansTextView = (TextView) getView().findViewById(
				R.id.main_tab_setting_profile_fans_textview);
		mGiftTextView = (TextView) getView().findViewById(
				R.id.main_tab_setting_profile_gift_textview);
		newMarkImgView = (ImageView) getView().findViewById(
				R.id.main_tab_myself_setting_new_mark);
		fansNewMarkimageView = (ImageView) getView().findViewById(
				R.id.main_tab_setting_profile_fans_new_mark_imageview);

		tp.setTextSize(mNameTextView.getTextSize());
		tp.setTypeface(mNameTextView.getTypeface());

		displayUser();
		getActivity().registerReceiver(mHandleRefreshNewMarkReceiver,
				new IntentFilter(CommonUtilities.REFERSH_NEW_MARK_ACTION));

	}

	public void refreshNewMark() {
		if (App.mBadgeCount.friendCount > 0) {
			mFasNewTextView.setText("+"
					+ String.valueOf(App.mBadgeCount.friendCount));
			mFasNewTextView.setVisibility(View.VISIBLE);
		} else {
			mFasNewTextView.setVisibility(View.GONE);
		}

		if (App.mBadgeCount.versionCount > 0) {
			newMarkImgView.setVisibility(View.VISIBLE);
		} else {
			newMarkImgView.setVisibility(View.GONE);
		}
	}

	@OnClick(R.id.main_tab_setting_profile_top_layout)
	public void setting_profile(View v) {
		gotoProfile();
	}

	@OnClick(R.id.main_tab_myself_share_layout)
	public void setting_share(View v) {

		Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
				.getLang());
		String downUrl = "";
		if (share != null) {
			downUrl = share.getThirdparty_share_targeturl();
		} else {
			downUrl = AppPreferences.APK_DOWNLOAD_URL;
		}
		String content = getActivity().getString(R.string.invite_message,
				App.readUser().getFullname(), downUrl);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, content);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getActivity().startActivity(
				Intent.createChooser(intent,
						getActivity().getString(R.string.share_to_my_friend)));
	}

	@OnClick(R.id.main_tab_myself_balance_layout)
	public void setting_recharge(View v) {
		Intent intent = new Intent(getActivity(), MyWalletActivity.class);
		startActivity(intent);
	}

	private void updateBalance() {
		mBalanceTextView.setText(Html.fromHtml(NumberFormat.getNumberInstance()
				.format(App.readUser().getBalance()))
				+ " "
				+ getString(R.string.point));
	}

	@OnClick(R.id.main_tab_setting_profile_fans_layout)
	public void viewFansFriendList(View v) {
		Intent intent = new Intent(getActivity(),
				FriendsFollowerListActivity.class);
		startActivity(intent);
		PrefUtils.removePrefUserNewFansCount(App.readUser().getId());
	}

	@OnClick(R.id.main_tab_setting_profile_gift_layout)
	public void viewUserGiftList(View v) {
		Intent intent = new Intent(getActivity(),
				UserStoryGiftListActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER_ID, App.readUser().getId());
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_setting_profile_follow_layout)
	public void viewFollowFriendList(View v) {
		Intent intent = new Intent(getActivity(), FriendListActivity.class);
		intent.putExtra(FriendListActivity.EXTRA_GOTO_ACTIVITY_FLAG, 1);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_setting_profile_popular_layout)
	public void viewPopular(View v) {
		gotoProfile();
	}
}