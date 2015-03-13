package com.ruptech.chinatalk.ui.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FileUploadTask.FileUploadInfo;
import com.ruptech.chinatalk.task.impl.SignupTask;
import com.ruptech.chinatalk.task.impl.UrlUploadTask;
import com.ruptech.chinatalk.ui.LoginGateActivity;
import com.ruptech.chinatalk.ui.LoginLoadingActivity;
import com.ruptech.chinatalk.ui.LoginSignupActivity;
import com.ruptech.chinatalk.ui.dialog.ChangeSignupPhotoActivity;
import com.ruptech.chinatalk.ui.setting.AgreementActivity;
import com.ruptech.chinatalk.ui.setting.IntroduceActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.GenderSpinnerAdapter;
import com.ruptech.chinatalk.widget.LangSpinnerAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SignupProfileActivity extends ActionBarActivity {

	private final int EXTRA_ACTIVITY_RESULT_MODIFY_USER = 1;
	public static final String EXTRA_USER = "EXTRA_USER";
	public static final String EXTRA_PHOTO_URL = "EXTRA_PHOTO_URL";

	static final String TAG = Utils.CATEGORY
			+ SignupProfileActivity.class.getSimpleName();

	public static SignupProfileActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	public static void open(Activity context, User user) {
		Intent intent = new Intent(context, SignupProfileActivity.class);
		intent.putExtra(EXTRA_USER, user);
		context.startActivity(intent);
	}

	private String[] langArray;
	@InjectView(R.id.activity_signup_profile_fullname_edittext)
	TextView mFullnameTextView;
	@InjectView(R.id.activity_signup_profile_select_spinner_gender)
	Spinner mGenderSpinner;
	@InjectView(R.id.activity_signup_profile_select_spinner_lang)
	Spinner mLangSpinner;
	@InjectView(R.id.activity_signup_profile_thumb_imgview)
	ImageView mThumbImageView;

	protected User mTempUser;
	protected long mUserId;

	private MenuItem saveMenu;

	private GenericTask mSignupTask;

	private final TaskListener mSignupTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onSignupSuccess();
			} else {
				String msg = task.getMsg();
				onSignupFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onSignupBegin();
		}

	};

	private ProgressDialog progressDialog;

	private GenericTask mUploadTask;

	private FileUploadInfo newFileInfo;

	private final TaskListener mUploadTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UrlUploadTask urlUploadTask = (UrlUploadTask) task;
			if (result == TaskResult.OK) {
				newFileInfo = urlUploadTask.getFileInfo();
				mTempUser.setPic_url(newFileInfo.fileName);
			}
			mSignupTask = new SignupTask(mTempUser);
			mSignupTask.setListener(mSignupTaskListener);
			mSignupTask.execute();
		}

		@Override
		public void onPreExecute(GenericTask task) {
			if (progressDialog == null) {
				progressDialog = Utils.showDialog(SignupProfileActivity.this,
						getString(R.string.action_registe_beagin));
			}
		}

	};

	private String mLang;

	@OnClick(R.id.activity_signup_profile_thumb_imgview)
	public void changeUserPhoto(View v) {
		Intent intent = new Intent(this, ChangeSignupPhotoActivity.class);
		startActivityForResult(intent, EXTRA_ACTIVITY_RESULT_MODIFY_USER);
	}

	private void disableEntry() {
		if (saveMenu != null) {
			saveMenu.setEnabled(false);
		}
	}

	private void displayUser() {
		if (mTempUser != null) {
			final String thumb = mTempUser.getPic_url();
			Utils.setUserPicImage(mThumbImageView, thumb);

			if (!Utils.isMail(mTempUser.getTel())) {// 第三方应用
				mFullnameTextView.setText(mTempUser.getFullname());
			}
			langArray = Utils
					.getSpinnerLang(AppPreferences.SPINNER_DEFAULT_LANG);

			LangSpinnerAdapter langAdapter = new LangSpinnerAdapter(this,
					langArray);
			mLangSpinner.setAdapter(langAdapter);

			mLangSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent,
								View v, int position, long id) {
							mLang = langArray[position];
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

						}

					});
			GenderSpinnerAdapter mGenderSpinnerAdapter = new GenderSpinnerAdapter(
					this, new int[] { 0, 1 });
			mGenderSpinner.setAdapter(mGenderSpinnerAdapter);
			mGenderSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent,
								View v, int position, long id) {
							mTempUser.setGender(position + 1);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

						}

					});
		}
	}

	private void enableEntry() {
		if (saveMenu != null) {
			saveMenu.setEnabled(true);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == EXTRA_ACTIVITY_RESULT_MODIFY_USER) {// modify
				if (null != data.getExtras()) {
					Bundle extras = data.getExtras();
					String pic_url = String
							.valueOf(extras.get(EXTRA_PHOTO_URL));
					mTempUser.setPic_url(pic_url);
					Utils.setUserPicImage(mThumbImageView, pic_url);
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup_profile);
		instance = this;
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.profile);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mTempUser = getUserFromExtras();
		displayUser();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		mMenu.clear();
		int order = 0;
		saveMenu = mMenu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.alert_dialog_ok);
		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(mMenu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == saveMenu.getItemId()) {
			String fullname = mFullnameTextView.getText().toString();
			fullname = fullname.replace(" ", "");
			String picture_url = mTempUser.getPic_url();
			if (Utils.isEmpty(picture_url)) {
				showErrorInformationDialog(R.string.please_upload_user_photo);
			} else if (Utils.isEmpty(fullname)) {
				showErrorInformationDialog(R.string.hint_input_your_nickname);
			} else if (AppPreferences.SPINNER_DEFAULT_LANG
					.equalsIgnoreCase(mLang)) {
				showErrorInformationDialog(R.string.language_unseleted);
			} else {
				mTempUser.setFullname(fullname);
				mTempUser.setLang(mLang);
				if (picture_url.startsWith("http://")
						|| picture_url.startsWith("https://")) {
					mUploadTask = new UrlUploadTask(picture_url);
					mUploadTask.setListener(mUploadTaskListener);
					mUploadTask.execute();
				} else {
					mSignupTask = new SignupTask(mTempUser);
					mSignupTask.setListener(mSignupTaskListener);
					mSignupTask.execute();
				}
			}
		} else if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void onSignupBegin() {
		disableEntry();
		if (progressDialog == null) {
			progressDialog = Utils.showDialog(this,
					getString(R.string.action_registe_beagin));
		}
	}

	private void onSignupFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
	}

	private void onSignupSuccess() {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.signup_successed, Toast.LENGTH_SHORT)
				.show();

		if (BuildConfig.DEBUG)
			Log.d(TAG, "Storing credentials.");

		LoginGateActivity.close();
		LoginSignupActivity.close();
		IntroduceActivity.close();
		AgreementActivity.close();
		PrefUtils.saveShowSystemFreeRechargePointInform(mTempUser.getTel());

		Intent intent = new Intent(this, LoginLoadingActivity.class);
		intent.putExtra(LoginLoadingActivity.PREF_USERINFO_NAME,
				mTempUser.getTel());
		intent.putExtra(LoginLoadingActivity.PREF_USERINFO_PASS,
				mTempUser.getPassword());
		startActivity(intent);
	}

	private void showErrorInformationDialog(int errorInformation) {
		new CustomDialog(SignupProfileActivity.this)
				.setTitle(getString(R.string.tips))
				.setNegativeButton(getString(R.string.got_it), null)
				.setMessage(getString(errorInformation)).show();
	}

}
