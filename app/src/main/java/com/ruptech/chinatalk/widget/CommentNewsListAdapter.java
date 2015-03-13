/**
 *
 */
package com.ruptech.chinatalk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.sqlite.TableContent.CommentNewsTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveCommentNewsTask;
import com.ruptech.chinatalk.ui.fragment.ChatFragment;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DateCommonUtils;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.Utils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CommentNewsListAdapter extends CursorAdapter {

	public static class ViewHolder {
		@InjectView(R.id.item_sub_tab_commnet_news_comment_title_textview)
		TextView titleTextView;
		@InjectView(R.id.item_sub_tab_commnet_news_comment_content_textview)
		TextView contentTextView;
		@InjectView(R.id.item_sub_tab_commnet_news_thumb_imageview)
		ImageView picImgView;
		@InjectView(R.id.item_sub_tab_commnet_news_user_thumb_imageview)
		ImageView userPicImgView;
		@InjectView(R.id.item_sub_tab_commnet_news_thumb_imageview_layout)
		FrameLayout newsThumbImgView;
		@InjectView(R.id.item_sub_tab_commnet_news_comment_layout)
		View newsCommentView;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}

	private final String type;
	private boolean notMoreDataFound = false;
	private SwipeRefreshLayout swypeLayout;
	private GenericTask mRetrieveCommentNewsTask;

	private TextView emptyTextView;

	private final TaskListener mRetrieveCommentNewsTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveCommentNewsTask commentNewsListTask = (RetrieveCommentNewsTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<CommentNews> commentNewsList = commentNewsListTask
						.getCommentNewsList();
				if (getMaxId() == AppPreferences.ID_IMPOSSIBLE) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(R.string.no_data_found);
				} else {
					emptyTextView.setVisibility(View.GONE);
				}

				if (commentNewsList.size() == 0) {
					if (commentNewsListTask.isTop()) {
						Toast.makeText(mContext, R.string.no_new_data,
								Toast.LENGTH_SHORT).show();
					} else {
						notMoreDataFound = true;
						Toast.makeText(mContext, R.string.no_more_data,
								Toast.LENGTH_SHORT).show();
					}
				} else {
					if (commentNewsListTask.isTop()
							&& commentNewsList.size() >= AppPreferences.PAGE_COUNT_20) {
						App.commentNewsDAO.deleteAll(type);
					}
					if (commentNewsList.size() > 0) {
						for (CommentNews commentNews : commentNewsList) {
							App.commentNewsDAO.insertCommentNews(commentNews);
						}
						changeCursor(fetchCommentNewsListCursor());
					}
				}

				if (commentNewsListTask.isTop()
						&& commentNewsList.size() < AppPreferences.PAGE_COUNT_20) {
					if (type.equals(ChatFragment.SUB_COMMENT_TYPE)) {
						App.mBadgeCount.commentCount = 0;
					} else {
						App.mBadgeCount.newsCount = 0;
					}
				}
				CommonUtilities.broadcastRefreshNewMark(mContext);
			} else {
				String msg = task.getMsg();
				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}

	};

	private static final int mResource = R.layout.item_sub_tab_commnet_news; // xml布局文件

	protected Context mContext;

	protected LayoutInflater mInflater;

	public CommentNewsListAdapter(Context context, String type) {
		super(context, null, false);
		mContext = context;
		this.type = type;

		if (context != null) {
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		holder.contentTextView.setVisibility(View.VISIBLE);
		holder.newsThumbImgView.setVisibility(View.VISIBLE);
		holder.newsCommentView.setVisibility(View.VISIBLE);
		final CommentNews commentNews = CommentNewsTable.parseCursor(cursor);
		if (!Utils.isEmpty(commentNews.getPic_url())) {
			// 礼物获取图片路径为present
			if (commentNews.getNews_title().equals(
					ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[7])) {
				ImageManager.imageLoader.displayImage(App.readServerAppInfo()
						.getServerPresent(commentNews.getPic_url()),
						holder.picImgView, ImageManager.getOptionsLandscape());
			} else {
				ImageManager.imageLoader.displayImage(App.readServerAppInfo()
						.getServerThumbnail(commentNews.getPic_url()),
						holder.picImgView, ImageManager.getOptionsLandscape());
			}
		} else {
			holder.picImgView.setOnClickListener(null);
		}

		Utils.setUserPicImage(holder.userPicImgView,
				commentNews.getUser_pic_url());
		// qa不需要头像点击事件
		if (!commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[6])) {
			holder.newsCommentView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							FriendProfileActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER_ID,
							commentNews.getFrom_user_id());
					context.startActivity(intent);
				}
			});
		} else {
			holder.newsCommentView.setOnClickListener(null);
		}

		String contentDate = "";
		if (!Utils.isEmpty(commentNews.getCreate_date())) {
			contentDate = " -"
					+ DateCommonUtils.formatConvUtcDateString(
							commentNews.getCreate_date(), false, false);
		}
		String fullname = Utils.getFriendName(commentNews.from_user_id,
				commentNews.getUser_fullname());
		// 回复 & @
		if ((commentNews.getNews_title()
				.equals(ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[0]))
				|| (commentNews.getNews_title()
						.equals(ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[1]))) {
			holder.titleTextView.setText(Html.fromHtml(Utils
					.htmlSpecialChars(fullname)
					+ "<small><i>"
					+ contentDate
					+ "</i></small>"));
			holder.contentTextView.setVisibility(View.VISIBLE);
			holder.contentTextView.setText(commentNews.getContent());
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[2])) {
			// 点赞，内容text不需要显示
			holder.titleTextView.setText(Html.fromHtml(Utils
					.htmlSpecialChars(fullname)
					+ "<small><i>"
					+ contentDate
					+ "</i></small>"));
			holder.contentTextView.setVisibility(View.VISIBLE);
			holder.contentTextView.setText(R.string.like_your_story);
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[3])) {
			// 新贴图，内容text不需要显示
			holder.titleTextView.setText(context.getString(
					R.string.news_title_new_story, commentNews.getContent()));
			holder.contentTextView.setVisibility(View.GONE);
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[4])) {
			// 加好友，内容text，图片和头像都不需要显示
			holder.titleTextView.setText(context.getString(
					R.string.news_title_friend, commentNews.getContent()));
			holder.contentTextView.setVisibility(View.GONE);
			holder.newsThumbImgView.setVisibility(View.GONE);
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[5])) {
			// 公告，暂时不对应，服务器端不推送
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[6])) {
			// qa，内容text，图片都不显示，用户头像显示未系统图标
			holder.contentTextView.setText(commentNews.getContent());
			holder.newsThumbImgView.setVisibility(View.GONE);
			holder.newsCommentView.setVisibility(View.GONE);
		} else if (commentNews.getNews_title().equals(
				ChatFragment.SUB_COMMENT_NEWS_TYPE_ARRAY[7])) {
			// 礼物，内容text不显示
			holder.titleTextView.setText(Html.fromHtml(Utils
					.htmlSpecialChars(fullname)
					+ "<small><i>"
					+ contentDate
					+ "</i></small>"));
			holder.contentTextView.setVisibility(View.VISIBLE);
			holder.contentTextView.setText(context.getString(
					R.string.gift_donate_count, commentNews.getContent()));
		}
	}

	public void doChangeAdapterCursor() {
		Cursor cursor = fetchCommentNewsListCursor();
		if (cursor == null
				|| cursor.getCount() == 0
				|| (type.equals(ChatFragment.SUB_COMMENT_TYPE) && App.mBadgeCount.commentCount > 0)
				|| (type.equals(ChatFragment.SUB_NEWS_TYPE) && App.mBadgeCount.newsCount > 0)) {
			doRetrieveCommentNewsList(true);
		} else {
			changeCursor(cursor);
			if ((type.equals(ChatFragment.SUB_COMMENT_TYPE) && App.mBadgeCount.commentCount > 0)
					|| (type.equals(ChatFragment.SUB_NEWS_TYPE) && App.mBadgeCount.newsCount > 0))
				doRetrieveCommentNewsList(true);
		}
	}

	public void doRetrieveCommentNewsList(boolean top) {

		if (notMoreDataFound && !top) {
			swypeLayout.setRefreshing(false);
			Toast.makeText(mContext, R.string.no_more_data, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		if (mRetrieveCommentNewsTask != null
				&& mRetrieveCommentNewsTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mRetrieveCommentNewsTask = new RetrieveCommentNewsTask(top, maxId,
				sinceId, type);
		mRetrieveCommentNewsTask.setListener(mRetrieveCommentNewsTaskListener);

		mRetrieveCommentNewsTask
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private Cursor fetchCommentNewsListCursor() {
		Cursor newsListCursor = App.commentNewsDAO.fetchCommentNewsCursor(type);
		return newsListCursor;
	}

	protected long getMaxId() {
		long maxCount = App.commentNewsDAO.getMinId(type);
		if (maxCount == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return maxCount;
		}
	}

	protected long getSinceId() {
		long sinceCount = App.commentNewsDAO.getMaxId(type);
		if (sinceCount == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return sinceCount;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mInflater.inflate(mResource, parent, false);
		ViewHolder holder = new ViewHolder(view);

		view.setTag(holder);
		return view;
	}

	public void setEmptyLayout(TextView emptyTextView) {
		this.emptyTextView = emptyTextView;
	}

	public void setSwypeLayout(SwipeRefreshLayout swypeLayout) {
		this.swypeLayout = swypeLayout;
	}
}