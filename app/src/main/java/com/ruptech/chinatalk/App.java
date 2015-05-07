package com.ruptech.chinatalk;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ruptech.chinatalk.http.HttpServer;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.sqlite.UserDAO;
import com.ruptech.chinatalk.task.TaskManager;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.AssetsPropertyReader;
import com.ruptech.chinatalk.utils.ImageManager;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.BuildConfig;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;


import java.io.File;
import java.util.Properties;

public class App extends Application  {

	public static Bus mBus;
	static public Properties properties;

	public static HttpServer getHttpServer() {
		if (httpServer == null) {
			httpServer = new HttpServer();
		}
		return httpServer;
	}

	public static boolean isAvailableShowMain() {
		return App.readUser() != null;
	}


	public static User readUser() {
		if (user == null)
			user = PrefUtils.readUser();
		return user;
	}

	public static void removeUser() {
		PrefUtils.writeUser(null);
		App.user = null;
	}

	public static void writeUser(User user) {
		if (user == null)
			return;
		PrefUtils.writeUser(user);
		App.user = user;
	}

	private static HttpServer httpServer;
	public static AppVersion mApkVersionOfClient;
	public static Context mContext;
	public static ImageManager mImageManager;

	public static NotificationManager notificationManager;

	public static TaskManager taskManager = new TaskManager();
	private static User user;

	public static UserDAO userDAO;

	public final static String TAG = Utils.CATEGORY + App.class.getSimpleName();

	public static int displayHeight;

	public static int displayWidth;


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

		mContext = this.getApplicationContext();

		mApkVersionOfClient = Utils.getAppVersionOfClient(this);

		notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		userDAO = new UserDAO(getApplicationContext());

		mImageManager = new ImageManager(App.mContext);
		initImageLoader(getApplicationContext());


		getDisplaySize();

		startVersionCheckReceiver(this);
	}
	public static PendingIntent versionCheckPendingIntent;
	//Receiver 版本检查
	public static void startVersionCheckReceiver(Context context) {
		//version check
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent versionCheckIntent = new Intent(context, VersionCheckReceiver.class);
		 versionCheckPendingIntent = PendingIntent.getBroadcast(context, 0, versionCheckIntent, 0);
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 60 * 1000, 90 * 60 * 1000, versionCheckPendingIntent);
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
		if (mBus != null) {
			mBus.unregister(this);
		}

		super.onTerminate();
	}



	public static XMPPService mService;
	public static ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			XMPPService.XBinder binder = (XMPPService.XBinder) service;
			mService = binder.getService();
			if (!mService.isAuthenticated()) {
				String account =  App.readUser().getOF_username();
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
			Log.e(TAG, "Service wasn't bound!", e);
		}
	}

	/**
	 * 绑定服务
	 */
	public static void bindXMPPService() {
		Log.i(TAG, "bindXMPPService");
		Intent serviceIntent = new Intent(App.mContext, XMPPService.class);
		App.mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

}