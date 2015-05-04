package com.ruptech.chinatalk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveOrgListTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class OrgActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ OrgActivity.class.getSimpleName();

	private String mParentOrgId;
	private String mTitle;


	private void startChatActivity(String userJid, String name) {
		Intent chatIntent = new Intent(this, ChatActivity.class);
		chatIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
		chatIntent.putExtra(ChatActivity.EXTRA_TITLE, name);
		startActivity(chatIntent);
	}

	// doChat
	public void doChat(MenuItem item) {
		startChatActivity(mParentOrgId, mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.org_actions, mMenu);
		return true;
	}

	@InjectView(R.id.activity_org_listview)
	ListView mOrgListView;
	private List<Map<String, Object>> itemList = new ArrayList<>();

	protected void displayTitle() {
		getSupportActionBar().setTitle(mTitle);
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

		setContentView(R.layout.activity_org);
		ButterKnife.inject(this);

		mParentOrgId = (String) getIntent().getExtras().get(PARENT_ORG_ID);
		mTitle = (String) getIntent().getExtras().get(PARENT_ORG_NAME);

		setupComponents();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		retrieveOrg(mParentOrgId);
	}

	private void retrieveOrg(String parentOrgJid) {
		RetrieveOrgListTask retrieveOrgListTask = new RetrieveOrgListTask(parentOrgJid);
		TaskAdapter taskListener = new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				super.onPostExecute(task, result);
				RetrieveOrgListTask retrieveOrgListTask = (RetrieveOrgListTask) task;
				if (result == TaskResult.OK) {

					List<Map<String, Object>> orgList = retrieveOrgListTask.getOrgList();
					List<Map<String, Object>> memberList = retrieveOrgListTask.getMemberList();

					itemList.addAll(orgList);
					itemList.addAll(memberList);
					setAdapter();

				}
			}

		};
		retrieveOrgListTask.setListener(taskListener);
		retrieveOrgListTask.execute();
	}

	private void setAdapter() {
		SimpleAdapter adapter;
		adapter = new SimpleAdapter(this, itemList, R.layout.item_org,
				new String[]{"jid", "name"},
				new int[]{R.id.item_org_jid, R.id.item_org_name});
		mOrgListView.setAdapter(adapter);
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

		mOrgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> view, View arg1,
			                        int position, long id) {
				Map<String, Object> item = (Map<String, Object>) view.getAdapter().getItem(position);
				String jid = (String) item.get("jid");
				String name = (String) item.get("name");
				if (jid.startsWith("teacher_") || jid.startsWith("student_")) {
					startChatActivity(jid, name);
				} else {
					startOrgActivity(jid, name);
				}
			}
		});
	}

	private void startOrgActivity(String jid, String name) {
		Intent orgIntent = new Intent(OrgActivity.this, OrgActivity.class);
		orgIntent.putExtra(OrgActivity.PARENT_ORG_ID, jid);
		orgIntent.putExtra(OrgActivity.PARENT_ORG_NAME, name);
		startActivity(orgIntent);
	}


	public static final String PARENT_ORG_ID = "PARENT_ORG_ID";
	public static final String PARENT_ORG_NAME = "PARENT_ORG_NAME";


	private void gotoSplashActivity() {
		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
	}


}
