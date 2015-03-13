package com.ruptech.chinatalk.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.ui.fragment.SegmentTabLayout.OnSegmentClickListener;
import com.ruptech.chinatalk.ui.story.ChannelPopularListActivity;
import com.ruptech.chinatalk.ui.story.UserStoryTranslateActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ChannelListCursorAdapter;
import com.ruptech.chinatalk.widget.HotListCursorAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;
import com.ruptech.chinatalk.widget.UserStoryListCursorAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.ruptech.chinatalk.sqlite.TableContent.ChannelTable;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

public class PopularFragment extends Fragment implements OnRefreshListener,
		OnSegmentClickListener {

	@InjectView(R.id.hot_layout)
	View hotLayoutView;
	@InjectView(R.id.hot_swype)
	SwipeRefreshLayout hotSwypeView;
	@InjectView(R.id.hot_list)
	GridView hotListView;
	@InjectView(R.id.hot_emptyview_text)
	TextView hotEmptyView;

	@InjectView(R.id.channel_layout)
	View channelLayoutView;
	@InjectView(R.id.channel_swype)
	SwipeRefreshLayout channelSwypeView;
	@InjectView(R.id.channel_list)
	ListView channelListView;
	@InjectView(R.id.channel_emptyview_text)
	TextView channelEmptyView;

	@InjectView(R.id.follow_layout)
	View followLayoutView;
	@InjectView(R.id.follow_swype)
	SwipeRefreshLayout followSwypeView;
	@InjectView(R.id.follow_list)
	ListView followListView;
	@InjectView(R.id.follow_emptyview_text)
	TextView followEmptyView;

	private HotListCursorAdapter mHotListCursorAdapter;
	private ChannelListCursorAdapter mChannelListCursorAdapter;
	private UserStoryListCursorAdapter mUserStoryListCursorAdapter;

	private int currentCheckId = -1;
	private View layoutList[];
	private SegmentTabLayout segmentTabLayout;

	private final BroadcastReceiver mHandleRefreshNewMarkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshNewMark();
		}
	};

	private final BroadcastReceiver mHandleChannelListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mChannelListCursorAdapter.doChangeAdapterCursor();
		}
	};

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mUserStoryListCursorAdapter.doChangeAdapterCursor();
			mHotListCursorAdapter.doChangeAdapterCursor();
		}
	};

	public static Fragment newInstance() {
		PopularFragment fragment = new PopularFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHotListCursorAdapter = new HotListCursorAdapter(getActivity(), null);
		mChannelListCursorAdapter = new ChannelListCursorAdapter(getActivity(),
				null);
		mUserStoryListCursorAdapter = new UserStoryListCursorAdapter(
				getActivity(), null);

	}

	private void registerReceiver() {
		getActivity().registerReceiver(mHandleChannelListReceiver,
				new IntentFilter(CommonUtilities.CHANNEL_LIST_ACTION));
		getActivity().registerReceiver(mHandleRefreshNewMarkReceiver,
				new IntentFilter(CommonUtilities.REFERSH_NEW_MARK_ACTION));
		getActivity().registerReceiver(
				mHandleMessageReceiver,
				new IntentFilter(
						CommonUtilities.STORY_POPULAR_CONTENT_MESSAGE_ACTION));
	}

	private void unRegisterReceiver() {
		try {
			getActivity().unregisterReceiver(mHandleChannelListReceiver);
			getActivity().unregisterReceiver(mHandleRefreshNewMarkReceiver);
			getActivity().unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab_popular, container,
				false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onDestroyView() {
		this.unRegisterReceiver();
		super.onDestroyView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		layoutList = new View[] { hotLayoutView, channelLayoutView,
				followLayoutView };

		Utils.showCustomActionbar(getActivity(), createActionBarView());

		setupHotLayout();
		setupChannelLayout();
		setupFollowLayout();

		if (currentCheckId == -1) {
			segmentTabLayout.clickTab(R.string.main_sub_tab_hot);
		} else {
			segmentTabLayout.clickTab(currentCheckId);
		}

		registerReceiver();
	}

	public void refreshNewMark() {
		segmentTabLayout.setNewCountForTab(App.mBadgeCount.followCount, 2);
	}

	private View createActionBarView() {
		LayoutInflater inflator = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.item_segment_bar, null);
		segmentTabLayout = (SegmentTabLayout) view
				.findViewById(R.id.segment_group);
		segmentTabLayout.setOnSegmentClickListener(this);
		segmentTabLayout.addTab(R.string.main_sub_tab_hot);
		segmentTabLayout.addTab(R.string.main_sub_tab_channel);
		segmentTabLayout.addTab(R.string.main_sub_tab_follow);
		segmentTabLayout.populateTab();

		refreshNewMark();
		return view;
	}

	private void setupHotLayout() {
		hotSwypeView.setOnRefreshListener(this);
		hotSwypeView.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		if (!PrefUtils.getPrefInitDeletedHotPhotos()) {
			App.hotUserPhotoDAO.deleteAll();
			PrefUtils.savePrefInitDeletedHotPhotos();
		}

		mHotListCursorAdapter = new HotListCursorAdapter(getActivity(), null);
		hotListView.setAdapter(mHotListCursorAdapter);
		mHotListCursorAdapter.setSwypeLayout(hotSwypeView);
		hotListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor item = (Cursor) mHotListCursorAdapter.getItem(position);
				UserPhoto userPhoto = UserPhotoTable.parseCursor(item);
				UserStoryTranslateActivity.gotoUserStoryCommentActivity(
						userPhoto, getActivity());
			}
		});

	}

	private void setupChannelLayout() {
		channelSwypeView.setOnRefreshListener(this);
		channelSwypeView.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mChannelListCursorAdapter = new ChannelListCursorAdapter(getActivity(),
				null);
		mChannelListCursorAdapter.setSwypeLayout(channelSwypeView);
		channelListView.setAdapter(mChannelListCursorAdapter);
		channelListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor item = (Cursor) mChannelListCursorAdapter
						.getItem(position);
				Channel channel = ChannelTable.parseCursor(item);
				Intent intent = new Intent(getActivity(),
						ChannelPopularListActivity.class);
				intent.putExtra(ChannelPopularListActivity.EXTRA_CHNANEL,
						channel);
				startActivity(intent);
			}
		});

	}

	private void setupFollowLayout() {
		followSwypeView.setOnRefreshListener(this);
		followSwypeView.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mUserStoryListCursorAdapter = new UserStoryListCursorAdapter(
				getActivity(), null);
		mUserStoryListCursorAdapter.setSwypeLayout(followSwypeView);
		followListView.setAdapter(mUserStoryListCursorAdapter);

	}

	private View getActiveLayout() {
		if (layoutList != null) {
			for (int i = 0; i < layoutList.length; i++) {
				if (layoutList[i].getVisibility() == View.VISIBLE)
					return layoutList[i];
			}
		}
		return null;
	}

	private void selectLayout(int checkedId) {
		switch (checkedId) {
		case R.string.main_sub_tab_hot:
			hotLayoutView.setVisibility(View.VISIBLE);
			channelLayoutView.setVisibility(View.GONE);
			followLayoutView.setVisibility(View.GONE);
			break;
		case R.string.main_sub_tab_channel:
			hotLayoutView.setVisibility(View.GONE);
			channelLayoutView.setVisibility(View.VISIBLE);
			followLayoutView.setVisibility(View.GONE);
			break;
		case R.string.main_sub_tab_follow:
			hotLayoutView.setVisibility(View.GONE);
			channelLayoutView.setVisibility(View.GONE);
			followLayoutView.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void refreshLayout(int checkedId) {
		switch (checkedId) {
		case R.string.main_sub_tab_hot:
			if (mHotListCursorAdapter.getCursor() == null
					|| mHotListCursorAdapter.getCount() == 0) {
				mHotListCursorAdapter.doChangeAdapterCursor();
			}
			break;
		case R.string.main_sub_tab_channel:
			if (mChannelListCursorAdapter.getCursor() == null
					|| mChannelListCursorAdapter.getCount() == 0) {
				mChannelListCursorAdapter.doChangeAdapterCursor();
			}
			break;
		case R.string.main_sub_tab_follow:
			if (mUserStoryListCursorAdapter.getCursor() == null
					|| mUserStoryListCursorAdapter.getCount() == 0
					|| App.mBadgeCount.followCount > 0) {
				mUserStoryListCursorAdapter.doChangeAdapterCursor();
			}
			break;
		}

		selectLayout(checkedId);
	}

	@Override
	public void onSegmentClick(int checkedId) {
		refreshLayout(checkedId);
		currentCheckId = checkedId;
	}

	@Override
	public void onRefresh(boolean isUp) {
		if (getActiveLayout() == hotLayoutView) {
			mHotListCursorAdapter.doRetrieveHotList(isUp, hotListView);
		} else if (getActiveLayout() == channelLayoutView) {
			mChannelListCursorAdapter.doRetrieveChannelList(isUp,
					channelListView);
		} else {
			mUserStoryListCursorAdapter.doRetrievePopularList(isUp,
					followListView);
		}
	}

	public void refreshCurrentTab() {
		onRefresh(true);
	}
}