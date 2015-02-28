package com.ruptech.chinatalk.ui.story;

import static butterknife.ButterKnife.findById;
import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.ChatTTTActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListCursorAdapter;

public class TextShareActivity extends ActionBarActivity {

	public static final String SEND_TEXT = "SEND_TEXT";

	private final String TAG = Utils.CATEGORY
			+ TextShareActivity.class.getSimpleName();
	@InjectView(R.id.activity_story_reply_listview)
	ListView mStoyReplyListView;
	@InjectView(R.id.activity_story_reply_empty_textview)
	TextView emptyTextView;
	private FriendListCursorAdapter mStoryReplyListCursorAdapter;

	private String sharedText;
	View mTTTItemView;

	public void gotoTTTChatActivity() {
		Intent intent = new Intent(this, ChatTTTActivity.class);
		if (sharedText != null)
			intent.putExtra(SEND_TEXT, sharedText);
		startActivity(intent);
		finish();
	}

	void handleSendText(Intent intent) {
		sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_text);
		ButterKnife.inject(this);
		setupComponents();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				handleSendText(intent); // Handle text being sent
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void send_message(User user) {
		Intent intent = new Intent(this, ChatActivity.class);
		intent.putExtra(ChatActivity.EXTRA_FRIEND, user);
		if (sharedText != null)
			intent.putExtra(SEND_TEXT, sharedText);
		startActivity(intent);
		finish();
	}

	private void setupComponents() {
		mStoyReplyListView.setEmptyView(emptyTextView);

		mStoryReplyListCursorAdapter = new FriendListCursorAdapter(this,
				App.userDAO.fetchFriends(App.readUser().getId()));
		mTTTItemView = setupViewItemChatTTT();
		mStoyReplyListView.addHeaderView(mTTTItemView, mTTTItemView, true);

		mStoyReplyListView.setAdapter(mStoryReplyListCursorAdapter);

		mStoyReplyListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					gotoTTTChatActivity();
				} else if (position > 0) {
					Cursor item = (Cursor) mStoyReplyListView
							.getItemAtPosition(position);
					User user = UserTable.parseCursor(item);
					send_message(user);
				}
			}

		});

	}

	private View setupViewItemChatTTT() {
		View view = View.inflate(this, R.layout.item_main_tab_ttt_header, null);
		TextView tttTextView = (TextView) findById(view,
				R.id.item_main_tab_ttt_header_textview);
		tttTextView.setText(R.string.translation_secretary);
		return view;
	}
}