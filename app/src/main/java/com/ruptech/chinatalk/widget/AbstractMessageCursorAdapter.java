package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask;
import com.ruptech.chinatalk.task.impl.RequestTranslateTask;
import com.ruptech.chinatalk.task.impl.RequestVerifyTask;
import com.ruptech.chinatalk.task.impl.RetrieveMessageTask;
import com.ruptech.chinatalk.task.impl.TranslateAcceptTask;
import com.ruptech.chinatalk.ui.AbstractChatActivity;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.setting.WebViewActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;

public abstract class AbstractMessageCursorAdapter extends CursorAdapter {
	static class MyHandler extends Handler {
		private final ImageView mVoiceImageView;
		private final ProgressBar mPlayProcessBar;

		MyHandler(ImageView voiceImageView, ProgressBar playProcessBar) {
			mVoiceImageView = voiceImageView;
			mPlayProcessBar = playProcessBar;
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			String returnUrl = msg.getData().getString("url");
			if (!Utils.isEmpty(returnUrl)) {
				playVoice(returnUrl, mVoiceImageView, mPlayProcessBar);
			} else {
				Toast.makeText(App.mContext, R.string.file_not_found,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static void playVoice(String url, final ImageView voiceImageView,
			final ProgressBar playProcessBar) {
		if (BuildConfig.DEBUG)
			Log.w(TAG, "url:" + url);
		try {
			if (App.mPlayer != null) {
				App.mPlayer.release();
				App.mPlayer = null;
			}
			App.mPlayer = new MediaPlayer();
			App.mPlayer.setDataSource(url);
			App.mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			App.mPlayer.prepare();
			App.mPlayer.start();

			voiceImageView.setVisibility(View.GONE);
			playProcessBar.setVisibility(View.VISIBLE);

			App.mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mPlayer) {
					voiceImageView.setVisibility(View.VISIBLE);
					playProcessBar.setVisibility(View.GONE);
					App.mPlayer.stop();
					App.mPlayer.release();
				}
			});
		} catch (Exception e) {
			voiceImageView.setVisibility(View.VISIBLE);
			playProcessBar.setVisibility(View.GONE);
			Utils.sendClientException(e, url);
			if (BuildConfig.DEBUG)
				Log.e(TAG, url, e);
		}
	};

	private ArrayList<String> chatPhotoList;

	private GenericTask mRequestVerifyTask;
	int CREATE_DATE_INDEX;

	LayoutInflater mInflater;
	private GenericTask mAcceptTranslateTask;

	private GenericTask retrieveMessageTask;

	public int voice_playing_width;

	private Message mMessage;// upload 图片时使用

