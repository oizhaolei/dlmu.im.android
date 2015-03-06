/**
 *
 */
package com.ruptech.chinatalk.widget;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RequestAutoTranslatePhotoTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoListTask;
import com.ruptech.chinatalk.ui.ImageViewActivity;
import com.ruptech.chinatalk.ui.friend.FriendsLeaderboardListActivity;
import com.ruptech.chinatalk.ui.gift.GiftDonateActivity;
import com.ruptech.chinatalk.ui.gift.GiftListActivity;
import com.ruptech.chinatalk.ui.story.AbstractUserStoryListActivity;
import com.ruptech.chinatalk.ui.story.ChannelPopularListActivity;
import com.ruptech.chinatalk.ui.story.ShareStoryDialogActivity;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.story.UserStoryTranslateActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.ThirdPartyUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.face.ParseEmojiMsgUtil;
import com.ruptech.chinatalk.widget.TopGallery.OnGalleryItemClickListener;

public class UserStoryListCursorAdapter extends CursorAdapter implements
		OnGalleryItemClickListener {

	public static class ViewHolder {
		private View channelView;
		private ImageView channelImageView;
		private TextView channelTitleTextView;
		private View timelineView;
		private TextView timelineUsernameTextView;
		private TextView timelineCommentTextView;
		private TextView contentTextView;
		private TextView contentTranslateTextView;
		private View contentView;
		private ImageProgressBar imageProgressBar;
		private View myContentView;
		private ImageView picImgView;
		private TextView distanceTextView;
		private ImageView userImgView;
		private ImageView userImgMaskView;
		private ImageView channelImgMaskView;
		private ImageView userLangImgView;
		private ImageView userMyLangImgView;
		private TextView usernameTextView;

		private TextView replyTextView;
		private TextView goodTextView;
		private TextView giftTextView;

		protected View deleteView;
		private View storyBottomView;
	}

	private SwipeRefreshLayout swypeLayout;
	private ListView mFollowListView;

	private GenericTask mRetrieveUserPhotoListTask;
	private static final String[] TIMELINE_STATUS = { "create", "comment",
			"like", "present" };

	private final TaskListener mRequestAutoTranslatePhotoTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				doChangeAdapterCursor();
			}
			swypeLayout.setRefreshing(false);

		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setProgressTop(true);
			swypeLayout.setRefreshing(true);
		}
	};

	private final TaskListener mRetrieveUserPhotoListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			swypeLayout.setRefreshing(false);
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			if (result == TaskResult.OK) {
				List<UserPhoto> mUserPhotoList = retrieveUserPhotoListTask
						.getUserPhotoList();
				if (mUserPhotoList.size() == 0) {
					if (retrieveUserPhotoListTask.isTop()) {
						App.mBadgeCount.followCount = 0;
						CommonUtilities.broadcastRefreshNewMark(mContext);
						Toast.makeText(mContext, R.string.no_new_data,
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, R.string.no_more_data,
								Toast.LENGTH_SHORT).show();
					}
				}
				if (mUserPhotoList != null && mUserPhotoList.size() > 0) {
					if (retrieveUserPhotoListTask.isTop()
							&& mUserPhotoList.size() >= AppPreferences.PAGE_COUNT_20) {
						App.userPhotoDAO.deleteAll();
					}
					if (retrieveUserPhotoListTask.isTop()) {
						App.mBadgeCount.followCount = 0;
						CommonUtilities.broadcastRefreshNewMark(mContext);
					}
					String lang = App.readUser().getLang();
					String lang1 = null;
					if (App.readUser().getAdditionalLangs() != null
							&& App.readUser().getAdditionalLangs().length > 0) {
						lang1 = App.readUser().getAdditionalLangs()[0];
					}

					for (UserPhoto item : mUserPhotoList) {
						// request auto translate 去掉重复请求自动翻译
						long requestKey = item.getId();
						List<Long> storyAutoRequestTransKeyList = RequestAutoTranslatePhotoTask
								.getStoryAutoRequestTransKeyList();
						if (!Utils.isEmpty(item.getLang())
								&& !Utils.isEmpty(item.getContent())
								&& (!lang.equals(item.getLang()) || (lang1 != null && !lang1
										.equals(item.getLang())))
								&& Utils.isEmpty(item.getTo_content())
								&& !UserPhoto.isAutoTranslated(item)
								&& !storyAutoRequestTransKeyList
										.contains(requestKey)) {
							if (lang.equals(item.getLang())
									|| (lang1 != null && !lang1.equals(item
											.getLang()))) {
								requestAutotranslate(item.getId(), lang1);
							} else {
								requestAutotranslate(item.getId(), lang);
							}
							storyAutoRequestTransKeyList.add(requestKey);
						}

						App.userPhotoDAO.insertUserPhoto(item,
								retrieveUserPhotoListTask.isTop());
					}
					onRetrieveUserPhotoListSuccess(mUserPhotoList);
				} else {
					if (getCount() <= 0) {
						Cursor friendCursor = App.userDAO.fetchFriends(App
								.readUser().getId());
						if (friendCursor == null
								|| (friendCursor != null && friendCursor
										.getCount() == 0)) {
							showGoToHotUserDialog();
						}
					}
				}

				if (retrieveUserPhotoListTask.isTop()
						&& mFollowListView != null) {
					mFollowListView.setSelection(0);
				}
			} else {
				String msg = task.getMsg();
				onRetrieveUserPhotoListFailure(msg);
			}

		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveUserPhotoListTask retrieveUserPhotoListTask = (RetrieveUserPhotoListTask) task;
			onRetrieveUserPhotoListBegin(retrieveUserPhotoListTask.isTop());
		}

	};

	public void doChangeAdapterCursor() {

		Cursor cursor = fetchStoryListCursor();
		if (cursor == null || cursor.getCount() == 0) {
			doRetrievePopularList(true, mFollowListView);
		} else {
			changeCursor(cursor);
			if (App.mBadgeCount.followCount > 0)
				doRetrievePopularList(true, mFollowListView);
		}
	}

	private Cursor fetchStoryListCursor() {
		Cursor storyListCursor = App.userPhotoDAO.fetchPopularCursor();
		return storyListCursor;
	}

	private void onRetrieveUserPhotoListBegin(boolean isTop) {
		swypeLayout.setProgressTop(isTop);
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveUserPhotoListFailure(String msg) {
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onRetrieveUserPhotoListSuccess(List<UserPhoto> mUserPhotoList) {
		if (mUserPhotoList.size() > 0) {
			doChangeAdapterCursor();
		} else {

		}
	}

	private void requestAutotranslate(long id, String lang) {
		RequestAutoTranslatePhotoTask requestAutoTranslatePhotoTask = new RequestAutoTranslatePhotoTask(
				id, lang);
		requestAutoTranslatePhotoTask
				.setListener(mRequestAutoTranslatePhotoTaskListener);

		requestAutoTranslatePhotoTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void doRetrievePopularList(boolean top, ListView followListView) {
		if (mRetrieveUserPhotoListTask != null
				&& mRetrieveUserPhotoListTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		this.mFollowListView = followListView;

		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}

		mRetrieveUserPhotoListTask = new RetrieveUserPhotoListTask(top, maxId,
				sinceId, AbstractUserStoryListActivity.STORY_TYPE_TIMELINE);
		mRetrieveUserPhotoListTask
				.setListener(mRetrieveUserPhotoListTaskListener);
		mRetrieveUserPhotoListTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private long getMaxId() {
		long maxId = App.userPhotoDAO.getMinId();
		if (maxId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return maxId - 1;
		}
	}

	public long getSinceId() {
		long sinceId = App.userPhotoDAO.getMaxId();
		if (sinceId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return sinceId + 1;
		}
	}

	static void bindUserPhoto(final Context context, final ViewHolder holder,
			final UserPhoto userPhoto, final BaseAdapter adapter,
			OnClickListener onDeleteClickListener,
			final SwipeRefreshLayout swypeLayout, boolean original) {

		ImageManager.imageLoader.clearMemoryCache();
		holder.usernameTextView.setVisibility(View.VISIBLE);
		// 频道

		if (holder.channelView != null) {
			if (userPhoto.getChannel_id() > 0
					&& !Utils.isEmpty(userPhoto.getChannel_title())) {
				holder.channelView.setVisibility(View.VISIBLE);
				String thumb = userPhoto.getChannel_pic();
				Utils.setUserPicImage(holder.channelImageView, thumb);

				holder.channelTitleTextView.setText(userPhoto
						.getChannel_title());

				holder.channelView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Channel channel = new Channel();
						channel.setId(userPhoto.getChannel_id());
						channel.setTitle(userPhoto.getChannel_title());
						channel.setPic_url(userPhoto.getChannel_pic());
						Intent intent = new Intent(context,
								ChannelPopularListActivity.class);
						intent.putExtra(
								ChannelPopularListActivity.EXTRA_CHNANEL,
								channel);
						context.startActivity(intent);
					}
				});
			} else {
				holder.channelView.setVisibility(View.GONE);
			}
		}
		// 贴图
		String image = userPhoto.getPic_url();
		if (!Utils.isEmpty(image)) {
			holder.picImgView.setVisibility(View.VISIBLE);

			String photoUri;
			if (original) {
				photoUri = App.readServerAppInfo().getServerOriginal(image);
			} else {
				photoUri = App.readServerAppInfo().getServerMiddle(image);
			}
			if (!photoUri.equals(holder.picImgView.getTag())) {
				if (original) {
					ImageManager.imageLoader
							.displayImage(
									photoUri,
									holder.picImgView,
									ImageManager.getOptionsLandscape(),
									ImageViewActivity
											.createImageLoadingListenerWithResize(
													holder.imageProgressBar,
													userPhoto.getWidth(),
													userPhoto.getHeight(), 9,
													1.1f),
									ImageViewActivity
											.createLoadingProgresListener(holder.imageProgressBar));

				} else {
					ImageManager.imageLoader
							.displayImage(
									photoUri,
									holder.picImgView,
									ImageManager.getOptionsLandscape(),
									ImageViewActivity
											.createImageLoadingListener(holder.imageProgressBar),
									ImageViewActivity
											.createLoadingProgresListener(holder.imageProgressBar));
				}

			}
			holder.picImgView.setTag(photoUri);
			holder.picImgView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoStoryCommentActivity(context, userPhoto);
				}
			});
		} else {
			holder.picImgView.setTag(null);
			holder.picImgView.setVisibility(View.GONE);
			holder.picImgView.setOnClickListener(null);
		}

		// 用户信息
		String userPic = userPhoto.getUser_pic();
		if (original) {// 点进profile,显示的是中图，不显示贴图头像
			holder.userImgView.setVisibility(View.VISIBLE);
			holder.userImgMaskView.setVisibility(View.VISIBLE);
			Utils.setUserPicImage(holder.userImgView, userPic);

		} else {
			holder.userImgView.setVisibility(View.GONE);
			holder.userImgMaskView.setVisibility(View.GONE);
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

		String fullname = Utils.getFriendName(userPhoto.getUserid(),
				userPhoto.getFullname());
		String create_date = DateCommonUtils.dateFormat(
				userPhoto.getCreate_date(),
				DateCommonUtils.DF_yyyyMMddHHmmssSSS);
		String content_date = DateCommonUtils.formatConvUtcDateString(
				create_date, false, false);
		holder.usernameTextView.setText(Html.fromHtml(Utils
				.htmlSpecialChars(fullname)
				+ "<small><i> -"
				+ content_date
				+ "</i></small>"));
		holder.usernameTextView.getPaint().setFakeBoldText(true);

		// 距离
		if (Utils.isValidLocation6(userPhoto.getLate6(), userPhoto.getLnge6())
				&& MyLocation.recentLocation != null) {
			holder.distanceTextView.setText(Utils.FomartDistance(Utils
					.GetDistance(userPhoto.getLnge6() / 1E6,
							userPhoto.getLate6() / 1E6,
							MyLocation.recentLocation.getLongitude(),
							MyLocation.recentLocation.getLatitude())));
			holder.distanceTextView.setVisibility(View.VISIBLE);
		} else {
			holder.distanceTextView.setVisibility(View.GONE);
		}

		// 贴图原文
		String content = userPhoto.getContent();
		final String to_content = userPhoto.getTo_content();
		boolean isNoNeedTranslate = true;
		if (Utils.isEmpty(content)) {
			holder.contentView.setVisibility(View.GONE);
		} else {
			isNoNeedTranslate = ParseEmojiMsgUtil.isNoNeedTranslate(content,
					context);
			holder.contentView.setVisibility(View.VISIBLE);
			// TODO 全民翻译的概念
			// 有翻译内容的话，显示“其他翻译”
			// 1. 自己的国别没有的话，进入设置列表：设置自己的国别，母语，以及能翻译的外语列表
			// 2. 国别有，外语列表没有的话，代表此人不懂任何外语，不显示“我来翻译”
			// 3. 否则，进入翻译列表界面

			if (Utils.isEmpty(to_content) || isNoNeedTranslate) {
				holder.contentTextView.setText(Html.fromHtml(Utils
						.highlightTag(Utils.htmlSpecialChars(content))));
				holder.contentTextView.setClickable(false);
			} else {
				int tipResID = R.string.request_translate;
				if (!Utils.showUserStoryTranslateBtn(userPhoto)) {// 显示翻译按钮
					tipResID = R.string.view_translate;
				}
				holder.contentTextView.setText(Html.fromHtml(Utils
						.highlightTag(Utils.htmlSpecialChars(content))
						+ "&nbsp;<small><font color='#191970'> - "
						+ context.getString(tipResID) + "</font></small>"));

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

		// 贴图译文

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

			if (userPhoto.getTranslator_id() > 0
					&& userPhoto.getTranslator_id() != AppPreferences.HUMAN_TRANSLATOR_ID
					&& !Utils.isEmpty(userPhoto.getTranslator_fullname())) {// 旧数据会有人工翻译，不可以点
				holder.contentTranslateTextView
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								UserStoryCommentActivity.gotoProfileAcitivity(
										userPhoto.getTranslator_id(), context);
							}
						});
				holder.contentTranslateTextView
						.setBackgroundResource(R.drawable.chat_content_bg);
			} else {
				holder.contentTranslateTextView.setBackgroundResource(0);
				holder.contentTranslateTextView.setOnClickListener(null);
			}

		}

		holder.storyBottomView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoStoryCommentActivity(context, userPhoto);
			}
		});

		// 点赞回复礼物个数
		holder.replyTextView.setText(String.valueOf(userPhoto.getComment()));
		holder.goodTextView.setText(String.valueOf(userPhoto.getGood()));
		if (holder.giftTextView != null) {
			holder.giftTextView.setText(String.valueOf(userPhoto
					.getPresent_count()));
		}
		if (userPhoto.getUserid() == App.readUser().getId()) {
			holder.deleteView.setVisibility(View.VISIBLE);
		} else {
			holder.deleteView.setVisibility(View.GONE);
		}
		// 删除
		holder.deleteView.setOnClickListener(onDeleteClickListener);

		// timeline
		if (holder.timelineView != null) {
			String timeline_status = userPhoto.getStatus();

			if (timeline_status == null || timeline_status.length() == 0
					|| TIMELINE_STATUS[0].equalsIgnoreCase(timeline_status)) {
				holder.timelineView.setVisibility(View.GONE);
			} else {
				holder.timelineView.setVisibility(View.VISIBLE);
				final String timeline_fullname = getTimeLineFullname(userPhoto);
				holder.timelineView.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						int maxWidth = holder.timelineView.getMeasuredWidth() / 2;
						TextPaint tp = new TextPaint();
						tp.setTextSize(holder.timelineUsernameTextView
								.getTextSize());
						tp.setTypeface(holder.timelineUsernameTextView
								.getTypeface());
						String name = (String) TextUtils.ellipsize(
								timeline_fullname, tp, maxWidth,
								TextUtils.TruncateAt.END);
						holder.timelineUsernameTextView.setText(Utils
								.htmlSpecialChars(name));
					}

				});

				holder.timelineUsernameTextView.getPaint()
						.setFakeBoldText(true);
				holder.timelineUsernameTextView
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								UserStoryCommentActivity.gotoProfileAcitivity(
										userPhoto.getTimelineUserId(), context);
							}
						});

				String timeline_create_date = userPhoto
						.getTimeline_Create_date();
				String timeline_content_date = DateCommonUtils
						.formatConvUtcDateString(timeline_create_date, false,
								false);

				int resId = R.string.timeline_comment;
				if (TIMELINE_STATUS[1].equalsIgnoreCase(timeline_status))
					resId = R.string.timeline_comment;
				else if (TIMELINE_STATUS[2].equalsIgnoreCase(timeline_status))
					resId = R.string.timeline_like;
				else if (TIMELINE_STATUS[3].equalsIgnoreCase(timeline_status))
					resId = R.string.timeline_present;

				holder.timelineCommentTextView.setText(Html
						.fromHtml(holder.timelineCommentTextView.getContext()
								.getString(resId, timeline_content_date)));

			}
		}
	}

	private static String getTimeLineFullname(UserPhoto userPhoto) {
		String timeLineFullname = Utils.getFriendName(
				userPhoto.getTimelineUserId(), userPhoto.getTimelineFullname());
		if (Utils.isEmpty(timeLineFullname)) {// 空的情况下显示ID
			timeLineFullname = String.valueOf(userPhoto.getTimelineUserId());
		}
		return timeLineFullname;
	}

	static ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder();

		holder.timelineView = view
				.findViewById(R.id.item_user_story_timeline_view);
		holder.timelineUsernameTextView = (TextView) view
				.findViewById(R.id.item_user_story_timeline_username);
		holder.timelineCommentTextView = (TextView) view
				.findViewById(R.id.item_user_story_timeline_comment);
		holder.picImgView = (ImageView) view
				.findViewById(R.id.item_user_story_pic_imageview);
		holder.userImgView = (ImageView) view
				.findViewById(R.id.item_user_story_user_imageview);
		holder.userImgMaskView = (ImageView) view.findViewById(R.id.item_mask);
		holder.userLangImgView = (ImageView) view
				.findViewById(R.id.item_user_story_lang_imageview);
		holder.userMyLangImgView = (ImageView) view
				.findViewById(R.id.item_user_story_my_lang_imageview);
		holder.contentTextView = (TextView) view
				.findViewById(R.id.item_user_story_content_textview);
		holder.channelView = view
				.findViewById(R.id.item_user_story_channel_view);
		holder.channelImageView = (ImageView) view
				.findViewById(R.id.item_user_story_channel_photo);
		holder.channelImgMaskView = (ImageView) view
				.findViewById(R.id.item_channel_mask);
		holder.channelTitleTextView = (TextView) view
				.findViewById(R.id.item_user_story_channel_title);
		holder.contentView = view
				.findViewById(R.id.item_user_story_from_content_view);
		holder.contentTranslateTextView = (TextView) view
				.findViewById(R.id.item_user_story_my_content_textview);
		holder.myContentView = view
				.findViewById(R.id.item_user_story_my_content_view);
		holder.distanceTextView = (TextView) view
				.findViewById(R.id.item_user_story_distance);
		holder.usernameTextView = (TextView) view
				.findViewById(R.id.item_user_story_fullname);
		holder.replyTextView = (TextView) view
				.findViewById(R.id.item_user_story_image_reply_textview);
		holder.goodTextView = (TextView) view
				.findViewById(R.id.item_user_story_image_good_textview);
		holder.giftTextView = (TextView) view
				.findViewById(R.id.item_user_story_image_gift_textview);
		holder.imageProgressBar = (ImageProgressBar) view
				.findViewById(R.id.image_progress_bar);

		holder.deleteView = view.findViewById(R.id.item_user_story_delete_view);
		holder.storyBottomView = view
				.findViewById(R.id.item_user_story_bottom_view);
		return holder;
	}

	public static void gotoGiftListActivity(Context context, UserPhoto userPhoto) {
		Intent intent = new Intent(context, GiftListActivity.class);
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID,
				userPhoto.getId());
		intent.putExtra(GiftDonateActivity.EXTRA_TO_USER_ID,
				userPhoto.getUserid());
		context.startActivity(intent);
	}

	private void showGoToHotUserDialog() {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(mContext,
						FriendsLeaderboardListActivity.class);
				mContext.startActivity(intent);
			}
		};
		new CustomDialog(mContext)
				.setTitle(mContext.getString(R.string.add_friend))
				.setMessage(
						mContext.getString(R.string.no_follow_tips_dialog_content))
				.setPositiveButton(R.string.alert_dialog_ok, positiveListener)
				.setNegativeButton(R.string.cancel, null).show();
	}

	public static void gotoSharePopup(final Context context,
			final UserPhoto userPhoto) {

		// 点击share进入生成一个url
		Map<String, String> map = new HashMap<>();
		String id = Base64.encodeToString(String.valueOf(userPhoto.getId())
				.getBytes(), Base64.DEFAULT);

		map.put("id", id);
		map.put("lang", App.readUser().getLang());
		// url待确定
		String url = App.getHttpStoryServer().genRequestNoSignURL("user_photo",
				map);
		String imgUrl = App.readServerAppInfo().getServerOriginal(
				userPhoto.getPic_url());
		String imgThumbUrl = App.readServerAppInfo().getServerThumbnail(
				userPhoto.getPic_url());
		String text = userPhoto.getContent();
		String title = context.getString(R.string.share_story_default_content);
		if (Utils.isEmpty(text)) {
			text = title;
		}
		Bitmap image = ThirdPartyUtil.getBitmapFromCacheByUrl(imgUrl);
		if (image == null) {
			return;
		}

		Intent intent = new Intent(context, ShareStoryDialogActivity.class);
		// TODO : use intent, not static
		ShareStoryDialogActivity.sharedTitle = title;
		ShareStoryDialogActivity.sharedUrl = url;
		ShareStoryDialogActivity.sharedText = text;
		ShareStoryDialogActivity.sharedImgUrl = imgUrl;
		ShareStoryDialogActivity.sharedImage = image;
		ShareStoryDialogActivity.sharedImgThumbUrl = imgThumbUrl;
		ShareStoryDialogActivity.sharedImageThumb = ThirdPartyUtil
				.getCompressedBitmap(ShareStoryDialogActivity.sharedImage, 10);

		context.startActivity(intent);
	}

	public static void gotoStoryCommentActivity(Context context,
			UserPhoto userPhoto) {
		Intent intent = new Intent(context, UserStoryCommentActivity.class);
		intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO, userPhoto);

		context.startActivity(intent);
	}

	protected Context mContext;

	protected LayoutInflater mInflater;

	public UserStoryListCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, false);
		mContext = context;
		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final UserPhoto userPhoto = UserPhotoTable.parseCursor(cursor);

		OnClickListener onDeleteClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserStoryCommentActivity.gotoDeletePhoto(userPhoto, context,
						new TaskAdapter() {
							@Override
							public void onPostExecute(GenericTask task,
									TaskResult result) {
								if (result == TaskResult.OK) {
									getCursor().requery();
								} else {
									Toast.makeText(context, task.getMsg(),
											Toast.LENGTH_SHORT).show();
								}
								swypeLayout.setRefreshing(false);
							}

							@Override
							public void onPreExecute(GenericTask task) {
								swypeLayout.setRefreshing(true);
							}
						});
			}
		};
		bindUserPhoto(context, holder, userPhoto, this, onDeleteClickListener,
				swypeLayout, true);
	}

	@Override
	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		final UserPhoto userPhoto = UserPhotoTable.parseCursor(cursor);
		boolean mine = userPhoto.getUserid() == App.readUser().getId();
		return mine ? 0 : 1;
	}

	public void setSwypeLayout(SwipeRefreshLayout swypeLayout) {
		this.swypeLayout = swypeLayout;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view;
		view = mInflater.inflate(R.layout.item_story_user_photo, parent, false);

		ViewHolder holder = getViewHolder(view);

		view.setTag(holder);
		return view;
	}

	@Override
	public void onGalleryItemClick(TopGallery gallery, int position) {
		User user = gallery.getData(position);
		if (user != null) {
			UserStoryCommentActivity.gotoProfileAcitivity(user.getId(),
					gallery.getContext());
		}

	}
}