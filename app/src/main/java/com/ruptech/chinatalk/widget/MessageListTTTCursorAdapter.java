package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

import static com.ruptech.chinatalk.sqlite.TableContent.MessageTable;

public class MessageListTTTCursorAdapter extends AbstractMessageCursorAdapter {

	static class ViewHolder {
		private TextView createDateTextView;

		private TextView toContentTextView;

		private ImageView userThumb;
		private ImageView voiceImageView;
		private TextView fromContentTextView;
		private TextView lengthTextView;
		private ProgressBar playProcessBar;
		private TextView timeTextView;

		private View fromBubbleLayout;

		private View toBubbleLayout;

		private View autoTranslationLayout;
		private TextView sendErrorView;
		private TextView sendStatusView;
	}

	static final String TAG = Utils.CATEGORY
			+ MessageListTTTCursorAdapter.class.getSimpleName();

	public MessageListTTTCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
		CREATE_DATE_INDEX = cursor
				.getColumnIndexOrThrow(MessageTable.Columns.CREATE_DATE);
	}

	private void bindLeftToView(final Message message, ViewHolder holder) {
		// to content
		if (Utils.isEmpty(message.getTo_content())) {
			holder.toContentTextView.setVisibility(View.GONE);
		} else {
			holder.toContentTextView.setVisibility(View.VISIBLE);
		}
		if (message.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
			holder.autoTranslationLayout.setVisibility(View.VISIBLE);
			bindAutoTranslationClick(message, holder.autoTranslationLayout,
					this.getContext());
		} else {
			holder.autoTranslationLayout.setVisibility(View.GONE);
		}
		int message_status = message.getMessage_status();
		if (!MessageReceiver.isMessageStatusEnd(message_status)) {
			holder.sendStatusView.setVisibility(View.VISIBLE);
			holder.sendStatusView
					.setText(R.string.message_status_text_requesting);
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

	private void bindRightFromView(final Message message, ViewHolder holder) {
		holder.fromContentTextView.setVisibility(View.GONE);
		holder.playProcessBar.setVisibility(View.GONE);
		holder.voiceImageView.setVisibility(View.GONE);
		holder.lengthTextView.setVisibility(View.GONE);

		// time
		String messageUtcDatetimeStr = DateCommonUtils.convUtcDateString(
				message.getCreate_date(), DateCommonUtils.DF_yyyyMMddHHmmss);
		holder.timeTextView.setText(DateCommonUtils.dateFormat(
				DateCommonUtils.parseToDateFromString(messageUtcDatetimeStr),
				DateCommonUtils.DF_HHmm));

		bindSendErrorView(
				AppPreferences.MESSAGE_STATUS_SEND_FAILED == message
						.getMessage_status(),
				holder.sendErrorView, message);

		// from content
		if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(message.file_type)) {
			// voice
			bindFromVoiceView(message, holder.voiceImageView,
					holder.lengthTextView, holder.playProcessBar);
		} else {
			// text
			bindFromContentView(message, holder.fromContentTextView);
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final Message message = MessageTable.parseCursor(cursor);

		ViewHolder holder = (ViewHolder) view.getTag();

		// event
		// bindUserThumbClickEvent(holder.userThumb, App.readUser());

		bindFromClickEvent(message, holder.fromBubbleLayout);
		bindFromClickEvent(message, holder.fromContentTextView);
		bindVoiceClickEvent(message, holder.voiceImageView,
				holder.playProcessBar, holder.fromBubbleLayout);
		bindToClickEvent(message, holder.toBubbleLayout);
		bindToClickEvent(message, holder.toContentTextView);

		// profile thumb
		User user = App.readUser();
		bindProfileThumb(user, holder.userThumb, null, null);

		bindRightFromView(message, holder);
		bindLeftToView(message, holder);

		// timegroup
		bindDateTimeView(cursor, holder.createDateTextView);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		MessageTable.parseCursor(cursor);

		Bitmap voice_playing_bmp = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.left_chatfrom_voice_playing);
		voice_playing_width = voice_playing_bmp.getWidth();
		View view = mInflater.inflate(R.layout.item_chatting_ttt_msg, parent,
				false);

		ViewHolder holder = new ViewHolder();
		holder.createDateTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_createdtime_textview);

		holder.fromBubbleLayout = view
				.findViewById(R.id.item_chatting_ttt_right_from_layout);
		holder.toBubbleLayout = view
				.findViewById(R.id.item_chatting_ttt_left_to_layout);
		holder.userThumb = (ImageView) view
				.findViewById(R.id.item_chatting_ttt_right_user_thumb_imageview);
		holder.fromContentTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_content_textview);
		holder.voiceImageView = (ImageView) view
				.findViewById(R.id.item_chatting_ttt_right_voice_imageview);
		holder.lengthTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_length_textview);
		holder.playProcessBar = (ProgressBar) view
				.findViewById(R.id.item_ttt_right_play_process_bar);
		holder.timeTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_right_time_textview);

		holder.toContentTextView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_left_content_textview);

		holder.autoTranslationLayout = view
				.findViewById(R.id.item_chatting_ttt_auto_translation_textview);
		holder.sendErrorView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_error_textview);
		holder.sendStatusView = (TextView) view
				.findViewById(R.id.item_chatting_ttt_msg_send_status_textview);

		view.setTag(holder);
		return view;
	}

}
