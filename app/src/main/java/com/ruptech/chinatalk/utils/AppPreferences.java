package com.ruptech.chinatalk.utils;

public class AppPreferences {

    public static final String IM_SERVER_RESOURCE = "im.dlmu.edu.cn";
    final public static int PAGE_COUNT_20 = 20;
    // 验证码重发计时 900 seconds
    public static final int SEND_SMS_CODE_REPEAT = 900;
    // 注册、添加好友添加‘默认选择语言’
    public static final String SPINNER_DEFAULT_LANG = "LANGUAGE_UNSELETED";
    public static final String SPINNER_ADDFRIEND_DEFAULT_LANG = "LANGUAGE_UNSELETED2";
    // 公开用户
    public static final int USERS_GENDER_MALE = 1;
    public static final int USERS_GENDER_FEMALE = 2;
    // 好友添加方式
    public static final String FRIEND_ADD_METHOD_BY_SERVICE = "service";
    public static final String MESSAGE_TYPE_NAME_TEXT = "text";
    public static final String APK_SECRET = "2a9304125e25edaa5aff574153eafc95c97672c6";
    public static final int AUTO_TRANSLATE_MESSSAGE = 1;
    public static final int USER_ACTIVE_STATUS = 1;
    public static final int FRIEND_BLOCK_STATUS = -1;
    public static final int FRIEND_NOT_ADD_STATUS = 0;
    public static final int FRIEND_ADDED_STATUS = 1;
    public static final String VOICE_SURFIX = ".amr";
    public static final long SYSTEM_REQUEST_TO_USERID = 20;
    public static final long TTT_REQUEST_TO_USERID = 0;
    public static final long STORY_REQUEST_TO_USERID = 10;
    public static final int LATE6_IMPOSSIBLE = 200 * 1000000;
    public static final int LNGE6_IMPOSSIBLE = 200 * 1000000;
    public static final String LANGUAGE_CN = "CN,ZH-CN,ZH";
    public static final String LANGUAGE_EN = "EN";
    public static final String LANGUAGE_KR = "KR,KO,KO-KR";
    public static final String LANGUAGE_JP = "JP,JA";
    public static final String LANGUAGE_DEFAULT = "EN";
    public static final String[] LanguageArray = {LANGUAGE_CN, LANGUAGE_EN,
            LANGUAGE_KR, LANGUAGE_JP};
    public static long[] NOTIFICATION_VIBRATE = new long[]{0, 200, 100, 200};// 数组是以毫秒为单位的暂停、震动、暂停……时间
    public static int MAX_INPUT_LENGTH = 200;
    public static int MAX_TRANSLATE_INPUT_LENGTH = 600;
    // 临时的 1000条
    public static int MAX_MSG_GC_NUM = 3000;
    public static int MAX_USER_PHOTO_GC_NUM = 20;
    // 账户余额底线
    public static int MINI_BALANCE = 130;
    public static int INI_BALANCE = 1000;
    // 朋友推荐 上传通讯录间隔时间 天
    public static long RECOMMENDED_FRIENDS_INTERVAL = 30L * 24 * 60 * 60 * 1000;
    public static int RECOMMENDED_FRIENDS_TO_NEW_USER_FLAG = -1;// 用于新朋友里面，区分不同类型的User
    // crouton message 获取的间隔时间
    public static long RECOMMENDED_GUIDE_INTERVAL = 1000 * 60 * 60 * 24 * 14; // 两周
    // Main画面信息延迟更新的时间间隔
    public static long POST_DELAYED_MILLIS = 1000 * 30; // 30秒
    public static long VERSION_CHECK_INTERVAL = 1000 * 60 * 60 * 24 * 2;// 每2天检查更新一次
    // Popular 刷新频率
    public static long POPULAR_CHECK_INTERVAL = 1000 * 60 * 60 * 2;// 每2小时检查更新一次

    //user property keys
    public static String USER_PROPERTY_KEY_PORTRAIT = "portrait";

}
