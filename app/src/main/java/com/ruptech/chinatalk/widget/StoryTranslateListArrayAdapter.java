/**
 *
 */
package com.ruptech.chinatalk.widget;

import static butterknife.ButterKnife.findById;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.StoryTranslateLikeTask;
import com.ruptech.chinatalk.ui.story.UserStoryTranslateActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

public class StoryTranslateListArrayAdapter extends
		ArrayAdapter<StoryTranslate> {
	static class ViewHolder {
		@InjectView(R.id.item_story_translate_content_textview)
		TextView contentTextView;
		@InjectView(R.id.item_story_translate_user_imageview)
		ImageView userImgView;
		@InjectView(R.id.item_story_translate_fullname_textview)
		TextView usernameTextView;
		@InjectView(R.id.item_mask)
		ImageView userImgMaskView;
		@InjectView(R.id.item_story_translate_lang_imageview)
		ImageView userLangImgView;
		@InjectView(R.id.item_user_story_good_imageview)
		ImageView goodImgView;
		@InjectView(R.id.item_user_story_image_good_textview)
		TextView goodCountTextView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private static SwipeRefreshLayout mSwypeLayout;
	private static final int mResource = R.layout.item_story_translate; // xml布局文件

	private static void gotoProfileAcitivity(long userid, Context context) {
		User user = App.userDAO.fetchUser(userid);
		Intent intent = new Intent(context, FriendProfileActivity.class);
		if (user == null) {
			intent.putExtra(ProfileActivity.EXTRA_USER_ID, userid);
		} else {
			intent.putExtra(ProfileActivity.EXTRA_USER, user);
		}
		context.startActivity(intent);
	}

	private final Context mContext;

	private final LayoutInflater mInflater;

	public StoryTranslateListArrayAdapter(Context context,
			SwipeRefreshLayout swypeLayout) {
		super(context, mResource);
		mContext = context;
		mSwypeLayout = swypeLayout;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	void bindUserPhoto(final Context context, final ViewHolder holder,
			final StoryTranslate storyTranslate, final BaseAdapter adapter) {
		// user photo
		String userPic = storyTranslate.getUser_pic();
		if (!Utils.isEmpty(userPic)) {
			if (!userPic.equals(holder.userImgView.getTag())) {
				ImageManager.imageLoader.displayImage(App.readServerAppInfo()
						.getServerThumbnail(userPic), holder.userImgView,
						ImageManager.getOptionsPortrait());
			}
			holder.userImgView.setTag(userPic);
		} else {
			holder.userImgView.setImageResource(R.drawable.ic_launcher);
		}

		holder.userImgMaskView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (storyTranslate.getUser_id() > 0
						&& storyTranslate.getUser_id() != AppPreferences.HUMAN_TRANSLATOR_ID
						&& !Utils.isEmpty(storyTranslate.getFullname())) {
					// 忽略ID:3635（对评论进行人工翻译）
					gotoProfileAcitivity(storyTranslate.getUser_id(), context);
				}
			}
		});
		holder.userLangImgView.setImageResource(Utils
				.getLanguageFlag(storyTranslate.getLang()));

		// fullname
		String fullname = storyTranslate.getFullname();
		if (Utils.isEmpty(fullname)) {

			if (storyTranslate.getUser_id() > 0) {
				fullname = mContext.getString(R.string.human_translation);
			} else {
				fullname = mContext.getString(R.string.auto_translation_msg);
			}
		} else {
			fullname = Utils.getFriendName(storyTranslate.getUser_id(),
					fullname);
		}

		String create_date = storyTranslate.getCreate_date();
		String content_date = DateCommonUtils.formatConvUtcDateString(
				create_date, false, false);
		holder.usernameTextView.setText(Html.fromHtml(Utils
				.htmlSpecialChars(fullname)
				+ "<small><i> -"
				+ content_date
				+ "</i></small>"));
		holder.usernameTextView.getPaint().setFakeBoldText(true);
		if (storyTranslate.getUser_id() > 0) {
			holder.usernameTextView.setTextColor(App.mContext.getResources()
					.getColor(R.color.black_color));
		} else {
			holder.usernameTextView.setTextColor(App.mContext.getResources()
					.getColor(R.color.text_gray));
		}

		// content
		String content = storyTranslate.getTo_content();
		if (Utils.isEmpty(content)) {
			content = "";
		}
		holder.contentTextView.setVisibility(View.VISIBLE);
		if (storyTranslate.getUser_id() > 0) {
			holder.contentTextView.setText(Html.fromHtml(Utils
					.highlightTag(Utils.htmlSpecialChars(content))));
			holder.contentTextView.setTextColor(App.mContext.getResources()
					.getColor(R.color.black_color));
		} else {
			holder.contentTextView.setText(Html.fromHtml(Utils
					.highlightTag(Utils.htmlSpecialChars(content))));
			holder.contentTextView.setTextColor(App.mContext.getResources()
					.getColor(R.color.text_gray));
		}

		// good

		int likeIconRes;
		if (storyTranslate.getFavorite() > 0) {
			likeIconRes = R.drawable.ic_action_social_like_selected;
		} else {
			likeIconRes = R.drawable.ic_action_social_like_unselected;
		}
		holder.goodImgView.setImageResource(likeIconRes);

		String goodCount = String.valueOf(storyTranslate.getGood());
		if (Utils.isEmpty(goodCount) || storyTranslate.getGood() < 1) {
			holder.goodCountTextView.setVisibility(View.GONE);
		} else {
			holder.goodCountTextView.setVisibility(View.VISIBLE);
			holder.goodCountTextView.setText(goodCount);
		}
		holder.goodImgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.likeAnimation(
						(ImageView) findById(v,
								R.id.item_user_story_good_imageview),
						(storyTranslate.getFavorite() > 0));
				UserStoryTranslateActivity.gotoTranslateLikePhoto(
						storyTranslate, new TaskAdapter() {
							@Override
							public void onPostExecute(GenericTask task,
									TaskResult result) {
								StoryTranslateLikeTask likeTask = (StoryTranslateLikeTask) task;
								if (result == TaskResult.FAILED) {
									Toast.makeText(context, task.getMsg(),
											Toast.LENGTH_SHORT).show();
								} else {
									StoryTranslate mStoryTranslate = likeTask
											.getStoryTranslate();
									Utils.toastTranslateLikeResult(context,
											mStoryTranslate);
									updateLikeStatus(mStoryTranslate);
									CommonUtilities.broadcastStoryTranslate(
											App.mContext, mStoryTranslate);
								}
								mSwypeLayout.setRefreshing(false);
							}

							@Override
							public void onPreExecute(GenericTask task) {
								mSwypeLayout.setRefreshing(true);
							}
						});
			}
		});
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final StoryTranslate userPhoto = getItem(position);

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

		bindUserPhoto(mContext, holder, userPhoto, this);
		return view;
	}

	ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	public void insertTranslate(StoryTranslate translate) {
		for (int i = 0; i < this.getCount(); i++) {
			StoryTranslate temp = this.getItem(i);
			if (temp.getId() == translate.getId()) {
				temp.to_content = translate.to_content;
				temp.create_date = translate.create_date;
				temp.good = translate.good;
				temp.favorite = translate.favorite;
				temp.user_pic = translate.user_pic;
				temp.fullname = translate.fullname;
				this.notifyDataSetChanged();
				return;
			}
		}

		this.insert(translate, 0);

	}

	public void updateLikeStatus(StoryTranslate translate) {
		for (int i = 0; i < this.getCount(); i++) {
			StoryTranslate temp = this.getItem(i);
			if (temp.getUser_photo_id() == translate.getUser_photo_id()
					&& temp.getLang().equals(translate.getLang())) {
				if (temp.getUser_id() == translate.getUser_id()) {
					if (temp.favorite > 0) {
						temp.favorite = 0;
						temp.good--;
						temp.good = Math.max(temp.good, 0);
					} else {
						temp.favorite = 1;
						temp.good++;
					}
				} else if (temp.favorite > 0) {
					temp.favorite = 0;
					temp.good--;
					temp.good = Math.max(temp.good, 0);
				}
			}
		}

		this.notifyDataSetChanged();
	}
}