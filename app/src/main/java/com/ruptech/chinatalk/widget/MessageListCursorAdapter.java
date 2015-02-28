package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class MessageListCursorAdapter extends AbstractMessageCursorAdapter {

	enum MessageType {
		MY_PHOTO, MY_VOICE, MY_TEXT, FRIEND_PHOTO, FRIEND_VOICE, FRIEND_TEXT, TYPE_COUNT
	}

	static class ViewHolder {
		private TextView createDateTextView;

		private View divider;
		private View userThumbView;
		private ImageView userThumbImageView;
		private ImageView smsImageView;
		private ImageView langImageView;
		private ImageView photoImageView;
		private ProgressBar photoProgressBar;
		private ImageView voiceImageView;
		private TextView contentTextView;
		private TextView translatedContentTextView;
		private TextView lengthTextView;
		private ProgressBar playProcessBar;
		private TextView timeTextView;
		private TextView sendErrorView;

		private View bubbleLayout;

		private View autoTranslationLayout;

	}

	static final String TAG = Utils.CATEGORY
			+ MessageListCursorAdapter.class.getSimpleName();

	private final User mFriendUser;

	public MessageListCursorAdapter(Context context, Cursor cursor, User friend) {
		super(context, cursor);
		this.mFriendUser = friend;

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}

		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		mWidth = display.getWidth();

		CREATE_DATE_INDEX = cursor
				.getColumnIndexOrThrow(MessageTable.Columns.CREATE_DATE);
	}

	private void bindFromView(final Message message, final ViewHolder holder) {
		if (holder.divider != null)
			holder.divider.setVisibility(View.GONE);
		if (holder.voiceImageView != null)
			holder.voiceImageView.setVisibility(View.GONE);
		if (holder.lengthTextView != null)
			holder.lengthTextView.setVisibility(View.GONE);
		if (holder.photoImageView != null)
			holder.photoImageView.setVisibility(View.GONE);
		if (holder.contentTextView != null)
			holder.contentTextView.setVisibility(View.GONE);
		if (holder.photoProgressBar != null)
			holder.photoProgressBar.setVisibility(View.GONE);

		// time
		String messageUtcDatetimeStr = DateCommonUtils.convUtcDateString(
				message.getCreate_date(), DateCommonUtils.DF_yyyyMMddHHmmss);
		holder.timeTextView.setText(DateCommonUtils.dateFormat(
				DateCommonUtils.parseToDateFromString(messageUtcDatetimeStr),
				DateCommonUtils.DF_HHmm));
		int status = message.getMessage_status();

		// divider
		if (!Utils.isEmpty(message.getTo_content()) && holder.divider != null) {
			holder.divider.setVisibility(View.VISIBLE);
		}
		if (isMyMessage(message)) {
			bindSendErrorView(
					AppPreferences.MESSAGE_STATUS_SEND_FAILED == status,
					holder.sendErrorView, message);
		}

		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message.file_type)) {
			// photo
			bindFromPhotoView(message, holder.photoImageView,
					holder.photoProgressBar);

		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
				.equals(message.file_type)) {
			// voice
			bindFromVoiceView(message, holder.voiceImageView,
					holder.lengthTextView, holder.playProcessBar);
		} else {// text
			bindFromContentView(message, holder.contentTextView);
		}

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Message message = MessageTable.parseCursor(cursor);
		boolean mine = isMyMessage(message);
		User user;
		if (mine) {
			user = App.readUser();
		} else {
			user = mFriendUser;
		}
		// from content
		bindFromView(message, holder);

		bindUserThumbClickEvent(holder.userThumbView, user);
		bindImageClickEvent(message, holder.photoImageView);

		bindLayoutClickEvent(message, holder.bubbleLayout);
		bindFromClickEvent(message, holder.contentTextView);
		bindVoiceClickEvent(message, holder.voiceImageView,
				holder.playProcessBar, holder.bubbleLayout);
		bindToClickEvent(message, holder.translatedContentTextView);

		// profile thumb
		bindProfileThumb(user, holder.userThumbImageView, holder.smsImageView,
				holder.langImageView);
		// to content
		bindToView(message, holder.translatedContentTextView,
				holder.autoTranslationLayout);

		// timegroup
		bindDateTimeView(cursor, holder.createDateTextView);
	}

	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		Message message = MessageTable.parseCursor(cursor);
		return intValue(getMessageType(message));
	}

	public MessageType getMessageType(Message message) {
		boolean mine = isMyMessage(message);
		MessageType type;
		if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(message.file_type)) {
			type = mine ? MessageType.MY_PHOTO : MessageType.FRIEND_PHOTO;
		} else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
				.equals(message.file_type)) {
			type = mine ? MessageType.MY_VOICE : MessageType.FRIEND_VOICE;
		} else {
			type = mine ? MessageType.MY_TEXT : MessageType.FRIEND_TEXT;
		}

		return type;
	}

	@Override
	public int getViewTypeCount() {
		return intValue(MessageType.TYPE_COUNT);
	}

	public int intValue(MessageType type) {
		return type.ordinal();
	}

	private boolean isMyMessage(Message message) {
		long userid = message.getUserid();
		return (App.readUser().getId() == userid);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final Message message = MessageTable.parseCursor(cursor);

		ViewHolder holder = new ViewHolder();
		View view;
		int resID;
		MessageType type = this.getMessageType(message);
		switch (type) {
		case MY_PHOTO:
			resID = R.layout.item_chatting_msg_right_photo;
			break;
		case MY_VOICE:
			resID = R.layout.item_chatting_msg_right_voice;
			break;
		case MY_TEXT:
			resID = R.layout.item_chatting_msg_right_text;
			break;
		case FRIEND_PHOTO:
			resID = R.layout.item_chatting_msg_left_photo;
			break;
		case FRIEND_VOICE:
			resID = R.layout.item_chatting_msg_left_voice;
			break;
		case FRIEND_TEXT:
			resID = R.layout.item_chatting_msg_left_text;
			break;
		default:
			if (isMyMessage(message))
				resID = R.layout.item_chatting_msg_right_text;
			else
				resID = R.layout.item_chatting_msg_left_text;
		}

		view = mInflater.inflate(resID, parent, false);

		holder.createDateTextView = (TextView) view
				.findViewById(R.id.item_chatting_createdtime_textview);

		holder.bubbleLayout = view.findViewById(R.id.item_chatting_from_layout);
		holder.divider = view.findViewById(R.id.item_chatting_divider);
		holder.userThumbView = view
				.findViewById(R.id.item_chatting_friend_mask);
		holder.userThumbImageView = (ImageView) view
				.findViewById(R.id.item_chatting_user_thumb_imageview);
		holder.smsImageView = (ImageView) view
				.findViewById(R.id.item_chatting_user_sms_imageview);
		holder.langImageView = (ImageView) view
				.findViewById(R.id.item_chatting_user_lang_imageview);

		holder.photoImageView = (ImageView) view
				.findViewById(R.id.item_chatting_photo_imageview);
		holder.photoProgressBar = (ProgressBar) view
				.findViewById(R.id.item_chatting_photo_progress);
		holder.voiceImageView = (ImageView) view
				.findViewById(R.id.item_chatting_voice_imageview);
		holder.contentTextView = (TextView) view
				.findViewById(R.id.item_chatting_content_textview);

		holder.translatedContentTextView = (TextView) view
				.findViewById(R.id.item_chatting_trans_content_textview);

		holder.lengthTextView = (TextView) view
				.findViewById(R.id.item_chatting_length_textview);
		holder.playProcessBar = (ProgressBar) view
				.findViewById(R.id.item_chatting_voice_play_process_bar);
		holder.timeTextView = (TextView) view
				.findViewById(R.id.item_chatting_time_textview);
		holder.sendErrorView = (TextView) view
				.findViewById(R.id.item_chatting_error_textview);

		holder.autoTranslationLayout = view
				.findViewById(R.id.item_chatting_auto_translation_textview);
		view.setTag(holder);
		return view;
	}
}
