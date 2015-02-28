package com.ruptech.chinatalk.ui.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.ui.dialog.ChangeLanguageActivity;
import com.ruptech.chinatalk.utils.Utils;

public class LanguageActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ LanguageActivity.class.getSimpleName();

	@InjectView(R.id.activity_language_main_textview)
	TextView mainLangTextView;
	@InjectView(R.id.activity_language_main_thumb_imageview)
	ImageView mainLangThumbView;
	@InjectView(R.id.activity_language_additional2_textview)
	TextView additional2LangTextView;
	@InjectView(R.id.activity_language_additional2_thumb_imageview)
	ImageView additional2ThumbView;
	@InjectView(R.id.activity_language_additional2_submenu_imageview)
	ImageView additional2SubmenuView;
	@InjectView(R.id.activity_language_additional2_delete_imageview)
	ImageView additional2DeleteView;
	@InjectView(R.id.activity_language_additional3_textview)
	TextView additional3LangTextView;
	@InjectView(R.id.activity_language_additional3_thumb_imageview)
	ImageView additional3ThumbView;
	@InjectView(R.id.activity_language_additional3_submenu_imageview)
	ImageView additional3SubmenuView;
	@InjectView(R.id.activity_language_additional3_delete_imageview)
	ImageView additional3DeleteView;
	@InjectView(R.id.activity_language_additional4_textview)
	TextView additional4LangTextView;
	@InjectView(R.id.activity_language_additional4_thumb_imageview)
	ImageView additional4ThumbView;
	@InjectView(R.id.activity_language_additional4_submenu_imageview)
	ImageView additional4SubmenuView;
	@InjectView(R.id.activity_language_additional4_delete_imageview)
	ImageView additional4DeleteView;

	private ProgressDialog progressDialog;

	protected User mUser;
	public static final String EXTRA_LANGUAGE_INDEX = "EXTRA_LANGUAGE_INDEX";
	public static final int MAIN_LANGUAGE_INDEX = -1;

	private GenericTask changeUserProfileTask;

	private final TaskListener changeUserProfileTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserProfileChangeTask fsTask = (UserProfileChangeTask) task;
			if (result == TaskResult.OK) {
				User user = fsTask.getUser();
				onLangChangeSuccess(user);

			} else {
				String msg = task.getMsg();
				onLangChangeFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onLangChangeBegin();
		}

	};

	@OnClick(R.id.activity_language_additional4)
	public void changeUserAdditionalFourLanguage(View v) {
		doChangeUserLanguage(v);
	}

	@OnClick(R.id.activity_language_additional2)
	public void changeUserAdditionalSecondLanguage(View v) {
		doChangeUserLanguage(v);
	}

	@OnClick(R.id.activity_language_additional3)
	public void changeUserAdditionalThirdLanguage(View v) {
		doChangeUserLanguage(v);
	}

	@OnClick(R.id.activity_language_user_main_language_layout)
	public void changeUserMainLanguage(View v) {
		doChangeUserLanguage(v);
	}

	private void deleteAdditionalLanguage(int index) {
		if (changeUserProfileTask != null
				&& changeUserProfileTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		changeUserProfileTask = new UserProfileChangeTask("change_prop",
				"additional_languages", getAdditionalLanguages(index));
		changeUserProfileTask.setListener(changeUserProfileTaskListener);
		changeUserProfileTask.execute();
	}

	private void doChangeUserLanguage(View v){
		if (mUser == null)
			return;
		Intent intent = new Intent(this, ChangeLanguageActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
		intent.putExtra(EXTRA_LANGUAGE_INDEX, getIndexFromView(v));
		startActivityForResult(intent,
				ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_USER);
	}

	private String getAdditionalLanguages(int deleteIndex) {
		StringBuffer sb = new StringBuffer();
		String[] additionalList = mUser.getAdditionalLangs();
		String toDelete = additionalList[deleteIndex];
		if (additionalList != null) {
			for (String l : additionalList) {
				if (!toDelete.equals(l)) {
					sb.append(l).append(',');
				}
			}
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	private int getIndexFromView(View clickedView) {
		int index = MAIN_LANGUAGE_INDEX;
		switch (clickedView.getId()) {
		case R.id.activity_language_user_main_language_layout:
			index = MAIN_LANGUAGE_INDEX;
			break;
		case R.id.activity_language_additional2:
			index = 0;
			break;
		case R.id.activity_language_additional3:
			index = 1;
			break;
		case R.id.activity_language_additional4:
			index = 2;
			break;
		}
		return index;
	}

	private String getLanguage(int index) {
		String lang = null;
		if (index == LanguageActivity.MAIN_LANGUAGE_INDEX) {
			lang = mUser.getLang();
		} else {
			if (mUser.getAdditionalLangs() != null
					&& index < mUser.getAdditionalLangs().length)
				lang = mUser.getAdditionalLangs()[index];
		}
		return lang;
	}

	private User getUserFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ProfileActivity.EXTRA_USER);
			return user;
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == ProfileActivity.EXTRA_ACTIVITY_RESULT_MODIFY_USER) {// modify
				if (null != data.getExtras()) {
					Bundle extras = data.getExtras();
					mUser = (User) extras.get(ProfileActivity.EXTRA_USER);
					updateUI();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_USER, mUser);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_language);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.change_language);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mUser = getUserFromExtras();
		setupComponents();
		updateUI();
	}

	private void onLangChangeBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
	}

	private void onLangChangeFailure(String msg) {
		Utils.dismissDialog(progressDialog);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onLangChangeSuccess(User user) {
		mUser = user;
		updateUI();
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void setupComponents() {
		additional2DeleteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteAdditionalLanguage(0);
			}
		});
		additional3DeleteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteAdditionalLanguage(1);
			}
		});

		additional4DeleteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteAdditionalLanguage(2);
			}
		});
	}

	private void updateUI() {
		if (mUser == null)
			return;

		mainLangTextView.setText(Utils.getLangDisplayName(mUser.getLang()));
		mainLangThumbView.setImageResource(Utils.getLanguageFlag(mUser.lang));

		String additionalLang = getLanguage(0);
		if (!Utils.isEmpty(additionalLang)) {
			additional2LangTextView.setText(Utils
					.getLangDisplayName(additionalLang));
			additional2ThumbView.setImageResource(Utils
					.getLanguageFlag(additionalLang));
			additional2ThumbView.setVisibility(View.VISIBLE);
			additional2SubmenuView.setVisibility(View.INVISIBLE);
			additional2DeleteView.setVisibility(View.VISIBLE);
		} else {
			additional2LangTextView.setText(R.string.additional_language2);
			additional2ThumbView.setVisibility(View.INVISIBLE);
			additional2SubmenuView.setVisibility(View.VISIBLE);
			additional2DeleteView.setVisibility(View.INVISIBLE);
		}
		additionalLang = getLanguage(1);
		if (!Utils.isEmpty(additionalLang)) {
			additional3LangTextView.setText(Utils
					.getLangDisplayName(additionalLang));
			additional3ThumbView.setImageResource(Utils
					.getLanguageFlag(additionalLang));
			additional3ThumbView.setVisibility(View.VISIBLE);
			additional3SubmenuView.setVisibility(View.INVISIBLE);
			additional3DeleteView.setVisibility(View.VISIBLE);
		} else {
			additional3LangTextView.setText(R.string.additional_language3);
			additional3ThumbView.setVisibility(View.INVISIBLE);
			additional3SubmenuView.setVisibility(View.VISIBLE);
			additional3DeleteView.setVisibility(View.INVISIBLE);
		}
		additionalLang = getLanguage(2);
		if (!Utils.isEmpty(additionalLang)) {
			additional4LangTextView.setText(Utils
					.getLangDisplayName(additionalLang));
			additional4ThumbView.setImageResource(Utils
					.getLanguageFlag(additionalLang));
			additional4ThumbView.setVisibility(View.VISIBLE);
			additional4SubmenuView.setVisibility(View.INVISIBLE);
			additional4DeleteView.setVisibility(View.VISIBLE);
		} else {
			additional4LangTextView.setText(R.string.additional_language4);
			additional4ThumbView.setVisibility(View.INVISIBLE);
			additional4SubmenuView.setVisibility(View.VISIBLE);
			additional4DeleteView.setVisibility(View.INVISIBLE);
		}

	}
}
