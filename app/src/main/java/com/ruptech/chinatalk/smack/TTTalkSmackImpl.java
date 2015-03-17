package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.event.AnnouncementEvent;
import com.ruptech.chinatalk.event.ConnectionStatusChangedEvent;
import com.ruptech.chinatalk.event.FriendEvent;
import com.ruptech.chinatalk.event.OfflineEvent;
import com.ruptech.chinatalk.event.OnlineEvent;
import com.ruptech.chinatalk.event.PresentEvent;
import com.ruptech.chinatalk.event.QAEvent;
import com.ruptech.chinatalk.event.StoryEvent;
import com.ruptech.chinatalk.exception.XMPPException;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.smack.ext.AbstractTTTalkExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkAnnouncementExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkBalanceExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkFriendExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkPresentExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkQaExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkStoryExtension;
import com.ruptech.chinatalk.smack.ext.TTTalkTranslatedExtension;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.NetUtil;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerUtilities;
import com.ruptech.chinatalk.utils.Utils;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TTTalkSmackImpl implements TTTalkSmack {
	public static final String XMPP_IDENTITY_NAME = "tttalk";
	public static final String XMPP_IDENTITY_TYPE = "phone";
	public static final int PACKET_TIMEOUT = 30000;

	protected final String TAG = Utils.CATEGORY + TTTalkSmackImpl.class.getSimpleName();

	final static private String[] SEND_OFFLINE_PROJECTION = new String[]{
			ChatTable.Columns.ID, ChatTable.Columns.TO_JID, ChatTable.Columns.CONTENT,
			ChatTable.Columns.CREATED_DATE, ChatTable.Columns.PACKET_ID};
	final static private String SEND_OFFLINE_SELECTION = ChatTable.Columns.FROM_JID
			+ " = '" + App.readUser().getOF_JabberID() + "' AND "
			+ ChatTable.Columns.DELIVERY_STATUS + " = " + ChatProvider.DS_NEW;

	private final ContentResolver mContentResolver;
	private ConnectionConfiguration mXMPPConfig;
	private XMPPConnection mXMPPConnection;
	private InvitationListener mInvitationListener;
	private PacketListener mSendFailureListener;
	private ConnectionListener mConnectionListener;
	private Roster mRoster;
	private RosterListener mRosterListener;

	static {
		registerSmackProviders();
	}

	public TTTalkSmackImpl(String server, int port, ContentResolver contentResolver) {
		boolean smackDebug = PrefUtils.getPrefBoolean(
				PrefUtils.SMACKDEBUG, false);
		boolean requireSsl = PrefUtils.getPrefBoolean(
				PrefUtils.REQUIRE_TLS, false);

		this.mXMPPConfig = new ConnectionConfiguration(server, port);

		this.mXMPPConfig.setReconnectionAllowed(false);
		this.mXMPPConfig.setSendPresence(false);
		this.mXMPPConfig.setCompressionEnabled(false); // disable for now
		this.mXMPPConfig.setDebuggerEnabled(smackDebug);
		this.mXMPPConfig.setSASLAuthenticationEnabled(requireSsl);
		if (requireSsl) {
			this.mXMPPConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
		} else {
			this.mXMPPConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
		mContentResolver = contentResolver;
	}

	// ping-pong服务器

	static void registerSmackProviders() {
		ProviderManager pm = ProviderManager.getInstance();
		// add IQ handling
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());

		// add delayed delivery notifications
		pm.addExtensionProvider("delay", "urn:xmpp:delay", new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
		// add carbons and forwarding
//		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE, new Forwarded.Provider());
//		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
//		pm.addExtensionProvider("received", Carbon.NAMESPACE, new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
		//MUC User
		//  Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

//        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
//        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
//        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

		pm.addExtensionProvider(TTTalkTranslatedExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkTranslatedExtension.Provider());
		pm.addExtensionProvider(TTTalkQaExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkQaExtension.Provider());
		pm.addExtensionProvider(TTTalkAnnouncementExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkAnnouncementExtension.Provider());
		pm.addExtensionProvider(TTTalkExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkExtension.Provider());
		pm.addExtensionProvider(TTTalkBalanceExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkBalanceExtension.Provider());
		pm.addExtensionProvider(TTTalkFriendExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkFriendExtension.Provider());
		pm.addExtensionProvider(TTTalkPresentExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkPresentExtension.Provider());
		pm.addExtensionProvider(TTTalkStoryExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkStoryExtension.Provider());


		ServiceDiscoveryManager.setIdentityName(XMPP_IDENTITY_NAME);
		ServiceDiscoveryManager.setIdentityType(XMPP_IDENTITY_TYPE);
	}

	@Override
	public boolean login(String account, String password) throws XMPPException {
		try {
			if (mXMPPConnection.isConnected()) {
				try {
					mXMPPConnection.disconnect();
				} catch (Exception e) {
					Log.d(TAG, "conn.disconnect() failed: " + e);
				}
			}
			SmackConfiguration.setPacketReplyTimeout(PACKET_TIMEOUT);
			SmackConfiguration.setKeepAliveInterval(-1);
			SmackConfiguration.setDefaultPingInterval(0);

			mXMPPConnection.connect();
			if (!mXMPPConnection.isConnected()) {
				throw new XMPPException("SMACK connect failed without exception!");
			}

			initServiceDiscovery();// 与服务器交互消息监听,发送消息需要回执，判断是否发送成功
			// SMACK auto-logins if we were authenticated before
			if (!mXMPPConnection.isAuthenticated()) {
				mXMPPConnection.login(account, password, XMPP_IDENTITY_NAME);
			}
			setStatusFromConfig();// 更新在线状态
		} catch (org.jivesoftware.smack.XMPPException e) {
			throw new XMPPException(e.getLocalizedMessage(),
					e.getWrappedThrowable());
		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			Log.e(TAG, "login(): " + Log.getStackTraceString(e));
			throw new XMPPException(e.getLocalizedMessage(), e.getCause());
		}
		registerAllListener();// 注册监听其他的事件，比如新消息

		ServerUtilities.registerOpenfirePushOnServer(getUser());
		App.mBus.post(new OnlineEvent());
		return mXMPPConnection.isAuthenticated();
	}

	/**
	 * **************************** start 联系人数据库事件处理 *********************************
	 */
	private void registerRosterListener() {
		mRoster = mXMPPConnection.getRoster();
		mRosterListener = new TTTalkRosterListener(mRoster, mContentResolver);
		mRoster.addRosterListener(mRosterListener);
	}

	/**
	 * ************** end 发送离线消息 **********************
	 */


	private void unRegisterAllListener() {
		mXMPPConnection.removeConnectionListener(mConnectionListener);
		mXMPPConnection.removePacketSendFailureListener(mSendFailureListener);
		MultiUserChat.removeInvitationListener(mXMPPConnection, mInvitationListener);
		mRoster.removeRosterListener(mRosterListener);
	}

	private void registerAllListener() {
		// actually, authenticated must be true now, or an exception must have
		// been thrown.
		if (isAuthenticated()) {
			registerConnectionListener();
			registerMessageListener();
			registerMessageSendFailureListener();
			registerChatRoomInvitationListener();
			registerRosterListener();// 监听联系人动态变化
//            registerPongListener();
			sendOfflineMessages();

		}
	}

	private void registerConnectionListener() {
		if (mConnectionListener != null)
			mXMPPConnection.removeConnectionListener(mConnectionListener);

		mConnectionListener = new ConnectionListener() {
			public void connectionClosedOnError(Exception e) {
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "connectionClosedOnError"));
			}

			public void connectionClosed() {
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "connectionClosed"));
			}

			public void reconnectingIn(int seconds) {
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectingIn"));
			}

			public void reconnectionFailed(Exception e) {
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectionFailed"));
			}

			public void reconnectionSuccessful() {
				App.mBus.post(new ConnectionStatusChangedEvent(NetUtil.NETWORK_NONE, "reconnectionSuccessful"));
			}
		};

		mXMPPConnection.addConnectionListener(mConnectionListener);
	}

	private void registerChatRoomInvitationListener() {
		if (mInvitationListener != null)
			MultiUserChat.removeInvitationListener(mXMPPConnection, mInvitationListener);

		mInvitationListener = new InvitationListener() {
			@Override
			public void invitationReceived(final Connection conn, final String room, final String inviter, final String reason,
			                               final String password, final Message message) {
				Log.e(TAG, String.format("invitationReceived - room:%s, inviter:%s, reason:%s, password:%s, message:%s", room, inviter, reason, password, message.toXML()));
				try {
					MultiUserChat muc = new MultiUserChat(conn, room);
					muc.join(App.readUser().getFullname());
					muc.sendMessage("Joined by " + App.readUser().getFullname());
				} catch (org.jivesoftware.smack.XMPPException e) {
					Log.e(TAG, e.getMessage());
				}

			}
		};

		MultiUserChat.addInvitationListener(mXMPPConnection, mInvitationListener);
	}

	/**
	 * ********* start 新消息处理 *******************
	 */
	private void registerMessageListener() {
		PacketFilter translatedFilter = new PacketExtensionFilter(TTTalkTranslatedExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter qaFilter = new PacketExtensionFilter(TTTalkQaExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter announceFilter = new PacketExtensionFilter(TTTalkAnnouncementExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter balanceFilter = new PacketExtensionFilter(TTTalkBalanceExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter friendFilter = new PacketExtensionFilter(TTTalkFriendExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter presentFilter = new PacketExtensionFilter(TTTalkPresentExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter storyFilter = new PacketExtensionFilter(TTTalkStoryExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);
		PacketFilter chatFilter = new PacketExtensionFilter(TTTalkExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE);

		//TTTalkQaExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkQaExtension>(TTTalkQaExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkQaExtension ext) {
				App.mBus.post(new QAEvent(msg.getBody(), Integer.valueOf(ext.getqa_id())));
			}
		}, qaFilter);
		//TTTalkAnnouncementExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkAnnouncementExtension>(TTTalkAnnouncementExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkAnnouncementExtension ext) {
				App.mBus.post(new AnnouncementEvent(msg.getBody(), Integer.valueOf(ext.get_announcement_id())));
			}
		}, announceFilter);
		//TTTalkBalanceExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkBalanceExtension>(TTTalkBalanceExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkBalanceExtension ext) {
				App.readUser().setBalance(Double.valueOf(ext.getBalance()));
			}
		}, balanceFilter);
		//TTTalkFriendExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkFriendExtension>(TTTalkFriendExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkFriendExtension ext) {
				App.mBus.post(new FriendEvent(ext.getFullname(), Integer.valueOf(ext.getFriend_id())));
			}
		}, friendFilter);
		//TTTalkPresentExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkPresentExtension>(TTTalkPresentExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkPresentExtension ext) {
				App.mBus.post(new PresentEvent(Long.valueOf(ext.getPresent_id()), Long.valueOf(ext.getTo_user_photo_id()),
						ext.getFullname(), ext.getTo_user_photo_id(), ext.getPic_url()));
			}
		}, presentFilter);
		//TTTalkStoryExtension
		mXMPPConnection.addPacketListener(new PacketExtensionListener<TTTalkStoryExtension>(TTTalkStoryExtension.class) {
			@Override
			public void processExtension(Message msg, TTTalkStoryExtension ext) {
				App.mBus.post(new StoryEvent(ext.getPhoto_id(), ext.getTitle(), ext.getContent(), ext.getFullname()));
			}
		}, storyFilter);

		//TTTalkTranslatedExtension
		PacketListener translatedListener = new TranslatedListener(TTTalkTranslatedExtension.class, mContentResolver);
		mXMPPConnection.addPacketListener(translatedListener, translatedFilter);

		//TTTalkExtension
		PacketListener chatListener = new TTTalkChatListener(TTTalkExtension.class, mContentResolver);
		mXMPPConnection.addPacketListener(chatListener, chatFilter);

	}

	/**
	 * ************** start 处理消息发送失败状态 **********************
	 */
	private void registerMessageSendFailureListener() {
		// do not register multiple packet listeners
		if (mSendFailureListener != null)
			mXMPPConnection
					.removePacketSendFailureListener(mSendFailureListener);

		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mSendFailureListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					Message msg = (Message) packet;
					String chatMessage = msg.getBody();

					Log.d("SmackableImp",
							"untranslate "
									+ chatMessage
									+ " could not be sent (ID:"
									+ (msg.getPacketID() == null ? "null"
									: msg.getPacketID()) + ")");
//                        changeMessageDeliveryStatus(msg.getPacketID(),
//                                ChatConstants.DS_NEW);
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					Log.e(TAG, "failed to process packet:");
					Log.e(TAG, e.getMessage(), e);
				}
			}
		};

		mXMPPConnection.addPacketSendFailureListener(mSendFailureListener,
				filter);
	}

	/**
	 * ************** end 处理消息发送失败状态 **********************
	 */

	public void changeMessageDeliveryStatus(String packetID, int new_status) {
//        ContentValues cv = new ContentValues();
//        cv.put(ChatConstants.DELIVERY_STATUS, new_status);
//        Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
//                + ChatProvider.TABLE_NAME);
//        mContentResolver.update(rowuri, cv, ChatConstants.PACKET_ID
//                + " = ? AND " + ChatConstants.FROM_JID + " = "
//                + ChatConstants.OUTGOING, new String[]{packetID});
	}


	/**
	 * 与服务器交互消息监听,发送消息需要回执，判断是否发送成功
	 */
	private void initServiceDiscovery() {
		// register connection features
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				.getInstanceFor(mXMPPConnection);
		if (sdm == null)
			sdm = new ServiceDiscoveryManager(mXMPPConnection);

		sdm.addFeature("http://jabber.org/protocol/disco#info");

		// reference PingManager, set ping flood protection to 10s
		PingManager.getInstanceFor(mXMPPConnection).setPingMinimumInterval(
				10 * 1000);
		// reference DeliveryReceiptManager, add listener

		DeliveryReceiptManager dm = DeliveryReceiptManager
				.getInstanceFor(mXMPPConnection);
		dm.enableAutoReceipts();
		dm.registerReceiptReceivedListener(new DeliveryReceiptManager.ReceiptReceivedListener() {
			public void onReceiptReceived(String fromJid, String toJid,
			                              String receiptId) {
				Log.d(TAG, "got delivery receipt for " + receiptId);
				//changeMessageDeliveryStatus(receiptId, ChatConstants.DS_ACKED);
			}
		});
	}

	@Override
	public boolean isAuthenticated() {
		return mXMPPConnection != null && mXMPPConnection.isConnected() && mXMPPConnection
				.isAuthenticated();
	}

	public void setStatusFromConfig() {
		boolean messageCarbons = true;
		String statusMode = PrefUtils.AVAILABLE;
		String statusMessage = "online";
		int priority = 0;
		if (messageCarbons)
			CarbonManager.getInstanceFor(mXMPPConnection).sendCarbonsEnabled(
					true);

		Presence presence = new Presence(Presence.Type.available);
		Presence.Mode mode = Presence.Mode.valueOf(statusMode);
		presence.setMode(mode);
		presence.setStatus(statusMessage);
		presence.setPriority(priority);
		mXMPPConnection.sendPacket(presence);
	}


	@Override
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
	public boolean createAccount(String username, String password) {
		boolean result = false;
		try {
			mXMPPConnection.connect();
			AccountManager accountManager = new AccountManager(mXMPPConnection);
			if (accountManager.supportsAccountCreation()) {
				accountManager.createAccount(username, password);
				result = true;
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	@Override
	public void sendMessage(String toJID, Chat chat) {
		final Message newMessage = new Message(toJID, Message.Type.chat);

		String content = chat.getContent();
		newMessage.setBody(content);
		newMessage.addExtension(new DeliveryReceiptRequest());
		newMessage.addExtension(new TTTalkExtension(AbstractTTTalkExtension.VALUE_TEST,
				AbstractTTTalkExtension.VALUE_VER,
				AbstractTTTalkExtension.VALUE_TITLE,
				chat.getType(),
				chat.getFilePath(),
				String.valueOf(chat.getFromContentLength())
		));

		Log.e(TAG, newMessage.toXML());

		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setToJid(toJID);
		chat.setPid(newMessage.getPacketID());
		chat.setCreated_date(System.currentTimeMillis());

		if (isAuthenticated()) {
			chat.setStatus(ChatProvider.DS_SENT_OR_READ);
			mXMPPConnection.sendPacket(newMessage);
		} else {
			// send offline -> store to DB
			chat.setStatus(ChatProvider.DS_NEW);
		}
		ChatProvider.insertChat(mContentResolver, chat);
	}

	@Override
	public void sendGroupMessage(MultiUserChat toMUChat, Chat chat) {
		final Message newMessage = new Message(toMUChat.getRoom(), Message.Type.groupchat);

		String message = chat.getContent();
		newMessage.setBody(message);
		newMessage.addExtension(new DeliveryReceiptRequest());
		newMessage.addExtension(new TTTalkExtension(AbstractTTTalkExtension.VALUE_TEST,
				AbstractTTTalkExtension.VALUE_VER,
				AbstractTTTalkExtension.VALUE_TITLE,
				chat.getType(),
				chat.getFilePath(),
				String.valueOf(chat.getFromContentLength())
		));

		Log.e(TAG, newMessage.toXML());

		chat.setFromJid(App.readUser().getOF_JabberID());
		chat.setToJid(toMUChat.getRoom());
		chat.setPid(newMessage.getPacketID());
		chat.setCreated_date(System.currentTimeMillis());

		if (isAuthenticated()) {
			chat.setStatus(ChatProvider.DS_SENT_OR_READ);
			try {
				toMUChat.sendMessage(newMessage);
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
			}
		} else {
			// send offline -> store to DB
			chat.setStatus(ChatProvider.DS_NEW);
		}
		ChatProvider.insertChat(mContentResolver, chat);
	}

	public static void sendOfflineMessage(ContentResolver cr, String toJID,
	                                      Chat chat) {
		ContentValues values = new ContentValues();
		values.put(ChatTable.Columns.FROM_JID, App.readUser().getOF_JabberID());
		values.put(ChatTable.Columns.TO_JID, toJID);
		values.put(ChatTable.Columns.CONTENT, chat.getContent());
		values.put(ChatTable.Columns.CONTENT_TYPE, chat.getType());
		values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_NEW);
		values.put(ChatTable.Columns.CREATED_DATE, System.currentTimeMillis());

		cr.insert(ChatProvider.CONTENT_URI, values);
	}

	@Override
	public String getNameForJID(String jid) {
		if (null != this.mRoster.getEntry(jid)
				&& null != this.mRoster.getEntry(jid).getName()
				&& this.mRoster.getEntry(jid).getName().length() > 0) {
			return this.mRoster.getEntry(jid).getName();
		} else {
			return jid;
		}
	}

	/**
	 * ************** start 发送离线消息 **********************
	 */
	public void sendOfflineMessages() {
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
			newMessage.addExtension(new DelayInfo(delay));
			newMessage.addExtension(new DeliveryReceiptRequest());
			if ((packetID != null) && (packetID.length() > 0)) {
				newMessage.setPacketID(packetID);
			} else {
				packetID = newMessage.getPacketID();
				mark_sent.put(ChatTable.Columns.PACKET_ID, packetID);
			}
			Uri rowuri = Uri.parse("content://" + ChatProvider.AUTHORITY + "/"
					+ ChatProvider.QUERY_URI + "/" + _id);
			mContentResolver.update(rowuri, mark_sent, null, null);
			mXMPPConnection.sendPacket(newMessage); // must be after marking
			// delivered, otherwise it
			// may override the
			// SendFailListener
		}
		cursor.close();
	}

	@Override
	public MultiUserChat createChatRoom(List<User> inviteUserList) {
		MultiUserChat room = null;
		try {
			String roomName = String.valueOf(App.readUser().getId());
            String nickName = App.readUser().getFullname();
			for (User user : inviteUserList) {
				roomName = String.format("%s_%d", roomName, user.getId());
                nickName = String.format("%s, %s", nickName, user.getFullname());
			}
			roomName = String.format("%s@conference.tttalk.org", roomName);
			room = new MultiUserChat(mXMPPConnection, roomName);
            room.addInvitationRejectionListener(new InvitationRejectionListener() {
				public void invitationDeclined(String invitee, String reason) {
					//TODO:
					Toast.makeText(App.mContext, "invitationDeclined", Toast.LENGTH_SHORT).show();
				}
			});
			room.create(App.readUser().getFullname());
			room.join(App.readUser().getFullname());

			Form form = room.getConfigurationForm();
			Form submitForm = form.createAnswerForm();
			for (Iterator fields = form.getFields(); fields.hasNext(); ) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			submitForm.setAnswer("muc#roomconfig_publicroom", true);
            submitForm.setAnswer("muc#roomconfig_roomname", nickName);
            submitForm.setAnswer("muc#roomconfig_roomdesc", nickName);
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);

            room.sendConfigurationForm(submitForm);

			for (User user : inviteUserList) {
				room.invite(user.getOF_JabberID(), "Meet me in this excellent room");
                room.grantOwnership(user.getOF_JabberID());
			}
            Chat chat = new Chat();
            chat.setContent("Created room by " + App.readUser().getFullname());
			sendGroupMessage(room, chat);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return room;
	}

	public MultiUserChat createChatRoomByRoomName(String roomName) {
		try {
			MultiUserChat chatRoom = new MultiUserChat(mXMPPConnection, roomName);
			if (!chatRoom.isJoined()) {
				chatRoom.join(App.readUser().getFullname());
			}
			return chatRoom;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		return null;
	}

	public RoomInfo getChatRoomInfo(String roomName) {
		try {
			RoomInfo info = MultiUserChat.getRoomInfo(mXMPPConnection, roomName);
			Log.e(TAG, "Number of occupants:" + info.getOccupantsCount());
			Log.e(TAG, "Room Subject:" + info.getSubject());
            return info;
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
	}
}
