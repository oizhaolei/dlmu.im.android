package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;


/**
 * @author Administrator
 */
public class VersionCheckReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        GenericTask mVersionCheckTask = new RetrieveServerVersionTask();

        mVersionCheckTask.execute();
    }

}