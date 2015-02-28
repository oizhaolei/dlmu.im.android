package com.ruptech.chinatalk.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserSignupCheckTask;
import com.ruptech.chinatalk.task.impl.WeChatGetAccessTokenTask;
import com.ruptech.chinatalk.thirdparty.qq.QQBaseUIListener;
import com.ruptech.chinatalk.thirdparty.qq.QQUtil;
import com.ruptech.chinatalk.thirdparty.sina.SinaAccessTokenKeeper;
import com.ruptech.chinatalk.thirdparty.sina.SinaUtil;
import com.ruptech.chinatalk.thirdparty.wechat.WeChatUtil;
import com.ruptech.chinatalk.ui.user.SignupProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.ThirdPartyUtil;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.EditTextWithDel;
import com.ruptech.chinatalk.wxapi.WXEntryActivity;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.ErrorInfo;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.connect.UserInfo;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginSignupActivity extends ActionBarActivity implements
        SwipeRefreshLayout.OnRefreshListener {
    /**
     * 登入按钮的监听器，接收授权结果。
     */
    private class SinaAuthListener implements WeiboAuthListener {
        @Override
        public void onCancel() {
            Toast.makeText(LoginSignupActivity.this, "CANCEL",
                    Toast.LENGTH_SHORT).show();
            Utils.dismissDialog(progressDialog);
        }

        @Override
        public void onComplete(Bundle values) {

            Oauth2AccessToken accessToken = Oauth2AccessToken
                    .parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {

                SinaAccessTokenKeeper.writeAccessToken(getApplicationContext(),
                        accessToken);

                UsersAPI mUsersAPI = new UsersAPI(accessToken);
                long uid = Long.parseLong(accessToken.getUid());
                mUsersAPI.show(uid, sinaApiListener);

                // String userInfo = "{\n" +
                // "uid:" + accessToken.getUid()
                // + ", token:" + accessToken.getToken()
                // + ", refreshToken:" + accessToken.getRefreshToken()
                // + ", expirestime:" + accessToken.getExpiresTime()
                // + "\n}";
                // Log.d("SINA WEIBO USER :", userInfo);
                // LoginUtil.showResultDialog(LoginActivity.this,
                // userInfo, "SINA WEIBO USER");
                // LoginUtil.dismissDialog();
                // excuteThirdPartyUserLoginTask(accessToken.getUid(), "",
                // accessToken.getToken());
            } else {
                // 当您注册的应用程序签名不正确时，就会收到错误Code，请确保签名正确
                String code = values.getString("code", "");
                Toast.makeText(LoginSignupActivity.this, code,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LoginSignupActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected static void setUserNameEditText(String mUsername) {
        if (instance != null)
            instance.mUserNameEditText.setText(mUsername);
    }

    @InjectView(R.id.activity_username_edittext)
    EditTextWithDel mUserNameEditText; // 帐号编辑框
    @InjectView(R.id.activity_next_button)
    Button nextBtn;

    @InjectView(R.id.activity_password_edittext)
    EditTextWithDel mPasswordEditText; // 密码编辑框

    @InjectView(R.id.activity_forget_passwd_textview)
    TextView findPasswordText; // 密码编辑框

    protected static final String EXTRA_TYPE = "LOGIN";
    protected static final String EXTRA_TYPE_LOGIN = "LOGIN";
    protected static final String EXTRA_TYPE_SIGNUP = "SIGNUP";

    private final String TAG = Utils.CATEGORY
            + LoginSignupActivity.class.getSimpleName();

    public static LoginSignupActivity instance;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    private String mUsername;

    /**
     * 微博 OpenAPI 回调接口。
     */
    private final RequestListener sinaApiListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i(TAG, response);
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
                    String userInfo = "{\n" + "avatar_hd:" + user.avatar_hd
                            + ", avatar_large:" + user.avatar_large
                            + ", bi_followers_count:" + user.bi_followers_count
                            + ", block_word:" + user.block_word + ", city:"
                            + user.city + ", created_at:" + user.created_at
                            + ", description:" + user.description + ", domain:"
                            + user.domain + ", favourites_count:"
                            + user.favourites_count + ", followers_count:"
                            + user.followers_count + ", friends_count:"
                            + user.friends_count + ", follow_me:"
                            + user.follow_me + ", following:" + user.following
                            + ", gender:" + user.gender + ", id:" + user.id
                            + ", idstr:" + user.idstr + ", lang:" + user.lang
                            + ", location:" + user.location + ", mbrank:"
                            + user.mbrank + ", mbtype:" + user.mbtype
                            + ", name:" + user.name + ", online_status:"
                            + user.online_status + ", profile_image_url:"
                            + user.profile_image_url + ", profile_url:"
                            + user.profile_url + ", province:" + user.province
                            + ", remark:" + user.remark + ", screen_name:"
                            + user.screen_name + ", star:" + user.star
                            + ", statuses_count:" + user.statuses_count
                            + ", url:" + user.url + ", verified_reason:"
                            + user.verified_reason + ", verified_type:"
                            + user.verified_type + ", weihao:" + user.weihao
                            + "\n}";
                    if (BuildConfig.DEBUG) {
                        Log.d("SINA WEIBO USER :", userInfo);
                    }
                    // LoginUtil.showResultDialog(LoginActivity.this,
                    // userInfo, "SINA WEIBO USER");
                    ThirdPartyUtil.dismissDialog();

                    Oauth2AccessToken accessToken = SinaAccessTokenKeeper
                            .readAccessToken(instance);
                    App.sinaOauth2AccessToken = accessToken;
                    excuteThirdPartyUserLoginTask(accessToken.getUid(),
                            user.screen_name, user.profile_image_url,
                            accessToken.getToken());
                } else {
                    Toast.makeText(LoginSignupActivity.this, response,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            LogUtil.e(TAG, "Exception: " + e.getMessage());
            ErrorInfo info = ErrorInfo.parse(e.getMessage());
            Toast.makeText(LoginSignupActivity.this, info.toString(),
                    Toast.LENGTH_LONG).show();
        }
    };

    @InjectView(R.id.sina_login_btn)
    com.sina.weibo.sdk.widget.LoginButton sinaLoginButton;
    //	@InjectView(R.id.google_login_btn)
//	com.google.android.gms.common.SignInButton googleLoginButton;
    @InjectView(R.id.facebook_login_btn)
    LoginButton facebookLoginButton;

    /**
     * 该按钮用于记录当前点击的是哪一个 Button，用于在 函数中进行区分。
     */
    private static final int QQ_LOGIN_BUTTON = 1;

    private static final int FACEBOOK_LOGIN_BUTTON = 2;

    private static final int GOOGLE_LOGIN_BUTTON = 3;

    private static final int SINA_LOGIN_BUTTON = 4;

    private static final int WECHAT_LOGIN_BUTTON = 5;

    private Bundle savedInstanceState;

    // public static QQAuth mQQAuth;
    private Tencent mTencent;

    private GraphUser facebook_user;

    private int currentButton = 0;

//	private PlusClient mPlusClient;

    public static String mQQAppid;
    public String wechat_code;
    public String wechat_openid;
    public String wechat_access_token;

    public String wechat_nickname;
    public String wechat_headimgurl;
    private UiLifecycleHelper uiHelper;
    //	private ConnectionResult mConnectionResult;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private static final int NOT_CONNECTED = 0;
    private static final int IS_CONNECTING = 1;
    private static final int IS_CONNECTED = 2;
    public static ProgressDialog progressDialog;
    private int googleConnectionStatus;
    private final OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            progressDialog = Utils.showDialog(instance,
                    getString(R.string.please_waiting));
            if (v instanceof com.facebook.widget.LoginButton) {
                instance.facebook_login(v);
            } else if (v instanceof com.sina.weibo.sdk.widget.LoginButton) {
                instance.sina_login(v);
//			} else if (v instanceof com.google.android.gms.common.SignInButton) {
//				instance.google_login(v);
            }
        }
    };

    private final TaskListener mWeChatAccessTokenTaskListener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                excuteThirdPartyUserLoginTask(wechat_openid, wechat_nickname,
                        wechat_headimgurl, wechat_access_token);
            } else {
                String msg = task.getMsg();
                Toast.makeText(LoginSignupActivity.this, msg,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPreExecute(GenericTask task) {
            //
        }

    };

    @InjectView(R.id.activity_login_signup_thirdpaty_qq_layout)
    View thirdpatyQQView;

    @InjectView(R.id.activity_login_signup_thirdpaty_weibo_layout)
    View thirdpatyWeiboView;

    @InjectView(R.id.activity_login_signup_thirdpaty_facebook_layout)
    View thirdpatyFacebookView;

    @InjectView(R.id.activity_login_signup_thirdpaty_gmail_layout)
    View thirdpatyGmailView;

    @InjectView(R.id.activity_login_signup_thirdpaty_wechat_layout)
    View thirdpatyWeChatView;

    private String selectType;

    private final TaskListener mSignupCheckTaskListener = new TaskAdapter() {

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.OK) {
                UserSignupCheckTask userSignupCheckTask = (UserSignupCheckTask) task;
                boolean isThirdParty = userSignupCheckTask.isThirdParty();
                com.ruptech.chinatalk.model.User user = userSignupCheckTask
                        .getUser();
                if (user != null && user.getActive() == 1) {
                    if (isThirdParty) {
                        App.writeUser(user);
                        Intent intent = new Intent(instance,
                                LoginLoadingActivity.class);
                        startActivity(intent);
                    } else {
                        Utils.dismissDialog(progressDialog);
                        showErrorInformationDialog(R.string.already_exists_user_pls_change_tel);
                    }
                } else {
                    Utils.dismissDialog(progressDialog);
                    Intent intent = new Intent(instance,
                            SignupProfileActivity.class);
                    intent.putExtra(SignupProfileActivity.EXTRA_USER, mTempUser);
                    startActivity(intent);
                }
            } else {
                String msg = task.getMsg();
                onUserSignupCheckFailure(msg);
            }
        }

        @Override
        public void onPreExecute(GenericTask task) {
            onUserSignupCheckBegin();
        }

    };

    private com.ruptech.chinatalk.model.User mTempUser;

    private GenericTask userSignupCheckTask;

    public void doLoginFacebook(View v) {
        facebookLoginButton.performClick();
    }

