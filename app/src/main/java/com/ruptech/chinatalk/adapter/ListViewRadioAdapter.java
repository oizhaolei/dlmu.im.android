package com.ruptech.chinatalk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruptech.dlmu.im.R;

import java.util.List;
import java.util.Map;

/**
 * Created by gaol on 2015-05-20.
 */
public class ListViewRadioAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Map<String, String>> data;
    private viewHolder holder;
    private int index = -1;
    private Context c;

    public ListViewRadioAdapter(Context c, List<Map<String, String>> data) {
        super();
        this.c = c;
        this.data = data;
        inflater = LayoutInflater.from(c);
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
            convertView = inflater.inflate(R.layout.activity_checkin_classroom_listview, null);
            holder.textView = (TextView) convertView.findViewById(R.id.activity_checkin_classroom_listview_textview);
            holder.radio = (RadioButton) convertView
                    .findViewById(R.id.activity_checkin_classroom_listview_radio);
            convertView.setTag(holder);
        } else {
            holder = (viewHolder) convertView.getTag();
        }

        holder.radio.setClickable(false);

        holder.textView.setText(data.get(position).get("TITLE"));
        holder.radio
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            index = position;
                            notifyDataSetChanged();
                        }
                    }
                });

        if (index == position) {
            holder.radio.setChecked(true);
        } else {
            holder.radio.setChecked(false);
        }
        return convertView;
    }

    public class viewHolder {
        public RadioButton radio;
        public TextView textView;
    }
}
