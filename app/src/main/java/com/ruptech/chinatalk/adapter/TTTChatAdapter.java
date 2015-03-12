package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.AbstractChatCursorAdapter;

public class TTTChatAdapter extends AbstractChatCursorAdapter {
    private static final String TAG = TTTChatAdapter.class.getName();

    enum ChatType {
        MY_PHOTO, MY_VOICE, MY_TEXT, FRIEND_PHOTO, FRIEND_VOICE, FRIEND_TEXT, TYPE_COUNT
    }

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

    private static final int DELAY_NEWMSG = 2000;
    private ActionBarActivity mContext;
    private User mFriendUser;

    public TTTChatAdapter(ActionBarActivity context, Cursor cursor, String[] from, TranslateClient client, User friend) {
        // super(context, android.R.layout.simple_list_item_1, cursor, from,
        // to);
        super(context, cursor);
        mContext = context;
        mFriendUser = friend;
        mClient = client;
        mInflater = LayoutInflater.from(context);
        mContentResolver = context.getContentResolver();

        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        mWidth = display.getWidth();
        CREATE_DATE_INDEX = cursor
                .getColumnIndexOrThrow(ChatTable.Columns.DATE);
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
        ChatType type;
        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())) {
            type = mine ? ChatType.MY_PHOTO : ChatType.FRIEND_PHOTO;
        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
                .equals(chat.getType())) {
            type = mine ? ChatType.MY_VOICE : ChatType.FRIEND_VOICE;
        } else {
            type = mine ? ChatType.MY_TEXT : ChatType.FRIEND_TEXT;
        }

        return type;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Chat chat = TableContent.ChatTable.parseCursor(cursor);
        ViewHolder holder = (ViewHolder) view.getTag();

        // event
        // bindUserThumbClickEvent(holder.userThumb, App.readUser());

        bindFromClickEvent(chat, holder.fromBubbleLayout);
        bindFromClickEvent(chat, holder.fromContentTextView);
        bindVoiceClickEvent(chat, holder.voiceImageView,
                holder.playProcessBar, holder.fromBubbleLayout);
        bindToClickEvent(chat, holder.toBubbleLayout);
        bindToClickEvent(chat, holder.toContentTextView);

        // profile thumb
        User user = App.readUser();
        bindProfileThumb(user, holder.userThumb, null, null);

        bindRightFromView(chat, holder);
        bindLeftToView(chat, holder);

        // timegroup
        bindDateTimeView(cursor, holder.createDateTextView);
    }

    private void bindRightFromView(final Chat chat, ViewHolder holder) {
        holder.fromContentTextView.setVisibility(View.GONE);
        holder.playProcessBar.setVisibility(View.GONE);
        holder.voiceImageView.setVisibility(View.GONE);
        holder.lengthTextView.setVisibility(View.GONE);

        // time
        String datetimeStr = TimeUtil.getHourAndMin(chat.getDate());
        holder.timeTextView.setText(datetimeStr);

        bindSendErrorView(
                ChatProvider.DS_NEW == chat.getStatus(),
                holder.sendErrorView, chat);

        // from content
        if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat.getType())) {
            // voice
            bindFromVoiceView(chat, holder.voiceImageView,
                    holder.lengthTextView, holder.playProcessBar);
        } else {
            // text
            bindFromContentView(chat, holder.fromContentTextView);
        }
    }

    private void bindLeftToView(final Chat chat, ViewHolder holder) {
        //TODO 翻译秘书 to content
        // to content
//        if (Utils.isEmpty(chat.getTo_content())) {
//            holder.toContentTextView.setVisibility(View.GONE);
//        } else {
//            holder.toContentTextView.setVisibility(View.VISIBLE);
//        }
//        if (chat.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
//            holder.autoTranslationLayout.setVisibility(View.VISIBLE);
//            bindAutoTranslationClick(chat, holder.autoTranslationLayout,
//                    this.getContext());
//        } else {
//            holder.autoTranslationLayout.setVisibility(View.GONE);
//        }
//        int message_status = chat.getStatus();
//        if (!MessageReceiver.isMessageStatusEnd(message_status)) {
//            holder.sendStatusView.setVisibility(View.VISIBLE);
//            holder.sendStatusView
//                    .setText(R.string.message_status_text_requesting);
//        } else {
//            if (AppPreferences.MESSAGE_STATUS_NO_TRANSLATE == message_status
//                    && message.getFrom_voice_id() > 0) {
//                holder.sendStatusView.setVisibility(View.VISIBLE);
//                holder.sendStatusView
//                        .setText(R.string.no_money_cannot_translate_voice);
//            } else if (AppPreferences.MESSAGE_STATUS_GIVEUP == message_status) {
//                holder.sendStatusView.setVisibility(View.VISIBLE);
//                holder.sendStatusView.setText(message.getStatus_text());
//            } else {
//                holder.sendStatusView.setVisibility(View.GONE);
//            }
//        }

        holder.toContentTextView.setText(chat.getTo_content());
    }

    private void markAsReadDelayed(final int id, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                markAsRead(id);
            }
        }, delay);
    }

    /**
     * 标记为已读消息
     *
     * @param id
     */
    private void markAsRead(int id) {
        Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
                + ChatProvider.QUERY_URI + "/" + id);
        Log.d(TAG, "markAsRead: " + rowuri);
        ContentValues values = new ContentValues();
        values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_SENT_OR_READ);
        mContext.getContentResolver().update(rowuri, values, null, null);
    }

    @Override
    public int getViewTypeCount() {
        return intValue(ChatType.TYPE_COUNT);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Chat chat = TableContent.ChatTable.parseCursor(cursor);

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