//	public void doLoginGoogle(View v) {
//		// googleLoginButton.performClick();
//		buttonClickListener.onClick(googleLoginButton);
//	}

    @OnClick(R.id.activity_login_signup_thirdpaty_qq_layout)
    public void doLoginQQ(View v) {
        progressDialog = Utils.showDialog(this,
                getString(R.string.please_waiting));
        this.qq_login(v);
    }

    @OnClick(R.id.activity_login_signup_thirdpaty_weibo_layout)
    public void doLoginSina(View v) {
        sinaLoginButton.performClick();
    }

    public void doLoginWechat(View v) {
        progressDialog = Utils.showDialog(this,
                getString(R.string.please_waiting));
        this.wechat_login(v);
    }

    @OnClick(R.id.activity_next_button)
    public void doNext(View v) {
        mUsername = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        mUsername = mUsername.replace(" ", "");
        mUsername = mUsername.toLowerCase(Locale.getDefault());

        mTempUser = new com.ruptech.chinatalk.model.User();
        mTempUser.setTel(mUsername);
        mTempUser.setPassword(password);

        if (Utils.isEmpty(mUsername)) {
            showErrorInformationDialog(R.string.please_input_email_or_telphone);
        } else if (selectType.equals(EXTRA_TYPE_LOGIN)
                && !Utils.isMail(mUsername) && !Utils.isTelphone(mUsername)) {
            showErrorInformationDialog(R.string.please_right_input_email_or_telphone);
        } else if (selectType.equals(EXTRA_TYPE_SIGNUP)
                && !Utils.isMail(mUsername)) {
            showErrorInformationDialog(R.string.email_input_has_blank);
        } else if (Utils.isEmpty(password)) {
            showErrorInformationDialog(R.string.pwd_is_null);
        } else {
            if (selectType.equals(EXTRA_TYPE_LOGIN)) {
                progressDialog = Utils.showDialog(instance,
                        getString(R.string.please_waiting));
                gotoLoginLoadingActivity(mUsername, password);
            } else {
                doUserSignupCheckTask(mUsername, false);
            }
        }
    }

    private void doUserSignupCheckTask(String tel, boolean isThirdParty) {
        if (userSignupCheckTask != null
                && userSignupCheckTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        }
        userSignupCheckTask = new UserSignupCheckTask(tel, isThirdParty);
        userSignupCheckTask.setListener(mSignupCheckTaskListener);
        userSignupCheckTask.execute();
    }

    private void excuteThirdPartyUserLoginTask(String tel, String fullname,
                                               String file_path, String access_token) {

        String type = "";
        if (currentButton == QQ_LOGIN_BUTTON) {
            type = AppPreferences.THIRD_PARTY_TYPE_QQ;
        } else if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            type = AppPreferences.THIRD_PARTY_TYPE_FACEBOOK;
        } else if (currentButton == GOOGLE_LOGIN_BUTTON) {
            type = AppPreferences.THIRD_PARTY_TYPE_GOOGLE;
        } else if (currentButton == SINA_LOGIN_BUTTON) {
            type = AppPreferences.THIRD_PARTY_TYPE_SINA;
        } else if (currentButton == WECHAT_LOGIN_BUTTON) {
            type = AppPreferences.THIRD_PARTY_TYPE_WECHAT;
        }

        PrefUtils.savePrefThirdPartyAccess(type, access_token, tel);
        tel = type + ":" + tel;

        mTempUser = new com.ruptech.chinatalk.model.User();
        mTempUser.setTel(tel);
        mTempUser.setFullname(fullname);
        mTempUser.setLang(Utils.getUserLanguage());
        mTempUser.setPic_url(file_path);
        mTempUser.setPassword("password");

        doUserSignupCheckTask(tel, true);
    }

    public void excuteWeChatAccessTokenTask(String code) {

        WeChatGetAccessTokenTask mWeChatAccessTokenTask = new WeChatGetAccessTokenTask(
                code);
        mWeChatAccessTokenTask.setListener(mWeChatAccessTokenTaskListener);
        mWeChatAccessTokenTask.execute();
    }

    private void extras() {
        Bundle extras = getIntent().getExtras();
        selectType = extras.getString(EXTRA_TYPE);
    }

    // facebook 登录
    public void facebook_login(View v) {
        currentButton = FACEBOOK_LOGIN_BUTTON;

        if (uiHelper == null) {
            Session.StatusCallback statusCallback = new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state,
                                 Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            };
            uiHelper = new UiLifecycleHelper(this, statusCallback);
            uiHelper.onCreate(savedInstanceState);
        }
    }

    private void facebookHandle() {
        GraphUser user = this.facebook_user;
        if (user != null) {
            String userInfo = "{\n" + "id:" + user.getId() + ", username:"
                    + user.getUsername() + ", name:" + user.getName()
                    + ", firstname:" + user.getFirstName() + ", middlename:"
                    + user.getMiddleName() + ", lastName:" + user.getLastName()
                    + ", link:" + user.getLink() + ", birthday:"
                    + user.getBirthday() + ", location:" + user.getLocation()
                    + "\n}";
            if (BuildConfig.DEBUG) {
                Log.d("FACEBOOK USER :", userInfo);
            }
            // LoginUtil.showResultDialog(LoginActivity.this,
            // userInfo, "FACEBOOK USER");
            ThirdPartyUtil.dismissDialog();
            String file_path = "http://graph.facebook.com/" + user.getId()
                    + "/picture?type=large";

            excuteThirdPartyUserLoginTask(user.getId(), user.getName(),
                    file_path, App.facebookSession.getAccessToken());
        }
    }

    @OnClick(R.id.activity_forget_passwd_textview)
    public void findPassword(View v) { // 忘记密码按钮
        mUsername = mUserNameEditText.getText().toString();
        mUsername = mUsername.replace(" ", "");
        mUsername = mUsername.toLowerCase(Locale.getDefault());

        Intent intent = new Intent(instance, FindPasswordActivity.class);
        intent.putExtra(FindPasswordActivity.PREF_USERINFO_NAME, mUsername);
        startActivity(intent);
    }

    private void getQQUserInfoByToken() {
        UserInfo qqInfo = new UserInfo(this, mTencent.getQQToken());
        if (QQUtil.qqIsReady(this, mTencent)) {
            qqInfo.getUserInfo(new QQBaseUIListener(this, "get_user_info") {
                @Override
                public void doComplete(JSONObject values) {
                    if (BuildConfig.DEBUG) {
                        Log.d("QQ USER :", values.toString());
                    }

                    // LoginUtil.showResultDialog(LoginActivity.this,
                    // values.toString(), "QQ USER");
                    ThirdPartyUtil.dismissDialog();
                    try {
                        String nickname;
                        String file_path;
                        try {
                            nickname = values.optString("nickname");
                            file_path = values.optString("figureurl_qq_2",
                                    values.optString("figureurl_qq_1"));
                        } catch (Exception e) {
                            nickname = "QQ_USER";
                            file_path = "";
                        }
                        // App.qqAuth = mQQAuth;
                        App.qqTencent = mTencent;
                        excuteThirdPartyUserLoginTask(mTencent.getQQToken()
                                .getOpenId(), nickname, file_path, mTencent
                                .getQQToken().getAccessToken());
                    } catch (Exception e) {
                        Utils.sendClientException(e);
                    }
                }
            });
            ThirdPartyUtil.showProgressDialog(this, null, null);
        } else {
            Utils.dismissDialog(progressDialog);
        }
    }

    // google plus 登录
