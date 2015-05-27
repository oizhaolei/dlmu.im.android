package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecentChatAdapter extends SimpleCursorAdapter {
    private static final String SELECT = ChatTable.Columns.CREATED_DATE
            + " in (select max(" + ChatTable.Columns.CREATED_DATE + ") from "
            + ChatProvider.TABLE_NAME + " group by " + ChatTable.Columns.FROM_JID
            + " having count(*)>0)";// 查询合并重复jid字段的所有聊天对象
    private static final String[] PROJECTION = new String[]{
            ChatTable.Columns.ID,
            ChatTable.Columns.CREATED_DATE,
            ChatTable.Columns.FROM_JID,
            ChatTable.Columns.TO_JID,
            ChatTable.Columns.FROM_FULLNAME,
            ChatTable.Columns.TO_FULLNAME,
            ChatTable.Columns.CONTENT,
            ChatTable.Columns.DELIVERY_STATUS};// 查询字段
    private static final String SORT_ORDER = ChatTable.Columns.CREATED_DATE + " DESC";
    private ContentResolver mContentResolver;
    private LayoutInflater mLayoutInflater;
    private Activity mContext;

    public RecentChatAdapter(Activity context) {
        super(context, 0, null, PROJECTION, null);
        mContext = context;
        mContentResolver = context.getContentResolver();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void requery() {
        Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI, PROJECTION,
                SELECT, null, SORT_ORDER);
        Cursor oldCursor = getCursor();
        changeCursor(cursor);
        mContext.stopManagingCursor(oldCursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Cursor cursor = this.getCursor();
        cursor.moveToPosition(position);
        long dateMilliseconds = cursor.getLong(cursor
                .getColumnIndex(ChatTable.Columns.CREATED_DATE));
        String date = DateCommonUtils.formatDateToString(dateMilliseconds, false, false);
        String message = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.CONTENT));
        String from_Jid = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.FROM_JID));
        String to_Jid = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.TO_JID));
        String fromFullname = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.FROM_FULLNAME));
        String toFullname = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.TO_FULLNAME));

        String selection = ChatTable.Columns.TO_JID + " = '" + to_Jid + "' AND "
                + ChatTable.Columns.FROM_JID + " = '" + App.readUser().getJid()
                + "' AND " + ChatTable.Columns.DELIVERY_STATUS + " = "
                + ChatProvider.DS_NEW;// 新消息数量字段
        Cursor msgcursor = mContentResolver.query(ChatProvider.CONTENT_URI,
                new String[]{"count(" + ChatTable.Columns.PACKET_ID + ")",
                        ChatTable.Columns.CREATED_DATE, ChatTable.Columns.CONTENT}, selection,
                null, SORT_ORDER);
        msgcursor.moveToFirst();
        int count = msgcursor.getInt(0);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(
                    R.layout.item_recent_chat, parent, false);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String jid = to_Jid;
        String fullname = toFullname;
        if (to_Jid.equals(App.readUser().getJid())) {
            jid = from_Jid;
            fullname = fromFullname;
        }

        String portrait = AppVersion.getPortraitUrl(User.getUsernameFromJid(jid));
        Utils.setUserPicImage(viewHolder.portraitImageView, portrait);

        viewHolder.fullnameView.setText(fullname);
        viewHolder.msgView.setText(XMPPUtils
                .convertNormalStringToSpannableString(mContext, message, true));
        viewHolder.dataView.setText(date);


        if (msgcursor.getInt(0) > 0) {
            viewHolder.msgView.setText(msgcursor.getString(msgcursor
                    .getColumnIndex(ChatTable.Columns.CONTENT)));
            viewHolder.dataView.setText(DateCommonUtils.formatDateToString(msgcursor
                    .getLong(msgcursor.getColumnIndex(ChatTable.Columns.CREATED_DATE)), false, false));
            viewHolder.unReadView.setText(msgcursor.getString(0));
        }
        viewHolder.unReadView.setVisibility(count > 0 ? View.VISIBLE
                : View.GONE);
        viewHolder.unReadView.bringToFront();
        msgcursor.close();

        return convertView;
    }


    static class ViewHolder {
        @InjectView(R.id.recent_portrait)
        ImageView portraitImageView;
        @InjectView(R.id.recent_list_item_name)
        TextView fullnameView;
        @InjectView(R.id.recent_list_item_time)
        TextView dataView;
        @InjectView(R.id.recent_list_item_msg)
        TextView msgView;
        @InjectView(R.id.unreadmsg)
        TextView unReadView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
