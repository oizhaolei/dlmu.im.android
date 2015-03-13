package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.event.AnnouncementEvent;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveInfoPeriodTask;
import com.ruptech.chinatalk.utils.PrefUtils;

import java.util.List;
import java.util.Map;


/**
 * @author Administrator
 */
public class InfoPeriodReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.readUser() !=null){
            retrieveInfoPeriodTask();
        }
    }

    private void retrieveInfoPeriodTask() {
        RetrieveInfoPeriodTask retrieveInfoPeriodTask = new RetrieveInfoPeriodTask();
        retrieveInfoPeriodTask.setListener(new TaskAdapter() {
            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                RetrieveInfoPeriodTask retrieveInfoPeriodTask = (RetrieveInfoPeriodTask) task;
                if (result == TaskResult.OK) {
                    List<Map<String, String>> announcementList = retrieveInfoPeriodTask
                            .getAnnouncementList();
                    if (announcementList.size() > 0) {
                        Map<String, String> announcement = announcementList
                                .get(announcementList.size() - 1);
                        if (announcement != null) {
                            PrefUtils
                                    .savePrefShowAccouncementDialogLastUpdatedate(announcement
		                                    .get("update_date"));
                            App.mBus.post(new AnnouncementEvent(announcement.get("content"),
                                    Long.parseLong(announcement.get("id"))));
                        }
                    }
                }
            }
        });
        retrieveInfoPeriodTask.execute();
    }
}