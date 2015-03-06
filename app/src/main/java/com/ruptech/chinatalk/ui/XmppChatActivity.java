package com.ruptech.chinatalk.ui;

import android.content.AsyncQueryHandler;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.baidutranslate.openapi.TranslateClient;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.XMPPService;
import com.ruptech.chinatalk.adapter.ChatAdapter;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.db.RosterProvider;
import com.ruptech.chinatalk.event.ConnectionStatusChangedEvent;
import com.ruptech.chinatalk.sqlite.TableContent;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.StatusMode;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
/**
 * 
 * @author
 */
public class XmppChatActivity extends ActionBarActivity implements View.OnTouchListener,
        View.OnClickListener {

	static final String TAG = Utils.CATEGORY
			+ XmppChatActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_USERNAME = ChatActivity.class
            .getName() + ".username";// 昵称对应的key

    private static final String[] PROJECTION_FROM = new String[]{
            ChatTable.Columns.ID, ChatTable.Columns.DATE,
            ChatTable.Columns.DIRECTION,
            ChatTable.Columns.JID, ChatTable.Columns.MESSAGE,
            ChatTable.Columns.TO_MESSAGE,
            ChatTable.Columns.MESSAGE_ID,
            ChatTable.Columns.DELIVERY_STATUS,
            ChatTable.Columns.PACKET_ID};// 查询字段
    // 查询联系人数据库字段
    private static final String[] STATUS_QUERY = new String[]{
            TableContent.RosterTable.Columns.STATUS_MODE,
            TableContent.RosterTable.Columns.STATUS_MESSAGE,};
    @InjectView(R.id.msg_listView)
    ListView mMsgListView;// 对话ListView
    @InjectView(R.id.send)
    Button mSendMsgBtn;// 发送消息button
    @InjectView(R.id.input)
    EditText mChatEditText;// 消息输入框
    private InputMethodManager mInputMethodManager;
    private String mWithJabberID = null;// 当前聊天用户的ID
    private ContentObserver mContactObserver = new ContactObserver();// 联系人数据监听，主要是监听对方在线状态

    private TranslateClient client;

    // 【重要】 onCreate时候初始化翻译相关功能
    private void initTransClient() {
        client = new TranslateClient(this, App.properties.getProperty("baidu_api_key"));

        // 这里可以设置为在线优先、离线优先、 只在线、只离线 4种模式，默认为在线优先。
        client.setPriority(TranslateClient.Priority.OFFLINE_FIRST);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmpp_chat);
        ButterKnife.inject(this);
        App.mBus.register(this);

//        initTransClient();// 初始化翻译相关功能

        initData();// 初始化数据
        initView();// 初始化view
        setChatWindowAdapter();// 初始化对话数据
        getContentResolver().registerContentObserver(
                RosterProvider.CONTENT_URI, true, mContactObserver);// 开始监听联系人数据库

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContactStatus();// 更新联系人状态
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateContactStatus() {
        Cursor cursor = getContentResolver().query(RosterProvider.CONTENT_URI,
                STATUS_QUERY, TableContent.RosterTable.Columns.JID + " = ?",
                new String[]{mWithJabberID}, null);
        int MODE_IDX = cursor
                .getColumnIndex(TableContent.RosterTable.Columns.STATUS_MODE);
        int MSG_IDX = cursor
                .getColumnIndex(TableContent.RosterTable.Columns.STATUS_MESSAGE);

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            int status_mode = cursor.getInt(MODE_IDX);
            String status_message = cursor.getString(MSG_IDX);
            Log.d(TAG, "contact status changed: " + status_mode + " " + status_message);
            getSupportActionBar().setTitle(XMPPUtils.splitJidAndServer(getIntent()
                    .getStringExtra(INTENT_EXTRA_USERNAME)));
            int statusId = StatusMode.values()[status_mode].getDrawableId();
            if (statusId != -1) {// 如果对应离线状态
                // Drawable icon = getResources().getDrawable(statusId);
                // mTitleNameView.setCompoundDrawablesWithIntrinsicBounds(icon,
                // null,
                // null, null);
            } else {
            }
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        if (hasWindowFocus())
            App.unbindXMPPService();// 解绑服务
        getContentResolver().unregisterContentObserver(mContactObserver);

        if (client != null) {
            client.onDestroy();
        }
        App.mBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 窗口获取到焦点时绑定服务，失去焦点将解绑
        if (hasFocus)
            App.bindXMPPService();
        else
            App.unbindXMPPService();
    }
    private void initData() {
        mWithJabberID = getIntent().getDataString().toLowerCase();// 获取聊天对象的id
        // 将表情map的key保存在数组中
    }

    /**
     * 设置聊天的Adapter
     */
    private void setChatWindowAdapter() {
        String selection = ChatTable.Columns.JID + "='" + mWithJabberID + "'";
        // 异步查询数据库
        new AsyncQueryHandler(getContentResolver()) {

            @Override
            protected void onQueryComplete(int token, Object cookie,
                                           Cursor cursor) {
                // ListAdapter adapter = new ChatWindowAdapter(cursor,
                // PROJECTION_FROM, PROJECTION_TO, mWithJabberID);
                ListAdapter adapter = new ChatAdapter(XmppChatActivity.this,
                        cursor, PROJECTION_FROM, client);
                mMsgListView.setAdapter(adapter);
                mMsgListView.setSelection(adapter.getCount() - 1);
            }

        }.startQuery(0, null, ChatProvider.CONTENT_URI, PROJECTION_FROM,
                selection, null, null);
        // 同步查询数据库，建议停止使用,如果数据庞大时，导致界面失去响应
        // Cursor cursor = managedQuery(ChatProvider.CONTENT_URI,
        // PROJECTION_FROM,
        // selection, null, null);
        // ListAdapter adapter = new ChatWindowAdapter(cursor, PROJECTION_FROM,
        // PROJECTION_TO, mWithJabberID);
        // mMsgListView.setAdapter(adapter);
        // mMsgListView.setSelection(adapter.getCount() - 1);
    }

    private void initView() {
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mChatEditText.setOnTouchListener(this);
        mChatEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                if (s.length() > 0) {
                    mSendMsgBtn.setEnabled(true);
                } else {
                    mSendMsgBtn.setEnabled(false);
                }
            }
        });
        mSendMsgBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:// 发送消息
                sendMessageIfNotNull();
                break;
            default:
                break;
        }
    }

    private void sendMessageIfNotNull() {
        if (mChatEditText.getText().length() >= 1) {
            if (App.mService != null) {
                App.mService.sendMessage(mWithJabberID, mChatEditText.getText()
                        .toString(), null);
                if (!App.mService.isAuthenticated())
                    Toast.makeText(this, "消息已经保存随后发送", Toast.LENGTH_SHORT).show();
            }
            mChatEditText.setText(null);
            mSendMsgBtn.setEnabled(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.msg_listView:
                mInputMethodManager.hideSoftInputFromWindow(
                        mChatEditText.getWindowToken(), 0);
                break;
            case R.id.input:
                mInputMethodManager.showSoftInput(mChatEditText, 0);
                break;

            default:
                break;
        }
        return false;
    }

    // 防止乱pageview乱滚动
    private View.OnTouchListener forbidenScroll() {
        return new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        };
    }
    @Subscribe
    public void answerConnectionStatusChanged(ConnectionStatusChangedEvent event) {
        int connectedState=event.connectedState;
        String reason=event.reason;

        switch (connectedState) {
            case XMPPService.CONNECTED:
                getSupportActionBar().setTitle(String.valueOf(App.readUser().getId()));
                break;
            case XMPPService.CONNECTING:
                getSupportActionBar().setTitle(R.string.start_receiving_messages);
                break;
            case XMPPService.DISCONNECTED:
                getSupportActionBar().setTitle(R.string.stop_receiving_messages);
                break;

            default:
                getSupportActionBar().setTitle(reason);
                break;
        }
    }

    /**
     * 联系人数据库变化监听
     */
    private class ContactObserver extends ContentObserver {
        public ContactObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            Log.d(TAG, "ContactObserver.onChange: " + selfChange);
            updateContactStatus();// 联系人状态变化时，刷新界面
        }
    }
}
