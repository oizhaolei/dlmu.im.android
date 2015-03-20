package com.ruptech.chinatalk.ui.fragment;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.adapter.RecentChatAdapter;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.FriendOperate;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChatFragment extends Fragment {

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

	@InjectView(R.id.chat_list)
	ListView chatListView;

	@InjectView(R.id.chat_emptyview_text)
	TextView chatEmptyView;


	private Handler mainHandler = new Handler();

	private ContentObserver mChatObserver = new ChatObserver();
	private ContentResolver mContentResolver;
	private RecentChatAdapter mRecentChatAdapter;


	static final String LOG_TAG = ChatFragment.class.getName();

	public void updateRoster() {
//        mRecentChatAdapter.requery();
	}


	private void startChatActivity(String userJid, String userName) {
		Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
		chatIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
		startActivity(chatIntent);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab_chats, container, false);
		ButterKnife.inject(this, view);
		return view;
	}


	@Override
	public void onPause() {
		super.onPause();
		mContentResolver.unregisterContentObserver(mChatObserver);
	}

	@Override
	public void onResume() {
		super.onResume();
		mRecentChatAdapter.requery();
		mContentResolver.registerContentObserver(ChatProvider.CONTENT_URI,
				true, mChatObserver);

	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {


		setupChatLayout();

	}


	private void refreshChatPage() {
		mRecentChatAdapter.requery();
	}


	private void setupChatLayout() {
		mContentResolver = getActivity().getContentResolver();
		mRecentChatAdapter = new RecentChatAdapter(getActivity());
		chatListView.setAdapter(mRecentChatAdapter);

		chatListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
			                        int position, long id) {
				Cursor clickCursor = mRecentChatAdapter.getCursor();
				clickCursor.moveToPosition(position);
				String jid = Utils.getChatFriendJID(clickCursor);

				startChatActivity(jid, XMPPUtils.splitJidAndServer(jid));
			}
		});



	}


}