package com.ruptech.chinatalk.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PrefUtils {

    final public static String PREF_USERINFO = "USER_INFO";
    final public static String PREF_SERVERAPP_INFO = "SERVER_APP_INFO";
    final public static String PREF_NOT_RECEICE_MESSAGE = "NOT_RECEICE_MESSAGE";
    final public static String PREF_VERIFICATION_MESSAGE = "VERIFICATION_MESSAGE";
    final public static String PREF_ACCOUNT_PASSWORD = "PREF_ACCOUNT_PASSWORD";
    final public static String AVAILABLE = "available";
    private static final String TAG = PrefUtils.class.getSimpleName();
    private static SharedPreferences mPref;

    private static SharedPreferences getmPref() {
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(App.mContext);
        }
        return mPref;
    }

    public static boolean getPrefNotReceiveMessage() {
        boolean isChecked = getmPref().getBoolean(
                PREF_NOT_RECEICE_MESSAGE, false);
        return isChecked;
    }

    public static boolean getPrefVerificationMessage() {
        boolean isChecked = getmPref().getBoolean(
                PREF_VERIFICATION_MESSAGE, false);
        return isChecked;
    }

    public static String getUserPassword() {
        String str = getmPref().getString(PREF_ACCOUNT_PASSWORD, "");
        return str;
    }

    private static Object readObject(String key) {
        String str = readStr(key);

        byte[] bytes = str.getBytes();
        if (bytes.length == 0) {
            return null;
        }
        try {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
            Base64InputStream base64InputStream = new Base64InputStream(
                    byteArray, Base64.DEFAULT);
            ObjectInputStream in = new ObjectInputStream(base64InputStream);
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public static AppVersion readServerAppInfo() {
        return (AppVersion) readObject(PREF_SERVERAPP_INFO);
    }

    private static String readStr(String key) {
        String str = getmPref().getString(key, "");
        return str;
    }


    public static User readUser() {
        return (User) readObject(PREF_USERINFO);
    }

    private static void remove(String key) {
        getmPref().edit().remove(key).commit();
    }

    public static void removePrefUser() {
        remove(PREF_USERINFO);
    }

    public static void savePrefUserPassword(String password) {
        getmPref()
                .edit()
                .putString(PREF_ACCOUNT_PASSWORD,
                        password).commit();
    }

    public static void savePrefNotReceiveMessage(boolean isChecked) {
        getmPref().edit().putBoolean(PREF_NOT_RECEICE_MESSAGE, isChecked)
                .commit();
    }

    public static void savePrefVerificationeMessage(boolean isChecked) {
        getmPref().edit().putBoolean(PREF_VERIFICATION_MESSAGE, isChecked)
                .commit();
    }

    public static void writeObject(String key, Object obj) {
        if (obj == null) {
            remove(key);
        } else {
            try {
                ByteArrayOutputStream out;
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                ObjectOutputStream objectOutput;
                objectOutput = new ObjectOutputStream(arrayOutputStream);
                objectOutput.writeObject(obj);
                byte[] data = arrayOutputStream.toByteArray();
                objectOutput.close();
                arrayOutputStream.close();

                out = new ByteArrayOutputStream();
                Base64OutputStream b64 = new Base64OutputStream(out,
                        Base64.DEFAULT);
                b64.write(data);
                b64.close();
                out.close();
                String str = new String(out.toByteArray());
                getmPref().edit().putString(key, str).commit();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);

            }
        }
    }

    public static void writeServerAppInfo(AppVersion serverAppInfo) {
        writeObject(PREF_SERVERAPP_INFO, serverAppInfo);
    }

    public static void writeUser(User user) {
        writeObject(PREF_USERINFO, user);
    }


}
