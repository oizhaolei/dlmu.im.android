package com.ruptech.chinatalk.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.impl.VersionCheckTask;
import com.ruptech.chinatalk.ui.UpdateVersionService;
import com.ruptech.chinatalk.widget.CustomDialog;

public class DownloadApk {

	private final Activity context;

	private GenericTask mVersionCheckTask;

	private static AlertDialog dialog;

	public DownloadApk(Activity activity) {
		context = activity;
	}

	public void cancelVersionCheckTask(){
		if (mVersionCheckTask != null
				&& mVersionCheckTask.getStatus() == GenericTask.Status.RUNNING) {
			mVersionCheckTask.cancel(true);
		}
		mVersionCheckTask = null;
	}

	public void checkApkUpdate(boolean silent) {
		if (dialog != null && dialog.isShowing() && !context.hasWindowFocus()) {
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
					.setNeutralButton(
							// 去市场更新
							context.getString(R.string.goto_app_store_update),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String downloadUrl = "market://details?id=com.ruptech.chinatalk";
									Intent i = new Intent(Intent.ACTION_VIEW,
											Uri.parse(downloadUrl));
									context.startActivity(i);
								}
							})
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
									checkUpdateVsersionWifiAvailible();
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

	private void checkUpdateVsersionWifiAvailible(){
		if(Utils.isWifiAvailible(context)){
			notificateUpdateVersion();
		} else {
			dialog = new CustomDialog(context)
					.setMessage(
							context.getString(R.string.wifi_are_not_available))
					.setNegativeButton(
							context.getString(R.string.alert_dialog_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							})
					.setPositiveButton(
							context.getString(R.string.alert_dialog_ok),// 设置确定按钮
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									notificateUpdateVersion();
								}
							});// 创建
			// 显示对话框
			dialog.show();
		}
	}

	public void doVersionCheck(TaskListener versionCheckListener) {

		if (mVersionCheckTask != null
				&& mVersionCheckTask.getStatus() == GenericTask.Status.RUNNING) {
			mVersionCheckTask.cancel(true);
		}

		mVersionCheckTask = new VersionCheckTask();
		mVersionCheckTask.setListener(versionCheckListener);

		mVersionCheckTask.execute();
	}

	private void notificateUpdateVersion() {
		Intent updateIntent = new Intent(App.mContext,
				UpdateVersionService.class);
		App.mContext.startService(updateIntent);
	}
}
