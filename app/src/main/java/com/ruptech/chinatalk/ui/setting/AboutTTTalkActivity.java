package com.ruptech.chinatalk.ui.setting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BaiduPushMessageReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.DownloadApk;
import com.ruptech.chinatalk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AboutTTTalkActivity extends ActionBarActivity {

    @InjectView(R.id.main_tab_setting_version_version)
    TextView text_version;
    @InjectView(R.id.main_tab_setting_version_new_icon)
    TextView new_icon;
    @InjectView(R.id.main_tab_setting_help_title_textview)
    TextView helpTitleTextView;

    private boolean isShowKoreanCall;

    private boolean isShowChinaQQ;

    private DownloadApk downloadApk;

    private ProgressDialog progressDialog;

    @InjectView(R.id.main_tab_setting_help_tel_layout)
    View helpTelView;

    private void cancelCheckVersionTask() {
        if (downloadApk != null) {
            downloadApk.cancelVersionCheckTask();
        }
    }

    // 版本检测
    @OnClick(R.id.main_tab_setting_check_version_layout)
    public void checkVersion(View v) {
        // version check
        downloadApk = new DownloadApk(AboutTTTalkActivity.this);
        downloadApk.doVersionCheck(new TaskAdapter() {

            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                if (result == TaskResult.OK) {
                    onVersionCheckSuccess();
                } else {
                    onVersionCheckFailure();
                }
            }

            @Override
            public void onPreExecute(GenericTask task) {
                onVersionCheckBegin();
            }

            protected void onVersionCheckBegin() {
                progressDialog = Utils.showDialog(AboutTTTalkActivity.this,
                        getString(R.string.version_checking));
            }

            protected void onVersionCheckFailure() {
                Utils.dismissDialog(progressDialog);
            }

            protected void onVersionCheckSuccess() {
                Utils.dismissDialog(progressDialog);
                showNewVersionMark();
                downloadApk.checkApkUpdate(false);
            }
        });
    }

    // 系统url
    @OnClick(R.id.main_tab_setting_show_system_url_textview)
    public void doShowSystemUrl(View v) {
        StringBuffer msg = new StringBuffer(64);
//        msg.append(GCMRegistrar.isRegistered(this) ? '1' : '0');
//        msg.append(GCMRegistrar.isRegisteredOnServer(this) ? '1' : '0');
        msg.append(BaiduPushMessageReceiver.isRegistered() ? '1' : '0');
        msg.append(BaiduPushMessageReceiver.isRegisteredOnServer() ? '1' : '0');
        Toast.makeText(this, App.readServerAppInfo().getAppServerUrl() + msg,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        cancelCheckVersionTask();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);
        ButterKnife.inject(this);
        getSupportActionBar().setTitle(R.string.about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupComponents();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    // 服务条款
    @OnClick(R.id.main_tab_setting_privacy_policy_layout)
    public void setting_agreement_privacy_policy(View v) {
        if (Utils.checkNetwork(this)) {
            Intent intent = new Intent(this, AgreementActivity.class);
            intent.putExtra(AgreementActivity.EXTRA_AGREEMENT_KEY,
                    AgreementActivity.AGREEMENT_PRIVACY_POLICY);
            startActivity(intent);
        }
    }

    // 隐私政策
    @OnClick(R.id.main_tab_setting_agreement_terms_layout)
    public void setting_agreement_terms_and_conditions(View v) {
        if (Utils.checkNetwork(this)) {
            Intent intent = new Intent(this, AgreementActivity.class);
            intent.putExtra(AgreementActivity.EXTRA_AGREEMENT_KEY,
                    AgreementActivity.AGREEMENT_TERMS_AND_CONDITIONS);
            startActivity(intent);
        }
    }

    // 公告
    @OnClick(R.id.main_tab_setting_announcement_layout)
    public void setting_announcement(View v) {
        Intent intent = new Intent(this, SettingAnnouncementActivity.class);
        startActivity(intent);
    }

    // 应用介绍
    @OnClick(R.id.main_tab_setting_introduce_layout)
    public void setting_introduce(View v) {
        if (Utils.checkNetwork(this)) {
            Intent intent = new Intent(this, IntroduceActivity.class);
            startActivity(intent);
        }
    }

    // 客服QQ
    @OnClick(R.id.main_tab_setting_help_tel_layout)
    public void setting_online_call(View v) {
        if (isShowKoreanCall) {
            String phone = getString(R.string.korean_call);
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
                    + phone));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (isShowChinaQQ) {
            try {
                String url = "mqqwpa://im/chat?chat_type=wpa&uin="
                        + getString(R.string.korean_call) + "&version=1";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
            }
        }
    }

    // 在线帮助
    @OnClick(R.id.main_tab_setting_help_online_layout)
    public void setting_online_help(View v) {
        if (Utils.checkNetwork(this)) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_URL, getResources()
                    .getString(R.string.online_help_url));
            intent.putExtra(WebViewActivity.EXTRA_WEBVIEW_TITLE, getResources()
                    .getString(R.string.help));
            startActivity(intent);
        }
    }

    // 好评
    @OnClick(R.id.main_tab_setting_rate_layout)
    public void setting_rate(View v) {
        if (Utils.checkNetwork(this)) {
            String downloadUrl = "market://details?id=com.ruptech.chinatalk";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
            startActivity(i);
        }
    }

    private void setupComponents() {
        text_version.setText(App.mApkVersionOfClient.verName);

        showNewVersionMark();

        // 系统为韩文(或中文），并且用户语言与系统为同种语言时候，显示资讯电话菜单
        List<String> conditionLanguageList = new ArrayList<String>();
        conditionLanguageList.add(AppPreferences.LANGUAGE_KR);
        conditionLanguageList.add(AppPreferences.LANGUAGE_CN);

        boolean visible = false;
        for (int i = 0; i < conditionLanguageList.size(); i++) {
            List<String> conditionLanguages = Arrays
                    .asList(conditionLanguageList.get(i).split(","));
            if (conditionLanguages.contains(getString(
                    R.string.LANGUAGE_ENVIRONMENT).toUpperCase())
                    && conditionLanguages.contains(Utils.getUserLanguage())) {
                visible = true;
                if (i == 0) {// 是韩语和韩文系统
                    isShowKoreanCall = true;
                } else if (i == 1) {// 是中文和中文系统
                    isShowChinaQQ = true;
                    helpTitleTextView
                            .setText(getString(R.string.customer_services_qq));
                }
                break;
            }
        }
        if (visible) {
            helpTelView.setVisibility(View.VISIBLE);
        } else {
            helpTelView.setVisibility(View.GONE);
        }

    }

    private void showNewVersionMark() {
        if (App.readServerAppInfo() != null
                && App.readServerAppInfo().verCode > App.mApkVersionOfClient.verCode) {
            new_icon.setVisibility(View.VISIBLE);
            new_icon.setText(App.readServerAppInfo().verName);
            App.mBadgeCount.versionCount = 1;
        } else {
            new_icon.setVisibility(View.GONE);
            App.mBadgeCount.versionCount = 0;
        }

        CommonUtilities.broadcastRefreshNewMark(this);
    }
}