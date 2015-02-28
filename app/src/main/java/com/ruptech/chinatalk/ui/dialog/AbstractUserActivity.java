package com.ruptech.chinatalk.ui.dialog;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.user.ProfileActivity;

public abstract class AbstractUserActivity extends ActionBarActivity {

	protected User mUser;

	private User getUserFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ProfileActivity.EXTRA_USER);
			return user;
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUser = getUserFromExtras();
		if (mUser == null) {
			Toast.makeText(this, R.string.user_infomation_is_invalidate,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

}
