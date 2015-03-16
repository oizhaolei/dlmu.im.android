package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
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
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.adapter.ChatAdapter;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.ui.setting.ChatSettingActivity;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.ui.story.TextShareActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper;
import com.ruptech.chinatalk.utils.face.SelectFaceHelper.OnFaceOprateListener;
import com.ruptech.chinatalk.widget.EditTextWithFace;
import com.ruptech.chinatalk.widget.RecordButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 
 * @author
 */
public class ChatActivity extends AbstractChatActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatActivity.class.getSimpleName();

    public static final String GROUP_CHAT_SUFFIX = "@conference.tttalk.org";
//	private User mFriendUser;
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

//	private final TaskListener mRetrieveUserListener = new TaskAdapter() {
//
//		@Override
//		public void onPostExecute(GenericTask task, TaskResult result) {
//			RetrieveUserTask retrieveUserTask = (RetrieveUserTask) task;
//			if (result == TaskResult.OK) {
//				if (retrieveUserTask.getUser() != null) {
//					mFriendUser = retrieveUserTask.getUser();
//					displayFriend();
//					remindFooter();
//				}
//			} else {
//				String msg = retrieveUserTask.getMsg();
//				onRetrieveUserTaskFailure(msg);
//			}
//		}
//
//	};

	private SelectFaceHelper mFaceHelper;

	private boolean isVisbilityFace = false;

	@InjectView(R.id.add_face_tool_layout)
	View mAddFaceToolView;

	@InjectView(R.id.item_chatting_balance_remind_textview)
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

	@Override
	protected void displayFriend() {
//		if (App.readUser().getLang().equals(mFriendUser.getLang())) {
//			mMessageEditText.setHint("");
//		} else {
//			String fromLangName = Utils.getLangDisplayName(App.readUser()
//					.getLang());
//			String toLangName = Utils.getLangDisplayName(mFriendUser.getLang());
//			if (fromLangName != null && toLangName != null) {
//				mMessageEditText.setHint(getString(
//						R.string.chat_language_from_to, fromLangName,
//						toLangName));
//			}
//		}
//		if (null == mFriend || Utils.isEmpty(mFriend.getFriend_nickname())) {
//			getSupportActionBar().setTitle(mFriendUser.getFullname());
//		} else {
//			getSupportActionBar().setTitle(mFriend.getFriend_nickname());
//		}

	}

	@Override
	public void doRefleshFooterBySelectLang() {
		getInitData();
		initInputPanel();
		remindFooter();
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

	@Override
	View getKeyboardButton() {
		return keyboardTypeButton;
	}

	@Override
	EditText getMessageEditText() {
		return mMessageEditText;
	}

	@Override
	ListView getMessageListView() {
		return mMessageListView;
	}

	@Override
	View getMessageTypeButton() {
		return mMessageTypeButton;
	}

	@Override
	String getMyLang() {
		return App.readUser().getLang();
	}

	@Override
	View getSendButton() {
		return mSendButton;
	}

    private void parseExtras(){
        mWithJabberID = (String) getIntent().getExtras().get(EXTRA_JID);
        isGroupChat = Utils.isGroupChat(mWithJabberID);
        if (isGroupChat){
            chatRoom = App.mSmack.createChatRoomByRoomName(mWithJabberID);
            if (chatRoom == null){
                Toast.makeText(this, "Can not join to chat room",
					Toast.LENGTH_LONG).show();
                finish();
            }
        }
//        mFriendUser = App.userDAO.fetchUser(Utils.getTTTalkIDFromOF_JID(mWithJabberID));
    }

	@Override
	View getVoiceButton() {
		return voiceTypeButton;
	}

	@Override
	TextView getVoiceRecordButton() {
		return mVoiceRecordButton;
	}

	private void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(TextShareActivity.SEND_TEXT);
		if (sharedText != null) {
			switchTextInputMode();
			mMessageEditText.setText(sharedText);
		}
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

	private void initInputPanel() {
//		isFriend = false;// 对方加我为好友后,我才可以给他发图片\语音
//		if (mFriendUser != null) {
//			Friend friendUserInfo = App.friendDAO.fetchFriend(
//					mFriendUser.getId(), App.readUser().getId());
//			if (friendUserInfo != null
//					&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS) {
//				isFriend = true;
//			}
//		}
//
//		boolean enableVoice = !googleTranslate && isFriend;
//		voiceTypeButton.setVisibility(enableVoice ? View.VISIBLE : View.GONE);
//
//		boolean isFromTTTChat = false;
//		boolean enablePicture = !isFromTTTChat && (mFriendUser.active == 1)
//				&& isFriend;
//		pictureTypeButton.setVisibility(enablePicture ? View.VISIBLE
//				: View.GONE);

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

    // 【重要】 onCreate时候初始化翻译相关功能
    private void initTransClient() {
        String baidu_api_key = App.properties.getProperty("baidu_api_key");

        client = new TranslateClient(this, baidu_api_key);

        // 这里可以设置为在线优先、离线优先、 只在线、只离线 4种模式，默认为在线优先。
        client.setPriority(TranslateClient.Priority.OFFLINE_FIRST);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		long start = System.currentTimeMillis();
		super.onCreate(savedInstanceState);
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
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onCreate:" + (System.currentTimeMillis() - start));
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

        updateContactStatus();
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

	@Override
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

	private void retrieveFriendUser() {
//		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(
//				mFriendUser.getId());
//		mRetrieveUserTask.setListener(mRetrieveUserListener);
//		mRetrieveUserTask.execute();
//
//		RetrieveServerVersionTask mRetrieveServerVersionTask = new RetrieveServerVersionTask();
//		mRetrieveServerVersionTask.execute();
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
			hideInputManager(this);
		}
	}

	@OnClick(R.id.activity_chat_message_type_button)
	public void selectMessageType(View v) {
		if (mSelectMessageTypeView.getVisibility() == View.GONE) {
			this.mSelectMessageTypeView.setVisibility(View.VISIBLE);
			mAddFaceToolView.setVisibility(View.GONE);
			hideInputManager(this);
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

	@Override
	public void setupComponents() {
		super.setupComponents();

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

		displayFriend();
	}

    /**
     * 设置聊天的Adapter
     */
    private void setChatWindowAdapter() {
        String selection = TableContent.ChatTable.Columns.JID + "='" + mWithJabberID + "'";
        // 异步查询数据库
        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                // ListAdapter adapter = new ChatWindowAdapter(cursor,
                // PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
                ListAdapter adapter = new ChatAdapter(ChatActivity.this,
                        cursor, PROJECTION_FROM, client);
                getMessageListView().setAdapter(adapter);
                getMessageListView().setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
                selection, null, null);
    }


}
