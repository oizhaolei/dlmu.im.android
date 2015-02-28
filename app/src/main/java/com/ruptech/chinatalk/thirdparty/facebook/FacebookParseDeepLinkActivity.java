package com.ruptech.chinatalk.thirdparty.facebook;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.ui.LoginGateActivity;
import com.ruptech.chinatalk.ui.LoginLoadingActivity;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;

public class FacebookParseDeepLinkActivity extends Activity {

	final public static String RECHARGE_FREE = "RECHARGE_FREE";
	final public static String SHARE_STORY = "SHARE_STORY";

	final private static String RECHARGE_FREE_URL = "www.tttalk.org/download.php";
	final private static String SHARE_STORY_URL_1 = "popular.tttalk.org";
	final private static String SHARE_STORY_URL_2 = "app.tttalk.org";

	protected final static String TAG = Utils.CATEGORY
			+ FacebookParseDeepLinkActivity.class.getSimpleName();

	private static FacebookParseDeepLinkActivity instance = null;

	private String calledFrom;
	public static final int INVALID = -1;
	private UserPhoto mUserPhoto;
	private String photoId;
	private String deepLink;

	protected final TaskListener mUserPhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
			if (result == TaskResult.OK) {
				mUserPhoto = retrieveUserPhotoTask.getUserPhoto();

				// 清空与当前story相关的通知栏通知
				long userPhotoId = retrieveUserPhotoTask.getUserPhotoId();
				App.notificationManager
						.cancel(R.layout.activity_story_comment_list
								+ (int) userPhotoId);

				CommonUtilities.broadcastStoryMessage(App.mContext, mUserPhoto);

				// Intent intent = new
				// Intent(FacebookParseDeepLinkActivity.this,
				// UserStoryImageViewActivity.class);
				Intent intent = new Intent(FacebookParseDeepLinkActivity.this,
						UserStoryCommentActivity.class);
				intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
						mUserPhoto);

				startActivity(intent);

				finish();
			} else {
				String msg = getString(R.string.photo_data_error);
				Toast.makeText(FacebookParseDeepLinkActivity.this, msg,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	};

	private void doRetrieveUserPhoto(String id) {
		GenericTask mUserPhotoTask = new RetrieveUserPhotoTask(id,
				App.readUser().lang);
		mUserPhotoTask.setListener(mUserPhotoTaskListener);
		mUserPhotoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void gotoLoginGateActivity() {
		Intent intent = new Intent(FacebookParseDeepLinkActivity.this,
				LoginGateActivity.class);
		startActivity(intent);
	}

	private void handleDeepLink() {
		if (SHARE_STORY.equals(calledFrom)) {
			if (App.readUser() == null) {
				gotoLoginGateActivity();
				this.finish();
			} else {
				doRetrieveUserPhoto(photoId);
			}
		} else if (RECHARGE_FREE.equals(calledFrom)) {
			LoginLoadingActivity
					.gotoMainActivity(FacebookParseDeepLinkActivity.this);
			this.finish();
		} else {
			this.finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Uri deepLinkUri = getIntent().getData();
		instance = this;
		if (deepLinkUri != null) {
			parseDeepLinkId(deepLinkUri);
			handleDeepLink();
		} else {
			finish();
		}
	}

	private void parseDeepLinkId(Uri deepLinkUri) {
		deepLink = deepLinkUri.toString();
		// deepLink =
		// "http://popular.tttalk.org:4001/user_photo?id=52342&lang=JP&loginid=47871&sign=413d4f852c401aa728ac74860b46e65ac1c42042&source=an-2014090918";

		if (deepLink.indexOf(RECHARGE_FREE_URL) > INVALID) {
			calledFrom = RECHARGE_FREE;
		} else if (deepLink.indexOf(SHARE_STORY_URL_1) > INVALID
				|| deepLink.indexOf(SHARE_STORY_URL_2) > INVALID) {
			calledFrom = SHARE_STORY;
			photoId = Uri.parse(deepLink).getQueryParameter("id");
		}
	}

}
