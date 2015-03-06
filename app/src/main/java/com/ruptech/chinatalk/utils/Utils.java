package com.ruptech.chinatalk.utils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.SearchAutoComplete;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.MainActivity;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.http.HttpConnection;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.impl.SendClientMessageTask;
import com.ruptech.chinatalk.ui.AbstractChatActivity;
import com.ruptech.chinatalk.ui.LoginLoadingActivity;
import com.ruptech.chinatalk.ui.setting.SettingSystemInfoActivity;
import com.ruptech.chinatalk.ui.story.UserStoryCommentActivity;
import com.ruptech.chinatalk.ui.user.ChangeTel3Activity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.ServerAppInfo.LangTrans;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.GuideViewManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean systemGeocoderAvaiable = true;

    public static final String CATEGORY = "chinatalk.";

    private static final double EARTH_RADIUS = 6378137;

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String last_friend_updatedate;

    public static long lastTouchMillis;

    public final static String TAG = Utils.CATEGORY
            + Utils.class.getSimpleName();

    public static String abbrString(String message_content, int len) {
        String messageContent = "";
        if (message_content.length() > len) {
            messageContent = message_content.substring(0, len) + "...";
        } else {
            messageContent = message_content;
        }
        return messageContent;
    }

    public static Map<String, String> additionalHeaders() {
        Map<String, String> headers = new HashMap<>();
        String lang = Utils.getUserLanguage();
        headers.put("Accept-Language", lang);
        return headers;
    }

    public static void AlertDialog(Context mContext,
                                   OnClickListener mPositiveListener,
                                   OnClickListener mNegativeListener, String mTitle, String mMessage) {

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

    public static boolean checkSimCountryIso() {
        return "cn".equals(App.simCountryIso) || "kr".equals(App.simCountryIso);
    }

    public static boolean checkTts(String lang) {
        if (App.tts == null) {
            return false;
        }
        Locale locale = Utils.getLocale(lang);
        int result = App.tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            return false;
        } else {
            return true;
        }
    }

    public static Map<String, String> convertJsonItem(JSONObject jo)
            throws JSONException {
        Map<String, String> map = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jo.getString(key));
        }

        return map;
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

    private static String createReportFromException(Throwable e, Object... msgs) {
        StringBuffer sb = new StringBuffer(1024);
        sb.append(HttpConnection.getUrlHistory());
        for (Object o : msgs) {
            sb.append(o).append(',');
        }

        sb.append("\nAPP: ").append(App.mApkVersionOfClient);
        sb.append("\nServerAppInfo: ").append(App.readServerAppInfo());
        sb.append("\nUser: ").append(App.readUser());
//        sb.append("\nPush: ")
//				.append(GCMRegistrar.isRegistered(App.mContext) ? '1' : '0')
//				.append(GCMRegistrar.isRegisteredOnServer(App.mContext) ? '1'
//						: '0')
//                .append(BaiduPushMessageReceiver.isRegistered() ? '1' : '0')
//                .append(BaiduPushMessageReceiver.isRegisteredOnServer() ? '1'
//                        : '0');
        sb.append(",\nHARDWARE: ").append(android.os.Build.HARDWARE)
                .append(",\nMODEL: ").append(android.os.Build.MODEL)
                .append(",\nPRODUCT: ").append(android.os.Build.PRODUCT)
                .append(",\nSERIAL: ").append(android.os.Build.SERIAL)
                .append(".\n");
        if (e != null) {
            sb.append(e.toString()).append("\n");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sb.append(sw.toString());
        }

        return sb.toString();
    }

    public static String currencyFormat(double num) {
        return NumberFormat.getNumberInstance().format(num);
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
        UserStoryCommentActivity.close();
        ChangeTel3Activity.close();
        AbstractChatActivity.close();
        MainActivity.close();
        ProfileActivity.close();
        SettingSystemInfoActivity.close();
        // 结束
        App.removeUser();

        App.friendDAO.deleteAll();
        App.userDAO.deleteAll();
        App.userPhotoDAO.deleteAll();
        App.channelDAO.deleteAll();
        App.commentNewsDAO.deleteAll();

        App.messageDAO.deleteByUserId(AppPreferences.SYSTEM_REQUEST_TO_USERID);
        last_friend_updatedate = null;

        PrefUtils.removePrefPopularType();
        PrefUtils.removePrefTTTLastSelectedLangs();
        PrefUtils.removePrefRecommendedFriendsLastUpdate();
        PrefUtils.removePrefShowAnnouncementDialogLastUpdateDate();
        PrefUtils.removePrefUser();
        PrefUtils.removePrefPreferLang();

        App.notificationManager.cancelAll();

        App.qqTencent = null;
        App.sinaOauth2AccessToken = null;
        App.facebookSession = null;
//		App.googlePlusClient = null;
        App.wechatAccessToken = null;

        App.mBus.post(new LogoutEvent());
    }

    /**
     * 目前只有中韩有代理手机，能支持手机号码注册
     *
     * @param userLanguage
     * @return
     */
    public static boolean existsViaTel(String userLanguage) {
        return "CN".equals(userLanguage) || "KR".equals(userLanguage);
    }

    public static boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String FomartDistance(int distance) {
        // 不足一千米显示附近
        String result = "";
        if (distance < 1000) {
            result = App.mContext.getString(R.string.vicinity);
        } else {
            // 大于一千米计算为千米单位，再进行格式化
            distance = distance / 1000;
            result = App.mContext.getString(R.string.distance, NumberFormat
                    .getNumberInstance().format(distance));
        }
        return result;
    }

    public static int getDisplayWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        return size.x;
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

        String sign = Utils.sha1(sb.toString());

        return sign;
    }

    /**
     * 口音
     *
     * @param langAbbr
     * @return
     */
    public static String getAccentLanguageFromAbbr(String langAbbr) {
        String[] accent_languages_from = App.mContext.getResources()
                .getStringArray(R.array.accent_languages);
        String[] languages_from_code = App.mContext.getResources()
                .getStringArray(R.array.lang_code);
        for (int i = 0; i < languages_from_code.length; i++) {
            if (languages_from_code[i].equals(langAbbr)) {
                return accent_languages_from[i];
            }
        }
        return null;
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

    public static AppVersion getAppVersionOfClient(Context context) {
        AppVersion ver = new AppVersion();
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            ver.verCode = packageInfo.versionCode;
            ver.verName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, e.getMessage(), e);
        }

        return ver;
    }

    /**
     * 翻译秘书选择语言的时候,根据lang1获取可翻译用的lang2
     *
     * @param lang
     * @return
     */
    public static String[] getAvailableLang2ByLang1(String lang) {
        String[] languages_from_code = App.mContext.getResources()
                .getStringArray(R.array.lang_code);
        List<String> list = new ArrayList<String>();
        for (String langCode : languages_from_code) {
            if (!lang.equals(langCode)) {
                list.add(langCode);
            }
        }
        return list.toArray(new String[0]);
    }

    public static String getCity(Context context, double latitude,
                                 double longitude) {
        String cityName = null;

        if (isValidLocation(latitude, longitude)) {

            if (systemGeocoderAvaiable) {
                try {
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context);
                    addresses = geocoder
                            .getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        String country = addresses.get(0).getCountryName();
                        String city = addresses.get(0).getLocality();
                        cityName = ((Utils.isEmpty(country) ? "" : country) + ' ' + (Utils
                                .isEmpty(city) ? "" : city)).trim();
                    }
                } catch (Exception e) {
                }
            }

            if (isEmpty(cityName)) {
                cityName = App.getHttp2Server().getAddress(latitude, longitude);
                systemGeocoderAvaiable = false;
            }
        } else {
            cityName = null;
        }

        return cityName;
    }

    public static String getCity(Context context, int late6, int lnge6) {
        double latitude = Double.valueOf(late6) / 1E6;
        double longitude = Double.valueOf(lnge6) / 1E6;
        return getCity(context, latitude, longitude);
    }

    public static int GetDistance(double lng1, double lat1, double lng2,
                                  double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return (int) s;
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

    public static File getFilterFolder(Context context) {
        File folder = null;
        folder = new File(FileHelper.getPublicPath(context), "Filter");
        if (!folder.exists())
            folder.mkdirs();
        return folder;
    }

    public static int[] getFollowFriendCountArray(int[] followFriendCountArray,
                                                  List<Friend> friends) {
        int fansNewFollowCount = 0;
        int fansAlreadyFollowCount = 0;
        int fansUnFollowCount = 0;
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);

            // db
            Friend dbFriend = App.friendDAO.fetchFriend(friend.user_id,
                    friend.friend_id);
            Friend dbCheckFriend = App.friendDAO.fetchFriend(friend.friend_id,
                    friend.user_id);
            // request
            if (friend.friend_id == App.readUser().getId() && friend.done == 1
                    && dbFriend == null && dbCheckFriend == null) {
                fansNewFollowCount++;
            }
            // accpet
            if (friend.friend_id == App.readUser().getId() && friend.done == 1
                    && dbFriend == null && dbCheckFriend != null
                    && dbCheckFriend.done == 1) {
                fansNewFollowCount++;
            }

            // already follow
            if (friend.friend_id == App.readUser().getId() && friend.done == 1
                    && dbFriend != null) {
                fansAlreadyFollowCount++;
            }

            // unfollowed
            if (friend.friend_id == App.readUser().getId() && friend.done == 0
                    && dbFriend != null && dbFriend.done != 0) {
                fansUnFollowCount++;
            }
        }
        followFriendCountArray[0] = fansNewFollowCount;
        followFriendCountArray[1] = fansAlreadyFollowCount;
        followFriendCountArray[2] = fansUnFollowCount;
        return followFriendCountArray;
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static String getFriendLastUpdatedate() {
        // mem
        if (last_friend_updatedate != null) {
            return last_friend_updatedate;
        }
        // local db
        last_friend_updatedate = App.friendDAO.getLastUpdatedate();
        if (last_friend_updatedate != null) {
            return last_friend_updatedate;
        }
        // zero time
        last_friend_updatedate = DateCommonUtils.getUtcDate(new Date(0),
                DateCommonUtils.DF_yyyyMMddHHmmssSSS);

        return last_friend_updatedate;
    }

    // story 相关的，有朋友昵称则使用昵称（评论里除外）,没有则使用fullname
    public static String getFriendName(Long userId, String defaultName) {
        Friend friendInfo = App.friendDAO.fetchFriend(App.readUser().getId(),
                userId);
        if (friendInfo != null
                && !Utils.isEmpty(friendInfo.getFriend_nickname())) {
            return friendInfo.getFriend_nickname();
        }
        return defaultName;
    }

    public static List<String> getFullLanguageList(String language) {
        List<String> langFull = Arrays.asList(AppPreferences.LANGUAGE_DEFAULT
                .split(","));
        if (AppPreferences.LANGUAGE_CN.contains(language)) {
            langFull = Arrays.asList(AppPreferences.LANGUAGE_CN.split(","));
        } else if (AppPreferences.LANGUAGE_EN.contains(language)) {
            langFull = Arrays.asList(AppPreferences.LANGUAGE_EN.split(","));
        } else if (AppPreferences.LANGUAGE_KR.contains(language)) {
            langFull = Arrays.asList(AppPreferences.LANGUAGE_KR.split(","));
        } else if (AppPreferences.LANGUAGE_JP.contains(language)) {
            langFull = Arrays.asList(AppPreferences.LANGUAGE_JP.split(","));
        } else {
            langFull = Arrays.asList(AppPreferences.LANGUAGE_EN.split(","));
        }

        return langFull;
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

    public static Locale getLocale(String langAbbr) {
        String[] languages_abbr = App.mContext.getResources().getStringArray(
                R.array.languages_abbr);
        String[] languages_from_code = App.mContext.getResources()
                .getStringArray(R.array.lang_code);
        for (int i = 0; i < languages_from_code.length; i++) {
            if (languages_from_code[i].equals(langAbbr)) {
                Locale l = new Locale(languages_abbr[i]);
                return l;
            }
        }
        return null;
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

    public static List<String> getMobileContactList(String displayName) {
        List<String> contacts = new ArrayList<String>();
        try {
            ContentResolver cr = App.mContext.getContentResolver();
            Cursor cur;
            if (isEmpty(displayName)) {
                cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                        null, null, ContactsContract.Contacts.DISPLAY_NAME);
            } else {
                cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                        Phone.DISPLAY_NAME + " = ?",
                        new String[]{displayName}, null);
            }

            if (cur != null && cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    // email
                    Cursor cursorEmail = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                    + " = ?", new String[]{id}, null);
                    if (cursorEmail != null) {
                        while (cursorEmail.moveToNext()) {
                            // to get the contact email
                            String email = cursorEmail
                                    .getString(cursorEmail
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            if (!contacts.contains(email) && !isEmpty(email)) {
                                contacts.add(email);
                            }

                        }
                        cursorEmail.close();
                    }

                    // phone
                    Cursor cursorPhone = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = ?", new String[]{id}, null);
                    if (cursorPhone != null) {
                        while (cursorPhone.moveToNext()) {
                            // to get the contact phone
                            String phone = cursorPhone
                                    .getString(cursorPhone
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                            if (!contacts.contains(phone) && !isEmpty(phone)) {
                                contacts.add(phone);
                            }
                        }
                        cursorPhone.close();
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return contacts;
    }

    public static String getNoPhotoMessage() {
        String msg = App.mContext.getResources().getString(
                R.string.photo_data_error);
        return msg;
    }

    public static String getPostStep(int total, int index, int resId) {
        String title = App.mContext.getResources().getString(resId);
        title = String.format("%s(%d/%d)", title, index, total);
        return title;
    }

    public static String[] getServerTransLang() {
        String[] languages_from_code = App.mContext.getResources()
                .getStringArray(R.array.lang_code);
        return languages_from_code;
    }

    // 注册、添加好友Spinner
    public static String[] getSpinnerLang(String addDefaultLang) {
        String[] storeLang = getServerTransLang();
        String[] newLangArry = new String[storeLang.length + 1];
        newLangArry[0] = addDefaultLang;
        for (int i = 0; i < storeLang.length; i++) {
            newLangArry[i + 1] = storeLang[i];
        }
        return newLangArry;
    }

    public static int getStoreTranslatorCount(String lang1, String lang2) {
        if (lang1.equals(lang2)) {
            return 0;
        }
        List<LangTrans> storeLang = App.readServerAppInfo().langsArray;
        for (LangTrans langTrans : storeLang) {
            if ((lang1.equalsIgnoreCase(langTrans.lang1) && lang2
                    .equalsIgnoreCase(langTrans.lang2))
                    || (lang2.equalsIgnoreCase(langTrans.lang1) && lang1
                    .equalsIgnoreCase(langTrans.lang2))) {
                return langTrans.translateCount;
            }
        }
        return 0;
    }

    public static String getStoryTagCodeByName(String tagName) {
        String[] storyTagCodeArray = App.mContext.getResources()
                .getStringArray(R.array.story_tag_code_array);
        String[] storyTagNameArray = App.mContext.getResources()
                .getStringArray(R.array.story_tag_array);
        for (int i = 0; i < storyTagNameArray.length; i++) {
            if (storyTagNameArray[i].equals(tagName)) {
                return storyTagCodeArray[i];
            }
        }
        return null;
    }

    public static String getStoryTagNameByCode(String tagCode) {
        // String[] storyTagCodeArray = App.mContext.getResources()
        // .getStringArray(R.array.story_tag_code_array);
        // String[] storyTagNameArray = App.mContext.getResources()
        // .getStringArray(R.array.story_tag_array);
        // for (int i = 0; i < storyTagCodeArray.length; i++) {
        // if (storyTagCodeArray[i].equals(tagCode)) {
        // return storyTagNameArray[i];
        // }
        // }
        return tagCode;
    }

    public static String getTimeSetting(Context context, int start, int duration) {
        String result = "";
        if (duration == 0) {
            result = context.getString(R.string.not_interrupt_no_setting);
        } else if (duration == 24) {
            result = context.getString(R.string.not_interrupt_all_day);
        } else {
            int end = start + duration;
            String nextDay;
            if (end < 24) {
                nextDay = "";
            } else {
                end = end % 24;
                nextDay = context
                        .getString(R.string.not_interrupt_time_next_day);
            }
            result = context.getString(R.string.not_interrupt_time_result,
                    start, nextDay, end);
        }
        return result;
    }

    public static String getUserLanguage() {
        // 语言种类还需要完善
        String lang = AppPreferences.LANGUAGE_DEFAULT;
        if (App.readUser() != null && !Utils.isEmpty(App.readUser().getLang())) {
            lang = App.readUser().getLang();
        } else {
            lang = Locale.getDefault().getLanguage();
        }

        int length = AppPreferences.LanguageArray.length;
        for (int i = 0; i < length; i++) {
            List<String> languageList = Arrays
                    .asList(AppPreferences.LanguageArray[i].split(","));
            if (languageList.contains(lang.toUpperCase())) {
                lang = languageList.get(0);
                break;
            }
        }

        if (BuildConfig.DEBUG)
            Log.i(TAG, "lang:" + lang);
        return lang;
    }

    public static File getVoiceFolder(Context context) {
        File folder = null;
        folder = new File(FileHelper.getPublicPath(context), "Voice");
        if (!folder.exists())
            folder.mkdirs();
        return folder;
    }

    public static void guideAnimation(final Activity ctx) {
        GuideViewManager guideViewManager = new GuideViewManager(ctx);
        guideViewManager.showGuideView();
    }

    private static boolean hasParent(Activity activity) {
        ActivityManager am = (ActivityManager) activity
                .getSystemService(Context.ACTIVITY_SERVICE);

        int activityCountInStack = am.getRunningTasks(1).get(0).numActivities;

        return activityCountInStack > 1;

    }

    public static String highlightTag(String content) {
        String result = content;
        int start = content.indexOf(UserStoryCommentActivity.TAG_PATTERN);
        int end = content.indexOf(UserStoryCommentActivity.SPLIT_PATTERN);
        if (start != -1 && end != -1 && end > start) {
            String prefix = content.substring(0, start);
            String tag = content.substring(start, end);
            String suffix = content.substring(end);
            String color = App.mContext.getString(R.color.reply_name);
            color = "#" + color.substring(color.length() - 6, color.length());
            result = String.format("%s<font color='%s'>%s</font>%s", prefix,
                    color, tag, suffix);
        }
        return result;
    }

    public static String htmlSpecialChars(String s) {

        if (s == null)
            return null;

        String[] key = new String[]{"&", "<", ">"};
        String[] val = new String[]{"&amp;", "&lt;", "&gt;"};
        for (int i = 0; i < key.length; i++) {
            s = s.replaceAll(key[i], val[i]);
        }
        return s;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0 || s.equals("null");
    }

    public static boolean isExistStore(String lang1, String lang2) {
        List<LangTrans> storeLang = App.readServerAppInfo().langsArray;
        for (LangTrans langTrans : storeLang) {
            if ((lang1.equalsIgnoreCase(langTrans.lang1) && lang2
                    .equalsIgnoreCase(langTrans.lang2))
                    || (lang2.equalsIgnoreCase(langTrans.lang1) && lang1
                    .equalsIgnoreCase(langTrans.lang2))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断两个语言是否是免费翻译
     *
     * @param lang1
     * @param lang2
     * @return
     */
    public static boolean isGoogleTranslate(String lang1, String lang2) {
        if (lang1.equals(lang2)) {
            return false;
        }
        List<LangTrans> storeLang = App.readServerAppInfo().langsArray;
        for (LangTrans langTrans : storeLang) {
            if ((lang1.equalsIgnoreCase(langTrans.lang1) && lang2
                    .equalsIgnoreCase(langTrans.lang2))
                    || (lang2.equalsIgnoreCase(langTrans.lang1) && lang1
                    .equalsIgnoreCase(langTrans.lang2))) {
                return langTrans.translateCount == 0;
            }
        }
        return true;
    }

    public static boolean isHttp(String url) {
        boolean isValid = false;

        String expression = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        CharSequence inputStr = url;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isInstalledApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            // 判断是否获取到了对应的包名信息
            if (pInfo != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            // nothing
        }
        return false;
    }

    public static boolean isAvailableIntent(Context context, String intentName) {
        // Check if the intent is available
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(intentName);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        // If the list size is greater than 0 the Intent is available
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isMail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
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

    // 暂时手机号10位或者11位
    public static boolean isTelphone(String str) {
        Pattern pattern = Pattern.compile("[0-9]{10,11}");
        Matcher isTel = pattern.matcher(str);
        if (!isTel.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isThirdPartyLogin() {
        if (App.readUser() == null) {
            return false;
        }
        String mUsertel = App.readUser().getTel();
        if (!Utils.isEmpty(mUsertel)
                && (mUsertel.startsWith(AppPreferences.THIRD_PARTY_TYPE_QQ)
                || mUsertel
                .startsWith(AppPreferences.THIRD_PARTY_TYPE_SINA)
                || mUsertel
                .startsWith(AppPreferences.THIRD_PARTY_TYPE_GOOGLE)
                || mUsertel
                .startsWith(AppPreferences.THIRD_PARTY_TYPE_FACEBOOK) || mUsertel
                .startsWith(AppPreferences.THIRD_PARTY_TYPE_WECHAT))) {
            return true;
        }
        return false;
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        boolean isValidLocation = false;
        if (((0 < latitude && 0 <= longitude) || (0 <= latitude && 0 < longitude))
                && (latitude < AppPreferences.LATE6_IMPOSSIBLE / 1E6 - 1)
                && (longitude < AppPreferences.LNGE6_IMPOSSIBLE / 1E6 - 1)) {
            isValidLocation = true;
        }
        return isValidLocation;
    }

    public static boolean isValidLocation6(int late6, int lnge6) {
        boolean isValidLocation6 = false;
        if (((0 < late6 && 0 <= lnge6) || (0 <= late6 && 0 < lnge6))
                && late6 < AppPreferences.LATE6_IMPOSSIBLE
                && lnge6 < AppPreferences.LNGE6_IMPOSSIBLE) {
            isValidLocation6 = true;
        }
        return isValidLocation6;
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

    public static String joinList(List<String> list, char c) {
        if (list == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer(list.size() * 10);
        for (String s : list) {
            sb.append(s).append(c);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static void likeAnimation(final ImageView aniView,
                                     final boolean isLike) {

        float scaleValue = 1.5f;
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(aniView, "scaleX", 1f,
                scaleValue).setDuration(500);
        scaleUpX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(aniView, "scaleY", 1f,
                scaleValue).setDuration(500);
        scaleUpY.setInterpolator(new AccelerateInterpolator());

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.addListener(new AnimatorListener() {

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isLike) {
                    aniView.setImageResource(R.drawable.ic_action_social_like_unselected);
                } else {
                    aniView.setImageResource(R.drawable.ic_action_social_like_selected);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

        });

        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(aniView, "scaleX",
                scaleValue, 1f).setDuration(500);
        scaleDownX.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(aniView, "scaleY",
                scaleValue, 1f).setDuration(500);
        scaleDownY.setInterpolator(new AccelerateInterpolator());

        AnimatorSet scaleDown = new AnimatorSet();
        scaleUp.playTogether(scaleDownX, scaleDownY);

        AnimatorSet likeAnimation = new AnimatorSet();
        likeAnimation.playSequentially(scaleUp, scaleDown);
        likeAnimation.start();

    }

    public static void onBackPressed(Activity activity) {

        if (!hasParent(activity)) {
            App.registePush(activity);
            LoginLoadingActivity.gotoMainActivity(activity);
            activity.finish();
        } else {
            activity.finish();
        }
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static void saveClientException(Throwable e, Object... msgs) {
        PrefUtils.savePrefException(createReportFromException(e, msgs));
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
        if (BuildConfig.DEBUG) {
            return;
        }
        String report = createReportFromException(e, msgs);

        SendClientMessageTask sendClientMessageTask = new SendClientMessageTask(
                report);
        sendClientMessageTask.execute();
    }

    public static void setGiftPicImage(ImageView imageView, String url) {
        if (!isEmpty(url)) {
            if (!url.equals(imageView.getTag())) {
                ImageManager.imageLoader.displayImage(App.readServerAppInfo()
                        .getServerPresent(url), imageView, ImageManager
                        .getOptionsPortrait());
                imageView.setTag(url);
            }
        } else {
            ImageManager.imageLoader.displayImage(null, imageView,
                    ImageManager.getOptionsPortrait());
            imageView.setTag(null);
        }
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

    public static void setSubTabImageResource(ImageView iconView, int resId) {
        Bitmap selectImage = createMask(resId, R.color.orange_highlight);
        Bitmap normalImage = createMask(resId, R.color.text_gray);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_selected},
                new BitmapDrawable(App.mContext.getResources(), selectImage));
        states.addState(new int[]{},
                new BitmapDrawable(App.mContext.getResources(), normalImage));
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

    public static void setUserPicImage(ImageView imageView, String url) {
        if (!isEmpty(url)) {
            if (!url.equals(imageView.getTag())) {
                ImageManager.imageLoader.displayImage(App.readServerAppInfo()
                        .getServerThumbnail(url), imageView, ImageManager
                        .getOptionsPortrait());
                imageView.setTag(url);
            }
        } else {
            ImageManager.imageLoader.displayImage(null, imageView,
                    ImageManager.getOptionsPortrait());
            imageView.setTag(null);
        }
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

    public static void showCustomActionbar(Activity activity, View customView) {
        ActionBar actionBar = ((ActionBarActivity)activity).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(customView);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static ProgressDialog showDialog(Context context, String msg) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(msg);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.show();
        return dialog;
    }

    public static void showNormalActionBar(Activity activity) {
        ActionBar actionBar = ((ActionBarActivity)activity).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowCustomEnabled(false);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static void showStoryTranslateAlertDialog(final Context context,
                                                     int tips, int message, int negativeBtnTitle, int positiveBtnTitle,
                                                     final Class<?> cls) {
        AlertDialog dialog = new CustomDialog(context)
                .setTitle(context.getString(tips))
                .setMessage(context.getString(message))
                        // 设置内容
                .setNegativeButton(context.getString(negativeBtnTitle),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        })
                .setPositiveButton(context.getString(positiveBtnTitle),// 设置确定按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent intent = new Intent(context, cls);
                                context.startActivity(intent);
                            }
                        });// 创建
        // 显示对话框
        dialog.show();
    }

    public static boolean showUserStoryTranslateBtn(UserPhoto mUserPhoto) {
        String lang = mUserPhoto.getLang();
        if (!Utils.isEmpty(lang)) {
            List<String> allLangs = App.readUser().getAllLangs();
            return allLangs.size() > 1 && allLangs.contains(lang);
        }
        return false;

    }

    public static String standardizeEmail(String contacts) {
        // 正规化
        String[] re = new String[]{" ", "<", ">", ",", ";", "+"};
        for (String r : re) {
            contacts = contacts.replace(r, "");
        }
        return contacts;
    }

    public static String standardizeMobile(String phone) {
        // 正规化
        phone = phone.replace("-", "");
        phone = phone.replace(" ", "");
        if (phone.startsWith("82") || phone.startsWith("81")) {
            phone = phone.substring(0, 2) + "0" + phone.substring(2);
        }
        if (phone.startsWith("+82") || phone.startsWith("+81")) {
            phone = phone.substring(0, 3) + "0" + phone.substring(3);
        }

        if (phone.length() >= 11) {
            phone = phone.substring(phone.length() - 11);
        } else {
            return null;
        }
        return phone;
    }

    public static boolean strIsNum(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 商业上的计算，除掉空格，和标点符号，的长度。
     *
     * @param text
     * @return
     */
    public static int textLength(String text) {
        text = text.replaceAll("[a-zA-Z]+", "A");
        text = text.replaceAll("[0-9]+", "A");

        String[] re = new String[]{" ", "　", ".", "?", "？"};
        for (String r : re) {
            text = text.replace(r, "");
        }

        return text.length();
    }

    public static void toastPhotoLikeResult(Context context,
                                            UserPhoto mUserPhoto) {
        int resId;
        if (mUserPhoto.getFavorite() == 0) {
            resId = R.string.point_subtract_by_unlike;
        } else {
            resId = R.string.point_add_by_like;
        }

        Toast.makeText(
                context,
                context.getString(resId, mUserPhoto.getFullname(),
                        App.readServerAppInfo().point_by_photo_like),
                Toast.LENGTH_SHORT).show();
    }

    public static void toastTranslateLikeResult(Context context,
                                                StoryTranslate storyTranslate) {
        int resId;
        if (storyTranslate.getFavorite() == 0) {
            resId = R.string.point_subtract_by_unlike;
        } else {
            resId = R.string.point_add_by_like;
        }

        String username = storyTranslate.getFullname();
        if (!Utils.isEmpty(username))
            Toast.makeText(
                    context,
                    context.getString(resId, username,
                            App.readServerAppInfo().point_by_translate_like),
                    Toast.LENGTH_SHORT).show();
    }

    private static boolean tts(Context context, String lang, String content) {
        try {
            App.tts.speak(content, TextToSpeech.QUEUE_FLUSH, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean tts(Context context, String lang, String lang2,
                              String content) {
        // en 优先级靠后
        if ("en".equals(lang)) {
            String tmp = lang;
            lang = lang2;
            lang2 = tmp;
        }

        return tts(context, lang, content) || tts(context, lang2, content);
    }

    public static void updateFriendLastUpdatedate() {
        String lastUpdatedate = App.friendDAO.getLastUpdatedate();
        if (lastUpdatedate != null) {
            last_friend_updatedate = lastUpdatedate;
        }
    }

    public static void uploadVoiceFile(final Context context,
                                       final String file_path, final Handler myHandler) {
        // server端 文件没有带.amr
        final String fUrl = App.readServerAppInfo().getServerVoice(file_path);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                File voiceFile = new File(Utils.getVoiceFolder(context),
                        file_path + AppPreferences.VOICE_SURFIX);
                boolean result = Utils.saveFileFromServer(fUrl, voiceFile);
                if (myHandler != null) {
                    android.os.Message msg = myHandler.obtainMessage();
                    Bundle b = new Bundle();
                    if (result) {
                        b.putString("url", fUrl);
                    } else {
                        b.putString("url", null);
                    }
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                }
            }
        };
        new Thread(runnable).start();
    }

    public static String getOF_username(long ttalkid){
        return String.format("chinatalk_%d", ttalkid);
    }

    public static String getFriendNameFromOF_JID(String jid){
        String pattern = "chinatalk_";
        String name = "TTTalk.org";
        if (jid.contains(pattern)) {
            long friend_id = Long.parseLong(jid.substring("chinatalk_".length(), jid.indexOf("@")));
            Friend friend = App.friendDAO.fetchFriend(App.readUser().getId(), friend_id);
            name = friend.getFriend_nickname();
            if (friend == null || Utils.isEmpty(name)) {
                User user = App.userDAO.fetchUser(Long.valueOf(friend_id));
                if (user != null)
                    name = user.getFullname();
            }
        }

        if (Utils.isEmpty(name))
            name = "TTTalk.org";
        return name;
    }
}