package com.ruptech.chinatalk.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestVerifyTask;
import com.ruptech.chinatalk.task.impl.RetrieveMessageTask;
import com.ruptech.chinatalk.task.impl.TranslateAcceptTask;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

public class TTTAdapter extends CursorAdapter {
	private static final String TAG = TTTAdapter.class.getName();

	static class ViewHolder {
		private TextView toContentTextView;

		private ImageView userThumb;
		private TextView fromContentTextView;
		private TextView lengthTextView;
		private TextView timeTextView;

		private View fromBubbleLayout;

		private View toBubbleLayout;

		private TextView sendStatusView;

	}

	private Context mContext;

	public TTTAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mContentResolver = context.getContentResolver();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Message chat = TableContent.MessageTable.parseCursor(cursor);
		ViewHolder holder = (ViewHolder) view.getTag();

		bindFromClickEvent(chat, holder.fromBubbleLayout);
		bindToClickEvent(chat, holder.toBubbleLayout);

		// profile thumb
		User user = App.readUser();
		bindProfileThumb(user, holder.userThumb);

		bindRightFromView(chat, holder);
		bindLeftToView(chat, holder);

	}

	private void bindRightFromView(final Message message, ViewHolder holder) {
		holder.fromContentTextView.setVisibility(View.GONE);
		holder.lengthTextView.setVisibility(View.GONE);

		// time
		String messageUtcDatetimeStr = DateCommonUtils.convUtcDateString(
				message.getCreate_date(), DateCommonUtils.DF_yyyyMMddHHmmss);
		holder.timeTextView.setText(DateCommonUtils.dateFormat(
				DateCommonUtils.parseToDateFromString(messageUtcDatetimeStr),
				DateCommonUtils.DF_HHmm));

		// from content
		// text
		bindFromContentView(message, holder.fromContentTextView);
	}

	private void bindLeftToView(final Message message, ViewHolder holder) {
		// to content
		if (Utils.isEmpty(message.getTo_content())) {
			holder.toContentTextView.setVisibility(View.GONE);
		} else {
			holder.toContentTextView.setVisibility(View.VISIBLE);
		}
		int message_status = message.getMessage_status();
		if (!MessageReceiver.isMessageStatusEnd(message_status)) {
			holder.sendStatusView.setVisibility(View.VISIBLE);
			holder.sendStatusView
					.setText(message.getStatus_text());
		} else {
			if (AppPreferences.MESSAGE_STATUS_NO_TRANSLATE == message_status
					&& message.getFrom_voice_id() > 0) {
				holder.sendStatusView.setVisibility(View.VISIBLE);
				holder.sendStatusView
						.setText(R.string.no_money_cannot_translate_voice);
			} else if (AppPreferences.MESSAGE_STATUS_GIVEUP == message_status) {
				holder.sendStatusView.setVisibility(View.VISIBLE);
				holder.sendStatusView.setText(message.getStatus_text());
			} else {
				holder.sendStatusView.setVisibility(View.GONE);
			}
		}

		holder.toContentTextView.setText(message.getTo_content());
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = mInflater.inflate(R.layout.item_chatting_ttt_msg, parent,
				false);

		ViewHolder holder = new ViewHolder();

		holder.fromBubbleLayout = view
				.findViewById(R.id.item_ttt_right_from_layout);
		holder.toBubbleLayout = view
				.findViewById(R.id.item_ttt_left_to_layout);
		holder.userThumb = (ImageView) view
				.findViewById(R.id.item_ttt_right_user_thumb_imageview);
		holder.fromContentTextView = (TextView) view
				.findViewById(R.id.item_ttt_right_content_textview);
		holder.lengthTextView = (TextView) view
				.findViewById(R.id.item_ttt_right_length_textview);
		holder.timeTextView = (TextView) view
				.findViewById(R.id.item_ttt_right_time_textview);

		holder.toContentTextView = (TextView) view
				.findViewById(R.id.item_ttt_left_content_textview);

		holder.sendStatusView = (TextView) view
				.findViewById(R.id.item_ttt_msg_send_status_textview);
		view.setTag(holder);
		return view;
	}


	private GenericTask mRequestVerifyTask;

	public LayoutInflater mInflater;


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

	public ContentResolver mContentResolver;

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

	protected void bindProfileThumb(User user, ImageView userThumb) {

		Utils.setUserPicImage(userThumb, user.getPic_url());
	}


	protected void bindToClickEvent(final Message message, View toTextView) {
		if (toTextView != null) {
			toTextView.setOnLongClickListener(null);
			toTextView
					.setOnLongClickListener(getToContentLongClickListener(message));
		}
	}


	// 原文文本菜单
	void createFromContentPopMenus(final Message message,
	                               final String content) {
		List<String> menuList = new ArrayList<String>();
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
	void createToContentPopMenus(final Message message,
	                             final String content) {
		List<String> menuList = new ArrayList<String>();
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
		Utils.AlertDialog(getContext(), positiveListener, negativeListener,
				getContext().getString(R.string.message_verify),
				getContext().getString(R.string.message_verify_cost));

	}

	public Context getContext() {
		return mContext;
	}

	//process data bounded on view
	private View.OnLongClickListener getFromContentLongClickListener(
			final Message chat) {
		View.OnLongClickListener listener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = getMessageMenuFromContent(chat);
				if (!Utils.isEmpty(content)) {
					// long click
					createFromContentPopMenus(chat, content);
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


	private View.OnLongClickListener getToContentLongClickListener(
			final Message chat) {
		View.OnLongClickListener listener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final String content = chat.getTo_content();
				if (!Utils.isEmpty(content)) {
					// long click
					createToContentPopMenus(chat, content);
				}
				return true;
			}
		};
		return listener;
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
	private GenericTask mAcceptTranslateTask;

	private GenericTask retrieveMessageTask;

	private void retrieveMessageById(final Long messageId) {
		if (retrieveMessageTask != null
				&& retrieveMessageTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		retrieveMessageTask = new RetrieveMessageTask(messageId);
		retrieveMessageTask.setListener(retrieveMessageByIdTaskListener);
		retrieveMessageTask.execute();

	}

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

	private void onAcceptTranslateSuccess(boolean existTranslatedMessage) {
		//getCursor().requery();
		//TODO

		// if (existTranslatedMessage) {
		// doRetrieveUser(App.readUser().getId());//
		// 回到Setting画面，能够立刻看到balance变化。
		// }
	}

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
}
