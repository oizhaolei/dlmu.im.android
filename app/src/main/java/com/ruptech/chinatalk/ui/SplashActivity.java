package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SplashActivity extends Activity {

    public static SplashActivity instance;
    public boolean directlyToMain = false;
    @InjectView(R.id.activity_splash_footer_textview)
    TextView footerTextView;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    private void gotoDispatchActivity() {
        this.directlyToMain = false;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // usage demo
                if (App.isAvailableShowMain()) {
                    gotoLoginLoadingActivity();
                } else {
                    gotoLoginGateActivity();
                }
            }
        });
    }

    private void gotoLoginGateActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginGateActivity.class);
        startActivity(intent);
    }

    private void gotoLoginLoadingActivity() {
        Intent intent = new Intent(SplashActivity.this,
                LoginLoadingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        ButterKnife.inject(this);

        if (App.isAvailableShowMain()) {

            this.directlyToMain = true;
            LoginLoadingActivity.gotoMainActivity(this);
            finish();
            return;
        }

        setupComponents();

        instance = this;
        App.taskManager.cancelAll();

    }

    @Override
    public void onResume() {
        super.onResume();
        gotoDispatchActivity();
    }

    private void setupComponents() {
        footerTextView.setText(getString(R.string.gate_footer_text, BuildConfig.VERSION_NAME));
    }
}