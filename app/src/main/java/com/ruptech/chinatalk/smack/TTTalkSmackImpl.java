package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.ConnectionStatusChangedEvent;
import com.ruptech.chinatalk.event.OfflineEvent;
import com.ruptech.chinatalk.event.OnlineEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.NetUtil;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.dlmu.im.BuildConfig;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.util.Date;

public class TTTalkSmackImpl implements TTTalkSmack {

	public static final String XMPP_IDENTITY_NAME = "tttalk";
	public static final String XMPP_IDENTITY_TYPE = "phone";
	static final DiscoverInfo.Identity TTTALK_IDENTITY = new DiscoverInfo.Identity("client",
			XMPP_IDENTITY_NAME,
			XMPP_IDENTITY_TYPE);
	public static final int PACKET_TIMEOUT = 30000;

	protected final String TAG = Utils.CATEGORY + TTTalkSmackImpl.class.getSimpleName();

	final static private String[] SEND_OFFLINE_PROJECTION = new String[]{
			ChatTable.Columns.ID, ChatTable.Columns.TO_JID, ChatTable.Columns.CONTENT,
			ChatTable.Columns.CREATED_DATE, ChatTable.Columns.PACKET_ID};
	final static private String SEND_OFFLINE_SELECTION = ChatTable.Columns.FROM_JID
			+ " = '" + App.readUser().getJid() + "' AND "
			+ ChatTable.Columns.DELIVERY_STATUS + " = " + ChatProvider.DS_NEW;

	private final ContentResolver mContentResolver;
	private AbstractXMPPConnection mXMPPConnection;
	private StanzaListener mSendFailureListener;
	private ConnectionListener mConnectionListener;

	static {
		registerSmackProviders();

		SmackConfiguration.setDefaultPacketReplyTimeout(PACKET_TIMEOUT);
	}

	public TTTalkSmackImpl(ContentResolver contentResolver) {

		mContentResolver = contentResolver;

		initXMPPConnection();

	}

	private void initXMPPConnection() {
		AppVersion serverAppInfo = PrefUtils.readServerAppInfo();
		String server;
		int port;//Integer.parseInt(App.properties.getProperty("xmpp.server.port"));
		if (serverAppInfo != null) {
			server = serverAppInfo.imHost;
			port = serverAppInfo.imPort;
		} else {
			server = App.properties.getProperty("xmpp.server.host");
			port = Integer.parseInt(App.properties.getProperty("xmpp.server.port"));
		}

		Log.i(TAG, String.format("xmpp: %s, %s", server, port));


		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setHost(server)
				.setPort(port);
		configBuilder.setResource(XMPP_IDENTITY_NAME);
		configBuilder.setServiceName(AppPreferences.IM_SERVER_RESOURCE);

		configBuilder.setSendPresence(false);
		configBuilder.setCompressionEnabled(false); // disable for now
		configBuilder.setDebuggerEnabled(BuildConfig.DEBUG);
		configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		this.mXMPPConnection = new XMPPTCPConnection(configBuilder.build());

		initServiceDiscovery();
	}
	// ping-pong服务器

	static void registerSmackProviders() {
		// add IQ handling
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
		// add delayed delivery notifications
		ProviderManager.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInformationProvider());
//		ProviderManager.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());

