package com.ruptech.chinatalk.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BlockUserAdapter extends SimpleCursorAdapter {
    private static final String[] PROJECTION = new String[]{
            TableContent.UserTable.Columns.PIC_URL,
            TableContent.UserTable.Columns.FULLNAME};
    private LayoutInflater mLayoutInflater;
    private ContentResolver mContentResolver;
    private Activity mContext;

    public BlockUserAdapter(Activity context) {
        super(context, 0, null, PROJECTION, null);
        mContext = context;
        mContentResolver = context.getContentResolver();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void requery() {
        Cursor blockUserCursor = App.userDAO.fetchBlockUsers();
        Cursor oldCursor = getCursor();
        changeCursor(blockUserCursor);
        mContext.stopManagingCursor(oldCursor);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Cursor cursor = this.getCursor();
        cursor.moveToPosition(position);
        final String userName = cursor.getString(cursor
                .getColumnIndex(TableContent.UserTable.Columns.USERNAME));
        String fullname = cursor.getString(cursor
                .getColumnIndex(TableContent.UserTable.Columns.FULLNAME));
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(
                    R.layout.item_block_user, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String portrait = AppVersion.getPortraitUrl(User.getUsernameFromJid(userName));
        Utils.setUserPicImage(viewHolder.portraitImageView, portrait);
        viewHolder.fullnameView.setText(fullname);
        viewHolder.blockReomveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.userDAO.removeBlockUser(User.getUsernameFromJid(userName), "");
                requery();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.item_block_user_portrait)
        ImageView portraitImageView;
        @InjectView(R.id.item_block_user_item_name)
        TextView fullnameView;
        @InjectView(R.id.item_block_remove_btn)
        Button blockReomveButton;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
