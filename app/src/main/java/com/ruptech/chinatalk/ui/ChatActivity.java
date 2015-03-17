package com.ruptech.chinatalk.ui;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.github.kevinsawicki.http.HttpRequest;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.adapter.ChatAdapter;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.sqlite.TableContent.ChatRoomTable;
import com.ruptech.chinatalk.sqlite.UserProvider;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.task.impl.RequestTranslateTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.setting.ChatSettingActivity;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.ui.story.TextShareActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper.OnFaceOprateListener;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.EditTextWithFace;
import com.ruptech.chinatalk.widget.RecordButton;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 *
 * @author
 */
public class ChatActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatActivity.class.getSimpleName();

	private RosterEntry mRosterEntry;
	private Map<Long, User> mFriendUsers;
	private boolean isExpandedState = false;


	@InjectView(R.id.activity_chat_voice_button)
	RecordButton mVoiceRecordButton;
	@InjectView(R.id.activity_chat_message_edittext)
	EditTextWithFace mMessageEditText;
	@InjectView(R.id.activity_chat_message_listview)
	ListView mMessageListView;
	@InjectView(R.id.activity_chat_btn_send)
	Button mSendButton;
	@InjectView(R.id.activity_chat_message_type_button)
	ImageView mMessageTypeButton;

	@InjectView(R.id.activity_select_message_type)
	View mSelectMessageTypeView;

	private final TaskListener mRetrieveUserListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserTask retrieveUserTask = (RetrieveUserTask) task;
			if (result == TaskResult.OK) {
				if (retrieveUserTask.getUser() != null) {
					User friend = retrieveUserTask.getUser();
					mFriendUsers.put(friend.getLike_id(), friend);

					displayTitle();
					remindFooter();
				}
			} else {
				String msg = retrieveUserTask.getMsg();
				onRetrieveUserTaskFailure(msg);
			}
		}

	};

	private SelectFaceHelper mFaceHelper;

	private boolean isVisbilityFace = false;

	@InjectView(R.id.add_face_tool_layout)
	View mAddFaceToolView;

	@InjectView(R.id.item_balance_remind_textview)
	TextView remindItemFooterTextView;

	@InjectView(R.id.text_layout)
	LinearLayout remindItemFooterLayout;

	@InjectView(R.id.expand_btn)
	ImageView expandBtn;

	@InjectView(R.id.remind_panel)
	View remindItemFooterView;

	OnFaceOprateListener mOnFaceOprateListener = new OnFaceOprateListener() {
		@Override
		public void onFaceDeleted() {
			int selection = mMessageEditText.getSelectionStart();
			String text = mMessageEditText.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					mMessageEditText.getText().delete(start, end);
					return;
				}
				mMessageEditText.getText().delete(selection - 1, selection);
			}
		}

		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				mMessageEditText.append(spanEmojiStr);
			}
		}

	};

	@InjectView(R.id.activity_chat_message_type_voice_button)
	View voiceTypeButton;

	@InjectView(R.id.activity_chat_message_type_keyboard_button)
	View keyboardTypeButton;

	@InjectView(R.id.activity_select_message_type_picture_button)
	View pictureTypeButton;

	private int footTextCount = 0;

	private void addFooterText(String text) {
		TextView textView = new TextView(this);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		textView.setText(Html.fromHtml(text));
		if (footTextCount > 0)
			textView.setVisibility(View.GONE);

		textView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		remindItemFooterLayout.addView(textView);
		footTextCount++;
	}

	protected void displayTitle() {
            getSupportActionBar().setTitle(mRosterEntry.getName());
	}

	public void doSetting(MenuItem item) {
		Intent intent = new Intent(this, ChatSettingActivity.class);
		intent.putExtra(ChatActivity.EXTRA_JID, mWithJabberID);
		this.startActivity(intent);
	}

