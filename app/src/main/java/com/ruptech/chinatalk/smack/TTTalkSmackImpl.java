package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.db.ChatProvider;
import com.ruptech.chinatalk.event.ConnectionStatusChangedEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.OfflineEvent;
import com.ruptech.chinatalk.event.OnlineEvent;
import com.ruptech.chinatalk.event.SystemMessageEvent;
import com.ruptech.chinatalk.exception.XMPPException;
import com.ruptech.chinatalk.sqlite.TableContent.ChatTable;
import com.ruptech.chinatalk.utils.NetUtil;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.carbons.CarbonManager;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.Collection;
public class TTTalkSmackImpl implements TTTalkSmack {
    public static final String XMPP_IDENTITY_NAME = "tttalk";
    public static final String XMPP_IDENTITY_TYPE = "phone";
    public static final int PACKET_TIMEOUT = 30000;

    protected final String TAG = Utils.CATEGORY
            + TTTalkSmackImpl.class.getSimpleName();
    //    final static private String[] SEND_OFFLINE_PROJECTION = new String[]{
//            ChatConstants._ID, ChatConstants.JID, ChatConstants.MESSAGE,
//            ChatConstants.DATE, ChatConstants.PACKET_ID};
//    final static private String SEND_OFFLINE_SELECTION = ChatConstants.DIRECTION
//            + " = "
//            + ChatConstants.OUTGOING
//            + " AND "
//            + ChatConstants.DELIVERY_STATUS + " = " + ChatConstants.DS_NEW;
    private final ContentResolver mContentResolver;
    private ConnectionConfiguration mXMPPConfig;
    private XMPPConnection mXMPPConnection;
    private PacketListener mPacketListener;
    private PacketListener mSendFailureListener;

    static {
        registerSmackProviders();
    }

    public TTTalkSmackImpl(String server, int port, ContentResolver contentResolver) {
        boolean smackDebug = PrefUtils.getPrefBoolean(
                PrefUtils.SMACKDEBUG, false);
        boolean requireSsl = PrefUtils.getPrefBoolean(
                PrefUtils.REQUIRE_TLS, false);

        ProviderManager.getInstance().addExtensionProvider(TTTalkRequestExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkRequestExtension.Provider());
        ProviderManager.getInstance().addExtensionProvider(TTTalkQaExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkQaExtension.Provider());
        ProviderManager.getInstance().addExtensionProvider(TTTalkAnnouncementExtension.ELEMENT_NAME, AbstractTTTalkExtension.NAMESPACE, new TTTalkAnnouncementExtension.Provider());

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
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());

