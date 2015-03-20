package com.ruptech.chinatalk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.adapter.ChatAdapter;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.ChatRoom;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.FriendProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.sqlite.UserProvider;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 */
public class ChatActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatActivity.class.getSimpleName();

	private String mToJid;//可以是friend的JID，也可以是muc的JID
	private ChatRoom mChatRoom;
	private Map<Long, User> mFriendUsers;
	private Map<Long, Friend> mFriendsToMe;
	private Map<Long, Friend> mFriendsFromMe;


	@InjectView(R.id.activity_chat_message_edittext)
	EditText mMessageEditText;
	@InjectView(R.id.activity_chat_message_listview)
	ListView mMessageListView;


	private final TaskListener mRetrieveUserListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserTask retrieveUserTask = (RetrieveUserTask) task;
			if (result == TaskResult.OK) {
				if (retrieveUserTask.getUser() != null) {
					User friend = retrieveUserTask.getUser();
					mFriendUsers.put(friend.getId(), friend);

					displayTitle();
				}
			} else {
				String msg = retrieveUserTask.getMsg();
				onRetrieveUserTaskFailure(msg);
			}
		}

	};

	private CursorAdapter cursorAdapter;

	protected void displayTitle() {
		String title = mChatRoom.getTitle();
		if (title == null) {
			User friendUser = mFriendUsers.get(mChatRoom.getParticipantIds().get(0));
			if (friendUser!=null) {
				title = friendUser.getFullname();
			}else {
				title = mToJid;
			}
		}
		getSupportActionBar().setTitle(title);
	}


	private Map<Long, Friend> retrieveFriendToMe(List<Long> participantIds) {
		Map<Long, Friend> friends = new HashMap<>(participantIds.size());
		for (Long participantId : participantIds) {
			// 检索好友与我的关系
			long me = App.readUser().getId();
			Friend friend = App.friendDAO.fetchFriend(participantId, me);
			if (friend!=null) {
				friends.put(participantId, friend);
			}
		}

		return friends;
	}

	private Map<Long, Friend> retrieveFriendFromMe(List<Long> participantIds) {
		Map<Long, Friend> friends = new HashMap<>(participantIds.size());
		for (Long participantId : participantIds) {
			// 检索好友与我的关系
			long me = App.readUser().getId();
			Friend friend = App.friendDAO.fetchFriend(me, participantId);
			friends.put(participantId, friend);
		}

		return friends;
	}

	// 隐藏软键盘
	private void hideInputManager() {
		mInputMethodManager.hideSoftInputFromWindow(mMessageEditText.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void initInputPanel() {
		amIFriend = amIFriend(mFriendUsers);
	}

	//检查我是不是任何一个friend的朋友
	private boolean amIFriend(Map<Long, User> friendUsers) {
		for (User friendUser : friendUsers.values()) {
			if (friendUser != null) {
				Friend friendUserInfo = App.friendDAO.fetchFriend(
						friendUser.getId(), App.readUser().getId());
				if (friendUserInfo != null
						&& friendUserInfo.getDone() == AppPreferences.FRIEND_ADDED_STATUS) {
					return true;
				}
			}
		}
		return false;
	}


	//

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			gotoSplashActivity();
			finish();
			return;
		}


		setContentView(R.layout.activity_chat);
		ButterKnife.inject(this);

		mToJid = (String) getIntent().getExtras().get(EXTRA_JID);

		mChatRoom = genChatRoom();
		mFriendUsers = retrieveFriendUsers(mChatRoom.getParticipantIds());


		// 检索好友与我的关系
		mFriendsToMe = retrieveFriendToMe(mChatRoom.getParticipantIds());
		//blocked check:被任何一个朋友block的话，需要推出
		for (Friend friend : mFriendsToMe.values()) {
			if (friend.getDone() == AppPreferences.FRIEND_BLOCK_STATUS) {
				Toast.makeText(this, R.string.friend_block_msg, Toast.LENGTH_LONG)
						.show();
				App.notificationManager.cancel(mToJid.hashCode());
				finish();
				return;
			}
		}
		// 检索我与好友的关系
		mFriendsFromMe = retrieveFriendFromMe(mChatRoom.getParticipantIds());
		//陌生人检查
		if (mFriendsToMe.size() < mChatRoom.getParticipantIds().size()) {
			Toast.makeText(this, R.string.no_follow_other, Toast.LENGTH_LONG)
					.show();
		}

		setupComponents();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		initInputPanel();
	}

	private ChatRoom genChatRoom() {
		ChatRoom chatRoom = App.chatRoomDAO.fetchChatRoomByJid(mToJid);
		if (chatRoom == null) {
			//gen a model
			chatRoom = new ChatRoom();
			chatRoom.setJid(mToJid);
			chatRoom.setAccountUserId(App.readUser().getId());
			long friendUserId = Utils.getTTTalkIDFromOF_JID(mToJid);
			chatRoom.addPaticipant(friendUserId);
		}
		return chatRoom;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_actions, mMenu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}

		return true;
	}

	private void onRetrieveUserTaskFailure(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private Map<Long, User> retrieveFriendUsers(List<Long> participantIds) {
		Map<Long, User> friendUsers = new HashMap<>();
		//mFriendUsers
		for (long friendUserId : participantIds) {
			User friendUser = App.userDAO.fetchUser(friendUserId);
			if (friendUser == null) {
				doRetrieveUser(friendUserId);

				RetrieveServerVersionTask mRetrieveServerVersionTask = new RetrieveServerVersionTask();
				mRetrieveServerVersionTask.execute();

			} else {
				friendUsers.put(friendUserId, friendUser);
			}
		}
		return friendUsers;
	}


	public void updateAdapterCursor(Cursor c) {
		Cursor oldCursor = cursorAdapter.swapCursor(c);
		oldCursor.close();
	}

	public void setupComponents() {
		mInputMethodManager = (InputMethodManager) this.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);


		Point screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		cursorAdapter = new ChatAdapter(this, reQuery(), screenSize);
		mMessageListView.setAdapter(cursorAdapter);
		mMessageListView.setSelection(cursorAdapter.getCount() - 1);

		mMessageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
			                     int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				hideInputManager();
			}

		});

		InputFilter[] filters = {new Utils.LengthFilter()};
		mMessageEditText.setFilters(filters);


		displayTitle();
	}


	public static final String EXTRA_JID = "EXTRA_JID";


	protected boolean amIFriend = true;


	InputMethodManager mInputMethodManager;


	private void doRetrieveUser(long userId) {
		RetrieveUserTask mRetrieveUserTask = new RetrieveUserTask(userId);
		mRetrieveUserTask.setListener(mRetrieveUserListener);
		mRetrieveUserTask.execute();
	}


	private void gotoSplashActivity() {
		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		long start = System.currentTimeMillis();
		super.onDestroy();
		if (BuildConfig.DEBUG)
			Log.i(TAG, "onDestroy:" + (System.currentTimeMillis() - start));
	}

	@Override
	public void onPause() {
		super.onPause();
		//PrefUtils.removePrefNewMessageCount(mToJid);//TODO 按 Home ||
		App.unbindXMPPService();

		getContentResolver().unregisterContentObserver(mUserObserver);
		getContentResolver().unregisterContentObserver(mChatObserver);
		getContentResolver().unregisterContentObserver(mMessageObserver);
		getContentResolver().unregisterContentObserver(mFriendObserver);
	}


	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(mToJid.hashCode());
		App.bindXMPPService();

		getContentResolver().registerContentObserver(
				UserProvider.CONTENT_URI, true, mUserObserver);// 开始监听friend user数据库
		getContentResolver().registerContentObserver(
				ChatProvider.CONTENT_URI, true, mChatObserver);// 开始监听chat数据库
		getContentResolver().registerContentObserver(
				FriendProvider.CONTENT_URI, true, mFriendObserver);// 开始监听friend数据库
	}

	@OnClick(R.id.activity_chat_btn_send)
	public void send_text() {
		String content = mMessageEditText.getText().toString().trim();
		// reform
		if (content.replaceAll("\n", "").length() == 0) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		// check
		if (content.length() == 0) {
			Toast.makeText(this, R.string.content_cannot_be_empty,
					Toast.LENGTH_SHORT).show();
			return;
		}
		sendText(content);

	}


	private void sendText(String content) {
		Chat chat = new Chat();
		chat.setContent(content);

		//chat
		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setToJid(mToJid);
		chat.setCreated_date(System.currentTimeMillis());
		sendMessageIfNotNull(chat);

	}


	protected static final String[] CHAT_PROJECTION = new String[]{
			ChatTable.Columns.ID,
			ChatTable.Columns.FROM_JID,
			ChatTable.Columns.TO_JID,
			ChatTable.Columns.CONTENT,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.DELIVERY_STATUS,
			ChatTable.Columns.CREATED_DATE,
			ChatTable.Columns.PACKET_ID};// 查询字段
	private ContentObserver mUserObserver = new UserObserver();
	private ContentObserver mChatObserver = new ChatObserver();
	private ContentObserver mMessageObserver = new MessageObserver();
	private ContentObserver mFriendObserver = new FriendObserver();

	private class UserObserver extends ContentObserver {
		public UserObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			retrieveFriendUsers(mChatRoom.getParticipantIds());// 联系人状态变化时，刷新界面
		}
	}

	private class ChatObserver extends ContentObserver {
		public ChatObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			updateAdapterCursor(reQuery());
		}
	}

	private Cursor reQuery() {

		String selection = ChatTable.Columns.FROM_JID + " = ? and " + ChatTable.Columns.TO_JID + " = ?";

		Cursor childCursor = getContentResolver().query(ChatProvider.CONTENT_URI,
				CHAT_PROJECTION, selection, new String[]{App.readUser().getOF_JabberID(), mToJid}, null);
		return childCursor;
	}

	private class MessageObserver extends ContentObserver {
		public MessageObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			// TODO
		}
	}

	private class FriendObserver extends ContentObserver {
		public FriendObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d(TAG, "ContactObserver.onChange: " + selfChange);
			// TODO
		}
	}

	protected void sendMessageIfNotNull(Chat chat) {
		String content = chat.getContent();
		if (content.length() >= 1) {
			if (App.mService != null) {
				App.mService.sendMessage(mToJid, chat);
			}
			mMessageEditText.setText(null);
		}
	}

}