//	@Override
//	String getFriendLang() {
//		return mFriendUser.getLang();
//	}
//
//	@Override
//	public long getFriendUserId() {
//		return mFriendUser.getId();
//	}

	public void getInitData() {
//		googleTranslate = Utils.isGoogleTranslate(App.readUser().getLang(),
//				mFriendUser.getLang());
//
//		existStore = Utils.isExistStore(App.readUser().getLang(),
//				mFriendUser.getLang());
//
//		translatorCount = Utils.getStoreTranslatorCount(App.readUser()
//				.getLang(), mFriendUser.getLang());
	}


	String getMyLang() {
		return App.readUser().getLang();
	}

    private void parseExtras(){
        mWithJabberID = (String) getIntent().getExtras().get(EXTRA_JID);
        if (Utils.isGroupChat(mWithJabberID)){
            chatRoom = App.mSmack.createChatRoomByRoomName(mWithJabberID);
            if (chatRoom == null){
                Toast.makeText(this, "Can not join to chat room",
					Toast.LENGTH_LONG).show();
                finish();
            }
        }
	    mFriendUsers =  retrieveFriendUsers(mWithJabberID);
    }

	private void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(TextShareActivity.SEND_TEXT);
		if (sharedText != null) {
			switchTextInputMode();
			mMessageEditText.setText(sharedText);
		}
	}

	// 隐藏软键盘
	private void hideInputManager() {
		mInputMethodManager.hideSoftInputFromWindow(mMessageEditText.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void initInputPanel() {
		amIFriend = amIFriend(mFriendUsers);

		boolean enableVoice =  amIFriend;
		voiceTypeButton.setVisibility(enableVoice ? View.VISIBLE : View.GONE);

		boolean enablePicture = amIFriend;
		pictureTypeButton.setVisibility(enablePicture ? View.VISIBLE
				: View.GONE);
	}

	private boolean amIFriend(Map<Long, User> friendUsers) {
		for(User friendUser :  friendUsers.values()) {
			if (friendUser != null) {
				Friend friendUserInfo = App.friendDAO.fetchFriend(
						friendUser.getId(), App.readUser().getId());
				if (friendUserInfo != null
						&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		if (mSelectMessageTypeView.getVisibility() == View.VISIBLE) {
			this.mSelectMessageTypeView.setVisibility(View.GONE);
			return;
		}

		if (isVisbilityFace) {
			isVisbilityFace = false;
			mAddFaceToolView.setVisibility(View.GONE);
			return;
		}

		Utils.onBackPressed(this);
	}

    //
    private void initTransClient() {
        String baidu_api_key = App.properties.getProperty("baidu_api_key");

        translateClient = new TranslateClient(this, baidu_api_key);

        // 这里可以设置为在线优先、离线优先、 只在线、只离线 4种模式，默认为在线优先。
        translateClient.setPriority(TranslateClient.Priority.OFFLINE_FIRST);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			gotoSplashActivity();
			finish();
			return;
		}
		// 打开界面页码设为第一页
		onPage = 1;

		getContentResolver().registerContentObserver(
				UserProvider.CONTENT_URI, true, mFriendUserObserver);// 开始监听联系人数据库
		getContentResolver().registerContentObserver(
				ChatProvider.CONTENT_URI, true, mChatObserver);// 开始监听联系人数据库


		setContentView(R.layout.activity_chat);
		ButterKnife.inject(this);

        parseExtras();
        initTransClient();

//		if (mFriendUser == null) {
//			Toast.makeText(this, R.string.user_infomation_is_invalidate,
//					Toast.LENGTH_LONG).show();
//			finish();
//			return;
//		}
//		if (mFriendUser.getActive() != AppPreferences.USER_ACTIVE_STATUS) {
//			if (Utils.isMail(mFriendUser.getTel())) {
//				Toast.makeText(this, R.string.already_email_friend_msg,
//						Toast.LENGTH_LONG).show();
//			} else {
//				Toast.makeText(this, R.string.already_sms_friend_msg,
//						Toast.LENGTH_LONG).show();
//			}
//		}
//		// 检索好友与我的关系
//		Friend friendUserInfo = App.friendDAO.fetchFriend(mFriendUser.getId(),
//				App.readUser().getId());
//
//		if (friendUserInfo != null
//				&& friendUserInfo.getDone() == AppPreferences.FRIEND_BLOCK_STATUS) {
//			Toast.makeText(this, R.string.friend_block_msg, Toast.LENGTH_LONG)
//					.show();
//			App.notificationManager.cancel(mWithJabberID.hashCode());
//			finish();
//			return;
//		}
//		retrieveFriendUser();
//		mFriend = App.friendDAO.fetchFriend(App.readUser().getId(),
//				mFriendUser.getId());
//
//		if (mFriend == null) {
//			Toast.makeText(this, R.string.no_follow_other, Toast.LENGTH_LONG)
//					.show();
//		}

		getInitData();

		ProfileActivity.close();
		setupComponents();
		switchTextInputMode();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		handleSendText(getIntent());
		initInputPanel();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_actions, mMenu);

		return true;
	}

	private void onExpandFooter(View v) {
		if (expandBtn.getTag().equals(
				String.valueOf(R.drawable.ic_action_arrow_up))) {
			for (int i = 0; i < remindItemFooterLayout.getChildCount(); i++) {
				View child = remindItemFooterLayout.getChildAt(i);
				child.setVisibility(View.VISIBLE);
			}
			expandBtn.setImageResource(R.drawable.ic_action_arrow_down);
			expandBtn.setTag(String.valueOf(R.drawable.ic_action_arrow_down));
			isExpandedState = true;
		} else {
			for (int i = 0; i < remindItemFooterLayout.getChildCount(); i++) {
				View child = remindItemFooterLayout.getChildAt(i);
				if (i > 0) {
					child.setVisibility(View.GONE);
				}
			}
			expandBtn.setImageResource(R.drawable.ic_action_arrow_up);
			expandBtn.setTag(String.valueOf(R.drawable.ic_action_arrow_up));
			isExpandedState = false;
		}

        updateFriendUserStatus();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}

		return true;
	}

	private void onRetrieveUserTaskFailure(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@OnClick(R.id.activity_chat_message_type_keyboard_button)
	public void onSelectKeyboardeMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		switchTextInputMode();
	}

	@OnClick(R.id.activity_select_message_type_picture_button)
	public void onSelectPictureMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		Intent intent = new Intent(this, PhotoAlbumActivity.class);
		intent.putExtra(PhotoAlbumActivity.EXTRA_GO_PREVIOUS_PAGE, true);
		startActivityForResult(intent, CHAT_RETURN_PHOTO);
	}

	@OnClick(R.id.activity_chat_message_type_voice_button)
	public void onSelectVoiceMessageType(View v) {
		mAddFaceToolView.setVisibility(View.GONE);
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		switchVoiceMode();
	}

	@OnClick(R.id.activity_select_message_type_voice_recognize_button)
	public void onSelectVoiceRecognizeTextMessageType(View v) {
		this.mSelectMessageTypeView.setVisibility(View.GONE);
		switchTextInputMode();
		voiceRecognition(null);
	}

	protected void remindFooter() {
//		remindItemFooterLayout.removeAllViews();
//		footTextCount = 0;
//
//		// 检索好友与我的关系
//		final Friend friendUserInfo = App.friendDAO.fetchFriend(
//				mFriendUser.getId(), App.readUser().getId());
//		// 检索我与好友的关系
//		mFriend = App.friendDAO.fetchFriend(App.readUser().getId(),
//				mFriendUser.getId());
//		remindItemFooterView.setVisibility(View.VISIBLE);
//		mSendButton.setEnabled(true);
//
//		if (mFriend == null
//				|| (mFriend != null && mFriend.getDone() != AppPreferences.FRIEND_ADDED_STATUS)) {
//			if (mFriendUser.getBalance() < AppPreferences.MINI_BALANCE) {
//				addFooterText(getString(
//						R.string.no_follow_user_balance_is_not_enough,
//						mFriendUser.getFullname()));
//			}
//			addFooterText(getString(R.string.no_follow_user,
//					mFriendUser.getFullname()));
//		} else if (friendUserInfo == null
//				|| (friendUserInfo != null && friendUserInfo.getDone() != AppPreferences.FRIEND_ADDED_STATUS)) {
//			addFooterText(getString(R.string.user_no_follow_me,
//					mFriendUser.getFullname()));
//		}
//
//		if (App.readUser().getLang().equals(mFriendUser.getLang())) {
//			if (mFriendUser.getActive() == 0) {
//				addFooterText(getString(
//						R.string.same_language_no_active_please_change,
//						Utils.getLangDisplayName(App.readUser().getLang())));
//			}
//		} else {
//			if (!existStore) {
//				addFooterText(getString(R.string.translate_is_free,
//						Utils.getLangDisplayName(App.readUser().getLang())));
//			} else {
//				if (translatorCount == 0) {
//					addFooterText(getString(R.string.no_translator_online));
//				} else {
//					if (App.readUser().getBalance() > AppPreferences.MINI_BALANCE
//							&& mFriend != null
//							&& mFriend.getDone() == AppPreferences.FRIEND_ADDED_STATUS
//							&& (friendUserInfo == null || (friendUserInfo != null && friendUserInfo
//									.getDone() != AppPreferences.FRIEND_ADDED_STATUS))) {
//						addFooterText(getString(R.string.normal_translation));
//					} else if (mFriend != null
//							&& mFriend.getDone() == AppPreferences.FRIEND_ADDED_STATUS
//							&& friendUserInfo != null
//							&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS) {
//						if (App.readUser().getBalance() > AppPreferences.MINI_BALANCE) {
//							addFooterText(getString(R.string.normal_translation));
//						} else if (mFriendUser.getBalance() > AppPreferences.MINI_BALANCE
//								&& friendUserInfo.getWallet_priority() == 1) {
//							addFooterText(getString(R.string.normal_translation));
//						}
//					}
//				}
//
//				// 如果双方互为好友
//				if (mFriend != null
//						&& mFriend.getDone() == AppPreferences.FRIEND_ADDED_STATUS
//						&& friendUserInfo != null
//						&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS) {
//
//					// 好友余额不足并且我没为他分享钱包我还有钱的情况下提示为好友付费
//					if (mFriendUser.getBalance() < AppPreferences.MINI_BALANCE
//							&& mFriend.getWallet_priority() == 0
//							&& App.readUser().getBalance() > AppPreferences.MINI_BALANCE) {
//						addFooterText(getString(R.string.your_friend_balance_is_not_enough));
//					}
//
//					// 如果好友的分享了钱包，并且对方有钱的情况下，显示对方为你承担费用
//					if (friendUserInfo.getWallet_priority() == 1
//							&& mFriend.getWallet_priority() == 0
//							&& mFriendUser.getBalance() > AppPreferences.MINI_BALANCE) {
//						addFooterText(getString(R.string.friend_share_wallet,
//								mFriend.getFriend_nickname()));
//					} else if ((friendUserInfo.getWallet_priority() == 0 && App
//							.readUser().getBalance() < AppPreferences.MINI_BALANCE)
//							|| (mFriendUser.getBalance() < AppPreferences.MINI_BALANCE && App
//									.readUser().getBalance() < AppPreferences.MINI_BALANCE)) {
//						// 好友没有分析钱包，或者双方余额都不足的时候并且我余额不足显示充值
//						addFooterText(getString(R.string.your_balance_is_not_enough));
//					}
//				}
//			}
//		}
//
//		if (footTextCount <= 1)
//			expandBtn.setVisibility(View.GONE);
//		else
//			expandBtn.setVisibility(View.VISIBLE);
//
//		if (footTextCount == 0) {
//			remindItemFooterView.setVisibility(View.GONE);
//		} else if (footTextCount > 0) {
//			remindItemFooterView.setVisibility(View.VISIBLE);
//			if (isExpandedState) {
//				expandBtn.setTag(String.valueOf(R.drawable.ic_action_arrow_up));
//				onExpandFooter(expandBtn);
//			}
//
//			remindItemFooterLayout.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (mFriend == null
//							|| (mFriend != null && mFriend.getDone() != AppPreferences.FRIEND_ADDED_STATUS)) {
//						Intent intent = new Intent(ChatActivity.this,
//								FriendProfileActivity.class);
//						intent.putExtra(ProfileActivity.EXTRA_USER, mFriendUser);
//						ChatActivity.this.startActivity(intent);
//					} else if (App.readUser().getBalance() < AppPreferences.MINI_BALANCE
//							&& friendUserInfo != null
//							&& friendUserInfo.getWallet_priority() == 0) {
//						Intent intent = new Intent(ChatActivity.this,
//								MyWalletActivity.class);
//						ChatActivity.this.startActivity(intent);
//					} else if (existStore
//							&& mFriend != null
//							&& mFriend.getDone() == AppPreferences.FRIEND_ADDED_STATUS
//							&& friendUserInfo != null
//							&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS
//							&& mFriendUser.getBalance() < AppPreferences.MINI_BALANCE
//							&& mFriend.getWallet_priority() == 0) {
//						doSetting(null);
//					}
//				}
//			});
//		}
	}

	private Map<Long, User> retrieveFriendUsers(String jid) {
		//TODO:
		App.userDAO.fetchUser(Utils.getTTTalkIDFromOF_JID(jid));

//		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(
//				mFriendUser.getId());
//		mRetrieveUserTask.setListener(mRetrieveUserListener);
//		mRetrieveUserTask.execute();
//
//		RetrieveServerVersionTask mRetrieveServerVersionTask = new RetrieveServerVersionTask();
//		mRetrieveServerVersionTask.execute();
		return null;
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
			mSelectMessageTypeView.setVisibility(View.GONE);
			hideInputManager();
		}
	}

	@OnClick(R.id.activity_chat_message_type_button)
	public void selectMessageType(View v) {
		if (mSelectMessageTypeView.getVisibility() == View.GONE) {
			this.mSelectMessageTypeView.setVisibility(View.VISIBLE);
			mAddFaceToolView.setVisibility(View.GONE);
			hideInputManager();
		} else if (mSelectMessageTypeView.getVisibility() == View.VISIBLE)
			this.mSelectMessageTypeView.setVisibility(View.GONE);

		if (mSelectMessageTypeView.getVisibility() == View.VISIBLE) {
			initInputPanel();
		}
	}

	@OnClick(R.id.activity_chat_btn_send)
	public void send_text(View v) {
		send_text();
	}

	public void setupComponents() {
		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		mSelectMessageTypeView.setVisibility(View.GONE);

		expandBtn.setImageResource(R.drawable.ic_action_arrow_up);
		expandBtn.setTag(String.valueOf(R.drawable.ic_action_arrow_up));
		expandBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onExpandFooter(v);
			}

		});

        setChatWindowAdapter();

		mMessageListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mSelectMessageTypeView.setVisibility(View.GONE);
			}

		});

		mVoiceRecordButton.setOnFinishedRecordListener(voiceRecordListener);

		mMessageEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mMessageEditText.getGlobalVisibleRect(rect);
					rect.left = rect.right - 100;
					if (rect.contains(eventX, eventY)) {
						selectMessageFace();
						return true;
					} else {
						isVisbilityFace = false;
						mMessageListView.setSelection(mMessageListView
								.getCount() - 1);
						mSelectMessageTypeView.setVisibility(View.GONE);
						mAddFaceToolView.setVisibility(View.GONE);
						return false;
					}
				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					int eventX = (int) event.getRawX();
					int eventY = (int) event.getRawY();
					Rect rect = new Rect();
					mMessageEditText.getGlobalVisibleRect(rect);
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

		InputFilter[] filters = { new Utils.LengthFilter() };
		mMessageEditText.setFilters(filters);

		mMessageEditText.addTextChangedListener(mTextWatcher);

		displayTitle();
	}

    /**
     * 设置聊天的Adapter
     */
    private void setChatWindowAdapter() {
        String selection = ChatTable.Columns.TO_JID + "='" + mWithJabberID + "'";
        // 异步查询数据库
        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                // ListAdapter adapter = new ChatWindowAdapter(cursor,
                // PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
                ListAdapter adapter = new ChatAdapter(ChatActivity.this,
                        cursor, PROJECTION_FROM, translateClient);
                mMessageListView.setAdapter(adapter);
                mMessageListView.setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
                selection, null, null);
    }


	static final int CHANGE_NICKNAME = 5678;

	static final int CHAT_RETURN_PHOTO = 4321;

	public static final String EXTRA_JID = "EXTRA_JID";

	static final int FRIEND_BLACK = 7856;


	private static GenericTask mUploadTask;

	static int onPage;

	static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	public void doRequestTranslate(Message message) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "doRequestTranslate");

		GenericTask mRequestTranslateTask = new RequestTranslateTask(message);
		mRequestTranslateTask.execute();
	}

	public static void doUploadFile(Chat chat,
	                                TaskListener mUploadTaskListener) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "doUploadFile");
		if (mUploadTask != null
				&& mUploadTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		File fFile = null;
		if (!Utils.isEmpty(chat.getFilePath())) {
			if (chat.getType()
					.equals(AppPreferences.MESSAGE_TYPE_NAME_VOICE)) {
				fFile = new File(Utils.getVoiceFolder(App.mContext),
						chat.getFilePath());
			} else {
				fFile = new File(chat.getFilePath());
			}
		}
		mUploadTask = new FileUploadTask(fFile, chat.getType(),
				new HttpRequest.UploadProgress() {
					@Override
					public void onUpload(long uploaded, long total) {
						Log.d(TAG, uploaded + " - " + total);
					}
				});
		mUploadTask.setListener(mUploadTaskListener);
		mUploadTask.execute();
	}

	protected boolean existStore;


	// view是否显示的标识
	boolean loadMoreFlag = true;

	protected boolean amIFriend = true;
	protected MultiUserChat chatRoom;


	InputMethodManager mInputMethodManager;
	protected Chat mChat;// upload 图片时使用

	File mPhotoFile;

