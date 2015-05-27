package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

public class RetrieveUserTask extends GenericTask {
    private final String username;
    private User user;

    public RetrieveUserTask(String username) {
        this.username = username;
    }

    public User getUser() {
        return user;
    }

    @Override
    protected TaskResult _doInBackground() throws Exception {
        user = App.getHttpServer().retrieveUser(username);


        return TaskResult.OK;
    }

    @Override
    public Object[] getMsgs() {
        return new Object[]{username};
    }
}
