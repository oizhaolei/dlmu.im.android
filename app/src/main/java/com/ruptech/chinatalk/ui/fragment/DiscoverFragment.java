package com.ruptech.chinatalk.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.ui.TTTActivity;
import com.ruptech.chinatalk.ui.friend.FriendAddRecommendedActivity;
import com.ruptech.chinatalk.ui.friend.FriendsLbsListActivity;
import com.ruptech.chinatalk.ui.friend.FriendsLeaderboardListActivity;
import com.ruptech.chinatalk.ui.friend.FriendsOnlineListActivity;
import com.ruptech.chinatalk.ui.story.UserStoryNewListActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.DiscoverKeywordListAdapter;
import com.ruptech.chinatalk.widget.DiscoverUserArrayAdapter.OnDiscoverListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class DiscoverFragment extends Fragment implements TextWatcher,
		OnDiscoverListener {
	public static Fragment newInstance() {
		DiscoverFragment fragment = new DiscoverFragment();
		return fragment;
	}

	private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			doRefresh();
		}
	};

	private static final int LAYOUT_MAIN = 0;
	private static final int KEYWORD_MIN_LENGTH = 4;
	private static final int LAYOUT_KEYWORD = 1;
	private static final int LAYOUT_RESULT = 2;

	private int currentLayout = LAYOUT_MAIN;
	private String keyword;

	private DiscoverKeywordListAdapter mKeywordAdapter;
	protected TabFragmentPagerAdapter mTabPagerAdapter;

	@InjectView(R.id.sliding_tabs)
	SlidingTabLayout mSlidingTabLayout;

	@InjectView(R.id.viewpager)
	ViewPager mViewPager;

	@InjectView(R.id.emptyTextView)
	View emptyTextView;

	@InjectView(R.id.main_tab_discover_keyword_layout)
	View keywordLayout;

	@InjectView(R.id.main_tab_discover_result_layout)
	View resultLayout;

	@InjectView(R.id.main_tab_discover_listView)
	ListView searchResultListView;

	@InjectView(R.id.main_tab_discover_friend_lbs_layout)
	View lbsView;

	@InjectView(R.id.main_tab_discover_user_photo_lbs_layout)
	View userPhotoLbsView;

	@InjectView(R.id.main_tab_discover_friend_recommended_layout)
	View recommendView;

	@InjectView(R.id.search_text)
	EditText searchTextView;

	@InjectView(R.id.search_clear)
	ImageView searchClearBtn;

	@InjectView(R.id.search_cancel_textview)
	TextView searchCancelText;

	private static final String TAG = Utils.CATEGORY
			+ DiscoverFragment.class.getSimpleName();

	private SubDiscoverUserFragment userFragment;

	private SubDiscoverChannelFragment channelFragment;

	InputMethodManager mInputMethodManager;

	@Override
	public void afterTextChanged(Editable s) {
		keyword = s.toString().trim();
		searchKeyword();
	}

	public boolean backPressed() {
		if (resultLayout.getVisibility() == View.VISIBLE) {
			showLayout(LAYOUT_KEYWORD);
			return true;
		} else if (keywordLayout.getVisibility() == View.VISIBLE) {
			searchTextView.setText("");
			showLayout(LAYOUT_MAIN);
			return true;
		}
		return false;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	private void doRefresh() {
		if (lbsView != null && userPhotoLbsView != null) {
			if (MyLocation.recentLocation != null) {
				lbsView.setVisibility(View.VISIBLE);
				userPhotoLbsView.setVisibility(View.VISIBLE);
			} else {
				lbsView.setVisibility(View.GONE);
				userPhotoLbsView.setVisibility(View.GONE);
			}
		}
		if (recommendView != null) {
			if (PrefUtils.readRecommendedFriendUserList() != null
					&& PrefUtils.readRecommendedFriendUserList().size() > 0) {
				recommendView.setVisibility(View.VISIBLE);
			} else {
				recommendView.setVisibility(View.GONE);
			}
		}
	}

	public String getKeyword() {
		return keyword;
	}

	@OnClick(R.id.main_tab_discover_translation_secretary_layout)
	public void gotoChatTTT() {
		Intent intent = new Intent(getActivity(), TTTActivity.class);
		getActivity().startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_friend_lbs_layout)
	public void gotoLbsFriend(View v) {
		Intent intent = new Intent(getActivity(), FriendsLbsListActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_user_photo_lbs_layout)
	public void gotoLbsUserPhoto(View v) {
		Intent intent = new Intent(getActivity(),
				UserStoryNewListActivity.class);
		intent.putExtra(UserStoryNewListActivity.EXTRA_USER_STORY_TYPE,
				UserStoryNewListActivity.STORY_TYPE_LBS);
		getActivity().startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_friend_leaderboard_layout)
	public void gotoLeaderBoardFriend(View v) {
		Intent intent = new Intent(getActivity(),
				FriendsLeaderboardListActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_user_photo_new_layout)
	public void gotoNewUserPhoto(View v) {
		Intent intent = new Intent(getActivity(),
				UserStoryNewListActivity.class);
		intent.putExtra(UserStoryNewListActivity.EXTRA_USER_STORY_TYPE,
				UserStoryNewListActivity.STORY_TYPE_NEW);
		getActivity().startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_friend_online_layout)
	public void gotoOnlineFriend(View v) {
		Intent intent = new Intent(getActivity(),
				FriendsOnlineListActivity.class);
		startActivity(intent);
	}

	@OnClick(R.id.main_tab_discover_friend_recommended_layout)
	public void gotoRecommendedFriend(View v) {
		Intent intent = new Intent(getActivity(),
				FriendAddRecommendedActivity.class);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mKeywordAdapter = new DiscoverKeywordListAdapter(getActivity(),
				R.layout.item_main_tab_friend);
		getActivity().registerReceiver(mLocationReceiver,
				new IntentFilter(CommonUtilities.ADDRESS_UPDATE_ACTION));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.main_tab_discover, container, false);
		ButterKnife.inject(this, v);

		return v;
	}

	@Override
	public void onDestroy() {
		try {
			getActivity().unregisterReceiver(mLocationReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	public void onDiscover(long userCount, long channelCount,
			List<Channel> channelList) {

		setTabTitle(userCount, channelCount);
		if (channelFragment != null) {
			channelFragment.setChannelList(channelList);
		}

		if (userCount > 0 || channelCount == 0) {
			mViewPager.setCurrentItem(0);
		} else {
			mViewPager.setCurrentItem(1);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.showNormalActionBar(getActivity());
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		doRefresh();

		mInputMethodManager = (InputMethodManager) getActivity()
				.getApplicationContext().getSystemService(
						Context.INPUT_METHOD_SERVICE);

		searchTextView.setHint(R.string.discover_search);
		searchTextView.addTextChangedListener(this);
		searchTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		searchTextView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchFriend();
					return true;
				}
				return false;
			}
		});

		searchClearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchTextView.setText("");
			}

		});

		searchCancelText.setVisibility(View.GONE);
		searchCancelText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchTextView.setText("");
				showLayout(LAYOUT_MAIN);
				mInputMethodManager.hideSoftInputFromWindow(
						searchTextView.getWindowToken(), 0);
			}
		});

		searchResultListView.setAdapter(mKeywordAdapter);
		searchResultListView.setEmptyView(emptyTextView);
		emptyTextView.setClickable(false);
		emptyTextView.setOnClickListener(null);

		searchResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				String key = mKeywordAdapter.getItem(position);
				searchTextView.setText(key);
				searchTextView.setSelection(key.length());
				searchFriend();
			}

		});
		showLayout(currentLayout);
		setupResultLayout();
	}

	private void searchFriend() {
		showLayout(LAYOUT_RESULT);
		if (userFragment != null) {
			userFragment.setKeyword(keyword);
			userFragment.clear();
			userFragment.onRefresh(true);
		}
		if (channelFragment != null) {
			channelFragment.setKeyword(keyword);
			channelFragment.clear();
			channelFragment.onRefresh(true);
		}

	}

	private void searchKeyword() {
		if (!Utils.isEmpty(keyword) && keyword.length() >= KEYWORD_MIN_LENGTH) {
			mKeywordAdapter.getList(keyword);
			showLayout(LAYOUT_KEYWORD);
		}
	}

	private void setTabTitle(long userCount, long channelCount) {
		String userTitle = getResources().getString(R.string.discover_tab_user,
				userCount);
		String channelTitle = getResources().getString(
				R.string.discover_tab_channel, channelCount);
		mSlidingTabLayout.setTitleForTab(userTitle, 0);
		mSlidingTabLayout.setTitleForTab(channelTitle, 1);
	}

	private void setupResultLayout() {
		mTabPagerAdapter = new TabFragmentPagerAdapter(
				getChildFragmentManager());

		mTabPagerAdapter.addTabItem(new PagerItem(getResources().getString(
				R.string.discover_tab_user, 0)) {
			@Override
			Fragment createFragment() {
				userFragment = (SubDiscoverUserFragment) SubDiscoverUserFragment
						.newInstance();
				return userFragment;
			}
		});
		mTabPagerAdapter.addTabItem(new PagerItem(getResources().getString(
				R.string.discover_tab_channel, 0)) {
			@Override
			Fragment createFragment() {
				channelFragment = (SubDiscoverChannelFragment) SubDiscoverChannelFragment
						.newInstance();
				return channelFragment;
			}
		});

		mViewPager.setAdapter(mTabPagerAdapter);
		mSlidingTabLayout.setViewPager(mViewPager);
	}

	private void showLayout(int layout) {
		switch (layout) {
		case LAYOUT_KEYWORD:
			searchCancelText.setVisibility(View.VISIBLE);
			resultLayout.setVisibility(View.GONE);
			keywordLayout.setVisibility(View.VISIBLE);
			break;
		case LAYOUT_RESULT:
			searchCancelText.setVisibility(View.VISIBLE);
			resultLayout.setVisibility(View.VISIBLE);
			keywordLayout.setVisibility(View.GONE);
			break;
		default:
			resultLayout.setVisibility(View.GONE);
			keywordLayout.setVisibility(View.GONE);
			searchCancelText.setVisibility(View.GONE);
		}

		currentLayout = layout;
	}
}