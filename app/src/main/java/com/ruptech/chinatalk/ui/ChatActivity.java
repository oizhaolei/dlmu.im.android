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
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.adapter.ChatAdapter;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.task.impl.SendGroupTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 */
public class ChatActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ ChatActivity.class.getSimpleName();

	private String mToJid;//可以是friend的JID，也可以是muc的JID
	private User mFriendUser;


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
					mFriendUser = retrieveUserTask.getUser();

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
		String title;
		if (mFriendUser != null) {
			title = mFriendUser.getFullname();
		} else {
			title = mToJid;
		}
		getSupportActionBar().setTitle(title);
	}


	// 隐藏软键盘
	private void hideInputManager() {
		mInputMethodManager.hideSoftInputFromWindow(mMessageEditText.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
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

		setupComponents();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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


	InputMethodManager mInputMethodManager;


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

		getContentResolver().unregisterContentObserver(mChatObserver);
	}


	@Override
	public void onResume() {
		super.onResume();
		App.notificationManager.cancel(mToJid.hashCode());
		App.bindXMPPService();

		getContentResolver().registerContentObserver(
				ChatProvider.CONTENT_URI, true, mChatObserver);// 开始监听chat数据库
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

	private final TaskListener mSendGroupMessageTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				SendGroupTask sgt = (SendGroupTask)task;
				Toast.makeText(ChatActivity.this,"Send to: "+sgt.getSendList(), Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void sendText(String content) {
		Chat chat = new Chat();
		chat.setContent(content);

		//chat
		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setToJid(mToJid);
		chat.setCreated_date(System.currentTimeMillis());
		sendMessageIfNotNull(chat);

		if (isGroup(mToJid)) {

			String fromJid = App.readUser().getOF_JabberID();
			SendGroupTask sendGroupTask = new SendGroupTask(fromJid, mToJid, "From:" + fromJid, content);
			sendGroupTask.setListener(mSendGroupMessageTaskListener);

			sendGroupTask.execute();

		}
	}

	private boolean isGroup(String jid) {
		return jid.startsWith("org_");
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
	private ContentObserver mChatObserver = new ChatObserver();

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

		return getContentResolver().query(ChatProvider.CONTENT_URI,
				CHAT_PROJECTION, selection, new String[]{App.readUser().getOF_JabberID(), mToJid}, null);
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
