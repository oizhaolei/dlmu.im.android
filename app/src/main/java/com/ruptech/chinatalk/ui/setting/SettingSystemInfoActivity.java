package com.ruptech.chinatalk.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.thirdparty.sina.SinaAccessTokenKeeper;
import com.ruptech.chinatalk.ui.friend.FriendsBlockedListActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.FileHelper;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;
import com.tencent.tauth.Tencent;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingSystemInfoActivity extends ActionBarActivity {
    private class SinaLogOutRequestListener implements RequestListener {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");

                    if ("true".equalsIgnoreCase(value)) {
                        SinaAccessTokenKeeper
                                .clear(SettingSystemInfoActivity.this);
                    }
                } catch (JSONException e) {
                    Utils.sendClientException(e);
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Log.d("SINA LOGOUT EXCEPTION :", e.getMessage());
        }
    }

    private final String TAG = Utils.CATEGORY
            + SettingSystemInfoActivity.class.getSimpleName();

    @InjectView(R.id.main_tab_setting_cache_title_textview)
    TextView text_cache;

    private static SettingSystemInfoActivity instance;

    private static ImageView newMarkImgView;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    public static void showNewMark() {
        if (App.readServerAppInfo() != null
                && App.readServerAppInfo().verCode > App.mApkVersionOfClient.verCode) {
            newMarkImgView.setVisibility(View.VISIBLE);
        } else {
            newMarkImgView.setVisibility(View.GONE);
        }
    }

    // 翻译者一览
    @OnClick(R.id.activity_setting_apply_translator_rl)
    public void applyTranslator(View v) {
        Intent intent = new Intent(this, TranslatorRankListActivity.class);
        startActivity(intent);
    }

    // 退出
    public void doSystemLogout(MenuItem item) {
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (Utils.isThirdPartyLogin()) {
                    try {
                        if (AppPreferences.THIRD_PARTY_TYPE_QQ.equals(PrefUtils
                                .getPrefThirdPartyType())) {
                            Tencent mTencent = App.qqTencent;
                            mTencent.logout(SettingSystemInfoActivity.this);
                        } else if (AppPreferences.THIRD_PARTY_TYPE_FACEBOOK
                                .equals(PrefUtils.getPrefThirdPartyType())) {
                            Session session = App.facebookSession;
                            session.closeAndClearTokenInformation();
//						} else if (AppPreferences.THIRD_PARTY_TYPE_GOOGLE
//								.equals(PrefUtils.getPrefThirdPartyType())) {
//							PlusClient mPlusClient = App.googlePlusClient;
//							if (mPlusClient.isConnected()) {
//								mPlusClient.clearDefaultAccount();
//								mPlusClient.disconnect();
//							}
                        } else if (AppPreferences.THIRD_PARTY_TYPE_SINA
                                .equals(PrefUtils.getPrefThirdPartyType())) {
                            SinaLogOutRequestListener mLogoutListener = new SinaLogOutRequestListener();
                            new LogoutAPI(
                                    SinaAccessTokenKeeper
                                            .readAccessToken(SettingSystemInfoActivity.this))
                                    .logout(mLogoutListener);
                        }
                    } catch (Exception e) {
                        Utils.sendClientException(e);
                    }
                    PrefUtils.removePrefThirdPartyAccess();
                }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_setting_system_info);
        ButterKnife.inject(this);
        getSupportActionBar().setTitle(R.string.setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupComponents();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting_actions, mMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        showNewMark();
    }

    // 关于
    @OnClick(R.id.activity_setting_about_rl)
    public void setting_about(View v) {
        Intent intent = new Intent(this, AboutTTTalkActivity.class);
        startActivity(intent);
    }

    // 清空缓存
    @OnClick(R.id.activity_setting_cleanup_rl)
    public void setting_cleanup(View v) {
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                FileHelper.cleanApplicationData(SettingSystemInfoActivity.this);
                text_cache
                        .setText(FileHelper
                                .getApplicationDataSize(SettingSystemInfoActivity.this));
                Toast.makeText(SettingSystemInfoActivity.this,
                        R.string.cleanup_cache_msg, Toast.LENGTH_SHORT).show();
            }
        };
        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        };

        Utils.AlertDialog(this, positiveListener, negativeListener,
                this.getString(R.string.tips),
                this.getString(R.string.clean_cache_tips));

    }

    // 翻译设定
    @OnClick(R.id.activity_setting_general_rl)
    public void setting_general(View v) {
        Intent intent = new Intent(this, SettingGeneralActivity.class);
        startActivity(intent);
    }

    // 问答
    @OnClick(R.id.activity_setting_qa_rl)
    public void setting_qa(View v) {
        Intent intent = new Intent(this, SettingQaActivity.class);
        startActivity(intent);

    }

    // 翻译校验
    @OnClick(R.id.activity_setting_verify_rl)
    public void setting_verify(View v) {
        Intent intent = new Intent(this, SettingVerifyActivity.class);
        startActivity(intent);
    }

    private void setupComponents() {
        text_cache.setText(FileHelper.getApplicationDataSize(this));
        newMarkImgView = (ImageView) findViewById(R.id.setting_about_submenu_new_mark);
    }

    @OnClick(R.id.activity_setting_view_blocklist_rl)
    public void viewBlockFriendList(View v) {
        Intent intent = new Intent(this, FriendsBlockedListActivity.class);
        startActivity(intent);
    }
}