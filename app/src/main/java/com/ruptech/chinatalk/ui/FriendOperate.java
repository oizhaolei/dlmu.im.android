package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

public class FriendOperate {

	public enum UserType {
		FRIEND, MYSELF, STRANGER
	}

	Activity mActivity;
	private ProgressDialog progressDialog;
	private GenericTask mFriendAddTask;

	private final TaskListener mFriendBlockListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				addFriendBlockSuccess();
			} else {
				String msg = task.getMsg();
				addFriendBlockFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			addFriendBlockBegin();
		}

	};
	private GenericTask mFriendBlockTask;

	private final TaskListener mFriendRemoveListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onFriendRemoveSuccess();
			} else {
				String msg = task.getMsg();
				onFriendRemoveFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onFriendRemoveBegin();
		}

	};

	private GenericTask mFriendRemoveTask;

	private GenericTask mFriendReportTask;

	private final TaskListener mFriendReportTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				addReportFriendSuccess();
			} else {
				String msg = task.getMsg();
				addReportFriendFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			addReportFriendBegin();
		}
	};


	private final User mUser;

	public FriendOperate(Activity context, final User user, UserType userType) {
		super();
		mActivity = context;
		mUser = user;
	}

	protected void addFriendBegin() {
		startProgress();
	}

	public void addFriendBlockBegin() {
		startProgress();
	}

	public void addFriendBlockFailure(String msg) {
		stopProgress();
		Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
	}

	public void addFriendBlockSuccess() {
		stopProgress();
		Toast.makeText(mActivity, R.string.friend_block_success_message,
				Toast.LENGTH_LONG).show();

		FriendProfileActivity.close();
		ProfileActivity.close();
		CommonUtilities.broadcastChatList(mActivity);
	}

	private void addFriendFailure(String msg) {
		stopProgress();
		Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
	}

	private void addFriendSuccess(String userId) {
		stopProgress();
		CommonUtilities.broadcastChatList(mActivity);
		// TODO FriendListFragment.doRefresh();
		PrefUtils.removePreRecommendedFriendById(Long.valueOf(userId));
		Toast.makeText(mActivity, R.string.friend_add_ok, Toast.LENGTH_SHORT)
				.show();
	}

	private void addReportFriendBegin() {
		startProgress();
	}

	private void addReportFriendFailure(String msg) {
		stopProgress();
		Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
	}

	private void addReportFriendSuccess() {
		stopProgress();
		Toast.makeText(mActivity, R.string.report_from_friends_ok,
				Toast.LENGTH_SHORT).show();
	}

	public void doSaveChatTopSetting(int isTop, boolean flag, User mFriendUser) {
		if (flag) {
			App.friendDAO.updateFriendIsTop(App.readUser().getId(),
					mFriendUser.getId(), isTop);
		} else {
			App.friendDAO.updateFriendIsTop(mFriendUser.getId(), App.readUser()
					.getId(), isTop);
		}
	}


	private void onFriendRemoveBegin() {
		startProgress();
	}

	private void onFriendRemoveFailure(String msg) {
		stopProgress();
		Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
	}

	private void onFriendRemoveSuccess() {
		stopProgress();
		Toast.makeText(mActivity, R.string.friend_delete_success,
				Toast.LENGTH_LONG).show();
		ProfileActivity.close();
		FriendProfileActivity.close();
		CommonUtilities.broadcastChatList(mActivity);
		CommonUtilities.broadcastRemoveFriend(App.mContext);
	}


	public void settingCleanMessage() {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				CommonUtilities.broadcastMessage(mActivity, null);
				CommonUtilities.broadcastChatList(mActivity);
				Toast.makeText(mActivity, R.string.messages_have_been_cleaned,
						Toast.LENGTH_SHORT).show();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(mActivity, positiveListener, negativeListener,
				mActivity.getString(R.string.friend_menu_clean_chat_history),
				mActivity
						.getString(R.string.be_sure_to_delete_all_the_messages));
	}


	private void startProgress() {
		progressDialog = Utils.showDialog(mActivity,
				mActivity.getString(R.string.please_waiting));

	}

	private void stopProgress() {
		Utils.dismissDialog(progressDialog);
	}

}