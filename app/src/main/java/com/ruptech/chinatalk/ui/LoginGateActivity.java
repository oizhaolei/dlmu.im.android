package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginGateActivity extends Activity {

    public static LoginGateActivity instance;


    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    private boolean checkNetwork() {
        boolean network = Utils.isMobileNetworkAvailible(this)
                || Utils.isWifiAvailible(this);
        if (!network) {
            Toast.makeText(this,
                    R.string.wifi_and_3g_networks_are_not_available,
                    Toast.LENGTH_LONG).show();
        }
        return network;
    }

    @OnClick(R.id.activity_login_gate_login_button)
    public void doLoginView(View v) {
        if (!checkNetwork()) {
            return;
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_login_gate);
        ButterKnife.inject(this);
        SplashActivity.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