//	public void google_login(View v) {
//		currentButton = GOOGLE_LOGIN_BUTTON;
//
//		if (googleConnectionStatus == NOT_CONNECTED) {
//
//			int available = GooglePlayServicesUtil
//					.isGooglePlayServicesAvailable(this);
//			if (available != ConnectionResult.SUCCESS) {
//				GooglePlayServicesUtil.getErrorDialog(available, this, 0)
//						.show();
//				Utils.dismissDialog(progressDialog);
//			} else {
//				googleConnectionStatus = NOT_CONNECTED;
//				mPlusClient = new PlusClient.Builder(this, this, this)
//						.setActions(GoogleUtil.ACTIONS).build();
//				// 开始通讯
//				mPlusClient.connect();
//				googleConnectionStatus = IS_CONNECTING;
//
//				// 在未解决连接故障时，显示进度条。
//				if (!mPlusClient.isConnected()) {
//					if (mConnectionResult == null) {
//						ThirdPartyUtil.showProgressDialog(this, null, null);
//					} else {
//						try {
//							mConnectionResult.startResolutionForResult(this,
//									REQUEST_CODE_RESOLVE_ERR);
//						} catch (SendIntentException e) {
//							// 重新尝试连接。
//							mConnectionResult = null;
//							mPlusClient.connect();
//						}
//					}
//				}
//			}
//		}
//
//	}

    private void gotoLoginLoadingActivity(String mUsername, String password) {
        Intent intent = new Intent(instance, LoginLoadingActivity.class);
        intent.putExtra(LoginLoadingActivity.PREF_USERINFO_NAME, mUsername);
        intent.putExtra(LoginLoadingActivity.PREF_USERINFO_PASS, password);
        startActivity(intent);
    }

