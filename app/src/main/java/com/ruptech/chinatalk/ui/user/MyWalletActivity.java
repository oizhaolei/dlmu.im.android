package com.ruptech.chinatalk.ui.user;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RechargFreeTask;
import com.ruptech.chinatalk.thirdparty.model.Share;
import com.ruptech.chinatalk.thirdparty.qq.QQBaseUIListener;
import com.ruptech.chinatalk.thirdparty.qq.QQUtil;
import com.ruptech.chinatalk.thirdparty.sina.SinaUtil;
import com.ruptech.chinatalk.thirdparty.sina.SinaWeiboShareResponseActivity;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.ui.setting.RechargeAlipayActivity;
import com.ruptech.chinatalk.ui.setting.RechargeRmbBankTransferActivity;
import com.ruptech.chinatalk.ui.setting.RechargeWeChatActivity;
import com.ruptech.chinatalk.ui.setting.RechargeWebpayActivity;
import com.ruptech.chinatalk.ui.setting.SettingBalanceActivity;
import com.ruptech.chinatalk.ui.setting.SettingFeeIntroduceActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.ThirdPartyUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.FreeChargeAdapter;
import com.ruptech.chinatalk.wxapi.WXEntryActivity;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.open.t.Weibo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MyWalletActivity extends ActionBarActivity implements
        OnItemClickListener {

    public class RechargeItem {
        public int imageId;
        public int titleId;

        public RechargeItem(int imageId, int titleId) {
            this.imageId = imageId;
            this.titleId = titleId;
        }
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        // callback when session changes state
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (session.isOpened()) {

                App.facebookSession = session;
                facebookShowFeedDialog();
            }
        }
    }

    // 腾讯微博Listener
    private class TQQApiListener extends QQBaseUIListener {
        private String mScope = "all";
        private Boolean mNeedReAuth = false;

        public TQQApiListener(String scope, boolean needReAuth,
                              Activity activity) {
            super(activity);
            this.mScope = scope;
            this.mNeedReAuth = needReAuth;
        }

        @Override
        public void onComplete(Object response) {
            final Activity activity = MyWalletActivity.this;
            try {
                JSONObject json = (JSONObject) response;
                int ret = json.getInt("ret");

                if (ret == 0 && json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    if (data.has("id")) {
                        String mLastAddTweetId = data.getString("id");
                        if (BuildConfig.DEBUG) {
                            Log.d("Tencent Weibo ID :", mLastAddTweetId);
                        }

                    }
                }
                if (ret == 0) {
                    Message msg = tencentHandler.obtainMessage(0, mScope);
                    Bundle data = new Bundle();
                    data.putString("response", response.toString());
                    msg.setData(data);
                    tencentHandler.sendMessage(msg);
                } else if (ret == 100030) {
                    if (mNeedReAuth) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                App.qqTencent.reAuth(activity, mScope,
                                        new QQBaseUIListener(
                                                MyWalletActivity.this));
                            }
                        };
                        MyWalletActivity.this.runOnUiThread(r);
                    }
                } else if (ret == 6) {
                    ThirdPartyUtil.toastMessage(MyWalletActivity.this,
                            "onComplete() JSONException: ");
                    Toast.makeText(MyWalletActivity.this,
                            R.string.unregistered_weibo, Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                ThirdPartyUtil.toastMessage(MyWalletActivity.this,
                        "onComplete() JSONException: " + response.toString());
                Toast.makeText(MyWalletActivity.this, R.string.share_failed,
                        Toast.LENGTH_SHORT).show();
                Utils.sendClientException(e);
            }
            ThirdPartyUtil.dismissDialog();
        }
    }

    @InjectView(R.id.recharge_share_memo)
    TextView shareMemoText;

    // Facebook
    private UiLifecycleHelper uiHelper;

    private Bundle savedInstanceState;

    // 当前按钮
    public int currentButton = 0;

    /**
     * 该按钮用于记录当前点击的是哪一个 Button，用于在 函数中进行区分。
     */

    public static final int NONE_BUTTON = 0;

    public static final int SINA_WEIBO_BUTTON = 1;

    public static final int QQ_BUTTON = 2;
    public static final int TQQ_BUTTON = 3;
    public static final int QZONE_BUTTON = 4;
    public static final int FACEBOOK_BUTTON = 5;
    public static final int WECHAT_BUTTON = 6;
    public static final int GOOGLE_PLUS_BUTTON = 7;
    public static MyWalletActivity instance;

    @InjectView(R.id.activity_myself_wallet_sns_grid)
    GridView snsGridView;
    @InjectView(R.id.activity_myself_wallet_recharge_grid)
    GridView rechargeGridView;

    // 新浪微博分享API
    IWeiboShareAPI mWeiboShareAPI;
    private final String TAG = Utils.CATEGORY
            + MyWalletActivity.class.getSimpleName();

    /**
     * 腾讯微博异步显示结果
     */
    private final Handler tencentHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Context context = MyWalletActivity.this;
            String scope = msg.obj.toString();
            String response = msg.getData().getString("response");
            if (BuildConfig.DEBUG) {
                Log.d("RechargeFreeActivity", "resposne = " + response
                        + " scope = " + scope);
            }

            if (response != null) {
                // 换行显示
                response = response.replace(",", "\r\n");
            }
            CustomDialog dialog = new CustomDialog(context)
                    .setMessage(response).setNegativeButton(
                            context.getString(R.string.got_it), null);
            if (scope.equals("get_info")) {
                dialog.setTitle(context.getString(R.string.userinfo));
            } else if (scope.equals("add_t")) {
                dialog.setTitle(context.getString(R.string.send_weibo));
            } else if (scope.equals("add_pic_t")) {
                dialog.setTitle(context.getString(R.string.send_picture_weibo));
            }
            dialog.show();
        }

        ;
    };

    // 免费充值Listener
    private final TaskListener mFreeRechargeListener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                RechargFreeTask rechargFreeTask = (RechargFreeTask) task;
                int rechargeResult = rechargFreeTask.getRechargeResult();
                if (rechargeResult == AppPreferences.FREE_RECHARGE_SUCEESS) {
                    // 免费充值成功，显示提示。
                    Toast.makeText(MyWalletActivity.this,
                            getString(R.string.recharge_process_finished),
                            Toast.LENGTH_SHORT).show();
                } else if (rechargeResult == AppPreferences.FREE_RECHARGE_INTERVAL_TIME) {
                    // 时间间隔不足，不符合免费充值条件，不显示提示。
                }
            } else {
                String msg = task.getMsg();
                Toast.makeText(MyWalletActivity.this, msg, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onPreExecute(GenericTask task) {
        }

    };

    // 支付方式
    public static final int ONLINE_PAY_TYPE_PAYPAL = 1;

    public static final int ONLINE_PAY_TYPE_INIPAY = 2;

    public static final String EXIST_ONLINE_PAY_TYPE = "ONLINE_PAY_TYPE";

    @InjectView(R.id.recharge_free_sns_share_layout)
    LinearLayout shareLayout;

    public void alipayRecharge(View v) {
        Intent intent = new Intent(this, RechargeAlipayActivity.class);
        startActivity(intent);
    }

    public void doRmbBankTransfer(View v) {
        Intent intent = new Intent(this, RechargeRmbBankTransferActivity.class);
        startActivity(intent);
    }

    public void doShareToFacebook(View v) {
        currentButton = FACEBOOK_BUTTON;

        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog

            facebookShareDialog();

        } else {
            // publish the post using the Feed Dialog

            facebookFeedDialog();
        }
    }

