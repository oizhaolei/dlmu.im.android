package com.ruptech.chinatalk.ui;

import java.io.File;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.FileHelper;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.Gallery;
import com.ruptech.chinatalk.widget.ImagePagerAdapter;
import com.ruptech.chinatalk.widget.ImageProgressBar;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

public class ImageViewActivity extends ActionBarActivity {
	private class ImageOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(int position) {

		}

		@Override
		public void onPageSelected(int position) {
			pagerPosition = position;
		}

	}

	private static ImageViewActivity instance = null;

	public static final String EXTRA_IMAGE_URLS = "EXTRA_IMAGE_URLS";

	public static final String EXTRA_POSITION = "POSITION";

	public static SimpleImageLoadingListener createImageLoadingListener(
			final ImageProgressBar imageProgressBar) {
		return new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				imageProgressBar.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				switch (failReason.getType()) {
				case IO_ERROR:
					break;
				case DECODING_ERROR:
					break;
				case NETWORK_DENIED:
					break;
				case OUT_OF_MEMORY:
					break;
				case UNKNOWN:
					break;
				}
				imageProgressBar.setVisibility(View.GONE);
				view.setTag(null);
			}

			@Override
			public void onLoadingStarted(String imageUri, View view) {
			}
		};
	}

	public static SimpleImageLoadingListener createImageLoadingListenerWithResize(
			final ImageProgressBar imageProgressBar, final int width,
			final int height, final int portraitLeftRightMargin,
			final float maxRate) {

		final int INIT_HEIGHT = 300;

		return new SimpleImageLoadingListener() {
			public void initLayout(View view) {
				RelativeLayout.LayoutParams imaegViewParams = (RelativeLayout.LayoutParams) view
						.getLayoutParams();
				if (width > 0 && height > 0) {
					resizeView(view, width, height);
				} else {
					imaegViewParams.height = Gallery.dip2px(view.getContext(),
							INIT_HEIGHT);
					imaegViewParams.setMargins(0, 0, 0, 0);
				}

			}

			@Override
			public void onLoadingComplete(String imageUri, final View view,
					final Bitmap loadedImage) {
				imageProgressBar.setVisibility(View.GONE);

				int imageViewWidth = view.getMeasuredWidth();
				if (imageViewWidth > 0) {
					resizeView(view, loadedImage.getWidth(),
							loadedImage.getHeight());
				} else {
					view.post(new Runnable() {
						@Override
						public void run() {
							resizeView(view, loadedImage.getWidth(),
									loadedImage.getHeight());
						}
					});
				}

			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				switch (failReason.getType()) {
				case IO_ERROR:
					break;
				case DECODING_ERROR:
					break;
				case NETWORK_DENIED:
					break;
				case OUT_OF_MEMORY:
					break;
				case UNKNOWN:
					break;
				}
				imageProgressBar.setVisibility(View.GONE);
				initLayout(view);
				view.setTag(null);
			}

			@Override
			public void onLoadingStarted(String imageUri, View view) {
				initLayout(view);
			}

			public void resizeView(View imageView, int width, int height) {
				int imageViewWidth = imageView.getMeasuredWidth();
				if (imageViewWidth <= 0) {
					imageViewWidth = App.displayWidth;
				}
				int imageViewHeight = height * imageViewWidth / width;
				int maxHeight = (int) (imageViewWidth * maxRate);
				if (imageViewHeight > maxHeight)
					imageViewHeight = maxHeight;

				RelativeLayout.LayoutParams imageViewParams = (RelativeLayout.LayoutParams) imageView
						.getLayoutParams();
				// imaegViewParams.width = imageViewWidth;
				imageViewParams.height = imageViewHeight;
				int margin = Gallery.dip2px(imageView.getContext(),
						portraitLeftRightMargin);
				if (imageViewHeight > imageViewWidth) {
					imageViewParams.setMargins(margin, 0, margin, 0);
				} else {
					imageViewParams.setMargins(0, 0, 0, 0);
				}
			}
		};
	}

	public static ImageLoadingProgressListener createLoadingProgresListener(
			final ImageProgressBar imageProgressBar) {
		return new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view,
					int current, int total) {
				int round = Math.round(100.0f * current / total);
				if (0 <= round && round < 100) {
					imageProgressBar.setVisibility(View.VISIBLE);
				} else {
					imageProgressBar.setVisibility(View.GONE);
				}
				imageProgressBar.setProgress(round);
			}
		};
	}

	private static void sendSavePhotoNotice(Context context, String filePath) {

		final int iconRes = R.drawable.ic_tttalk_gray_light;
		long when = System.currentTimeMillis();
		NotificationCompat.Builder mBuilder = new MyNotificationBuilder(context)
				.setSmallIcon(iconRes)
				.setLargeIcon(
						BitmapFactory.decodeResource(context.getResources(),
								R.drawable.ic_launcher))
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(
						context.getString(R.string.photo_save_path)
								+ FileHelper.getPublicPath(context))
				.setShowSetting(false);
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
		notificationIntent.setDataAndType(Uri.parse("file://" + filePath),
				"image/*");

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setWhen(when);
		mBuilder.setAutoCancel(true);
		mBuilder.setTicker(context.getString(R.string.photo_save_path)
				+ FileHelper.getPublicPath(context));//
		mBuilder.setContentIntent(resultPendingIntent);
		App.notificationManager.notify(iconRes, mBuilder.build());
		// close after 60 seconds
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				App.notificationManager.cancel(iconRes);
			}
		}, 1000 * 60);
	}

	@InjectView(R.id.activity_image_pager)
	ViewPager imagePager;
	private ImagePagerAdapter imagePagerAdapter;

	@InjectView(R.id.image_progress_bar)
	ImageProgressBar imageProgressBar;
	File saveTo;

	ArrayList<String> imageUrlList = new ArrayList<String>();

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			boolean result = (Boolean) msg.obj;
			if (result) {
				sendSavePhotoNotice(ImageViewActivity.this, saveTo.getPath());
				App.mImageManager.scanPhotos(saveTo.getPath());
			} else {
				Toast.makeText(ImageViewActivity.this, R.string.save_failure,
						Toast.LENGTH_SHORT).show();
			}
		};
	};

	private int pagerPosition;

	private final String TAG = Utils.CATEGORY
			+ ImageViewActivity.class.getSimpleName();

	private Drawable mActionBarBackgroundDrawable;

	public void btn_back(View v) { // 标题栏 返回按钮
		this.finish();
	}

	public void doSavePhoto(MenuItem item) {
		String url = imageUrlList.get(pagerPosition);
		saveAction(url, this);
	}

	private int getContentViewRes() {
		return R.layout.activity_image_pager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_AppCompat);
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		ButterKnife.inject(this);
		instance = this;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.picture);
		parseExtras(getIntent().getExtras());
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.photo_save_actions, mMenu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	protected void parseExtras(Bundle extras) {
		this.pagerPosition = extras.getInt(EXTRA_POSITION);
		this.imageUrlList = extras.getStringArrayList(EXTRA_IMAGE_URLS);
	}

	private void saveAction(final String downloadUrl, final Context context) {
		Toast.makeText(this, getString(R.string.data_saving),
				Toast.LENGTH_SHORT).show();
		new Thread() {
			@Override
			public void run() {
				saveTo = new File(FileHelper.getPublicPath(context),
						System.currentTimeMillis() + ".jpg");
				boolean result = Utils.saveFileFromServer(downloadUrl, saveTo);
				Message msg = new Message();
				msg.obj = result;
				mHandler.sendMessage(msg);
			}

		}.start();
	}

	private void setupComponents() {
		mActionBarBackgroundDrawable = getResources().getDrawable(
				R.color.action_bar_background_comment);
		mActionBarBackgroundDrawable.setAlpha(40);
		getSupportActionBar().setBackgroundDrawable(
				mActionBarBackgroundDrawable);

		imagePagerAdapter = new ImagePagerAdapter(this, imageProgressBar);
		imagePagerAdapter.setImageUrlList(imageUrlList);

		imagePager.setAdapter(imagePagerAdapter);
		imagePager.setCurrentItem(pagerPosition);
		imagePager.setOnPageChangeListener(new ImageOnPageChangeListener());

	}
}