//	@Override
//	public void onAccessRevoked(ConnectionResult status) {
//		// 在取消关联之前，请运行 clearDefaultAccount()。
//		mPlusClient.clearDefaultAccount();
//
//		mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
//			@Override
//			public void onAccessRevoked(ConnectionResult status) {
//				// mPlusClient 现在已断开，并且访问权限已被撤消。
//				// 触发应用逻辑以确保遵守开发者政策
//			}
//		});
//	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
                @Override
                public void onComplete(FacebookDialog.PendingCall pendingCall,
                                       Bundle data) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Facebook Login", "Success!");
                    }

                }

                @Override
                public void onError(FacebookDialog.PendingCall pendingCall,
                                    Exception error, Bundle data) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Facebook Login",
                                String.format("Error: %s", error.toString()));
                    }

                }
            };
            uiHelper.onActivityResult(requestCode, resultCode, data,
                    dialogCallback);
//		} else if (currentButton == GOOGLE_LOGIN_BUTTON) {
//			if (requestCode == REQUEST_CODE_RESOLVE_ERR
//					&& resultCode == RESULT_OK) {
//				mConnectionResult = null;
//				mPlusClient.connect();
//			} else if (requestCode == REQUEST_CODE_RESOLVE_ERR
//					&& resultCode == RESULT_CANCELED) {
//				mPlusClient.disconnect();
//				googleConnectionStatus = NOT_CONNECTED;
//				ThirdPartyUtil.dismissDialog();
//			}
        } else if (currentButton == SINA_LOGIN_BUTTON) {

            sinaLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

//	@Override
//	public void onConnected(Bundle connectionHint) {
//		if (googleConnectionStatus == IS_CONNECTING) {
//
//			Person user = mPlusClient.getCurrentPerson();
//			if (user == null) {
//				// 在国内，有时会出现连接成功，但是无法取得帐户信息的情况
//				// 此时应视为没有连接
//				mPlusClient.disconnect();
//				googleConnectionStatus = NOT_CONNECTED;
//				ThirdPartyUtil.dismissDialog();
//
//				Utils.dismissDialog(progressDialog);
//				Toast.makeText(LoginSignupActivity.this,
//						R.string.google_service_connect_failed,
//						Toast.LENGTH_LONG).show();
//				return;
//			}
//			String userInfo = "{\n" + "id:" + user.getId() + ", name:"
//					+ user.getName() + ", aboutMe:" + user.getAboutMe()
//					+ ", birthday:" + user.getBirthday() + ", braggingRights:"
//					+ user.getBraggingRights() + ", circledByCount:"
//					+ user.getCircledByCount() + ", currentLocation:"
//					+ user.getCurrentLocation() + ", displayName:"
//					+ user.getDisplayName() + ", gender:" + user.getGender()
//					+ ", language:" + user.getLanguage() + ", nickname:"
//					+ user.getNickname() + ", objectType:"
//					+ user.getObjectType() + ", plusOneCount:"
//					+ user.getPlusOneCount() + ", relationshipStatus:"
//					+ user.getRelationshipStatus() + ", tagline:"
//					+ user.getTagline() + ", url:" + user.getUrl()
//					+ ", ageRange:" + user.getAgeRange() + ", cover:"
//					+ user.getCover() + ", image:" + user.getImage() + "\n}";
//			if (BuildConfig.DEBUG) {
//				Log.d("GOOGLE PLUS USER :", userInfo);
//			}
//
//			// LoginUtil.showResultDialog(LoginActivity.this,
//			// userInfo, "GOOGLE PLUS USER");
//			ThirdPartyUtil.dismissDialog();
//
//			App.googlePlusClient = mPlusClient;
//			excuteThirdPartyUserLoginTask(user.getId(), user.getDisplayName(),
//					user.getImage().getUrl(), "");
//		}
//		googleConnectionStatus = IS_CONNECTED;
//	}
//
//	@Override
//	public void onConnectionFailed(ConnectionResult result) {
//		// if (mConnectionProgressDialog.isShowing()) {
//		if (googleConnectionStatus == IS_CONNECTING) {
//			// The user clicked the sign-in button already. Start to resolve
//			// connection errors. Wait until onConnected() to dismiss the
//			// connection dialog.
//			if (result.hasResolution()) {
//				try {
//					result.startResolutionForResult(this,
//							REQUEST_CODE_RESOLVE_ERR);
//				} catch (SendIntentException e) {
//					mPlusClient.connect();
//				}
//			}
//		}
//		// Save the result and resolve the connection failure upon a user click.
//		mConnectionResult = result;
//	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        instance = this;
        extras();
        ButterKnife.inject(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SplashActivity.close();
        setupComponents();

        setupFacebook();
        setupQQ();
//        setupGoogle();
        setupSina();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            uiHelper.onDestroy();
        }
    }

