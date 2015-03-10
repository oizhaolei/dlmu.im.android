package com.ruptech.chinatalk.task.impl;

import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.model.Chat;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class XmppRequestTranslateTask extends GenericTask {
	private Message message;
    private Chat chat;

	private boolean existTranslatedMessage;

    public XmppRequestTranslateTask(Chat chat) {
        Message message = new Message();
        this.chat = chat;
    }

	@Override
	protected TaskResult _doInBackground() throws Exception {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "RequestTranslateTask");
        message = App.getHttpServer().xmpp_requestTranslate(chat);

		return TaskResult.OK;
	}


	public boolean getIsNeedRetrieveUser() {
		return existTranslatedMessage;
	}

	public Message getMessage() {
		return message;
	}
    public Chat getChat() {
        return chat;
    }
	@Override
	public Object[] getMsgs() {
		return new Object[] {message};
	}
}
