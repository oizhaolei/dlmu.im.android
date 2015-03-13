package com.ruptech.chinatalk.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.db.RosterProvider;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.smack.TTTalkSmackImpl;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.RosterTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.task.impl.RequestTranslateTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.StatusMode;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.RecordButton.OnFinishedRecordListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * 
 * @author
 */
public abstract class AbstractChatActivity extends ActionBarActivity {

	public static class LengthFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			int sourceLen = source.toString().length();
			int destLen = dest.toString().length();
			int cjkCharCount = getCJKCharCount(source.toString())
					+ getCJKCharCount(dest.toString());

			int strLen = (sourceLen + destLen - cjkCharCount + cjkCharCount * 3);
			if (strLen > AppPreferences.MAX_INPUT_LENGTH) {
				return "";
			}
			return source;
		}
	}

	public static class TranslateLengthFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			int sourceLen = source.toString().length();
			int destLen = dest.toString().length();
			if (sourceLen + destLen > AppPreferences.MAX_TRANSLATE_INPUT_LENGTH) {
				return "";
			}
			return source;
		}
	}

    public User mFriendUser;

	static final int CHANGE_NICKNAME = 5678;

	static final int CHAT_RETURN_PHOTO = 4321;

    public static final String EXTRA_JID = "EXTRA_JID";

	static final int FRIEND_BLACK = 7856;

	public static AbstractChatActivity instance = null;

	private static GenericTask mUploadTask;

	static int onPage;

	static final String TAG = Utils.CATEGORY
			+ AbstractChatActivity.class.getSimpleName();

	static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

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
				new UploadProgress() {
					@Override
					public void onUpload(long uploaded, long total) {
						Log.d(TAG, uploaded + " - " + total);
					}
				});
		mUploadTask.setListener(mUploadTaskListener);
		mUploadTask.execute();
	}

	public static int getCJKCharCount(String text) {
		int count = 0;
		for (char c : text.toCharArray()) {
			if ((Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES)
					|| (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS)) {
				count++;
			}
		}

		return count;
	}

	public static void refreshFooterBySelectLang() {
		if (instance != null) {
			instance.doRefleshFooterBySelectLang();
		}
	}

	protected boolean existStore;

	protected boolean googleTranslate;

	// view是否显示的标识
	boolean loadMoreFlag = true;

	protected Friend mFriend;

	protected boolean isFriend = true;

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message message = (Message) intent.getExtras().getSerializable(
					CommonUtilities.EXTRA_MESSAGE);
			if (BuildConfig.DEBUG)
				Log.d(TAG, "received message:" + message);
			getMessageListView().setTranscriptMode(
					ListView.TRANSCRIPT_MODE_NORMAL);
			doRefresh();
		}
	};

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
                        mChat.setMessage(getString(R.string.notification_voice));
                    else
                        mChat.setMessage(getString(R.string.notification_picture));
                    
                    sendMessageIfNotNull(mChat);
                }
			} else if (result == TaskResult.FAILED) {
				String msg = photoTask.getMsg();
//				onRequestTranslateFailure(mChat, msg);
                if (!Utils.isEmpty(msg)) {
                    Toast.makeText(AbstractChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	File mVoiceFile;

	// 黑屏的时候需要做出相应，以便提醒功能
	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				AbstractChatActivity.instance = null;
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				AbstractChatActivity.instance = AbstractChatActivity.this;
			}
		}
	};

	protected int translatorCount = 0;

	OnFinishedRecordListener voiceRecordListener = new OnFinishedRecordListener() {
		@Override
		public void onFinishedRecord(File audioFile) {
			if (BuildConfig.DEBUG)
				Log.v(TAG, "onFinishedRecord");
			if (audioFile.exists()) {
				mVoiceFile = audioFile;

				sendVoice();
			} else {
				getVoiceRecordButton().setText(R.string.press_to_record);
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
				getSendButton().setVisibility(View.VISIBLE);
				getMessageTypeButton().setVisibility(View.INVISIBLE);
			} else {
				getMessageTypeButton().setVisibility(View.VISIBLE);
				getSendButton().setVisibility(View.GONE);
			}
		}

	};

	abstract void displayFriend();

	protected void doClearContent() {
		getMessageEditText().setText("");
		mVoiceFile = null;
		mPhotoFile = null;
	}

	abstract void doRefleshFooterBySelectLang();

	private void doRefresh() {
        updateContactStatus();// 更新联系人状态

		displayFriend();
		remindFooter();
	}

	private void doRetrieveUser(long userId) {

		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(userId);
		mRetrieveUserTask.execute();
	}

	protected void doUpload(String content, String fromLang,
			String toLang, Long toUserId, String file_path, int contentLength,
			String filetype) {
        Chat chat = new Chat();
        chat.setFromLang(fromLang);
        chat.setToLang(toLang);
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

	abstract String getFriendLang();

	public abstract long getFriendUserId();

	abstract View getKeyboardButton();

	abstract EditText getMessageEditText();

	abstract ListView getMessageListView();

	abstract View getMessageTypeButton();

	abstract String getMyLang();

	// 获取下一页的数据
	Cursor getPreviousChatsCursor() {
		onPage = onPage + 1;
		Cursor chatsCursor = App.messageDAO.fetchMessages(App.readUser()
				.getId(), getFriendUserId(), onPage);
		return chatsCursor;
	}

	abstract View getSendButton();

	abstract View getVoiceButton();

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

	abstract TextView getVoiceRecordButton();

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
										getMessageEditText().setText(
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
                RosterProvider.CONTENT_URI, true, mContactObserver);// 开始监听联系人数据库

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				CommonUtilities.CONTENT_MESSAGE_ACTION));
		registerReceiver(screenOffReceiver, new IntentFilter(
				Intent.ACTION_SCREEN_ON));
		registerReceiver(screenOffReceiver, new IntentFilter(
				Intent.ACTION_SCREEN_OFF));
	}

	@Override
	protected void onDestroy() {
		long start = System.currentTimeMillis();
		instance = null;
		try {
			unregisterReceiver(screenOffReceiver);
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
        if (client != null) {
            client.onDestroy();
        }
		super.onDestroy();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onDestroy:" + (System.currentTimeMillis() - start));
	}

    @Override
	public void onPause() {
		long start = System.currentTimeMillis();
		super.onPause();
		App.mBadgeCount.removeNewMessageCount(getFriendUserId());
		// PrefUtils.removePrefNewMessageCount(getFriendUserId());// 按 Home ||
		// Back
		instance = null;
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onPause:" + (System.currentTimeMillis() - start));
	}

	void onRequestTranslateBegin() {
		getMessageListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		getMessageEditText().selectAll();
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
		instance = this;
		App.notificationManager.cancel(Long.valueOf(getFriendUserId())
				.intValue());
		doRefresh();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onResume:" + (System.currentTimeMillis() - start));
	}

	abstract void remindFooter();

	void send_text() {
		boolean noTranslate = true;
		String content = getMessageEditText().getText().toString().trim();
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
				getMessageEditText().getText(), this);
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
			String toLang = getFriendLang();
			Long toUserId = getFriendUserId();
			String content = null;

			String file_path = mPhotoFile.getAbsolutePath();
			int contentLength = 0;
			String filetype = AppPreferences.MESSAGE_TYPE_NAME_PHOTO;

            doUpload(content, fromLang, toLang, toUserId,
					file_path, contentLength, filetype);

		}
	}

	private void sendText(String content, boolean noTranslate) {
		String fromLang = getMyLang();
		if (noTranslate) {
			fromLang = getFriendLang();
		}
		String toLang = getFriendLang();
		Long toUserId = getFriendUserId();

		String file_path = null;
		int contentLength = Utils.textLength(content);
		String filetype = AppPreferences.MESSAGE_TYPE_NAME_TEXT;

        Chat chat = new Chat();
        chat.setMessage(content);
        chat.setFromLang(fromLang);
        chat.setToLang(toLang);
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
            message.setTo_userid(toUserId);
            message.setFrom_lang(fromLang);
            message.setTo_lang(toLang);
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
            chat.setFromMe(ChatProvider.OUTGOING);
            chat.setJid(mWithJabberID);
            chat.setPid(null);
            chat.setDate(System.currentTimeMillis());


            TTTalkSmackImpl.addChatMessageToDB(getContentResolver(), chat);
            getMessageEditText().setText(null);
        }else{
            //新版本发给新版本
            sendMessageIfNotNull(chat);
        }

	}

	private void sendVoice() {
		String fromLang = getMyLang();
		String toLang = getFriendLang();
		Long toUserId = getFriendUserId();
		String content = null;

		String file_path = mVoiceFile.getName();
		int contentLength = getVoiceLength(this, mVoiceFile);
		String filetype = AppPreferences.MESSAGE_TYPE_NAME_VOICE;

        doUpload(content, fromLang, toLang, toUserId,
				file_path, contentLength, filetype);
	}

	protected void setupComponents() {

		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
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
		getVoiceRecordButton().setVisibility(View.GONE);
		getMessageEditText().setVisibility(View.VISIBLE);
		getSendButton().setVisibility(View.GONE);
		getMessageEditText().requestFocus();
		getKeyboardButton().setVisibility(View.GONE);
		if (!googleTranslate && isFriend) {
			getVoiceButton().setVisibility(View.VISIBLE);
		}
		doClearContent();
	}

	protected void switchVoiceMode() {
		getVoiceRecordButton().setVisibility(View.VISIBLE);
		getVoiceRecordButton().setEnabled(true);
		getVoiceRecordButton().setText(R.string.press_to_record);
		getMessageEditText().setVisibility(View.GONE);
		getSendButton().setVisibility(View.GONE);
		getKeyboardButton().setVisibility(View.VISIBLE);
		getVoiceButton().setVisibility(View.INVISIBLE);

		doClearContent();
		mInputMethodManager.hideSoftInputFromWindow(getMessageEditText()
				.getWindowToken(), 0);
	}

	protected void voiceRecognition(String selectLang) {
		try {
			String language = Utils.getAccentLanguageFromAbbr(getMyLang());
			if (!Utils.isEmpty(selectLang))
				language = Utils.getAccentLanguageFromAbbr(selectLang);
			startVoiceRecognitionActivity(language);
		} catch (Exception e) {
			Toast.makeText(
					instance,
					getString(R.string.not_support_function,
							getString(R.string.voice_recognition)),
					Toast.LENGTH_SHORT).show();
		}
	}


    public static final String INTENT_EXTRA_USERNAME = ChatActivity.class
            .getName() + ".username";// 昵称对应的key
    protected String mWithJabberID = null;// 当前聊天用户的ID
    protected TranslateClient client;

    protected static final String[] PROJECTION_FROM = new String[]{
            TableContent.ChatTable.Columns.ID,
            TableContent.ChatTable.Columns.DATE,
            TableContent.ChatTable.Columns.DIRECTION,
            TableContent.ChatTable.Columns.JID,
            TableContent.ChatTable.Columns.MESSAGE,
            TableContent.ChatTable.Columns.TYPE,
            TableContent.ChatTable.Columns.FILE_PATH,
            TableContent.ChatTable.Columns.CONTENT_LENGTH,
            TableContent.ChatTable.Columns.TO_MESSAGE,
            TableContent.ChatTable.Columns.DATE,
            TableContent.ChatTable.Columns.MESSAGE_ID,
            TableContent.ChatTable.Columns.DELIVERY_STATUS,
            TableContent.ChatTable.Columns.PACKET_ID};// 查询字段
    // 查询联系人数据库字段
    private static final String[] STATUS_QUERY = new String[]{
            RosterTable.Columns.STATUS_MODE,
            RosterTable.Columns.STATUS_MESSAGE,};
    private ContentObserver mContactObserver = new ContactObserver();// 联系人数据监听，主要是监听对方在线状态
    /**
     * 联系人数据库变化监听
     */
    private class ContactObserver extends ContentObserver {
        public ContactObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Log.d(TAG, "ContactObserver.onChange: " + selfChange);
            updateContactStatus();// 联系人状态变化时，刷新界面
        }
    }

    protected void updateContactStatus() {
        Cursor cursor = getContentResolver().query(RosterProvider.CONTENT_URI,
                STATUS_QUERY, RosterTable.Columns.JID + " = ?",
                new String[]{mWithJabberID}, null);
        int MODE_IDX = cursor
                .getColumnIndex(RosterTable.Columns.STATUS_MODE);
        int MSG_IDX = cursor
                .getColumnIndex(RosterTable.Columns.STATUS_MESSAGE);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            int status_mode = cursor.getInt(MODE_IDX);
            String status_message = cursor.getString(MSG_IDX);
            Log.d(TAG, "contact status changed: " + status_mode + " " + status_message);
            getSupportActionBar().setTitle(XMPPUtils.splitJidAndServer(getIntent()
                    .getStringExtra(INTENT_EXTRA_USERNAME)));
            int statusId = StatusMode.values()[status_mode].getDrawableId();
            if (statusId != -1) {// 如果对应离线状态
                // Drawable icon = getResources().getDrawable(statusId);
                // mTitleNameView.setCompoundDrawablesWithIntrinsicBounds(icon,
                // null,
                // null, null);
            } else {
            }
        }
        cursor.close();
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
        String content = chat.getMessage();
        if (content.length() >= 1) {
            if (App.mService != null) {
                App.mService.sendMessage(mWithJabberID, chat);
                if (!App.mService.isAuthenticated())
                    Toast.makeText(this, "消息已经保存随后发送", Toast.LENGTH_SHORT).show();
            }
            getMessageEditText().setText(null);
        }
        getVoiceRecordButton().setEnabled(true);
    }
}