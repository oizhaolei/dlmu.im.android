package com.ruptech.chinatalk;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.facebook.Session;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruptech.chinatalk.http.Http2Server;
import com.ruptech.chinatalk.http.HttpServer;
import com.ruptech.chinatalk.http.HttpStoryServer;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.smack.TTTalkSmack;
import com.ruptech.chinatalk.sqlite.ChannelDAO;
import com.ruptech.chinatalk.sqlite.CommentNewsDAO;
import com.ruptech.chinatalk.sqlite.FriendDAO;
import com.ruptech.chinatalk.sqlite.HotUserPhotoDAO;
import com.ruptech.chinatalk.sqlite.MessageDAO;
import com.ruptech.chinatalk.sqlite.UserDAO;
import com.ruptech.chinatalk.sqlite.UserPhotoDAO;
import com.ruptech.chinatalk.task.TaskManager;
import com.ruptech.chinatalk.task.impl.SendClientMessageTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.AssetsPropertyReader;
import com.ruptech.chinatalk.utils.BadgeCount;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ServerAppInfo;
import com.ruptech.chinatalk.utils.Utils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.util.Properties;

public class App extends Application implements
        Thread.UncaughtExceptionHandler {

    public static Bus mBus;
    public static TTTalkSmack mSmack;
    static public Properties properties;

    public static void baiduRegiste(Context context) {
//        PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY,
//                getMetaValue(context, "api_key"));
    }

    public static void gcmRegiste(final Context context) {
//		// Make sure the device has the proper dependencies.
//		GCMRegistrar.checkDevice(context);
//		// Make sure the manifest was properly set - comment out this line
//		// while developing the app, then uncomment it when it's ready.
//		GCMRegistrar.checkManifest(context);
//
//		String regId = GCMRegistrar.getRegistrationId(context);
//		if (Utils.isEmpty(regId)) {
//			// Automatically registers application on startup.
//			GCMRegistrar.register(context, SENDER_ID);
//		}
    }

    public static Http2Server getHttp2Server() {
        if (http2Server == null) {
            http2Server = new Http2Server();
        }
        return http2Server;
    }

    public static HttpServer getHttpServer() {
        if (httpServer == null) {
            httpServer = new HttpServer();
        }
        return httpServer;
    }

    public static HttpStoryServer getHttpStoryServer() {
        if (httpStoryServer == null) {
            httpStoryServer = new HttpStoryServer();
        }
        return httpStoryServer;
    }

    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            Utils.sendClientException(e);

        }
        return apiKey;
    }

    public static boolean isAvailableShowMain() {
        return App.isVersionChecked() && App.readUser() != null
                && App.readServerAppInfo() != null;
    }

    public static boolean isVersionChecked() {
        return versionChecked
                || ((System.currentTimeMillis() - PrefUtils
                .getPrefVersionCheckedDate()) < AppPreferences.VERSION_CHECK_INTERVAL);
    }

    public static ServerAppInfo readServerAppInfo() {
        if (mServerAppInfo == null) {
            mServerAppInfo = PrefUtils.readServerAppInfo();
        }
        return mServerAppInfo;
    }

    public static User readUser() {
        if (user == null)
            user = PrefUtils.readUser();
        return user;
    }

    public static void registePush(Context context) {
        Log.d(TAG, "App.onCreate push start");
        if (Utils.isEmpty(App.simCountryIso) || "cn".equals(App.simCountryIso)) {
            try {
                // 百度云推送初始化
                baiduRegiste(context);
            } catch (Exception e) {
            }
        } else {
            try {
                // 谷歌推送注册
                gcmRegiste(context);
            } catch (Exception e) {
            }
        }
    }

    public static void removeUser() {
        PrefUtils.writeUser(null);
        App.user = null;
    }

    public static void writeServerAppInfo(ServerAppInfo serverAppInfo) {
        PrefUtils.writeServerAppInfo(serverAppInfo);
        App.mServerAppInfo = serverAppInfo;
    }

    public static void writeUser(User user) {
        if (user == null)
            return;
        PrefUtils.writeUser(user);
        App.user = user;
    }

    private static Http2Server http2Server;
    private static HttpServer httpServer;
    private static HttpStoryServer httpStoryServer;
    public static AppVersion mApkVersionOfClient;
    public static Context mContext;
    public static ImageManager mImageManager;

    public static MediaPlayer mPlayer;

    public static NotificationManager notificationManager;

    public static BadgeCount mBadgeCount = new BadgeCount();
    public static String simCountryIso;
    public static TaskManager taskManager = new TaskManager();
    public static TextToSpeech tts;
    private static User user;
    private static ServerAppInfo mServerAppInfo;

    public static MessageDAO messageDAO;

    public static UserDAO userDAO;

    public static ChannelDAO channelDAO;

    public static CommentNewsDAO commentNewsDAO;

    public static UserPhotoDAO userPhotoDAO;

    public static FriendDAO friendDAO;

    public static HotUserPhotoDAO hotUserPhotoDAO;

    public final static String TAG = Utils.CATEGORY + App.class.getSimpleName();

    public static boolean versionChecked = false;

    public static Oauth2AccessToken sinaOauth2AccessToken;

    public static Tencent qqTencent;

    public static String wechatAccessToken;

