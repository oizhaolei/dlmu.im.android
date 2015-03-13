/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.story.UserStoryImageViewActivity;
import com.ruptech.chinatalk.ui.story.UserStoryTranslateActivity;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class UserStoryCommentListArrayAdapter extends ArrayAdapter<UserPhoto> {
	public static class ViewHolder {
		@InjectView(R.id.item_story_comment_content_textview)
		TextView contentTextView;
		@InjectView(R.id.item_story_comment_my_content_textview)
		TextView contentTranslateTextView;
		@InjectView(R.id.item_story_comment_my_content_view)
		View myContentView;
		@InjectView(R.id.item_story_comment_user_imageview)
		ImageView userImgView;
		@InjectView(R.id.item_story_comment_fullname_textview)
		TextView usernameTextView;
		@InjectView(R.id.item_mask)
		ImageView userImgMaskView;
		@InjectView(R.id.item_story_comment_lang_imageview)
		ImageView userLangImgView;
		@InjectView(R.id.item_story_comment_my_lang_imageview)
		ImageView userMyLangImgView;
		@InjectView(R.id.item_story_comment_my_content_image)
		ImageView userCommentImage;
		@InjectView(R.id.item_user_story_comment_delete_view)
		View userCommentDeleteView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static final int mResource = R.layout.item_story_user_comment; // xml布局文件

	public static void bindUserPhoto(final Context context,
			final ViewHolder holder, final UserPhoto userPhoto) {
		// user photo
		String userPic = userPhoto.getUser_pic();

		Utils.setUserPicImage(holder.userImgView, userPic);

		String commentPic = userPhoto.getPic_url();
		if (!Utils.isEmpty(commentPic)) {
			if (!commentPic.equals(holder.userCommentImage.getTag())) {
				ImageManager.imageLoader.displayImage(App.readServerAppInfo()
						.getServerThumbnail(commentPic),
						holder.userCommentImage, ImageManager
								.getOptionsLandscape());
			}
			holder.userCommentImage.setTag(commentPic);
			holder.userCommentImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							UserStoryImageViewActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);

					context.startActivity(intent);
				}

			});
			holder.userCommentImage.setVisibility(View.VISIBLE);
		} else {
			holder.userCommentImage.setVisibility(View.GONE);
			holder.userCommentImage.setTag(null);
		}

		holder.userImgMaskView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryCommentActivity.gotoProfileAcitivity(userPhoto,
						context);
			}
		});
		holder.userLangImgView.setImageResource(Utils.getLanguageFlag(userPhoto
				.getLang()));

		// fullname
		String create_date = DateCommonUtils.dateFormat(
				userPhoto.getCreate_date(),
				DateCommonUtils.DF_yyyyMMddHHmmssSSS);
		String content_date = DateCommonUtils.formatConvUtcDateString(
				create_date, false, false);
		String fullname = Utils.getFriendName(userPhoto.getUserid(),
				userPhoto.getFullname());
		holder.usernameTextView.setText(Html.fromHtml(Utils
				.htmlSpecialChars(fullname)
				+ "<small><i> -"
				+ content_date
				+ "</i></small>"));
		holder.usernameTextView.getPaint().setFakeBoldText(true);
		// content
		final String content = userPhoto.getContent();
		final String to_content = userPhoto.getTo_content();
		boolean isNoNeedTranslate = true;
		if (Utils.isEmpty(content)) {
			holder.contentTextView.setVisibility(View.GONE);
		} else {
			isNoNeedTranslate = ParseEmojiMsgUtil.isNoNeedTranslate(content,
					context);
			holder.contentTextView.setVisibility(View.VISIBLE);
			if (Utils.isEmpty(to_content) || isNoNeedTranslate) {
				holder.contentTextView.setText(Html.fromHtml(Utils
						.highlightTag(Utils.htmlSpecialChars(content))));
				holder.contentTextView.setClickable(false);
			} else {
				int tipResID = R.string.request_translate;
				if (!Utils.showUserStoryTranslateBtn(userPhoto)) {// 显示翻译按钮
					tipResID = R.string.view_translate;
				}

				holder.contentTextView
						.setText(Html.fromHtml(Utils.highlightTag(Utils
								.htmlSpecialChars(content))
								+ "&nbsp;<small><font color='#191970'> - "
								+ App.mContext.getString(tipResID)
								+ "</font></small>"));

				holder.contentTextView
						.setOnClickListener(new OnClickListener() {
							private void gotoStoryTranslateActivity() {
								Intent intent = new Intent(context,
										UserStoryTranslateActivity.class);
								intent.putExtra(
										UserStoryCommentActivity.EXTRA_USER_PHOTO,
										userPhoto);
								context.startActivity(intent);
							}

							@Override
							public void onClick(View v) {
								gotoStoryTranslateActivity();
							}
						});
				holder.contentTextView.setClickable(true);
			}

		}

		// my
		if (Utils.isEmpty(to_content) || isNoNeedTranslate) {
			// 和登陆者的语言相同就不显示自动翻译
			holder.myContentView.setVisibility(View.GONE);

			holder.contentTranslateTextView
					.setText(R.string.message_status_text_translating);
		} else {
			holder.myContentView.setVisibility(View.VISIBLE);
			holder.userMyLangImgView.setImageResource(Utils
					.getLanguageFlag(userPhoto.getTo_lang()));

			String translator_fullname = userPhoto.getTranslator_fullname();
			if (Utils.isEmpty(translator_fullname)) {
				if (userPhoto.getTranslator_id() > 0) {
					translator_fullname = context
							.getString(R.string.human_translation);
				} else {
					translator_fullname = context
							.getString(R.string.auto_translation_msg);
				}
			} else {
				translator_fullname = Utils.getFriendName(
						userPhoto.getTranslator_id(), translator_fullname);
			}
			holder.contentTranslateTextView.setText(Html.fromHtml(Utils
					.htmlSpecialChars(userPhoto.getTo_content())
					+ "&nbsp;<small><font color='#ff6600'>"
					+ translator_fullname + "</font></small>"));
			holder.contentTranslateTextView.setTextColor(context.getResources()
					.getColor(R.color.text_gray));

		}
	}

	private final Context mContext;

	private final SwipeRefreshLayout mSwypeLayout;
	private final LayoutInflater mInflater;

	private final long storyPhotoUserId;// 发图的用户ID

	public UserStoryCommentListArrayAdapter(Context context, long userId,
			SwipeRefreshLayout swypeLayout) {
		super(context, mResource);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		storyPhotoUserId = userId;
		mSwypeLayout = swypeLayout;
	}

	void bindUserPhotoDelete(final Context context, final ViewHolder holder,
			final UserPhoto userPhoto) {
		OnClickListener onDeleteClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryCommentActivity.gotoDeletePhoto(userPhoto, context,
						new TaskAdapter() {
							@Override
							public void onPostExecute(GenericTask task,
									TaskResult result) {
								if (result == TaskResult.OK) {
									remove(userPhoto);
									notifyDataSetChanged();
									App.notificationManager
											.cancel(R.layout.activity_story_comment_list
													+ (int) userPhoto.getId());
									App.userPhotoDAO
											.deleteUserPhotosById(userPhoto
													.getId());
								}
								mSwypeLayout.setRefreshing(false);
							}

							@Override
							public void onPreExecute(GenericTask task) {
								mSwypeLayout.setRefreshing(true);
							}
						});
			}
		};
		if (storyPhotoUserId == App.readUser().getId()
				|| userPhoto.getUserid() == App.readUser().getId()) {
			holder.userCommentDeleteView.setVisibility(View.VISIBLE);
			holder.userCommentDeleteView
					.setOnClickListener(onDeleteClickListener);
		} else {
			holder.userCommentDeleteView.setVisibility(View.GONE);
		}
	}

	public void changeUserPhoto(UserPhoto userPhoto) {
		for (int i = 0; i < getCount(); i++) {
			UserPhoto item = getItem(i);
			if (item.getId() == userPhoto.getId()) {
				item.mergeFrom(userPhoto);
				notifyDataSetChanged();
				break;
			}
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

		bindUserPhoto(mContext, holder, userPhoto);
		bindUserPhotoDelete(mContext, holder, userPhoto);
		return view;
	}

	ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	// 提交评论、下拉刷新 都往list里面添加数据，需要判断优先，item已经存在了，不需要再插入。
	public boolean isCanAddListItem(UserPhoto userPhoto) {
		boolean isCanAddItem = true;
		for (int i = 0; i < getCount(); i++) {
			UserPhoto item = getItem(i);
			if (item.getId() == userPhoto.getId()) {
				isCanAddItem = false;
				break;
			}
		}
		return isCanAddItem;
	}

	public void updateTranslate(StoryTranslate storyTranslate) {
		for (int i = 0; i < getCount(); i++) {
			UserPhoto item = getItem(i);
			if (item.getId() == storyTranslate.getUser_photo_id()
					&& item.getTo_lang().equalsIgnoreCase(
							storyTranslate.getLang())) {
				item.to_content = storyTranslate.getTo_content();
				item.translator_fullname = storyTranslate.fullname;
				item.translator_id = storyTranslate.user_id;
				notifyDataSetChanged();
				break;
			}
		}
	}
}