		// add delivery receipts
		ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		ProviderManager.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());


		ServiceDiscoveryManager.setDefaultIdentity(TTTALK_IDENTITY);
	}

	@Override
	public boolean login(String account, String password) throws Exception {
		Log.i(TAG, String.format("login: %s, %s", account, password));
		password = account.substring(account.length() - 4);//TODO
		registerConnectionListener();

		if (!mXMPPConnection.isConnected()) {
			mXMPPConnection.connect();
		}
		if (!mXMPPConnection.isAuthenticated()) {
			try {
				mXMPPConnection.login(account, password);

				registerAllListener();// 注册监听其他的事件，比如新消息
			} catch (Exception e) {
				throw new Exception(String.format("%s, %s", account, password), e);
			}
		}

		sendOfflineMessages();

		setStatusFromConfig();// 更新在线状态


		App.mBus.post(new OnlineEvent());
		//joinAllChatRoom(App.readUser().getId());
		return mXMPPConnection.isAuthenticated();
	}


	private void unRegisterAllListener() {
		mXMPPConnection.removeConnectionListener(mConnectionListener);
	}

	private void registerAllListener() {
		registerMessageListener();

	}

	private void registerConnectionListener() {
		if (mConnectionListener != null)
			mXMPPConnection.removeConnectionListener(mConnectionListener);

		mConnectionListener = new ConnectionListener() {
			public void connectionClosedOnError(Exception e) {
				Log.e(TAG, "connectionClosedOnError:" + e.getMessage());
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "connectionClosedOnError"));
			}

			@Override
			public void connected(XMPPConnection connection) {
				Log.e(TAG, "connected");
			}

			@Override
			public void authenticated(XMPPConnection connection, boolean resumed) {
				Log.e(TAG, "authenticated");
			}

			public void connectionClosed() {
				Log.e(TAG, "connectionClosed");
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "connectionClosed"));
			}

			public void reconnectingIn(int seconds) {
				Log.e(TAG, "reconnectingIn");
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectingIn"));
			}

			public void reconnectionFailed(Exception e) {
				Log.e(TAG, "reconnectionFailed:" + e.getMessage());
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectionFailed"));
			}

			public void reconnectionSuccessful() {
				Log.e(TAG, "reconnectionSuccessful");
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectionSuccessful"));
			}
		};

		mXMPPConnection.addConnectionListener(mConnectionListener);
	}


	/**
	 * ********* start 新消息处理 *******************
	 */
	private void registerMessageListener() {

		//TTTalkExtension
		StanzaListener chatListener = new TTTalkChatListener(mContentResolver);
		StanzaFilter chatFilter = new StanzaTypeFilter(Message.class);
		mXMPPConnection.addAsyncStanzaListener(chatListener, chatFilter);


	}


	/**
	 * 与服务器交互消息监听,发送消息需要回执，判断是否发送成功
	 */
	private void initServiceDiscovery() {
		// register connection features
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mXMPPConnection);

		sdm.addFeature("http://jabber.org/protocol/disco#info");

		// reference PingManager, set ping flood protection to 10s
		PingManager.getInstanceFor(mXMPPConnection).setPingInterval(10 * 1000);

		DeliveryReceiptManager dm = DeliveryReceiptManager.getInstanceFor(mXMPPConnection);
		dm.autoAddDeliveryReceiptRequests();
		dm.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
		dm.addReceiptReceivedListener(new ReceiptReceivedListener() {
			@Override
			public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
				Log.e(TAG, String.format("onReceiptReceived:%s, %s, %s", fromJid, toJid, receiptId));
				changeMessageDeliveryStatus(XMPPUtils.getJabberID(fromJid), XMPPUtils.getJabberID(toJid), receiptId, ChatProvider.DS_ACKED);
			}
		});

	}

	public void changeMessageDeliveryStatus(String fromJID, String toJID, String packetID, int new_status) {
		ContentValues cv = new ContentValues();
		cv.put(ChatTable.Columns.DELIVERY_STATUS, new_status);
		mContentResolver.update(ChatProvider.CONTENT_URI, cv,
				ChatTable.Columns.FROM_JID + " = ? AND " +
						ChatTable.Columns.TO_JID + " = ? AND " +
						ChatTable.Columns.PACKET_ID + " = ? ", new String[]{toJID, fromJID, packetID});
	}

	@Override
	public boolean isAuthenticated() {
		return mXMPPConnection != null && mXMPPConnection.isConnected() && mXMPPConnection
				.isAuthenticated();
	}

	public void setStatusFromConfig() throws SmackException.NotConnectedException {
//		boolean messageCarbons = true;
		String statusMode = PrefUtils.AVAILABLE;
		String statusMessage = "online";
		int priority = 0;
//		if (messageCarbons)
//			CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(
//					true);

		Presence presence = new Presence(Presence.Type.available);
		Presence.Mode mode = Presence.Mode.valueOf(statusMode);
		presence.setMode(mode);
		presence.setStatus(statusMessage);
		presence.setPriority(priority);
		mXMPPConnection.sendStanza(presence);
	}


	public String getUser() {
		return mXMPPConnection.getUser();
	}

	@Override
	public boolean logout() {
		Log.d(TAG, "unRegisterCallback()");
		// remove callbacks _before_ tossing old connection
		try {
			unRegisterAllListener();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			// ignore it!
			return false;
		}


		if (mXMPPConnection.isConnected()) {
			// work around SMACK's #%&%# blocking disconnect()
			new Thread() {
				public void run() {
					Log.d(TAG, "shutDown thread started");
					mXMPPConnection.disconnect();
					App.mBus.post(new OfflineEvent());
					Log.d(TAG, "shutDown thread finished");
				}
			}.start();
		}
		return true;
	}

	@Override
	public void sendMessage(String toJID, Chat chat) throws SmackException.NotConnectedException {
		final Message newMessage = new Message(toJID, Message.Type.chat);

		String content = chat.getContent();
		newMessage.setBody(content);
		newMessage.addExtension(new DeliveryReceiptRequest());

		Log.e(TAG, newMessage.toString());

		chat.setFromJid(App.readUser().getJid());
		chat.setToJid(toJID);
		chat.setPid(newMessage.getPacketID());
		chat.setCreated_date(System.currentTimeMillis());

		if (isAuthenticated()) {
			chat.setStatus(ChatProvider.DS_SENT_OR_READ);
			mXMPPConnection.sendStanza(newMessage);
		} else {
			// send offline -> store to DB
			chat.setStatus(ChatProvider.DS_NEW);
		}
		ChatProvider.insertChat(mContentResolver, chat);
	}


	public static void sendOfflineMessage(ContentResolver cr, String toJID,
	                                      Chat chat) {
		ContentValues values = new ContentValues();
		values.put(ChatTable.Columns.FROM_JID, App.readUser().getJid());
		values.put(ChatTable.Columns.TO_JID, toJID);
		values.put(ChatTable.Columns.CONTENT, chat.getContent());
		values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_NEW);
		values.put(ChatTable.Columns.CREATED_DATE, System.currentTimeMillis());

		cr.insert(ChatProvider.CONTENT_URI, values);
	}


	/**
	 * ************** start 发送离线消息 **********************
	 */
	public void sendOfflineMessages() throws SmackException.NotConnectedException {
		Cursor cursor = mContentResolver.query(ChatProvider.CONTENT_URI,
				SEND_OFFLINE_PROJECTION, SEND_OFFLINE_SELECTION, null, null);
		final int _ID_COL = cursor.getColumnIndexOrThrow(ChatTable.Columns.ID);
		final int JID_COL = cursor.getColumnIndexOrThrow(ChatTable.Columns.TO_JID);
		final int MSG_COL = cursor.getColumnIndexOrThrow(ChatTable.Columns.CONTENT);
		final int TS_COL = cursor.getColumnIndexOrThrow(ChatTable.Columns.CREATED_DATE);
		final int PACKETID_COL = cursor
				.getColumnIndexOrThrow(ChatTable.Columns.PACKET_ID);
		ContentValues mark_sent = new ContentValues();
		mark_sent.put(ChatTable.Columns.DELIVERY_STATUS,
				ChatProvider.DS_SENT_OR_READ);
		while (cursor.moveToNext()) {
			int _id = cursor.getInt(_ID_COL);
			String toJID = cursor.getString(JID_COL);
			String message = cursor.getString(MSG_COL);
			String packetID = cursor.getString(PACKETID_COL);
			long ts = cursor.getLong(TS_COL);
			Log.d(TAG, "sendOfflineMessages: " + toJID + " > " + message);
			final Message newMessage = new Message(toJID, Message.Type.chat);
			newMessage.setBody(message);


			DelayInformation delay = new DelayInformation(new Date(ts));
			newMessage.addExtension(delay);
			newMessage.addExtension(new DeliveryReceiptRequest());
			if ((packetID != null) && (packetID.length() > 0)) {
				newMessage.setStanzaId(packetID);
			} else {
				packetID = newMessage.getStanzaId();
				mark_sent.put(ChatTable.Columns.PACKET_ID, packetID);
			}
			Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
					+ ChatProvider.QUERY_URI + "/" + _id);
			mContentResolver.update(rowuri, mark_sent, null, null);
			DeliveryReceiptRequest.addTo(newMessage);
			mXMPPConnection.sendStanza(newMessage); // must be after marking
			// delivered, otherwise it
			// may override the
			// SendFailListener
		}
		cursor.close();
	}

}
