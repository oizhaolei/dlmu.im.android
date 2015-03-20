/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ruptech.chinatalk.utils.Utils;

import java.util.Locale;

public class CountryListViewAdapter extends AbstractLangListViewAdapter {

	public CountryListViewAdapter(Context context, String[] langArray,
	                              int clickPosition) {
		this.mContext = context;
		this.langArray = langArray;
		this.clickPosition = clickPosition;
		if (context != null) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
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

		String langDisplayName = Utils.getLangDisplayName(langArray[position]);
		holder.languageText.setText(langDisplayName);

		String lang = langArray[position].toLowerCase(Locale.getDefault());
		holder.languageFlagImg.setImageResource(Utils.getLanguageFlag(lang));

		if (position == clickPosition) {
			holder.languageRadioButton.setChecked(true);
			holder.languageRadioButton.setFocusable(true);
		} else {
			holder.languageRadioButton.setChecked(false);
			holder.languageRadioButton.setFocusable(false);
		}
		return view;
	}
}