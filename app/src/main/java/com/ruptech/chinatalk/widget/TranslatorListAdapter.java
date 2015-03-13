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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TranslatorListAdapter extends ArrayAdapter<Map<String, String>> {
	static class ViewHolder {
		@InjectView(R.id.item_translator_thumb_imageview)
		ImageView translatorThumb;
		@InjectView(R.id.item_translator_lang1_flag_imageview)
		ImageView translatorLang1Flag;
		@InjectView(R.id.item_translator_lang2_flag_imageview)
		ImageView translatorLang2Flag;
		@InjectView(R.id.item_translator_fullname_textview)
		TextView fullName;
		@InjectView(R.id.item_translator_translate_number_textview)
		TextView translatorNumber;
		@InjectView(R.id.translator_lang1_introduce_layout)
		View translatorLang1IntroduceView;
		@InjectView(R.id.translator_lang2_introduce_layout)
		View translatorLang2IntroduceView;
		@InjectView(R.id.translator_lang1_introduce_textview)
		TextView translatorLang1IntroduceTextView;
		@InjectView(R.id.translator_lang2_introduce_textview)
		TextView translatorLang2IntroduceTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	static final String TAG = Utils.CATEGORY
			+ TranslatorListAdapter.class.getSimpleName();
	private static final int mResource = R.layout.item_translator;
	private final LayoutInflater mInflater;
	private final Context mContext;

	public TranslatorListAdapter(Context context) {
		super(context, mResource);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = mInflater.inflate(mResource, null);
			holder = new ViewHolder(view);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		view.setTag(holder);
		final Map<String, String> rankedMap = getItem(position);

		Utils.setUserPicImage(holder.translatorThumb, rankedMap.get("pic_url"));

		holder.translatorLang1Flag.setImageResource(Utils
				.getLanguageFlag(rankedMap.get("lang1")));
		holder.translatorLang2Flag.setImageResource(Utils
				.getLanguageFlag(rankedMap.get("lang2")));

		String fullname = Utils.getFriendName(
				Long.valueOf(rankedMap.get("t_user_id")),
				rankedMap.get("fullname"));
		holder.fullName.setText(fullname);

		String formatNumber = Utils.currencyFormat(Integer.parseInt(rankedMap
				.get("translate_number")));
		holder.translatorNumber.setText(mContext.getString(
				R.string.translate_number, formatNumber));

		// expand
		if (Utils.isEmpty(rankedMap.get("self_introduction_lang1"))) {
			holder.translatorLang1IntroduceView.setVisibility(View.GONE);
		} else {
			holder.translatorLang1IntroduceView.setVisibility(View.VISIBLE);
			holder.translatorLang1IntroduceTextView.setText(rankedMap
					.get("self_introduction_lang1"));
		}
		if (Utils.isEmpty(rankedMap.get("self_introduction_lang2"))) {
			holder.translatorLang2IntroduceView.setVisibility(View.GONE);
		} else {
			holder.translatorLang2IntroduceView.setVisibility(View.VISIBLE);
			holder.translatorLang2IntroduceTextView.setText(rankedMap
					.get("self_introduction_lang2"));
		}

		return view;
	}

}