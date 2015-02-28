package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.ui.user.SignupProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

public class ChangeSignupPhotoActivity extends AbstractUploadPhotoActivity {

	public final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FileUploadTask photoTask = (FileUploadTask) task;
			Utils.dismissDialog(progressDialog);
			if (result == TaskResult.OK) {
				Toast.makeText(ChangeSignupPhotoActivity.this,
						R.string.save_success,
						Toast.LENGTH_LONG).show();
				String photo_name = photoTask.getFileInfo().fileName;
				uploadSuccess(photo_name);

			} else if (result == TaskResult.FAILED) {
				Toast.makeText(ChangeSignupPhotoActivity.this,
						photoTask.getMsg(),
						Toast.LENGTH_SHORT).show();
				ChangeSignupPhotoActivity.this.finish();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			progressDialog = Utils.showDialog(ChangeSignupPhotoActivity.this,
					getString(R.string.photo_upload));
		}
	};

	private static final int CHANGE_RETURN_PHOTO = 5678;

	public static String EXTRA_CHANGE_TASK = "EXTRA_CHANGE_TASK";

	private ProgressDialog progressDialog;
	private static ChangeSignupPhotoActivity instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		Intent intent = new Intent(ChangeSignupPhotoActivity.this,
				PhotoAlbumActivity.class);
		intent.putExtra(PhotoAlbumActivity.EXTRA_IMAGE_CAPTURE, true);
		intent.putExtra(PhotoAlbumActivity.EXTRA_GO_PREVIOUS_PAGE, true);
		startActivityForResult(intent, CHANGE_RETURN_PHOTO);
	}


	@Override
	public void setTaskListener() {
		mUploadTask.setListener(mUploadTaskListener);
	}

	private void uploadSuccess(String photoName){
		Utils.dismissDialog(progressDialog);
		Intent intent = getIntent();
		intent.putExtra(SignupProfileActivity.EXTRA_PHOTO_URL, photoName);
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}
}
