package com.ruptech.chinatalk.ui.story;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

/**
 * 支持各种查询：地址，朋友等
 *
 * @author zhaolei
 *
 */
public class UserStoryListActivity extends AbstractUserStoryListActivity {

	private String mAddress;

	public static final String EXTRA_TITLE = "EXTRA_TITLE";

	private void doPostStory() {
		Intent intent = new Intent(this, PhotoAlbumActivity.class);
		startActivity(intent);
	}

	@Override
	protected void extractExtras() {
		super.extractExtras();
		Bundle extras = getIntent().getExtras();

		mLate6 = extras.getInt(EXTRA_STORY_LATE6, 0);
		mLnge6 = extras.getInt(EXTRA_STORY_LNGE6, 0);
		mAddress = extras.getString(EXTRA_STORY_ADDRESS);
		mTag = extras.getString(EXTRA_STORY_TAG);

		String title = extras.getString(EXTRA_TITLE, getString(R.string.story));
		getSupportActionBar().setTitle(title);
		if (Utils.isEmpty(mAddress) && Utils.isValidLocation6(mLate6, mLnge6)) {
			AsyncTask<Void, Void, Void> mAddressTask;
			mAddressTask = new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					mAddress = Utils.getCity(UserStoryListActivity.this,
							mLate6, mLnge6);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					if (!Utils.isEmpty(mAddress)) {
						getSupportActionBar().setTitle(mAddress);
					}
				}
			};
			mAddressTask.execute();

			mStoryType = STORY_TYPE_LOCATION;
		} else if (!Utils.isEmpty(mAddress)) {
			getSupportActionBar().setTitle(mAddress);
			mStoryType = STORY_TYPE_LOCATION;
		} else if (!Utils.isEmpty(mTag)) {
			getSupportActionBar().setTitle(Utils.getStoryTagNameByCode(mTag));
			mStoryType = STORY_TYPE_TAG;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		extractExtras();
		setupComponents();
		doRetrieveUserPhotoList(true);
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

}
