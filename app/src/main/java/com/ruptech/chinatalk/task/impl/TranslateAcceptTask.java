package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Message;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.AppPreferences;

import java.util.List;

/**
 * 接收者接收到message后，请求翻译。因为发送者费用不足支付翻译费用了。
 *
 * @author zhaolei
 *
 */
public class TranslateAcceptTask extends GenericTask {
	private final long messageId;
	private List<Message> messageList;
	private boolean existTranslatedMessage;

	public TranslateAcceptTask(long messageId) {
		this.messageId = messageId;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		messageList = App.getHttpServer().acceptTranslateMessage(messageId);
		for (Message message : messageList) {
			if (AppPreferences.MESSAGE_STATUS_TRANSLATED == message.getMessage_status()) {
				existTranslatedMessage = true;
				break;
			}
		}

		App.messageDAO.insertMessages(messageList, false);

		return TaskResult.OK;
	}

	public boolean getIsNeedRetrieveUser() {
		return existTranslatedMessage;
	}

	@Override
	public Object[] getMsgs() {
		return new Object[] {messageId};
	}
}