//	@Override
//	public void onDisconnected() {
//		if (BuildConfig.DEBUG) {
//			Log.d("GOOGLE PLUS", "disconnected");
//		}
//
//		// mPlusClient.connect();
//		// googleConnectionStatus = IS_CONNECTING;
//		googleConnectionStatus = NOT_CONNECTED;
//	}

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
        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            uiHelper.onPause();
        }
    }

    @Override
    public void onRefresh() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            uiHelper.onResume();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
            uiHelper.onSaveInstanceState(outState);
        }
    }

    private void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Facebook Logged in...");
            App.facebookSession = session;
        } else if (state.isClosed()) {
            Log.i(TAG, "Facebook Logged out...");
            App.facebookSession = session;
        }

    }

//	@Override
//	protected void onStart() {
//		super.onStart();
//		if (currentButton == GOOGLE_LOGIN_BUTTON) {
//			if (mPlusClient != null) {
//				mPlusClient.connect();
//			}
//		}
//	}
//
//	@Override
//	protected void onStop() {
//		super.onStop();
//		if (currentButton == GOOGLE_LOGIN_BUTTON) {
//			if (mPlusClient != null) {
//				mPlusClient.disconnect();
//			}
//		}
//	}

    private void onUserSignupCheckBegin() {
        // Utils.showProgress(this, R.string.please_waiting);
    }

    private void onUserSignupCheckFailure(String msg) {
        // Utils.hideProgress(this);
        Utils.dismissDialog(progressDialog);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // qq 登录
    public void qq_login(View v) {
        currentButton = QQ_LOGIN_BUTTON;

        mQQAppid = QQUtil.appid;
        mTencent = Tencent.createInstance(mQQAppid, instance);

        mTencent.login(instance, "all", new QQBaseUIListener() {
            @Override
            public void doComplete(JSONObject values) {
                if (BuildConfig.DEBUG) {
                    Log.d("QQ USER :", values.toString());
                }
                getQQUserInfoByToken();
            }

            @Override
            public void onCancel() {
                Log.v("onCancel", "");
            }

            @Override
            public void onError(UiError e) {
                Log.v("onError:", "code:" + e.errorCode + ", msg:"
                        + e.errorMessage + ", detail:" + e.errorDetail);
            }
        });

    }

    private void setDefaultAccountText() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        for (Account account : accounts) {
            if ("com.google".equals(account.type)) {
                mUserNameEditText.setText(account.name);
                setPasswordFocusable();
                break;
            }
        }
    }

    private void setPasswordFocusable() {
        mPasswordEditText.setFocusable(true);
        mPasswordEditText.setFocusableInTouchMode(true);
        mPasswordEditText.requestFocus();
    }

    private void setupComponents() {
        if (!ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ)
                && !ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ_LITE)
                && !ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_QQ_INTERNATIONAL)) {
            thirdpatyQQView.setVisibility(View.GONE);
        }
        if (!ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_SINA_WEIBO)) {
            thirdpatyWeiboView.setVisibility(View.GONE);
        }
        if (!ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_FACEBOOK)) {
            thirdpatyFacebookView.setVisibility(View.GONE);
        }
        if (!ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_GOOGLE)) {
            thirdpatyGmailView.setVisibility(View.GONE);
        }
        if (!ThirdPartyUtil
                .isAvailableClient(AppPreferences.THIRD_PARTY_TYPE_TENCENT_WECHAT)) {
            thirdpatyWeChatView.setVisibility(View.GONE);
        }

        if (selectType.equals(EXTRA_TYPE_LOGIN)) {
            mUserNameEditText.setHint(R.string.please_input_email_or_telphone);
            getSupportActionBar().setTitle(R.string.login);
            nextBtn.setText(R.string.login);
            findPasswordText.setVisibility(View.VISIBLE);
        } else {
            mUserNameEditText.setHint(R.string.email_input_has_blank);
            getSupportActionBar().setTitle(R.string.signup);
            nextBtn.setText(R.string.next);
            findPasswordText.setVisibility(View.GONE);
        }
        setDefaultAccountText();
    }

    private void setupFacebook() {
        facebookLoginButton
                .setUserInfoChangedCallback(new com.facebook.widget.LoginButton.UserInfoChangedCallback() {
                    @Override
                    public void onUserInfoFetched(GraphUser user) {
                        facebook_user = user;
                        if (currentButton == FACEBOOK_LOGIN_BUTTON) {
                            facebookHandle();
                        }
                    }
                });
        facebookLoginButton.setOnClickListener(buttonClickListener);
    }

