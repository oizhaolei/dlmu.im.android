package com.ruptech.chinatalk.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.adapter.RecentChatAdapter;
import com.ruptech.chinatalk.model.CommentNews;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.CommentNewsTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.DeleteCommentNewsTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.fragment.SegmentTabLayout.OnSegmentClickListener;
import com.ruptech.chinatalk.ui.setting.SettingQaActivity;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.chinatalk.widget.CommentNewsListAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static butterknife.ButterKnife.findById;

public class ChatFragment extends Fragment implements OnRefreshListener,
		TextWatcher, OnSegmentClickListener {

    private class ChatObserver extends ContentObserver {
        public ChatObserver() {
            super(mainHandler);
        }

        public void onChange(boolean selfChange) {
            mainHandler.postDelayed(new Runnable() {
                public void run() {
                    updateRoster();
                }
            }, 100);
        }
    }

	public static Fragment newInstance() {
		ChatFragment fragment = new ChatFragment();
		return fragment;
	}
	@InjectView(R.id.chat_layout)
	View chatLayoutView;
	@InjectView(R.id.chat_list)
	ListView chatListView;

	@InjectView(R.id.chat_emptyview_text)
	TextView chatEmptyView;
	@InjectView(R.id.comment_layout)
	View commentLayoutView;
	@InjectView(R.id.comment_swype)
	SwipeRefreshLayout commentSwypeView;
	@InjectView(R.id.comment_list)
	ListView commentListView;

	@InjectView(R.id.comment_emptyview_text)
	TextView commentEmptyView;
	@InjectView(R.id.news_layout)
	View newsLayoutView;
	@InjectView(R.id.news_swype)
	SwipeRefreshLayout newsSwypeView;
	@InjectView(R.id.news_list)
	ListView newsListView;

	@InjectView(R.id.news_emptyview_text)
	TextView newsEmptyView;

	private static String keyword = null;
	private Cursor chatsCursor;
	private ImageView searchClearBtn;
	private View chatHeaderView;
	private EditText searchTextView;
	private int currentCheckId = -1;

	private SegmentTabLayout segmentTabLayout;
	private String beforeSearchText = "";
	public final static String SUB_NEWS_TYPE = "news";
	private View layoutList[];

	public final static String SUB_COMMENT_TYPE = "comment";

	public static String[] SUB_COMMENT_NEWS_TYPE_ARRAY = { "story_new_comment",
			"story_new_comment_reply", "story_like", "story_new", "friend",
			"announcement", "qa", "present_donate" };
//	private ChatListCursorAdapter mChatListViewAdapter;
	private CommentNewsListAdapter mNewsListAdapter;

	private CommentNewsListAdapter mCommentListAdapter;
	private GenericTask mDeleteNewsTask;

	private GenericTask mDeleteCommentTask;


    private Handler mainHandler = new Handler();

    private ContentObserver mChatObserver = new ChatObserver();
    private ContentResolver mContentResolver;
    private RecentChatAdapter mRecentChatAdapter;

	private final BroadcastReceiver mHandleChatListReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
//			refreshChatPage();
		}
	};

	private final BroadcastReceiver mHandleRefreshNewMarkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshNewMark();
		}
	};

	static final String LOG_TAG = ChatFragment.class.getName();

    public void updateRoster() {
        mRecentChatAdapter.requery();
    }

	@Override
	public void afterTextChanged(Editable s) {
		keyword = s.toString();
		if (!beforeSearchText.equals(keyword)) {
//			refreshChatPage();
		}
		beforeSearchText = keyword;

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	private View createActionBarView() {
		LayoutInflater inflator = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflator.inflate(R.layout.item_segment_bar, null);
		segmentTabLayout = (SegmentTabLayout) view
				.findViewById(R.id.segment_group);
		segmentTabLayout.setOnSegmentClickListener(this);
		segmentTabLayout.addTab(R.string.main_sub_tab_chat);
		segmentTabLayout.addTab(R.string.main_sub_tab_comment);
		segmentTabLayout.addTab(R.string.main_sub_tab_news);
		segmentTabLayout.populateTab();

		return view;
	}


    private void startChatActivity(String userJid, String userName) {
        User user = App.userDAO.fetchUser(Utils.getTTTalkIDFromOF_JID(userJid));
        Intent chatIntent = new Intent(getActivity(),ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_FRIEND, user);
        startActivity(chatIntent);
    }

	private void doDeleteCommentList(long id) {
		if (mDeleteCommentTask != null
				&& mDeleteCommentTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mDeleteCommentTask = new DeleteCommentNewsTask(id);
		mDeleteCommentTask.setListener(new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				if (result == TaskResult.OK) {
					Toast.makeText(getActivity(),
							getString(R.string.messages_delete_success),
							Toast.LENGTH_LONG).show();
					mCommentListAdapter.doChangeAdapterCursor();
				}
			}

		});

		mDeleteCommentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void doDeleteNewsList(long id) {
		if (mDeleteNewsTask != null
				&& mDeleteNewsTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mDeleteNewsTask = new DeleteCommentNewsTask(id);
		mDeleteNewsTask.setListener(new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				if (result == TaskResult.OK) {
					Toast.makeText(getActivity(),
							getString(R.string.messages_delete_success),
							Toast.LENGTH_LONG).show();
					mNewsListAdapter.doChangeAdapterCursor();
				}
			}

		});

		mDeleteNewsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private View getActiveLayout() {
		if (layoutList != null) {
			for (int i = 0; i < layoutList.length; i++) {
				if (layoutList[i].getVisibility() == View.VISIBLE)
					return layoutList[i];
			}
		}
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (this.getActiveLayout() == chatLayoutView) {
//				refreshChatPage();
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		mChatListViewAdapter = new ChatListCursorAdapter(getActivity(), null);
		mNewsListAdapter = new CommentNewsListAdapter(getActivity(),
				SUB_NEWS_TYPE);
		mCommentListAdapter = new CommentNewsListAdapter(getActivity(),
				SUB_COMMENT_TYPE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab_chats, container, false);
		ButterKnife.inject(this, view);
		return view;
	}

	@Override
	public void onDestroyView() {
		unRegisterReceiver();
		super.onDestroyView();
	}

	@Override
	public void onRefresh(boolean isUp) {
		if (getActiveLayout() == commentLayoutView) {
			mCommentListAdapter.doRetrieveCommentNewsList(isUp);
		} else if (getActiveLayout() == newsLayoutView) {
			mNewsListAdapter.doRetrieveCommentNewsList(isUp);
		}
	}

    @Override
    public void onPause() {
        super.onPause();
        mContentResolver.unregisterContentObserver(mChatObserver);
    }

	@Override
	public void onResume() {
		super.onResume();
		if (currentCheckId == R.string.main_sub_tab_chat) {
//			refreshChatPage();
            mRecentChatAdapter.requery();
            mContentResolver.registerContentObserver(com.ruptech.chinatalk.db.ChatProvider.CONTENT_URI,
                    true, mChatObserver);
		}

		refreshNewMark();
	}

	@Override
	public void onSegmentClick(int checkedId) {
		refreshLayout(checkedId);
		currentCheckId = checkedId;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		layoutList = new View[] { chatLayoutView, commentLayoutView,
				newsLayoutView };

		setupChatLayout();
		setupNewsLayout();
		setupCommentLayout();

		Utils.showCustomActionbar(getActivity(), createActionBarView());

		if (currentCheckId == -1) {
			segmentTabLayout.clickTab(R.string.main_sub_tab_chat);
		} else {
			segmentTabLayout.clickTab(currentCheckId);
		}

		registerReceiver();
	}

	private void unRegisterReceiver() {
		try {
			getActivity().unregisterReceiver(mHandleChatListReceiver);
			getActivity().unregisterReceiver(mHandleRefreshNewMarkReceiver);
		} catch (Exception e) {
		}
	}

	private void registerReceiver() {
		getActivity().registerReceiver(mHandleChatListReceiver,
				new IntentFilter(CommonUtilities.CHAT_LIST_ACTION));
		getActivity().registerReceiver(mHandleRefreshNewMarkReceiver,
				new IntentFilter(CommonUtilities.REFERSH_NEW_MARK_ACTION));
	}

//	private void refreshChatPage() {
//		String args[] = new String[] { String.valueOf(App.readUser().getId()),
//				keyword };
//		new AsyncQueryHandler(getActivity().getContentResolver()) {
//
//			@Override
//			protected void onQueryComplete(int token, Object cookie,
//					Cursor cursor) {
//				chatsCursor = cursor;
//				mChatListViewAdapter.changeCursor(chatsCursor);
//				updateChatTabNewMark();
//			}
//
//		}.startQuery(0, null, ChatProvider.CONTENT_URI, null, null, args, null);
//	}

	public void refreshCurrentTab() {
		onRefresh(true);
	}

	private void refreshLayout(int checkedId) {
		switch (checkedId) {
		case R.string.main_sub_tab_chat:
//			if (mChatListViewAdapter.getCursor() == null
//					|| mChatListViewAdapter.getCount() == 0)
//				refreshChatPage();
			break;
		case R.string.main_sub_tab_comment:
			if (mCommentListAdapter.getCursor() == null
					|| mCommentListAdapter.getCount() == 0
					|| App.mBadgeCount.commentCount > 0)
				mCommentListAdapter.doChangeAdapterCursor();
			break;
		case R.string.main_sub_tab_news:
			if (mNewsListAdapter.getCursor() == null
					|| mNewsListAdapter.getCount() == 0
					|| App.mBadgeCount.newsCount > 0)
				mNewsListAdapter.doChangeAdapterCursor();
			break;
		}

		selectLayout(checkedId);
	}

	public void refreshNewMark() {
		segmentTabLayout.setNewCountForTab(
				App.mBadgeCount.getNewMessageTotalCount(), 0);
		segmentTabLayout.setNewCountForTab(App.mBadgeCount.commentCount, 1);
		segmentTabLayout.setNewCountForTab(App.mBadgeCount.newsCount, 2);

	}

	private void selectLayout(int checkedId) {
		switch (checkedId) {
		case R.string.main_sub_tab_chat:
			chatLayoutView.setVisibility(View.VISIBLE);
			commentLayoutView.setVisibility(View.GONE);
			newsLayoutView.setVisibility(View.GONE);
			break;
		case R.string.main_sub_tab_comment:
			chatLayoutView.setVisibility(View.GONE);
			commentLayoutView.setVisibility(View.VISIBLE);
			newsLayoutView.setVisibility(View.GONE);
			break;
		case R.string.main_sub_tab_news:
			chatLayoutView.setVisibility(View.GONE);
			commentLayoutView.setVisibility(View.GONE);
			newsLayoutView.setVisibility(View.VISIBLE);
			break;
		}
	}

	private void setChatHeaderView() {
		View view = View.inflate(this.getActivity(), R.layout.search_bar, null);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, getResources()
						.getDimensionPixelSize(R.dimen.tab_bar_height));
		view.setLayoutParams(params);
		searchTextView = (EditText) findById(view, R.id.search_text);
		searchClearBtn = (ImageView) findById(view, R.id.search_clear);
		searchClearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchTextView.setText("");
			}

		});
		searchTextView.addTextChangedListener(this);
		chatHeaderView = view;
	}

	private void setupChatLayout() {
		setChatHeaderView();

        mContentResolver = getActivity().getContentResolver();
        mRecentChatAdapter = new RecentChatAdapter(getActivity());
        chatListView.setAdapter(mRecentChatAdapter);

        chatListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
                Cursor clickCursor = mRecentChatAdapter.getCursor();
                clickCursor.moveToPosition(position);
                String jid = clickCursor.getString(clickCursor
                        .getColumnIndex(TableContent.ChatTable.Columns.JID));

                startChatActivity(jid, XMPPUtils.splitJidAndServer(jid));
			}
		});

