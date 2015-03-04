package com.ruptech.chinatalk.task.impl;


import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class XMPPLoginTask extends GenericTask {
    String account;
    String password;

    public XMPPLoginTask(String username, String password) {
        this.account = username;
        this.password = password;
    }

    @Override
    protected TaskResult _doInBackground() throws Exception {

        boolean result = App.mSmack.login(account, password);
        if (!result) {
            throw new Exception("failed.");
        }
        return TaskResult.OK;
    }
}