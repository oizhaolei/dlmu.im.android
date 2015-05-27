package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.model.User;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChatUserListAdapter extends ArrayAdapter<User> {
    private final LayoutInflater viewInflater;

    public ChatUserListAdapter(Context context, int resource) {
        super(context, resource);
        viewInflater = LayoutInflater.from(getContext());
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = viewInflater.inflate(R.layout.item_chat_user,
                    parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == getCount() - 1) {
            holder.userThumbView.setImageResource(R.drawable.invite_btn);
            holder.userThumbView.setTag(null);
            holder.userNameView.setText(R.string.invite_chat);
        } else {
            User user = this.getItem(position);
            holder.userNameView.setText(user.getFullname());
        }

        return convertView;
    }

    public List<User> getUserList() {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < super.getCount(); i++) {
            list.add(getItem(i));
        }
        return list;
    }

    class ViewHolder {
        @InjectView(R.id.item_chat_user_thumb)
        ImageView userThumbView;
        @InjectView(R.id.item_chat_user_name)
        TextView userNameView;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
