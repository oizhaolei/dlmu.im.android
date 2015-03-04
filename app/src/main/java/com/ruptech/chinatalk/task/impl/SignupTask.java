package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.smack.TTTalkSmackImpl;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.Utils;

public class SignupTask extends GenericTask {

	private final User tempUser;

	public SignupTask(User tempUser) {
		this.tempUser = tempUser;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		User user = App.getHttpServer().userSignup(tempUser.getTel(),
				tempUser.getPassword(), tempUser.getFullname(),
				tempUser.getPic_url(), String.valueOf(tempUser.getGender()),
				tempUser.getLang());
        createOF_User(user);
		App.writeUser(user);
		return TaskResult.OK;
	}

    private void createOF_User(User user){
        String server = App.properties.getProperty("xmpp.server.host");
        int port = Integer.parseInt(App.properties.getProperty("xmpp.server.port"));
        App.mSmack = new TTTalkSmackImpl(server, port, App.mContext.getContentResolver());
        App.mSmack.createAccount(Utils.getOF_username(user.getId()), user.getPassword());
    }

	@Override
	public Object[] getMsgs() {
		return new Object[] { tempUser.getTel(), tempUser.getPassword(),
				tempUser.getFullname(), tempUser.getPic_url(),
				String.valueOf(tempUser.getGender()), tempUser.getLang() };
	}
}
