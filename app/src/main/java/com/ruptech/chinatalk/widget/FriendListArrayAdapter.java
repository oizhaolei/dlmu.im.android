/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FriendListArrayAdapter extends ArrayAdapter<User> {
    private static final int mResource = R.layout.item_main_tab_friend; // xml布局文件
    protected LayoutInflater mInflater;

    public FriendListArrayAdapter(Context context) {
        super(context, mResource);

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view;
        final ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();

        }

        User user = getItem(position);

        holder.friendTel.setText(Utils.abbrString(user.getUsername(), 20));
        String fullName = user.getFullname();
        if (Utils.isEmpty(fullName)) {
            holder.friendFullName.setText("");
        } else {
            holder.friendFullName.setText(Utils.abbrString(fullName, 8));
        }


        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.item_main_tab_friend_thumb_imageview)
        ImageView friendThumb;
        @InjectView(R.id.item_main_tab_friend_tel_textview)
        TextView friendTel;
        @InjectView(R.id.item_main_tab_friend_fullname_textview)
        TextView friendFullName;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}