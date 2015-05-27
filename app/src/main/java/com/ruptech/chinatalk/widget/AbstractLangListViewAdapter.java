/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AbstractLangListViewAdapter extends BaseAdapter {

    public static final int mResource = R.layout.item_main_tab_profile_lang; // xml布局文件
    static final String TAG = Utils.CATEGORY + AbstractLangListViewAdapter.class.getSimpleName();
    public String[] langArray;
    public int clickPosition = -1;
    public boolean itemCanClick = true;
    public Context mContext;
    public ViewHolder holder;
    public LayoutInflater mInflater;

    @Override
    public int getCount() {
        return langArray.length;
    }

    @Override
    public Object getItem(int position) {
        return langArray[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        return view;
    }

    public void setItem(int position) {
        this.clickPosition = position;
    }

    public static class ViewHolder {
        @InjectView(R.id.language_text)
        TextView languageText;
        @InjectView(R.id.language_radio_btn)
        RadioButton languageRadioButton;
        @InjectView(R.id.language_flag_img)
        ImageView languageFlagImg;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}