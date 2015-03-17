package com.ruptech.chinatalk.ui;

import android.content.ContentValues;
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
import com.ruptech.chinatalk.adapter.TTTAdapter;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.MessageProvider;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.MessageTable;
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
public class TTTActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ TTTActivity.class.getSimpleName();
	private TTTAdapter tttAdapter;

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

	@InjectView(R.id.activity_ttt_message_edittext)
	EditText mMessageEditText;
	@InjectView(R.id.activity_ttt_message_listview)
	ListView mMessageListView;
	@InjectView(R.id.btn_swap)
	ImageView mBtnSwap;

	private String[] mLang1Array;
	@InjectView(R.id.spinner_lang1)
	Spinner mLang1Spinner;

	private String[] mLang2Array;

	@InjectView(R.id.spinner_lang2)
	Spinner mLang2Spinner;

	@InjectView(R.id.item_balance_remind_textview)
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

		setContentView(R.layout.activity_ttt);
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
					Intent intent = new Intent(TTTActivity.this,
							MyWalletActivity.class);
					TTTActivity.this.startActivity(intent);
				}
			}
		});
	}

	@OnClick(R.id.activity_ttt_btn_send)
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
						TTTActivity.this, mLang2Array);
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
					updateTTT(reQuery());
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
				String fromLang = mLang1Array[mLang1Spinner.getSelectedItemPosition()];
				String toLang = mLang2Array[position];
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
				updateTTT(reQuery());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		tttAdapter = new TTTAdapter(this, reQuery());
		mMessageListView.setAdapter(tttAdapter);
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
		App.unbindXMPPService();

		getContentResolver().unregisterContentObserver(tttObserver);
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
//		App.messageDAO.mergeMessage(message);

        ContentValues cv = new ContentValues();
        cv.put(TableContent.MessageTable.Columns.MESSAGE_STATUS, message.getMessage_status());
        cv.put(TableContent.MessageTable.Columns.STATUS_TEXT, message.getStatus_text());

        getContentResolver().update(MessageProvider.CONTENT_URI, cv, TableContent.MessageTable.Columns.ID
                + " = ?  " , new String[]{String.valueOf(message.getId())});

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	void onRequestTranslateSuccess(RequestTranslateTask fsTask) {
//		App.messageDAO.mergeMessage(message);

		if (fsTask.getIsNeedRetrieveUser()) {
			doRetrieveUser(App.readUser().getId());// 回到Setting画面，能够立刻看到balance变化。
		}

		if (BuildConfig.DEBUG)
			Log.d(TAG, "send Success");

	}

	private ContentObserver tttObserver = new TTTObserver();
	private Handler mainHandler = new Handler();

	private class TTTObserver extends ContentObserver {
		public TTTObserver() {
			super(mainHandler);
		}

		public void onChange(boolean selfChange) {
			mainHandler.postDelayed(new Runnable() {
				public void run() {
					updateTTT(reQuery());
				}
			}, 100);
		}
	}

	public void updateTTT(Cursor c) {
		Cursor oldCursor = tttAdapter.swapCursor(c);
		oldCursor.close();
	}

	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(mWithJabberID.hashCode());
		App.bindXMPPService();

		getContentResolver().registerContentObserver(MessageProvider.CONTENT_URI,
				true, tttObserver);
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
		String fromLang = PrefUtils.getPrefTTTLastSelectedLang(1);
		String toLang = PrefUtils.getPrefTTTLastSelectedLang(2);

		//新版本发给旧版本
		Message message = new Message();
		long localId = System.currentTimeMillis();
		message.setId(localId);
		message.setMessageid(localId);
		message.setUserid(App.readUser().getId());
		message.setTo_userid(AppPreferences.TTT_REQUEST_TO_USERID);
		message.setFrom_lang(fromLang);
		message.setTo_lang(toLang);
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

        doSaveLocalAndRequestTranslate(message);

		mMessageEditText.setText(null);
	}

    void doSaveLocalAndRequestTranslate(Message message){
        ContentValues values = new ContentValues();
        values.put(TableContent.MessageTable.Columns.ID, message.getId());
        values.put(TableContent.MessageTable.Columns.MESSAGEID, message.getMessageid());
        values.put(TableContent.MessageTable.Columns.USERID, message.getUserid());
        values.put(TableContent.MessageTable.Columns.TO_USERID, message.getTo_userid());
        values.put(TableContent.MessageTable.Columns.FROM_LANG, message.from_lang);
        values.put(TableContent.MessageTable.Columns.TO_LANG, message.to_lang);
        values.put(TableContent.MessageTable.Columns.FROM_CONTENT, message.from_content);
        values.put(TableContent.MessageTable.Columns.MESSAGE_STATUS, message.getMessage_status());
        values.put(TableContent.MessageTable.Columns.STATUS_TEXT, message.getStatus_text());
        values.put(TableContent.MessageTable.Columns.FILE_PATH, message.getFile_path());
        values.put(TableContent.MessageTable.Columns.FROM_CONTENT, message.getFrom_content());
        values.put(TableContent.MessageTable.Columns.FILE_TYPE, message.getFile_type());
        values.put(TableContent.MessageTable.Columns.CREATE_DATE, message.getCreate_date());
        values.put(TableContent.MessageTable.Columns.UPDATE_DATE, message.getUpdate_date());

        getContentResolver().insert(MessageProvider.CONTENT_URI, values);

        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
                .getFile_type())
                || AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(message
                .getFile_type())) {// 先上传图片或者voice,然后发送请求
            //mMessage = message;
            //doUploadFile(message, mUploadTaskListener);
        } else {
            doRequestTranslate(message);
        }
    }


	protected void switchTextInputMode() {
		mMessageEditText.setVisibility(View.VISIBLE);
		mMessageEditText.requestFocus();
		doClearContent();
	}

	protected String mWithJabberID = "ttt";// 当前聊天用户的ID

	protected static final String[] TTT_MESSAGE_QUERY = new String[]{
			MessageTable.Columns.ID,
			MessageTable.Columns.MESSAGEID,
			MessageTable.Columns.USERID,
			MessageTable.Columns.VIA_TEL,
			MessageTable.Columns.TO_USERID,
			MessageTable.Columns.TRANSLATOR_ID,
			MessageTable.Columns.FROM_LANG,
			MessageTable.Columns.TO_LANG,
			MessageTable.Columns.FROM_VOICE_ID,
			MessageTable.Columns.FROM_CONTENT,
			MessageTable.Columns.FROM_CONTENT_LENGTH,
			MessageTable.Columns.TO_CONTENT,
			MessageTable.Columns.STATUS_TEXT,
			MessageTable.Columns.FEE,
			MessageTable.Columns.TRANSLATE_FEE,
			MessageTable.Columns.AUTO_TRANSLATE,
			MessageTable.Columns.TO_USER_FEE,
			MessageTable.Columns.ACQUIRE_DATE,
			MessageTable.Columns.TRANSLATED_DATE,
			MessageTable.Columns.VERIFY_STATUS,
			MessageTable.Columns.MESSAGE_STATUS,
			MessageTable.Columns.CREATE_ID,
			MessageTable.Columns.CREATE_DATE,
			MessageTable.Columns.UPDATE_ID,
			MessageTable.Columns.UPDATE_DATE,
			MessageTable.Columns.FILE_PATH,
			MessageTable.Columns.FILE_TYPE,
			MessageTable.Columns.DETECT_LANGUAGE
	};

	private Cursor reQuery() {
		String fromLang = PrefUtils.getPrefTTTLastSelectedLang(1);
		String toLang = PrefUtils.getPrefTTTLastSelectedLang(2);

		String selection = MessageTable.Columns.TO_USERID + " = ? and ((" + MessageTable.Columns.FROM_LANG + " = ? and " + MessageTable.Columns.TO_LANG + " = ?)" +
                " or (" + MessageTable.Columns.TO_LANG + " = ? and " + MessageTable.Columns.FROM_LANG + " = ?))";

		Cursor childCursor = getContentResolver().query(MessageProvider.CONTENT_URI,
				TTT_MESSAGE_QUERY, selection, new String[]{String.valueOf(AppPreferences.TTT_REQUEST_TO_USERID), fromLang, toLang, fromLang, toLang}, null);
		return childCursor;
	}

}