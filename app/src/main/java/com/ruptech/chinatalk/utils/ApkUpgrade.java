package com.ruptech.chinatalk.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;
import com.ruptech.chinatalk.widget.CustomDialog;

public class ApkUpgrade {

	private final Context context;

	private GenericTask mVersionCheckTask;

	private static AlertDialog dialog;

	public ApkUpgrade(Context activity) {
		context = activity;
	}

	public void cancelVersionCheckTask() {
		if (mVersionCheckTask != null
				&& mVersionCheckTask.getStatus() == GenericTask.Status.RUNNING) {
			mVersionCheckTask.cancel(true);
		}
		mVersionCheckTask = null;
	}

	public void checkApkUpdate(boolean silent) {
		if (dialog != null && dialog.isShowing()) {
			return;
		}
		// new version confirm
		if (App.readServerAppInfo() != null
				&& App.readServerAppInfo().verCode > App.mApkVersionOfClient.verCode) {
			StringBuffer sb = new StringBuffer();
			sb.append(context.getString(R.string.current_version))
					.append(":\n\t").append(App.mApkVersionOfClient.verName);
			sb.append("\n")
					.append(context.getString(R.string.new_version_found))
					.append(":\n\t").append(App.readServerAppInfo().verName);
			sb.append("\n").append(context.getString(R.string.whether_udpate));
			dialog = new CustomDialog(context)
					.setMessage(sb.toString())
							// 设置内容
					.setNegativeButton(
							context.getString(R.string.temporarily_no_update),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
								                    int whichButton) {
								}
							})
					.setPositiveButton(context.getString(R.string.update),// 设置确定按钮
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
								                    int which) {
									notificateUpdateVersion();
								}
							});// 创建
			// 显示对话框
			dialog.show();
		} else if (!silent) {
			Toast.makeText(context,
					R.string.version_check_already_newest_version,
					Toast.LENGTH_SHORT).show();
		}
	}
	private void notificateUpdateVersion() {

		String downloadUrl = App.readServerAppInfo().getAppServerUrl() + App.readServerAppInfo().appname + ".apk";
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
		browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(browserIntent);

	}

	public void doRetrieveServerVersion(TaskListener versionCheckListener) {

		if (mVersionCheckTask != null
				&& mVersionCheckTask.getStatus() == GenericTask.Status.RUNNING) {
			mVersionCheckTask.cancel(true);
		}

		mVersionCheckTask = new RetrieveServerVersionTask();
		mVersionCheckTask.setListener(versionCheckListener);

		mVersionCheckTask.execute();
	}
}
