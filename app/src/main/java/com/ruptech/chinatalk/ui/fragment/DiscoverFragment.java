package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.OrgActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiscoverFragment extends Fragment {
	public static Fragment newInstance() {
		DiscoverFragment fragment = new DiscoverFragment();
		return fragment;
	}

	private static final String TAG = Utils.CATEGORY
			+ DiscoverFragment.class.getSimpleName();

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.main_tab_discover, container, false);
		ButterKnife.inject(this, v);

		return v;

	}

	@OnClick(R.id.main_tab_organize_muc_testroom1_layout)
	public void chatToMucTestRoom1(View v) {
		Intent orgIntent = new Intent(getActivity(), OrgActivity.class);
		orgIntent.putExtra(OrgActivity.PARENT_ORG_ID, "100000");
		orgIntent.putExtra(OrgActivity.PARENT_ORG_NAME, getString(R.string.dlmu_title));
		startActivity(orgIntent);

		//startChatActivity("org_101000@im.dlmu.edu.cn");

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
	}


}