//		chatListView.addHeaderView(chatHeaderView);
//
//		chatListView.setAdapter(mChatListViewAdapter);
//		chatListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1,
//					int position, long id) {
//				if (position == 0)
//					return;
//				else
//					position--;
//
//				Cursor item = (Cursor) mChatListViewAdapter.getItem(position);
//				User user = UserTable.parseCursor(item);
//
//				// 对话界面
//				Intent intent = new Intent(getActivity(), ChatActivity.class);
//				intent.putExtra(ChatActivity.EXTRA_FRIEND, user);
//				getActivity().startActivity(intent);
//			}
//		});
//
//		chatListView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View v,
//					int position, long id) {
//				if (position == 0)
//					return true;
//				else
//					position--;
//
//				Cursor item = (Cursor) mChatListViewAdapter.getItem(position);
//				final User user = UserTable.parseCursor(item);
//				// 设定弹出菜单选项
//				final Friend friend = App.friendDAO.fetchFriend(App.readUser()
//						.getId(), user.getId());
//				UserType userType;
//				if (friend == null || friend.getDone() == 0) {
//					userType = UserType.STRANGER;
//				} else {
//					userType = UserType.FRIEND;
//				}
//				final FriendOperate friendOperate = new FriendOperate(
//						getActivity(), user, userType);
//				List<String> menuList = new ArrayList<>();
//				menuList.add(getActivity()
//						.getString(R.string.friend_menu_block));
//				menuList.add(getActivity().getString(
//						R.string.friend_menu_report));
//				menuList.add(getActivity().getString(
//						R.string.friend_menu_clean_chat_history));
//				menuList.add(getActivity().getString(
//						R.string.friend_menu_more_chat_history));
//				final Friend tempFriend;
//				if (friend == null) {
//					tempFriend = App.friendDAO.fetchFriend(user.getId(), App
//							.readUser().getId());
//				} else {
//					tempFriend = friend;
//				}
//				if (tempFriend != null) {
//					if (tempFriend.getIs_top() == 1) {
//						menuList.add(getActivity().getString(
//								R.string.is_cancel_on_top));
//					} else {
//						menuList.add(getActivity()
//								.getString(R.string.is_on_top));
//					}
//				}
//				final String[] menus = new String[menuList.size()];
//				menuList.toArray(menus);
//				CustomDialog alertDialog = new CustomDialog(getActivity())
//						.setItems(menus, new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface dialog,
//									int which) {
//								String selectedItem = menus[which];
//								if (getActivity().getString(
//										R.string.friend_menu_block).equals(
//										selectedItem)) {
//									friendOperate.settingBlockFriend();
//								} else if (getActivity().getString(
//										R.string.friend_menu_report).equals(
//										selectedItem)) {
//									friendOperate.settingReportFriend();
//								} else if (getActivity()
//										.getString(
//												R.string.friend_menu_clean_chat_history)
//										.equals(selectedItem)) {
//									friendOperate.settingCleanMessage();
//								} else if (getActivity().getString(
//										R.string.friend_menu_more_chat_history)
//										.equals(selectedItem)) {
//									friendOperate.settingGetHistoryFriend();
//								} else if (getActivity().getString(
//										R.string.is_cancel_on_top).equals(
//										selectedItem)
//										|| getActivity().getString(
//												R.string.is_on_top).equals(
//												selectedItem)) {
//									int isTop = 0;
//									if (tempFriend.getIs_top() == 0) {
//										isTop = 1;
//									}
//									if (friend == null) {
//										friendOperate.doSaveChatTopSetting(
//												isTop, false, user);
//									} else {
//										friendOperate.doSaveChatTopSetting(
//												isTop, true, user);
//									}
//									refreshChatPage();
//								}
//							}
//
//						});
//				String title = null;
//				// 不是自己显示好友昵称，如果没有显示好友自己的名字
//				if (friend != null) {
//					title = Utils.isEmpty(friend.getFriend_nickname()) ? user
//							.getFullname() : friend.getFriend_nickname();
//				} else {
//					title = user.getFullname();
//				}
//				alertDialog.setTitle(title).show();
//				return true;
//			}
//		});

	}

	private void setupCommentLayout() {
		commentSwypeView.setOnRefreshListener(this);
		commentSwypeView.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mCommentListAdapter.setSwypeLayout(commentSwypeView);
		mCommentListAdapter.setEmptyLayout(commentEmptyView);
		commentListView.setAdapter(mCommentListAdapter);
		commentListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor item = (Cursor) mCommentListAdapter.getItem(position);
				CommentNews commentNews = CommentNewsTable.parseCursor(item);
				if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[0])
						|| commentNews.getNews_title().equals(
								SUB_COMMENT_NEWS_TYPE_ARRAY[1])) {
					// 新贴图和@点击后跳转到回复页面
					UserPhoto userPhoto = new UserPhoto();
					userPhoto.setId(commentNews.getRelation_id());
					userPhoto.setParent_id(commentNews.getNews_id());
					userPhoto.setPic_url(commentNews.getPic_url());
					Intent intent = new Intent(getActivity(),
							UserStoryCommentActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);
					intent.putExtra(
							UserStoryCommentActivity.EXTRA_IS_REAL_USERPHOTO,
							false);
					intent.putExtra(
							UserStoryCommentActivity.EXTRA_IS_REAL_COMMENT_USERPHOTO,
							true);
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[2])
						|| commentNews.getNews_title().equals(
								SUB_COMMENT_NEWS_TYPE_ARRAY[3])) {
					// 点赞和新贴图点击跳转到贴图详细页面
					UserPhoto userPhoto = new UserPhoto();
					userPhoto.setId(commentNews.getNews_id());
					userPhoto.setPic_url(commentNews.getPic_url());
					Intent intent = new Intent(getActivity(),
							UserStoryCommentActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[4])) {
					// 新关注点击跳转到贴图详细页面
					Intent intent = new Intent(getActivity(),
							FriendProfileActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER_ID,
							commentNews.getNews_id());
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[6])) {
					// QA点击跳转到Qa详细页面
					Intent intent = new Intent(getActivity(),
							SettingQaActivity.class);
					intent.putExtra(SettingQaActivity.EXTRA_QA_ID,
							commentNews.getNews_id());
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[7])) {
					// 礼物页面，如果有Relation_id(user_phoot_id)跳转到贴图详细页面，否则跳转到个人信息页面
					if (commentNews.getRelation_id() > 0) {
						UserPhoto userPhoto = new UserPhoto();
						userPhoto.setId(commentNews.getRelation_id());
						Intent intent = new Intent(getActivity(),
								UserStoryCommentActivity.class);
						intent.putExtra(
								UserStoryCommentActivity.EXTRA_USER_PHOTO,
								userPhoto);
						getActivity().startActivity(intent);
					} else {
						Intent intent = new Intent(getActivity(),
								FriendProfileActivity.class);
						intent.putExtra(ProfileActivity.EXTRA_USER_ID, App
								.readUser().getId());
						intent.putExtra(FriendProfileActivity.EXTRA_PAGE_ITEM,
								2);
						getActivity().startActivity(intent);
					}
				}
			}
		});

		commentListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View v, int position, long id) {
						Cursor item = (Cursor) mCommentListAdapter
								.getItem(position);
						final CommentNews commentNews = CommentNewsTable
								.parseCursor(item);
						DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								doDeleteCommentList(commentNews.getId());
							}
						};
						DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						};

						Utils.AlertDialog(
								getActivity(),
								positiveListener,
								negativeListener,
								getActivity().getString(R.string.tips),
								getActivity().getString(
										R.string.are_you_sure_delete_message));

						return true;
					}
				});
		commentEmptyView.setVisibility(View.GONE);
		commentEmptyView.setText("");
		commentListView.setEmptyView(commentEmptyView);
	}

	private void setupNewsLayout() {
		newsSwypeView.setOnRefreshListener(this);
		newsSwypeView.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mNewsListAdapter.setSwypeLayout(newsSwypeView);
		mNewsListAdapter.setEmptyLayout(newsEmptyView);
		newsListView.setAdapter(mNewsListAdapter);
		newsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor item = (Cursor) mNewsListAdapter.getItem(position);
				CommentNews commentNews = CommentNewsTable.parseCursor(item);
				if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[0])
						|| commentNews.getNews_title().equals(
								SUB_COMMENT_NEWS_TYPE_ARRAY[1])) {
					// 新贴图和@点击后跳转到回复页面
					UserPhoto userPhoto = new UserPhoto();
					userPhoto.setId(commentNews.getRelation_id());
					userPhoto.setParent_id(commentNews.getNews_id());
					userPhoto.setPic_url(commentNews.getPic_url());
					Intent intent = new Intent(getActivity(),
							UserStoryCommentActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);
					intent.putExtra(
							UserStoryCommentActivity.EXTRA_IS_REAL_USERPHOTO,
							false);
					intent.putExtra(
							UserStoryCommentActivity.EXTRA_IS_REAL_COMMENT_USERPHOTO,
							true);
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[2])
						|| commentNews.getNews_title().equals(
								SUB_COMMENT_NEWS_TYPE_ARRAY[3])) {
					// 点赞和新贴图点击跳转到贴图详细页面
					UserPhoto userPhoto = new UserPhoto();
					userPhoto.setId(commentNews.getNews_id());
					userPhoto.setPic_url(commentNews.getPic_url());
					Intent intent = new Intent(getActivity(),
							UserStoryCommentActivity.class);
					intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
							userPhoto);
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[4])) {
					// 新关注点击跳转到贴图详细页面
					Intent intent = new Intent(getActivity(),
							FriendProfileActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER_ID,
							commentNews.getNews_id());
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[6])) {
					// QA点击跳转到Qa详细页面
					Intent intent = new Intent(getActivity(),
							SettingQaActivity.class);
					intent.putExtra(SettingQaActivity.EXTRA_QA_ID,
							commentNews.getNews_id());
					getActivity().startActivity(intent);
				} else if (commentNews.getNews_title().equals(
						SUB_COMMENT_NEWS_TYPE_ARRAY[7])) {
					// 礼物页面，如果有Relation_id(user_phoot_id)跳转到贴图详细页面，否则跳转到个人信息页面
					if (commentNews.getRelation_id() > 0) {
						UserPhoto userPhoto = new UserPhoto();
						userPhoto.setFullname(App.readUser().getFullname());
						userPhoto.setId(commentNews.getRelation_id());
						Intent intent = new Intent(getActivity(),
								UserStoryCommentActivity.class);
						intent.putExtra(
								UserStoryCommentActivity.EXTRA_USER_PHOTO,
								userPhoto);
						getActivity().startActivity(intent);
					} else {
						Intent intent = new Intent(getActivity(),
								FriendProfileActivity.class);
						intent.putExtra(ProfileActivity.EXTRA_USER_ID, App
								.readUser().getId());
						intent.putExtra(FriendProfileActivity.EXTRA_PAGE_ITEM,
								2);
						getActivity().startActivity(intent);
					}
				}
			}
		});

		newsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				Cursor item = (Cursor) mNewsListAdapter.getItem(position);
				final CommentNews commentNews = CommentNewsTable
						.parseCursor(item);
				DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						doDeleteNewsList(commentNews.getId());
					}
				};
				DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				};

				Utils.AlertDialog(
						getActivity(),
						positiveListener,
						negativeListener,
						getActivity().getString(R.string.tips),
						getActivity().getString(
								R.string.are_you_sure_delete_message));

				return true;
			}
		});
		newsEmptyView.setVisibility(View.GONE);
		newsEmptyView.setText("");
		newsListView.setEmptyView(newsEmptyView);
	}

	private void updateChatTabNewMark() {
		CommonUtilities.broadcastRefreshNewMark(getActivity());
	}
}