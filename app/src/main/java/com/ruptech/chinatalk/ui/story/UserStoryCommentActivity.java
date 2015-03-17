package com.ruptech.chinatalk.ui.story;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.PresentDonateReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.UserStoryReceiver;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.PhotoLikeTask;
import com.ruptech.chinatalk.task.impl.RequestAutoTranslatePhotoTask;
import com.ruptech.chinatalk.task.impl.RetrieveStoryLikeListTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserGiftListTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoListTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.task.impl.UserPhotoRemoveTask;
import com.ruptech.chinatalk.task.impl.UserStoryNewTask;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.gift.GiftDonateActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper.OnFaceOperateListener;
import com.ruptech.chinatalk.widget.EditTextWithFace;
import com.ruptech.chinatalk.widget.Gallery;
import com.ruptech.chinatalk.widget.Gallery.OnGalleryItemClickListener;
import com.ruptech.chinatalk.widget.ImageProgressBar;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.UserStoryCommentListArrayAdapter;
import com.ruptech.chinatalk.widget.UserStoryCommentListArrayAdapter.ViewHolder;
import com.ruptech.chinatalk.widget.UserStoryListCursorAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static butterknife.ButterKnife.findById;
import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

public class UserStoryCommentActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {
	public static final String EXTRA_COMMENT = "EXTRA_COMMENT";

	// 从我的评论进入的，获得的userphoto不是真正的userPhoto，它是由 父pic_url + 我评论内容等组成
	public static final String EXTRA_IS_REAL_USERPHOTO = "IS_REAL_USERPHOTO";

	public static final String EXTRA_PARENT_ID = "EXTRA_PARENT_ID";

	public static final String EXTRA_REPLY_ID = "EXTRA_REPLY_ID";

	public static final String EXTRA_REPLY_NAME = "EXTRA_REPLY_NAME";

	public static final String EXTRA_USER_PHOTO = "USER_PHOTO";

	public static final String EXTRA_IS_NOTIFICATION = "EXTRA_IS_NOTIFICATION";

	public static final String EXTRA_IS_REAL_COMMENT_USERPHOTO = "IS_REAL_COMMENT_USERPHOTO";

	private static UserStoryCommentActivity instance;

	private static UserStoryCommentListArrayAdapter mUserStoryCommentListArrayAdapter;

