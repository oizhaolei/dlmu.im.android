package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.baidu.baidutranslate.openapi.callback.ITransResultCallback;
import com.baidu.baidutranslate.openapi.entity.TransResult;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.XmppRequestTranslateTask;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.AbstractChatCursorAdapter;
import com.ruptech.chinatalk.widget.AbstractMessageCursorAdapter;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends AbstractChatCursorAdapter {
    private static final String TAG = ChatAdapter.class.getName();

    enum ChatType {
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

    private static final int DELAY_NEWMSG = 2000;
    private ActionBarActivity mContext;
    private User mFriendUser;

    public ChatAdapter(ActionBarActivity context, Cursor cursor, String[] from, TranslateClient client, User friend) {
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
        final ViewHolder holder = (ViewHolder) view.getTag();
        Chat chat = TableContent.ChatTable.parseCursor(cursor);
        boolean mine = isMine(chat);
        User user;
        if (mine) {
            chat.setToLang(mFriendUser.getLang());
            chat.setFromLang(App.readUser().getLang());
            user = App.readUser();
        } else {
            chat.setFromLang(mFriendUser.getLang());
            chat.setToLang(App.readUser().getLang());
            user = mFriendUser;
        }

        if (!mine && chat.getRead() == ChatProvider.DS_NEW) {
            markAsReadDelayed(chat.getId(), DELAY_NEWMSG);
        }

        bindProfileThumb(user, holder.userThumbImageView, holder.smsImageView,
                holder.langImageView);
        bindUserThumbClickEvent(holder.userThumbView, user);
        bindImageClickEvent(chat, holder.photoImageView);
        bindVoiceClickEvent(chat, holder.voiceImageView,
                holder.playProcessBar, holder.bubbleLayout);
        bindLayoutClickEvent(chat, holder.bubbleLayout);
        bindFromView(chat, holder);
        bindToView(chat, holder.translatedContentTextView,
                holder.autoTranslationLayout);
        bindDateTimeView(cursor, holder.createDateTextView);
    }

    private void bindFromView(Chat chat, final ViewHolder holder) {
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
        String datetimeStr = TimeUtil.getHourAndMin(chat.getDate());
        holder.timeTextView.setText(datetimeStr);


        // divider
        if (!Utils.isEmpty(chat.getTo_content()) && holder.divider != null) {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat.getType())) {
            // photo
            bindFromPhotoView(chat, holder.photoImageView,
                    holder.photoProgressBar);

        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE
                .equals(chat.getType())) {
            // voice
            bindFromVoiceView(chat, holder.voiceImageView,
                    holder.lengthTextView, holder.playProcessBar);
        } else {// text
            bindFromContentView(chat, holder.contentTextView);
        }

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

        ViewHolder holder = new ViewHolder();
        View view;
        int resID;
        ChatType type = this.getChatType(chat);
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
                if (isMine(chat))
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
