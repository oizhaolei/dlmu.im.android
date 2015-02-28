package com.ruptech.chinatalk.ui.friend;

import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListCursorAdapter;

public class FriendListActivity extends ActionBarActivity implements
		OnQueryTextListener {
	public static void doRefresh() {
		if (instance != null) {
			if (instance.mFriendListViewAdapter != null) {
				instance.searchFriend();
			}
		}
	}

	private static FriendListActivity instance;
	public static final String EXTRA_GOTO_ACTIVITY_FLAG = "EXTRA_GOTO_ACTIVITY_FLAG";
	private int gotoActivityFlag = 0;

	private static final String TAG = Utils.CATEGORY
			+ FriendListActivity.class.getSimpleName();

	private FriendListCursorAdapter mFriendListViewAdapter;

	@InjectView(R.id.activity_friend_listView)
	ListView mFriendListView;
	@InjectView(R.id.activity_friend_list_emptyview_text)
	TextView emptyTextView;

	private MenuItem menuItemSearchFriend;

	private String keyword;

	private void getExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			gotoActivityFlag = extras.getInt(EXTRA_GOTO_ACTIVITY_FLAG);
		}
	}

	private void gotoProfileActivity(Activity context, User user) {
		Intent intent = new Intent(context, FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		context.startActivity(intent);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			mFriendListViewAdapter.notifyDataSetChanged();
		}
	};

	@Override
	public void onBackPressed() {
		if (menuItemSearchFriend.isActionViewExpanded()) {
			menuItemSearchFriend.collapseActionView();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.friend_follow);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getExtras();
		instance = this;
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.friend_search, mMenu);
		menuItemSearchFriend = mMenu.findItem(R.id.friend_search);
		SearchView searchView = (SearchView) menuItemSearchFriend
				.getActionView();
		searchView = Utils.cutomizeSearchView(searchView);
		searchView.setIconified(true);
		searchView.setQueryHint(getResources().getString(R.string.full_name));
		searchView.setOnQueryTextListener(this);
		menuItemSearchFriend
				.setOnActionExpandListener(new OnActionExpandListener() {

					@Override
					public boolean onMenuItemActionCollapse(MenuItem item) {
						keyword = "";
						searchFriend();
						return true;
					}

					@Override
					public boolean onMenuItemActionExpand(MenuItem item) {
						// TODO Auto-generated method stub
						return true;
					}

				});

		return super.onCreateOptionsMenu(mMenu);

	}

	@Override
	public void onDestroy() {
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
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		keyword = arg0;
		searchFriend();
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		instance = this;
		searchFriend();
	}

	private void searchFriend() {

		if (keyword == null || keyword.length() == 0) {
			mFriendListViewAdapter.changeCursor(App.userDAO.fetchFriends(App
					.readUser().getId()));
		} else {
			mFriendListViewAdapter.changeCursor(App.userDAO.fetchFriends(App
					.readUser().getId(), keyword));
		}

	}

	private void setupComponents() {
		mFriendListViewAdapter = new FriendListCursorAdapter(this,
				App.userDAO.fetchFriends(App.readUser().getId()));
		mFriendListView.setAdapter(mFriendListViewAdapter);
		mFriendListView.setEmptyView(emptyTextView);
		mFriendListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor item = (Cursor) mFriendListViewAdapter.getItem(position);
				User user = UserTable.parseCursor(item);
				if (gotoActivityFlag == 0) {
					Intent intent = new Intent(FriendListActivity.this,
							ChatActivity.class);
					intent.putExtra(ChatActivity.EXTRA_FRIEND, user);
					startActivity(intent);
				} else {
					gotoProfileActivity(FriendListActivity.this, user);
				}
			}
		});

	}
}