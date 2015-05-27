package com.ruptech.chinatalk.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class ServiceActivity extends ActionBarActivity implements
        SwipeRefreshLayout.OnRefreshListener {
    public static final String EXTERNAL_URL = "EXTRA_E_URL";
    public static final String EXTERNAL_TITLE = "EXTRA_TITLE";

    static final String TAG = Utils.CATEGORY
            + ServiceActivity.class.getSimpleName();
    @InjectView(R.id.service_webview)
    WebView webview;
    @InjectView(R.id.swype)
    SwipeRefreshLayout swypeLayout;
    private String mUrl;
    private String mTitle;

    public void doExternalUrl(MenuItem item) {
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            finish();
        }
    }

    @Override
    public void onRefresh() {
        swypeLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.org_service, mMenu);
        return true;
    }

    protected void displayTitle() {
        getSupportActionBar().setTitle(mTitle);
    }

    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.readUser() == null) {
            gotoSplashActivity();
            finish();
            return;
        }

        setContentView(R.layout.activity_service);
        ButterKnife.inject(this);

        mUrl = (String) getIntent().getExtras().get(EXTERNAL_URL);
        mTitle = (String) getIntent().getExtras().get(EXTERNAL_TITLE);

        setupComponents();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        System.out.println("--------------" + mTitle);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utils.onBackPressed(this);
        }

        return true;
    }


    public void setupComponents() {
        displayTitle();
        swypeLayout.setOnRefreshListener(this);


        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swypeLayout.setRefreshing(false);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                swypeLayout.setRefreshing(true);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                swypeLayout.setRefreshing(true);
                view.loadUrl(url);
                return true;
            }
        });
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDomStorageEnabled(true);// 解决加载出现空白
        webview.clearCache(true);

        Log.i(TAG, mUrl);
        webview.loadUrl(mUrl);
    }

}
