package com.ruptech.chinatalk.ui.story;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.PhotoLikeTask;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ImageProgressBar;
import com.ruptech.chinatalk.widget.UserStoryListCursorAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

public class UserStoryImageViewActivity extends ActionBarActivity {

	private UserPhoto mUserPhoto;

	@InjectView(R.id.image_progress_bar)
	ImageProgressBar imageProgressBar;
	@InjectView(R.id.item_story_imageview_imageView)
	ImageViewTouch imageView;
	@InjectView(R.id.item_story_imageview_location_textview)
	TextView locationTextView;
	@InjectView(R.id.item_story_image_reply_textview)
	TextView replyTextView;
	@InjectView(R.id.item_story_image_good_imageview)
	ImageView goodImageView;
	@InjectView(R.id.item_story_image_good_textview)
	TextView goodTextView;
	@InjectView(R.id.item_story_image_good_view)
	View goodView;

	private final String TAG = Utils.CATEGORY
			+ UserStoryImageViewActivity.class.getSimpleName();

	private final OnImageViewTouchSingleTapListener singleTapListener = new OnImageViewTouchSingleTapListener() {

		@Override
		public void onSingleTapConfirmed() {
			finish();
		}
	};

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UserPhoto userPhoto = (UserPhoto) intent.getExtras()
					.getSerializable(CommonUtilities.EXTRA_MESSAGE);
			if (userPhoto != null) {
				mUserPhoto.mergeFrom(userPhoto);
				refreshUI();
			}
		}
	};

	@InjectView(R.id.item_story_image_reply_view)
	View replyView;

	@InjectView(R.id.item_story_image_share_view)
	View shareView;

	private void extractExtras() {

		Bundle extras = getIntent().getExtras();
		mUserPhoto = (UserPhoto) extras
				.getSerializable(UserStoryCommentActivity.EXTRA_USER_PHOTO);

	}

	private int getContentViewRes() {
		return R.layout.activity_story_image_view;
	}

	private void goToStoryLocation(UserPhoto userPhoto) {
		Intent intent = new Intent(this, UserStoryListActivity.class);
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_LATE6,
				userPhoto.getLate6());
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_LNGE6,
				userPhoto.getLnge6());
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_ADDRESS,
				userPhoto.getAddress());
		startActivity(intent);
	}

	private void goToStoryTag(UserPhoto userPhoto) {
		Intent intent = new Intent(this, UserStoryListActivity.class);
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_TAG,
				userPhoto.getCategory());
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_AppCompat);
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		ButterKnife.inject(this);

		extractExtras();
		if (savedInstanceState != null) {
			mUserPhoto = (UserPhoto) savedInstanceState
					.getSerializable(UserStoryCommentActivity.EXTRA_USER_PHOTO);

		}
		setupComponents();
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				CommonUtilities.STORY_CONTENT_MESSAGE_ACTION));
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(UserStoryCommentActivity.EXTRA_USER_PHOTO,
				mUserPhoto);

	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		try {
			super.onTouchEvent(ev);
		} catch (IllegalArgumentException ex) {
		}
		return false;
	}
	private void refreshUI() {
		String url = App.readServerAppInfo().getServerOriginal(
				mUserPhoto.getPic_url());
		if (!Utils.isEmpty(url)) {
			if (!url.equals(imageView.getTag())) {
				ImageManager.imageLoader
						.displayImage(
								url,
								imageView,
								ImageManager.getImageOptionsPortrait(),
								ImageViewActivity
										.createImageLoadingListener(imageProgressBar),
								ImageViewActivity
										.createLoadingProgresListener(imageProgressBar));
				imageView.setTag(url);
			}
		}

		replyTextView.setText(String.valueOf(mUserPhoto.getComment()));

		int likeIconRes;
		if (mUserPhoto.getFavorite() > 0) {
			likeIconRes = R.drawable.ic_action_social_like_selected;
		} else {
			likeIconRes = R.drawable.ic_action_social_like_unselected;
		}
		goodImageView.setImageResource(likeIconRes);

		goodTextView.setText(String.valueOf(mUserPhoto.getGood()));
	}
	private void setupComponents() {
		getSupportActionBar().hide();

		imageView.setDisplayType(DisplayType.FIT_TO_SCREEN);
		imageView.setSingleTapListener(singleTapListener);

		replyView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(UserStoryImageViewActivity.this,
						UserStoryCommentActivity.class);
				intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
						mUserPhoto);

				startActivity(intent);
			}
		});

		goodView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.likeAnimation(goodImageView,
						(mUserPhoto.getFavorite() > 0));
				UserStoryCommentActivity.gotoLikePhoto(mUserPhoto,
						new TaskAdapter() {
							@Override
							public void onPostExecute(GenericTask task,
									TaskResult result) {
								PhotoLikeTask photoLikeTask = (PhotoLikeTask) task;
								if (result == TaskResult.FAILED) {
								} else {
									UserPhoto userPhoto = photoLikeTask
											.getUserPhoto();
									Utils.toastPhotoLikeResult(
											UserStoryImageViewActivity.this,
											userPhoto);
									if (userPhoto.getFavorite() == 0) {
										userPhoto.setGood(mUserPhoto.getGood() - 1);
									} else {
										userPhoto.setGood(mUserPhoto.getGood() + 1);
									}
									// 更新本地
									ContentValues v = new ContentValues();
									v.put(UserPhotoTable.Columns.GOOD,
											userPhoto.getGood());
									v.put(UserPhotoTable.Columns.COMMENT,
											userPhoto.getComment());
									v.put(UserPhotoTable.Columns.FAVORITE,
											userPhoto.getFavorite());
									App.userPhotoDAO.updateUserPhoto(
											userPhoto.getId(), v);
									CommonUtilities.broadcastStoryMessage(
											App.mContext, userPhoto);
								}
								goodView.setEnabled(true);
							}

							@Override
							public void onPreExecute(GenericTask task) {
								goodView.setEnabled(false);
							}

						});
			}
		});

		shareView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryListCursorAdapter.gotoSharePopup(v.getContext(),
						mUserPhoto);
			}
		});

		locationTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToStoryLocation(mUserPhoto);
			}

		});

		String address = mUserPhoto.getAddress();
		if (Utils.isEmpty(address)) {
			locationTextView.setVisibility(View.GONE);
		} else {
			locationTextView.setVisibility(View.VISIBLE);
			locationTextView.setText(address);
		}

		if (mUserPhoto.isOldAddress()) {
			AsyncTask<Void, Void, Void> mAddressTask;
			mAddressTask = new AsyncTask<Void, Void, Void>() {
				private String city;

				@Override
				protected Void doInBackground(Void... params) {
					city = Utils.getCity(UserStoryImageViewActivity.this,
							mUserPhoto.getLate6(), mUserPhoto.getLnge6());
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					if (!Utils.isEmpty(city)) {
						locationTextView.setVisibility(View.VISIBLE);
						locationTextView.setText(city);
						mUserPhoto.setAddress(city);
						mUserPhoto.setOldAddress(false);
					}
				}
			};
			mAddressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		refreshUI();

	}
}