	public static final String SPLIT_PATTERN = ",";

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	public static Object convertViewToDrawable(View view) {
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(),
				view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);

	}

	protected static TextView createContactTextView(String text,
	                                                Context context, EditText editText) {
		// creating textview dynamically
		TextView tv = new TextView(context);
		tv.setText(text);
		tv.setPadding(2, 2, 2, 2);
		tv.setTextColor(context.getResources().getColor(R.color.reply_name));
		tv.setTextSize(editText.getTextSize());
		tv.setBackgroundResource(R.color.at_block);
		// tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		return tv;
	}

	public static void gotoDeletePhoto(final UserPhoto userPhoto,
	                                   Context context, final TaskListener deleteListener) {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				GenericTask mUserPhotoRemoveTask = new UserPhotoRemoveTask(
						userPhoto.getId());

				mUserPhotoRemoveTask.setListener(deleteListener);
				mUserPhotoRemoveTask
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(context, positiveListener, negativeListener,
				context.getString(R.string.delete_selected),
				context.getString(R.string.are_you_sure_delete_message));
	}

	public static void gotoLikePhoto(UserPhoto userPhoto,
	                                 TaskListener likeListener) {
		PhotoLikeTask photoLikeTask = new PhotoLikeTask(userPhoto.getId(),
				userPhoto.getFavorite() == 0);
		photoLikeTask.setListener(likeListener);
		photoLikeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static void gotoProfileAcitivity(long userid, Context context) {
		User user = App.userDAO.fetchUser(userid);
		Intent intent = new Intent(context, FriendProfileActivity.class);
		if (user == null) {
			intent.putExtra(ProfileActivity.EXTRA_USER_ID, userid);
		} else {
			intent.putExtra(ProfileActivity.EXTRA_USER, user);
		}
		context.startActivity(intent);
	}

	public static void gotoProfileAcitivity(UserPhoto userPhoto, Context context) {
		gotoProfileAcitivity(userPhoto.getUserid(), context);
	}

	protected static boolean hasSpan(ImageSpan span, EditText editText) {
		if (span != null && editText.getText().getSpanEnd(span) >= 0) {
			return true;
		} else {
			return false;
		}

	}

	protected static void requestAutotranslate(UserPhoto userPhoto,
	                                           String lang,
	                                           final TaskListener requestAutoTranslatePhotoTaskListener) {
		RequestAutoTranslatePhotoTask requestAutoTranslatePhotoTask = new RequestAutoTranslatePhotoTask(
				userPhoto.getId(), lang);
		requestAutoTranslatePhotoTask
				.setListener(requestAutoTranslatePhotoTaskListener);

		requestAutoTranslatePhotoTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private final String TAG = Utils.CATEGORY
			+ UserStoryCommentActivity.class.getSimpleName();

	public static final String TAG_PATTERN = "@";

	OnFaceOperateListener mOnFaceOperateListener = new OnFaceOperateListener() {
		@Override
		public void onFaceDeleted() {
			int selection = mCommentEditText.getSelectionStart();
			String text = mCommentEditText.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					mCommentEditText.getText().delete(start, end);
					return;
				}
				mCommentEditText.getText().delete(selection - 1, selection);
			}
		}

		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				mCommentEditText.append(spanEmojiStr);
			}
		}

	};

	private int actionBarHeight;
	private int activityHeight;
	private int activityWidth;

	private View channelView;

	private ImageView channelImageView;

	private TextView channelTitleTextView;

	private ImageSpan bgSpan;

	private View contentBottomView;

	private TextView contentTextView;

	private TextView contentTranslateTextView;

	private View contentView;
	private ImageView goodImageView;

	private View goodView;

	private ImageProgressBar imageProgressBar;

	public Boolean isCreate;

	private boolean isEnableSendTranslateBtn;// 未屏蔽我

	private boolean isNeedRefleshHeaderView;

	private boolean isRealUserPhoto = true;

	private boolean isRealCommentUserPhoto = false;
	private TextView locationTextView;

	private Drawable mActionBarBackgroundDrawable;

	@InjectView(R.id.activity_story_add_message)
	EditTextWithFace mCommentEditText;

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UserPhoto userPhoto = (UserPhoto) intent.getExtras()
					.getSerializable(CommonUtilities.EXTRA_MESSAGE);
			if (userPhoto != null) {
				if (userPhoto.getParent_id() == 0// 刷新上面UserPhoto
						&& userPhoto.getId() == mUserPhoto.getId()) {
					mUserPhoto = userPhoto;
					displayHeaderView();
				} else if (userPhoto.getParent_id() > 0) {// 刷新评论list
					mUserStoryCommentListArrayAdapter
							.changeUserPhoto(userPhoto);
				}
			}
		}
	};

	private final BroadcastReceiver mHandleGiftReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			doRetrieveUserPhoto();
			doRetrieveGiftList(false);
		}
	};

	private final BroadcastReceiver mHandleTranslateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			StoryTranslate storyTranslate = (StoryTranslate) intent.getExtras()
					.getSerializable(CommonUtilities.EXTRA_MESSAGE);
			if (storyTranslate != null) {
				if (mUserPhoto.getId() == storyTranslate.getUser_photo_id()) {
					mUserPhoto.to_content = storyTranslate.getTo_content();
					mUserPhoto.translator_fullname = storyTranslate.fullname;
					mUserPhoto.translator_id = storyTranslate.user_id;
					displayHeaderView();
				} else {
					mUserStoryCommentListArrayAdapter
							.updateTranslate(storyTranslate);
				}
			}
		}
	};
	InputMethodManager mInputMethodManager;
	private final TaskListener mRequestAutoTranslatePhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				RequestAutoTranslatePhotoTask requestAutoTranslatePhotoTask = ((RequestAutoTranslatePhotoTask) task);
				mUserStoryCommentListArrayAdapter
						.changeUserPhoto(requestAutoTranslatePhotoTask
								.getUserPhoto());
			}
			swypeLayout.setRefreshing(false);
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}
	};
	private GenericTask mRetrieveUserStoryCommentListTask;
	private final TaskListener mRetrieveUserStoryCommentListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoListTask retrieveUserStoryCommentListTask = (RetrieveUserPhotoListTask) task;
			if (result == TaskResult.OK) {
				List<UserPhoto> mUserStoryCommentList = retrieveUserStoryCommentListTask
						.getUserPhotoList();
				if (!retrieveUserStoryCommentListTask.isTop()
						&& mUserStoryCommentList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (retrieveUserStoryCommentListTask.isTop()
						&& mUserStoryCommentList.size() == 0 && !isCreate) {
					Toast.makeText(UserStoryCommentActivity.this,
							R.string.no_new_data, Toast.LENGTH_SHORT).show();
				}

				addAllStoryListArrayAdapter(mUserStoryCommentList,
						retrieveUserStoryCommentListTask.isTop());
				swypeLayout.setRefreshing(false);
				isCreate = false;
			} else {
				String msg = task.getMsg();
				swypeLayout.setRefreshing(false);

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(UserStoryCommentActivity.this, msg,
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			swypeLayout.setProgressTop(retrieveUserPhotoListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};
	private String mUserName;
	private UserPhoto mUserPhoto;

	private final TaskListener mUserPhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
            mUserPhoto = retrieveUserPhotoTask.getUserPhoto();
			if (result == TaskResult.FAILED) {
				isRealUserPhoto = true;

				// 清空与当前story相关的通知栏通知
				long userPhotoId = retrieveUserPhotoTask.getUserPhotoId();
				App.notificationManager
						.cancel(R.layout.activity_story_comment_list
								+ (int) userPhotoId);

				if (isNeedRefleshHeaderView) {
					displayHeaderView();// 通知刷新
				}
				setSendCommentBtnAvailable(mUserPhoto);
				CommonUtilities.broadcastStoryMessage(App.mContext, mUserPhoto);
			} else {
				String msg = task.getMsg();
				Toast.makeText(UserStoryCommentActivity.this, msg,
						Toast.LENGTH_SHORT).show();
                App.hotUserPhotoDAO.deleteHotUserPhotosById(mUserPhoto.getId());
                App.userPhotoDAO.deleteUserPhotosById(mUserPhoto.getId());
                CommonUtilities.broadcastStoryMessage(App.mContext, mUserPhoto);
                finish();
			}
		}
	};

	private final TaskListener mCommentUserPhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
			if (result == TaskResult.OK) {
				UserPhoto commentUserPhoto = retrieveUserPhotoTask
						.getUserPhoto();
				displayCommentReply(commentUserPhoto);// 通知刷新
				onClickComment(commentUserPhoto);
			}
		}
	};
	private final TaskListener mUserStoryNewTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				UserStoryNewTask userStoryNewTask = (UserStoryNewTask) task;
				onSaveCommentSuccess(userStoryNewTask.getUserPhoto());
			} else {
				String msg = task.getMsg();
				onSaveCommentFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setProgressTop(true);
			swypeLayout.setRefreshing(true);
			sendCommmentBtn.setEnabled(false);
		}

		private void onSaveCommentFailure(String msg) {
			swypeLayout.setRefreshing(false);
			userStoryListView.requestFocusFromTouch();
			userStoryListView.setSelection(0);

			if (!Utils.isEmpty(msg)) {
				Toast.makeText(UserStoryCommentActivity.this, msg,
						Toast.LENGTH_SHORT).show();
			}
			sendCommmentBtn.setEnabled(true);
		}

		private void onSaveCommentSuccess(UserPhoto comment) {
			userStoryListView.removeFooterView(userStoryCommmentFooterView);
			userStoryListView.setSelectionAfterHeaderView();
			mCommentEditText.setText("");
			mAddFaceToolView.setVisibility(View.GONE);
			// doRetrieveUserStoryCommentList(true);
			if (mUserStoryCommentListArrayAdapter.isCanAddListItem(comment)) {
				mUserStoryCommentListArrayAdapter.insert(comment, 0);
			}

			mUserPhoto.setComment(mUserPhoto.getComment() + 1);
			// 更新本地
			ContentValues v = new ContentValues();
			v.put(UserPhotoTable.Columns.COMMENT, mUserPhoto.getComment());
			App.userPhotoDAO.updateUserPhoto(mUserPhoto.getId(), v);
			CommonUtilities.broadcastStoryMessage(App.mContext, mUserPhoto);
			swypeLayout.setRefreshing(false);
		}
	};
	private View myContentView;
	private boolean notMoreDataFound = false;

	private ImageView picImgView;
	private String replyId;
	@InjectView(R.id.activity_story_add_comment_layout)
	View sendCommentView;
	@InjectView(R.id.activity_story_detail_btn_send)
	Button sendCommmentBtn;

	@InjectView(R.id.activity_story_detail_comment_listView)
	ListView userStoryListView;
	private View shareView;
	private View giftView;
	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;
	private ImageView userImgMaskView;
	private ImageView userImgView;

	private ImageView userLangImgView;

	private ImageView userMyLangImgView;

	private TextView usernameTextView;

	private View userStoryCommmentFooterView;

	private View userStoryCommmentHeaderView;

	private View userStoryCommmentLikeHeaderView;

	private View userStoryCommmentGiftHeaderView;

	private int viewHeight;

	private SelectFaceHelper mFaceHelper;

	@InjectView(R.id.add_face_tool_layout)
	View mAddFaceToolView;

	private GenericTask mRetrieveStoryLikeListTask;

	private GenericTask mRetrieveUserGiftListTask;

	private List<User> mLikeUserList;

	private List<Gift> mGiftList;

	private final TaskListener mStoryLikeListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveStoryLikeListTask retrieveLikeUserListTask = (RetrieveStoryLikeListTask) task;

			if (result == TaskResult.OK) {
				mLikeUserList = retrieveLikeUserListTask.getLikeUserList();
				ArrayList<String> tempUrlList = new ArrayList<>();
				int totalSize = mLikeUserList.size() > maxShowLikePeopleCnt ? maxShowLikePeopleCnt
						: mLikeUserList.size();
				for (int i = 0; i < totalSize; i++) {
					tempUrlList.add(App.readServerAppInfo().getServerThumbnail(
							mLikeUserList.get(i).getPic_url()));
					mAlbumGallery.setImageList(tempUrlList, width, height,
							margin);
					mAlbumGallery.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	private final TaskListener mStoryGiftListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserGiftListTask retrieveUserGiftListTask = (RetrieveUserGiftListTask) task;

			if (result == TaskResult.OK) {
				mGiftList = retrieveUserGiftListTask.getGiftList();
				ArrayList<String> tempUrlList = new ArrayList<>();
				int totalSize = mGiftList.size() > maxShowLikePeopleCnt ? maxShowLikePeopleCnt
						: mGiftList.size();
				int presentCount = 0;
				for (int i = 0; i < totalSize; i++) {
					presentCount += mGiftList.get(i).getQuantity();
					tempUrlList.add(App.readServerAppInfo().getServerPresent(
							mGiftList.get(i).getPic_url()));
					mGiftGallery.setImageList(tempUrlList, width, height,
							margin);
					mGiftGallery.setVisibility(View.VISIBLE);
				}
				if (mGiftList.size() > 0
						&& mUserPhoto.getPresent_count() != mGiftList.size()) {
					// 更新本地
					ContentValues v = new ContentValues();
					v.put(UserPhotoTable.Columns.PRESENT_COUNT,
							mGiftList.size());
					App.userPhotoDAO.updateUserPhoto(mUserPhoto.getId(), v);
					mUserPhoto.setPresent_count(presentCount);
					displayHeaderView();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	int width;

	int height;

	int margin;
	private Gallery mAlbumGallery;
	private Gallery mGiftGallery;

	private final int maxShowLikePeopleCnt = 5;

	private static final int GALLERY_ITEM_WIDTH = 40;

	private static final int GALLERY_ITEM_MARGIN = 2;

	@InjectView(R.id.activity_story_add_comment_reply_layout)
	LinearLayout commentreplyLayout;

	private void addAllStoryListArrayAdapter(List<UserPhoto> userPhotoList,
	                                         boolean up) {
		Boolean isAdd = true;
		int insertId = 0;
		if (mUserStoryCommentListArrayAdapter.getCount() == 0) {
			isAdd = true;
		} else {
			isAdd = false;
		}
		String lang = App.readUser().getLang();
		String lang1 = null;
		if (App.readUser().getAdditionalLangs() != null
				&& App.readUser().getAdditionalLangs().length > 0) {
			lang1 = App.readUser().getAdditionalLangs()[0];
		}

		for (UserPhoto userPhoto : userPhotoList) {
			// request auto translate 去掉重复请求自动翻译
			long requestKey = userPhoto.getId();
			List<Long> storyAutoRequestTransKeyList = RequestAutoTranslatePhotoTask
					.getStoryAutoRequestTransKeyList();
			if (!Utils.isEmpty(userPhoto.getLang())
					&& !Utils.isEmpty(userPhoto.getContent())
					&& (!lang.equals(userPhoto.getLang()) || (lang1 != null && !lang1
					.equals(userPhoto.getLang())))
					&& Utils.isEmpty(userPhoto.getTo_content())
					&& !UserPhoto.isAutoTranslated(userPhoto)
					&& !storyAutoRequestTransKeyList.contains(requestKey)) {
				if (lang.equals(userPhoto.getLang())
						|| (lang1 != null && !lang1.equals(userPhoto.getLang()))) {
					requestAutotranslate(userPhoto, lang1,
							mRequestAutoTranslatePhotoTaskListener);
				} else {
					requestAutotranslate(userPhoto, lang,
							mRequestAutoTranslatePhotoTaskListener);
				}
				storyAutoRequestTransKeyList.add(requestKey);
			}

			if (mUserStoryCommentListArrayAdapter.isCanAddListItem(userPhoto)) {
				if (isAdd || !up) {
					mUserStoryCommentListArrayAdapter.add(userPhoto);
				} else {
					mUserStoryCommentListArrayAdapter.insert(userPhoto,
							insertId);
				}

			}
			insertId++;
		}
		if (userPhotoList.size() == 0
				&& userStoryListView.getFooterViewsCount() == 0) {
			userStoryCommmentFooterView = setupUserStoryCommentFooterEmptyView();
			userStoryListView.addFooterView(userStoryCommmentFooterView);
			userStoryCommmentFooterView.post(new Runnable() {
				@Override
				public void run() {
					showFooterView();
				}
			});
			;
		}
		if (mUserStoryCommentListArrayAdapter.getCount() > 0) {
			userStoryListView.removeFooterView(userStoryCommmentFooterView);
		}
	}

	private void displayCommentReply(final UserPhoto userPhoto) {
		// 描绘描述页面
		commentreplyLayout.setVisibility(View.VISIBLE);
		final UserStoryCommentListArrayAdapter.ViewHolder holder;
		holder = new ViewHolder(commentreplyLayout);
		UserStoryCommentListArrayAdapter.bindUserPhoto(this, holder, userPhoto);
	}

	private void displayHeaderView() {
		// channel
		if (mUserPhoto.getChannel_id() > 0) {
			channelView.setVisibility(View.VISIBLE);
			channelView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Channel channel = new Channel();
					channel.setId(mUserPhoto.getChannel_id());
					channel.setTitle(mUserPhoto.getChannel_title());
					channel.setPic_url(mUserPhoto.getChannel_pic());
					Intent intent = new Intent(UserStoryCommentActivity.this,
							ChannelPopularListActivity.class);
					intent.putExtra(ChannelPopularListActivity.EXTRA_CHNANEL,
							channel);
					startActivity(intent);
				}
			});
			String image = mUserPhoto.getChannel_pic();
			if (!Utils.isEmpty(image)) {
				channelImageView.setVisibility(View.VISIBLE);

				final String photoUri = App.readServerAppInfo()
						.getServerOriginal(image);
				if (!photoUri.equals(channelImageView.getTag())) {
					ImageManager.imageLoader.displayImage(photoUri,
							channelImageView);
				}
				channelImageView.setTag(photoUri);
			} else {
				channelImageView.setTag(null);
				channelImageView.setVisibility(View.VISIBLE);
				channelImageView.setOnClickListener(null);
			}

			if (!Utils.isEmpty(mUserPhoto.getChannel_title())) {
				channelTitleTextView.setText(mUserPhoto.getChannel_title());
			}
		} else {
			channelView.setVisibility(View.GONE);
		}

		// photo
		String image = mUserPhoto.getPic_url();
		if (!Utils.isEmpty(image)) {
			picImgView.setVisibility(View.VISIBLE);

			final String photoUri = App.readServerAppInfo().getServerOriginal(
					image);
			if (!photoUri.equals(picImgView.getTag())) {
				ImageManager.imageLoader
						.displayImage(
								photoUri,
								picImgView,
								ImageManager.getOptionsLandscape(),
								ImageViewActivity.createImageLoadingListenerWithResize(
										imageProgressBar,
										mUserPhoto.getWidth(),
										mUserPhoto.getHeight(), 0, 1.3f),
								ImageViewActivity
										.createLoadingProgresListener(imageProgressBar));
			}
			picImgView.setTag(photoUri);
			picImgView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					Intent intent = new Intent(UserStoryCommentActivity.this,
							UserStoryImageViewActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							mUserPhoto);
					startActivity(intent);
				}
			});
		} else {
			picImgView.setTag(null);
			picImgView.setVisibility(View.VISIBLE);
			picImgView.setOnClickListener(null);
		}

		if (isRealUserPhoto) {// 是真正的userPhoto
			String userPic = mUserPhoto.getUser_pic();
			Utils.setUserPicImage(userImgView, userPic);
			userImgMaskView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoProfileAcitivity(mUserPhoto,
							UserStoryCommentActivity.this);
				}
			});

			// fullname
			String create_date = DateCommonUtils.dateFormat(
					mUserPhoto.getCreate_date(),
					DateCommonUtils.DF_yyyyMMddHHmmssSSS);
			String content_date = DateCommonUtils.formatConvUtcDateString(
					create_date, false, false);
			// fullname
			String fullname = Utils.getFriendName(mUserPhoto.getUserid(),
					mUserPhoto.getFullname());
			if (!Utils.isEmpty(fullname)) {
				usernameTextView.setText(Html.fromHtml(Utils
						.htmlSpecialChars(fullname)
						+ "<small><i> -"
						+ content_date + "</i></small>"));
				usernameTextView.getPaint().setFakeBoldText(true);
			} else {
				usernameTextView.setText("");
			}

			contentBottomView.setVisibility(View.VISIBLE);
			userLangImgView.setImageResource(Utils.getLanguageFlag(mUserPhoto
					.getLang()));

			String content = mUserPhoto.getContent();
			final String to_content = mUserPhoto.getTo_content();
			boolean isNoNeedTranslate = true;
			if (Utils.isEmpty(content)) {
				contentView.setVisibility(View.GONE);
			} else {
				isNoNeedTranslate = ParseEmojiMsgUtil.isNoNeedTranslate(
						content, this);
				contentView.setVisibility(View.VISIBLE);

				int tipResID = R.string.request_translate;
				if (!Utils.showUserStoryTranslateBtn(mUserPhoto)) {// 显示翻译按钮
					tipResID = R.string.view_translate;
				}

				contentTextView
						.setText(Html.fromHtml(Utils.highlightTag(Utils
								.htmlSpecialChars(content))
								+ "&nbsp;<small><font color='#191970'> - "
								+ App.mContext.getString(tipResID)
								+ "</font></small>"));

				contentTextView.setOnClickListener(new OnClickListener() {
					private void gotoStoryTranslateActivity() {
						Intent intent = new Intent(
								UserStoryCommentActivity.this,
								UserStoryTranslateActivity.class);
						intent.putExtra(
								UserStoryCommentActivity.EXTRA_USER_PHOTO,
								mUserPhoto);
						startActivity(intent);
					}

					@Override
					public void onClick(View v) {
						gotoStoryTranslateActivity();
					}
				});
				contentTextView.setClickable(true);

			}

			// my
			if (Utils.isEmpty(to_content) || isNoNeedTranslate) {
				// 和登陆者的语言相同就不显示自动翻译
				myContentView.setVisibility(View.GONE);

				contentTranslateTextView
						.setText(R.string.message_status_text_translating);
			} else {
				myContentView.setVisibility(View.VISIBLE);
				userMyLangImgView.setImageResource(Utils
						.getLanguageFlag(mUserPhoto.getTo_lang()));

				String translator_fullname = mUserPhoto
						.getTranslator_fullname();
				if (Utils.isEmpty(translator_fullname)) {
					if (mUserPhoto.getTranslator_id() > 0) {
						translator_fullname = getString(R.string.human_translation);
					} else {
						translator_fullname = getString(R.string.auto_translation_msg);
					}
				} else {
					translator_fullname = Utils.getFriendName(
							mUserPhoto.getTranslator_id(), translator_fullname);
				}
				contentTranslateTextView.setText(Html.fromHtml(Utils
						.htmlSpecialChars(mUserPhoto.getTo_content())
						+ "&nbsp;<small><font color='#ff6600'>"
						+ translator_fullname + "</font></small>"));
				contentTranslateTextView.setTextColor(getResources().getColor(
						R.color.text_gray));

			}
		} else {
			contentBottomView.setVisibility(View.GONE);
		}

		TextView likeCountView = (TextView) findById(
				this.userStoryCommmentLikeHeaderView,
				R.id.item_story_comment_like_textview);
		if (mUserPhoto.getGood() <= 0) {
			likeCountView.setText(this.getString(R.string.no_like_count));
		} else {
			likeCountView.setText(this.getString(R.string.like_count,
					mUserPhoto.getGood()));
		}

		likeCountView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUserPhoto.getGood() > 0) {
					gotoLikeListActivity();
				}
			}
		});

		TextView giftCountView = (TextView) findById(
				this.userStoryCommmentGiftHeaderView,
				R.id.item_story_comment_gift_textview);
		if (mUserPhoto.getPresent_count() <= 0) {
			giftCountView.setText(this.getString(R.string.no_gift_count));
		} else {
			giftCountView.setText(this.getString(R.string.gift_count,
					mUserPhoto.getPresent_count()));
		}

		giftCountView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUserPhoto.getPresent_count() > 0) {
					gotoGiftListActivity();
				}
			}
		});

		int likeIconRes;
		if (mUserPhoto.getFavorite() > 0) {
			likeIconRes = R.drawable.ic_action_social_like_selected;
		} else {
			likeIconRes = R.drawable.ic_action_social_like_unselected;
		}
		goodImageView.setImageResource(likeIconRes);
		goodView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.likeAnimation(
						(ImageView) findById(goodView,
								R.id.item_story_comment_good_imageview),
						(mUserPhoto.getFavorite() > 0));
				UserStoryCommentActivity.gotoLikePhoto(mUserPhoto,
						new TaskAdapter() {
							@Override
							public void onPostExecute(GenericTask task,
							                          TaskResult result) {
								PhotoLikeTask photoLikeTask = (PhotoLikeTask) task;
								if (result == TaskResult.FAILED) {
									Toast.makeText(
											UserStoryCommentActivity.this,
											task.getMsg(), Toast.LENGTH_SHORT)
											.show();
								} else {
									UserPhoto userPhoto = photoLikeTask
											.getUserPhoto();
									Utils.toastPhotoLikeResult(
											UserStoryCommentActivity.this,
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
									displayHeaderView();
									doRetrieveStoryLikeList(false);
								}
								swypeLayout.setRefreshing(false);
								goodView.setEnabled(true);
							}

							@Override
							public void onPreExecute(GenericTask task) {
								goodView.setEnabled(false);
								swypeLayout.setRefreshing(true);
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

		giftView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryListCursorAdapter.gotoGiftListActivity(v.getContext(),
						mUserPhoto);
			}
		});

		if (Utils
				.isValidLocation6(mUserPhoto.getLate6(), mUserPhoto.getLnge6())
				&& MyLocation.recentLocation != null) {
			locationTextView.setText(Utils.FomartDistance(Utils.GetDistance(
					mUserPhoto.getLnge6() / 1E6, mUserPhoto.getLate6() / 1E6,
					MyLocation.recentLocation.getLongitude(),
					MyLocation.recentLocation.getLatitude())));
			locationTextView.setVisibility(View.VISIBLE);
		} else {
			locationTextView.setVisibility(View.GONE);
		}

	}

	@OnClick(R.id.activity_story_detail_btn_reply)
	public void doAddReply(View v) {
		Intent intent = new Intent(this, UserStoryReplyActivity.class);
		startActivityForResult(intent, UserStorySaveActivity.ADD_REPLY);
	}

	private void doRetrieveCommentUserPhoto() {
		long photoId = mUserPhoto.getId();
		String lang = App.readUser().getLang();

		GenericTask mUserPhotoTask = new RetrieveUserPhotoTask(photoId, lang);
		mUserPhotoTask.setListener(mCommentUserPhotoTaskListener);
		mUserPhotoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void doRetrieveGiftList(boolean top) {
		long sinceId;
		long maxId;
		maxId = AppPreferences.ID_IMPOSSIBLE;
		sinceId = AppPreferences.ID_IMPOSSIBLE;

		mRetrieveUserGiftListTask = new RetrieveUserGiftListTask(top, 0,
				mUserPhoto.getId(), maxId, sinceId);
		mRetrieveUserGiftListTask.setListener(mStoryGiftListTaskListener);

		mRetrieveUserGiftListTask.execute();
	}

	private void doRetrieveStoryLikeList(boolean top) {
		long sinceId;
		long maxId;
		maxId = AppPreferences.ID_IMPOSSIBLE;
		sinceId = AppPreferences.ID_IMPOSSIBLE;
		mRetrieveStoryLikeListTask = new RetrieveStoryLikeListTask(top,
				mUserPhoto.getId(), maxId, sinceId);
		mRetrieveStoryLikeListTask.setListener(mStoryLikeListTaskListener);
		mRetrieveStoryLikeListTask.execute();
	}

	private void doRetrieveUserPhoto() {
		long photoId;
		if (!isRealUserPhoto) {// !!!!
			photoId = mUserPhoto.getParent_id();
		} else {
			photoId = mUserPhoto.getId();
		}
		String lang = App.readUser().getLang();

		GenericTask mUserPhotoTask = new RetrieveUserPhotoTask(photoId, lang);
		mUserPhotoTask.setListener(mUserPhotoTaskListener);
		mUserPhotoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void doRetrieveUserStoryCommentList(boolean top) {
		if ((notMoreDataFound && !top)
				|| (mRetrieveUserStoryCommentListTask != null && mRetrieveUserStoryCommentListTask
				.getStatus() == GenericTask.Status.RUNNING)) {
			return;
		}
		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		long parent_id = 0;
		if (!isRealUserPhoto) {
			parent_id = mUserPhoto.getParent_id();
		} else {
			parent_id = mUserPhoto.getId();
		}
		mRetrieveUserStoryCommentListTask = new RetrieveUserPhotoListTask(top,
				maxId, sinceId, -1, parent_id, "", 0, 0, "", "");
		mRetrieveUserStoryCommentListTask
				.setListener(mRetrieveUserStoryCommentListTaskListener);
		mRetrieveUserStoryCommentListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void doSend(String text) {
		long photoRreplyId = 0;
		if (hasSpan(bgSpan, mCommentEditText) && !Utils.isEmpty(replyId)) {
			photoRreplyId = Long.valueOf(replyId);
		}
		int late6 = 0;
		int lnge6 = 0;
		if (MyLocation.recentLocation != null) {
			late6 = (Double
					.valueOf(MyLocation.recentLocation.getLatitude() * 1E6))
					.intValue();
			lnge6 = (Double
					.valueOf(MyLocation.recentLocation.getLongitude() * 1E6))
					.intValue();
		}
		GenericTask userStoryNewTask = new UserStoryNewTask(App.readUser()
				.getLang(), "", 0, 0, text, mUserPhoto.getId(), photoRreplyId,
				late6, lnge6, "", "");
		userStoryNewTask.setListener(mUserStoryNewTaskListener);

		userStoryNewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mInputMethodManager.hideSoftInputFromWindow(
				mCommentEditText.getWindowToken(), 0);
	}

	@OnClick(R.id.activity_story_detail_btn_send)
	public void doSendComment(View v) {
		final String text = mCommentEditText.getText().toString().trim();
		if (Utils.isEmpty(text)) {
			return;
		} else {
			sendCommmentBtn.setEnabled(false);
		}
		String content = ParseEmojiMsgUtil.convertToMsg(
				mCommentEditText.getText(), this);
		doSend(content);
	}

	private int getContentViewRes() {
		return R.layout.activity_story_comment_list;
	}

	private long getMaxId() {
		if (mUserStoryCommentListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mUserStoryCommentListArrayAdapter.getItem(
					mUserStoryCommentListArrayAdapter.getCount() - 1).getId() - 1;
		}
	}

	private long getSinceId() {
		if (mUserStoryCommentListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mUserStoryCommentListArrayAdapter.getItem(0).getId() + 1;
		}
	}

	private int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private void gotoGiftListActivity() {
		Intent intent = new Intent(this, UserStoryGiftListActivity.class);
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID,
				mUserPhoto.getId());
		startActivity(intent);
	}

	private void gotoLikeListActivity() {
		Intent intent = new Intent(this, UserStoryLikeListActivity.class);
		intent.putExtra(UserStoryLikeListActivity.EXTRA_PHOTO_ID,
				mUserPhoto.getId());
		startActivity(intent);
	}

	public void gotoProfile(View v) {
		if (mUserPhoto == null)
			return;

		User user = App.userDAO.fetchUser(mUserPhoto.getUserid());
		Intent intent = new Intent(UserStoryCommentActivity.this,
				FriendProfileActivity.class);
		if (user == null) {
			intent.putExtra(ProfileActivity.EXTRA_USER_ID,
					mUserPhoto.getUserid());
		} else {
			intent.putExtra(ProfileActivity.EXTRA_USER, user);
		}
		startActivity(intent);
	}

	public void goToStoryLocation(View v) {
		Intent intent = new Intent(this, UserStoryListActivity.class);
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_LATE6,
				mUserPhoto.getLate6());
		intent.putExtra(AbstractUserStoryListActivity.EXTRA_STORY_LNGE6,
				mUserPhoto.getLnge6());
		startActivity(intent);
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
			if (requestCode == UserStorySaveActivity.ADD_REPLY) {
				Bundle extras = data.getExtras();
				String userid = extras
						.getString(UserStoryReplyActivity.EXTRA_REPLY_ID);
				String fullname = extras
						.getString(UserStoryReplyActivity.EXTRA_REPLY_NAME);

				UserPhoto userPhoto = new UserPhoto();
				userPhoto.setUserid(Long.valueOf(userid));
				userPhoto.setFullname(fullname);
				onClickComment(userPhoto);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (mAddFaceToolView.getVisibility() == View.VISIBLE) {
			mAddFaceToolView.setVisibility(View.GONE);
			return;
		}

		Utils.onBackPressed(this);
	}

	private void onClickComment(UserPhoto userPhoto) {
		try {
			// 回复某个人描画一下回复页面
			if (!Utils.isEmpty(userPhoto.getContent())) {
				displayCommentReply(userPhoto);
			}
		} catch (Exception e) {

		}
		mUserName = userPhoto.getFullname();

		TextPaint tp = new TextPaint();
		tp.setTextSize(mCommentEditText.getTextSize());
		tp.setTypeface(mCommentEditText.getTypeface());
		mUserName = (String) TextUtils.ellipsize(mUserName, tp, 200,
				TextUtils.TruncateAt.MIDDLE);

		replyId = String.format("%d", userPhoto.getUserid());
		String userName = String.format("%s%s", TAG_PATTERN, mUserName);
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		TextView tv = createContactTextView(userName, this, mCommentEditText);
		BitmapDrawable bd = (BitmapDrawable) convertViewToDrawable(tv);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());

		String suffix = ParseEmojiMsgUtil.convertToMsg(
				mCommentEditText.getText(), this);
		String prefix;
		if (hasSpan(bgSpan, mCommentEditText)) {
			int start = mCommentEditText.getText().getSpanStart(bgSpan);
			int end = mCommentEditText.getText().getSpanEnd(bgSpan);
			prefix = suffix.substring(0, start).trim();
			suffix = suffix.substring(end);

			sb.append(prefix);
		}

		if (!suffix.startsWith(SPLIT_PATTERN))
			suffix = String.format("%s %s", SPLIT_PATTERN, suffix);

		sb.append(userName);

		int startIndex = sb.length() - userName.length();
		int endIndex = sb.length();
		bgSpan = new ImageSpan(bd);
		sb.setSpan(bgSpan, startIndex, endIndex,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sb.append(suffix);
		mCommentEditText.setText(sb);
		mCommentEditText.setSelection(sb.length());

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.story_comment);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;

		parseExtras(getIntent().getExtras());

		setupComponents();

		isCreate = true;
		doRetrieveUserStoryCommentList(true);
		doRetrieveUserPhoto();
		doRetrieveStoryLikeList(false);
		doRetrieveGiftList(false);
		if (isRealCommentUserPhoto) {
			doRetrieveCommentUserPhoto();
		}

		registerReceiver(mHandleTranslateReceiver, new IntentFilter(
				CommonUtilities.STORY_TRANSLATE_ACTION));
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				CommonUtilities.STORY_COMMENT_MESSAGE_ACTION));
		registerReceiver(mHandleGiftReceiver, new IntentFilter(
				CommonUtilities.STORY_GIFT_ACTION));

		PresentDonateReceiver.count_of_gift_notification = 0;
	}

	@Override
	protected void onDestroy() {
		instance = null;
		userStoryListView.removeHeaderView(userStoryCommmentLikeHeaderView);
		userStoryListView.removeHeaderView(userStoryCommmentGiftHeaderView);
		userStoryListView.removeHeaderView(userStoryCommmentHeaderView);
		try {
			unregisterReceiver(mHandleTranslateReceiver);
			unregisterReceiver(mHandleMessageReceiver);
			unregisterReceiver(mHandleGiftReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

	@Override
	public void onRefresh(boolean isTop) {
		swypeLayout.setRefreshing(false);
		if (isTop) {
			doRetrieveUserStoryCommentList(isTop);
		} else if (notMoreDataFound && !isTop) {
			Toast.makeText(this, R.string.no_more_data, Toast.LENGTH_SHORT)
					.show();
		}

	}

	private void parseExtras(Bundle extras) {
		if (extras.getBoolean(UserStoryCommentActivity.EXTRA_IS_NOTIFICATION))
			UserStoryReceiver.count_of_story_new_notification = 0;

		mUserPhoto = (UserPhoto) extras.getSerializable(EXTRA_USER_PHOTO);
		isRealUserPhoto = extras.getBoolean(EXTRA_IS_REAL_USERPHOTO, true);
		if (!isRealUserPhoto
				|| (mUserPhoto != null && Utils.isEmpty(mUserPhoto
				.getUser_pic()))) {// 传值是ID，不是UserPhoto，是需要刷新头像
			isNeedRefleshHeaderView = true;
		}
		isRealCommentUserPhoto = extras.getBoolean(
				EXTRA_IS_REAL_COMMENT_USERPHOTO, false);
	}

	private void selectMessageFace() {
		if (null == mFaceHelper) {
			mFaceHelper = new SelectFaceHelper(this, mAddFaceToolView);
			mFaceHelper.setFaceOpreateListener(mOnFaceOperateListener);
		}
		if (mAddFaceToolView.getVisibility() == View.VISIBLE) {
			mAddFaceToolView.setVisibility(View.GONE);
		} else {
			mAddFaceToolView.setVisibility(View.VISIBLE);
			hideInputManager(this);
		}
	}

	private void setSendCommentBtnAvailable(UserPhoto userPhoto) {
		Friend friend = App.friendDAO.fetchFriend(userPhoto.getUserid(), App
				.readUser().getId());
		if (friend != null && friend.getDone() == -1) {// 好友屏蔽我，不可以点击发送
			isEnableSendTranslateBtn = false;
		} else {
			isEnableSendTranslateBtn = true;
		}
	}

	private void setupComponents() {
		mActionBarBackgroundDrawable = getResources().getDrawable(
				R.color.action_bar_background);
		mActionBarBackgroundDrawable = mActionBarBackgroundDrawable
				.getConstantState().newDrawable();

		WindowManager wm = this.getWindowManager();
		activityHeight = wm.getDefaultDisplay().getHeight();
		activityWidth = wm.getDefaultDisplay().getWidth();
		new TypedValue();
		actionBarHeight = 0;

		// if (this.getTheme().resolveAttribute(android.R.attr.actionBarSize,
		// tv,
		// true)) {
		// actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
		// this.getResources().getDisplayMetrics());
		// }

		sendCommmentBtn.setEnabled(false);
		if (isRealUserPhoto) {
			setSendCommentBtnAvailable(mUserPhoto);
		}

		viewHeight = activityHeight - actionBarHeight
				- sendCommentView.getHeight();

		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		userStoryCommmentLikeHeaderView = setupUserStoryCommentLikeHeaderView();
		userStoryCommmentGiftHeaderView = setupUserStoryCommentGiftHeaderView();
		userStoryCommmentHeaderView = setupUserStoryCommentHeaderView();
		try {
			displayHeaderView();
		} catch (Exception e) {

		}

		userStoryListView.addHeaderView(userStoryCommmentHeaderView);
		userStoryListView.addHeaderView(userStoryCommmentLikeHeaderView);
		userStoryListView.addHeaderView(userStoryCommmentGiftHeaderView);

		// comment
		mUserStoryCommentListArrayAdapter = new UserStoryCommentListArrayAdapter(
				this, mUserPhoto.getUserid(), swypeLayout);
		userStoryListView.setAdapter(mUserStoryCommentListArrayAdapter);
		OnScrollListener onScrollListener = new OnScrollListener() {

			// private final String title = getString(R.string.story_comment);

			// private void onNewScroll(int scrollPosition) {
			// int headerHeight = picImgView.getHeight();
			// float ratio = (float) Math.min(Math.max(scrollPosition, 0),
			// headerHeight) / headerHeight;
			// int newAlpha = (int) (ratio * 255);
			// String titleColor = String.format("#%02x%02x%02x", (newAlpha),
			// (newAlpha), (newAlpha));
			// getSupportActionBar().setTitle(
			// Html.fromHtml("<font color=\"" + titleColor + "\"><b>"
			// + title + "</b></font>"));
			// Log.i("scroll", String.format("alpha=%d", newAlpha));
			// mActionBarBackgroundDrawable.setAlpha(newAlpha);
			//
			// getSupportActionBar().setBackgroundDrawable(
			// mActionBarBackgroundDrawable);
			// }

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
			                     int visibleItemCount, int totalItemCount) {
				// View topChild = view.getChildAt(0);
				//
				// if (topChild == null) {
				// onNewScroll(0);
				// } else if (topChild != userStoryCommmentHeaderView) {
				// onNewScroll(picImgView.getHeight());
				// } else {
				// onNewScroll(-topChild.getTop());
				// }
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == view.getCount() - 1
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 加载数据代码
					doRetrieveUserStoryCommentList(false);
				}
			}
		};
		userStoryListView.setOnScrollListener(onScrollListener);

		userStoryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
			                        long arg3) {
				try {
					if (arg2 == 1) {
						gotoLikeListActivity();
					} else if (arg2 == 2) {
						gotoGiftListActivity();
					} else if (arg2 > 2
							&& mUserStoryCommentListArrayAdapter.getCount() > 0) {
						UserPhoto userPhoto = mUserStoryCommentListArrayAdapter
								.getItem(arg2 - 3);
						onClickComment(userPhoto);
						mInputMethodManager.showSoftInput(mCommentEditText, 0);
					}
				} catch (Exception e) {

				}
			}

		});
		mCommentEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mCommentEditText.getGlobalVisibleRect(rect);
					rect.left = rect.right - 100;
					if (rect.contains(eventX, eventY)) {
						selectMessageFace();
						return true;
					} else {
						// mMessageListView.setSelection(mMessageListView
						// .getCount() - 1);
						mAddFaceToolView.setVisibility(View.GONE);
						return false;
					}
				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mCommentEditText.getGlobalVisibleRect(rect);
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
		mCommentEditText.setFilters(filters);
		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mCommentEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable edit) {
				if (edit.length() > 0 && isEnableSendTranslateBtn) {
					sendCommmentBtn.setEnabled(true);
				} else {
					sendCommmentBtn.setEnabled(false);
				}

				if (edit.length() == 0
						|| !UserStoryCommentActivity.hasSpan(bgSpan,
						mCommentEditText)) {
					replyId = "";
					mUserName = "";
					// 隐藏描述页面
					commentreplyLayout.setVisibility(View.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence text, int start,
			                              int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence text, int start, int before,
			                          int count) {

			}

		});

	}

	// emptyView
	private View setupUserStoryCommentFooterEmptyView() {
		View view = View
				.inflate(this, R.layout.item_story_comment_footer, null);
		view.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.MATCH_PARENT, viewHeight));
		return view;
	}

	private View setupUserStoryCommentGiftHeaderView() {
		View view = View.inflate(this, R.layout.item_story_comment_gift_header,
				null);
		TextView giftCountView = (TextView) findById(view,
				R.id.item_story_comment_gift_textview);
		if (mUserPhoto.getPresent_count() <= 0) {
			giftCountView.setText(this.getString(R.string.no_gift_count));
		} else {
			giftCountView.setText(this.getString(R.string.gift_count,
					mUserPhoto.getPresent_count()));
		}

		mGiftGallery = (Gallery) findById(view,
				R.id.activity_story_comment_gift_gallery);
		mGiftGallery
				.setOnGalleryItemClickListener(new OnGalleryItemClickListener() {
					@Override
					public void onGalleryItemClick(Gallery gallery, int position) {
						gotoGiftListActivity();
					}
				});

		// dp转px
		width = Gallery.dip2px(this, GALLERY_ITEM_WIDTH);
		height = Gallery.dip2px(this, GALLERY_ITEM_WIDTH);
		margin = Gallery.dip2px(this, GALLERY_ITEM_MARGIN);

		return view;
	}

	private View setupUserStoryCommentHeaderView() {
		View view = View
				.inflate(this, R.layout.item_story_comment_header, null);
		// view.setLayoutParams(new ListView.LayoutParams(
		// LayoutParams.MATCH_PARENT, (viewHeight) * 3 / 4));
		channelView = findById(view, R.id.user_story_comment_channel_view);
		channelImageView = (ImageView) findById(view,
				R.id.user_story_comment_channel_photo);
		channelTitleTextView = (TextView) findById(view,
				R.id.user_story_comment_channel_title);

		picImgView = (ImageView) findById(view,
				R.id.user_story_comment_pic_imageview);
		imageProgressBar = (ImageProgressBar) findById(view,
				R.id.image_progress_bar);
		userImgView = (ImageView) findById(view,
				R.id.user_story_comment_user_imageview);
		userImgMaskView = (ImageView) findById(view, R.id.item_mask);
		usernameTextView = (TextView) findById(view,
				R.id.user_story_comment_fullname);
		contentBottomView = findById(view, R.id.user_story_comment_bottom_view);
		userLangImgView = (ImageView) findById(view,
				R.id.user_story_comment_lang_imageview);
		contentView = findById(view, R.id.user_story_comment_from_content_view);
		contentTextView = (TextView) findById(view,
				R.id.user_story_comment_content_textview);
		myContentView = findById(view, R.id.user_story_comment_my_content_view);
		userMyLangImgView = (ImageView) findById(view,
				R.id.user_story_comment_my_lang_imageview);
		contentTranslateTextView = (TextView) findById(view,
				R.id.user_story_comment_my_content_textview);
		goodView = findById(view, R.id.item_story_comment_good_view);
		goodImageView = (ImageView) findById(view,
				R.id.item_story_comment_good_imageview);
		shareView = findById(view, R.id.item_story_comment_share_view);
		giftView = findById(view, R.id.item_story_comment_gift_view);
		locationTextView = (TextView) findById(view,
				R.id.item_story_comment_location_textview);

		return view;
	}

	private View setupUserStoryCommentLikeHeaderView() {
		View view = View.inflate(this, R.layout.item_story_comment_like_header,
				null);
		TextView likeCountView = (TextView) findById(view,
				R.id.item_story_comment_like_textview);
		if (mUserPhoto.getGood() <= 0) {
			likeCountView.setText(this.getString(R.string.no_like_count));
		} else {
			likeCountView.setText(this.getString(R.string.like_count,
					mUserPhoto.getGood()));
		}
		mAlbumGallery = (Gallery) findById(view,
				R.id.activity_story_comment_user_gallery);
		mAlbumGallery
				.setOnGalleryItemClickListener(new OnGalleryItemClickListener() {
					@Override
					public void onGalleryItemClick(Gallery gallery, int position) {
						User user = mLikeUserList.get(position);
						if (user != null) {
							UserStoryCommentActivity.gotoProfileAcitivity(
									user.getId(), gallery.getContext());
						}
					}
				});

		// dp转px
		width = Gallery.dip2px(this, GALLERY_ITEM_WIDTH);
		height = Gallery.dip2px(this, GALLERY_ITEM_WIDTH);
		margin = Gallery.dip2px(this, GALLERY_ITEM_MARGIN);

		return view;
	}

	private void showFooterView() {
		int footerHeight = activityHeight - actionBarHeight
				- sendCommentView.getHeight() - getStatusBarHeight()
				- userStoryCommmentLikeHeaderView.getMeasuredHeight()
				- userStoryCommmentGiftHeaderView.getMeasuredHeight()
				- userStoryCommmentHeaderView.getMeasuredHeight();
		if (footerHeight < 100)
			footerHeight = 100;
		userStoryCommmentFooterView.setLayoutParams(new ListView.LayoutParams(
				LayoutParams.MATCH_PARENT, footerHeight));
	}
}