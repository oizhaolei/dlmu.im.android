package com.ruptech.chinatalk.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.SearchAutoComplete;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MainActivity;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    public static final String CATEGORY = "chinatalk.";
    public final static String TAG = Utils.CATEGORY
            + Utils.class.getSimpleName();

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static String last_friend_updatedate;
    private static double d;

    public static String abbrString(String message_content, int len) {
        String messageContent = "";
        if (message_content.length() > len) {
            messageContent = message_content.substring(0, len) + "...";
        } else {
            messageContent = message_content;
        }
        return messageContent;
    }

    public static int getCJKCharCount(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if ((Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES)
                    || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS)) {
                count++;
            }
        }

        return count;
    }

    public static void AlertDialog(Context mContext,
                                   OnClickListener mPositiveListener,
                                   OnClickListener mNegativeListener, String mMessage) {

        new CustomDialog(mContext)
                .setMessage(mMessage)
                .setPositiveButton(R.string.alert_dialog_ok, mPositiveListener)
                .setNegativeButton(R.string.alert_dialog_cancel,
                        mNegativeListener).show();
    }

    public static boolean checkNetwork(Context mContext) {
        if (!isMobileNetworkAvailible(mContext)
                && !Utils.isWifiAvailible(mContext)) {
            Toast.makeText(mContext,
                    R.string.wifi_and_3g_networks_are_not_available,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static Bitmap createMask(int imageResId, int colorResId) {
        int highlight_color = App.mContext.getResources().getColor(colorResId);
        Bitmap org = BitmapFactory.decodeResource(App.mContext.getResources(),
                imageResId);
        Bitmap result = Bitmap.createBitmap(org.getWidth(), org.getHeight(),
                Config.ARGB_4444);

        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawColor(highlight_color);
        mCanvas.drawBitmap(org, 0, 0, paint);
        paint.setXfermode(null);

        return result;
    }

    public static SearchView cutomizeSearchView(SearchView searchView) {
        View bgView = searchView.findViewById(R.id.search_plate);
        if (bgView != null)
            bgView.setBackgroundResource(R.drawable.abc_textfield_search_default_holo_light);

        SearchAutoComplete textView = (SearchAutoComplete) searchView
                .findViewById(R.id.search_src_text);
        if (textView != null) {
            textView.setHintTextColor(Color.WHITE);
            textView.setTextColor(Color.WHITE);
        }

        // ImageView searchButton = (ImageView) searchView
        // .findViewById(R.id.search_button);
        // if (searchButton != null) {
        // searchButton.setImageResource(R.drawable.ic_action_search);
        // }
        //
        // ImageView closeButton = (ImageView) searchView
        // .findViewById(R.id.search_close_btn);
        // if (closeButton != null)
        // closeButton.setImageResource(R.drawable.ic_clear_light);

        return searchView;
    }

    public static void dismissDialog(ProgressDialog dialog) {
        try {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        } catch (Exception e) {
        }
    }

    public static void doLogout(Context context) {
        // 关闭所有可能打开的activity
        MainActivity.close();
        ProfileActivity.close();
        // 结束
        App.removeUser();

        App.userDAO.deleteAll();
        last_friend_updatedate = null;
        PrefUtils.removePrefUser();

        App.notificationManager.cancelAll();

        App.mBus.post(new LogoutEvent());
    }


    public static String genUrl(Map<String, String> params, String url) {
        if (params.isEmpty()) return url;
        String rtn = url + "?";
        for (String key : params.keySet()) {
            rtn = rtn + key + "=" + params.get(key) + "&";
        }
        return rtn = rtn.substring(0, rtn.length() - 1);
    }

    public static Map<String, String> genParam(String[] params) {
        if (params.length == 0) return null;
        Map<String, String> rtn = new HashMap<String, String>();
        for (int i = 0; i < params.length; i++) {
            if (params[i].equals("loginid"))
                rtn.put(params[i], App.readUser().getUsername());
            if (params[i].equals("passwd"))
                rtn.put(params[i], new String(Base64.decode(PrefUtils.getUserPassword(), Base64.DEFAULT)));
            if (params[i].equals("zxjxjhh"))
                rtn.put(params[i], "2014-2015-2-1");
        }
        //TODO 传递客户端版本；
        rtn.put("version", "1");
        if (rtn.get("loginid") == null)
            rtn.put("loginid", App.readUser().getUsername());
        rtn.put("sign", Utils.genSign(rtn, App.readUser().getUsername()));
        return rtn;
    }

    public static String genSign(Map<String, String> params, String appkey) {
        // sign
        StringBuilder sb = new StringBuilder();
        sb.append(appkey);

        // 对参数名进行字典排序
        String[] keyArray = params.keySet().toArray(new String[params.size()]);
        Arrays.sort(keyArray);

        for (String key : keyArray) {
            String value = params.get(key);
            if (!Utils.isEmpty(value)) {
                sb.append(key).append(value);
            }
        }
        sb.append(AppPreferences.APK_SECRET);
        Log.d(TAG, sb.toString());

        String sign = Utils.sha1(sb.toString());
        return sign;
    }

    public static int getAppVersionCode() {
        int verCode = 0;
        try {
            PackageInfo packageInfo = App.mContext.getPackageManager()
                    .getPackageInfo(App.mContext.getPackageName(), 0);
            verCode = packageInfo.versionCode;

        } catch (NameNotFoundException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, e.getMessage(), e);
        }

        return verCode;
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            if (BuildConfig.DEBUG)
                Log.e("test", "cannot read exif", ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (byte aByte : bytes) {
            buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[aByte & 0x0f]);
        }
        return buf.toString();
    }

    private static String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder();

        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }

        return builder.toString();
    }

    public static String getLangDisplayName(String langCode) {
        if (langCode.equals(AppPreferences.SPINNER_DEFAULT_LANG)) {
            return App.mContext.getString(R.string.language_unseleted);
        } else if (langCode
                .equals(AppPreferences.SPINNER_ADDFRIEND_DEFAULT_LANG)) {
            return App.mContext.getString(R.string.language_unseleted2);
        }
        String[] lang_codes = App.mContext.getResources().getStringArray(
                R.array.lang_code);
        String[] lang_names = App.mContext.getResources().getStringArray(
                R.array.lang_display_name);
        for (int i = 0; i < lang_names.length; i++) {
            if (lang_codes[i].equals(langCode)) {
                return lang_names[i];
            }
        }
        return null;
    }

    public static int getLanguageFlag(String langCode) {
        if (langCode == null)
            return 0;
        String packageName = App.mContext.getPackageName();
        int identifier = App.mContext.getResources().getIdentifier(
                langCode.toLowerCase(Locale.ENGLISH), "drawable", packageName);
        if (identifier == 0) {
            identifier = App.mContext.getResources().getIdentifier("flag",
                    "drawable", packageName);
        }
        return identifier;
    }

    // MD5 hases are used to generate filenames based off a URL.
    public static String getMd5(String str) {
        MessageDigest mDigest;
        try {
            mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());

            return getHashString(mDigest);
        } catch (NoSuchAlgorithmException e) {
            Utils.sendClientException(e);
            throw new RuntimeException("No MD5 algorithm.");
        }

    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0 || s.equals("null");
    }

    public static boolean isMobileNetworkAvailible(Context content) {
        ConnectivityManager conMan = (ConnectivityManager) content
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            // mobile 3G Data Network
            State mobile = conMan.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).getState();

            if (mobile == State.CONNECTED || mobile == State.CONNECTING)
                return true;
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isWifiAvailible(Context content) {
        if (content == null) {
            return true;
        }

        ConnectivityManager conMan = (ConnectivityManager) content
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        // wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        if (wifi == State.CONNECTED || wifi == State.CONNECTING)
            return true;
        return false;
    }

    public static boolean saveFileFromServer(String imageURL, File file) {
        URL url;
        try {
            url = new URL(imageURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public static void sendClientException(Throwable e, Object... msgs) {
    }

    public static void setImageResource(ImageView iconView, int resId) {
        Bitmap selectImage = createMask(resId, R.color.orange_highlight);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_selected},
                new BitmapDrawable(App.mContext.getResources(), selectImage));
        states.addState(new int[]{},
                App.mContext.getResources().getDrawable(resId));
        iconView.setImageDrawable(states);
    }

    public static void setTextColor(TextView textview) {
        int normal_color = textview.getTextColors().getDefaultColor();
        int highlight_color = App.mContext.getResources().getColor(
                R.color.orange_highlight);

        int[][] color_states = new int[][]{
                new int[]{android.R.attr.state_selected}, // unchecked
                new int[]{}};

        int[] colors = new int[]{highlight_color, normal_color};

        ColorStateList colorList = new ColorStateList(color_states, colors);
        textview.setTextColor(colorList);
    }

    public static String sha1(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.update(str.getBytes());
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void showNormalActionBar(Activity activity) {
        ActionBar actionBar = ((ActionBarActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static void setUserPicImage(ImageView imageView, String url) {
        if (!isEmpty(url)) {
            if (!url.equals(imageView.getTag())) {
                ImageManager.imageLoader.displayImage(url, imageView, ImageManager
                        .getOptionsPortrait());
                imageView.setTag(url);
            }
        } else {
            ImageManager.imageLoader.displayImage(null, imageView,
                    ImageManager.getOptionsPortrait());
            imageView.setTag(null);
        }
    }

    public static boolean isExistNewVersion() {
        AppVersion serverAppInfo = PrefUtils.readServerAppInfo();
        if (serverAppInfo != null
                && serverAppInfo.verCode > BuildConfig.VERSION_CODE) {
            return true;
        }
        return false;
    }

    public static void doNotifyNewVersionFound(Context context, boolean defaultSound) {
        AppVersion serverAppInfo = PrefUtils.readServerAppInfo();

        String content = context.getString(R.string.please_click_to_update_newapk);
        int defaults = Notification.DEFAULT_LIGHTS;
        if (defaultSound) {
            defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
        }
        long when = System.currentTimeMillis();
        NotificationCompat.Builder mBuilder = new MyNotificationBuilder(context)
                .setSmallIcon(R.drawable.tt_logo2)
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.tt_logo2))
                .setContentTitle(context.getString(R.string.app_name))
                .setTicker(content).setContentText(content)
                .setVibrate(null)
                .setDefaults(defaults).setAutoCancel(true).setWhen(when);
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(serverAppInfo.appUrl));
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, 0);
        mBuilder.setContentIntent(contentIntent);

        App.notificationManager.cancel(R.string.please_click_to_update_newapk);
        App.notificationManager.notify(R.string.please_click_to_update_newapk,
                mBuilder.build());
    }

    public static class LengthFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            int sourceLen = source.toString().length();
            int destLen = dest.toString().length();
            int cjkCharCount = getCJKCharCount(source.toString())
                    + getCJKCharCount(dest.toString());

            int strLen = (sourceLen + destLen - cjkCharCount + cjkCharCount * 3);
            if (strLen > AppPreferences.MAX_INPUT_LENGTH) {
                return "";
            }
            return source;
        }
    }

}