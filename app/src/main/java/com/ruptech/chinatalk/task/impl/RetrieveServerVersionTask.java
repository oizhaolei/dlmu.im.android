package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.PrefUtils;

public class RetrieveServerVersionTask extends GenericTask {


    @Override
    protected TaskResult _doInBackground() throws Exception {
        // check version
        AppVersion serverAppInfo = App.getHttpServer().ver();
        if (serverAppInfo != null) {
            PrefUtils.writeServerAppInfo(serverAppInfo);
        }
        return TaskResult.OK;
    }


}
