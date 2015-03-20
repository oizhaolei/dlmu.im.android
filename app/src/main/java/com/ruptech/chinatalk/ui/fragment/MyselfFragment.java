package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.ui.setting.SettingSystemInfoActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyselfFragment extends Fragment {

	public static Fragment newInstance() {
		return new MyselfFragment();
	}

	@InjectView(R.id.main_tab_setting_profile_thumb_imageview)
	ImageView mThumbImageview;

	@InjectView(R.id.main_tab_setting_profile_name_textview)
	TextView mNameTextView;
	@InjectView(R.id.main_tab_setting_profile_gender_imageview)
	ImageView mGenderImageView;
	@InjectView(R.id.main_tab_setting_profile_tel_textview)
	TextView mTelTextView;

	@InjectView(R.id.main_tab_setting_profile_top_layout)
	View profileTopView;

	public static MyselfFragment instance = null;

	private final TextPaint tp = new TextPaint();


	@OnClick(R.id.main_tab_myself_setting_layout)
	public void doSystemSettinginfo(View v) {
		Intent intent = new Intent(getActivity(),
				SettingSystemInfoActivity.class);
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

		View v = inflater.inflate(R.layout.main_tab_myself, container, false);
		ButterKnife.inject(this, v);

		return v;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.showNormalActionBar(getActivity());
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		instance = this;

		tp.setTextSize(mNameTextView.getTextSize());
		tp.setTypeface(mNameTextView.getTypeface());

	}


	@OnClick(R.id.main_tab_setting_profile_top_layout)
	public void setting_profile(View v) {
		gotoProfile();
	}


}