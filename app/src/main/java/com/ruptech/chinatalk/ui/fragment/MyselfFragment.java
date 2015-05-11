package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.ui.setting.SettingSystemInfoActivity;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyselfFragment extends Fragment {

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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.setting_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@OnClick(R.id.main_tab_myself_setting_layout)
	public void doSystemSettinginfo(View v) {
		Intent intent = new Intent(getActivity(),
				SettingSystemInfoActivity.class);
		startActivity(intent);
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

		mTelTextView.setText(App.readUser().getUsername());
		String portrait = AppVersion.getPortraitUrl(App.readUser().getUsername());
		Utils.setUserPicImage(mThumbImageview, portrait);

	}

}