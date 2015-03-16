package com.ruptech.chinatalk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.adapter.TTTChatAdapter;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.smack.TTTalkSmackImpl;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestTranslateTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.story.TextShareActivity;
import com.ruptech.chinatalk.ui.user.MyWalletActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.LangSpinnerAdapter;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author zhao
 */
public class ChatTTTActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatTTTActivity.class.getSimpleName();
	private TTTChatAdapter untranslatedMessageListAdapter;

	/**
	 * TTT根据新的lang列表，重新定位之前使用过的lang位置
	 */
	private int getLangArrayPosition(String lang, String[] langArray) {
		int pos = 0;
		for (int i = 0; i < langArray.length; i++) {
			if (lang.equalsIgnoreCase(langArray[i])) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	@InjectView(R.id.activity_ttt_chat_message_edittext)
	EditText mMessageEditText;
	@InjectView(R.id.activity_ttt_chat_message_listview)
	ListView mMessageListView;
	private String fromLang;
	@InjectView(R.id.btn_swap)
	ImageView mBtnSwap;

	private String[] mLang1Array;
	@InjectView(R.id.spinner_lang1)
	Spinner mLang1Spinner;

	private String[] mLang2Array;

	@InjectView(R.id.spinner_lang2)
	Spinner mLang2Spinner;

	private String toLang;

	@InjectView(R.id.item_chatting_balance_remind_textview)
	TextView remindItemFooterTextView;

	@InjectView(R.id.remind_panel)
	View remindItemFooterView;

	private boolean isSelectedLang = false;


	void handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(TextShareActivity.SEND_TEXT);
		if (sharedText != null) {
			switchTextInputMode();
			mMessageEditText.setText(sharedText);
		}
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

		setContentView(R.layout.activity_ttt_chat);
		ButterKnife.inject(this);

		getSupportActionBar().setTitle(R.string.translation_secretary);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mWithJabberID = AppPreferences.TTT_OF_USERNAME;
		setupComponents();
		switchTextInputMode();
		handleSendText(getIntent());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}


	protected void remindFooter() {
		if (!isSelectedLang) {
			remindItemFooterView.setVisibility(View.GONE);
			return;
		}
		remindItemFooterView.setVisibility(View.VISIBLE);
		if (googleTranslate) {
			remindItemFooterTextView.setText(R.string.translate_is_free);
		} else if (App.readUser().getBalance() < AppPreferences.MINI_BALANCE) {
			// 1.自己钱不够，显示
			remindItemFooterTextView.setText(Html
					.fromHtml(getString(R.string.your_balance_is_not_enough)));
		} else if (translatorCount == 0) {
			remindItemFooterTextView.setText(R.string.no_translator_online);// 没有翻译者的时候显示不能进行翻译
		} else {
			remindItemFooterTextView.setText(R.string.normal_translation);// 没有翻译者的时候显示不能进行翻译
		}

		remindItemFooterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (App.readUser().getBalance() < AppPreferences.MINI_BALANCE) {
					Intent intent = new Intent(ChatTTTActivity.this,
							MyWalletActivity.class);
					ChatTTTActivity.this.startActivity(intent);
				}
			}
		});
	}

	@OnClick(R.id.activity_ttt_chat_btn_send)
	public void send_ttt_text(View v) {
		send_text();
	}

	private void setupComponents() {
		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		int lang1SpinnerSelectPos = 0;

		mLang1Array = Utils.getServerTransLang();
		LangSpinnerAdapter lang1Adapter = new LangSpinnerAdapter(this,
				mLang1Array);
		mLang1Spinner.setAdapter(lang1Adapter);
		String lastSelectedLang1 = PrefUtils.getPrefTTTLastSelectedLang(1);
		if (!Utils.isEmpty(lastSelectedLang1)) {
			lang1SpinnerSelectPos = getLangArrayPosition(lastSelectedLang1,
					mLang1Array);
		} else {
			String userLanguage = Utils.getUserLanguage();
			for (int i = 0; i < mLang1Array.length; i++) {
				if (mLang1Array[i].equalsIgnoreCase(userLanguage)) {
					lang1SpinnerSelectPos = i;
					break;
				}
			}
		}
		mLang1Spinner.setSelection(lang1SpinnerSelectPos);
		mLang1Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
			                           int position, long id) {
				mLang2Array = Utils
						.getAvailableLang2ByLang1(mLang1Array[position]);
				LangSpinnerAdapter lang2Adapter = new LangSpinnerAdapter(
						ChatTTTActivity.this, mLang2Array);
				mLang2Spinner.setAdapter(lang2Adapter);
				String lastSelectedLang2 = PrefUtils
						.getPrefTTTLastSelectedLang(2);
				if (lastSelectedLang2.equals(mLang1Array[position])) {
					lastSelectedLang2 = PrefUtils.getPrefTTTLastSelectedLang(1);
				}
				if (!Utils.isEmpty(lastSelectedLang2)) {
					mLang2Spinner.setSelection(getLangArrayPosition(
							lastSelectedLang2, mLang2Array));
				} else {
					updateUntranslate(reQuery());
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});

		mLang2Spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
			                           int position, long id) {
				fromLang = mLang1Array[mLang1Spinner.getSelectedItemPosition()];
				toLang = mLang2Array[position];
				PrefUtils.savePrefTTTLastSelectedLang(1, fromLang);
				PrefUtils.savePrefTTTLastSelectedLang(2, toLang);


				googleTranslate = Utils.isGoogleTranslate(fromLang, toLang);

				translatorCount = Utils.getStoreTranslatorCount(fromLang,
						toLang);
				isSelectedLang = true;// refresh remindFooter
				remindFooter();
				if (googleTranslate) {
					switchTextInputMode();
				}
				updateUntranslate(reQuery());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		untranslatedMessageListAdapter = new TTTChatAdapter(this,
				reQuery());
		mMessageListView.setAdapter(untranslatedMessageListAdapter);
		mMessageListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
			                     int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				mInputMethodManager.hideSoftInputFromWindow(
						mMessageEditText.getWindowToken(), 0);
			}

		});

		mMessageEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mMessageListView.setSelection(mMessageListView.getCount() - 1);
			}

		});

		InputFilter[] filters = {new Utils.LengthFilter()};
		mMessageEditText.setFilters(filters);

		mBtnSwap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String lastSelectedLang2 = PrefUtils
						.getPrefTTTLastSelectedLang(2);
				int position = getLangArrayPosition(lastSelectedLang2,
						mLang1Array);
				mLang1Spinner.setSelection(position);
			}
		});
	}

	static int onPage;

	public void doRequestTranslate(Message message) {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "doRequestTranslate");

		GenericTask mRequestTranslateTask = new RequestTranslateTask(message);
		mRequestTranslateTask.setListener(mRequestTranslateListener);
		mRequestTranslateTask.execute();
	}


	protected boolean googleTranslate;

	InputMethodManager mInputMethodManager;

	private final TaskListener mRequestTranslateListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RequestTranslateTask fsTask = (RequestTranslateTask) task;
			if (result == TaskResult.OK) {
				onRequestTranslateSuccess(fsTask);
			} else {
				String msg = fsTask.getMsg();
				Message message = fsTask.getMessage();
				onRequestTranslateFailure(message, msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRequestTranslateBegin();
		}

	};

	protected int translatorCount = 0;


	protected void doClearContent() {
		mMessageEditText.setText("");
	}


	private void doRetrieveUser(long userId) {

		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(userId);
		mRetrieveUserTask.execute();
	}


	@Override
	public void finish() {
		super.finish();
	}

	private void gotoSplashActivity() {
		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
	}


	@Override
	protected void onDestroy() {
		long start = System.currentTimeMillis();
		super.onDestroy();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onDestroy:" + (System.currentTimeMillis() - start));
	}

	@Override
	public void onPause() {
		super.onPause();
//		App.mBadgeCount.removeNewMessageCount(getFriendUserId());
		// PrefUtils.removePrefNewMessageCount(getFriendUserId());// 按 Home ||

		getContentResolver().unregisterContentObserver(mUntranslateObserver);
	}

	void onRequestTranslateBegin() {
		mMessageListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		mMessageEditText.selectAll();
		switchTextInputMode();
	}

	void onRequestTranslateFailure(Message message, String msg) {
		// save message
		message.setMessage_status(AppPreferences.MESSAGE_STATUS_SEND_FAILED);
		message.setStatus_text(getString(R.string.message_action_click_resend));
		App.messageDAO.mergeMessage(message);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	void onRequestTranslateSuccess(RequestTranslateTask fsTask) {
		Message message = fsTask.getMessage();
		//chat
		Chat chat = new Chat();
		chat.setMessage(message.getFrom_content());
		chat.setType(message.getFile_type());
		chat.setFromContentLength(message.from_content_length);
		chat.setFilePath(message.getFile_path());

		chat.setFromMe(ChatProvider.OUTGOING);
		chat.setJid(mWithJabberID);
		chat.setPid(null);
		chat.setDate(System.currentTimeMillis());


		TTTalkSmackImpl.addChatMessageToDB(getContentResolver(), chat);

		if (fsTask.getIsNeedRetrieveUser()) {
			doRetrieveUser(App.readUser().getId());// 回到Setting画面，能够立刻看到balance变化。
		}

		if (BuildConfig.DEBUG)
			Log.d(TAG, "send Success");

	}

	private ContentObserver mUntranslateObserver = new ttChatObserver();
	private Handler mainHandler = new Handler();

	private class ttChatObserver extends ContentObserver {
		public ttChatObserver() {
			super(mainHandler);
		}

		public void onChange(boolean selfChange) {
			mainHandler.postDelayed(new Runnable() {
				public void run() {
					updateUntranslate(reQuery());
				}
			}, 100);
		}
	}

	public void updateUntranslate(Cursor c) {
		Cursor oldCursor = untranslatedMessageListAdapter.swapCursor(c);
		oldCursor.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(mWithJabberID.hashCode());

		getContentResolver().registerContentObserver(ChatProvider.CONTENT_URI,
				true, mUntranslateObserver);
	}


	void send_text() {
		String content = mMessageEditText.getText().toString().trim();
		// reform
		if (content.replaceAll("\n", "").length() == 0) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		// check
		if (content.length() == 0) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String[] msgArray = content.split("]");
		for (String msg : msgArray) {
			if (!ParseEmojiMsgUtil.checkMsgFace(msg + "]")) {
				break;
			}
		}
		content = ParseEmojiMsgUtil.convertToMsg(
				mMessageEditText.getText(), this);
		sendText(content);

	}

	private void sendText(String content) {
		int contentLength = Utils.textLength(content);
		String fileType = AppPreferences.MESSAGE_TYPE_NAME_TEXT;

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
		message.setFile_path(null);
		message.setFrom_content_length(contentLength);
		message.setFile_type(fileType);
		String createDateStr = DateCommonUtils.getUtcDate(new Date(),
				DateCommonUtils.DF_yyyyMMddHHmmssSSS);
		message.create_date = createDateStr;
		message.update_date = createDateStr;
		doRequestTranslate(message);

		mMessageEditText.setText(null);

	}


	protected void switchTextInputMode() {
		mMessageEditText.setVisibility(View.VISIBLE);
		mMessageEditText.requestFocus();
		doClearContent();
	}

	protected String mWithJabberID = "ttt";// 当前聊天用户的ID

	protected static final String[] TTT_CHAT_QUERY = new String[]{
			ChatTable.Columns.ID,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.FROM_JID,
			ChatTable.Columns.TO_JID,
			ChatTable.Columns.CONTENT,
			ChatTable.Columns.CONTENT_TYPE,
			ChatTable.Columns.FILE_PATH,
			ChatTable.Columns.VOICE_SECOND,
			ChatTable.Columns.TO_MESSAGE,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.MESSAGE_ID,
			ChatTable.Columns.DELIVERY_STATUS,
			ChatTable.Columns.PACKET_ID};// 查询字段


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// 窗口获取到焦点时绑定服务，失去焦点将解绑
		if (hasFocus)
			App.bindXMPPService();
		else
			App.unbindXMPPService();
	}

	private Cursor reQuery() {
		//TODO
		Cursor childCursor = getContentResolver().query(ChatProvider.CONTENT_URI,
				TTT_CHAT_QUERY, null, null, null);
		return childCursor;
	}

}