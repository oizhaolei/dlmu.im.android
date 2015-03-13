package com.ruptech.chinatalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ruptech.chinatalk.MainTabLayout.OnTabClickListener;
import com.ruptech.chinatalk.event.AnnouncementEvent;
import com.ruptech.chinatalk.event.BalanceChangeEvetnt;
import com.ruptech.chinatalk.event.FriendEvent;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.NewVersionFoundEvent;
import com.ruptech.chinatalk.event.OfflineEvent;
import com.ruptech.chinatalk.event.OnlineEvent;
import com.ruptech.chinatalk.event.PresentEvent;
import com.ruptech.chinatalk.event.QAEvent;
import com.ruptech.chinatalk.event.RefreshNewMarkEvent;
import com.ruptech.chinatalk.event.StoryEvent;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.BadgeCountTask;
import com.ruptech.chinatalk.task.impl.GuideTask;
import com.ruptech.chinatalk.ui.FindPasswordActivity;
import com.ruptech.chinatalk.ui.LoginGateActivity;
import com.ruptech.chinatalk.ui.LoginLoadingActivity;
import com.ruptech.chinatalk.ui.LoginSignupActivity;
import com.ruptech.chinatalk.ui.SplashActivity;
import com.ruptech.chinatalk.ui.fragment.ChatFragment;
import com.ruptech.chinatalk.ui.fragment.DiscoverFragment;
import com.ruptech.chinatalk.ui.fragment.MyselfFragment;
import com.ruptech.chinatalk.ui.fragment.PopularFragment;
import com.ruptech.chinatalk.ui.friend.FriendAddRecommendedActivity;
import com.ruptech.chinatalk.ui.friend.FriendListActivity;
import com.ruptech.chinatalk.ui.setting.AgreementActivity;
import com.ruptech.chinatalk.ui.setting.IntroduceActivity;
import com.ruptech.chinatalk.ui.story.PhotoAlbumActivity;
import com.ruptech.chinatalk.ui.user.SignupProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.squareup.otto.Subscribe;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity implements
        OnTabClickListener {

    public static int TAB_INDEX_POPULAR = -1;

    public static int TAB_INDEX_DISCOVER = -1;

    public static int TAB_INDEX_CHAT = -1;

    public static int TAB_INDEX_MYSELF = -1;

    public static MainActivity instance = null;

    private static String NOTIFICATION_MESSAGE_ID = "NOTIFICATION_MESSAGE_ID";

    private static String NOTIFICATION_NOTI_ID = "NOTIFICATION_NOTI_ID";

    private static String NOTIFICATION_USER_PHOTO = "NOTIFICATION_USER_PHOTO";

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    Timer periodTimer = null;

    private Handler mainHandler = new Handler();

    @InjectView(R.id.activity_main_tab)
    MainTabLayout mainTab;

    private CustomDialog dialog;

    private final BroadcastReceiver mHandleRefreshNewMarkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshNewMark();
        }
    };

    long back_pressed;

    protected final String TAG = Utils.CATEGORY
            + MainActivity.class.getSimpleName();

    private PopularFragment popularFragment;

    private DiscoverFragment discoverFragment;

    private ChatFragment chatFragment;

    private MyselfFragment myselfFragment;
    private Fragment currentFragment;

    public void changeTab(Fragment fragment) {
        refreshBadgeCount();
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.addToBackStack(fragment.getTag() + "stack_item");
            transaction.replace(R.id.main_tab_fragment, fragment);
        }
        transaction.commit();
    }

    private void checkPushService() {
//		if (!BaiduPushMessageReceiver.isRegistered()
//				&& !GCMRegistrar.isRegistered(this)) {
//			App.registePush(this);
//		} else {
//			if (BaiduPushMessageReceiver.isRegistered()
//					&& !BaiduPushMessageReceiver.isRegisteredOnServer()) {
//				ServerUtilities.registerBaiduPushOnServer(this);
//			} else if (GCMRegistrar.isRegistered(this)
//					&& !GCMRegistrar.isRegisteredOnServer(this)) {
//				ServerUtilities.registerGCMOnServer(this);
//			}
//		}
    }

    private void closeOthers() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                LoginLoadingActivity.close();
                SplashActivity.close();
                LoginGateActivity.close();
                LoginSignupActivity.close();
                FindPasswordActivity.close();
                IntroduceActivity.close();
                AgreementActivity.close();
                LoginSignupActivity.close();
                SignupProfileActivity.close();
            }
        }, 1000);
    }

    private void delayTask() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // recommended friends
                long recommendedUploadContactsLastUpdate = PrefUtils
                        .getPrefRecommendedFriendsLastUpdate();
                if (System.currentTimeMillis()
                        - recommendedUploadContactsLastUpdate > AppPreferences.RECOMMENDED_FRIENDS_INTERVAL) {
                    FriendAddRecommendedActivity.doRecommendedFriends();
                }
                // guide
                long guideLastUpdate = PrefUtils.getPrefGuideLastUpdate();
                if (PrefUtils.isThirdPartyShareInfoEmpty()
                        || System.currentTimeMillis() - guideLastUpdate > AppPreferences.RECOMMENDED_GUIDE_INTERVAL) {
                    doGuideTask();
                }

                // // 刷新热门图片
                // SubHotFragment.doRetrieveHotList(true, new TaskAdapter() {
                // @Override
                // public void onPostExecute(GenericTask task,
                // TaskResult result) {
                // RetrievePopularStoryTask hotListTask =
                // (RetrievePopularStoryTask) task;
                // if (result == TaskResult.OK) {
                // List<UserPhoto> hotList = hotListTask
                // .getPopularStoryList();
                // if (hotList != null && hotList.size() > 0) {
                // for (UserPhoto hotUserPhoto : hotList) {
                // App.hotUserPhotoDAO
                // .insertHotUserPhotoTable(hotUserPhoto);
                // }
                // CommonUtilities
                // .broadcastUserPhotoList(getApplicationContext());
                // }
                // }
                // }
                // });
                //
                // // 刷新频道
                // GenericTask mRetrieveChannelListTask = new
                // RetrieveChannelListTask(
                // true, AppPreferences.ID_IMPOSSIBLE,
                // AppPreferences.ID_IMPOSSIBLE);
                // mRetrieveChannelListTask.setListener(new TaskAdapter() {
                // @Override
                // public void onPostExecute(GenericTask task,
                // TaskResult result) {
                // RetrieveChannelListTask channelListTask =
                // (RetrieveChannelListTask) task;
                // if (result == TaskResult.OK) {
                // List<Channel> channelList = channelListTask
                // .getChannelList();
                // if (channelList != null && channelList.size() > 0) {
                // if (channelList.size() > 0) {
                // App.channelDAO.deleteAll();
                // for (Channel channel : channelList) {
                // App.channelDAO.insertChannel(channel);
                // }
                // CommonUtilities
                // .broadcastChannelList(getApplicationContext());
                // }
                // }
                // }
                // }
                // });
                // mRetrieveChannelListTask
                // .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, AppPreferences.POST_DELAYED_MILLIS);
    }

    private void doGuideTask() {
        GenericTask mGuideTask = new GuideTask();
        mGuideTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // Add Task to manager
    }

    void doPostStory() {
        Intent intent = new Intent(this, PhotoAlbumActivity.class);
        startActivity(intent);
    }

    public void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {// 结束程序进程之后,点击通知栏消息进入，需要保存 传参
            int notiId = extras.getInt(NOTIFICATION_NOTI_ID);
            long messageId = extras.getLong(NOTIFICATION_MESSAGE_ID);
            UserPhoto userPhoto = (UserPhoto) extras
                    .get(NOTIFICATION_USER_PHOTO);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("notiId", notiId);
            params.put("messageId", messageId);
            params.put("userPhoto", userPhoto);
            PrefUtils.writeNotificationExtras(params);
        } else {
            PrefUtils.removeNotificationExtras();
        }
    }

    public void gotoFriendList(View v) {
        Intent intent = new Intent(this, FriendListActivity.class);
        startActivity(intent);
    }

    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        if (discoverFragment != null && currentFragment == discoverFragment) {
            if (discoverFragment.backPressed())
                return;
        }

        if (back_pressed + 2000 > System.currentTimeMillis()) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.press_again_to_exit),
                    Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.mBus.register(this);

        if (!App.isAvailableShowMain()) {
            getExtras();
            gotoSplashActivity();
            finish();
            return;
        }
        instance = this;

        // requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        App.bindXMPPService();
        App.mBadgeCount.loadBadgeCount();
        setupComponents(savedInstanceState);
        delayTask();
        // 关闭之前所有进入Main的activity
        closeOthers();

        // 判断是否存在AccessToken
        if (App.qqTencent == null && App.facebookSession == null
//				&& App.googlePlusClient == null
                && App.sinaOauth2AccessToken == null
                && App.wechatAccessToken == null) {
            PrefUtils.removePrefThirdPartyAccess();
        }
        if (PrefUtils.isShowSystemFreeRechargePointInform(App.readUser()
                .getTel())) {
            showFreeRechargeBalanceInformDialog();
        }

        registerReceiver(mHandleRefreshNewMarkReceiver, new IntentFilter(
                CommonUtilities.REFERSH_NEW_MARK_ACTION));
    }

    @Override
    protected void onDestroy() {
        try {
            if (periodTimer != null) {
                periodTimer.cancel();
            }
            unregisterReceiver(mHandleRefreshNewMarkReceiver);
        } catch (Exception e) {
        }

        App.unbindXMPPService();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        long start = System.currentTimeMillis();
        if (!App.isVersionChecked() || App.readUser() == null
                || App.readServerAppInfo() == null) {
            gotoSplashActivity();
            finish();
            return;
        }
        refreshNewMark();

        Log.w(TAG, "onResume: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void onTabClick(int viewId) {

        switch (viewId) {
            case R.string.popular:
                if (popularFragment == null)
                    popularFragment = new PopularFragment();
                changeTab(popularFragment);
                break;
            case R.string.main_tab_discover:
                if (discoverFragment == null)
                    discoverFragment = new DiscoverFragment();
                changeTab(discoverFragment);
                break;
            case R.string.main_tab_post:
                doPostStory();
                break;
            case R.string.main_tab_chat:
                if (chatFragment == null)
                    chatFragment = new ChatFragment();
                changeTab(chatFragment);
                break;
            case R.string.main_tab_myself:
                if (myselfFragment == null)
                    myselfFragment = new MyselfFragment();
                changeTab(myselfFragment);
                break;

        }

    }

    @Override
    public void onTabDoubleClick(int viewId) {
        switch (viewId) {
            case R.string.popular:
                if (popularFragment == currentFragment) {
                    popularFragment.refreshCurrentTab();
                }
                break;
            case R.string.main_tab_chat:
                if (chatFragment == currentFragment) {
                    chatFragment.refreshCurrentTab();
                }
                break;
        }
    }

    private void refreshBadgeCount() {
        GenericTask badgeTask = new BadgeCountTask();
        badgeTask.setListener(new TaskAdapter() {

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                CommonUtilities.broadcastRefreshNewMark(MainActivity.this);
            }
        });
        badgeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshNewMark() {

        if (mainTab != null) {
            mainTab.setNewCountForTab(App.mBadgeCount.getPopularNewCount(),
                    MainActivity.TAB_INDEX_POPULAR);
            mainTab.setNewCountForTab(App.mBadgeCount.getChatNewCount(),
                    MainActivity.TAB_INDEX_CHAT);
            mainTab.setNewCountForTab(App.mBadgeCount.getMySelfNewCount(),
                    MainActivity.TAB_INDEX_MYSELF);
        }
    }

    private void setupComponents(Bundle savedInstanceState) {
        mainTab.setOnTabClickListener(this);
        mainTab.clickTab(0);
    }

    private void showFreeRechargeBalanceInformDialog() {
        dialog = new CustomDialog(instance)
                .setTitle(getString(R.string.tips))
                .setMessage(
		                getString(R.string.system_free_recharge_inform,
				                App.readServerAppInfo().signup_give_balance))
                .setPositiveButton(R.string.alert_dialog_ok,
		                new DialogInterface.OnClickListener() {
			                @Override
			                public void onClick(DialogInterface dialog,
			                                    int whichButton) {
			                }
		                });// 创建;
        dialog.show();
        PrefUtils.removeShowSystemFreeRechargePointInform(App.readUser()
                .getTel());
    }

    @Subscribe
    public void answerLogout(LogoutEvent event) {
        if (App.mService != null)
            App.mService.logout();
        App.removeUser();
        finish();
    }

    @Subscribe
    public void answerNewVersionFound(NewVersionFoundEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                Utils.doNotifyVersionUpdate(App.mContext);
            }
        });
    }

    @Subscribe
    public void refreshNewMark(RefreshNewMarkEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                refreshNewMark();
            }
        });
    }


    @Subscribe
    public void answerNewChatReceived(final NewChatEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                MediaPlayer.create(MainActivity.this, R.raw.office).start();
                App.mService.displayMessageNotification(event);
            }
        });
    }

    @Subscribe
    public void answerBalanceChange(BalanceChangeEvetnt event) {
        mainHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(App.mContext,
                        "answerBalanceChange",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe
    public void onlineChange(OnlineEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(App.mContext,
                        R.string.start_receiving_messages,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe
    public void offlineChange(OfflineEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(App.mContext,
                        R.string.stop_receiving_messages,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe
    public void answerQaReceived(final QAEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                App.mService.displayQaNotification(event);
            }
        });
    }

    @Subscribe
    public void answerAnnouncementReceived(final AnnouncementEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                App.mService.displayAnnouncementNotification(event);
            }
        });
    }

    @Subscribe
    public void answerFriendReceived(final FriendEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                App.mService.displayFrirendNotification(event);
            }
        });
    }

    @Subscribe
    public void answerPrsentReceived(final PresentEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                App.mService.displayPresentNotification(event);
            }
        });
    }

    @Subscribe
    public void answerStoryReceived(final StoryEvent event) {
        mainHandler.post(new Runnable() {
            public void run() {
                App.mService.displayStoryNotification(event);
            }
        });
    }
}