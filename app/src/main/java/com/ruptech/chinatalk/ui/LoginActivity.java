package com.ruptech.chinatalk.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class LoginActivity extends ActionBarActivity implements
        SwipeRefreshLayout.OnRefreshListener {
    public static LoginActivity instance;
    public static ProgressDialog progressDialog;
    private final String TAG = Utils.CATEGORY
            + LoginActivity.class.getSimpleName();
    @InjectView(R.id.activity_username_edittext)
    EditText mUserNameEditText; // 帐号编辑框
    @InjectView(R.id.activity_next_button)
    Button nextBtn;
    @InjectView(R.id.activity_password_edittext)
    EditText mPasswordEditText; // 密码编辑框

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    @OnClick(R.id.activity_next_button)
    public void doNext(View v) {
        String mUsername = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        mUsername = mUsername.replace(" ", "");
        mUsername = mUsername.toLowerCase(Locale.getDefault());

        gotoLoginLoadingActivity(mUsername, password);
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


    private void setupComponents() {

        mUserNameEditText.setHint(R.string.please_input_email_or_telphone);
        getSupportActionBar().setTitle(R.string.login);
        nextBtn.setText(R.string.login);
    }


}