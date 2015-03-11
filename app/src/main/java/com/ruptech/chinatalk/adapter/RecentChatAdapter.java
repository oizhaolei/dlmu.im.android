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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.TimeUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecentChatAdapter extends SimpleCursorAdapter {
    private static final String SELECT = ChatTable.Columns.DATE
            + " in (select max(" + ChatTable.Columns.DATE + ") from "
            + ChatProvider.TABLE_NAME + " group by " + ChatTable.Columns.JID
            + " having count(*)>0)";// 查询合并重复jid字段的所有聊天对象
    private static final String[] FROM = new String[]{
            ChatTable.Columns.ID, ChatTable.Columns.DATE,
            ChatTable.Columns.DIRECTION,
            ChatTable.Columns.JID, ChatTable.Columns.MESSAGE,
            ChatTable.Columns.DELIVERY_STATUS};// 查询字段
    private static final String SORT_ORDER = ChatTable.Columns.DATE + " DESC";
    private ContentResolver mContentResolver;
    private LayoutInflater mLayoutInflater;
    private Activity mContext;

    public RecentChatAdapter(Activity context) {
        super(context, 0, null, FROM, null);
        mContext = context;
        mContentResolver = context.getContentResolver();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void requery() {
        Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI, FROM,
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
                .getColumnIndex(ChatTable.Columns.DATE));
        String date = TimeUtil.getChatTime(dateMilliseconds);
        String message = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.MESSAGE));
        String jid = cursor.getString(cursor
                .getColumnIndex(ChatTable.Columns.JID));

        String selection = ChatTable.Columns.JID + " = '" + jid + "' AND "
                + ChatTable.Columns.DIRECTION + " = " + ChatProvider.INCOMING
                + " AND " + ChatTable.Columns.DELIVERY_STATUS + " = "
                + ChatProvider.DS_NEW;// 新消息数量字段
        Cursor msgcursor = mContentResolver.query(ChatProvider.CONTENT_URI,
                new String[]{"count(" + ChatTable.Columns.PACKET_ID + ")",
                        ChatTable.Columns.DATE, ChatTable.Columns.MESSAGE}, selection,
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
        String name = Utils.getFriendNameFromOF_JID(jid);
        viewHolder.jidView.setText(name);
        viewHolder.msgView.setText(XMPPUtils
                .convertNormalStringToSpannableString(mContext, message, true));
        viewHolder.dataView.setText(date);

        String thumb = Utils.getPicUrlFromOF_JID(jid);
        Utils.setUserPicImage(viewHolder.icon, thumb);

        if (msgcursor.getInt(0) > 0) {
            viewHolder.msgView.setText(msgcursor.getString(msgcursor
                    .getColumnIndex(ChatTable.Columns.MESSAGE)));
            viewHolder.dataView.setText(TimeUtil.getChatTime(msgcursor
                    .getLong(msgcursor.getColumnIndex(ChatTable.Columns.DATE))));
            viewHolder.unReadView.setText(msgcursor.getString(0));
        }
        viewHolder.unReadView.setVisibility(count > 0 ? View.VISIBLE
                : View.GONE);
        viewHolder.unReadView.bringToFront();
        msgcursor.close();

        return convertView;
    }


    static class ViewHolder {
        @InjectView(R.id.icon)
        ImageView icon;
        @InjectView(R.id.recent_list_item_name)
        TextView jidView;
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