//	public static PlusClient googlePlusClient;

    public static Session facebookSession;

    public static int displayHeight;

    public static int displayWidth;

    private PendingIntent versionCheckPendingIntent;
    private PendingIntent uploadUserLocationPendingIntent;
    private PendingIntent retrieveInfoPeriodPendingIntent;

    public void exitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    // 获取servic名
    String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {

                return appProcess.processName;
            }
        }
        return null;
    }

    private void getDisplaySize() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        displayWidth = size.x;
        displayHeight = size.y;
    }

    private void initImageLoader(Context context) {
        File cacheDir = StorageUtils.getCacheDirectory(context);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .memoryCacheExtraOptions(1600, 1600)
                        // default = device screen dimensions
                .threadPoolSize(3)
                        // default
                .threadPriority(Thread.NORM_PRIORITY - 1)
                        // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache())
                        // 1 days in secs: 3*24*60*60 = 2592000
                .diskCache(new LimitedAgeDiscCache(cacheDir, 6 * 60 * 60))
                        // default
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                        // default
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Log.e(TAG, "App.onCreate");

        mBus = new Bus(ThreadEnforcer.ANY);
        mBus.register(this);

        AssetsPropertyReader assetsPropertyReader = new AssetsPropertyReader(this);
        properties = assetsPropertyReader.getProperties("env.properties");

        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = this.getApplicationContext();
        Log.i(TAG, "App.onCreate:" + getCurProcessName(this));
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        simCountryIso = mTelephonyMgr.getSimCountryIso();
        if (BuildConfig.DEBUG)
            Log.d(TAG, "simCountryIso:" + simCountryIso);

        mApkVersionOfClient = Utils.getAppVersionOfClient(this);
        mServerAppInfo = PrefUtils.readServerAppInfo();

        notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            tts = new TextToSpeech(this, new OnInitListener() {
                @Override
                public void onInit(int status) {
                }
            });
        } catch (Exception e) {
        }

        messageDAO = new MessageDAO(getApplicationContext());
        userDAO = new UserDAO(getApplicationContext());
        channelDAO = new ChannelDAO(getApplicationContext());
        commentNewsDAO = new CommentNewsDAO(getApplicationContext());
        userPhotoDAO = new UserPhotoDAO(getApplicationContext());
        friendDAO = new FriendDAO(getApplicationContext());
        hotUserPhotoDAO = new HotUserPhotoDAO(getApplicationContext());

        mImageManager = new ImageManager(App.mContext);
        initImageLoader(getApplicationContext());

        MyLocation.refreshMyLocation(App.this);

        getDisplaySize();

        checkPreviousException();

        // receiver 定期执行
        cancelReceiverPendingIntent();
        startPeriodReceiver();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageManager.imageLoader.clearMemoryCache();

        if (BuildConfig.DEBUG)
            Log.e(TAG, "onLowMemory");
    }

    @Override
    public void onTerminate() {

        if (BuildConfig.DEBUG)
            Log.e(TAG, "onTerminate");

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        mBus.unregister(this);
        cancelReceiverPendingIntent();
        super.onTerminate();
    }

    private void checkPreviousException() {
        String previousException = PrefUtils.getPrefException();
        if (previousException != null) {
            SendClientMessageTask sendClientMessageTask = new SendClientMessageTask(
                    previousException);
            //sendClientMessageTask.execute();
            PrefUtils.removePrefException();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, thread.getName(), throwable);

        Utils.saveClientException(throwable);
        exitApp();
    }

    public static XMPPService mService;
    public static ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            XMPPService.XBinder binder = (XMPPService.XBinder) service;
            mService = binder.getService();
            if (!mService.isAuthenticated()) {
                String account = Utils.getOF_username(App.readUser().getId());
                String password = App.readUser().getPassword();

                mService.login(account, password);
                // setStatusImage(false);
                // mTitleProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

    };

    /**
     * 解绑服务
     */
    public static void unbindXMPPService() {
        try {
            App.mContext.unbindService(mServiceConnection);
            Log.i(TAG, "unbindXMPPService");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Service wasn't bound!");
        }
    }

    /**
     * 绑定服务
     */
    public static void bindXMPPService() {
        Log.i(TAG, "bindXMPPService");
        Intent serviceIntent = new Intent( App.mContext, XMPPService.class);
        App.mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void cancelReceiverPendingIntent(){
        Utils.cancelReceiverPendingIntent(App.mContext, versionCheckPendingIntent);
        Utils.cancelReceiverPendingIntent(App.mContext, retrieveInfoPeriodPendingIntent);
        Utils.cancelReceiverPendingIntent(App.mContext, uploadUserLocationPendingIntent);
    }

    private void startPeriodReceiver(){
        versionCheckPendingIntent =  Utils.startVersionCheckReceiver(App.mContext);
        retrieveInfoPeriodPendingIntent = Utils.startInfoPeriodReceiver(App.mContext);
        uploadUserLocationPendingIntent = Utils.startUserLocationReceiver(App.mContext);
    }


}