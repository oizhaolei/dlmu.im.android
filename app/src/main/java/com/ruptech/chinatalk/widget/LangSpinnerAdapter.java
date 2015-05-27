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
import android.widget.TextView;

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LangSpinnerAdapter extends BaseAdapter {

    static final String TAG = Utils.CATEGORY + LangSpinnerAdapter.class.getSimpleName();
    private static final int mResource = R.layout.item_main_tab_ttt_spinner_lang; // xml布局文件
    private final String[] langArray;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    public LangSpinnerAdapter(Context context, String[] langArray) {
        this.langArray = langArray;
        if (context != null) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

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
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mResource, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        holder.languageText.setText(Utils.getLangDisplayName(langArray[position]));
        String lang = langArray[position].toLowerCase(Locale.getDefault());
        holder.languageFlagImg.setImageResource(Utils.getLanguageFlag(lang));

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.ttt_spinner_language_text)
        TextView languageText;
        @InjectView(R.id.ttt_spinner_language_flag_img)
        ImageView languageFlagImg;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}