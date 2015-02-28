package com.ruptech.chinatalk.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.MessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.FileHelper;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

/**
 * 更新版本
 *
 * @author Administrator
 *
 */
public class UpdateVersionService extends Service {

	private final String TAG = Utils.CATEGORY
			+ UpdateVersionService.class.getSimpleName();
	private static boolean doUpdateVersion;

	MyNotificationBuilder mBuilder;
	private PendingIntent pendingIntent;

	/***
	 * 创建通知栏
	 */

	private final int notificationId = R.string.new_version_loading_begin;

	private int updateCount = 0;// 已经上传的文件大小

	private File updateApkFile;

	private final Handler handler = new Handler();

	private String updateFailureMsg;

	/***
	 * 开线程下载
	 */
	private void createDownloadThread() {

		downApkFile();

	}

	private void createNotification() {

		mBuilder = MessageReceiver.createNotificationBuilder(this,
				getString(R.string.app_name), App.readServerAppInfo().verName
						+ " " + getString(R.string.new_version_loading) + "0%",
				null, false);
		mBuilder.setAutoCancel(true);
		mBuilder.setShowSetting(false);
		mBuilder.setTicker(getString(R.string.new_version_loading_begin));
		App.notificationManager.cancel(R.string.please_click_to_update_newapk);
		App.notificationManager.cancel(notificationId);
		mBuilder.setProgress(100, 0, false);
		App.notificationManager.notify(notificationId, mBuilder.build());

	}
	void downApkFile() {
		new Thread() {
			@Override
			public void run() {
				try {
					updateApkFile = FileHelper.apkToSave();
					getApkFile(updateApkFile);
					if (updateApkFile.exists()
							&& UpdateVersionService.doUpdateVersion) {
						sendMsg(2);// 通知下载完成
					}
				} catch (Exception e) {
					String msg = e.toString();
					if (msg.contains("No space left on device")) {
						updateFailureMsg = getString(R.string.no_space_left_on_device);
						sendMsg(-1);// 通知下载失败
					}
					if (msg.contains("Read-only file system")) {
						updateFailureMsg = getString(R.string.read_only_file_system_left_on_device);
						sendMsg(-1);// 通知下载失败
					} else {
						updateFailureMsg = getString(R.string.new_version_load_failure);
						sendMsg(-1);// 通知下载失败
					}
					Utils.sendClientException(e,
							updateApkFile.getAbsolutePath());
				}
			}

		}.start();
	}

	// 大文件的下载的速度太慢，需要替换掉Response，直接使用Url。
	private void getApkFile(File apkFile) throws Exception {
		UpdateVersionService.doUpdateVersion = true;
		String downloadUrl = App.readServerAppInfo().getAppServerUrl() + "../"
				+ App.readServerAppInfo().apkname;
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "downloadUrl:" + downloadUrl);
			Log.d(TAG, "outFile:" + apkFile);
		}

		URL url = new URL(downloadUrl);
		URLConnection conn = url.openConnection();
		if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
			conn.setRequestProperty("Connection", "close");
		}
		conn.connect();
		int status = ((HttpURLConnection) conn).getResponseCode();
		if (status == HttpURLConnection.HTTP_OK) {
			InputStream is = conn.getInputStream();
			FileOutputStream fileOutputStream = null;
			if (is != null) {
				fileOutputStream = new FileOutputStream(apkFile);
				byte[] buf = new byte[1024];
				int ch = -1;
				int downLoadFileSize = 0;
				int down_step = 5;
				updateCount = 0;
				long totalSize = App.readServerAppInfo().fileSize;
				while ((ch = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, ch);
					downLoadFileSize += ch;
					/**
					 * 每次增张5%
					 */
					if (updateCount == 0
							|| (downLoadFileSize * 100 / totalSize - down_step) >= updateCount) {
						updateCount += down_step;
						if (UpdateVersionService.doUpdateVersion) {
							sendMsg(1);// 更新进度条
						} else {
							break;
						}
					}
				}
				if (BuildConfig.DEBUG)
					Log.d(TAG, "downLoadFileSize:" + downLoadFileSize);
			}
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
			is.close();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		createNotification();

		createDownloadThread();

		return super.onStartCommand(intent, flags, startId);

	}

	private void sendMsg(final int flag) {

		final String newVersionName = String.format("%s ",
				App.readServerAppInfo().verName);
		handler.post(new Runnable() {

			@Override
			public void run() {
				switch (flag) {
				case 1:
					mBuilder.setProgress(100, updateCount, false);
					mBuilder.setContentText(newVersionName
							+ getString(R.string.new_version_loading)
							+ updateCount + "%");
					if (updateCount == 100) {
						mBuilder.setDefaults(Notification.DEFAULT_SOUND);
					}
					if (updateCount == 0)
						mBuilder.setVibrate(AppPreferences.NOTIFICATION_VIBRATE);
					else
						mBuilder.setVibrate(null);
					App.notificationManager.notify(notificationId,
							mBuilder.build());

					break;
				case 2:
					Uri uri = Uri.fromFile(updateApkFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(uri,
							"application/vnd.android.package-archive");

					pendingIntent = PendingIntent.getActivity(
							UpdateVersionService.this, 0, intent, 0);
					mBuilder.setContentText(
							newVersionName
									+ getString(R.string.new_version_load_success))
							.setTicker(
									newVersionName
											+ getString(R.string.new_version_load_success))
							.setVibrate(AppPreferences.NOTIFICATION_VIBRATE)
							.setProgress(0, 0, false)
							.setContentIntent(pendingIntent).setContent(null);
					App.notificationManager.notify(notificationId,
							mBuilder.build());
					break;
				case -1:
					Intent updateIntent = UpdateVersionServiceActivity
					.createIntent(getApplicationContext());
					pendingIntent = PendingIntent.getActivity(
							UpdateVersionService.this, 0, updateIntent, 0);
					mBuilder.setContentText(updateFailureMsg)
					.setTicker(updateFailureMsg)
					.setVibrate(AppPreferences.NOTIFICATION_VIBRATE)
					.setContentIntent(pendingIntent).setContent(null);
					App.notificationManager.notify(notificationId,
							mBuilder.build());
					break;
				default:
					break;
				}
			}
		});

	}
}