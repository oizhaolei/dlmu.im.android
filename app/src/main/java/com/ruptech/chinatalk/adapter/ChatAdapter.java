package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.EmojiParser;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.CustomDialog;

import org.jivesoftware.smack.packet.PacketExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatAdapter extends CursorAdapter {
    private static final String TAG = ChatAdapter.class.getName();

    enum ChatType {
        MY_PHOTO, MY_VOICE, MY_TEXT, FRIEND_PHOTO, FRIEND_VOICE, FRIEND_TEXT, TYPE_COUNT
    }

    private static final int DELAY_NEWMSG = 2000;
    private final TranslateClient mClient;
    private ActionBarActivity mContext;
    private LayoutInflater mInflater;
    private ContentResolver mContentResolver;
    private User mFriendUser;
    public int mWidth;
    int CREATE_DATE_INDEX;

    private final TaskListener mRequestTranslateListener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            XmppRequestTranslateTask fsTask = (XmppRequestTranslateTask) task;
            if (result == TaskResult.OK) {
                Message message = fsTask.getMessage();
                setMessageID(fsTask.getChat().getPid(), message.getMessageid(), message.getTo_content());
                Log.d(TAG, "Request translate Success");
            } else {
                String msg = fsTask.getMsg();
                Log.d(TAG, "Request translate fail:" + msg);
            }
        }

        @Override
        public void onPreExecute(GenericTask task) {

        }

    };

    public void setMessageID(String packetID, long messageID, String to_content) {
        ContentValues cv = new ContentValues();
        cv.put(ChatTable.Columns.MESSAGE_ID, messageID);
        if (to_content == null || to_content.length() == 0)
            cv.put(ChatTable.Columns.TO_MESSAGE, "Translating...");
        else
            cv.put(ChatTable.Columns.TO_MESSAGE, to_content);

        mContentResolver.update(ChatProvider.CONTENT_URI, cv, ChatTable.Columns.PACKET_ID
                + " = ? AND " + ChatTable.Columns.DIRECTION + " = "
                + ChatProvider.INCOMING, new String[]{packetID});
    }

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

    public void baiduTranslate(String content, String fromLang, String toLang) {

        if (TextUtils.isEmpty(content))
            return;

        if (mClient != null){
            mClient.translate(content, fromLang, toLang, new ITransResultCallback() {

                @Override
                public void onResult(TransResult result) {// 翻译结果回调
                    if (result == null) {
                        Log.d(TAG, "Trans Result is null");

                    } else {
                        Log.d(TAG, result.toJSONString());

                        String msg;
                        if (result.error_code == 0) {// 没错
                            msg = result.trans_result;
                        } else {
                            msg = result.error_msg;
                        }
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

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

    private boolean isMine(Chat chat) {
        return (chat.getFromMe() == ChatProvider.OUTGOING);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        Chat chat = TableContent.ChatTable.parseCursor(cursor);

        boolean mine = isMine(chat);
        User user;
        if (mine) {
            user = App.readUser();
        } else {
            user = mFriendUser;
        }

        bindProfileThumb(user, holder.userThumbImageView, holder.smsImageView,
                holder.langImageView);
        bindUserThumbClickEvent(holder.userThumbView, user);
        bindLayoutClickEvent(chat, holder.bubbleLayout);
        bindFromView(chat, holder);
        bindToView(chat, holder.translatedContentTextView,
                holder.autoTranslationLayout);
        bindDateTimeView(cursor, holder.createDateTextView);
    }

    protected void bindLayoutClickEvent(final Chat chat, View layoutView) {
        if (layoutView == null)
            return;

        layoutView.setOnLongClickListener(null);
        if (!isMine(chat)) {
            layoutView
                    .setOnLongClickListener(getToContentLongClickListener(chat));
        } else {
            layoutView
                    .setOnLongClickListener(getFromContentLongClickListener(chat));
        }
    }

    private String getMessageMenuToContent(final Chat chat) {
        String content = chat.getTo_content();
        return content;
    }

    // 译文文本菜单
    void createToContentPopMenus(final Chat chat, View v,
                                 final String content) {
        List<String> menuList = new ArrayList<>();
        menuList.add(mContext.getString(R.string.message_action_copy));
        menuList.add(mContext.getString(R.string.message_action_share));
        menuList.add(mContext.getString(R.string.message_action_fullscreen));
        if (Utils.checkTts(chat.getFromLang())
                || Utils.checkTts(chat.getToLang())) {
            menuList.add(mContext.getString(R.string.message_action_tts));
        }
        menuList.add(mContext.getString(
                R.string.message_action_auto_translation));
        menuList.add(mContext.getString(
                R.string.message_action_accept_translate));
        menuList.add(mContext.getString(R.string.delete_message));
        // 翻译过的，并且未验证，出钱的人可以点击验证
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
//                && message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
//                && ((App.readUser().getId() == message.getUserid() && message
//                .getFee() > 0) || (App.readUser().getId() == message
//                .getTo_userid() && message.getTo_user_fee() > 0))) {
//            menuList.add(mContext.getString(R.string.message_action_verify));
//        }

        menuOnClickListener(menuList, chat, content);
    }

    private void menuOnClickListener(List<String> menuList,
                                     final Chat chat, final String content) {
        final String[] menus = new String[menuList.size()];
        menuList.toArray(menus);
        CustomDialog alertDialog = new CustomDialog(mContext).setTitle(
                mContext.getString(R.string.tips)).setItems(menus,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedItem = menus[which];
                        if (mContext
                                .getString(R.string.message_action_copy)
                                .equals(selectedItem)) {
//                            doCopy(content);
                        } else if (mContext.getString(
                                R.string.message_action_share).equals(
                                selectedItem)) {
//                            doShare(content);
                        } else if (mContext.getString(
                                R.string.message_action_fullscreen).equals(
                                selectedItem)) {
//                            doFullscreen(content);
                        } else if (mContext.getString(
                                R.string.message_action_tts).equals(
                                selectedItem)) {
//                            doTTS(message.from_lang, message.to_lang, content);// from_lang优先
                        } else if (mContext.getString(
                                R.string.delete_message).equals(selectedItem)) {
//                            deleteMessageByMsgId(message.messageid);
                        } else if (mContext.getString(
                                        R.string.message_action_request_translate_again)
                                .equals(selectedItem)) {
//                            doReRequestTranslate(message.getId());
                        } else if (mContext.getString(
                                R.string.message_action_auto_translation)
                                .equals(selectedItem)) {
//                            doAcceptTranslate(message.messageid);
                            baiduTranslate(chat.getMessage(), "zh", "en");
                        } else if (mContext.getString(
                                R.string.message_action_accept_translate)
                                .equals(selectedItem)) {
                            requestTTTalkTranslate(chat, "CN", "KR");
                        } else if (mContext.getString(
                                R.string.message_action_retrieve_message)
                                .equals(selectedItem)) {
//                            retrieveMessageById(message.messageid);
                        } else if (mContext.getString(
                                R.string.message_action_verify).equals(
                                selectedItem)) {
//                            doVerify(message.messageid);
                        }
                    }
                });
        alertDialog.setTitle(content).show();
    }

    private View.OnLongClickListener getFromContentLongClickListener(
            final Chat chat) {
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String content = getMessageMenuFromContent(chat);
//                if (!Utils.isEmpty(content))
                {
                    // long click
                    createFromContentPopMenus(chat, v, content);
                }
                return true;
            }
        };
        return listener;
    }

    // 原文文本菜单
    void createFromContentPopMenus(final Chat chat, View v,
                                   final String content) {
        List<String> menuList = new ArrayList<>();
        if (!AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
                .getType())
                && !AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
                .getType())) {

            menuList.add(mContext.getString(R.string.message_action_copy));
            menuList.add(mContext.getString(R.string.message_action_share));
            menuList.add(mContext.getString(
                    R.string.message_action_fullscreen));
            if (Utils.checkTts(chat.getFromLang())
                    || Utils.checkTts(chat.getToLang())) {
                menuList.add(mContext
                        .getString(R.string.message_action_tts));
            }
            // 正在请求中的可以请求自动翻译
//            if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS)
//            {
//                menuList.add(mContext.getString(
//                        R.string.message_action_auto_translation));
//            }
        }
        menuList.add(mContext.getString(R.string.delete_message));
        // 请求失败可以再请求一次
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_SEND_FAILED) {
//            menuList.add(getContext().getString(
//                    R.string.message_action_request_translate_again));
//        } else if (message.getTo_userid() == App.readUser().getId()
//                && message.getMessage_status() == AppPreferences.MESSAGE_STATUS_NO_TRANSLATE
//                && (!message.getFrom_lang().equals(message.getTo_lang())
//                && !AppPreferences.MESSAGE_TYPE_NAME_PHOTO
//                .equals(message.file_type) && Utils
//                .isEmpty(message.getTo_content()))) {
//            // 只有接收者才能点击接受翻译；
//            // 缺钱的话，发送者也不能点击接受翻译。
//            menuList.add(getContext().getString(
//                    R.string.message_action_accept_translate));
//        }
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_REQUEST_TRANS
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATING
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATE
//                || message.getMessage_status() == AppPreferences.MESSAGE_STATUS_ACCEPT_TRANSLATING) {
//            menuList.add(getContext().getString(
//                    R.string.message_action_retrieve_message));
//        }
//        // 翻译过的，并且未验证，出钱的人可以点击验证
//        if (message.getMessage_status() == AppPreferences.MESSAGE_STATUS_TRANSLATED
//                && message.getVerify_status() < AppPreferences.VERIFY_STATUS_REQUEST
//                && ((App.readUser().getId() == message.getUserid() && message
//                .getFee() > 0) || (App.readUser().getId() == message
//                .getTo_userid() && message.getTo_user_fee() > 0))) {
//            menuList.add(getContext().getString(R.string.message_action_verify));
//        }
        menuOnClickListener(menuList, chat, content);
    }

    private String getMessageMenuFromContent(final Chat chat) {
        String content;
        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO.equals(chat
                .getType())) {
            content = mContext.getString(R.string.notification_picture);
        } else if (AppPreferences.MESSAGE_TYPE_NAME_VOICE.equals(chat
                .getType())) {
            content = mContext.getString(R.string.notification_voice);
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
                final String content = getMessageMenuToContent(chat);
//                if (!Utils.isEmpty(content))
                {
                    // long click
                    createToContentPopMenus(chat, v, content);
                }
                return true;
            }
        };
        return listener;
    }

    protected void bindUserThumbClickEvent(View userThumb, final User user) {

        if (userThumb == null)
            return;

        userThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,
                        FriendProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER, user);
                mContext.startActivity(intent);
            }
        });
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

    protected void bindDateTimeView(Cursor cursor, final TextView dateTextView) {
        long pubDate = cursor.getLong(CREATE_DATE_INDEX);
        long prevPubDate;
        if (cursor.moveToPrevious()) {
            prevPubDate = cursor.getLong(CREATE_DATE_INDEX);
            cursor.moveToNext();
        } else {
            prevPubDate = 0;
        }

        dateTextView.setVisibility(View.GONE);
        if (prevPubDate > 0 && pubDate > 0) {
            boolean isDiff = (pubDate - prevPubDate) > DateCommonUtils.CHAT_TIME_SPAN_SIZE;
            if (isDiff) {
                dateTextView.setText(TimeUtil.getChatTime(pubDate));
                dateTextView.setVisibility(View.VISIBLE);
            }
        } else if (prevPubDate == 0 && pubDate > 0) {
            dateTextView.setText(TimeUtil.getChatTime(pubDate));
            dateTextView.setVisibility(View.VISIBLE);
        }
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


    protected void bindToView(final Chat chat,
                              TextView translatedContentTextView, View autoTranslationLayout) {

        if (translatedContentTextView == null || autoTranslationLayout == null)
            return;

        translatedContentTextView.setVisibility(View.GONE);
        autoTranslationLayout.setVisibility(View.GONE);

        final String toContent = chat.getTo_content();
        if (AppPreferences.MESSAGE_TYPE_NAME_PHOTO
                .equals(chat.getType()) || Utils.isEmpty(toContent)) {
            //
        } else {

            translatedContentTextView.setVisibility(View.VISIBLE);
//            if (chat.getAuto_translate() == AppPreferences.AUTO_TRANSLATE_MESSSAGE) {
//                autoTranslationLayout.setVisibility(View.VISIBLE);
//
//                bindAutoTranslationClick(message, autoTranslationLayout,
//                        this.getContext());
//            }
            String unicode = EmojiParser.getInstance(mContext).parseEmoji(
                    chat.getTo_content());
            SpannableString spannableString = ParseEmojiMsgUtil
                    .getExpressionString(mContext, unicode);
            if (spannableString != null) {
                translatedContentTextView.setText(spannableString);
            } else {
                translatedContentTextView.setText(chat.getTo_content());
            }
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

    private void requestTTTalkTranslate(Chat chat, String fromLang, String toLang) {
        Collection<PacketExtension> extensions = new ArrayList<>();
        String callback_id = chat.getPid();
        requestTranslate(chat, fromLang, toLang);
    }

    public void requestTranslate(Chat chat, String from_lang, String to_lang) {
        XmppRequestTranslateTask mRequestTranslateTask = new XmppRequestTranslateTask(chat, from_lang, to_lang);
        mRequestTranslateTask.setListener(mRequestTranslateListener);
        mRequestTranslateTask.execute();
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
                .getExpressionString(mContext, unicode);
        if (spannableString != null) {
            rightFromContentTextView.setText(spannableString);
        } else {
            rightFromContentTextView.setText(chat.getMessage());
        }
    }

    protected void bindFromPhotoView(final Chat chat,
                                     ImageView photoImageView, ProgressBar progressBar) {
        if (photoImageView == null)
            return;

        photoImageView.setVisibility(View.VISIBLE);
        final String file_path = chat.getFilePath();
        String fileName = file_path.substring(file_path.lastIndexOf('/') + 1);
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        if (!Utils.isEmpty(fileName)) {
            String tag = (String) photoImageView.getTag();
            if (!fileName.equals(tag)) {
                ImageManager.imageLoader.displayImage(App
                                .readServerAppInfo().getServerMiddle(fileName),
                        photoImageView, ImageManager.getOptionsLandscape());
                photoImageView.setTag(fileName);
                setItemSize(photoImageView);
            } else {
                photoImageView.setImageBitmap(ImageManager
                        .getDefaultLandscape(mContext));
                photoImageView.setTag(null);
            }
        } else {
            photoImageView.setImageBitmap(ImageManager
                    .getDefaultLandscape(mContext));
            photoImageView.setTag(null);

        }

        photoImageView.setPadding(2, 2, 2, 2);
    }

    protected void bindFromVoiceView(final Chat chat,
                                     final ImageView rightVoiceImageView, TextView rightLengthTextView,
                                     final ProgressBar rightPlayProcessBar) {

        if (rightVoiceImageView == null || rightLengthTextView == null
                || rightPlayProcessBar == null)
            return;

        rightVoiceImageView.setVisibility(View.VISIBLE);
        rightVoiceImageView.setTag(chat.getFilePath());
        rightLengthTextView.setVisibility(View.VISIBLE);
        rightLengthTextView.setText(chat.getFromContentLength() + "'");
        // 下载语音
        File voiceFolder = Utils.getVoiceFolder(mContext);
        File mVoiceFile = new File(voiceFolder, chat.getFilePath());
        File mDownVoiceFile = new File(voiceFolder, chat.getFilePath()
                + AppPreferences.VOICE_SURFIX);
        if (!mVoiceFile.exists() && !mDownVoiceFile.exists()) {
            if (!Utils.isEmpty(chat.getFilePath())) {
                Utils.uploadVoiceFile(mContext, chat.getFilePath(), null);
            }
        }
    }

    private void setItemSize(View itemView) {
        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams) itemView.getLayoutParams();
        imageParams.width = mWidth / 4;
        imageParams.height = mWidth / 4;
        itemView.setLayoutParams(imageParams);
    }
}
