package com.ruptech.chinatalk;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.ruptech.chinatalk.MainTabLayout.OnTabClickListener;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.event.NewChatEvent;
import com.ruptech.chinatalk.event.OfflineEvent;
import com.ruptech.chinatalk.event.OnlineEvent;
import com.ruptech.chinatalk.ui.LoginActivity;
import com.ruptech.chinatalk.ui.LoginGateActivity;
import com.ruptech.chinatalk.ui.LoginLoadingActivity;
import com.ruptech.chinatalk.ui.OrgActivity;
import com.ruptech.chinatalk.ui.SplashActivity;
import com.ruptech.chinatalk.ui.fragment.ChatFragment;
import com.ruptech.chinatalk.ui.fragment.MyselfFragment;
import com.ruptech.chinatalk.ui.fragment.ServiceFragment;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.dlmu.im.R;
import com.squareup.otto.Subscribe;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity implements
        OnTabClickListener {

    public static int TAB_INDEX_DISCOVER = -1;

    public static int TAB_INDEX_CHAT = -1;

    public static int TAB_INDEX_MYSELF = -1;

    public static MainActivity instance = null;
    protected final String TAG = Utils.CATEGORY
            + MainActivity.class.getSimpleName();
    @InjectView(R.id.activity_main_tab)
    MainTabLayout mainTab;
    long back_pressed;
    private Handler mainHandler = new Handler();
    private CustomDialog dialog;
    private ServiceFragment serviceFragment;
    private ChatFragment chatFragment;
    private MyselfFragment myselfFragment;
    private Fragment currentFragment;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    public void changeTab(Fragment fragment) {
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


    private void closeOthers() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                LoginLoadingActivity.close();
                SplashActivity.close();
                LoginGateActivity.close();
                LoginActivity.close();
            }
        }, 1000);
    }


    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

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
            gotoSplashActivity();
            finish();
            return;
        }
        instance = this;

        // requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        App.bindXMPPService(this);
        setupComponents(savedInstanceState);
        // 关闭之前所有进入Main的activity
        closeOthers();


    }

    @Override
    protected void onDestroy() {

        App.unbindXMPPService(this);
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
        if (App.readUser() == null) {
            gotoSplashActivity();
            finish();
            return;
        }

        if (Utils.isExistNewVersion()) {
            mainHandler.post(new Runnable() {
                public void run() {
                    Utils.doNotifyNewVersionFound(App.mContext, false);
                }
            });
        }
        Log.w(TAG, "onResume: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void onTabClick(int viewId) {

        switch (viewId) {
            case R.string.main_tab_service:
                if (serviceFragment == null)
                    serviceFragment = new ServiceFragment();
                changeTab(serviceFragment);
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
    }

    private void setupComponents(Bundle savedInstanceState) {
        mainTab.setOnTabClickListener(this);
        mainTab.clickTab(0);
    }


    @Subscribe
    public void answerLogout(LogoutEvent event) {
        if (App.mService != null)
            App.mService.logout();
        App.removeUser();
        finish();
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
    public void onlineChange(OnlineEvent event) {
//        mainHandler.post(new Runnable() {
//            public void run() {
//                Toast.makeText(App.mContext,
//                        R.string.start_receiving_messages,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Subscribe
    public void offlineChange(OfflineEvent event) {
//        mainHandler.post(new Runnable() {
//            public void run() {
//                Toast.makeText(App.mContext,
//                        R.string.stop_receiving_messages,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    public void doChatStudent(MenuItem item) {
        Intent orgIntent = new Intent(this, OrgActivity.class);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_JID, "100000@" + AppPreferences.IM_SERVER_RESOURCE);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_NAME, getString(R.string.dlmu_title));
        startActivity(orgIntent);
    }

    public void doChatTeacher(MenuItem item) {

        Intent orgIntent = new Intent(this, OrgActivity.class);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_JID, "100000@" + AppPreferences.IM_SERVER_RESOURCE);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_NAME, getString(R.string.dlmu_title));
        orgIntent.putExtra(OrgActivity.PARENT_ORG_STUDENT, false);
        startActivity(orgIntent);
    }

    // 退出
    public void doSystemLogout(MenuItem item) {
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                Utils.doLogout(App.mContext);
            }
        };
        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        };
        Utils.AlertDialog(this, positiveListener, negativeListener,
                this.getString(R.string.logout),
                this.getString(R.string.tip_logout));
    }

}