	private final TaskListener mRequestVerifyListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				Toast.makeText(getContext(), R.string.send_request_success,
						Toast.LENGTH_SHORT).show();

			} else {
				String msg = task.getMsg();

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			Toast.makeText(getContext(), R.string.message_verify_processing,
					Toast.LENGTH_SHORT).show();
		}

	};

	private final TaskListener mAcceptTranslateListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			TranslateAcceptTask fsTask = (TranslateAcceptTask) task;
			if (result == TaskResult.OK) {
				boolean existTranslatedMessage = fsTask.getIsNeedRetrieveUser();
				onAcceptTranslateSuccess(existTranslatedMessage);

				Toast.makeText(getContext(), R.string.send_request_success,
						Toast.LENGTH_SHORT).show();

			} else {
				String msg = fsTask.getMsg();
				if (!Utils.isEmpty(msg)) {
					Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT)
							.show();
				}
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			Toast.makeText(getContext(), R.string.message_sending,
					Toast.LENGTH_SHORT).show();
		}

	};
	static final String TAG = Utils.CATEGORY
			+ AbstractMessageCursorAdapter.class.getSimpleName();
	private Context context;

	private final TaskListener mRequestTranslateListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onRequestTranslateSuccess();
			} else {
				RequestTranslateTask fsTask = (RequestTranslateTask) task;
				String msg = fsTask.getMsg();
				Message message = fsTask.getMessage();
				onRequestTranslateFailure(message, msg);
			}
		}

	};

	private final TaskListener retrieveMessageByIdTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveMessageTask retrieveMessageTask = (RetrieveMessageTask) task;
			if (result == TaskResult.OK) {
				Message message = retrieveMessageTask.getMessage();

				CommonUtilities.messageNotification(App.mContext, message);
			} else {
				String msg = retrieveMessageTask.getMsg();
				if (BuildConfig.DEBUG)
					Log.d(TAG, "retrieveMessageTask:" + msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	private final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FileUploadTask photoTask = (FileUploadTask) task;
			if (result == TaskResult.OK) {
				Message message = mMessage;
				message.setFile_path(photoTask.getFileInfo().fileName);
				AbstractChatActivity.doRequestTranslate(message,
						mRequestTranslateListener);
			} else if (result == TaskResult.FAILED) {
				String msg = photoTask.getMsg();
				Message message = mMessage;
				onRequestTranslateFailure(message, msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	public int mWidth;

	public AbstractMessageCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		this.setContext(context);
	}

	protected void bindAutoTranslationClick(final Message message,
			final View autoTranslationLayout, final Context mContext) {
		autoTranslationLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Map<String, String> params = new HashMap<>();
				params.put("message_id", String.valueOf(message.getMessageid()));
				params = HttpConnection.genParams(params);
				String mUrl = App.getHttpServer().genRequestURL(
						"auto_translation_help.php", params);

				Intent intent = new Intent(mContext, WebViewActivity.class);
				intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_URL, mUrl);
				intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_TITLE, mContext
						.getResources().getString(R.string.help));
				mContext.startActivity(intent);
			}
		});
	}

	protected void bindDateTimeView(Cursor cursor, final TextView dateTextView) {
		String pubDate = cursor.getString(CREATE_DATE_INDEX);
		String prevPubDate;
		if (cursor.moveToPrevious()) {
			prevPubDate = cursor.getString(CREATE_DATE_INDEX);
			cursor.moveToNext();
		} else {
			prevPubDate = null;
		}

		dateTextView.setVisibility(View.GONE);
		if (prevPubDate != null && pubDate != null) {
			boolean isDiff = DateCommonUtils.chatDiffTime(prevPubDate, pubDate);
			if (isDiff) {
				String text = pubDate;

				if (text != null) {
					dateTextView.setText(DateCommonUtils
							.formatConvUtcDateString(text, true, false));
				}

				dateTextView.setVisibility(View.VISIBLE);
			}
		} else if (Utils.isEmpty(prevPubDate) && !Utils.isEmpty(pubDate)) {
			dateTextView.setText(DateCommonUtils.formatConvUtcDateString(
					pubDate, true, true));
			dateTextView.setVisibility(View.VISIBLE);
		}
	}

	protected void bindFromClickEvent(final Message message, View fromTextView) {
		if (fromTextView == null)
			return;

		fromTextView.setOnLongClickListener(null);
		fromTextView
				.setOnLongClickListener(getFromContentLongClickListener(message));
	}

	protected void bindFromContentView(final Message message,
			final TextView rightFromContentTextView) {
		if (rightFromContentTextView == null)
			return;

		rightFromContentTextView.setVisibility(View.VISIBLE);
		rightFromContentTextView.setMovementMethod(LinkMovementMethod
				.getInstance());
		String unicode = EmojiParser.getInstance(getContext()).parseEmoji(
				message.getFrom_content());
		SpannableString spannableString = ParseEmojiMsgUtil
				.getExpressionString(getContext(), unicode);
		if (spannableString != null) {
			rightFromContentTextView.setText(spannableString);
		} else {
			rightFromContentTextView.setText(message.getFrom_content());
		}
	}

	protected void bindFromPhotoView(final Message message,
			ImageView photoImageView, ProgressBar progressBar) {
		if (photoImageView == null)
			return;

		photoImageView.setVisibility(View.VISIBLE);
		final String file_path = message.file_path;
		String fileName = file_path.substring(file_path.lastIndexOf('/') + 1);
		if (progressBar != null)
			progressBar.setVisibility(View.GONE);
		if (!Utils.isEmpty(fileName)) {
			if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE) {
				String tag = (String) photoImageView.getTag();
				if (!fileName.equals(tag)) {
					ImageManager.imageLoader.displayImage(App
							.readServerAppInfo().getServerMiddle(fileName),
							photoImageView, ImageManager.getOptionsLandscape());
					photoImageView.setTag(fileName);
					setItemSize(photoImageView);
				}
			} else {
				Bitmap bitmap = ImageManager.getLoacalBitmap(file_path);
				if (bitmap != null) {
					if (progressBar != null
							&& message.getMessage_status() != AppPreferences.MESSAGE_STATUS_SEND_FAILED)
						progressBar.setVisibility(View.VISIBLE);
					photoImageView.setImageBitmap(bitmap);
					photoImageView.setTag(fileName);
					setItemSize(photoImageView);
				} else {
					photoImageView.setImageBitmap(ImageManager
							.getDefaultLandscape(context));
					photoImageView.setTag(null);
				}

			}
		} else {
			photoImageView.setImageBitmap(ImageManager
					.getDefaultLandscape(context));
			photoImageView.setTag(null);

		}

		photoImageView.setPadding(2, 2, 2, 2);
	}

	protected void bindFromVoiceView(final Message message,
			final ImageView rightVoiceImageView, TextView rightLengthTextView,
			final ProgressBar rightPlayProcessBar) {

		if (rightVoiceImageView == null || rightLengthTextView == null
				|| rightPlayProcessBar == null)
			return;

		rightVoiceImageView.setVisibility(View.VISIBLE);
		rightVoiceImageView.setTag(message.file_path);
		rightLengthTextView.setVisibility(View.VISIBLE);
		rightLengthTextView.setText(message.from_content_length + "'");
		// 下载语音
		File voiceFolder = Utils.getVoiceFolder(context);
		File mVoiceFile = new File(voiceFolder, message.file_path);
		File mDownVoiceFile = new File(voiceFolder, message.file_path
				+ AppPreferences.VOICE_SURFIX);
		if (!mVoiceFile.exists() && !mDownVoiceFile.exists()) {
			if (!Utils.isEmpty(message.file_path)) {
				Utils.uploadVoiceFile(context, message.file_path, null);
			}
		}
	}

	protected void bindImageClickEvent(final Message message,
			View photoImageView) {

		if (photoImageView == null)
			return;

		photoImageView.setOnClickListener(null);
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message.file_type)) {
			photoImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (message.getMessage_status() != AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
						gotoImageViewActivity(message.getFile_path());
					}
				}
			});

			OnLongClickListener photoLongListener = new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					List<String> menuList = new ArrayList<>();
					if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
						menuList.add(getContext()
								.getString(
										R.string.message_action_request_translate_again));
					}
					menuList.add(getContext()
							.getString(R.string.delete_message));
					final String[] menus = new String[menuList.size()];
					menuList.toArray(menus);
					new CustomDialog(getContext())
							.setTitle(getContext().getString(R.string.tips))
							.setItems(menus,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String selectedItem = menus[which];
											if (getContext().getString(
													R.string.delete_message)
													.equals(selectedItem)) {
												deleteMessageByMsgId(message.messageid);
											} else if (getContext()
													.getString(
															R.string.message_action_request_translate_again)
													.equals(selectedItem)) {
												doReRequestTranslate(message
														.getId());
											}
										}

									}).show();
					return true;
				}
			};
			photoImageView.setOnLongClickListener(photoLongListener);
		}
	}

	protected void bindLayoutClickEvent(final Message message, View layouttView) {
		if (layouttView == null)
			return;

		layouttView.setOnLongClickListener(null);
		if (message.getUserid() != App.readUser().getId()
				&& message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED) {
			layouttView
					.setOnLongClickListener(getToContentLongClickListener(message));
		} else {
			layouttView
					.setOnLongClickListener(getFromContentLongClickListener(message));
		}
	}

	protected void bindProfileThumb(User user, ImageView userThumb,
			ImageView smsImageView, ImageView langImageView) {

		Utils.setUserPicImage(userThumb, user.getPic_url());

		if (smsImageView != null) {
			// sms user
			if (user.active == 1) {
				smsImageView.setVisibility(View.INVISIBLE);
			} else {
				smsImageView.setVisibility(View.VISIBLE);
			}
		}

		if (langImageView != null) {
			langImageView.setImageResource(Utils.getLanguageFlag(user.lang));
		}
	}

	protected void bindSendErrorView(final boolean error,
			final TextView sendErrorView, final Message message) {
		if (error) {
			sendErrorView.setVisibility(View.VISIBLE);
		} else {
			sendErrorView.setVisibility(View.GONE);
		}
		sendErrorView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doReRequestTranslate(message.getId());
			}
		});
	}

	protected void bindToClickEvent(final Message message, View toTextView) {
		if (toTextView != null) {
			toTextView.setOnLongClickListener(null);
			toTextView
					.setOnLongClickListener(getToContentLongClickListener(message));
		}
	}

	protected void bindToView(final Message message,
			TextView translatedContentTextView, View autoTranslationLayout) {

		if (translatedContentTextView == null || autoTranslationLayout == null)
			return;

		translatedContentTextView.setVisibility(View.GONE);
		autoTranslationLayout.setVisibility(View.GONE);

		final String toContent = message.getTo_content();
		if (message.getFrom_lang().equals(message.getTo_lang())
				|| AppPreferences.MESSAGE_TYPE_NAME_PHOTO
						.equals(message.file_type) || Utils.isEmpty(toContent)) {
			//
		} else {

			translatedContentTextView.setVisibility(View.VISIBLE);
			if (message.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
				autoTranslationLayout.setVisibility(View.VISIBLE);

				bindAutoTranslationClick(message, autoTranslationLayout,
						this.getContext());
			}
			String unicode = EmojiParser.getInstance(getContext()).parseEmoji(
					message.getTo_content());
			SpannableString spannableString = ParseEmojiMsgUtil
					.getExpressionString(getContext(), unicode);
			if (spannableString != null) {
				translatedContentTextView.setText(spannableString);
			} else {
				translatedContentTextView.setText(message.getTo_content());
			}
		}
	}

	protected void bindUserThumbClickEvent(View userThumb, final User user) {

		if (userThumb == null)
			return;

		userThumb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(),
						FriendProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_USER, user);
				getContext().startActivity(intent);
			}
		});
	}

	protected void bindVoiceClickEvent(final Message message,
			final ImageView voiceImageView, final ProgressBar playProcessBar,
			final View bubbleLayout) {

		if (voiceImageView == null || playProcessBar == null)
			return;

		bubbleLayout.setOnClickListener(null);
		if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(message.file_type)) {
			bubbleLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// if (message.getMessage_status() !=
					// AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
					String url = "";
					File voiceFolder = Utils.getVoiceFolder(context);
					File mVoiceFile = new File(voiceFolder, message.file_path);
					File mDownVoiceFile = new File(voiceFolder,
							message.file_path + AppPreferences.VOICE_SURFIX);
					if (mVoiceFile.exists()) {
						url = message.file_path;
						playVoice(voiceFolder + "/" + url, voiceImageView,
								playProcessBar);
					} else if (mDownVoiceFile.exists()) {
						url = message.file_path + AppPreferences.VOICE_SURFIX;
						playVoice(voiceFolder + "/" + url, voiceImageView,
								playProcessBar);
					} else {
						if (!Utils.isEmpty(message.file_path)) {
							final MyHandler myHandler = new MyHandler(
									voiceImageView, playProcessBar);
							Utils.uploadVoiceFile(context, message.file_path,
									myHandler);
						} else {
							Toast.makeText(getContext(),
									R.string.play_voice_failure,
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				// }
			});
		}
	}

	// 原文文本菜单
	void createFromContentPopMenus(final Message message, View v,
			final String content) {
		List<String> menuList = new ArrayList<>();
		if (!AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
				.getFile_type())
				&& !AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(message
						.getFile_type())) {

			menuList.add(getContext().getString(R.string.message_action_copy));
			menuList.add(getContext().getString(R.string.message_action_share));
			menuList.add(getContext().getString(
					R.string.message_action_fullscreen));
			if (Utils.checkTts(message.getFrom_lang())
					|| Utils.checkTts(message.getTo_lang())) {
				menuList.add(getContext()
						.getString(R.string.message_action_tts));
			}
			// 正在请求中的可以请求自动翻译
			if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS) {
				menuList.add(getContext().getString(
						R.string.message_action_auto_translation));
			}
		}
		menuList.add(getContext().getString(R.string.delete_message));
		// 请求失败可以再请求一次
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
			menuList.add(getContext().getString(
					R.string.message_action_request_translate_again));
		} else if (message.getTo_userid() == App.readUser().getId()
				&& message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE
				&& (!message.getFrom_lang().equals(message.getTo_lang())
						&& !AppPreferences.MESSAGE_TYPE_NAME_PHOTO
								.equals(message.file_type) && Utils
							.isEmpty(message.getTo_content()))) {
			// 只有接收者才能点击接受翻译；
			// 缺钱的话，发送者也不能点击接受翻译。
			menuList.add(getContext().getString(
					R.string.message_action_accept_translate));
		}
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATING
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATE
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATING) {
			menuList.add(getContext().getString(
					R.string.message_action_retrieve_message));
		}
		// 翻译过的，并且未验证，出钱的人可以点击验证
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
				&& message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
				&& ((App.readUser().getId() == message.getUserid() && message
						.getFee() > 0) || (App.readUser().getId() == message
						.getTo_userid() && message.getTo_user_fee() > 0))) {
			menuList.add(getContext().getString(R.string.message_action_verify));
		}
		menuOnClickListener(menuList, message, content);
	}

	// 译文文本菜单
	void createToContentPopMenus(final Message message, View v,
			final String content) {
		List<String> menuList = new ArrayList<>();
		menuList.add(getContext().getString(R.string.message_action_copy));
		menuList.add(getContext().getString(R.string.message_action_share));
		menuList.add(getContext().getString(R.string.message_action_fullscreen));
		if (Utils.checkTts(message.getFrom_lang())
				|| Utils.checkTts(message.getTo_lang())) {
			menuList.add(getContext().getString(R.string.message_action_tts));
		}
		menuList.add(getContext().getString(R.string.delete_message));
		// 翻译过的，并且未验证，出钱的人可以点击验证
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
				&& message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
				&& ((App.readUser().getId() == message.getUserid() && message
						.getFee() > 0) || (App.readUser().getId() == message
						.getTo_userid() && message.getTo_user_fee() > 0))) {
			menuList.add(getContext().getString(R.string.message_action_verify));
		}

		menuOnClickListener(menuList, message, content);
	}

	private void deleteMessageByMsgId(final Long messageId) {
		App.messageDAO.deleteByMessageId(messageId);
		Toast.makeText(getContext(), R.string.messages_delete_success,
				Toast.LENGTH_SHORT).show();
		getCursor().requery();
		return;
	}

	private void doAcceptTranslate(final Long id) {
		Toast.makeText(
				getContext(),
				getContext()
						.getString(R.string.message_action_accept_translate),
				Toast.LENGTH_SHORT).show();

		if (mAcceptTranslateTask != null
				&& mAcceptTranslateTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mAcceptTranslateTask = new TranslateAcceptTask(id);
		mAcceptTranslateTask.setListener(mAcceptTranslateListener);
		mAcceptTranslateTask.execute();

	}

	private void doCopy(String text) {
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(text);
		Toast.makeText(getContext(),
				getContext().getString(R.string.already_copy_to_clipboard),
				Toast.LENGTH_SHORT).show();
	}

	private void doFullscreen(String text) {
		Intent intent = new Intent(getContext(), FullScreenActivity.class);
		intent.putExtra(FullScreenActivity.EXTRA_MESSAGE, text);
		getContext().startActivity(intent);
	}

	protected void doReRequestTranslate(Long localId) {
		Toast.makeText(getContext(),
				getContext().getString(R.string.status_sending),
				Toast.LENGTH_SHORT).show();

		Message message = App.messageDAO.fetchMessage(localId);
		message.setMessage_status(AppPreferences.MESSAGE_STATUS_BEFORE_SEND);
		message.setStatus_text(getContext().getString(R.string.data_sending));
		App.messageDAO.mergeMessage(message);
		getCursor().requery();

		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
				.getFile_type())) {// js 先上传图片然后发送请求
			mMessage = message;
			AbstractChatActivity.doUploadFile(message, mUploadTaskListener);
		} else {
			AbstractChatActivity.doRequestTranslate(message,
					mRequestTranslateListener);
		}
	}

	private void doShare(String text) {
		// create the send intent
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);

		// set the type
		shareIntent.setType("text/plain");

		// add a subject
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");

		// add the message
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);

		// start the chooser for sharing
		getContext().startActivity(
				Intent.createChooser(shareIntent,
						getContext().getString(R.string.app_name)));
	}

	private void doTTS(String lang1, String lang2, String text) {
		if (!Utils.tts(getContext(), lang1, lang2, text)) {
			Toast.makeText(getContext(),
					getContext().getString(R.string.tts_no_supported_language),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void doVerify(final Long id) {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mRequestVerifyTask != null
						&& mRequestVerifyTask.getStatus() == GenericTask.Status.RUNNING) {
					return;
				}

				mRequestVerifyTask = new RequestVerifyTask(id);
				mRequestVerifyTask.setListener(mRequestVerifyListener);
				mRequestVerifyTask.execute();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(context, positiveListener, negativeListener,
				context.getString(R.string.message_verify),
				context.getString(R.string.message_verify_cost));

	}

	public Context getContext() {
		return context;
	}

	private OnLongClickListener getFromContentLongClickListener(
			final Message message) {
		OnLongClickListener listener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = getMessageMenuFromContent(message);
				if (!Utils.isEmpty(content)) {
					// long click
					createFromContentPopMenus(message, v, content);
				}
				return true;
			}
		};
		return listener;
	}

	private String getMessageMenuFromContent(final Message message) {
		String content;
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
				.getFile_type())) {
			content = getContext().getString(R.string.notification_picture);
		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(message
				.getFile_type())) {
			content = getContext().getString(R.string.notification_voice);
		} else {
			content = message.getFrom_content();
		}

		return content;
	}

	private String getMessageMenuToContent(final Message message) {
		String content = message.getTo_content();
		return content;
	}

	String getMessageStatusText(final Message message) {
		if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_GIVEUP) {
			// use server definition
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATE) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_requesting));
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATING
				|| message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATING) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_translating));
		} else if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE
				&& !message.getFrom_lang().equals(message.getTo_lang())
				&& !AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message
						.getFile_type())
				&& message.getTo_userid() == App.readUser().getId()) {
			message.setStatus_text(getContext().getString(
					R.string.message_status_text_accept_translate));
		} else {
			message.setStatus_text("");
		}

		return message.getStatus_text();
	}

	private OnLongClickListener getToContentLongClickListener(
			final Message message) {
		OnLongClickListener listener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = getMessageMenuToContent(message);
				if (!Utils.isEmpty(content)) {
					// long click
					createToContentPopMenus(message, v, content);
				}
				return true;
			}
		};
		return listener;
	}

	private void gotoImageViewActivity(String file_path) {
		setChatPhotoList(getCursor());
		int position = chatPhotoList.indexOf(App.readServerAppInfo()
				.getServerOriginal(file_path));
		Intent intent = new Intent(getContext(), ImageViewActivity.class);
		intent.putExtra(ImageViewActivity.EXTRA_POSITION, position);
		intent.putExtra(ImageViewActivity.EXTRA_IMAGE_URLS, chatPhotoList);
		getContext().startActivity(intent);
	}

	private void menuOnClickListener(List<String> menuList,
			final Message message, final String content) {
		final String[] menus = new String[menuList.size()];
		menuList.toArray(menus);
		CustomDialog alertDialog = new CustomDialog(getContext()).setTitle(
				getContext().getString(R.string.tips)).setItems(menus,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String selectedItem = menus[which];
						if (getContext()
								.getString(R.string.message_action_copy)
								.equals(selectedItem)) {
							doCopy(content);
						} else if (getContext().getString(
								R.string.message_action_share).equals(
								selectedItem)) {
							doShare(content);
						} else if (getContext().getString(
								R.string.message_action_fullscreen).equals(
								selectedItem)) {
							doFullscreen(content);
						} else if (getContext().getString(
								R.string.message_action_tts).equals(
								selectedItem)) {
							doTTS(message.from_lang, message.to_lang, content);// from_lang优先
						} else if (getContext().getString(
								R.string.delete_message).equals(selectedItem)) {
							deleteMessageByMsgId(message.messageid);
						} else if (getContext()
								.getString(
										R.string.message_action_request_translate_again)
								.equals(selectedItem)) {
							doReRequestTranslate(message.getId());
						} else if (getContext().getString(
								R.string.message_action_accept_translate)
								.equals(selectedItem)) {
							doAcceptTranslate(message.messageid);
						} else if (getContext().getString(
								R.string.message_action_retrieve_message)
								.equals(selectedItem)) {
							retrieveMessageById(message.messageid);
						} else if (getContext().getString(
								R.string.message_action_verify).equals(
								selectedItem)) {
							doVerify(message.messageid);
						}
					}
				});
		alertDialog.setTitle(content).show();
	}

	private void onAcceptTranslateSuccess(boolean existTranslatedMessage) {
		getCursor().requery();

		// if (existTranslatedMessage) {
		// doRetrieveUser(App.readUser().getId());//
		// 回到Setting画面，能够立刻看到balance变化。
		// }
	}

	private void onRequestTranslateFailure(Message message, String msg) {
		// save message
		message.setMessage_status(AppPreferences.MESSAGE_STATUS_SEND_FAILED);
		message.setStatus_text(getContext().getString(
				R.string.message_action_click_resend));
		App.messageDAO.mergeMessage(message);

		getCursor().requery();

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRequestTranslateSuccess() {
		getCursor().requery();
		// AbstractChatActivity.doRefresh();
		CommonUtilities.broadcastMessage(context, null);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "send Success");

	}

	private void retrieveMessageById(final Long messageId) {
		if (retrieveMessageTask != null
				&& retrieveMessageTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		retrieveMessageTask = new RetrieveMessageTask(messageId);
		retrieveMessageTask.setListener(retrieveMessageByIdTaskListener);
		retrieveMessageTask.execute();

	}

	private void setChatPhotoList(Cursor chatsCursor) {
		chatPhotoList = new ArrayList<>();
		for (chatsCursor.moveToFirst(); !chatsCursor.isAfterLast(); chatsCursor
				.moveToNext()) {
			Message message = MessageTable.parseCursor(chatsCursor);
			if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
					.equals(message.file_type)
					&& AppPreferences.MESSAGE_STATUS_SEND_FAILED != message
							.getMessage_status()) {
				final String file_path = message.file_path;
				String fileName = file_path.substring(file_path
						.lastIndexOf('/') + 1);
				chatPhotoList.add(App.readServerAppInfo().getServerOriginal(
						fileName));
			}
		}
	}

	public void setContext(Context context) {
		this.context = context;
	}

	private void setItemSize(View itemView) {
		LayoutParams imageParams = (LayoutParams) itemView.getLayoutParams();
		imageParams.width = mWidth / 4;
		imageParams.height = mWidth / 4;
		itemView.setLayoutParams(imageParams);
	}
}