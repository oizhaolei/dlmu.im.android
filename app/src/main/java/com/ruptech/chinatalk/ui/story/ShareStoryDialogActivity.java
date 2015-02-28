package com.ruptech.chinatalk.ui.story;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
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
import com.ruptech.chinatalk.thirdparty.qq.QQBaseUIListener;
import com.ruptech.chinatalk.thirdparty.qq.QQUtil;
import com.ruptech.chinatalk.thirdparty.sina.SinaUtil;
import com.ruptech.chinatalk.thirdparty.sina.SinaWeiboShareResponseActivity;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.ThirdPartyUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ShareStoreAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ShareStoryDialogActivity extends Activity {
    // OO空间Listener
    private class QZoneApiListener extends QQBaseUIListener {

        public QZoneApiListener(Activity activity) {
            super(activity);
        }

        @Override
        public void doCancel() {
            ThirdPartyUtil.toastMessage(ShareStoryDialogActivity.this,
                    getString(R.string.share_canceled));
            currentButton = NONE_BUTTON;
            ShareStoryDialogActivity.close();
        }

        @Override
        public void doComplete(JSONObject values) {
            // QQ空间分享成功
            ThirdPartyUtil.toastMessage(ShareStoryDialogActivity.this,
                    getString(R.string.share_success));
            currentButton = NONE_BUTTON;
            ShareStoryDialogActivity.close();
        }

        @Override
        public void doError(UiError e) {
            ThirdPartyUtil.toastMessage(ShareStoryDialogActivity.this,
                    getString(R.string.share_failed));
            currentButton = NONE_BUTTON;
            ShareStoryDialogActivity.close();
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
            final Activity activity = ShareStoryDialogActivity.this;
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
                    Toast.makeText(ShareStoryDialogActivity.this,
                            getString(R.string.share_success),
                            Toast.LENGTH_SHORT).show();
                } else if (ret == 100030) {
                    if (mNeedReAuth) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                App.qqTencent.reAuth(activity, mScope,
                                        new QQBaseUIListener(
                                                ShareStoryDialogActivity.this));
                            }
                        };
                        ShareStoryDialogActivity.this.runOnUiThread(r);
                    }
                } else if (ret == 6) {
                    ThirdPartyUtil.toastMessage(ShareStoryDialogActivity.this,
                            "onComplete() JSONException: ");
                    Toast.makeText(ShareStoryDialogActivity.this,
                            R.string.unregistered_weibo, Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                Utils.sendClientException(e);
                ThirdPartyUtil.toastMessage(ShareStoryDialogActivity.this,
                        "onComplete() JSONException: " + response.toString());
            }

            ThirdPartyUtil.dismissDialog();

            currentButton = NONE_BUTTON;
            ShareStoryDialogActivity.close();
        }
    }

    @InjectView(R.id.activity_photo_album_gridview)
    GridView mGridView;

    private ShareStoreAdapter mShareStoreAdapter;

    public static ShareStoryDialogActivity instance;

    final public static String PARAM_ID = "id";

    final public static String PARAM_LANG = "lang";

    public static String sharedTitle_en = "TTTalk Popular";
    public static String sharedTitle_zh = "TT译聊「贴图」";
    public static String sharedTitle;

    public static String sharedText;
    public static String sharedImgUrl;
    public static String sharedUrl;
    public static Bitmap sharedImage;
    public static String sharedImgThumbUrl;
    public static Bitmap sharedImageThumb;
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
    public static final int TENCENT_QQ_BUTTON = 2;
    public static final int TENCENT_TQQ_BUTTON = 3;
    public static final int TENCENT_QZONE_BUTTON = 4;
    public static final int FACEBOOK_BUTTON = 5;
    public static final int TENCENT_WECHAT_MOMENT_BUTTON = 6;
    public static final int TENCENT_WECHAT_FRIEND_BUTTON = 6;
    public static final int GOOGLE_PLUS_BUTTON = 8;
    public static final int OTHERS_BUTTON = 10;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    // 新浪微博分享API
    IWeiboShareAPI mWeiboShareAPI;

    private final String TAG = Utils.CATEGORY
            + ShareStoryDialogActivity.class.getSimpleName();

    Map<String, String> mShare;

    public void doShareOnFacebook() {
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

//    public void doShareOnGooglePlus() {
//        currentButton = GOOGLE_PLUS_BUTTON;
//
//        String url = sharedUrl;
//        Uri streamUri = Uri.parse(MediaStore.Images.Media.insertImage(
//                getContentResolver(), sharedImage, null, null));
//        Uri userPhotoUri = Uri.parse(url);
//        String id = userPhotoUri.getQueryParameter(PARAM_ID);
//        String lang = userPhotoUri.getQueryParameter(PARAM_LANG);
//        String authority = userPhotoUri.getAuthority();
//        String path = userPhotoUri.getPath();
//        String scheme = userPhotoUri.getScheme();
//        url = scheme + "://" + authority + path + "?" + PARAM_ID + "=" + id
//                + "&" + PARAM_LANG + "=" + lang;
//        String text = url + " " + sharedText;
//
//        Intent shareIntent = new PlusShare.Builder(this)
//                // .setType("image/jpeg|text/plain")
//                // .setType("image/jpeg")
//                .setType("text/plain")
//                .setText(text)
//                .setContentUrl(userPhotoUri)
//                .setStream(streamUri)
//                .setContentDeepLinkId(
//                        GoogleParseDeepLinkActivity.SHARE_STORY + "," + id)
//                .getIntent();
//        try {
//            startActivityForResult(shareIntent, 0);
//        } catch (Exception e) {
//            currentButton = NONE_BUTTON;
//            Toast.makeText(ShareStoryDialogActivity.this,
//                    getString(R.string.share_failed), Toast.LENGTH_SHORT)
//                    .show();
//            currentButton = NONE_BUTTON;
//            ShareStoryDialogActivity.close();
//        }
//
//    }

    public void doShareOnOthers() {
        currentButton = OTHERS_BUTTON;

        ThirdPartyUtil.share(this, null, null, sharedText, sharedImageThumb,
                sharedUrl);

    }

    public void doShareOnSinaWeiBo() {

        currentButton = SINA_WEIBO_BUTTON;
        SinaWeiboShareResponseActivity.calledFrom = SinaWeiboShareResponseActivity.SHARE_STORY;

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
                            Toast.makeText(ShareStoryDialogActivity.this,
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
                sinaWeiboSendMessage();
            }
        } catch (WeiboShareException e) {
            Toast.makeText(ShareStoryDialogActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void doShareOnSMS() {
        String url = null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("sms_body", url);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);
    }

    public void doShareOnTencentQQ() {
        currentButton = TENCENT_QQ_BUTTON;

        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, sharedText);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, sharedUrl);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, sharedText);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, sharedImgUrl);
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        // params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0);
        // 0:Default; 1:SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN;
        // 2:SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE;
        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        tencentQQSendMessage(params);
    }

    public void doShareOnTencentQZone() {
        currentButton = TENCENT_QZONE_BUTTON;
        final Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, sharedTitle_zh);
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, sharedText);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, sharedUrl);
        params.putInt(QzoneShare.SHARE_TO_QQ_EXT_INT,
                QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        // 支持传多个imageUrl
        ArrayList<String> imageUrls = new ArrayList<String>();
        imageUrls.add(sharedImgUrl);
        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        tencentQZoneSendMessage(params);
    }

    public void doShareOnTencentTQQ() {
        currentButton = TENCENT_TQQ_BUTTON;

        Tencent mTencent = App.qqTencent;
        if (App.qqTencent == null) {
            mTencent = Tencent.createInstance(QQUtil.appid,
                    ShareStoryDialogActivity.this);
            App.qqTencent = mTencent;
        }

        if (!App.qqTencent.isSessionValid()) {
            mTencent.login(this, "all", new QQBaseUIListener() {
                @Override
                public void doComplete(JSONObject values) {
//                    if (BuildConfig.DEBUG) {
//                        Log.d("Share TO Tencent Weibo --> Text :",
//                                values.toString());
//                    }

                    tencentTQQSentPicText();
                }
            });
        } else {
            tencentTQQSentPicText();
        }
    }

    public void doShareOnTencentWeChatFriend() {
        if (!Utils.isWifiAvailible(this)) {
            Toast.makeText(this, getString(R.string.network_is_bad),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        currentButton = TENCENT_WECHAT_FRIEND_BUTTON;
        WXEntryActivity.calledFrom = WXEntryActivity.SHARE_STORY;

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

        // // 判断微信是否支持朋友圈
        // int wxSdkVersion = api.getWXAppSupportAPI();
        // if (wxSdkVersion < WeChatUtil.TIMELINE_SUPPORTED_VERSION) {
        // Toast.makeText(this,
        // getString(R.string.wechat_timeline_is_not_supported), 0)
        // .show();
        // return;
        // }

        // 将应用的appId注册到微信
        boolean register = api.registerApp(WeChatUtil.APP_ID);
        if (register) {

            // 发送请求到微信
            tencentWeChatSentWebpageToFriend(api);
        } else {
            Log.e("WeChat", "appId have not been registed. ");
        }
    }

    public void doShareOnTencentWeChatMoment() {
        if (!Utils.isWifiAvailible(this)) {
            Toast.makeText(this, getString(R.string.network_is_bad),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        currentButton = TENCENT_WECHAT_MOMENT_BUTTON;
        WXEntryActivity.calledFrom = WXEntryActivity.SHARE_STORY;

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
            tencentWeChatSentWebpageToMoment(api);
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
        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                .setLink(sharedUrl).setName(sharedTitle_en)
                .setDescription(sharedText).setPicture(sharedImgUrl).build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
    }

    private void facebookShowFeedDialog() {
        Bundle params = new Bundle();
        params.putString("caption", sharedTitle_en);
        params.putString("description", sharedText);
        params.putString("link", sharedUrl);
        params.putString("picture", sharedImgUrl);

        WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
                ShareStoryDialogActivity.this, Session.getActiveSession(),
                params)).setOnCompleteListener(new OnCompleteListener() {

            @Override
            public void onComplete(Bundle values, FacebookException error) {
                if (error == null) {
                    // When the story is posted, echo the success
                    // and the post Id.
                    final String postId = values.getString("post_id");
                    if (postId != null) {
                        // Toast.makeText(ShareStoryDialogActivity.this,
                        // "Posted story, id: " + postId,
                        // Toast.LENGTH_SHORT).show();
                        Toast.makeText(ShareStoryDialogActivity.this,
                                getString(R.string.share_success),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // User clicked the Cancel button
                        Toast.makeText(ShareStoryDialogActivity.this,
                                getString(R.string.share_canceled),
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (error instanceof FacebookOperationCanceledException) {
                    // User clicked the "x" button
                    Toast.makeText(ShareStoryDialogActivity.this,
                            getString(R.string.share_canceled),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Generic, ex: network error
                    Toast.makeText(
                            ShareStoryDialogActivity.this,
                            getString(R.string.share_failed)
                                    + "Error Message: " + error.toString(),
                            Toast.LENGTH_SHORT).show();
                }

                currentButton = NONE_BUTTON;
                ShareStoryDialogActivity.close();
            }

        }).build();
        feedDialog.show();
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
                                        Toast.makeText(
                                                ShareStoryDialogActivity.this,
                                                R.string.share_canceled,
                                                Toast.LENGTH_SHORT).show();
                                    } else if (FacebookDialog.COMPLETION_GESTURE_POST
                                            .equals(result)) {
                                        // Facebook分享成功
                                        Toast.makeText(
                                                ShareStoryDialogActivity.this,
                                                R.string.share_success,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        // user cancel
                                        Toast.makeText(
                                                ShareStoryDialogActivity.this,
                                                R.string.share_canceled,
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    currentButton = NONE_BUTTON;
                                    ShareStoryDialogActivity.close();
                                }

                            }

                            @Override
                            public void onError(
                                    FacebookDialog.PendingCall pendingCall,
                                    Exception error, Bundle data) {
                                Toast.makeText(
                                        ShareStoryDialogActivity.this,
                                        getString(R.string.share_failed)
                                                + " Error Message: "
                                                + error.toString(),
                                        Toast.LENGTH_SHORT).show();
                                currentButton = NONE_BUTTON;
                                ShareStoryDialogActivity.close();
                            }
                        });

            } else {
                Session.getActiveSession().onActivityResult(this, requestCode,
                        resultCode, data);
            }
        } else if (currentButton == GOOGLE_PLUS_BUTTON) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {
                ThirdPartyUtil.toastMessage(this,
                        getString(R.string.share_canceled));
            }
            currentButton = NONE_BUTTON;
            this.finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_story);
        ButterKnife.inject(this);
        setupComponents();
        instance = this;
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

    // Facebook
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    private void setupComponents() {
        mShareStoreAdapter = new ShareStoreAdapter(this);
        // 放入item qq
        List<Map<String, String>> shareList = new ArrayList<Map<String, String>>();
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_qq));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_qq));
        mShare.put("title", getString(R.string.share_to_qq));
        shareList.add(mShare);
        // 放入item qzone
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_qzone));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_qq));
        mShare.put("title", getString(R.string.share_to_qzone));
        shareList.add(mShare);
        // 放入item qq weibo
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_tqq));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_qq));
        mShare.put("title", getString(R.string.share_to_tqq));
        shareList.add(mShare);
        // 放入item weibo
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_weibo));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_sina));
        mShare.put("title", getString(R.string.share_to_sina_weibo));
        shareList.add(mShare);
        if (ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_WECHAT)) {
            // 放入item wechat
            mShare = new HashMap<String, String>();
            mShare.put("thumb", String.valueOf(R.drawable.button_wechat));
            mShare.put("background",
                    String.valueOf(R.drawable.background_round_button_wechat));
            mShare.put("title", getString(R.string.share_to_wechat));
            shareList.add(mShare);
            // 放入item moments
            mShare = new HashMap<String, String>();
            mShare.put("thumb", String.valueOf(R.drawable.button_moments));
            mShare.put("background",
                    String.valueOf(R.drawable.background_round_button_moment));
            mShare.put("title", getString(R.string.share_to_wechat_moment));
            shareList.add(mShare);
        }
        if (ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_GOOGLE_PLUS)) {
            // 放入item googleplus
            mShare = new HashMap<String, String>();
            mShare.put("thumb", String.valueOf(R.drawable.button_googleplus));
            mShare.put("background",
                    String.valueOf(R.drawable.background_round_button_google));
            mShare.put("title", getString(R.string.share_to_google));
            shareList.add(mShare);
        }
        // 放入item facebook
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_facebook));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_facebook));
        mShare.put("title", getString(R.string.share_to_facebook));
        shareList.add(mShare);
        mGridView.setAdapter(mShareStoreAdapter);
        // 放入item more
        mShare = new HashMap<String, String>();
        mShare.put("thumb", String.valueOf(R.drawable.button_more));
        mShare.put("background",
                String.valueOf(R.drawable.background_round_button_moment));
        mShare.put("title", getString(R.string.share_to_others));
        shareList.add(mShare);

        mShareStoreAdapter.addAll(shareList);
        mGridView.setAdapter(mShareStoreAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Map<String, String> shareStory = mShareStoreAdapter
                        .getItem(position);
                if (shareStory.get("title").equals(
                        getString(R.string.share_to_qq))) {
                    doShareOnTencentQQ();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_qzone))) {
                    doShareOnTencentQZone();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_sina_weibo))) {
                    doShareOnSinaWeiBo();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_tqq))) {
                    doShareOnTencentTQQ();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_wechat))) {
                    doShareOnTencentWeChatFriend();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_wechat_moment))) {
                    doShareOnTencentWeChatMoment();

//                } else if (shareStory.get("title").equals(
//                        getString(R.string.share_to_google))) {
//                    doShareOnGooglePlus();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_facebook))) {
                    doShareOnFacebook();

                } else if (shareStory.get("title").equals(
                        getString(R.string.share_to_others))) {
                    doShareOnOthers();

                }
            }
        });
    }

    /**
     * 新浪微博，创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject sinaWeiboGetImageObj() {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(sharedImage);
        // imageObject
        // .setImageObject(ThirdPartyUtil
        // .getBitmapFromCacheByUrl(sharedImgUrl));
        return imageObject;
    }

    /**
     * 新浪微博，创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject sinaWeiboGetTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = sharedText;

        return textObject;
    }

    /**
     * 新浪微博，创建多媒体（网页）消息对象。
     *
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject sinaWeiboGetWebpageObj() {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = sharedTitle_zh;
        mediaObject.description = sharedText;

        // 设置 Bitmap 类型的图片到网页对象里
        mediaObject.setThumbImage(sharedImageThumb);
        mediaObject.actionUrl = sharedUrl;
        // mediaObject.defaultText = "";
        return mediaObject;
    }

    /**
     * 新浪微博，第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sinaWeiboSendMessage() {

        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */) {
                sinaWeiboSendMultiMessage();
            } else {
                sinaWeiboSendSingleMessage();
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
    private void sinaWeiboSendMultiMessage() {

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        weiboMessage.textObject = sinaWeiboGetTextObj();
        weiboMessage.imageObject = sinaWeiboGetImageObj();
        weiboMessage.mediaObject = sinaWeiboGetWebpageObj();

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
    private void sinaWeiboSendSingleMessage() {

        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMessage weiboMessage = new WeiboMessage();
        weiboMessage.mediaObject = sinaWeiboGetTextObj();
        weiboMessage.mediaObject = sinaWeiboGetImageObj();
        weiboMessage.mediaObject = sinaWeiboGetWebpageObj();

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(request);
    }

    /**
     * QQ分享，用异步方式启动分享
     *
     * @param params
     */
    private void tencentQQSendMessage(final Bundle params) {
        final Activity activity = ShareStoryDialogActivity.this;
        Tencent mTencent = App.qqTencent;
        if (App.qqTencent == null) {
            mTencent = Tencent.createInstance(QQUtil.appid,
                    ShareStoryDialogActivity.this);
            App.qqTencent = mTencent;
        }
        final QQShare mQQShare = new QQShare(ShareStoryDialogActivity.this,
                App.qqTencent.getQQToken());

        mQQShare.shareToQQ(activity, params, new IUiListener() {

            @Override
            public void onCancel() {
                // 用户取消分享操作
                ThirdPartyUtil.toastMessage(activity,
                        getString(R.string.share_canceled));
                currentButton = NONE_BUTTON;
                ShareStoryDialogActivity.close();
            }

            @Override
            public void onComplete(Object response) {
                // QQ分享成功
                ThirdPartyUtil.toastMessage(activity,
                        getString(R.string.share_success));

                currentButton = NONE_BUTTON;
                ShareStoryDialogActivity.close();
            }

            @Override
            public void onError(UiError e) {
                ThirdPartyUtil.toastMessage(activity, "onError: "
                        + e.errorMessage, "e");
                currentButton = NONE_BUTTON;
                ShareStoryDialogActivity.close();
            }

        });
    }

    /**
     * QQ空间，用异步方式启动分享
     *
     * @param params
     */
    private void tencentQZoneSendMessage(final Bundle params) {
        final Activity activity = ShareStoryDialogActivity.this;
        final Tencent tencent = Tencent.createInstance(QQUtil.appid,
                ShareStoryDialogActivity.this);

        if (ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ)) {
            tencent.shareToQzone(activity, params, new QZoneApiListener(this));
        } else {

            tencent.shareToQzone(activity, params, new IUiListener() {

                @Override
                public void onCancel() {
                }

                @Override
                public void onComplete(Object response) {
                }

                @Override
                public void onError(UiError e) {
                }
            });
            currentButton = NONE_BUTTON;
            ShareStoryDialogActivity.close();
        }

    }

    /**
     * 腾讯微博发送图文微博
     */
    private void tencentTQQSentPicText() {
        Weibo mWeibo = new Weibo(this, App.qqTencent.getQQToken());
        if (QQUtil.qqIsReady(ShareStoryDialogActivity.this, App.qqTencent)) {
            String content = sharedText + "  " + sharedUrl;
            String imgPath = ThirdPartyUtil.getCachePathByUrl(sharedImgUrl);
            // mWeibo.sendText(content, new TQQApiListener("add_t", false,
            // ShareStoryDialogActivity.this));
            mWeibo.sendPicText(content, imgPath, new TQQApiListener("add_t",
                    false, ShareStoryDialogActivity.this));
            ThirdPartyUtil.showProgressDialog(this, null, null);
        }
    }

    /**
     * 微信分享网页到朋友圈
     *
     * @param api 微信API接口
     */
    private void tencentWeChatSentWebpageToFriend(IWXAPI api) {

        // 初始化一个WXWebpageObject对象
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = sharedUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = sharedText;
        msg.description = sharedTitle_zh;
        msg.thumbData = ThirdPartyUtil.bmpToByteArray(sharedImageThumb, false);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "webpage" + System.currentTimeMillis();
        req.message = msg;

        req.scene = SendMessageToWX.Req.WXSceneSession;

        // 用api接口发送数据到微信
        api.sendReq(req);
    }

    /**
     * 微信分享网页到朋友圈
     *
     * @param api 微信API接口
     */
    private void tencentWeChatSentWebpageToMoment(IWXAPI api) {

        // 初始化一个WXWebpageObject对象
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = sharedUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = sharedText;
        msg.description = sharedTitle_zh;
        msg.thumbData = ThirdPartyUtil.bmpToByteArray(sharedImageThumb, false);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "webpage" + System.currentTimeMillis();
        req.message = msg;

        req.scene = SendMessageToWX.Req.WXSceneTimeline;

        // 用api接口发送数据到微信
        api.sendReq(req);
    }

}