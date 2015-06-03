package com.ruptech.chinatalk.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.adapter.RecentChatAdapter;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChatFragment extends Fragment {

    static final String LOG_TAG = ChatFragment.class.getName();
    @InjectView(R.id.chat_list)
    ListView chatListView;

    @InjectView(R.id.chat_emptyview_text)
    TextView chatEmptyView;


    private Handler mainHandler = new Handler();

    private ContentObserver mChatObserver = new ChatObserver();
    private ContentResolver mContentResolver;
    private RecentChatAdapter mRecentChatAdapter;

    public void updateChat() {
        mRecentChatAdapter.requery();
    }

    private void startChatActivity(String userJid, String userFullname) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
        chatIntent.putExtra(ChatActivity.EXTRA_TITLE, userFullname);
        startActivity(chatIntent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_chats, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chat_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                String from_Jid = clickCursor.getString(clickCursor
                        .getColumnIndex(ChatTable.Columns.FROM_JID));
                String to_Jid = clickCursor.getString(clickCursor
                        .getColumnIndex(ChatTable.Columns.TO_JID));
                String name = to_Jid;
                if (to_Jid.startsWith(App.readUser().getUsername())) {
                    name = from_Jid;
                }

                String from_fullname = clickCursor.getString(clickCursor
                        .getColumnIndex(ChatTable.Columns.FROM_FULLNAME));
                String to_fullanem = clickCursor.getString(clickCursor
                        .getColumnIndex(ChatTable.Columns.TO_FULLNAME));
                String fullname = to_fullanem;
                if (to_Jid.startsWith(App.readUser().getUsername())) {
                    fullname = from_fullname;
                }
                startChatActivity(name, fullname);
            }
        });


    }

    private class ChatObserver extends ContentObserver {
        public ChatObserver() {
            super(mainHandler);
        }

        public void onChange(boolean selfChange) {
            mainHandler.postDelayed(new Runnable() {
                public void run() {
                    updateChat();
                }
            }, 100);
        }
    }


}