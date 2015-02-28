package com.ruptech.chinatalk.utils;

public class AppPreferences {
	// 5 seconds
	public static final long RETRIEVE_INTERVAL_MESSAGE_FAST = 31 * 1000;
	// 29 seconds
	public static final long RETRIEVE_INTERVAL_MESSAGE_SLOW = 2 * 60 * 1000;
	// 11 minitus
	public static final long RETRIEVE_INTERVAL_MESSAGE_VERY_SLOW = 11 * 60 * 1000;

	public static final long RETRIEVE_INTERVAL_FRIENDS_REQUEST = 30 * 1000;// 30seconds

	public static final int REFRESH_INTERVAL_MILLIS = 10 * 1000;// 10seconds

	public static long[] NOTIFICATION_VIBRATE = new long[] { 0, 200, 100, 200 };// 数组是以毫秒为单位的暂停、震动、暂停……时间

	final public static String[] SERVER_BASE_URL = {
			"http://app.tttalk.org/tttalk150214/",
			"http://115.68.24.72/tttalk150214/" };

	public static final String SERVER_BASE_VERSION = "v1.4/";

	public static final int MESSAGE_STATUS_SEND_FAILED = -1;
	public static final int MESSAGE_STATUS_BEFORE_SEND = 0;
	public static final int MESSAGE_STATUS_REQUEST_TRANS = 1;
	public static final int MESSAGE_STATUS_TRANSLATING = 2;
	public static final int MESSAGE_STATUS_TRANSLATED = 3;
	public static final int MESSAGE_STATUS_FEEDBACKED = 4;
	public static final int MESSAGE_STATUS_PASSED = 5;
	public static final int MESSAGE_STATUS_GIVEUP = 6;
	public static final int MESSAGE_STATUS_ACCEPT_TRANSLATE = 7;
	public static final int MESSAGE_STATUS_NO_TRANSLATE = 8;
	public static final int MESSAGE_STATUS_ACCEPT_TRANSLATING = 9;

	public static final int VERIFY_STATUS_REQUEST = 1;
	public static final int VERIFY_STATUS_MISTAKE = 2;
	public static final int VERIFY_STATUS_RIGHT = 3;

	public static int MAX_INPUT_LENGTH = 200;
	public static int MAX_TRANSLATE_INPUT_LENGTH = 600;
	final public static int PAGE_COUNT_20 = 20;
	// 临时的 1000条
	public static int MAX_MSG_GC_NUM = 3000;
	public static int MAX_USER_PHOTO_GC_NUM = 20;

	// 验证码重发计时 900 seconds
	public static final int SEND_SMS_CODE_REPEAT = 900;

	// 账户余额底线
	public static int MINI_BALANCE = 130;
	public static int INI_BALANCE = 1000;

	public static final int MESSAGE_TYPE_TEXT = 1;
	public static final int MESSAGE_TYPE_VOICE = 2;
	public static final int MESSAGE_TYPE_PHOTO = 3;
	public static final int MESSAGE_TYPE_NONE = -1;

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

	// 注册、添加好友添加‘默认选择语言’
	public static final String SPINNER_DEFAULT_LANG = "LANGUAGE_UNSELETED";
	public static final String SPINNER_ADDFRIEND_DEFAULT_LANG = "LANGUAGE_UNSELETED2";

	// 公开用户
	public static final int USERS_GENDER_MALE = 1;
	public static final int USERS_GENDER_FEMALE = 2;
	public static final int USERS_GENDER_UNKNOW = 0;

	// 登陆选择方式：手机/Email
	public static final int LOGIN_SELECT_MODE_TEL = 1;
	public static final int LOGIN_SELECT_MODE_EMAIL = 2;

	// 好友添加方式
	public static final String FRIEND_ADD_METHOD_BY_SERVICE = "service";

	public static final String MESSAGE_TYPE_NAME_TEXT = "text";
	public static final String MESSAGE_TYPE_NAME_VOICE = "voice";
	public static final String MESSAGE_TYPE_NAME_PHOTO = "photo";
	public static final String MESSAGE_TYPE_NAME_NONE = "none";

	public static final String ALIPAY_PARTNER = "2088311842716681";

	public static final String ALIPAY_SELLER = "info@tttalk.org";

