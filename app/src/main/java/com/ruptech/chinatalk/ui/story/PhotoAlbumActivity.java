package com.ruptech.chinatalk.ui.story;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveAlbumTask;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.FileHelper;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.AlbumListAdapter;

public class PhotoAlbumActivity extends ActionBarActivity {
	static final int EXTRA_ACTIVITY_RESULT_SEND_STORY = 1012;

	public static final String EXTRA_GO_PREVIOUS_PAGE = "EXTRA_GO_PREVIOUS_PAGE";
	public static final String EXTRA_FILE = "EXTRA_FILE";
	public static final String EXTRA_IMAGE_CAPTURE = "EXTRA_IMAGE_CAPTURE";
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	public static final int IMAGE_COUNT_PER_PAGE = 100;

	private String lastImageId;

	public static final String RETURN_IMAGE_URI = "RETURN_IMAGE_URI";

	private Uri captureUri;

	private static File mNewImageFile;

	private ContentResolver cr;

	private boolean mGoPreviousPage;

	@InjectView(R.id.activity_photo_album_emptyview_text)
	TextView mEmptyTextView;
	@InjectView(R.id.activity_photo_album_gridview)
	GridView mGridView;
	@InjectView(R.id.image_progress_bar)
	ProgressBar progressBar;
	private RetrieveAlbumTask albumTask;
	private AlbumListAdapter mPhotoGridViewAdapter;

	private final String TAG = Utils.CATEGORY
			+ PhotoAlbumActivity.class.getSimpleName();

	protected final TaskListener mAlbumTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveAlbumTask retrieveAlbumTask = (RetrieveAlbumTask) task;
			lastImageId = retrieveAlbumTask.getLastImageId();
			if (result == TaskResult.OK) {
				if (mPhotoGridViewAdapter.getCount() == 0) {
					Map<String, String> mPhoto = new HashMap<>();
					mPhoto.put("path", "");
					mPhotoGridViewAdapter.add(mPhoto);
				}
				List<Map<String, String>> mPhotoList = retrieveAlbumTask
						.getPhotoList();
				mPhotoGridViewAdapter.addAll(mPhotoList);
				if (mPhotoList.size() < IMAGE_COUNT_PER_PAGE) {
					mGridView.setOnScrollListener(null);
				}
			} else {

			}

			progressBar.setVisibility(View.GONE);
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private String _getPhotoFilename() {
		return DateCommonUtils.dateFormat(new Date(),
				DateCommonUtils.DF_yyyyMMddHHmmssSSS2) + ".jpg";
	}

	private void gotoAction(String path) {
		String file_path;
		if (!Utils.isEmpty(path)) {
			file_path = path;
			try {
				File mImageFile = new File(file_path);
				captureUri = Uri.fromFile(mImageFile);
			} catch (Exception e) {
				Utils.sendClientException(e);
			}
		} else {
			if ("content".equals(captureUri.getScheme())) {
				file_path = ImageManager.getRealPathFromURI(this, captureUri);
			} else {
				file_path = captureUri.getPath();
			}
		}

		if (mGoPreviousPage) {
			Intent intent = new Intent();
			intent.putExtra(RETURN_IMAGE_URI, captureUri);
			setResult(Activity.RESULT_OK, intent);
			this.finish();
		} else {
			gotoStoryImageCropActivity(file_path);
		}
	}

	private void gotoStoryImageCropActivity(String imageFile) {

		Intent intent = new Intent(this, UserStoryImageCropActivity.class);
		if (UserStorySaveActivity.ACTION_COMMENT_UPLOAD.equals(this.getIntent()
				.getAction())) {
			intent.setAction(UserStorySaveActivity.ACTION_COMMENT_UPLOAD);
			intent.putExtras(this.getIntent());
		}
		if (getIntent().getExtras() != null) {
			intent.putExtra(UserStoryTagActivity.EXTRA_TAG, getIntent()
					.getExtras().getString(UserStoryTagActivity.EXTRA_TAG));
		}
		intent.putExtra(EXTRA_FILE, imageFile);
		startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_SEND_STORY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_IMAGE_CAPTURE) {
				if (captureUri != null) {
					gotoAction(null);
				} else {
					if (mNewImageFile != null) {
						gotoAction(mNewImageFile.getPath());
					} else {
						this.finish();
					}
				}
			} else if (requestCode == EXTRA_ACTIVITY_RESULT_SEND_STORY) {
				setResult(Activity.RESULT_OK);
				this.finish();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_album);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.album);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mGoPreviousPage = extras.getBoolean(EXTRA_GO_PREVIOUS_PAGE, false);
			extras.getBoolean(EXTRA_IMAGE_CAPTURE, false);
		}
		cr = getContentResolver();
		setupComponents();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				showMoreImage();
			}
		}, 1000);
	}

	@Override
	protected void onDestroy() {
		mPhotoGridViewAdapter.removeCallback();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	// 调用系统相机
	private void openImageCapture() {
		try {
			String filename = _getPhotoFilename();
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Photo filename=" + filename);
			mNewImageFile = new File(FileHelper.getPublicPath(this), filename);
			captureUri = Uri.fromFile(mNewImageFile);
			if (Utils.isAvailableIntent(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
				startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
			} else {
				Toast.makeText(this, "IMAGE_CAPTURE is not available",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, e.getMessage(), e);
			Utils.sendClientException(e);
		}
	}

	private void setupAdapter() {

		mPhotoGridViewAdapter = new AlbumListAdapter(this, 0);

		mGridView.setAdapter(mPhotoGridViewAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					mPhotoGridViewAdapter.releaseCamera();
					openImageCapture();
				} else {
					String path = mPhotoGridViewAdapter.getItem(position).get(
							"path");
					if (!Utils.isEmpty(path)) {// 防止NullPointerException
						gotoAction(path);
					}
				}
			}
		});

		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == view.getCount() - 1
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 加载数据代码
					showMoreImage();
				}
			}

		});
	}

	private void setupComponents() {
		progressBar.setVisibility(View.VISIBLE);

		mGridView.setEmptyView(mEmptyTextView);
		setupAdapter();
	}

	private void showMoreImage() {
		if (albumTask != null
				&& albumTask.getStatus() == GenericTask.Status.RUNNING)
			return;

		albumTask = new RetrieveAlbumTask(cr, lastImageId);
		albumTask.setListener(mAlbumTaskListener);
		albumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

}