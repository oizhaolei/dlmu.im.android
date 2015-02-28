package com.ruptech.chinatalk.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.AcceptInviteTask;

public class InviteFriendUtils {

	public static void doAcceptInviteTask(final Context context,
			String invite_user_id) {

		TaskListener acceptInviteTaskListener = new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				if (result == TaskResult.OK) {
					int point = App.readServerAppInfo().point_by_invite_sns_friend;
					String message = context.getString(
							R.string.invite_accept_message, point);
					Toast.makeText(App.mContext, message, Toast.LENGTH_LONG)
							.show();
				}
			}
		};
		GenericTask acceptInviteTask = new AcceptInviteTask(invite_user_id);
		acceptInviteTask.setListener(acceptInviteTaskListener);
		acceptInviteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public static void inviteKakaoFriend(Context context) {
		long id = App.readUser().getId();
		String fullname = App.readUser().getFullname();
		String downUrl = App.readServerAppInfo().getAppServerUrl() + "../"
				+ App.readServerAppInfo().apkname;

		if (isPackageInstalled("com.kakao.talk", App.mContext)) {

			String pointGetUrl = App.readServerAppInfo().getAppServerUrl()
					+ "tttalk_invite.php?id=" + id;
			int point = App.readServerAppInfo().point_by_invite_sns_friend;

			String content = context.getString(R.string.invite_message,
					fullname, downUrl, point, pointGetUrl);
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, content);
			intent.setPackage("com.kakao.talk");

			context.startActivity(intent);
		} else {
			Toast.makeText(context, R.string.kakaotalk_is_not_installed,
					Toast.LENGTH_SHORT).show();
		}
	}

	private static boolean isPackageInstalled(String packagename,
			Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}
