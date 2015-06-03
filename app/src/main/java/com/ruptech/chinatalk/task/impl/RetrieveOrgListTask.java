package com.ruptech.chinatalk.task.impl;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskResult;

import java.util.List;
import java.util.Map;

public class RetrieveOrgListTask extends GenericTask {
    private final String parentJid;
    private final String isStudent;
    private List<Map<String, Object>> orgList;
    private List<Map<String, Object>> memberList;

    public RetrieveOrgListTask(String parentJid, String isStudent) {
        this.parentJid = parentJid;
        this.isStudent = isStudent;
    }

    public List<Map<String, Object>> getOrgList() {
        return orgList;
    }

    public List<Map<String, Object>> getMemberList() {
        return memberList;
    }

    @Override
    protected TaskResult _doInBackground() throws Exception {
        Map map = App.getHttpServer().retrieveOrgList(parentJid, isStudent);
        orgList = (List<Map<String, Object>>) map.get("orgs");
        memberList = (List<Map<String, Object>>) map.get("members");


        return TaskResult.OK;
    }

    @Override
    public Object[] getMsgs() {
        return new Object[]{parentJid};
    }
}
