package com.ruptech.chinatalk.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
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
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.ui.FullScreenActivity;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends CursorAdapter {
    private static final String TAG = ChatAdapter.class.getName();
    private static final int DELAY_NEW_MSG = 2000;
    public LayoutInflater mInflater;
    public ContentResolver mContentResolver;
    public int mWidth;
    int CREATE_DATE_INDEX;
    private Context mContext;


    public ChatAdapter(Context context, Cursor cursor, Point displaySize) {
        super(context, cursor, false);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mContentResolver = context.getContentResolver();
        mWidth = displaySize.x;

        CREATE_DATE_INDEX = cursor
                .getColumnIndexOrThrow(ChatTable.Columns.CREATED_DATE);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        Chat chat = ChatTable.parseCursor(cursor);
        return intValue(getChatType(chat));
    }

    public int intValue(ChatType type) {
        return type.ordinal();
    }

    public ChatType getChatType(Chat chat) {
        boolean mine = isMine(chat);

        return mine ? ChatType.MY_TEXT : ChatType.FRIEND_TEXT;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        Chat chat = ChatTable.parseCursor(cursor);
        boolean mine = isMine(chat);

        if (!mine && chat.getRead() == ChatProvider.DS_NEW) {
            markAsReadDelayed(chat.getId(), DELAY_NEW_MSG);
        }

        String portrait = AppVersion.getPortraitUrl(User.getUsernameFromJid(chat.getFromJid()));
        Utils.setUserPicImage(holder.userThumbImageView, portrait);


        bindLayoutClickEvent(chat, holder.bubbleLayout);
        bindFromView(chat, holder);
        bindDateTimeView(cursor, holder.createDateTextView);
    }

    private void bindFromView(Chat chat, final ViewHolder holder) {
        if (holder.contentTextView != null)
            holder.contentTextView.setVisibility(View.GONE);

        // time
        String datetimeStr = TimeUtil.getHourAndMin(chat.getCreated_date());
        holder.timeTextView.setText(datetimeStr);

        bindFromContentView(chat, holder.contentTextView);

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
     */
    private void markAsRead(int id) {
        Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
                + ChatProvider.QUERY_URI + "/" + id);
        Log.d(TAG, "markAsRead: " + rowuri);
        ContentValues values = new ContentValues();
        values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_SENT_OR_READ);
        getContext().getContentResolver().update(rowuri, values, null, null);
    }

    @Override
    public int getViewTypeCount() {
        return intValue(ChatType.TYPE_COUNT);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Chat chat = ChatTable.parseCursor(cursor);

        ViewHolder holder = new ViewHolder();
        View view;
        int resID;
        ChatType type = this.getChatType(chat);
        switch (type) {
            case MY_TEXT:
                resID = R.layout.item_chatting_msg_right_text;
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
        holder.userThumbView = view
                .findViewById(R.id.item_chatting_friend_mask);
        holder.userThumbImageView = (ImageView) view
                .findViewById(R.id.item_chatting_user_thumb_imageview);

        holder.contentTextView = (TextView) view
                .findViewById(R.id.item_chatting_content_textview);


        holder.timeTextView = (TextView) view
                .findViewById(R.id.item_chatting_time_textview);
        view.setTag(holder);
        return view;
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
                dateTextView.setText(DateCommonUtils.formatDateToString(pubDate, true, false));
                dateTextView.setVisibility(View.VISIBLE);
            }
        } else if (prevPubDate == 0 && pubDate > 0) {
            dateTextView.setText(DateCommonUtils.formatDateToString(
                    pubDate, true, true));
            dateTextView.setVisibility(View.VISIBLE);
        }
    }

    protected void bindFromContentView(final Chat chat,
                                       final TextView rightFromContentTextView) {
        if (rightFromContentTextView == null)
            return;

        rightFromContentTextView.setVisibility(View.VISIBLE);
        rightFromContentTextView.setMovementMethod(LinkMovementMethod
                .getInstance());
        rightFromContentTextView.setText(chat.getContent());
    }

    protected void bindLayoutClickEvent(final Chat chat, View layoutView) {
        if (layoutView == null)
            return;

        layoutView.setOnLongClickListener(null);
        layoutView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String content = getMessageMenuToContent(chat);
//                if (!Utils.isEmpty(content))
                {
                    // long click
                    createContentPopMenus(chat, content);
                }
                return true;
            }
        });
    }

    protected void bindProfileThumb(User user, ImageView userThumb) {

    }

    // 译文文本菜单
    void createContentPopMenus(final Chat chat,
                               final String content) {
        List<String> menuList = new ArrayList<>();

        menuList.add(getContext().getString(R.string.message_action_copy));
        menuList.add(getContext().getString(R.string.message_action_share));
        menuList.add(getContext().getString(R.string.message_action_fullscreen));
        menuList.add(getContext().getString(R.string.delete_message));
        menuOnClickListener(menuList, chat, content);
    }

    private void doCopy(Chat chat) {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(chat.getContent());
        Toast.makeText(getContext(),
                getContext().getString(R.string.already_copy_to_clipboard),
                Toast.LENGTH_SHORT).show();
    }

    private void doFullscreen(Chat chat) {
        Intent intent = new Intent(getContext(), FullScreenActivity.class);
        intent.putExtra(FullScreenActivity.EXTRA_MESSAGE, chat.getContent());
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, chat.getContent());

        // start the chooser for sharing
        getContext().startActivity(
                Intent.createChooser(shareIntent,
                        getContext().getString(R.string.app_name)));
    }

    private void doDelete(Chat chat) {

        mContentResolver.delete(ChatProvider.CONTENT_URI, ChatTable.Columns.ID
                + " = ?  ", new String[]{String.valueOf(chat.getId())});
    }

    public Context getContext() {
        return mContext;
    }

    private String getMessageMenuToContent(final Chat chat) {
        return chat.getContent();
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
                        if (getContext().getString(R.string.message_action_copy).equals(selectedItem)) {
                            doCopy(chat);
                        } else if (getContext().getString(R.string.message_action_share).equals(selectedItem)) {
                            doShare(chat);
                        } else if (getContext().getString(R.string.message_action_fullscreen).equals(selectedItem)) {
                            doFullscreen(chat);
                        } else if (getContext().getString(R.string.delete_message).equals(selectedItem)) {
                            doDelete(chat);
                        }
                    }
                });
        alertDialog.setTitle(content).show();
    }

    public boolean isMine(Chat chat) {
        return (chat.getFromJid().equalsIgnoreCase(App.readUser().getJid()));
    }


    enum ChatType {
        MY_TEXT, FRIEND_TEXT, TYPE_COUNT
    }

    static class ViewHolder {
        private TextView createDateTextView;

        private View userThumbView;
        private ImageView userThumbImageView;
        private TextView contentTextView;
        private TextView timeTextView;

        private View bubbleLayout;
    }

}
