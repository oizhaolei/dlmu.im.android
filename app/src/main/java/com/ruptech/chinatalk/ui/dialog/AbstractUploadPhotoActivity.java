package com.ruptech.chinatalk.ui.dialog;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public abstract class AbstractUploadPhotoActivity extends Activity {

	protected GenericTask mUploadTask;

	private final String TAG = Utils.CATEGORY
			+ AbstractUploadPhotoActivity.class.getSimpleName();

	protected static final int CHANGE_RETURN_PHOTO = 5678;

	public static String EXTRA_CHANGE_TASK = "EXTRA_CHANGE_TASK";

	private File mPhotoFile;
	protected ProgressDialog progressDialog;

	private void btn_send_user() {
		if (mPhotoFile != null) {
			try {
				boolean wifiAvailible = Utils.isWifiAvailible(this);
				mPhotoFile = ImageManager.compressImage(mPhotoFile, 75, this, wifiAvailible);

				doUpload(mPhotoFile);

			} catch (Exception e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
			}
		} else {
			Toast.makeText(this, R.string.user_infomation_is_invalidate,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Rect dialogBounds = new Rect();
		getWindow().getDecorView().getHitRect(dialogBounds);

		if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN)
				return false;
		}
		return super.dispatchTouchEvent(ev);
	}

	protected void doUpload(File photo) {
		if (mUploadTask != null
				&& mUploadTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mUploadTask = new FileUploadTask(photo,
				AppPreferences.MESSAGE_TYPE_NAME_PHOTO, new UploadProgress() {
			@Override
			public void onUpload(long uploaded, long total) {
				Log.d(TAG, uploaded + " - " + total);
			}
		});
		setTaskListener();
		mUploadTask.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == CHANGE_RETURN_PHOTO) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Uri mImageUri = (Uri) extras
							.get(PhotoAlbumActivity.RETURN_IMAGE_URI);
					if (mImageUri != null) {
						if ("content".equals(mImageUri.getScheme())) {
							String file_path = ImageManager.getRealPathFromURI(
									this, mImageUri);
							mPhotoFile = new File(file_path);
						} else {
							mPhotoFile = new File(mImageUri.getPath());
						}
						btn_send_user();
					}
				} else {
					Toast.makeText(this,
							getString(R.string.uplaod_photo_failure),
							Toast.LENGTH_SHORT).show();
					this.finish();
				}
			}
		} else {
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		if (mUploadTask != null
				&& mUploadTask.getStatus() == GenericTask.Status.RUNNING) {
			mUploadTask.cancel(true);
		}
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			EXTRA_CHANGE_TASK = (String) extras
					.get(ChangePhotoActivity.EXTRA_CHANGE_TASK);
		}
	}

	protected void setTaskListener() {

	}
}