//	public void doShareToGooglePlus(View v) {
//		currentButton = GOOGLE_PLUS_BUTTON;
//		Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
//				.getLang());
//		if (share != null) {
//			String title = share.getThirdparty_share_title();
//			String text = share.getThirdparty_share_text();
//			// if (text.contains("http://")) {
//			// text = title;
//			// } else {
//			// text = title + "\n" + text;
//			// }
//			text = title + "\n" + text;
//			String url = share.getThirdparty_share_targeturl();
//			// Uri thumbnailUri = Uri.parse(share.getThirdparty_share_imgurl());
//
//			Intent shareIntent = new PlusShare.Builder(this)
//					.setType("text/plain")
//					.setText(text)
//					.setContentDeepLinkId(
//							GoogleParseDeepLinkActivity.RECHARGE_FREE)
//					.setContentUrl(Uri.parse(url))
//					// .setContentDeepLinkId(ParseDeepLinkActivity.RECHARGE_FREE,
//					// title, text, thumbnailUri)
//					// .setContentDeepLinkId(GoogleParseDeepLinkActivity.RECHARGE_FREE)
//					.getIntent();
//
//			startActivityForResult(shareIntent, 0);
//		}
//	}

    public void doShareToQQ(View v) {
        currentButton = QQ_BUTTON;

        final Bundle params = new Bundle();
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());
        if (share != null) {
            params.putString(QQShare.SHARE_TO_QQ_TITLE,
                    share.getThirdparty_share_title());
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,
                    share.getThirdparty_share_targeturl());
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY,
                    share.getThirdparty_share_text());
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,
                    share.getThirdparty_share_imgurl());
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME,
                    share.getThirdparty_share_appname());
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);
            qqSendMessage(params);
        }
    }

    public void doShareToQzone(View v) {
        currentButton = QZONE_BUTTON;
        final Bundle params = new Bundle();
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());

        if (share != null) {
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                    QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE,
                    share.getThirdparty_share_title());
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,
                    share.getThirdparty_share_text());
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,
                    share.getThirdparty_share_targeturl());
            // 支持传多个imageUrl
            ArrayList<String> imageUrls = new ArrayList<>();
            imageUrls.add(share.getThirdparty_share_imgurl());
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL,
                    imageUrls);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME,
                    share.getThirdparty_share_appname());
            qzoneSendMessage(params);
        }
    }

    public void doShareToSinaWeibo(View v) {
        currentButton = SINA_WEIBO_BUTTON;
        SinaWeiboShareResponseActivity.calledFrom = SinaWeiboShareResponseActivity.RECHARGE_FREE;

        // 创建微博 SDK 接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, SinaUtil.APP_KEY);

        // 获取微博客户端相关信息，如是否安装、支持 SDK 的版本
        boolean isInstalledWeibo = mWeiboShareAPI.isWeiboAppInstalled();
        // int supportApiLevel = mWeiboShareAPI.getWeiboAppSupportAPI();

        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!isInstalledWeibo) {
            mWeiboShareAPI
                    .registerWeiboDownloadListener(new IWeiboDownloadListener() {
                        @Override
                        public void onCancel() {
                            Toast.makeText(MyWalletActivity.this,
                                    R.string.cancel_download_weibo,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // 注册到新浪微博
        mWeiboShareAPI.registerApp();

        // // 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
        // // 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
        // // 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
        // // 失败返回 false，不调用上述回调
        // if (savedInstanceState != null) {
        // mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        // }

        try {
            // 检查微博客户端环境是否正常，如果未安装微博，弹出对话框询问用户下载微博客户端
            if (mWeiboShareAPI.checkEnvironment(true)) {
                sinaSendMessage();
            }
        } catch (WeiboShareException e) {
            Toast.makeText(MyWalletActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Utils.sendClientException(e);
        }
    }

    public void doShareToTencentWeibo(View v) {
        currentButton = TQQ_BUTTON;

        Tencent mTencent = App.qqTencent;
        if (App.qqTencent == null) {
            mTencent = Tencent.createInstance(QQUtil.appid,
                    MyWalletActivity.this);
            App.qqTencent = mTencent;
        }
        if (!App.qqTencent.isSessionValid()) {
            mTencent.login(this, "all", new QQBaseUIListener() {
                @Override
                public void doComplete(JSONObject values) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Share TO Tencent Weibo --> Text :",
                                values.toString());
                    }

                    tQQSentText();
                }
            });
        } else {
            tQQSentText();
        }
    }

    public void doShareToWeChat(View v) {
        currentButton = WECHAT_BUTTON;
        WXEntryActivity.calledFrom = WXEntryActivity.RECHARGE_FREE;

        // IWXAPI 是第三方app和微信通信的openapi接口
        IWXAPI api = WXAPIFactory.createWXAPI(this, WeChatUtil.APP_ID, false);
        // IWXAPI api = WXAPIFactory.createWXAPI(this, WeChatUtil.APP_ID, true);

        // 判断微信客户端是否安装
        boolean isInstalled = api.isWXAppInstalled();
        if (!isInstalled) {
            Toast.makeText(this, getString(R.string.wechat_is_not_installed),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断安装的版本是否支持微信开放平台
        boolean isSupportAPI = api.isWXAppSupportAPI();
        if (!isSupportAPI) {
            Toast.makeText(this,
                    getString(R.string.wechat_api_is_not_supported),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 判断微信是否支持朋友圈
        int wxSdkVersion = api.getWXAppSupportAPI();
        if (wxSdkVersion < WeChatUtil.TIMELINE_SUPPORTED_VERSION) {
            Toast.makeText(this,
                    getString(R.string.wechat_timeline_is_not_supported),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 将应用的appId注册到微信
        boolean register = api.registerApp(WeChatUtil.APP_ID);
        if (register) {

            // 发送请求到微信
            weChatSentWebpage(api);
        } else {
            Log.e("WeChat", "appId have not been registed. ");
        }
    }

    /**
     * publish the post using the Feed Dialog
     */
    private void facebookFeedDialog() {
        Arrays.asList("publish_actions");

        Session currentSession = App.facebookSession;

        if (currentSession == null) {
            currentSession = new Session(this);
            Session.setActiveSession(currentSession);
            SessionStatusCallback statusCallback = new SessionStatusCallback();
            Session.OpenRequest openRequest = new Session.OpenRequest(this);
            openRequest.setCallback(statusCallback);
            if (currentSession.getState().equals(
                    SessionState.CREATED_TOKEN_LOADED)) {
                currentSession.openForRead(new Session.OpenRequest(this)
                        .setCallback(statusCallback));
            }
            if (!currentSession.isOpened() && !currentSession.isClosed()) {
                currentSession.openForRead(openRequest);
            } else {
                Session.openActiveSession(this, true, statusCallback);
            }
            // SessionStatusCallback statusCallback = new
            // SessionStatusCallback();
            // Session.openActiveSession(this, true, statusCallback);

            // openRequest.setPermissions(PERMISSIONS);
            // currentSession.openForPublish(openRequest);
        } else if (!currentSession.isOpened()) {
            Session.setActiveSession(currentSession);
            // Session.OpenRequest openRequest = new Session.OpenRequest(this);
            // openRequest.setPermissions(PERMISSIONS);
            // openRequest
            // .setLoginBehavior(SessionLoginBehavior.SSO_WITH_FALLBACK);
            // currentSession.openForPublish(openRequest);
            Session.OpenRequest openRequest = new Session.OpenRequest(this);
            SessionStatusCallback statusCallback = new SessionStatusCallback();
            openRequest.setCallback(statusCallback);
            currentSession.openForRead(openRequest);

            // facebookShowFeedDialog();
        } else {
            Session.setActiveSession(currentSession);
            facebookShowFeedDialog();
        }
    }

    /**
     * Publish the post using the Share Dialog
     */
    private void facebookShareDialog() {
        if (uiHelper == null) {
            uiHelper = new UiLifecycleHelper(this, null);
            uiHelper.onCreate(savedInstanceState);
        }
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());

        if (share != null) {
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
                    this).setLink(share.getThirdparty_share_targeturl())
                    .setName(share.getThirdparty_share_title())
                    .setDescription(share.getThirdparty_share_text())
                    .setPicture(share.getThirdparty_share_imgurl()).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        }
    }

    private void facebookShowFeedDialog() {
        Bundle params = new Bundle();
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());

        if (share != null) {
            params.putString("name", share.getThirdparty_share_appname());
            params.putString("caption", share.getThirdparty_share_title());
            params.putString("description", share.getThirdparty_share_text());
            params.putString("link", share.getThirdparty_share_targeturl());
            params.putString("picture", share.getThirdparty_share_imgurl());

            WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
                    MyWalletActivity.this, Session.getActiveSession(), params))
                    .setOnCompleteListener(new OnCompleteListener() {

                        @Override
                        public void onComplete(Bundle values,
                                               FacebookException error) {
                            if (error == null) {
                                // When the story is posted, echo the success
                                // and the post Id.
                                final String postId = values
                                        .getString("post_id");
                                if (postId != null) {
                                    // Toast.makeText(MyWalletActivity.this,
                                    // "Posted story, id: " + postId,
                                    // Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MyWalletActivity.this,
                                            getString(R.string.share_success),
                                            Toast.LENGTH_SHORT).show();
                                    freeRecharge(AppPreferences.SHARE_TO_FACEBOOK);
                                    currentButton = NONE_BUTTON;
                                } else {
                                    // User clicked the Cancel button
                                    Toast.makeText(MyWalletActivity.this,
                                            getString(R.string.share_canceled),
                                            Toast.LENGTH_SHORT).show();
                                    currentButton = NONE_BUTTON;
                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                // User clicked the "x" button
                                Toast.makeText(MyWalletActivity.this,
                                        getString(R.string.share_canceled),
                                        Toast.LENGTH_SHORT).show();
                                currentButton = NONE_BUTTON;
                            } else {
                                // Generic, ex: network error
                                Toast.makeText(
                                        MyWalletActivity.this,
                                        getString(R.string.share_failed)
                                                + "Error Message: "
                                                + error.toString(),
                                        Toast.LENGTH_SHORT).show();
                                currentButton = NONE_BUTTON;
                            }
                        }

                    }).build();
            feedDialog.show();
        }
    }

    /*
     * 免费充值
     */
    public void freeRecharge(String type) {
        RechargFreeTask mRechargFreeTask = new RechargFreeTask(type);
        mRechargFreeTask.setListener(mFreeRechargeListener);
        mRechargFreeTask.execute();
    }

    @OnClick(R.id.activity_myself_wallet_consume_layout)
    public void gotoConsumeActivity(View v) {
        Intent intent = new Intent(this, SettingBalanceActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.activity_myself_wallet_fee_introduce_layout)
    public void gotoFeeIntroduceActivity(View v) {
        if (Utils.checkNetwork(this)) {
            Intent intent = new Intent(this, SettingFeeIntroduceActivity.class);
            startActivity(intent);
        }
    }

    public void inipayRecharge(View v) {
        Intent intent = new Intent(this, RechargeWebpayActivity.class);
        intent.putExtra(EXIST_ONLINE_PAY_TYPE, ONLINE_PAY_TYPE_INIPAY);
        startActivity(intent);
    }

    private void initComponents() {
        ServerAppInfo mAppInfo = App.readServerAppInfo();

        FreeChargeAdapter rechargeAdapter = new FreeChargeAdapter(this,
                R.layout.item_recharge);
        rechargeGridView.setAdapter(rechargeAdapter);
        rechargeAdapter.add(new RechargeItem(R.drawable.paypal,
                R.string.recharge_paypal_demo));
        rechargeAdapter.add(new RechargeItem(R.drawable.alipay_logo,
                R.string.recharge_rmb_pay_demo));
        // rechargeAdapter.add(new RechargeItem(R.drawable.wechatpay_logo,
        // R.string.recharge_rmb_pay_demo));
        rechargeAdapter.add(new RechargeItem(R.drawable.inipay,
                R.string.recharge_inipay_demo));
        rechargeAdapter.add(new RechargeItem(R.drawable.china_pay,
                R.string.recharge_no_alipay_account_card_pay));

        rechargeGridView.setOnItemClickListener(this);

        shareMemoText.setText(String.format(
                getString(R.string.recharge_free_share_memo),
                mAppInfo.thirdparty_share_interval_time,
                mAppInfo.share_to_micro_blog_point));

        // addFriendMemoText = (TextView)
        // findViewById(R.id.recharge_free_contact_add_memo);
        // addFriendMemoText.setText(String.format(
        // getString(R.string.recharge_free_contact_add_memo),
        // mAppInfo.friend_add_from_contact_give_balance));

        if (PrefUtils.isThirdPartyShareInfoEmpty()) {
            shareLayout.setVisibility(View.GONE);
        } else {
            FreeChargeAdapter snsAdapter = new FreeChargeAdapter(this,
                    R.layout.item_recharge);
            snsGridView.setAdapter(snsAdapter);

            snsAdapter.add(new RechargeItem(R.drawable.sina_weibo_40,
                    R.string.share_to_sina_weibo));
            snsAdapter.add(new RechargeItem(R.drawable.facebook_40,
                    R.string.share_to_facebook));
            if (ThirdPartyUtil
                    .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_WECHAT)) {
                snsAdapter.add(new RechargeItem(R.drawable.wechat_moments_40,
                        R.string.share_to_wechat));
            }
            if (ThirdPartyUtil
                    .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_GOOGLE_PLUS)) {
                snsAdapter.add(new RechargeItem(R.drawable.google_plus_40,
                        R.string.share_to_google));
            }

            snsAdapter.add(new RechargeItem(R.drawable.qzone_40,
                    R.string.share_to_qzone));

            int gridViewHeight = snsGridView.getLayoutParams().height;
            if (snsAdapter.getCount() % 4 <= 0) {
                gridViewHeight = gridViewHeight / 2;
            }
            snsGridView.setLayoutParams(new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, gridViewHeight));
            snsGridView.setOnItemClickListener(this);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (currentButton == FACEBOOK_BUTTON) {

            if (uiHelper != null) {
                uiHelper.onActivityResult(requestCode, resultCode, data,
                        new FacebookDialog.Callback() {
                            @Override
                            public void onComplete(
                                    FacebookDialog.PendingCall pendingCall,
                                    Bundle data) {

                                if (FacebookDialog
                                        .getNativeDialogDidComplete(data)) {
                                    String result = FacebookDialog
                                            .getNativeDialogCompletionGesture(data);
                                    if (FacebookDialog.COMPLETION_GESTURE_CANCEL
                                            .equals(result)) {
                                        // user cancel
                                        Toast.makeText(MyWalletActivity.this,
                                                R.string.share_canceled,
                                                Toast.LENGTH_SHORT).show();
                                        currentButton = NONE_BUTTON;
//  TODO                                  } else if (FacebookDialog.COMPLETION_GESTURE_POST
//                                            .equals(result)) {
//                                        // Facebook分享成功
//                                        Toast.makeText(MyWalletActivity.this,
//                                                R.string.share_success,
//                                                Toast.LENGTH_SHORT).show();
//                                        // 免费充值
//                                        freeRecharge(AppPreferences.SHARE_TO_FACEBOOK);
//                                        currentButton = NONE_BUTTON;
                                    } else {
                                        // user cancel
                                        Toast.makeText(MyWalletActivity.this,
                                                R.string.share_canceled,
                                                Toast.LENGTH_SHORT).show();
                                        currentButton = NONE_BUTTON;
                                    }
                                }

                            }

                            @Override
                            public void onError(
                                    FacebookDialog.PendingCall pendingCall,
                                    Exception error, Bundle data) {
                                Toast.makeText(
                                        MyWalletActivity.this,
                                        getString(R.string.share_failed)
                                                + " Error Message: "
                                                + error.toString(),
                                        Toast.LENGTH_SHORT).show();
                                currentButton = NONE_BUTTON;
                            }
                        });

            } else {
                Session.getActiveSession().onActivityResult(this, requestCode,
                        resultCode, data);
            }
        } else if (currentButton == GOOGLE_PLUS_BUTTON) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MyWalletActivity.this, R.string.share_success,
                        Toast.LENGTH_SHORT).show();

                // 免费充值
                freeRecharge(AppPreferences.SHARE_TO_GOOGLE_PLUS);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MyWalletActivity.this, R.string.share_canceled,
                        Toast.LENGTH_SHORT).show();
                currentButton = NONE_BUTTON;
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myself_wallet);
        ButterKnife.inject(this);
        instance = this;

        getSupportActionBar().setTitle(R.string.balance);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initComponents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentButton == FACEBOOK_BUTTON) {
            if (uiHelper != null) {
                uiHelper.onDestroy();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
                            int paramInt, long paramLong) {
        if (paramAdapterView == this.snsGridView) {
            switch (paramView.getId()) {
                case R.drawable.sina_weibo_40:
                    doShareToSinaWeibo(paramView);
                    break;
                case R.drawable.facebook_40:
                    doShareToFacebook(paramView);
                    break;
//			case R.drawable.google_plus_40:
//				doShareToGooglePlus(paramView);
//				break;
                case R.drawable.wechat_moments_40:
                    doShareToWeChat(paramView);
                    break;
                case R.drawable.qzone_40:
                    doShareToQzone(paramView);
                    break;
            }
        }

        if (paramAdapterView == this.rechargeGridView) {
            switch (paramView.getId()) {
                case R.drawable.paypal:
                    paypalRecharge(paramView);
                    break;
                case R.drawable.alipay_logo:
                    alipayRecharge(paramView);
                    break;
                // case R.drawable.wechatpay_logo:
                // tenpayRecharge(paramView);
                // break;
                case R.drawable.inipay:
                    inipayRecharge(paramView);
                    break;
                case R.drawable.china_pay:
                    doRmbBankTransfer(paramView);
                    break;

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentButton == FACEBOOK_BUTTON) {
            if (uiHelper != null) {
                uiHelper.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentButton == FACEBOOK_BUTTON) {
            if (uiHelper != null) {
                uiHelper.onResume();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentButton == FACEBOOK_BUTTON) {
            if (uiHelper != null) {
                uiHelper.onSaveInstanceState(outState);
            } else {
                Session session = Session.getActiveSession();
                Session.saveSession(session, outState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentButton == FACEBOOK_BUTTON && uiHelper == null) {
            SessionStatusCallback statusCallback = new SessionStatusCallback();
            Session.getActiveSession().addCallback(statusCallback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (currentButton == FACEBOOK_BUTTON && uiHelper == null) {
            SessionStatusCallback statusCallback = new SessionStatusCallback();
            Session.getActiveSession().removeCallback(statusCallback);
        }
    }

    public void paypalRecharge(View v) {
        Intent intent = new Intent(this, RechargeWebpayActivity.class);
        intent.putExtra(EXIST_ONLINE_PAY_TYPE, ONLINE_PAY_TYPE_PAYPAL);
        startActivity(intent);
    }

    /**
     * QQ分享，用异步方式启动分享
     *
     * @param params
     */
    private void qqSendMessage(final Bundle params) {
        final Activity activity = MyWalletActivity.this;
        Tencent mTencent = App.qqTencent;
        if (App.qqTencent == null) {
            mTencent = Tencent.createInstance(QQUtil.appid,
                    MyWalletActivity.this);
            App.qqTencent = mTencent;
        }
        final QQShare mQQShare = new QQShare(MyWalletActivity.this,
                App.qqTencent.getQQToken());

        mQQShare.shareToQQ(activity, params, new IUiListener() {

            @Override
            public void onCancel() {
                // 用户取消分享操作
                currentButton = NONE_BUTTON;
            }

            @Override
            public void onComplete(Object response) {
                // 免费充值
                freeRecharge(AppPreferences.SHARE_TO_QQ);

                currentButton = NONE_BUTTON;
            }

            @Override
            public void onError(UiError e) {
                currentButton = NONE_BUTTON;
            }
        });

    }

    /**
     * QQ空间，用异步方式启动分享
     *
     * @param params
     */
    private void qzoneSendMessage(final Bundle params) {
        final Activity activity = MyWalletActivity.this;
        final Tencent tencent = Tencent.createInstance(QQUtil.appid,
                MyWalletActivity.this);

        tencent.shareToQzone(activity, params, new IUiListener() {

            @Override
            public void onCancel() {
                currentButton = NONE_BUTTON;
            }

            @Override
            public void onComplete(Object response) {
                // QQ空间分享成功
                Toast.makeText(MyWalletActivity.this, R.string.share_success,
                        Toast.LENGTH_SHORT).show();
                // 免费充值
                freeRecharge(AppPreferences.SHARE_TO_QZONE);
                currentButton = NONE_BUTTON;
            }

            @Override
            public void onError(UiError e) {
                ThirdPartyUtil.toastMessage(activity, "onError: "
                        + e.errorMessage, "e");
                Toast.makeText(MyWalletActivity.this, R.string.share_failed,
                        Toast.LENGTH_SHORT).show();
                currentButton = NONE_BUTTON;
            }

        });

    }

    /**
     * 新浪微博，创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject sinaGetImageObj() {
        ImageObject imageObject = new ImageObject();

        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        Bitmap bitmapDrawable = ((BitmapDrawable) drawable).getBitmap();

        // Bitmap bitmapDrawable = getQRBitMap();

        imageObject.setImageObject(bitmapDrawable);
        return imageObject;
    }

    /**
     * 新浪微博，创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject sinaGetTextObj() {
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());
        TextObject textObject = new TextObject();
        if (share != null) {
            String format = getString(R.string.thirdparty_share_webpage_template);
            String text = String.format(format,
                    share.getThirdparty_share_text());
            textObject.text = text + " @"
                    + share.getThirdparty_share_sina_officalaccount();
        }
        return textObject;
    }

    /**
     * 新浪微博，创建多媒体（网页）消息对象。
     *
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject sinaGetWebpageObj() {
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());
        WebpageObject mediaObject = new WebpageObject();

        if (share != null) {
            mediaObject.identify = Utility.generateGUID();
            mediaObject.title = share.getThirdparty_share_title();
            mediaObject.description = share.getThirdparty_share_text();

            // 设置 Bitmap 类型的图片到网页对象里
            Drawable drawable = getResources().getDrawable(
                    R.drawable.ic_launcher);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            mediaObject.setThumbImage(bitmap);
            mediaObject.actionUrl = share.getThirdparty_share_targeturl();
            // mediaObject.defaultText = "";
        }
        return mediaObject;
    }

    /**
     * 新浪微博，第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sinaSendMessage() {

        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */) {
                sinaSendMultiMessage();
            } else {
                sinaSendSingleMessage();
            }
        } else {
            Toast.makeText(this, R.string.weibosdk_not_support_api_hint,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 新浪微博，第三方应用发送请求消息到微博，唤起微博分享界面。 注意：当
     * {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     */
    private void sinaSendMultiMessage() {

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        weiboMessage.textObject = sinaGetTextObj();
        weiboMessage.imageObject = sinaGetImageObj();
        weiboMessage.mediaObject = sinaGetWebpageObj();

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(request);
    }

    /**
     * 新浪微博，第三方应用发送请求消息到微博，唤起微博分享界面。 当
     * {@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     */
    private void sinaSendSingleMessage() {

        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.mediaObject = sinaGetTextObj();
        weiboMessage.mediaObject = sinaGetImageObj();
        weiboMessage.mediaObject = sinaGetWebpageObj();

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(request);
    }

    public void tenpayRecharge(View v) {
        Intent intent = new Intent(this, RechargeWeChatActivity.class);
        // intent.putExtra(EXIST_ONLINE_PAY_TYPE,
        // ONLINE_PAY_TYPE_TENPAY);
        startActivity(intent);
    }

    /**
     * 腾讯微博发送纯文本微博
     */
    private void tQQSentText() {
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());

        if (share != null) {
            Weibo mWeibo = new Weibo(this, App.qqTencent.getQQToken());
            if (QQUtil.qqIsReady(MyWalletActivity.this, App.qqTencent)) {
                String content = share.getThirdparty_share_text();
                mWeibo.sendText(content, new TQQApiListener("add_t", false,
                        MyWalletActivity.this));
                ThirdPartyUtil.showProgressDialog(MyWalletActivity.this, null,
                        null);
            }
        }
    }

    /**
     * 微信分享网页到朋友圈
     *
     * @param api 微信API接口
     */
    private void weChatSentWebpage(IWXAPI api) {
        Share share = ThirdPartyUtil.getThirdPartyShare(App.readUser()
                .getLang());

        if (share != null) {
            // 初始化一个WXWebpageObject对象
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = share.getThirdparty_share_targeturl();
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = share.getThirdparty_share_title();
            msg.description = share.getThirdparty_share_text();
            Bitmap thumb = ((BitmapDrawable) getResources().getDrawable(
                    R.drawable.ic_launcher)).getBitmap();
            msg.thumbData = ThirdPartyUtil.bmpToByteArray(thumb, false);

            // 构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = "webpage" + System.currentTimeMillis();
            req.message = msg;

            req.scene = SendMessageToWX.Req.WXSceneTimeline;

            // 用api接口发送数据到微信
            api.sendReq(req);
        }
    }
}
