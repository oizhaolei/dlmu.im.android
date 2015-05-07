package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruptech.chinatalk.ui.OrgActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServiceFragment extends Fragment {

	private static final String TAG = Utils.CATEGORY
			+ ServiceFragment.class.getSimpleName();

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.main_tab_service, container, false);
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
	}


}