package com.ruptech.chinatalk.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestVerifyTask;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

public class TTTChatAdapter extends CursorAdapter {
	private static final String TAG = TTTChatAdapter.class.getName();

	enum ChatType {
		MY_TEXT, FRIEND_TEXT
	}

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

	public TTTChatAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mContentResolver = context.getContentResolver();
	}


	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		Chat chat = TableContent.ChatTable.parseCursor(cursor);
		return intValue(getChatType(chat));
	}

	public int intValue(ChatType type) {
		return type.ordinal();
	}

	public ChatType getChatType(Chat chat) {
		boolean mine = (chat.getFromMe() == ChatProvider.OUTGOING);
		ChatType type = mine ? ChatType.MY_TEXT : ChatType.FRIEND_TEXT;

		return type;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Chat chat = TableContent.ChatTable.parseCursor(cursor);
		ViewHolder holder = (ViewHolder) view.getTag();

		bindFromClickEvent(chat, holder.fromBubbleLayout);
		bindToClickEvent(chat, holder.toBubbleLayout);

		// profile thumb
		User user = App.readUser();
		bindProfileThumb(user, holder.userThumb);

		bindRightFromView(chat, holder);
		bindLeftToView(chat, holder);

	}

	private void bindRightFromView(final Chat chat, ViewHolder holder) {
		holder.fromContentTextView.setVisibility(View.GONE);
		holder.lengthTextView.setVisibility(View.GONE);

		// time
		String datetimeStr = TimeUtil.getHourAndMin(chat.getDate());
		holder.timeTextView.setText(datetimeStr);

		// from content
		// text
		bindFromContentView(chat, holder.fromContentTextView);
	}

	private void bindLeftToView(final Chat chat, ViewHolder holder) {
		//TODO 翻译秘书 to content
		// to content
		if (Utils.isEmpty(chat.getTo_content())) {
			holder.toContentTextView.setVisibility(View.GONE);
		} else {
			holder.toContentTextView.setVisibility(View.VISIBLE);
		}
		int message_status = chat.getStatus();
		if (!MessageReceiver.isMessageStatusEnd(message_status)) {
			holder.sendStatusView.setVisibility(View.VISIBLE);
			holder.sendStatusView
					.setText(R.string.message_status_text_requesting);
		} else {
			if (AppPreferences.MESSAGE_STATUS_NO_TRANSLATE == message_status) {
				holder.sendStatusView.setVisibility(View.VISIBLE);
				holder.sendStatusView
						.setText(R.string.no_money_cannot_translate_voice);
			} else if (AppPreferences.MESSAGE_STATUS_GIVEUP == message_status) {
				holder.sendStatusView.setVisibility(View.VISIBLE);
				//TODO
				//holder.sendStatusView.setText(chat.getStatus_text());
			} else {
				holder.sendStatusView.setVisibility(View.GONE);
			}
		}

		holder.toContentTextView.setText(chat.getTo_content());
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
				.findViewById(R.id.item_chatting_ttt_right_from_layout);
		holder.toBubbleLayout = view
				.findViewById(R.id.item_chatting_ttt_left_to_layout);
		holder.userThumb = (ImageView) view
				.findViewById(R.id.item_chatting_ttt_right_user_thumb_imageview);
		holder.fromContentTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_content_textview);
		holder.lengthTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_length_textview);
		holder.timeTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_time_textview);

		holder.toContentTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_left_content_textview);

		holder.sendStatusView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_msg_send_status_textview);
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

	protected void bindFromClickEvent(final Chat chat, View fromTextView) {
		if (fromTextView == null)
			return;

		fromTextView.setOnLongClickListener(null);
		fromTextView
				.setOnLongClickListener(getFromContentLongClickListener(chat));
	}

	protected void bindFromContentView(final Chat chat,
	                                   final TextView rightFromContentTextView) {
		if (rightFromContentTextView == null)
			return;

		rightFromContentTextView.setVisibility(View.VISIBLE);
		rightFromContentTextView.setMovementMethod(LinkMovementMethod
				.getInstance());
		String unicode = EmojiParser.getInstance(mContext).parseEmoji(
				chat.getMessage());
		SpannableString spannableString = ParseEmojiMsgUtil
				.getExpressionString(getContext(), unicode);
		if (spannableString != null) {
			rightFromContentTextView.setText(spannableString);
		} else {
			rightFromContentTextView.setText(chat.getMessage());
		}
	}

	protected void bindProfileThumb(User user, ImageView userThumb) {

		Utils.setUserPicImage(userThumb, user.getPic_url());
	}


	protected void bindToClickEvent(final Chat chat, View toTextView) {
		if (toTextView != null) {
			toTextView.setOnLongClickListener(null);
			toTextView
					.setOnLongClickListener(getToContentLongClickListener(chat));
		}
	}


	// 原文文本菜单
	void createFromContentPopMenus(final Chat chat,
	                               final String content) {
		List<String> menuList = new ArrayList<>();
		if (!AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
				.getType())
				&& !AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
				.getType())) {

			menuList.add(getContext().getString(R.string.message_action_copy));
			menuList.add(getContext().getString(R.string.message_action_share));
			menuList.add(getContext().getString(
					R.string.message_action_fullscreen));
			if (Utils.checkTts(chat.getFromLang())
					|| Utils.checkTts(chat.getToLang())) {
				menuList.add(getContext()
						.getString(R.string.message_action_tts));
			}
		}
		// 请求失败可以再请求一次
		if (chat.getStatus() == AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
			menuList.add(getContext().getString(
					R.string.message_action_request_translate_again));
		}
		if (chat.getStatus() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
				|| chat.getStatus() == AppPreferences.MESSAGE_STATUS_TRANSLATING) {
			menuList.add(getContext().getString(
					R.string.message_action_retrieve_message));
		}
		// 翻译过的，并且未验证，出钱的人可以点击验证
		if (chat.getStatus() == AppPreferences.MESSAGE_STATUS_TRANSLATED
				&& chat.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST) {
			menuList.add(getContext().getString(R.string.message_action_verify));
		}
		menuOnClickListener(menuList, chat, content);
	}

	// 译文文本菜单
	void createToContentPopMenus(final Chat chat,
	                             final String content) {
		List<String> menuList = new ArrayList<>();
		menuList.add(getContext().getString(R.string.message_action_copy));
		menuList.add(getContext().getString(R.string.message_action_share));
		menuList.add(getContext().getString(R.string.message_action_fullscreen));
		if (Utils.checkTts(chat.getFromLang())
				|| Utils.checkTts(chat.getToLang())) {
			menuList.add(getContext().getString(R.string.message_action_tts));
		}
		// 翻译过的，并且未验证，出钱的人可以点击验证
		if (chat.getStatus() == AppPreferences.MESSAGE_STATUS_TRANSLATED
				&& chat.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST) {
			menuList.add(getContext().getString(R.string.message_action_verify));
		}

		menuOnClickListener(menuList, chat, content);
	}

	private void doCopy(Chat chat) {
		android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(chat.getMessage());
		Toast.makeText(getContext(),
				getContext().getString(R.string.already_copy_to_clipboard),
				Toast.LENGTH_SHORT).show();
	}

	private void doFullscreen(Chat chat) {
		Intent intent = new Intent(getContext(), FullScreenActivity.class);
		intent.putExtra(FullScreenActivity.EXTRA_MESSAGE, chat.getMessage());
		getContext().startActivity(intent);
	}

	private void doShare(Chat chat) {
		// create the send intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		// set the type
		shareIntent.setType("text/plain");

		// add a subject
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "");

		// add the message
		shareIntent.putExtra(Intent.EXTRA_TEXT, chat.getMessage());

		// start the chooser for sharing
		getContext().startActivity(
				Intent.createChooser(shareIntent,
						getContext().getString(R.string.app_name)));
	}

	private void doTTS(Chat chat) {
		if (!Utils.tts(getContext(), chat.getFromLang(), chat.getToLang(), chat.getMessage())) {
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
			final Chat chat) {
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

	private String getMessageMenuFromContent(final Chat chat) {
		String content;
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
				.getType())) {
			content = getContext().getString(R.string.notification_picture);
		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
				.getType())) {
			content = getContext().getString(R.string.notification_voice);
		} else {
			content = chat.getMessage();
		}

		return content;
	}


	private View.OnLongClickListener getToContentLongClickListener(
			final Chat chat) {
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
	                                 final Chat chat, final String content) {
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
							doCopy(chat);
						} else if (getContext().getString(
								R.string.message_action_share).equals(
								selectedItem)) {
							doShare(chat);
						} else if (getContext().getString(
								R.string.message_action_fullscreen).equals(
								selectedItem)) {
							doFullscreen(chat);
						} else if (getContext().getString(
								R.string.message_action_tts).equals(
								selectedItem)) {
							doTTS(chat);// from_lang优先
						} else if (getContext().getString(
								R.string.message_action_retrieve_message)
								.equals(selectedItem)) {
//                            retrieveMessageById(message.messageid);
						} else if (getContext().getString(
								R.string.message_action_verify).equals(
								selectedItem)) {
//                            doVerify(message.messageid);
						}
					}
				});
		alertDialog.setTitle(content).show();
	}

}
