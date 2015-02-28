package com.ruptech.chinatalk.ui.friend;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.InviteFriendUtils;

public class FriendInviteActivity extends ActionBarActivity {

	@InjectView(R.id.friend_invite_main_memo0)
	TextView memoText;

	@OnClick(R.id.contact_friend_talk_img)
	public void onClickFriendInvite(View view) {
		InviteFriendUtils.inviteKakaoFriend(this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_invite_main);
		ButterKnife.inject(this);
		memoText.setText(String.format(
				getString(R.string.recharge_free_contact_add_memo),
				App.readServerAppInfo().point_by_invite_sns_friend));

		// getSupportActionBar().setTitle(R.string.add_friend);
		getSupportActionBar().setTitle(R.string.recharge_free_contact_add);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

}
