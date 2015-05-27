package com.ruptech.chinatalk.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileActivity extends ActionBarActivity {
    public static final int EXTRA_ACTIVITY_RESULT_MODIFY_FRIEND = 2;
    public static final String EXTRA_FRIEND = "EXTRA_FRIEND";
    public static final String EXTRA_PUBLIC_IS_SELECTED = "EXTRA_PUBLIC_IS_SELECTED";
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    protected static final int EXTRA_ACTIVITY_RESULT_MODIFY_USER = 1;
    static final String TAG = Utils.CATEGORY
            + ProfileActivity.class.getSimpleName();
    private static ProfileActivity instance = null;
    @InjectView(R.id.activity_profile_user_fullname_textview)
    TextView mFullnameTextView;
    @InjectView(R.id.activity_profile_user_gender_textview)
    TextView mGenderTextView;
    @InjectView(R.id.activity_profile_user_tel_textview)
    TextView mTelTextView;
    @InjectView(R.id.activity_profile_user_thumb_imageview)
    ImageView mThumbImageView;
    @InjectView(R.id.activity_profile_user_tel_layout)
    View profileUserTelView;
    @InjectView(R.id.activity_profile_user_tel_title_textview)
    TextView mAccountLabel;
    private User mUser;
    private long mUserId;

    public static void close() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    private void displayUser() {
        try {
            if (mUser != null && App.readUser() != null) {
                mAccountLabel.setText(this.getString(R.string.account));
                profileUserTelView.setClickable(false);


                mFullnameTextView.setText(mUser.getFullname());
                if (Utils.isEmpty(mUser.getUsername())) {
                    profileUserTelView.setVisibility(View.GONE);
                } else {
                    profileUserTelView.setVisibility(View.VISIBLE);
                    mTelTextView.setText(mUser.getUsername());
                }

            }
        } catch (Exception e) {
            Utils.sendClientException(e);
        }
    }


    private User getUserFromExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            User user = (User) extras.get(EXTRA_USER);
            return user;
        }
        return null;
    }

    private long getUserIdFromExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long userId = extras.getLong(EXTRA_USER_ID);
            return userId;
        }
        return 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EXTRA_ACTIVITY_RESULT_MODIFY_USER) {// modify
                if (null != data.getExtras()) {
                    Bundle extras = data.getExtras();
                    mUser = (User) extras.get(EXTRA_USER);
                    getSupportActionBar().setTitle(mUser.getFullname());

                    mFullnameTextView.setText(mUser.getFullname());
                    if (Utils.isEmpty(mUser.getUsername())) {
                        profileUserTelView.setVisibility(View.GONE);
                    } else {
                        profileUserTelView.setVisibility(View.VISIBLE);
                        mTelTextView.setText(mUser.getUsername());
                    }

                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);
        getSupportActionBar().setTitle(R.string.detail_information);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        instance = this;
        mUser = getUserFromExtras();
        if (mUser == null) {
            mUserId = getUserIdFromExtras();
        } else {
            mUserId = mUser.getId();
        }
        if (mUser == null && mUserId <= 0) {
            Toast.makeText(this, R.string.user_infomation_is_invalidate,
                    Toast.LENGTH_LONG).show();
            finish();
        }
        displayUser();
    }

    @Override
    protected void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
