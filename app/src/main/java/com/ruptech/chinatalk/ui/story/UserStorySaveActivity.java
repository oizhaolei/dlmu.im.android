package com.ruptech.chinatalk.ui.story;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.UserStoryReceiver;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.task.impl.UrlUploadTask;
import com.ruptech.chinatalk.task.impl.UserStoryNewTask;
import com.ruptech.chinatalk.ui.user.LanguageActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper.OnFaceOprateListener;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.EditTextWithFace;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class UserStorySaveActivity extends ActionBarActivity {
	public enum UploadStatus {
		Upload_Start, Upload_Success, Upload_Fail,
	}

	public static class UploadStoryData {
		public String fileName;
		public String photoUrl;
		public String comment;
		public long parentId;
		public String replyId;
		public String replyName;
		public String address;
		public int late6;
		public int lnge6;
	}

	private static final int ADD_TAG = 0;

	public static UploadStoryData uploadStoryData;

	public static final String ACTION_RE_UPLOAD = "com.ruptech.chinatalk.ACTION_RE_UPLOAD";
	public static final String ACTION_COMMENT_UPLOAD = "com.ruptech.chinatalk.ACTION_COMMENT_UPLOAD";

	public static final String EXTRA_ADDRESS = "address";
	public static final String EXTRA_LATE6 = "late6";
	public static final String EXTRA_LNGE6 = "lnge6";

	private final String TAG = Utils.CATEGORY
			+ UserStorySaveActivity.class.getSimpleName();
	@InjectView(R.id.activity_story_save_content_edittext)
	EditTextWithFace mContentEditText;
	@InjectView(R.id.activity_story_scrollview)
	ScrollView mScrollView;
	private File mFile;
	@InjectView(R.id.activity_story_save_pic_imageview)
	ImageView mPicImageView;
	@InjectView(R.id.activity_story_save_lang_imageview)
	ImageView langImageView;
	private String mPhotoUrl;
	private String mComment;
	private String mFileName;
	@InjectView(R.id.activity_story_save_tag_textview)
	TextView mTagTextView;
	@InjectView(R.id.activity_story_save_tag_view)
	View mTagView;
	@InjectView(R.id.activity_story_save_tag_tip_textview)
	View mTagTipView;

	private MyNotificationBuilder mBuilder;
	private int late6;
	private int lnge6;
	private String address;

	private boolean uploadError = false;

	private final UploadProgress uploadProgress = new UploadProgress() {
		@Override
		public void onUpload(long uploaded, long total) {
			// Call the onProgressUpdate method with the percent
			// completed
			publishProgress((int) ((uploaded / (float) total) * 100));
			Log.d(TAG, uploaded + " - " + total);
		}
	};

	private final TaskListener mUserStoryNewTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onSaveSuccess();
			} else {
				onSaveFailure();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	private FileUploadInfo newFileInfo;
	private final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FileUploadTask fileUploadTask = (FileUploadTask) task;
			if (result == TaskResult.OK) {
				newFileInfo = fileUploadTask.getFileInfo();
			} else {
				uploadError = true;
			}
			if (saveWaiting) {
				doSave();
			}
		}

	};

	private boolean saveWaiting;
	private ImageSpan bgSpan;

	static final int ADD_REPLY = 1;
	private MenuItem saveMenu;

	private GenericTask mUploadTask;

	private long mParentId;
	private String mReplyId;
	private String mReplyName;

	private SelectFaceHelper mFaceHelper;

	@InjectView(R.id.add_face_tool_layout)
	View mAddFaceToolView;

	@InjectView(R.id.activity_location_view)
	View mAddressView;

	OnFaceOprateListener mOnFaceOprateListener = new OnFaceOprateListener() {
		@Override
		public void onFaceDeleted() {
			int selection = mContentEditText.getSelectionStart();
			String text = mContentEditText.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					mContentEditText.getText().delete(start, end);
					return;
				}
				mContentEditText.getText().delete(selection - 1, selection);
			}
		}

		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				mContentEditText.append(spanEmojiStr);
			}
		}

	};

	private boolean isVisbilityFace = false;

	InputMethodManager mInputMethodManager;

	private String postContent = "";

	@InjectView(R.id.activity_story_save_location_textview)
	TextView mAddressTextView;

	private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateAddressView();
		}
	};

	@OnClick(R.id.activity_story_save_reply_imagebutton)
	public void doAddReply(View v) {
		Intent intent = new Intent(this, UserStoryReplyActivity.class);
		startActivityForResult(intent, ADD_REPLY);
	}

	@OnClick(R.id.activity_story_save_tag_tip_textview)
	public void doAddTagsByTip(View v) {
		Intent intent = new Intent(this, UserStoryTagActivity.class);
		startActivityForResult(intent, ADD_TAG);
	}

	private void doSave() {
		String text = mContentEditText.getText().toString().trim();
		if (!Utils.isEmpty(text)) {
			postContent = ParseEmojiMsgUtil.convertToMsg(
					mContentEditText.getText(), this);
		}
		if (uploadError) {
			onSaveFailure();
			return;
		}
		if (newFileInfo == null) {
			saveWaiting = true;
			return;
		}
		mInputMethodManager.hideSoftInputFromWindow(
				mContentEditText.getWindowToken(), 0);

		long replyId = 0;
		if (UserStoryCommentActivity.hasSpan(bgSpan, mContentEditText)
				&& !Utils.isEmpty(mReplyId)) {
			replyId = Long.valueOf(mReplyId);
		}

		if (mAddressTextView.getVisibility() == View.VISIBLE) {
			late6 = (Double
					.valueOf(MyLocation.recentLocation.getLatitude() * 1E6))
					.intValue();
			lnge6 = (Double
					.valueOf(MyLocation.recentLocation.getLongitude() * 1E6))
					.intValue();
		} else {
			late6 = 0;
			lnge6 = 0;
		}

		if (mAddressTextView.getVisibility() == View.VISIBLE)
			address = mAddressTextView.getText().toString();

		if (Utils.isEmpty(address)) {
			address = "";
		}

		GenericTask userStoryNewTask = new UserStoryNewTask(
				PrefUtils.getPrefPerferLang(), newFileInfo.fileName,
				newFileInfo.width, newFileInfo.height, postContent, mParentId,
				replyId, late6, lnge6, address,
				PrefUtils.getUserStoryPhotoTagCode());
		userStoryNewTask.setListener(mUserStoryNewTaskListener);
		userStoryNewTask.execute();
	}

	private void doUpload(File mPhotoFile) {
		mUploadTask = new FileUploadTask(mPhotoFile,
				AppPreferences.MESSAGE_TYPE_NAME_PHOTO, uploadProgress);
		try {
			boolean wifiAvailible = Utils.isWifiAvailible(this);
			mPhotoFile = ImageManager.compressImage(mPhotoFile, 75, this,
					wifiAvailible);

		} catch (IOException e) {
			Utils.sendClientException(e);
		}

		mUploadTask.setListener(mUploadTaskListener);
		mUploadTask.execute();
	}

	private void doUrlUpload(String ulr) {
		mUploadTask = new UrlUploadTask(mPhotoUrl);
		mUploadTask.setListener(mUploadTaskListener);
		mUploadTask.execute();
	}

	private void finishSuccessful() {
		setResult(Activity.RESULT_OK);

		finish();
	}

	protected void gotoChangeLangActivity() {
		User user = App.readUser();
		if (user == null)
			return;
		Intent intent = new Intent(this, LanguageActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		startActivity(intent);
	}

	@OnClick(R.id.activity_story_save_lang_imageview)
	public void gotoSelectLang(View v) {
		final List<String> allLangs = App.readUser().getAllLangs();
		allLangs.add(getString(R.string.other_language));
		final String[] menus = new String[allLangs.size()];
		final String[] langCode = new String[allLangs.size()];
		for (int i = 0; i < allLangs.size(); i++) {
			if (i < allLangs.size() - 1) {
				menus[i] = Utils.getLangDisplayName(allLangs.get(i));
				langCode[i] = allLangs.get(i);
			} else {
				menus[i] = allLangs.get(i);
			}
		}

		CustomDialog alertDialog = new CustomDialog(this).setTitle(
				getString(R.string.language)).setItems(menus,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == menus.length - 1) {
							gotoChangeLangActivity();
						} else {
							String selectLang = langCode[which];
							setContentEditTextHint(selectLang);
							langImageView.setImageResource(Utils
									.getLanguageFlag(selectLang));
							PrefUtils.savePrefPreferLang(selectLang);
						}
					}
				});
		alertDialog.show();
	}

	// 隐藏软键盘
	private void hideInputManager(Context ct) {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(((Activity) ct).getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			Log.e(TAG, "hideInputManager Catch error,skip it!", e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			if (requestCode == ADD_REPLY) {
				mReplyId = extras
						.getString(UserStoryReplyActivity.EXTRA_REPLY_ID);
				mReplyName = extras
						.getString(UserStoryReplyActivity.EXTRA_REPLY_NAME);

				showComment();
			} else if (requestCode == ADD_TAG) {
				String channelStr = extras
						.getString(UserStoryTagActivity.EXTRA_TAG);
				PrefUtils.saveUserStoryPhotoTagCode(channelStr);

				mTagTipView.setVisibility(View.GONE);
				mTagView.setVisibility(View.VISIBLE);
				mTagTextView.setVisibility(View.VISIBLE);
				mTagTextView.setText(channelStr);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (isVisbilityFace) {
			isVisbilityFace = false;
			mAddFaceToolView.setVisibility(View.GONE);
			return;
		}

		if (mUploadTask != null
				&& mUploadTask.getStatus() == GenericTask.Status.RUNNING) {
			mUploadTask.cancel(true);
		}
		App.taskManager.cancelAll();

		Utils.onBackPressed(this);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (App.readUser() == null) {
			finish();
		}
		parseExtras();
		if ((mFile == null || !mFile.exists()) && mPhotoUrl == null) {
			Toast.makeText(this, R.string.image_file_is_not_exist,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		setContentView(R.layout.activity_story_save);
		ButterKnife.inject(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.post_popular);

		// PrefUtils.removeUserStoryPhotoTagCode();

		setupComponents();

		if (mFile != null) {
			doUpload(mFile);
		} else if (mPhotoUrl == null) {
			doUrlUpload(mPhotoUrl);
		} else {
			this.finish();
		}

		registerReceiver(mLocationReceiver, new IntentFilter(
				CommonUtilities.ADDRESS_UPDATE_ACTION));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				Utils.getPostStep(3, 3, R.string.send));

		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(mLocationReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		} else if (item.getItemId() == saveMenu.getItemId()) {
			saveMenu.setEnabled(false);
			// 关闭页面
			finishSuccessful();
			// 立刻通知 图片发送中
			mBuilder = MessageReceiver.createNotificationBuilder(this,
					getString(R.string.app_name),
					getString(R.string.picture_sending), null, false);
			mBuilder.setAutoCancel(true);
			mBuilder.setShowSetting(false);
			mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
			mBuilder.setProgress(100, 0, false);
			mBuilder.setTicker(getString(R.string.picture_sending));
			UserStoryReceiver.sendStoryPhotoNotice(this, mBuilder,
					UploadStatus.Upload_Start);

			doSave();
		}
		return true;
	}

	private void onSaveFailure() {

		UserStoryReceiver.sendStoryPhotoNotice(this, mBuilder,
				UploadStatus.Upload_Fail);

		uploadStoryData = new UploadStoryData();
		uploadStoryData.comment = postContent;
		uploadStoryData.fileName = mFileName;
		uploadStoryData.photoUrl = this.mPhotoUrl;
		uploadStoryData.parentId = this.mParentId;
		uploadStoryData.replyId = this.mReplyId;
		uploadStoryData.replyName = this.mReplyName;
		uploadStoryData.address = this.address;
		uploadStoryData.late6 = this.late6;
		uploadStoryData.lnge6 = this.lnge6;
	}

	private void onSaveSuccess() {

		if (mBuilder != null) {
			UserStoryReceiver.sendStoryPhotoNotice(this, mBuilder,
					UploadStatus.Upload_Success);
		}

		uploadStoryData = null;
		mAddFaceToolView.setVisibility(View.GONE);
		if (mFile.exists())
			mFile.delete();

	}

	private void parseExtras() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		if (ACTION_RE_UPLOAD.equals(action) && uploadStoryData != null) {
			mFileName = uploadStoryData.fileName;
			mPhotoUrl = uploadStoryData.photoUrl;
			mComment = uploadStoryData.comment;
			mParentId = uploadStoryData.parentId;
			mReplyId = uploadStoryData.replyId;
			mReplyName = uploadStoryData.replyName;
			address = uploadStoryData.address;
			late6 = uploadStoryData.late6;
			lnge6 = uploadStoryData.lnge6;
			if (!Utils.isEmpty(mReplyName)) {
				mComment = mComment.replaceAll(mReplyName, "");
				mReplyName = mReplyName.replaceAll("@", "");
			}

			if (mFileName != null) {
				mFile = new File(mFileName);
			}
			return;
		}

		if (extras != null) {
			if (ACTION_COMMENT_UPLOAD.equals(action)) {
				mParentId = extras
						.getLong(UserStoryCommentActivity.EXTRA_PARENT_ID);
				mReplyId = extras
						.getString(UserStoryCommentActivity.EXTRA_REPLY_ID);
				mReplyName = extras
						.getString(UserStoryCommentActivity.EXTRA_REPLY_NAME);
				mComment = extras
						.getString(UserStoryCommentActivity.EXTRA_COMMENT);
			}

			mFileName = extras.getString(PhotoAlbumActivity.EXTRA_FILE);
		}

		if (mFileName != null) {
			mFile = new File(mFileName);
		}
	}

	private void publishProgress(int percent) {
		if (mBuilder != null) {
			mBuilder.setProgress(100, percent, false);
			mBuilder.setVibrate(null);
			App.notificationManager.notify(R.drawable.ic_tttalk_gray_light,
					mBuilder.build());
		}
	}

	private void selectMessageFace() {
		if (null == mFaceHelper) {
			mFaceHelper = new SelectFaceHelper(this, mAddFaceToolView);
			mFaceHelper.setFaceOpreateListener(mOnFaceOprateListener);
		}
		if (isVisbilityFace) {
			isVisbilityFace = false;
			mAddFaceToolView.setVisibility(View.GONE);
		} else {
			isVisbilityFace = true;
			mAddFaceToolView.setVisibility(View.VISIBLE);
			hideInputManager(this);
		}
	}

	private void setContentEditTextHint(String lang) {
		String userLangName = Utils.getLangDisplayName(lang);
		mContentEditText.setHint(getString(R.string.hint_input_content,
				userLangName));
	}

	private void setupComponents() {
		langImageView.setImageResource(Utils.getLanguageFlag(PrefUtils
				.getPrefPerferLang()));
		String path = "file://" + mFile;
		if (mFile == null && mPhotoUrl != null)
			path = mPhotoUrl;

		if (!Utils.isEmpty(path)) {
			ImageManager.imageLoader.displayImage(path, mPicImageView,
					ImageManager.getOptionsLandscape(), null);
		}

		setContentEditTextHint(PrefUtils.getPrefPerferLang());
		if (mComment != null)
			mContentEditText.setText(mComment);
		mContentEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mContentEditText.getGlobalVisibleRect(rect);
					rect.left = rect.right - 100;
					if (rect.contains(eventX, eventY)) {
						selectMessageFace();
						return true;
					} else {
						isVisbilityFace = false;
						// mMessageListView.setSelection(mMessageListView
						// .getCount() - 1);
						mAddFaceToolView.setVisibility(View.GONE);
						return false;
					}
				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mContentEditText.getGlobalVisibleRect(rect);
					rect.left = rect.right - 100;
					if (rect.contains(eventX, eventY)) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		});
		InputFilter[] filters = {new Utils.LengthFilter()};
		mContentEditText.setFilters(filters);

		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mScrollView.post(new Runnable() {
							@Override
							public void run() {
								mScrollView.scrollTo(0,
										mScrollView.getChildAt(0).getHeight());
							}
						});
					}
				});

		if (!Utils.isEmpty(mReplyId)) {
			showComment();
		}

		if (MyLocation.address == null) {
			mAddressView.setVisibility(View.GONE);
			mAddressTextView.setVisibility(View.INVISIBLE);
		} else {
			mAddressView.setVisibility(View.VISIBLE);
			mAddressTextView.setVisibility(View.VISIBLE);

			mAddressTextView.setText(MyLocation.address);
			mAddressTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mAddressTextView.setVisibility(View.GONE);
				}
			});

		}

		String mCategoryTagCode = PrefUtils.getUserStoryPhotoTagCode();
		if (!Utils.isEmpty(mCategoryTagCode)) {
			mTagView.setVisibility(View.VISIBLE);
			mTagTextView.setVisibility(View.VISIBLE);
			mTagTextView.setText(Utils.getStoryTagNameByCode(mCategoryTagCode));
			mTagTipView.setVisibility(View.GONE);
		} else {
			mTagView.setVisibility(View.GONE);
			mTagTipView.setVisibility(View.VISIBLE);
		}

		String mChannelTitle = "";
		if (getIntent().getExtras() != null) {
			mChannelTitle = getIntent().getExtras().getString(
					UserStoryTagActivity.EXTRA_TAG);
		}
		if (!Utils.isEmpty(mChannelTitle)) {
			mTagView.setVisibility(View.VISIBLE);
			mTagTextView.setVisibility(View.VISIBLE);
			mTagTextView.setText(mChannelTitle);
			mTagTipView.setVisibility(View.GONE);
			PrefUtils.saveUserStoryPhotoTagCode(mChannelTitle);
		}

		mTagTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Utils.isEmpty(mTagTextView.getText().toString())) {
					mTagTextView.setVisibility(View.GONE);
					mTagTextView.setText("");
					mTagView.setVisibility(View.GONE);
					mTagTipView.setVisibility(View.VISIBLE);
					PrefUtils.removeUserStoryPhotoTagCode();
				}
			}

		});
	}

	private void showComment() {

		TextPaint tp = new TextPaint();
		tp.setTextSize(mContentEditText.getTextSize());
		tp.setTypeface(mContentEditText.getTypeface());
		mReplyName = (String) TextUtils.ellipsize(mReplyName, tp, 400,
				TextUtils.TruncateAt.MIDDLE);

		mReplyName = String.format("%s%s",
				UserStoryCommentActivity.TAG_PATTERN, mReplyName);
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		TextView tv = UserStoryCommentActivity.createContactTextView(
				mReplyName, this, mContentEditText);
		BitmapDrawable bd = (BitmapDrawable) UserStoryCommentActivity
				.convertViewToDrawable(tv);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());

		String suffix = ParseEmojiMsgUtil.convertToMsg(
				mContentEditText.getText(), this);
		String prefix;
		if (UserStoryCommentActivity.hasSpan(bgSpan, mContentEditText)) {
			int start = mContentEditText.getText().getSpanStart(bgSpan);
			int end = mContentEditText.getText().getSpanEnd(bgSpan);
			prefix = suffix.substring(0, start).trim();
			suffix = suffix.substring(end).trim();

			sb.append(prefix);
		}

		if (!suffix.startsWith(UserStoryCommentActivity.SPLIT_PATTERN))
			suffix = String.format("%s %s",
					UserStoryCommentActivity.SPLIT_PATTERN, suffix);

		sb.append(mReplyName);

		int startIndex = sb.length() - mReplyName.length();
		int endIndex = sb.length();
		bgSpan = new ImageSpan(bd);
		sb.setSpan(bgSpan, startIndex, endIndex,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sb.append(suffix);
		mContentEditText.setText(sb);
		mContentEditText.setSelection(sb.length());
	}

	private void updateAddressView() {
		if (MyLocation.address != null) {
			mAddressTextView.setText(MyLocation.address);
			if (mAddressView.getVisibility() == View.GONE) {
				mAddressTextView.setVisibility(View.VISIBLE);
				mAddressTextView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mAddressTextView.setVisibility(View.GONE);
					}
				});
			}
			mAddressView.setVisibility(View.VISIBLE);
		}
	}
}
