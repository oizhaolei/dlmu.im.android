package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FriendAddTask;
import com.ruptech.chinatalk.task.impl.FriendBlockTask;
import com.ruptech.chinatalk.task.impl.FriendRemoveTask;
import com.ruptech.chinatalk.task.impl.FriendReportTask;
import com.ruptech.chinatalk.task.impl.RetrieveMessageHistoryTask;
import com.ruptech.chinatalk.ui.setting.ChatSettingActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;

public class FriendOperate {

	public enum UserType {
		FRIEND, MYSELF, STRANGER
	}

	Activity mActivity;
	private ProgressDialog progressDialog;
	private GenericTask mFriendAddTask;

	private final TaskListener mFriendAddListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FriendAddTask friendAddTask = (FriendAddTask) task;
			if (result == TaskResult.OK) {
				addFriendSuccess(friendAddTask.getFriendId());
			} else {
				String msg = task.getMsg();
				addFriendFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			addFriendBegin();
		}
	};

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

	private final TaskListener mRetrieveMessageHistoryListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				getMessageHistorySuccess();
			} else {
				String msg = task.getMsg();
				getMessageHistoryFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			getMessageHistoryBegin();
		}
	};

	private GenericTask mRetrieveMessageHistoryTask;

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
		ChatSettingActivity.close();
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

	private void getMessageHistoryBegin() {
		startProgress();
	}

	private void getMessageHistoryFailure(String msg) {
		stopProgress();
		Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
	}

	private void getMessageHistorySuccess() {
		stopProgress();
		CommonUtilities.broadcastChatList(mActivity);
		// AbstractChatActivity.doRefresh();
		CommonUtilities.broadcastMessage(mActivity, null);
		Toast.makeText(mActivity, R.string.send_request_success,
				Toast.LENGTH_SHORT).show();
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

	public void settingBlockFriend() {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				mFriendBlockTask = new FriendBlockTask(mUser.getId());
				mFriendBlockTask.setListener(mFriendBlockListener);
				mFriendBlockTask.execute();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(mActivity, positiveListener, negativeListener,
				mActivity.getString(R.string.friend_menu_block),
				mActivity.getString(R.string.be_sure_to_block_friend));
	}

	public void settingCleanMessage() {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mUser == null) {
					App.messageDAO.deleteAll();
				} else {
					App.messageDAO.deleteByUserId(mUser.getId());
					App.mBadgeCount.removeNewMessageCount(mUser.getId());
					// PrefUtils.removePrefNewMessageCount(mUser.getId());
				}

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

	public void settingFriendAdd(TaskListener pFriendAddListener) {
		if (mFriendAddTask != null
				&& mFriendAddTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mFriendAddTask = new FriendAddTask(mUser.getTel(), String.valueOf(mUser
				.getId()), mUser.getFullname(), "", "", false);
		if (pFriendAddListener != null) {
			mFriendAddTask.setListener(pFriendAddListener);
		} else {
			mFriendAddTask.setListener(mFriendAddListener);
		}
		mFriendAddTask.execute();

	}

	public void settingGetHistoryFriend() {
		settingGetHistoryFriend(mUser.getId(),
				App.messageDAO.getMinMessageId(mUser.getId()));
	}

	public void settingGetHistoryFriend(long friendId, long minMessageId) {// 获取历史聊天记录的方法
		if (mRetrieveMessageHistoryTask != null
				&& mRetrieveMessageHistoryTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mRetrieveMessageHistoryTask = new RetrieveMessageHistoryTask(friendId,
				minMessageId);
		mRetrieveMessageHistoryTask
				.setListener(mRetrieveMessageHistoryListener);
		mRetrieveMessageHistoryTask.execute();
	}

	public void settingRemoveFriend() {

		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				mFriendRemoveTask = new FriendRemoveTask(mUser.getId());
				mFriendRemoveTask.setListener(mFriendRemoveListener);
				mFriendRemoveTask.execute();
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(mActivity, positiveListener, negativeListener,
				mActivity.getString(R.string.friend_menu_delete),
				mActivity.getString(R.string.be_sure_to_remove_friend));
	}

	public void settingReportFriend() {
		final EditText inputServer = new EditText(mActivity);
		inputServer.setFocusable(true);
		inputServer.setHint(R.string.please_input_reason_for_report);
		int colorResId = mActivity.getResources().getColor(R.color.text_gray);
		inputServer.setHintTextColor(colorResId);
		inputServer.setTextColor(colorResId);
		inputServer.setBackgroundDrawable(mActivity.getResources().getDrawable(
				R.drawable.yellow_light_edit_text_holo_light));

		CustomDialog builder = new CustomDialog(mActivity);
		builder.setTitle(R.string.friend_menu_report);
		builder.setMessage(mActivity
				.getString(R.string.be_sure_to_report_friend));
		builder.setView(inputServer);
		builder.setNegativeButton(R.string.alert_dialog_cancel, null);
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String inputContent = inputServer.getText().toString();
						if (Utils.isEmpty(inputContent)) {
							Toast.makeText(mActivity,
									R.string.please_input_reason_for_report,
									Toast.LENGTH_SHORT).show();
						} else {
							if (mFriendReportTask != null
									&& mFriendReportTask.getStatus() == GenericTask.Status.RUNNING) {
								return;
							}
							mFriendReportTask = new FriendReportTask(mUser
									.getId(), inputContent);
							mFriendReportTask
									.setListener(mFriendReportTaskListener);
							mFriendReportTask.execute();
						}
					}
				});
		builder.show();
	}

	private void startProgress() {
		progressDialog = Utils.showDialog(mActivity,
				mActivity.getString(R.string.please_waiting));

	}

	private void stopProgress() {
		Utils.dismissDialog(progressDialog);
	}

}