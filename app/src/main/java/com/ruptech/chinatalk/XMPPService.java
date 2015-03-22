package com.ruptech.chinatalk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.ruptech.chinatalk.event.ConnectionStatusChangedEvent;
import com.ruptech.chinatalk.event.NetChangeEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.smack.TTTalkSmackImpl;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.XMPPLoginTask;
import com.ruptech.chinatalk.task.impl.XMPPLogoutTask;
import com.ruptech.chinatalk.utils.NetUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.squareup.otto.Subscribe;

import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public class XMPPService extends BaseService {

	public static final int CONNECTED = 0;
	public static final int DISCONNECTED = -1;
	private int mConnectedState = DISCONNECTED; // 是否已经连接
	public static final int CONNECTING = 1;
	public static final String LOGOUT = "logout";// 手动退出
	public static final String NETWORK_ERROR = "network error";// 网络错误
	public static final String LOGIN_FAILED = "login failed";// 登录失败
	// 自动重连 start
	private static final int RECONNECT_AFTER = 5;
	private int mReconnectTimeout = RECONNECT_AFTER;
	private static final int RECONNECT_MAXIMUM = 10 * 60;// 最大重连时间间隔
	private static final String RECONNECT_ALARM = "com.ruptech.tttalk.RECONNECT_ALARM";
	private Intent mAlarmIntent = new Intent(RECONNECT_ALARM);
	protected final String TAG = Utils.CATEGORY
			+ XMPPService.class.getSimpleName();
	private IBinder mBinder = new XBinder();
	private PendingIntent mPAlarmIntent;
	private BroadcastReceiver mAlarmReceiver = new ReconnectAlarmReceiver();

	/**
	 * UI线程反馈连接失败
	 *
	 * @param reason
	 */
	private void connectionFailed(String reason) {
		Log.i(TAG, "connectionFailed: " + reason);
		mConnectedState = DISCONNECTED;// 更新当前连接状态
		if (TextUtils.equals(reason, LOGOUT)) {// 如果是手动退出
			((AlarmManager) getSystemService(Context.ALARM_SERVICE))
					.cancel(mPAlarmIntent);
			return;
		}
		// 回调
		App.mBus.post(new ConnectionStatusChangedEvent(mConnectedState, reason));

		// 无网络连接时,直接返回
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE) {
			((AlarmManager) getSystemService(Context.ALARM_SERVICE))
					.cancel(mPAlarmIntent);
			return;
		}

		String account = App.readUser().getUsername();
		String password = App.readUser().getPassword();
		// 无保存的帐号密码时，也直接返回
		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
			Log.d(TAG, "account = null || password = null");
			return;
		}
		// 如果不是手动退出并且需要重新连接，则开启重连闹钟
		Log.d(TAG, "connectionFailed(): registering reconnect in "
				+ mReconnectTimeout + "s");
		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ mReconnectTimeout * 1000, mPAlarmIntent);
		mReconnectTimeout = mReconnectTimeout * 2;
		if (mReconnectTimeout > RECONNECT_MAXIMUM)
			mReconnectTimeout = RECONNECT_MAXIMUM;

	}

	// 是否连接上服务器
	public boolean isAuthenticated() {
		if (App.mSmack != null) {
			return App.mSmack.isAuthenticated();
		}

		return false;
	}

	@Subscribe
	public void onNetChange(NetChangeEvent event) {
		if (event.connectivity == NetUtil.NETWORK_NONE) {// 如果是网络断开，不作处理
			connectionFailed(NETWORK_ERROR);
			return;
		}

		if (isAuthenticated())// 如果已经连接上，直接返回
			return;


		String account =App.readUser().getOF_username();
		String password = App.readUser().getPassword();

		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password))// 如果没有帐号，也直接返回
			return;
		login(account, password);// 重连
	}    // 判断程序是否在后台运行的任务

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "[SERVICE] onBind");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isAuthenticated()) {

			String account = App.readUser().getOF_username();
			String password = App.readUser().getPassword();
			login(account, password);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mPAlarmIntent = PendingIntent.getBroadcast(this, 0, mAlarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		registerReceiver(mAlarmReceiver, new IntentFilter(RECONNECT_ALARM));

		createSmack();
	}

	private void createSmack() {
		if (App.mSmack == null) {
			String server = App.properties.getProperty("xmpp.server.host");
			int port = Integer.parseInt(App.properties.getProperty("xmpp.server.port"));
			App.mSmack = new TTTalkSmackImpl(server, port, getContentResolver());
		}
	}

	// 登录
	public void login(final String account, final String password) {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE) {
			connectionFailed(NETWORK_ERROR);
			return;
		}

		XMPPLoginTask xLoginTask = new XMPPLoginTask(account, password);
		xLoginTask.setListener(new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				super.onPostExecute(task, result);

				if (result == TaskResult.OK) {
					// 登陆成功
					connectionScuessed();
				} else {
					// 登陆失败
					connectionFailed("onLoginFailure");
				}
			}

			@Override
			public void onPreExecute(GenericTask task) {
				super.onPreExecute(task);
				connecting();
			}
		});
		xLoginTask.execute();

	}

	public void logout() {
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORK_NONE) {
			connectionFailed(NETWORK_ERROR);
			return;
		}

		XMPPLogoutTask xLogoutTask = new XMPPLogoutTask();
		xLogoutTask.execute();
	}

	private void connectionScuessed() {
		mConnectedState = CONNECTED;// 已经连接上
		mReconnectTimeout = RECONNECT_AFTER;// 重置重连的时间

		App.mBus.post(new ConnectionStatusChangedEvent(mConnectedState, ""));
	}

	private void connecting() {
		mConnectedState = CONNECTING;// 连接中
		String reason = "";
		App.mBus.post(new ConnectionStatusChangedEvent(mConnectedState, reason));
	}

	@Override
	public void onDestroy() {

		((AlarmManager) getSystemService(Context.ALARM_SERVICE))
				.cancel(mPAlarmIntent);// 取消重连闹钟
		unregisterReceiver(mAlarmReceiver);// 注销广播监听

		super.onDestroy();
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

	// 自动重连广播
	private class ReconnectAlarmReceiver extends BroadcastReceiver {
		public void onReceive(Context ctx, Intent i) {
			Log.d(TAG, "Alarm received.");

			if (mConnectedState != DISCONNECTED) {
				Log.d(TAG, "Reconnect attempt aborted: we are connected again!");
				return;
			}

			String account = App.readUser().getOF_username();
			String password = App.readUser().getPassword();

			if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
				Log.d(TAG, "account = null || password = null");
				return;
			}
			login(account, password);
		}
	}

	public class XBinder extends Binder {
		public XMPPService getService() {
			return XMPPService.this;
		}
	}

	// 发送消息
	public void sendMessage(String toJID, Chat chat) {
		if (App.mSmack != null)
			App.mSmack.sendMessage(toJID, chat);
		else
			TTTalkSmackImpl.sendOfflineMessage(getContentResolver(), toJID, chat);
	}

	// 发送消息
	public void sendGroupMessage(MultiUserChat chatRoom, Chat chat) {
		try {
			if (chatRoom != null)
				App.mSmack.sendGroupMessage(chatRoom, chat);
			else
				TTTalkSmackImpl.sendOfflineMessage(getContentResolver(), chatRoom.getRoom(), chat);
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}
}