        // add delayed delivery notifications
        pm.addExtensionProvider("delay", "urn:xmpp:delay",
                new DelayInfoProvider());
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
        // add carbons and forwarding
        pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
                new Forwarded.Provider());
        pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
        pm.addExtensionProvider("received", Carbon.NAMESPACE,
                new Carbon.Provider());
        // add delivery receipts
        pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
                DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
                DeliveryReceipt.NAMESPACE,
                new DeliveryReceiptRequest.Provider());
        // add XMPP Ping (XEP-0199)
        pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());

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
            //registerRosterListener();// 监听联系人动态变化
            mXMPPConnection.connect();
            if (!mXMPPConnection.isConnected()) {
                throw new XMPPException("SMACK connect failed without exception!");
            }
            mXMPPConnection.addConnectionListener(new ConnectionListener() {
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
            });
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

    private void registerAllListener() {
        // actually, authenticated must be true now, or an exception must have
        // been thrown.
        if (isAuthenticated()) {
            registerMessageListener();
            registerMessageSendFailureListener();
//            registerPongListener();
//            sendOfflineMessages();
//            if (mSmackListener == null) {
//                mXMPPConnection.disconnect();
//                return;
//            }
//            // we need to "ping" the service to let it know we are actually
//            // connected, even when no roster entries will come in
//            mSmackListener.onRosterChanged();
        }
    }

    /**
     * ********* start 新消息处理 *******************
     */
    private void registerMessageListener() {
        // do not register multiple packet listeners
        if (mPacketListener != null)
            mXMPPConnection.removePacketListener(mPacketListener);

        PacketTypeFilter filter = new PacketTypeFilter(Message.class);

        mPacketListener = new PacketListener() {
            public void processPacket(Packet packet) {
                try {
                    if (packet instanceof Message) {
                        Message msg = (Message) packet;
                        String chatMessage = msg.getBody();

                        String fromJID = XMPPUtils.getJabberID(msg.getFrom());

                        Log.e(TAG, msg.toXML());
                        if ("tttalk.org".equals(fromJID)) {
                            App.mBus.post(new SystemMessageEvent(chatMessage));
                        }

                        // try to extract a carbon
                        Carbon cc = CarbonManager.getCarbon(msg);
                        if (cc != null
                                && cc.getDirection() == Carbon.Direction.received) {
                            Log.d(TAG, "carbon: " + cc.toXML());
                            msg = (Message) cc.getForwarded()
                                    .getForwardedPacket();
                            chatMessage = msg.getBody();
                            // fall through
                        } else if (cc != null
                                && cc.getDirection() == Carbon.Direction.sent) {
                            Log.d(TAG, "carbon: " + cc.toXML());
                            msg = (Message) cc.getForwarded()
                                    .getForwardedPacket();
                            chatMessage = msg.getBody();
                            if (chatMessage == null)
                                return;

                            addChatMessageToDB(ChatProvider.OUTGOING, fromJID,
                                    chatMessage, ChatProvider.DS_SENT_OR_READ,
                                    System.currentTimeMillis(),
                                    msg.getPacketID());
                            // always return after adding
                            return;
                        }

                        if (chatMessage == null) {
                            return;
                        }

                        if (msg.getType() == Message.Type.error) {
                            chatMessage = "<Error> " + chatMessage;
                        }

                        long ts;
                        DelayInfo timestamp = (DelayInfo) msg.getExtension(
                                "delay", "urn:xmpp:delay");
                        if (timestamp == null)
                            timestamp = (DelayInfo) msg.getExtension("x",
                                    "jabber:x:delay");
                        if (timestamp != null)
                            ts = timestamp.getStamp().getTime();
                        else
                            ts = System.currentTimeMillis();

                        if (fromJID.startsWith(App.properties.getProperty("translator_jid"))){
                            Collection<PacketExtension> extensions = msg.getExtensions();
                            for(PacketExtension ext : extensions){
                                if (ext instanceof TTTalkExtension){
                                    TTTalkExtension tttalkExtension =(TTTalkExtension)ext;
                                    String messageId = tttalkExtension.getValue("message_id");
                                    setToContent(messageId, chatMessage);
                                }
                            }

                        }else{
                            addChatMessageToDB(ChatProvider.INCOMING, fromJID,
                                    chatMessage, ChatProvider.DS_NEW, ts,
                                    msg.getPacketID());
                        }

                        App.mBus.post(new NewChatEvent(fromJID, chatMessage));

                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to process packet:");
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        };

        mXMPPConnection.addPacketListener(mPacketListener, filter);
    }

    private void setToContent(String messageID, String message) {
        ContentValues cv = new ContentValues();
        cv.put(ChatTable.Columns.TO_MESSAGE, message);

        mContentResolver.update(ChatProvider.CONTENT_URI, cv, ChatTable.Columns.MESSAGE_ID
                + " = ?  " , new String[]{messageID});
    }

    private void addChatMessageToDB(int direction, String JID, String message,
                                    int delivery_status, long ts, String packetID) {
        ContentValues values = new ContentValues();

        values.put(ChatTable.Columns.DIRECTION, direction);
        values.put(ChatTable.Columns.JID, JID);
        values.put(ChatTable.Columns.MESSAGE, message);
        values.put(ChatTable.Columns.DELIVERY_STATUS, delivery_status);
        values.put(ChatTable.Columns.DATE, ts);
        values.put(ChatTable.Columns.PACKET_ID, packetID);

        mContentResolver.insert(ChatProvider.CONTENT_URI, values);
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
                    if (packet instanceof Message) {
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
                    }
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
//                + " = ? AND " + ChatConstants.DIRECTION + " = "
//                + ChatConstants.OUTGOING, new String[]{packetID});
    }


    private String getGroup(Collection<RosterGroup> groups) {
        for (RosterGroup group : groups) {
            return group.getName();
        }
        return "";
    }

    private String getName(RosterEntry rosterEntry) {
        String name = rosterEntry.getName();
        if (name != null && name.length() > 0) {
            return name;
        }
        name = StringUtils.parseName(rosterEntry.getUser());
        if (name.length() > 0) {
            return name;
        }
        return rosterEntry.getUser();
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
        if (mXMPPConnection != null) {
            return (mXMPPConnection.isConnected() && mXMPPConnection
                    .isAuthenticated());
        }
        return false;
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
    public byte[] getAvatar(String jid) throws XMPPException {
        try {
            VCard vcard = new VCard();
            vcard.load(mXMPPConnection, jid);
            return vcard.getAvatar();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw new XMPPException(e.getMessage());
        }
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
            mXMPPConnection.removePacketListener(mPacketListener);
            mXMPPConnection
                    .removePacketSendFailureListener(mSendFailureListener);

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
    public void sendMessage(String toJID, String message, Collection<PacketExtension> extensions) {

        final Message newMessage = new Message(toJID, Message.Type.chat);
        newMessage.setBody(message);
        newMessage.addExtension(new DeliveryReceiptRequest());
        if (extensions != null) {
            //TODO: merge tttalk extensions
            for (PacketExtension extension : extensions) {
                newMessage.addExtension(extension);
            }
        }

        if (isAuthenticated()) {
            addChatMessageToDB(ChatProvider.OUTGOING, toJID, message,
                    ChatProvider.DS_SENT_OR_READ, System.currentTimeMillis(),
                    newMessage.getPacketID());
            mXMPPConnection.sendPacket(newMessage);
        } else {
            // send offline -> store to DB
            addChatMessageToDB(ChatProvider.OUTGOING, toJID, message,
                    ChatProvider.DS_NEW, System.currentTimeMillis(),
                    newMessage.getPacketID());
        }
    }

    public static void sendOfflineMessage(ContentResolver cr, String toJID,
                                          String message) {
        ContentValues values = new ContentValues();
        values.put(ChatTable.Columns.DIRECTION, ChatProvider.OUTGOING);
        values.put(ChatTable.Columns.JID, toJID);
        values.put(ChatTable.Columns.MESSAGE, message);
        values.put(ChatTable.Columns.DELIVERY_STATUS, ChatProvider.DS_NEW);
        values.put(ChatTable.Columns.DATE, System.currentTimeMillis());

        cr.insert(ChatProvider.CONTENT_URI, values);
    }
}
