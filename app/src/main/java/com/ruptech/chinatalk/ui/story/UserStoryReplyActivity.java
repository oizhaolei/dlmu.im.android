package com.ruptech.chinatalk.ui.story;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ChatListCursorAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static butterknife.ButterKnife.findById;
import static com.ruptech.chinatalk.sqlite.TableContent.UserTable;

public class UserStoryReplyActivity extends ActionBarActivity implements
		TextWatcher {

	static final String EXTRA_REPLY_NAME = "EXTRA_REPLY_NAME";
	static final String EXTRA_REPLY_ID = "EXTRA_REPLY_ID";

	private static Cursor chatsCursor;

	private final String TAG = Utils.CATEGORY
			+ UserStoryReplyActivity.class.getSimpleName();
	@InjectView(R.id.activity_story_reply_listview)
	ListView mStoyReplyListView;
	@InjectView(R.id.activity_story_reply_empty_textview)
	TextView emptyTextView;

	private ChatListCursorAdapter mChatListViewAdapter;

	private String keyword = "";
	private String beforeSearchText = "";

	private ImageView searchClearBtn;
	private EditText searchTextView;

	@Override
	public void onResume() {
		super.onResume();
		refreshChatPage();
	}

	@Override
	public void afterTextChanged(Editable s) {
		keyword = s.toString();
		if (!beforeSearchText.equals(keyword)) {
			refreshChatPage();
		}
		beforeSearchText = keyword;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Yellow_light);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("@");
		setContentView(R.layout.activity_story_reply);
		ButterKnife.inject(this);
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	private void refreshChatPage() {

		String args[] = new String[] { String.valueOf(App.readUser().getId()),
				keyword };
		new AsyncQueryHandler(this.getContentResolver()) {

			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				chatsCursor = cursor;
				if (mChatListViewAdapter != null) {
					mChatListViewAdapter.changeCursor(chatsCursor);
					if (mChatListViewAdapter.getCount() == 0) {
						emptyTextView.setVisibility(View.VISIBLE);
						emptyTextView.setText(R.string.no_data_found);
					} else {
						emptyTextView.setVisibility(View.GONE);
					}
				}
			}

		}.startQuery(0, null, ChatProvider.CONTENT_URI, null, null, args, null);
	}

	private void saveReply(String replyName, long replyId) {
		Intent intent = getIntent();
		intent.putExtra(EXTRA_REPLY_ID, String.valueOf(replyId));
		intent.putExtra(EXTRA_REPLY_NAME, replyName);
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}

	private View chatHeaderView;

	private void setHeaderView() {
		View view = View.inflate(this, R.layout.search_bar, null);
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
		searchTextView.setHint(R.string.fullname_or_nickname);
		chatHeaderView = view;
	}

	private void setupComponents() {
		mChatListViewAdapter = new ChatListCursorAdapter(this, null);
		setHeaderView();
		mStoyReplyListView.addHeaderView(chatHeaderView);
		mStoyReplyListView.setAdapter(mChatListViewAdapter);
		mStoyReplyListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position >= 0) {
					Cursor item = (Cursor) parent.getItemAtPosition(position);
					User user = UserTable.parseCursor(item);
					if (Utils.isEmpty(user.getFullname())) {// 有些加的好友fullname是空的
						saveReply(user.getTel(), user.getId());
					} else {
						saveReply(user.getFullname(), user.getId());
					}
				}
			}

		});

	}

}