//	private final TaskListener mRequestTranslateListener = new TaskAdapter() {
//
//		@Override
//		public void onPostExecute(GenericTask task, TaskResult result) {
//			RequestTranslateTask fsTask = (RequestTranslateTask) task;
//			if (result == TaskResult.OK) {
//				onRequestTranslateSuccess(fsTask);
//			} else {
//				String msg = fsTask.getMsg();
//				Message message = fsTask.getMessage();
//				onRequestTranslateFailure(message, msg);
//			}
//		}
//
//		@Override
//		public void onPreExecute(GenericTask task) {
//			onRequestTranslateBegin();
//		}
//
//	};

	protected final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FileUploadTask photoTask = (FileUploadTask) task;
			if (result == TaskResult.OK) {
				if (mChat != null) {
					mChat.setFilePath(photoTask.getFileInfo().fileName);
					if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
							.equals(mChat.getType()))
						mChat.setContent(getString(R.string.notification_voice));
					else
						mChat.setContent(getString(R.string.notification_picture));

					sendMessageIfNotNull(mChat);
				}
			} else if (result == TaskResult.FAILED) {
				String msg = photoTask.getMsg();
//				onRequestTranslateFailure(mChat, msg);
				if (!Utils.isEmpty(msg)) {
					Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	File mVoiceFile;

	protected int translatorCount = 0;

	RecordButton.OnFinishedRecordListener voiceRecordListener = new RecordButton.OnFinishedRecordListener() {
		@Override
		public void onFinishedRecord(File audioFile) {
			if (BuildConfig.DEBUG)
				Log.v(TAG, "onFinishedRecord");
			if (audioFile.exists()) {
				mVoiceFile = audioFile;

				sendVoice();
			} else {
				mVoiceRecordButton.setText(R.string.press_to_record);
				mVoiceFile = null;
			}
		}

	};

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
		                              int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
		                          int count) {
			if (s.length() > 0) {
				mSendButton.setVisibility(View.VISIBLE);
				mMessageTypeButton.setVisibility(View.INVISIBLE);
			} else {
				mMessageTypeButton.setVisibility(View.VISIBLE);
				mSendButton.setVisibility(View.GONE);
			}
		}

	};


	protected void doClearContent() {
		mMessageEditText.setText("");
		mVoiceFile = null;
		mPhotoFile = null;
	}


	private void doRefresh() {
		updateFriendUserStatus();// 更新联系人状态

		displayTitle();
		remindFooter();
	}

	private void doRetrieveUser(long userId) {

		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(userId);
		mRetrieveUserTask.execute();
	}

	protected void doUpload(String content, String file_path, int contentLength,
	                        String filetype) {
		Chat chat = new Chat();
		chat.setType(filetype);
		chat.setFromContentLength(contentLength);
		chat.setFilePath(file_path);

		mChat = chat;
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())
				|| AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat.getType())) {// 先上传图片或者voice,然后发送请求
			doUploadFile(chat, mUploadTaskListener);
		}
	}

	@Override
	public void finish() {
		super.finish();
	}


	private int getVoiceLength(Context context, File voiceFile) {
		try {

			MediaPlayer player = MediaPlayer.create(context,
					Uri.parse(voiceFile.getAbsolutePath()));
			int voiceLength = player.getDuration() / 1000;
			player.release();
			return voiceLength;

		} catch (Exception e) {
			return 3;
		}
	}


	private void gotoSplashActivity() {
		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				final String[] contents = new String[matches.size()];
				new CustomDialog(this)
						.setTitle(getString(R.string.voice_recognition))
						.setItems(matches.toArray(contents),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
									                    int which) {
										mMessageEditText.setText(
												contents[which]);
									}
								}).show();
			} else if (requestCode == CHAT_RETURN_PHOTO) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Uri mImageUri = (Uri) extras
							.get(PhotoAlbumActivity.RETURN_IMAGE_URI);
					if (mImageUri != null) {
						if ("content".equals(mImageUri.getScheme())) {
							String file_path = ImageManager.getRealPathFromURI(
									this, mImageUri);
							if (Utils.isEmpty(file_path)) {
								Utils.sendClientException(new Exception(),
										"getRealPathFromURI is null");
								this.finish();
							} else {
								mPhotoFile = new File(file_path);
							}
						} else {
							mPhotoFile = new File(mImageUri.getPath());
						}
						sendPhoto();
					}
				}
			} else if (requestCode == FRIEND_BLACK) {
				this.finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		long start = System.currentTimeMillis();
		if (translateClient != null) {
			translateClient.onDestroy();
		}
		super.onDestroy();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onDestroy:" + (System.currentTimeMillis() - start));
	}

	@Override
	public void onPause() {
		long start = System.currentTimeMillis();
		super.onPause();
//		App.mBadgeCount.removeNewMessageCount(getFriendUserId());
		// PrefUtils.removePrefNewMessageCount(getFriendUserId());// 按 Home ||
		// Back
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onPause:" + (System.currentTimeMillis() - start));
	}

	void onRequestTranslateBegin() {
		mMessageListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		mMessageEditText.selectAll();
		switchTextInputMode();
	}

