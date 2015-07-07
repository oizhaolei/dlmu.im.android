package com.ruptech.chinatalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaol on 2015-05-20.
 */
public class ListViewServiceSettingAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Service> data;
    private viewHolder holder;
    // 用来控制CheckBox的选中状况
    public static HashMap<Integer, Boolean> isSelected;

    private Context c;

    public ListViewServiceSettingAdapter(Context c, List<Service> data) {
        super();
        this.c = c;
        this.data = data;
        inflater = LayoutInflater.from(c);
        isSelected = new HashMap<Integer, Boolean>();
        for (int i = 0; i < data.size(); i++) {
            isSelected.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        holder = new viewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_service_setting, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_service_setting_listview_imageview);
            holder.textView = (TextView) convertView.findViewById(R.id.item_service_setting_listview_textview);
            holder.check = (CheckBox) convertView
                    .findViewById(R.id.item_service_setting_listview_checkbox);
            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        if (!data.get(position).getIcon().isEmpty()) {
            Utils.setUserPicImage(holder.imageView, data.get(position).getIcon());
        }
        holder.textView.setText(data.get(position).getTitle());
        holder.check.setChecked(isSelected.get(position));
        return convertView;
    }

    public class viewHolder {
        public CheckBox check;
        public ImageView imageView;
        public TextView textView;
    }
}
