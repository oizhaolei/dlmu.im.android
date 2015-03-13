package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.ui.user.LanguageActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;
import com.ruptech.chinatalk.widget.LangListViewAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangeLanguageActivity extends AbstractUserActivity {

	private final String TAG = Utils.CATEGORY
			+ ChangeLanguageActivity.class.getSimpleName();

	@InjectView(R.id.langlistview)
	ListView mLangList;
	private LangListViewAdapter mLangListViewAdapter;
	private int clickPosition = -1;
	private String[] langArray;
	private int langIndex = 1;
	private ProgressDialog progressDialog;

	private static ChangeLanguageActivity instance = null;

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

	public void doChangeLang(String lang) {
		if (mUser.getAllLangs().contains(lang)) {
			new CustomDialog(this)
					.setTitle(getString(R.string.language_select_error_title))
					.setMessage(getString(R.string.language_select_error_msg))
					.setPositiveButton(R.string.alert_dialog_ok, null).show();

			return;
		}
		if (changeUserProfileTask != null
				&& changeUserProfileTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		String mFunc = "";
		String mKey = "";
		String mValue = "";
		if (langIndex == LanguageActivity.MAIN_LANGUAGE_INDEX) {
			mFunc = "change_column";
			mKey = "lang";
			mValue = lang;
		} else {
			mFunc = "change_prop";
			mKey = "additional_languages";
			mValue = getAdditionalLanguages(lang, langIndex);
		}
		changeUserProfileTask = new UserProfileChangeTask(mFunc, mKey, mValue);
		changeUserProfileTask.setListener(changeUserProfileTaskListener);
		changeUserProfileTask.execute();
	}

	private String getAdditionalLanguages(String lang, int index) {
		StringBuffer sb = new StringBuffer();
		String[] additionalList = mUser.getAdditionalLangs();
		if (additionalList != null) {
			if (index == 0) {// 选择第二语言
				for (int i = 0; i < additionalList.length; i++) {
					if (i == 0) {
						sb.append(lang).append(',');
					} else {
						sb.append(additionalList[i]).append(',');
					}
				}
			} else if (index == 1) {// 选择第三语言
				if (additionalList.length < 3) {
					sb.append(additionalList[0]).append(',');
					sb.append(lang).append(',');
				} else {
					sb.append(additionalList[0]).append(',');
					sb.append(lang).append(',');
					sb.append(additionalList[2]).append(',');
				}
			} else if (index == 2) {// 选择第四语言
				if (additionalList.length == 1) {
					sb.append(additionalList[0]).append(',');
					sb.append(lang).append(',');
				} else if (additionalList.length > 1) {
					sb.append(additionalList[0]).append(',');
					sb.append(additionalList[1]).append(',');
					sb.append(lang).append(',');
				}
			}
		} else {
			sb.append(lang).append(',');
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	private String getLanguage() {
		String lang = null;
		if (langIndex == LanguageActivity.MAIN_LANGUAGE_INDEX) {
			lang = mUser.getLang();
		} else {
			if (mUser.getAdditionalLangs() != null
					&& langIndex < mUser.getAdditionalLangs().length)
				lang = mUser.getAdditionalLangs()[langIndex];
		}
		return lang;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.change_profile_language);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.change_language);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			langIndex = extras.getInt(LanguageActivity.EXTRA_LANGUAGE_INDEX);
		}

		setupComponents();
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
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();

		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void setupComponents() {
		if (App.readServerAppInfo().langsArray != null) {
			langArray = Utils.getServerTransLang();
			String language = getLanguage();
			if (!Utils.isEmpty(language)) {
				for (int i = 0; i < langArray.length; i++) {
					if (language.equals(langArray[i])) {
						clickPosition = i;
						break;
					}
				}
			}
			mLangListViewAdapter = new LangListViewAdapter(instance, langArray,
					clickPosition, mUser);
			mLangList.setAdapter(mLangListViewAdapter);
			mLangList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (position != clickPosition
							&& mLangListViewAdapter.itemCanClick) {
						clickPosition = position;
						mLangListViewAdapter.clickPosition = clickPosition;
						mLangListViewAdapter.notifyDataSetChanged();
						doChangeLang(langArray[position]);
					}
				}

			});
		}
	}
}
