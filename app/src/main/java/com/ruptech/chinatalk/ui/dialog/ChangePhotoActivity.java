package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

public class ChangePhotoActivity extends AbstractUploadPhotoActivity {

	private GenericTask mUserProfileChangeTask;

	private static ChangePhotoActivity instance = null;

	private final TaskListener mUserProfileChangeTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserProfileChangeTask userProfileChangeTask = (UserProfileChangeTask) task;
			if (result == TaskResult.OK) {
				User user = userProfileChangeTask.getUser();
				onPhotoChangeSuccess(user);
			} else {
				String msg = task.getMsg();
				onPhotoChangeFailure(msg);
			}

		}

		@Override
		public void onPreExecute(GenericTask task) {
			onPhotoChangeBegin();
		}

	};

	private final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FileUploadTask photoTask = (FileUploadTask) task;
			Utils.dismissDialog(progressDialog);
			if (result == TaskResult.OK) {
				Toast.makeText(ChangePhotoActivity.this, R.string.save_success,
						Toast.LENGTH_LONG).show();

				String photo_name = photoTask.getFileInfo().fileName;

				String mFunc = "";
				String mKey = "";
				String mValue = "";
				if (EXTRA_CHANGE_TASK != null
						&& EXTRA_CHANGE_TASK.equals("changeUserBackground")) {
					mFunc = "change_prop";
					mKey = "background_url";
					mValue = photo_name;
				} else {
					mFunc = "change_column";
					mKey = "photo_name";
					mValue = photo_name;
				}
				mUserProfileChangeTask = new UserProfileChangeTask(mFunc, mKey,
						mValue);
				mUserProfileChangeTask
						.setListener(mUserProfileChangeTaskListener);
				mUserProfileChangeTask.execute();
			} else if (result == TaskResult.FAILED) {
				UserProfileChangeFailure(photoTask.getMsg());
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			progressDialog = Utils.showDialog(ChangePhotoActivity.this,
					getString(R.string.photo_upload));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}
		instance = this;

		Intent intent = new Intent(ChangePhotoActivity.this,
				PhotoAlbumActivity.class);
		intent.putExtra(PhotoAlbumActivity.EXTRA_IMAGE_CAPTURE, true);
		intent.putExtra(PhotoAlbumActivity.EXTRA_GO_PREVIOUS_PAGE, true);
		startActivityForResult(intent, CHANGE_RETURN_PHOTO);
	}

	private void onPhotoChangeBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
	}

	private void onPhotoChangeFailure(String msg) {
		Utils.dismissDialog(progressDialog);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		this.finish();
	}

	private void onPhotoChangeSuccess(User user) {
		Utils.dismissDialog(progressDialog);
		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	@Override
	public void setTaskListener() {
		mUploadTask.setListener(mUploadTaskListener);
	}

	public void UserProfileChangeFailure(String msg){
		Toast.makeText(this, msg,
				Toast.LENGTH_SHORT).show();
		ChangePhotoActivity.this.finish();
	}
}
