package com.ruptech.chinatalk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class OrgActivity extends ActionBarActivity {

	static final String TAG = Utils.CATEGORY
			+ OrgActivity.class.getSimpleName();

	private String mParentOrgJid;


	@InjectView(R.id.activity_org_listview)
	ListView mOrgListView;
	private List<Map<String, Object>> orgList;
	private List<Map<String, Object>> memberList;

	protected void displayTitle() {
		String title;
		title = mParentOrgJid;
		getSupportActionBar().setTitle(title);
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

		mParentOrgJid = (String) getIntent().getExtras().get(PARENT_ORG_ID);

		setupComponents();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		retrieveOrg(mParentOrgJid);
	}

	private void retrieveOrg(String parentOrgJid) {
		RetrieveOrgListTask retrieveOrgListTask = new RetrieveOrgListTask(parentOrgJid);
		TaskAdapter taskListener = new TaskAdapter() {
			@Override
			public void onPostExecute(GenericTask task, TaskResult result) {
				super.onPostExecute(task, result);
				RetrieveOrgListTask retrieveOrgListTask = (RetrieveOrgListTask) task;
				if (result == TaskResult.OK) {
					orgList = retrieveOrgListTask.getOrgList();
					memberList = retrieveOrgListTask.getMemberList();
					setAdapter();

				}
			}

		};
		retrieveOrgListTask.setListener(taskListener);
		retrieveOrgListTask.execute();
	}

	private void setAdapter() {
		SimpleAdapter adapter;
		if (orgList.size() > 0) {
			adapter = new SimpleAdapter(this, orgList, R.layout.item_org,
					new String[]{"jid", "name"},
					new int[]{R.id.item_org_jid, R.id.item_org_name});
		} else {
			adapter = new SimpleAdapter(this, memberList, R.layout.item_member,
					new String[]{"jid", "name"},
					new int[]{R.id.item_member_jid, R.id.item_org_name});
		}
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

				Intent orgIntent = new Intent(OrgActivity.this, OrgActivity.class);
				orgIntent.putExtra(OrgActivity.PARENT_ORG_ID, (String)item.get("jid"));
				startActivity(orgIntent);
			}
		});
	}


	public static final String PARENT_ORG_ID = "PARENT_ORG_ID";


	private void gotoSplashActivity() {
		Intent intent = new Intent(this, SplashActivity.class);
		startActivity(intent);
	}


}
