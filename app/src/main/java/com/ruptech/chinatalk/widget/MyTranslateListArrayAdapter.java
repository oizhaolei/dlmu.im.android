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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

public class MyTranslateListArrayAdapter extends ArrayAdapter<StoryTranslate> {
	static class ViewHolder {
		@InjectView(R.id.item_my_comment_content_textview)
		TextView contentTextView;
		@InjectView(R.id.item_my_comment_lang_imageview)
		ImageView userLangImgView;
		@InjectView(R.id.item_my_comment_to_content_textview)
		TextView toContentTextView;
		@InjectView(R.id.item_my_comment_to_lang_imageview)
		ImageView toUserLangImgView;
		@InjectView(R.id.item_user_translate_good_textview)
		TextView likeCountTextView;
		@InjectView(R.id.item_user_story_good_imageview)
		ImageView likeImgView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_my_translate;

	private final Context mContext;

	private final LayoutInflater mInflater;

	public MyTranslateListArrayAdapter(Context context) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	void bindUserPhoto(int position, final Context context,
			final ViewHolder holder, final StoryTranslate storyTranslate,
			final BaseAdapter adapter) {

		// content
		final String content = storyTranslate.from_content;
		if (Utils.isEmpty(content)) {
			holder.contentTextView.setVisibility(View.GONE);
			holder.userLangImgView.setVisibility(View.GONE);
		} else {
			holder.contentTextView.setVisibility(View.VISIBLE);
			holder.userLangImgView.setVisibility(View.VISIBLE);
			holder.userLangImgView.setImageResource(Utils
					.getLanguageFlag(storyTranslate.from_lang));
			String createDate = storyTranslate.getCreate_date();
			String contentDate = DateCommonUtils.formatConvUtcDateString(
					createDate,
					false, false);
			holder.contentTextView.setText(Html.fromHtml(Utils
					.highlightTag(Utils.htmlSpecialChars(content))
					+ "<small><i> -"
					+ contentDate
					+ "</i></small>"));
		}
		final String toContent = storyTranslate.getTo_content();
		if (Utils.isEmpty(toContent)) {
			holder.toContentTextView.setVisibility(View.GONE);
			holder.toUserLangImgView.setVisibility(View.GONE);
		} else {
			holder.toContentTextView.setVisibility(View.VISIBLE);
			holder.toUserLangImgView.setVisibility(View.VISIBLE);
			holder.toContentTextView.setText(toContent);
			holder.toUserLangImgView.setImageResource(Utils
					.getLanguageFlag(storyTranslate.getLang()));
			holder.toContentTextView.setTextColor(context.getResources()
					.getColor(R.color.text_black));
		}

		holder.likeCountTextView.setText(String.valueOf(storyTranslate
				.getGood()));

		int likeIconRes;
		if (storyTranslate.getFavorite() > 0) {
			likeIconRes = R.drawable.ic_action_social_like_selected;
		} else {
			likeIconRes = R.drawable.ic_action_social_like_unselected;
		}
		holder.likeImgView.setImageResource(likeIconRes);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final StoryTranslate storyTranslate = getItem(position);

		View view;
		final ViewHolder holder;

		if (convertView == null) {
			view = mInflater.inflate(mResource, parent, false);

			holder = getViewHolder(view);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();

		}

		bindUserPhoto(position, mContext, holder, storyTranslate, this);
		return view;
	}

	ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	public void updateTranslate(StoryTranslate storyTranslate) {
		for (int i = 0; i < getCount(); i++) {
			StoryTranslate item = getItem(i);
			if (item.getId() == storyTranslate.getId()) {
				item.to_content = storyTranslate.getTo_content();
				item.fullname = storyTranslate.fullname;
				item.user_id = storyTranslate.user_id;
				item.favorite = storyTranslate.favorite;
				item.good = storyTranslate.good;
				item.create_date = storyTranslate.create_date;
				notifyDataSetChanged();
				break;
			}
		}
	}

}