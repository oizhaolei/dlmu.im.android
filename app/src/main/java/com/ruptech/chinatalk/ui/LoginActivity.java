package com.ruptech.chinatalk.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	public LoginActivity() {
	}

	@InjectView(R.id.activity_username_edittext)
	EditText mUserNameEditText; // 帐号编辑框
	@InjectView(R.id.activity_next_button)
	Button nextBtn;

	@InjectView(R.id.activity_password_edittext)
	EditText mPasswordEditText; // 密码编辑框


	protected static final String EXTRA_TYPE = "LOGIN";
	protected static final String EXTRA_TYPE_LOGIN = "LOGIN";
	protected static final String EXTRA_TYPE_SIGNUP = "SIGNUP";

	private final String TAG = Utils.CATEGORY
			+ LoginActivity.class.getSimpleName();

	public static LoginActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private String mUsername;

	public static ProgressDialog progressDialog;

	private String selectType;


	@OnClick(R.id.activity_next_button)
	public void doNext(View v) {
		mUsername = mUserNameEditText.getText().toString();
		String password = mPasswordEditText.getText().toString();
		mUsername = mUsername.replace(" ", "");
		mUsername = mUsername.toLowerCase(Locale.getDefault());


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
			}
		}
	}

	private void extras() {
		Bundle extras = getIntent().getExtras();
		selectType = extras.getString(EXTRA_TYPE);
	}

	private void gotoLoginLoadingActivity(String mUsername, String password) {
		Intent intent = new Intent(instance, LoginLoadingActivity.class);
		intent.putExtra(LoginLoadingActivity.PREF_USERINFO_NAME, mUsername);
		intent.putExtra(LoginLoadingActivity.PREF_USERINFO_PASS, password);
		startActivity(intent);
	}

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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}


	@Override
	public void onRefresh() {
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

		if (selectType.equals(EXTRA_TYPE_LOGIN)) {
			mUserNameEditText.setHint(R.string.please_input_email_or_telphone);
			getSupportActionBar().setTitle(R.string.login);
			nextBtn.setText(R.string.login);
		} else {
			mUserNameEditText.setHint(R.string.email_input_has_blank);
			getSupportActionBar().setTitle(R.string.signup);
			nextBtn.setText(R.string.next);
		}
		setDefaultAccountText();
	}


	private void showErrorInformationDialog(int errorInformation) {
		new CustomDialog(instance).setTitle(getString(R.string.tips))
				.setNegativeButton(getString(R.string.got_it), null)
				.setMessage(getString(errorInformation)).show();
	}


}