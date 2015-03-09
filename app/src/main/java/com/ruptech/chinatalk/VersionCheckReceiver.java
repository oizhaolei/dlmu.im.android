package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruptech.chinatalk.event.NewVersionFoundEvent;
import com.ruptech.chinatalk.event.RefreshNewMarkEvent;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;
import com.ruptech.chinatalk.utils.ApkUpgrade;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.Utils;


/**
 * @author Administrator
 */
public class VersionCheckReceiver extends BroadcastReceiver {

    private final TaskListener serverInfoCheckTaskListener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            ServerAppInfo serverAppInfo = ((RetrieveServerVersionTask) task).getServerAppInfo();
            if (serverAppInfo == null) {
                App.mBadgeCount.versionCount = 0;
            } else {
                PrefUtils.saveVersionCheckedTime();
                App.versionChecked = true;
                if (serverAppInfo.verCode > Utils.getAppVersionCode()) {
                    App.mBus.post(new NewVersionFoundEvent());
                    App.mBadgeCount.versionCount = 1;
                }
            }
            App.mBus.post(new RefreshNewMarkEvent());
        }
    };
    private ApkUpgrade apkUpgrade;

    @Override
    public void onReceive(Context context, Intent intent) {
        getApkUpgrade(context).doRetrieveServerVersion(serverInfoCheckTaskListener);
    }

    private ApkUpgrade getApkUpgrade(Context context) {
        if (apkUpgrade ==null) {
            apkUpgrade = new ApkUpgrade(context);
        }
        return apkUpgrade;
    }
}