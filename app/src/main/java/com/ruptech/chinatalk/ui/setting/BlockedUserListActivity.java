package com.ruptech.chinatalk.ui.setting;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.adapter.BlockUserAdapter;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class BlockedUserListActivity extends ActionBarActivity {

	private final String TAG = Utils.CATEGORY
			+ BlockedUserListActivity.class.getSimpleName();
	@InjectView(R.id.activity_block_user_listView)
	ListView blockedFriendListView;
	BlockUserAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_block_user_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.block_users);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// 加载画面
		setupComponents();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		return super.onCreateOptionsMenu(mMenu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void setupComponents() {
		adapter = new BlockUserAdapter(this);
		blockedFriendListView.setAdapter(adapter);
		adapter.requery();
	}

}