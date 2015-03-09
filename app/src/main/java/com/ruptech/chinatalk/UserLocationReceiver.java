package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.event.NewVersionFoundEvent;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;
import com.ruptech.chinatalk.task.impl.UploadUserLocationTask;
import com.ruptech.chinatalk.utils.ApkUpgrade;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.Utils;


/**
 * @author Administrator
 */
public class UserLocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(App.readUser()!=null){
            int late6 = 0;
            int lnge6 = 0;
            if (MyLocation.recentLocation != null) {
                late6 = (Double
                        .valueOf(MyLocation.recentLocation.getLatitude() * 1E6))
                        .intValue();
                lnge6 = (Double
                        .valueOf(MyLocation.recentLocation.getLongitude() * 1E6))
                        .intValue();
            }
            UploadUserLocationTask uploadUserLocationTask = new UploadUserLocationTask(
                    late6, lnge6);
            uploadUserLocationTask.execute();
        }
    }
}