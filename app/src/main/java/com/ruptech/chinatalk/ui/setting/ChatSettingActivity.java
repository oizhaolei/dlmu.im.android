package com.ruptech.chinatalk.ui.setting;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FriendWalletPriorityTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.FriendOperate;
import com.ruptech.chinatalk.ui.FriendOperate.UserType;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class ChatSettingActivity extends ActionBarActivity {
	private final String TAG = Utils.CATEGORY
			+ ChatSettingActivity.class.getSimpleName();
	private static ChatSettingActivity instance = null;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	@InjectView(R.id.activity_chat_setting_share_wallet_slipswitch)
	ToggleButton shareWalletSlipswitch;
	@InjectView(R.id.activity_chat_setting_share_is_on_top_slipswitch)
	ToggleButton isTopslipswitch;

	private User mFriendUser;

	private FriendOperate friendOperate;

	boolean isMyFriend = false;

	private final TaskListener mFriendWalletPriorityTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
			} else {
				Toast.makeText(ChatSettingActivity.this, task.getMsg(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	@InjectView(R.id.activity_chat_setting_share_wallet_textview)
	TextView shareWalletTextView;

	@InjectView(R.id.activity_chat_setting_share_wallet_memo_textview)
	View walletMemoView;

	@OnClick(R.id.activity_chat_setting_block_layout)
	public void chatBlockHistory(View v) {
		friendOperate.settingBlockFriend();
	}

	@OnClick(R.id.activity_chat_setting_clean_history_layout)
	public void chatCleanHistory(View v) {
		friendOperate.settingCleanMessage();
	}

	@OnClick(R.id.activity_chat_setting_get_history_layout)
	public void chatGetHistory(View v) {
		friendOperate.settingGetHistoryFriend(mFriendUser.getId(),
				App.messageDAO.getMinMessageId(mFriendUser.getId()));
	}

	@OnClick(R.id.activity_chat_setting_report_layout)
	public void chatReportHistory(View v) {
		friendOperate.settingReportFriend();
	}

	private void doSaveChatSetting(int wallet_priority) {
		FriendWalletPriorityTask mFriendWalletPriorityTask = new FriendWalletPriorityTask(
				mFriendUser.getId(), wallet_priority);
		mFriendWalletPriorityTask
				.setListener(mFriendWalletPriorityTaskListener);
		mFriendWalletPriorityTask.execute();
	}

	private void doSaveChatTopSetting(int isTop, boolean isMyFriend) {
		if (isMyFriend) {
			App.friendDAO.updateFriendIsTop(App.readUser().getId(),
					mFriendUser.getId(), isTop);
		} else {
			App.friendDAO.updateFriendIsTop(mFriendUser.getId(), App.readUser()
					.getId(), isTop);
		}
	}

	private User getUserFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ChatActivity.EXTRA_FRIEND);
			return user;
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_setting);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.chat_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		instance = this;
		mFriendUser = getUserFromExtras();
		friendOperate = new FriendOperate(this, mFriendUser, UserType.FRIEND);
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}
	private void setupComponents() {
		String friendName = Utils.getFriendName(mFriendUser.getId(),
				mFriendUser.getFullname());
		shareWalletTextView.setText(getString(R.string.share_wallet_for,
				friendName));
		Friend friend = App.friendDAO.fetchFriend(App.readUser().getId(),
				mFriendUser.getId());
		shareWalletSlipswitch.setChecked(friend != null
				&& friend.getWallet_priority() != 0);
		shareWalletSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							doSaveChatSetting(1);
							walletMemoView.setVisibility(View.GONE);
						} else {
							doSaveChatSetting(0);
							walletMemoView.setVisibility(View.VISIBLE);
						}
					}
				});

		if (friend == null) {
			isMyFriend = false;
			shareWalletSlipswitch.setVisibility(View.GONE);
			walletMemoView.setVisibility(View.GONE);
			friend = App.friendDAO.fetchFriend(mFriendUser.getId(), App
					.readUser().getId());
		} else {
			isMyFriend = true;
			if (App.readUser().getBalance() > AppPreferences.MINI_BALANCE) {
				shareWalletSlipswitch.setVisibility(View.VISIBLE);
				if (friend.getWallet_priority() != 0) {
					walletMemoView.setVisibility(View.GONE);
				} else {
					walletMemoView.setVisibility(View.VISIBLE);
				}
			} else {
				shareWalletSlipswitch.setVisibility(View.GONE);
			}
		}

		isTopslipswitch.setChecked(friend != null && friend.getIs_top() == 1);
		isTopslipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							doSaveChatTopSetting(1, isMyFriend);
						} else {
							doSaveChatTopSetting(0, isMyFriend);
						}
					}
				});
	}
}
