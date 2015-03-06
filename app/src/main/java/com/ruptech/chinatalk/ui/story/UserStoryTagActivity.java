package com.ruptech.chinatalk.ui.story;

import static com.ruptech.chinatalk.sqlite.TableContent.UserPhotoTable;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FindByKeywordTask;
import com.ruptech.chinatalk.task.impl.PhotoChangeTagTask;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.EditTextWithDel;
import com.ruptech.chinatalk.widget.StoryTagListArrayAdapter;

public class UserStoryTagActivity extends ActionBarActivity {

	static final String EXTRA_TAG = "EXTRA_TAG";
	private static final String SPLIT_PATTERN = ",";

	private final String TAG = Utils.CATEGORY
			+ UserStoryTagActivity.class.getSimpleName();

	@InjectView(R.id.activity_story_tag_gridview)
	ListView mStoyrTagListView;

	@InjectView(R.id.activity_story_tag_locallist)
	ListView localListView;

	@InjectView(R.id.activity_story_tag_channel_edittext)
	EditTextWithDel channelEdit;

	private StoryTagListArrayAdapter serverTagListArrayAdapter;
	private StoryTagListArrayAdapter localTagListArrayAdapter;
	public static final String EXTRA_USER_PHOTO = "USER_PHOTO";
	private UserPhoto userPhoto;

	private final TaskListener mPhotoChangeTagTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			PhotoChangeTagTask photoChangeTagTask = (PhotoChangeTagTask) task;
			if (result == TaskResult.OK) {
				UserPhoto userPhoto = photoChangeTagTask.getUserPhoto();
				onSaveSuccess(userPhoto);
			} else {
				onSaveFailure();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}
	};

	private MenuItem saveMenu;
	private List<String> localChannelList;

	private List<String> channelsOfKeyword;

	private final TaskListener mTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FindByKeywordTask findTask = (FindByKeywordTask) task;
			if (result == TaskResult.OK) {
				channelsOfKeyword = findTask.getChannels();
				serverTagListArrayAdapter.setData(channelsOfKeyword);
			}
		}

	};

	private final OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position >= 0) {
				String channelStr = (String) parent.getAdapter().getItem(
						position);
				channelEdit.setText(channelStr);
				channelEdit.setSelection(channelStr.length());
				onSave();
			}
		}

	};

	private static final int KEYWORD_MIN_LENGTH = 4;

	private void doFindByKeyword() {
		String keyword = channelEdit.getText().toString().trim();

		if (Utils.isEmpty(keyword)) {
			serverTagListArrayAdapter.setData(new ArrayList<String>());
		} else if (keyword.length() >= KEYWORD_MIN_LENGTH) {
			GenericTask mUserPhotoTask = new FindByKeywordTask(keyword);
			mUserPhotoTask.setListener(mTaskListener);
			mUserPhotoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private List<String> getLocalList() {
		localChannelList = new ArrayList<>();
		String localChannelStr = PrefUtils.getUserChannelList();
		if (localChannelStr.length() != 0) {
			String[] temp = localChannelStr.split(",");
			for (int i = 0; i < temp.length; i++) {
				localChannelList.add(temp[i]);
			}
		}
		return localChannelList;
	}

	private void hideInputManager(Context ct) {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(((Activity) ct).getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Yellow_light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_tag);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.add_channel);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		parseExtras();
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.finish);

		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	private void onSave() {
		hideInputManager(this);
		String channelStr = channelEdit.getText().toString();
		if (channelStr != null && channelStr.length() > 0)
			saveTag(channelStr);
		else
			Toast.makeText(this, "Please input channel name",
					Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			hideInputManager(this);
			onBackPressed();
		} else if (item.getItemId() == saveMenu.getItemId()) {
			onSave();
		}
		return true;
	}

	private void onSaveFailure() {
		Toast.makeText(this, R.string.update_tag_failure, Toast.LENGTH_SHORT)
				.show();
	}

	private void onSaveSuccess(UserPhoto userPhoto) {
		ContentValues v = new ContentValues();
		v.put(UserPhotoTable.Columns.CATEGORY, userPhoto.getCategory());
		App.userPhotoDAO.updateUserPhoto(userPhoto.getId(), v);
		CommonUtilities.broadcastStoryMessage(App.mContext, userPhoto);
	}

	private void parseExtras() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			userPhoto = (UserPhoto) extras.getSerializable(EXTRA_USER_PHOTO);
		}
	}

	private void saveLocalList(String newChannel) {
		newChannel = newChannel.trim();
		if (localChannelList.contains(newChannel)) {
			localChannelList.remove(newChannel);
		}
		localChannelList.add(0, newChannel);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < localChannelList.size(); i++) {
			buffer.append(localChannelList.get(i) + SPLIT_PATTERN);
		}
		String str = buffer.toString();
		if (str.length() > 0)
			str = str.substring(0, str.length() - 1);

		PrefUtils.saveUserChannelList(str);
	}

	private void saveTag(String tagCode) {
		if (userPhoto == null) {
			Intent intent = getIntent();
			intent.putExtra(EXTRA_TAG, tagCode);
			setResult(Activity.RESULT_OK, intent);
		} else {
			GenericTask photoChangeTagTask = new PhotoChangeTagTask(
					userPhoto.getId(), tagCode);
			photoChangeTagTask.setListener(mPhotoChangeTagTaskListener);
			photoChangeTagTask.execute();
		}

		this.saveLocalList(tagCode);
		this.finish();
	}

	private void setupComponents() {
		mStoyrTagListView.setEmptyView(localListView);

		serverTagListArrayAdapter = new StoryTagListArrayAdapter(this);
		mStoyrTagListView.setAdapter(serverTagListArrayAdapter);
		mStoyrTagListView.setOnItemClickListener(itemClickListener);

		localTagListArrayAdapter = new StoryTagListArrayAdapter(this);
		localTagListArrayAdapter.setLocal();
		localListView.setAdapter(localTagListArrayAdapter);
		localListView.setOnItemClickListener(itemClickListener);

		channelEdit.setText(PrefUtils.getUserStoryPhotoTagCode());
		channelEdit.setSelection(channelEdit.getText().toString().length());
		channelEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				doFindByKeyword();
			}

		});
	}

}