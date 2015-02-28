package com.ruptech.chinatalk.task.impl;

import java.util.List;

import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;

public class RequestTranslateTask extends GenericTask {
	private Message message;
	private boolean existTranslatedMessage;
	private List<Message> messageList;

	public RequestTranslateTask(Message message) {
		this.message = message;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		if (BuildConfig.DEBUG)
			Log.v(TAG, "RequestTranslateTask");

		Long localId = message.getId();
		Long toUserId = message.to_userid;
		String text = message.getFrom_content();
		String fromLang = message.from_lang;
		String toLang = message.to_lang;
		int contentLength = message.getFrom_content_length();
		String filetype = message.file_type;
		String filePath = message.getFile_path();

		String lastUpdatedate = PrefUtils
				.getPrefMessageLastUpdateDate(message.getUserid());

		messageList = App.getHttpServer().requestTranslate(localId,
				toUserId, fromLang, toLang, text, contentLength,
				filetype, lastUpdatedate, filePath);

		_message(messageList);

		PrefUtils.savePrefMessageLastUpdatedate(message.getUserid(),
				lastUpdatedate);

		// 没有从服务器端取得message_id的数据，再重新取得一边
		List<Message> list = App.messageDAO.fetchNoMessageIdMessage();
		if (list.size() > 0) {
			StringBuffer localIds = new StringBuffer();
			for (Message message : list) {
				localIds.append(message.getId()).append(',');
			}
			localIds.deleteCharAt(localIds.length() - 1);
			List<Message> remoteList = App.getHttpServer()
					.getMessageByLocalIds(localIds.toString());
			_message(remoteList);
		}


		return TaskResult.OK;
	}

	private void _message(List<Message> messageList) {
		for (Message msg : messageList) {
			if (AppPreferences.MESSAGE_STATUS_TRANSLATED == msg
					.getMessage_status()) {
				existTranslatedMessage = true;
			}
			if (msg.getId() == message.getId()) {
				message = msg;
			}
			MessageReceiver.messageCommonAction(App.mContext, msg);
		}
	}

	public boolean getIsNeedRetrieveUser() {
		return existTranslatedMessage;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {message, messageList};
	}
}
