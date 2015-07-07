package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class RetrieveServiceListTask extends GenericTask {
    private String userid;
    private String ctrl;
    private List<Service> serviceList;

    public List<Service> getServiceList() {
        return serviceList;
    }

    public RetrieveServiceListTask(String p0, String p1) {
        userid = p0;
        ctrl = p1;
    }

    @Override
    protected TaskResult _doInBackground() throws Exception {

        serviceList = App.getHttpServer().retrieveServiceList(userid,ctrl);

        return TaskResult.OK;
    }

}
