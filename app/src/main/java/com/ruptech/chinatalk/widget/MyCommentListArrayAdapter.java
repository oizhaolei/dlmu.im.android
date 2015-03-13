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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MyCommentListArrayAdapter extends ArrayAdapter<UserPhoto> {
	static class ViewHolder {
		@InjectView(R.id.item_my_comment_content_textview)
		TextView contentTextView;
		@InjectView(R.id.item_my_comment_lang_imageview)
		ImageView userLangImgView;
		@InjectView(R.id.item_my_comment_to_content_textview)
		TextView toContentTextView;
		@InjectView(R.id.item_my_comment_to_lang_imageview)
		ImageView toUserLangImgView;
		@InjectView(R.id.item_my_comment_imageview)
		ImageView photoImgView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_my_comment;

	private final Context mContext;

	private final LayoutInflater mInflater;

	public MyCommentListArrayAdapter(Context context) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	void bindUserPhoto(int position, final Context context,
			final ViewHolder holder, final UserPhoto userPhoto,
			final BaseAdapter adapter) {
		String storyPic = userPhoto.getPic_url();
		Utils.setUserPicImage(holder.photoImgView, storyPic);

		// content
		final String content = userPhoto.getContent();
		if (Utils.isEmpty(content)) {
			holder.contentTextView.setVisibility(View.GONE);
			holder.userLangImgView.setVisibility(View.GONE);
		} else {
			holder.contentTextView.setVisibility(View.VISIBLE);
			holder.userLangImgView.setVisibility(View.VISIBLE);
			holder.userLangImgView.setImageResource(Utils
					.getLanguageFlag(userPhoto.getLang()));
			String createDate = DateCommonUtils.dateFormat(
					userPhoto.getCreate_date(),
					DateCommonUtils.DF_yyyyMMddHHmmssSSS);
			String contentDate = DateCommonUtils.formatConvUtcDateString(
					createDate, false, false);
			holder.contentTextView.setText(Html.fromHtml(Utils
					.highlightTag(Utils.htmlSpecialChars(content))
					+ "<small><i> -" + contentDate + "</i></small>"));
		}
		final String toContent = userPhoto.getTo_content();
		if (Utils.isEmpty(toContent)) {
			holder.toContentTextView.setVisibility(View.GONE);
			holder.toUserLangImgView.setVisibility(View.GONE);
		} else {
			holder.toContentTextView.setVisibility(View.VISIBLE);
			holder.toUserLangImgView.setVisibility(View.VISIBLE);
			holder.toContentTextView.setText(toContent);
			holder.toUserLangImgView.setImageResource(Utils
					.getLanguageFlag(userPhoto.getTo_lang()));
			holder.toContentTextView.setTextColor(context.getResources()
					.getColor(R.color.text_black));
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final UserPhoto userPhoto = getItem(position);

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

		bindUserPhoto(position, mContext, holder, userPhoto, this);
		return view;
	}

	ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

}