package com.ruptech.chinatalk.ui.story;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveStoryTranslateListTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.task.impl.StoryTranslateLikeTask;
import com.ruptech.chinatalk.task.impl.StoryTranslateNewTask;
import com.ruptech.chinatalk.ui.AbstractChatActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.LanguageActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.EditTextWithLang;
import com.ruptech.chinatalk.widget.StoryTranslateListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

public class UserStoryTranslateActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	public static final String EXTRA_USER_PHOTO = "USER_PHOTO";

	public static void gotoTranslateLikePhoto(StoryTranslate translate,
			TaskListener likeListener) {
		StoryTranslateLikeTask photoLikeTask = new StoryTranslateLikeTask(
				translate.getUser_photo_id(), translate.getLang(),
				translate.getId(), translate.getFavorite() == 0);
		photoLikeTask.setListener(likeListener);
		photoLikeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	public static void gotoUserStoryCommentActivity(UserPhoto userPhoto,
			Context context) {
		Intent intent = new Intent(context, UserStoryCommentActivity.class);
		intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO, userPhoto);
		context.startActivity(intent);
	}

	private final String TAG = Utils.CATEGORY
			+ UserStoryTranslateActivity.class.getSimpleName();

	private static StoryTranslateListArrayAdapter mStoryTranslateListArrayAdapter;

	@InjectView(R.id.activity_story_translate_message)
	EditTextWithLang mTranslateEditText;

	@InjectView(R.id.activity_story_translate_send_btn)
	Button sendTranslateBtn;

	private GenericTask mRetrieveStoryTranslateListTask;

	List<StoryTranslate> mStoryTranslateList;

	private static UserStoryTranslateActivity instance = null;

	private final TaskListener mRetrieveStoryTranslateListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveStoryTranslateListTask retrieveStoryTranslateListTask = (RetrieveStoryTranslateListTask) task;
			if (result == TaskResult.OK) {
				mStoryTranslateList = retrieveStoryTranslateListTask
						.getStoryTranslateList();
				if (!retrieveStoryTranslateListTask.isTop()
						&& mStoryTranslateList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				if (retrieveStoryTranslateListTask.isTop()
						&& mStoryTranslateList.size() == 0) {
					Toast.makeText(UserStoryTranslateActivity.this,
							R.string.no_new_data,
							Toast.LENGTH_SHORT).show();
				}

				addAllStoryListArrayAdapter(mStoryTranslateList,
						retrieveStoryTranslateListTask.isTop());
				selectTranslate();
				swypeLayout.setRefreshing(false);
				if (mStoryTranslateListArrayAdapter.getCount() == 0) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(R.string.no_data_found);
				} else {
					emptyTextView.setVisibility(View.GONE);
				}
			} else {
				String msg = task.getMsg();
				swypeLayout.setRefreshing(false);

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(UserStoryTranslateActivity.this, msg,
							Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveStoryTranslateListTask retrievestoryTranslateListTask = (RetrieveStoryTranslateListTask) task;
			swypeLayout.setProgressTop(retrievestoryTranslateListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	private UserPhoto mUserPhoto;
	private final TaskListener mUserStoryNewTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				StoryTranslateNewTask storyTranslateNewTask = (StoryTranslateNewTask) task;
				onSaveStoryTranslateSuccess(storyTranslateNewTask
						.getStoryTranslate());
			} else {
				String msg = task.getMsg();
				onSaveStoryTranslateFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setProgressTop(true);
			swypeLayout.setRefreshing(true);
			sendTranslateBtn.setEnabled(false);
		}

		private void onSaveStoryTranslateFailure(String msg) {
			swypeLayout.setRefreshing(false);
			storyTranslateListView.requestFocusFromTouch();
			storyTranslateListView.setSelection(0);

			if (!Utils.isEmpty(msg)) {
				Toast.makeText(UserStoryTranslateActivity.this, msg,
						Toast.LENGTH_SHORT).show();
			}
			sendTranslateBtn.setEnabled(true);
		}

		private void onSaveStoryTranslateSuccess(StoryTranslate translate) {
			storyTranslateListView.requestFocusFromTouch();
			storyTranslateListView.setSelection(0);
			mTranslateEditText.setText("");
			// sendTranslateBtn.setEnabled(true);
			// doRetrieveUserStoryCommentList(true);
			mStoryTranslateListArrayAdapter.insertTranslate(translate);

			// 更新本地
			if (translate.getLang().equalsIgnoreCase(App.readUser().getLang())) {
				ContentValues v = new ContentValues();
				v.put(UserPhotoTable.Columns.TO_CONTENT, translate.to_content);
				v.put(UserPhotoTable.Columns.TRANSLATOR_FULLNAME,
						translate.fullname);
				v.put(UserPhotoTable.Columns.TRANSLATOR_ID, translate.user_id);
				App.userPhotoDAO.updateUserPhoto(translate.getUser_photo_id(),
						v);
				CommonUtilities.broadcastStoryMessage(App.mContext, null);

				CommonUtilities
						.broadcastStoryTranslate(App.mContext, translate);
			}
			swypeLayout.setRefreshing(false);
		}
	};

	private boolean isEverSelected = false;

	private boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	InputMethodManager mInputMethodManager;

	@InjectView(R.id.item_story_comment_user_imageview)
	ImageView userImgView;

	@InjectView(R.id.item_mask)
	ImageView userImgMaskView;

	@InjectView(R.id.item_story_comment_lang_imageview)
	ImageView userLangImgView;

	@InjectView(R.id.item_story_comment_fullname_textview)
	TextView usernameTextView;
	@InjectView(R.id.item_story_comment_content_textview)
	TextView contentTextView;
	@InjectView(R.id.activity_story_add_translate_layout)
	View bottomInputView;
	@InjectView(R.id.activity_story_add_translate_tips_layout)
	View bottomTranalteTipsView;
	@InjectView(R.id.activity_story_add_translate_tips_textview)
	TextView bottomTranalteTipsTextView;

	private boolean isEnableSendTranslateBtn;// 未屏蔽我

	@InjectView(R.id.activity_story_detail_translate_listview_emptyview_text)
	TextView emptyTextView;

	@InjectView(R.id.activity_story_detail_translate_listView)
	ListView storyTranslateListView;

	private void addAllStoryListArrayAdapter(
			List<StoryTranslate> mStoryTranslateList, boolean up) {
		for (StoryTranslate userPhoto : mStoryTranslateList) {
			if (!Utils.isEmpty(userPhoto.getTo_content())) {
				mStoryTranslateListArrayAdapter.add(userPhoto);
			}
		}
	}

	private void displayHeaderView(final UserPhoto headerUserPhoto) {
		// user photo
		String image = headerUserPhoto.getPic_url();
		Utils.setUserPicImage(userImgView, image);

		userImgMaskView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryTranslateActivity.gotoUserStoryCommentActivity(
						headerUserPhoto, UserStoryTranslateActivity.this);
			}
		});
		userLangImgView.setImageResource(Utils.getLanguageFlag(mUserPhoto
				.getLang()));

		// fullname
		String fullname = Utils.getFriendName(mUserPhoto.getUserid(),
				mUserPhoto.getFullname());
		String create_date = DateCommonUtils.dateFormat(
				mUserPhoto.getCreate_date(),
				DateCommonUtils.DF_yyyyMMddHHmmssSSS);
		String content_date = DateCommonUtils.formatConvUtcDateString(
				create_date, false, false);
		usernameTextView.setText(Html.fromHtml(Utils.htmlSpecialChars(fullname)
				+ "<small><i> -" + content_date + "</i></small>"));
		usernameTextView.getPaint().setFakeBoldText(true);

		// content
		final String content = mUserPhoto.getContent();
		contentTextView.setVisibility(View.VISIBLE);
		contentTextView.setText(Html.fromHtml(Utils.highlightTag(Utils
				.htmlSpecialChars(content))));
	}

	private void doRetrieveHeaderUserPhoto() {
		String image = mUserPhoto.getPic_url();
		if (Utils.isEmpty(image) && mUserPhoto.getParent_id() > 0) {
			UserPhoto parentUserPhoto = App.userPhotoDAO
					.getStoryByUserPhotoId(mUserPhoto.getParent_id());
			if (parentUserPhoto != null) {
				displayHeaderView(parentUserPhoto);
			} else {
				RetrieveUserPhotoTask retrieveUserPhotoTask = new RetrieveUserPhotoTask(
						mUserPhoto.getParent_id(), App.readUser().lang);
				retrieveUserPhotoTask.setListener(new TaskAdapter() {
					@Override
					public void onPostExecute(GenericTask task,
							TaskResult result) {
						RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
						if (result == TaskResult.OK) {
							UserPhoto userPhoto = retrieveUserPhotoTask
									.getUserPhoto();
							displayHeaderView(userPhoto);
						}
					}
				});
				retrieveUserPhotoTask.execute();
			}
		} else if (!Utils.isEmpty(image)) {
			displayHeaderView(mUserPhoto);
		}
	}

	private void doRetrieveStoryTranslateList(boolean top) {
		if ((notMoreDataFound && !top)
				|| (mRetrieveStoryTranslateListTask != null && mRetrieveStoryTranslateListTask
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
		mRetrieveStoryTranslateListTask = new RetrieveStoryTranslateListTask(
				top, maxId, sinceId, mUserPhoto.getId(), -1);
		mRetrieveStoryTranslateListTask
				.setListener(mRetrieveStoryTranslateListTaskListener);
		mRetrieveStoryTranslateListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void doSend(String text) {
		GenericTask translateNewTask = new StoryTranslateNewTask(text,
				PrefUtils.getPrefPerferLang(), mUserPhoto.getId());
		translateNewTask.setListener(mUserStoryNewTaskListener);
		translateNewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mInputMethodManager.hideSoftInputFromWindow(
				mTranslateEditText.getWindowToken(), 0);
	}

	@OnClick(R.id.activity_story_translate_send_btn)
	public void doSendTranslate(View v) {
		final String text = mTranslateEditText.getText().toString().trim();
		if (Utils.isEmpty(text)) {
			return;
		} else if (mUserPhoto.getLang().equals(PrefUtils.getPrefPerferLang())) {
			Toast.makeText(UserStoryTranslateActivity.this,
					R.string.please_select_translate_language,
					Toast.LENGTH_SHORT).show();
			return;
		} else if (isSameTranslation(text)) {// 不可以输入同样的翻译内容
			Toast.makeText(UserStoryTranslateActivity.this,
					R.string.translation_is_same,
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			sendTranslateBtn.setEnabled(false);
		}

		if (!Utils.isEmpty(text)) {
			doSend(text);
		}
	}

	private int getContentViewRes() {
		return R.layout.activity_story_translate_list;
	}

	private long getMaxId() {
		if (mStoryTranslateListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mStoryTranslateListArrayAdapter.getItem(
					mStoryTranslateListArrayAdapter.getCount() - 1).getId() - 1;
		}
	}

	private long getSinceId() {
		if (mStoryTranslateListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mStoryTranslateListArrayAdapter.getItem(0).getId() + 1;
		}
	}

	@OnClick(R.id.activity_story_add_translate_tips_layout)
	public void gotoChangeLanguageActivity(View v) {
		Intent intent = new Intent(UserStoryTranslateActivity.this,
				LanguageActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, App.readUser());
		startActivity(intent);
	}

	public void gotoProfile(View v) {
		if (mUserPhoto == null)
			return;

		User user = App.userDAO.fetchUser(mUserPhoto.getUserid());
		Intent intent = new Intent(UserStoryTranslateActivity.this,
				FriendProfileActivity.class);
		if (user == null) {
			intent.putExtra(ProfileActivity.EXTRA_USER_ID,
					mUserPhoto.getUserid());
		} else {
			intent.putExtra(ProfileActivity.EXTRA_USER, user);
		}
		startActivity(intent);
	}

	private void initLayout() {
		if (Utils.showUserStoryTranslateBtn(mUserPhoto)) {
			getSupportActionBar().setTitle(R.string.request_translate);
		} else {
			getSupportActionBar().setTitle(R.string.view_translate);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
		doRetrieveHeaderUserPhoto();
		doRetrieveStoryTranslateList(true);
	}

	private boolean isSameTranslation(String text) {
		boolean isSameTranslation = false;
		if (mStoryTranslateList != null && mStoryTranslateList.size() > 0) {
			for (int i = 0; i < mStoryTranslateList.size(); i++) {
				if (text.equals(mStoryTranslateList.get(i).getTo_content())) {
					isSameTranslation = true;
					break;
				}
			}
		}
		return isSameTranslation;
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		instance = this;
		ButterKnife.inject(this);
		parseExtras(getIntent().getExtras());

		App.notificationManager.cancel(R.layout.activity_story_translate_list);

		if (PrefUtils.getPrefStoryTranslationShowTipsDialog() == 0) {
			showTipsDialog();
			PrefUtils.savePrefStoryTranslateShowTipsDialog();
		}

		initLayout();
	}

	@Override
	protected void onDestroy() {
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
			doRetrieveStoryTranslateList(isTop);
		} else if (notMoreDataFound && !isTop) {
			Toast.makeText(this, R.string.no_more_data, Toast.LENGTH_SHORT)
					.show();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		showBottomView();
	}

	private void onSelectItem(int position) {
		try {
			if (position >= 0) {
				StoryTranslate item = (StoryTranslate) storyTranslateListView
						.getItemAtPosition(position);
				String content = item.getTo_content();
				if (!Utils.isEmpty(content)) {
					mTranslateEditText.setText(content);
				}

				if (App.readUser().isCanTranslate(item.getLang())) {
					mTranslateEditText.changeLang(item.getLang());
				}
			}
		} catch (Exception e) {

		}
	}

	private void parseExtras(Bundle extras) {
		mUserPhoto = (UserPhoto) extras.getSerializable(EXTRA_USER_PHOTO);
	}

	private void selectTranslate() {
		if (isEverSelected)
			return;

		int firstTranslateIndex[] = { -1, -1, -1, -1 };
		for (int i = 0; i < mStoryTranslateListArrayAdapter.getCount(); i++) {
			StoryTranslate temp = mStoryTranslateListArrayAdapter.getItem(i);

			List<String> langs = App.readUser().getAllLangs();
			for (int j = 0; j < langs.size(); j++) {
				if (temp.getLang().equals(langs.get(j))
						&& !mUserPhoto.getLang().equals(langs.get(j))
						&& firstTranslateIndex[j] == -1) {
					firstTranslateIndex[j] = i;
				}
			}
		}

		isEverSelected = true;
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
		sendTranslateBtn.setEnabled(false);
		setSendCommentBtnAvailable(mUserPhoto);

		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		emptyTextView.setVisibility(View.GONE);

		// comment
		mStoryTranslateListArrayAdapter = new StoryTranslateListArrayAdapter(
				this, swypeLayout);
		storyTranslateListView.setAdapter(mStoryTranslateListArrayAdapter);
		storyTranslateListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						onSelectItem(position);
					}

				});
		mTranslateEditText.setHintContent(R.string.please_enter_translation,
				mUserPhoto.getLang());
		InputFilter[] filters = { new AbstractChatActivity.TranslateLengthFilter() };
		mTranslateEditText.setFilters(filters);
		mTranslateEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0 && isEnableSendTranslateBtn) {
					sendTranslateBtn.setEnabled(true);
				} else {
					sendTranslateBtn.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	private void showBottomView() {
		if (!Utils.showUserStoryTranslateBtn(mUserPhoto)) {
			bottomInputView.setVisibility(View.GONE);
			bottomTranalteTipsView.setVisibility(View.VISIBLE);
			bottomTranalteTipsTextView.setText(getString(
					R.string.participate_in_transaltion, String.valueOf(Utils
							.getLangDisplayName(mUserPhoto.getLang()))));
		} else {
			bottomInputView.setVisibility(View.VISIBLE);
			bottomTranalteTipsView.setVisibility(View.GONE);
		}
	}

	private void showTipsDialog() {
		new CustomDialog(this)
				.setTitle(getString(R.string.story_translate_tips_dialog_title))
				.setNegativeButton(getString(R.string.got_it), null)
				.setMessage(
						getString(R.string.story_translate_tips_dialog_content))
				.show();
	}
}