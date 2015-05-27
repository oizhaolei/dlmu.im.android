/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AnnouncementListArrayAdapter extends ArrayAdapter<Map<String, String>> {
    private static final int mResource = R.layout.item_announcement_full; // xml布局文件
    private final LayoutInflater mInflater;

    public AnnouncementListArrayAdapter(Context context) {
        super(context, mResource);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        String utcDatetimeStr = DateCommonUtils.convUtcDateString(
                getItem(position).get("create_date"),
                DateCommonUtils.DF_yyyyMMddHHmmss);
        if (Utils.isEmpty(utcDatetimeStr)) {
            holder.listitem_announcement_create_date.setText("");
        } else {
            holder.listitem_announcement_create_date.setText(utcDatetimeStr
                    .substring(0, 11));
        }
        holder.listitem_announcement_title.setText(getItem(position).get("title"));
        holder.listitem_announcement_content.setText(Html.fromHtml(getItem(
                position).get("content")));

        return view;
    }

    static class ViewHolder {

        @InjectView(R.id.listitem_announcement_create_date)
        TextView listitem_announcement_create_date;
        @InjectView(R.id.listitem_announcement_title)
        TextView listitem_announcement_title;
        @InjectView(R.id.listitem_announcement_content)
        TextView listitem_announcement_content;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}