	public static final String ALIPAY_RSA_PRIVATE = "MIICXgIBAAKBgQDTXzvhTtevNDOxEyvIfUBmQ5dhapLWbnX3wMu2WvM4Wu6h3XKSvowYGmcjA1uc2eDR4eGlmU8oAzqFZJB7LdbnRnJKXvsVKZ7ZTmGmgQSdMJMubgVlqJEPqm6Hs7KL9c46w63TKhDcIawtu9MkRVDZaWNDlZ/rY/jToBOlB7+z6wIDAQABAoGBAKkvnDVurzM83HdK+gujPa3dQkkmTdw3VFN3zVbsG6wrFMEZCMEupeIRGCatZGH6/3nfjIbJXNORKgFlikQeX9kPIdqasMmwRGlkWL+DnU6oxN+5Gcip72wIbrpV+RuoelmYH2jPeHj7l5xZRFgbJv6bzb3LCKl1wVYgrYm782bBAkEA/JdKPCPRhjcnXGpE2WWwQQYNqvqrcFxLBNYkHxzA9JQivkbUgtR1oaPCzNuHXWz7s18x7Qh+6nNLIfCz7cKuSwJBANY5h/L1nfwlWcGpW5KBTmwIUi8EudccB9fGR00aVLdIQ/rCNK1ycP4B1GoYiue/goQTQhI0z++OCPftlu7LDOECQQDFqkcnpQgUNhkRUwAp+E/jsq8DfEKpHTB/ymxeBIxjWYGO0bL/5u9e3N5WnfzieaTHC9nwGlPneO30036as1zdAkBCuaazsXdk+0lKvfKM3oPLuIfIp2MvMkbrGZJJ3MK4V+T8rRL1V2kEZROBfGV/q7H+a3Uv8I0343i9qJ5TgKFhAkEA1AA7ZjiLusgEd26mCCwuKYF1YBTRvSn2QI0LqkFavhCtFA7TZE3mKTyoTWwyp3dXg1pmGuiEqEEYZhtfujmfAw==";

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

	// 第三方插件类别
	public static final String THIRD_PARTY_TYPE_QQ = "QQ";
	public static final String THIRD_PARTY_TYPE_GOOGLE = "GOOGLE";
	public static final String THIRD_PARTY_TYPE_SINA = "SINA";
	public static final String THIRD_PARTY_TYPE_FACEBOOK = "FACEBOOK";
	public static final String THIRD_PARTY_TYPE_WECHAT = "WECHAT";

	public static final String THIRD_PARTY_TYPE_TENCENT_QQ = "TENCENT_QQ";
	public static final String THIRD_PARTY_TYPE_TENCENT_QQ_LITE = "TENCENT_QQ_LITE"; // QQ轻聊版
	public static final String THIRD_PARTY_TYPE_TENCENT_QQ_INTERNATIONAL = "TENCENT_QQ_INTERNATIONAL"; // QQ国际版
	public static final String THIRD_PARTY_TYPE_TENCENT_WECHAT = "TENCENT_WECHAT";

	public static final String THIRD_PARTY_TYPE_GOOGLE_PLUS = "GOOGLE_PLUS";

	public static final String THIRD_PARTY_TYPE_SINA_WEIBO = "SINA_WEIBO";

	public static final String THIRD_PARTY_TYPE_KAKAO = "KAKAO_TALK";
	public static final String THIRD_PARTY_TYPE_LINE = "NAVER_LINE";

	public static final String THIRD_PARTY_ACCESS_TOKEN = "third_party_access_token";
	public static final String THIRD_PARTY_TYPE = "third_party_type";
	public static final String THIRD_PARTY_USER_ID = "third_party_user_id";

	// 第三方分享
	public static final String ADD_FRIEND_FROM_CONTACT = "CONTACT_FRIEND";// 通讯录好友
	public static final String SHARE_TO_QQ = "QQ";// QQ
	public static final String SHARE_TO_TQQ = "TQQ";// 腾讯微博
	public static final String SHARE_TO_QZONE = "QZONE";// QQ空间
	public static final String SHARE_TO_SINA_WEIBO = "SINA_WEIBO";// 新浪微博
	public static final String SHARE_TO_FACEBOOK = "FACEBOOK";// FACEBOOK
	public static final String SHARE_TO_WECHAT_MEMENTS = "WECHAT_MEMENTS";// FACEBOOK
	public static final String SHARE_TO_GOOGLE_PLUS = "GOOGLE_PLUS";// Google+

	public static final int FREE_RECHARGE_SUCEESS = 1;
	public static final int FREE_RECHARGE_INTERVAL_TIME = 2;
	public static final int FREE_RECHARGE_FAILED = 3;

	public static final int REQUEST_ADD_FRIEND = 1;
	public static final int REQUEST_ACCEP_FRIEND = 2;

	public static final int LATE6_IMPOSSIBLE = 200 * 1000000;
	public static final int LNGE6_IMPOSSIBLE = 200 * 1000000;

	public static final String LANGUAGE_CN = "CN,ZH-CN,ZH";
	public static final String LANGUAGE_EN = "EN";
	public static final String LANGUAGE_KR = "KR,KO,KO-KR";
	public static final String LANGUAGE_JP = "JP,JA";
	public static final String LANGUAGE_DEFAULT = "EN";

	public static final String[] LanguageArray = { LANGUAGE_CN, LANGUAGE_EN,
			LANGUAGE_KR, LANGUAGE_JP };

	// story list 上的翻译(popular、相册)
	public static final int STORY_LIST_TRANSLATE_TYPE = 1;
	// story comment header上面的翻译
	public static final int STORY_COMMENT_HEADER_TRANSLATE_TYPE = 2;
	// story comment list 上面的翻译
	public static final int STORY_COMMENT_LIST_TRANSLATE_TYPE = 3;

	// 人工翻译：翻译贴图文字
	public static final int HUMAN_TRANSLATOR_ID = 3635;
	
	public static long ID_IMPOSSIBLE = Long.MAX_VALUE;

	public static final String APK_DOWNLOAD_URL = "http://www.tttalk.org/download.php";
}