//	private void setupGoogle() {
//		googleConnectionStatus = NOT_CONNECTED;
//		googleLoginButton.setForeground(getResources().getDrawable(
//				R.drawable.google_plus_logo_60));
//		googleLoginButton.setOnClickListener(buttonClickListener);
//		googleLoginButton.setStyle(SignInButton.SIZE_ICON_ONLY,
//				SignInButton.COLOR_DARK);
//	}

    private void setupQQ() {
    }

    private void setupSina() {
        // 创建新浪微博授权认证信息
        AuthInfo authInfo = new AuthInfo(this, SinaUtil.APP_KEY,
                SinaUtil.REDIRECT_URL, SinaUtil.SCOPE);
        /** 新浪微博登陆认证对应的listener */
        SinaAuthListener loginListener = new SinaAuthListener();
        sinaLoginButton.setWeiboAuthInfo(authInfo, loginListener);
        sinaLoginButton.setExternalOnClickListener(buttonClickListener);
    }

    private void showErrorInformationDialog(int errorInformation) {
        new CustomDialog(instance).setTitle(getString(R.string.tips))
                .setNegativeButton(getString(R.string.got_it), null)
                .setMessage(getString(errorInformation)).show();
    }

    // sina 微博 登录
    public void sina_login(View v) {
        currentButton = SINA_LOGIN_BUTTON;
    }

    // 微信 登录
    public void wechat_login(View v) {
        currentButton = WECHAT_LOGIN_BUTTON;
        WXEntryActivity.calledFrom = WXEntryActivity.LOGIN;

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

        // 将应用的appId注册到微信
        boolean register = api.registerApp(WeChatUtil.APP_ID);
        if (register) {

            // IWXAPI 是第三方app和微信通信的openapi接口
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "TTTalk_WeChat_Login";
            api.sendReq(req);
        }
    }

}