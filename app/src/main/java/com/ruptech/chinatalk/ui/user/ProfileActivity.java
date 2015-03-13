package com.ruptech.chinatalk.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.dialog.ChangeFullNameActivity;
import com.ruptech.chinatalk.ui.dialog.ChangeGenderActivity;
import com.ruptech.chinatalk.ui.dialog.ChangePasswordActivity;
import com.ruptech.chinatalk.ui.dialog.ChangePhotoActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ProfileActivity extends ActionBarActivity {
	protected static final int EXTRA_ACTIVITY_RESULT_MODIFY_USER = 1;

	public static final int EXTRA_ACTIVITY_RESULT_MODIFY_FRIEND = 2;

	public static final String EXTRA_FRIEND = "EXTRA_FRIEND";

	public static final String EXTRA_PUBLIC_IS_SELECTED = "EXTRA_PUBLIC_IS_SELECTED";

	public static final String EXTRA_USER = "EXTRA_USER";

	public static final String EXTRA_USER_ID = "EXTRA_USER_ID";

	private static ProfileActivity instance = null;

	static final String TAG = Utils.CATEGORY
			+ ProfileActivity.class.getSimpleName();

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	@InjectView(R.id.activity_profile_user_fullname_textview)
	TextView mFullnameTextView;
	@InjectView(R.id.activity_profile_user_gender_textview)
	TextView mGenderTextView;
	@InjectView(R.id.activity_profile_user_language_textview)
	TextView mLanguageTextView;

	@InjectView(R.id.activity_profile_user_thumb_lang_imageview)
	ImageView langImageView;
	@InjectView(R.id.activity_profile_user_thumb_lang2_imageview)
	ImageView lang2ImageView;
	@InjectView(R.id.activity_profile_user_thumb_lang3_imageview)
	ImageView lang3ImageView;
	@InjectView(R.id.activity_profile_user_thumb_lang4_imageview)
	ImageView lang4ImageView;

	private User mUser;

	private long mUserId;

	@InjectView(R.id.activity_profile_user_tel_textview)
	TextView mTelTextView;

	@InjectView(R.id.activity_profile_user_thumb_imageview)
	ImageView mThumbImageView;

	@InjectView(R.id.activity_profile_user_tel_layout)
	View profileUserTelView;
	@InjectView(R.id.activity_profile_user_password_layout)
	View profileUserPassword;

	@InjectView(R.id.activity_profile_volunteer_imageview)
	ImageView volunteerImageView;

	@OnClick(R.id.activity_profile_user_fullname_layout)
	public void changeUserFullName(View v) {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangeFullNameActivity.class);
		intent.putExtra(EXTRA_USER, mUser);
		startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
	}

	@OnClick(R.id.activity_profile_user_gender_layout)
	public void changeUserGender(View v) {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangeGenderActivity.class);
		intent.putExtra(EXTRA_USER, mUser);
		startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
	}

	private void changeUserLanguage() {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, LanguageActivity.class);
		intent.putExtra(EXTRA_USER, mUser);
		startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
	}

	@OnClick(R.id.activity_profile_user_language_rl)
	public void changeUserLanguage(View v) {
		changeUserLanguage();
	}

	@OnClick(R.id.activity_profile_user_password_layout)
	public void changeUserPassword(View v) {
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangePasswordActivity.class);
		intent.putExtra(EXTRA_USER, mUser);
		startActivity(intent);
	}

	@OnClick(R.id.activity_profile_user_thumb_layout)
	public void changeUserPhoto(View v) {
		if (mUser != null && mUser.getId() == App.readUser().getId()) {
			Intent intent = new Intent(this, ChangePhotoActivity.class);
			intent.putExtra(EXTRA_USER, mUser);
			startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
		}
	}

	private void displayLangView() {
		langImageView.setImageResource(Utils.getLanguageFlag(mUser.lang));
		mLanguageTextView.setText(Utils.getLangDisplayName(mUser.getLang()));
		String additionalLang = getLanguage(0);
		if (!Utils.isEmpty(additionalLang)) {
			lang2ImageView.setVisibility(View.VISIBLE);
			lang2ImageView.setImageResource(Utils
					.getLanguageFlag(additionalLang));
		} else {
			lang2ImageView.setVisibility(View.GONE);
		}

		String additionalLang2 = getLanguage(1);
		if (!Utils.isEmpty(additionalLang2)) {
			lang3ImageView.setVisibility(View.VISIBLE);
			lang3ImageView.setImageResource(Utils
					.getLanguageFlag(additionalLang2));
		} else {
			lang3ImageView.setVisibility(View.GONE);
		}

		String additionalLang3 = getLanguage(2);
		if (!Utils.isEmpty(additionalLang3)) {
			lang4ImageView.setVisibility(View.VISIBLE);
			lang4ImageView.setImageResource(Utils
					.getLanguageFlag(additionalLang3));
		} else {
			lang4ImageView.setVisibility(View.GONE);
		}

	}

	private void displayUser() {
		try {
			if (mUser != null && App.readUser() != null) {
				String thumb = mUser.getPic_url();
				Utils.setUserPicImage(mThumbImageView, thumb);

				if (Utils.isThirdPartyLogin()) {
					profileUserTelView.setClickable(false);
					profileUserPassword.setVisibility(View.GONE);
				}

				if (mUser != null && mUser.getIs_volunteer() > 0) {
					volunteerImageView.setVisibility(View.VISIBLE);
				} else {
					volunteerImageView.setVisibility(View.GONE);
				}

				mFullnameTextView.setText(mUser.getFullname());
				if (Utils.isEmpty(mUser.getTel())) {
					profileUserTelView.setVisibility(View.GONE);
				} else {
					profileUserTelView.setVisibility(View.VISIBLE);
					mTelTextView.setText(mUser.getTel());
				}

				displayLangView();
				if (mUser.getGender() < 1) {
					mGenderTextView.setText(R.string.no_setting);
				} else if (mUser.getGender() == AppPreferences.USERS_GENDER_MALE) {
					mGenderTextView.setText(R.string.gender_male);
				} else if (mUser.getGender() == AppPreferences.USERS_GENDER_FEMALE) {
					mGenderTextView.setText(R.string.gender_female);
				}
			}
		} catch (Exception e) {
			Utils.sendClientException(e);
		}
	}

	@OnClick(R.id.activity_profile_user_thumb_mask)
	public void displayUserOriginal(View v) {
		if (!Utils.isEmpty(mUser.getPic_url())) {
			UserPhoto userPhoto = new UserPhoto();
			userPhoto.setPic_url(mUser.getPic_url());
			ArrayList<String> extraPhotos = new ArrayList<>();
			extraPhotos.add(App.readServerAppInfo().getServerOriginal(
					mUser.getPic_url()));
			Intent intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra(ImageViewActivity.EXTRA_POSITION, 0);
			intent.putExtra(ImageViewActivity.EXTRA_IMAGE_URLS, extraPhotos);
			startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
		} else {
			if (mUser == null || mUserId != App.readUser().getId())
				return;
			Intent intent = new Intent(this, ChangePhotoActivity.class);
			intent.putExtra(EXTRA_USER, mUser);
			startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
		}
	}

	@OnClick(R.id.activity_profile_user_language_layout)
	public void doChangeUserLanguage(View v) {
		changeUserLanguage();
	}

	private String getLanguage(int index) {
		String lang = null;
		if (index == LanguageActivity.MAIN_LANGUAGE_INDEX) {
			lang = mUser.getLang();
		} else {
			if (mUser.getAdditionalLangs() != null
					&& index < mUser.getAdditionalLangs().length)
				lang = mUser.getAdditionalLangs()[index];
		}
		return lang;
	}

	private User getUserFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(EXTRA_USER);
			return user;
		}
		return null;
	}

	private long getUserIdFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(EXTRA_USER_ID);
			return userId;
		}
		return 0;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == EXTRA_ACTIVITY_RESULT_MODIFY_USER) {// modify
				if (null != data.getExtras()) {
					Bundle extras = data.getExtras();
					mUser = (User) extras.get(EXTRA_USER);
					getSupportActionBar().setTitle(mUser.getFullname());

					mFullnameTextView.setText(mUser.getFullname());
					if (Utils.isEmpty(mUser.getTel())) {
						profileUserTelView.setVisibility(View.GONE);
					} else {
						profileUserTelView.setVisibility(View.VISIBLE);
						mTelTextView.setText(mUser.getTel());
					}
					displayLangView();

					if (mUser.getGender() < 1) {
						mGenderTextView.setText(R.string.no_setting);
					} else if (mUser.getGender() == AppPreferences.USERS_GENDER_MALE) {
						mGenderTextView.setText(R.string.gender_male);
					} else if (mUser.getGender() == AppPreferences.USERS_GENDER_FEMALE) {
						mGenderTextView.setText(R.string.gender_female);
					}

					Utils.setUserPicImage(mThumbImageView, mUser.getPic_url());
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.detail_information);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;
		mUser = getUserFromExtras();
		if (mUser == null) {
			mUserId = getUserIdFromExtras();
		} else {
			mUserId = mUser.getId();
		}
		if (mUser == null && mUserId <= 0) {
			Toast.makeText(this, R.string.user_infomation_is_invalidate,
					Toast.LENGTH_LONG).show();
			finish();
		}
		displayUser();
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
}
