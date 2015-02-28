package com.ruptech.chinatalk.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BaiduPushMessageReceiver;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.http.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final String APPNAME = "chinatalk";
    private final static String TAG = Utils.CATEGORY
            + ServerUtilities.class.getSimpleName();

    /**
     * Register this account/device pair within the server.
     *
     * @param userid
     * @param ifPage
     * @return
     */
    private static boolean registe(final Context context, final String regId,
                                   long userid, String ifPage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "registering device (regId = " + regId + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put("task", "register");
        params.put("devicetoken", regId);
        params.put("clientid", String.valueOf(userid));
        params.put("appname", APPNAME);
        params.put("appversion", App.mApkVersionOfClient.verName);
        params.put("devicename", android.os.Build.BRAND);
        params.put("devicemodel", android.os.Build.MODEL);
        params.put("deviceversion", android.os.Build.VERSION.RELEASE);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        params = HttpConnection.genParams(params);

        String serverUrl = App.getHttpServer().genRequestURL(ifPage, params);
        try {

            Response res = App.getHttpServer().get(serverUrl);
            JSONObject result = res.asJSONObject();
            if (result.getBoolean("success")) {
                return true;
            }
        } catch (Exception e) {
            Utils.sendClientException(e, serverUrl);
            if (BuildConfig.DEBUG)
                Log.e(TAG, serverUrl);
        }
        String message = context.getString(R.string.server_register_error);
        CommonUtilities.displaySystemMessage(context, message);
        return false;
    }

    public static void registerBaiduPushOnServer(final Context context) {
//		final String regId = BaiduPushMessageReceiver.mUserlId;
//		if (App.readUser() != null && App.readUser().getId() > 0
//				&& !Utils.isEmpty(regId)) {
//			final long userid = App.readUser().getId();
//			AsyncTask<Void, Void, Void> mRegisterTask;
//			mRegisterTask = new AsyncTask<Void, Void, Void>() {
//
//				@Override
//				protected Void doInBackground(Void... params) {
//					String ifPage = "baidu.php";
//					registe(context, regId, userid, ifPage);
//					BaiduPushMessageReceiver.setRegisteredOnServer(true);
//					GCMRegistrar.setRegisteredOnServer(context, false);
//					return null;
//				}
//
//				@Override
//				protected void onPostExecute(Void result) {
//				}
//
//			};
//			mRegisterTask.execute();
//		}
    }

    public static void registerGCMOnServer(final Context context) {
//		final String regId = GCMRegistrar.getRegistrationId(context);
//		// Try to register again, but not in the UI thread.
//		// It's also necessary to cancel the thread onDestroy(),
//		// hence the use of AsyncTask instead of a raw thread.
//		if (App.readUser() != null && App.readUser().getId() > 0
//				&& !Utils.isEmpty(regId)) {
//			AsyncTask<Void, Void, Void> mRegisterTask;
//			mRegisterTask = new AsyncTask<Void, Void, Void>() {
//
//				@Override
//				protected Void doInBackground(Void... params) {
//					String ifPage = "gcm.php";
//					long userid = App.readUser().getId();
//					if (registe(context, regId, userid, ifPage)) {
//						GCMRegistrar.setRegisteredOnServer(context, true);
//						// 只要gcm好用就取消百度
//						BaiduPushMessageReceiver.setRegisteredOnServer(false);
//					}
//					return null;
//				}
//
//				@Override
//				protected void onPostExecute(Void result) {
//					// BootReceiver.restartReceiver(context);
//				}
//
//			};
//			mRegisterTask.execute();
//		}
    }

    /**
     * Unregister this account/device pair within the server.
     *
     * @param ifPage
     * @return
     */
    private static boolean unregiste(final Context context, final String regId,
                                     String ifPage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "unregistering device (regId = " + regId + ")");
        Map<String, String> params = new HashMap<String, String>();
        params.put("task", "unregister");
        params.put("devicetoken", regId);
        params = HttpConnection.genParams(params);

        String serverUrl = App.getHttpServer().genRequestURL(ifPage, params);
        try {

            Response res = App.getHttpServer().get(serverUrl);
            JSONObject result = res.asJSONObject();
            return result.getBoolean("success");
        } catch (Exception e) {
            Utils.sendClientException(e, serverUrl);
            if (BuildConfig.DEBUG)
                Log.e(TAG, serverUrl);
            String message = context.getString(R.string.server_register_error);
            CommonUtilities.displaySystemMessage(context, message);
            return false;
        }
    }

    public static void unregisterBaiduPushOnServer(final Context context) {
        final String regId = BaiduPushMessageReceiver.mUserlId;
        if (!Utils.isEmpty(regId)) {
            AsyncTask<Void, Void, Void> mRegisterTask;
            mRegisterTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    String ifPage = "baidu.php";
                    unregiste(context, regId, ifPage);
                    BaiduPushMessageReceiver.setRegisteredOnServer(false);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                }

            };
            mRegisterTask.execute();
        }
    }

    public static void unregisterGCMOnServer(final Context context) {
//		final String regId = GCMRegistrar.getRegistrationId(context);
//		if (!Utils.isEmpty(regId)) {
//			AsyncTask<Void, Void, Void> mRegisterTask;
//			mRegisterTask = new AsyncTask<Void, Void, Void>() {
//
//				@Override
//				protected Void doInBackground(Void... params) {
//					String ifPage = "gcm.php";
//					unregiste(context, regId, ifPage);
//					GCMRegistrar.setRegisteredOnServer(context, false);
//					return null;
//				}
//
//				@Override
//				protected void onPostExecute(Void result) {
//					// BootReceiver.restartReceiver(context);
//					// String message =
//					// context.getString(R.string.server_registered);
//					// Toast.makeText(context, message,
//					// Toast.LENGTH_SHORT).show();
//				}
//
//			};
//			mRegisterTask.execute();
//		}
    }
}