//	void onRequestTranslateFailure(Message message, String msg) {
//		// save message
//		message.setMessage_status(AppPreferences.MESSAGE_STATUS_SEND_FAILED);
//		message.setStatus_text(getString(R.string.message_action_click_resend));
//		App.messageDAO.mergeMessage(message);
//		doRefresh();
//		if (!Utils.isEmpty(msg)) {
//			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
//		}
//	}

	void onRequestTranslateSuccess(RequestTranslateTask fsTask) {
		doRefresh();

		if (fsTask.getIsNeedRetrieveUser()) {
			doRetrieveUser(App.readUser().getId());// 回到Setting画面，能够立刻看到balance变化。
		}

		if (BuildConfig.DEBUG)
			Log.d(TAG, "send Success");

	}

	@Override
	public void onResume() {
		long start = System.currentTimeMillis();
		super.onResume();
		App.notificationManager.cancel(mWithJabberID.hashCode());
		doRefresh();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onResume:" + (System.currentTimeMillis() - start));
	}


	void send_text() {
		boolean noTranslate = true;
		String content = mMessageEditText.getText().toString().trim();
		// reform
		if (content.replaceAll("\n", "").length() == 0) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		// check
		if (content.length() == 0 && mVoiceFile == null && mPhotoFile == null) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String[] msgArray = content.split("]");
		for (String msg : msgArray) {
			if (!ParseEmojiMsgUtil.checkMsgFace(msg + "]")) {
				noTranslate = false;
				break;
			}
		}
		content = ParseEmojiMsgUtil.convertToMsg(
				mMessageEditText.getText(), this);
		sendText(content, noTranslate);

	}

	private void sendPhoto() {
		if (mPhotoFile != null) {
			try {
				boolean wifiAvailible = Utils.isWifiAvailible(this);
				mPhotoFile = ImageManager.compressImage(mPhotoFile, 75, this,
						wifiAvailible);
			} catch (IOException e) {
				if (BuildConfig.DEBUG)
					Log.e(TAG, e.getMessage(), e);
				Utils.sendClientException(e);
			}
			String fromLang = getMyLang();
			String content = null;

			String file_path = mPhotoFile.getAbsolutePath();
			int contentLength = 0;
			String filetype = AppPreferences.MESSAGE_TYPE_NAME_PHOTO;

			doUpload(content, file_path, contentLength, filetype);

		}
	}

	private void sendText(String content, boolean noTranslate) {
		String fromLang = getMyLang();
		String file_path = null;
		int contentLength = Utils.textLength(content);
		String filetype = AppPreferences.MESSAGE_TYPE_NAME_TEXT;

		Chat chat = new Chat();
		chat.setContent(content);
		chat.setType(filetype);
		chat.setFromContentLength(contentLength);
		chat.setFilePath(file_path);

		//新旧版本比较
		if(mFriendUser!=null &&
				(Utils.isEmpty(mFriendUser.getTerminal_type()) || mFriendUser.getTerminal_type() != App.readUser().getTerminal_type())){
			//新版本发给旧版本
			Message message = new Message();
			long localId = System.currentTimeMillis();
			message.setId(localId);
			message.setMessageid(localId);
			message.setUserid(App.readUser().getId());
//            message.setTo_userid(toUserId);
			message.setFrom_lang(fromLang);
//            message.setTo_lang(toLang);
			message.setFrom_content(content);
			message.setMessage_status(AppPreferences.MESSAGE_STATUS_BEFORE_SEND);
			message.setStatus_text(getString(R.string.data_sending));
			message.setFile_path(file_path);
			message.setFrom_content_length(contentLength);
			message.setFile_type(filetype);
			String createDateStr = DateCommonUtils.getUtcDate(new Date(),
					DateCommonUtils.DF_yyyyMMddHHmmssSSS);
			message.create_date = createDateStr;
			message.update_date = createDateStr;
			doRequestTranslate(message);

			//chat
			chat.setFromJid(App.readUser().getOF_JabberID());
			chat.setToJid(mWithJabberID);
			chat.setPid(null);
			chat.setCreated_date(System.currentTimeMillis());


			ChatProvider.insertChat(getContentResolver(), chat);
			mMessageEditText.setText(null);
		}else{
			//新版本发给新版本
			sendMessageIfNotNull(chat);
		}

	}

	private void sendVoice() {

		String content = null;
		String file_path = mVoiceFile.getName();
		int contentLength = getVoiceLength(this, mVoiceFile);
		String filetype = AppPreferences.MESSAGE_TYPE_NAME_VOICE;

		doUpload(content, file_path, contentLength, filetype);
	}


	/**
	 * Fire an intent to start the speech recognition activity.
	 *
	 * @param language
	 */
	private void startVoiceRecognitionActivity(String language) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.voice_recognition));

		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	protected void switchTextInputMode() {
		mVoiceRecordButton.setVisibility(View.GONE);
		mMessageEditText.setVisibility(View.VISIBLE);
		mSendButton.setVisibility(View.GONE);
		mMessageEditText.requestFocus();
		keyboardTypeButton.setVisibility(View.GONE);
		if (!googleTranslate && amIFriend) {
			voiceTypeButton.setVisibility(View.VISIBLE);
		}
		doClearContent();
	}

	protected void switchVoiceMode() {
		mVoiceRecordButton.setVisibility(View.VISIBLE);
		mVoiceRecordButton.setEnabled(true);
		mVoiceRecordButton.setText(R.string.press_to_record);
		mMessageEditText.setVisibility(View.GONE);
		mSendButton.setVisibility(View.GONE);
		keyboardTypeButton.setVisibility(View.VISIBLE);
		voiceTypeButton.setVisibility(View.INVISIBLE);

		doClearContent();
		mInputMethodManager.hideSoftInputFromWindow(mMessageEditText
				.getWindowToken(), 0);
	}

	protected void voiceRecognition(String selectLang) {
		try {
			String language = Utils.getAccentLanguageFromAbbr(getMyLang());
			if (!Utils.isEmpty(selectLang))
				language = Utils.getAccentLanguageFromAbbr(selectLang);
			startVoiceRecognitionActivity(language);
		} catch (Exception e) {
			Toast.makeText( this, 	getString(R.string.not_support_function,
							getString(R.string.voice_recognition)),
					Toast.LENGTH_SHORT).show();
		}
	}


	public static final String INTENT_EXTRA_USERNAME = ChatActivity.class
			.getName() + ".username";// 昵称对应的key
	protected String mWithJabberID = null;// 当前聊天用户的ID
	protected TranslateClient translateClient;

	protected static final String[] PROJECTION_FROM = new String[]{
			ChatTable.Columns.ID,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.FROM_JID,
			ChatTable.Columns.TO_JID,
			ChatTable.Columns.CONTENT,
			ChatTable.Columns.CONTENT_TYPE,
			ChatTable.Columns.FILE_PATH,
			ChatTable.Columns.VOICE_SECOND,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.MESSAGE_ID,
			ChatTable.Columns.DELIVERY_STATUS,
			ChatTable.Columns.PACKET_ID};// 查询字段
	// 查询联系人数据库字段
	private static final String[] STATUS_QUERY = new String[]{
			ChatRoomTable.Columns.STATUS_MODE,
			ChatRoomTable.Columns.STATUS_MESSAGE,};
	private ContentObserver mFriendUserObserver = new FriendUserObserver();
	private ContentObserver mChatObserver = new ChatObserver();
	private class FriendUserObserver extends ContentObserver {
		public FriendUserObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			updateFriendUserStatus();// 联系人状态变化时，刷新界面
		}
	}
	private class ChatObserver extends ContentObserver {
		public ChatObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			updateChat(reQuery());
		}
	}

	public void updateChat(Cursor c) {
		Cursor oldCursor = tttAdapter.swapCursor(c);
		oldCursor.close();
	}

	protected void updateFriendUserStatus() {
//TODO
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 窗口获取到焦点时绑定服务，失去焦点将解绑
		if (hasFocus)
			App.bindXMPPService();
		else
			App.unbindXMPPService();
	}

	protected void sendMessageIfNotNull(Chat chat) {
		String content = chat.getContent();
		if (content.length() >= 1) {
			if (App.mService != null) {
				if (isGroupChat){
					App.mService.sendGroupMessage(chatRoom, chat);
				}else{
					App.mService.sendMessage(mWithJabberID, chat);
					if (!App.mService.isAuthenticated())
						Toast.makeText(this, "消息已经保存随后发送", Toast.LENGTH_SHORT).show();

				}
			}
			mMessageEditText.setText(null);
		}
		mVoiceRecordButton.setEnabled(true);
	}

}
