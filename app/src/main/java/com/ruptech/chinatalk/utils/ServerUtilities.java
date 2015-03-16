package com.ruptech.chinatalk.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.event.OnlineEvent;
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

    public static boolean register(final String token,
                                   long clientId, String ifPage) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, "registering device (regId = " + token + ")");
        Map<String, String> params = new HashMap<>();
        params.put("task", "register");
        params.put("devicetoken", token);
        params.put("clientid", String.valueOf(clientId));
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
        return false;
    }

    public static void registerOpenfirePushOnServer(final String token) {
        if (App.readUser() != null
                && App.readUser().getId() > 0
                && !Utils.isEmpty(token)) {
            final long userId = App.readUser().getId();
            AsyncTask<Void, Void, Void> mRegisterTask;
            mRegisterTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    String ifPage = "push/openfire_devices.php";
                    boolean result = register(token, userId, ifPage);
                    if(result){
                        App.mBus.post(new OnlineEvent());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                }

            };
            mRegisterTask.execute();
        }
    }
}