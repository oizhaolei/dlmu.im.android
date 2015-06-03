package com.ruptech.chinatalk.smack;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.VerificationEvent;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.ChatProvider;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.dlmu.im.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;

/**
 * TTTalk translated
 */
public class TTTalkChatListener implements StanzaListener {
    protected final String TAG = Utils.CATEGORY + TTTalkChatListener.class.getSimpleName();
    private final ContentResolver mContentResolver;

    protected TTTalkChatListener(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        Message msg = (Message) packet;

        String body = msg.getBody();
        if (body == null) {
            return;
        }
        String fromJID = XMPPUtils.getJabberID(msg.getFrom());
        String toJID = XMPPUtils.getJabberID(msg.getTo());

        Log.e(TAG, msg.toString());
        if (fromJID.equals(toJID)) {
            return;
        }
        //不接受任何消息
        if(PrefUtils.getPrefNotReceiveMessage()){
            return;
        }
        //屏蔽
        User user = App.userDAO.fetchUserByUsername(User.getUsernameFromJid(fromJID));
        if(user != null && user.block.equals("true")){
            return;
        }
        //需要验证
        Cursor chatHistory = ChatActivity.reQuery(mContentResolver, fromJID);
        if (PrefUtils.getPrefVerificationMessage() && (chatHistory == null || chatHistory.getCount() <= 0 )) {
            App.mBus.post(new VerificationEvent(fromJID, body));
        } else {
            App.mBus.post(new NewChatEvent(fromJID, body));
        }

        long ts = System.currentTimeMillis();

        Chat chat = new Chat();
        chat.setFromJid(fromJID);
        chat.setToJid(toJID);
        chat.setContent(body);
        chat.setPid(msg.getPacketID());
        chat.setStatus(ChatProvider.DS_NEW);
        chat.setCreated_date(ts);

        //from fullname, to fullname
        User fromUser = App.userDAO.fetchUserByUsername(User.getUsernameFromJid(fromJID));
        User toUser = App.userDAO.fetchUserByUsername(User.getUsernameFromJid(toJID));
        if (fromUser != null) {
            chat.setFromFullname(fromUser.getFullname());
        }
        if (toUser != null) {
            chat.setToFullname(toUser.getFullname());
        }

        if (fromUser == null) {
            retrieveUser(fromJID, false);
        } else if (toUser == null) {
            retrieveUser(toJID, false);
        }

        ChatProvider.insertChat(mContentResolver, chat);
    }

    public static void retrieveUser(final String jid, final boolean block) {
        String username = User.getUsernameFromJid(jid);
        RetrieveUserTask retrieveUserTask = new RetrieveUserTask(username);
        TaskAdapter taskListener = new TaskAdapter() {
            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                super.onPostExecute(task, result);
                RetrieveUserTask retrieveUserTask = (RetrieveUserTask) task;
                if (result == TaskResult.OK) {
                    User user = retrieveUserTask.getUser();
                    if(block){
                        user.setBlock(String.valueOf(block));
                    }
                    App.userDAO.mergeUser(user);

                    ChatProvider.updateChat(jid, user.getFullname());
                }
            }

        };
        retrieveUserTask.setListener(taskListener);
        retrieveUserTask.execute();
    }


}