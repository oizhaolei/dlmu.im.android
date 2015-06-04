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
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveOrgListTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.OrgListArrayAdapter;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class OrgActivity extends ActionBarActivity {

    public static final String PARENT_ORG_JID = "PARENT_ORG_JID";
    public static final String PARENT_ORG_NAME = "PARENT_ORG_NAME";
    public static final String PARENT_ORG_STUDENT = "PARENT_ORG_STUDENT";
    static final String TAG = Utils.CATEGORY
            + OrgActivity.class.getSimpleName();

    @InjectView(R.id.activity_org_listview)
    ListView mOrgListView;
    OrgListArrayAdapter mOrgListArrayAdapter;

    private String mParentOrgJId;
    private String mTitle;
    private String mIsStudent;
    private List<Map<String, Object>> itemList = new ArrayList<>();

    private void startChatActivity(String userJid, String fullname) {
        User user = new User(User.getUsernameFromJid(userJid), fullname);
        App.userDAO.mergeUser(user);

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
        chatIntent.putExtra(ChatActivity.EXTRA_TITLE, fullname);
        startActivity(chatIntent);
    }

    // doChat
    public void doChat(MenuItem item) {
        startChatActivity(mParentOrgJId, mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu mMenu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.org_actions, mMenu);
        return true;
    }

    protected void displayTitle() {
        getSupportActionBar().setTitle(mTitle);
    }

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

        mParentOrgJId = getIntent().getExtras().getString(PARENT_ORG_JID);
        mTitle = getIntent().getExtras().getString(PARENT_ORG_NAME);
        mIsStudent = getIntent().getExtras().getString(PARENT_ORG_STUDENT);
        setupComponents();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        retrieveTeacher(mParentOrgJId);
    }

    private void retrieveTeacher(String parentOrgJid) {
        RetrieveOrgListTask retrieveOrgListTask = new RetrieveOrgListTask(parentOrgJid, mIsStudent);
        TaskAdapter taskListener = new TaskAdapter() {
            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                super.onPostExecute(task, result);
                RetrieveOrgListTask retrieveOrgListTask = (RetrieveOrgListTask) task;
                if (result == TaskResult.OK) {

                    List<Map<String, Object>> orgList = retrieveOrgListTask.getOrgList();
                    List<Map<String, Object>> memberList = retrieveOrgListTask.getMemberList();
                    for(Map<String, Object> org : orgList){
                        org.put("is_parent" , "true");
                        itemList.add(org);
                    }for(Map<String, Object> member : memberList){
                        member.put("is_parent" , "false");
                        itemList.add(member);
                    }
                    mOrgListArrayAdapter.addAll(itemList);
                    mOrgListArrayAdapter.notifyDataSetChanged();
                }
            }
        };
        retrieveOrgListTask.setListener(taskListener);
        retrieveOrgListTask.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    public void setupComponents() {
        displayTitle();
        mOrgListArrayAdapter = new OrgListArrayAdapter(this);
        mOrgListView.setAdapter(mOrgListArrayAdapter);

        mOrgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> view, View arg1,
                                    int position, long id) {
                Map<String, Object> item = (Map<String, Object>) view.getAdapter().getItem(position);
                String jid = (String) item.get("jid");
                String name = (String) item.get("name");
                String is_parent = (String) item.get("is_parent");
                if ("false".equals(is_parent)) {
                    startChatActivity(jid, name);
                } else {
                    startOrgActivity(jid, name);
                }
            }
        });
    }

    private void startOrgActivity(String jid, String name) {
        Intent orgIntent = new Intent(OrgActivity.this, OrgActivity.class);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_JID, jid);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_NAME, name);
        orgIntent.putExtra(OrgActivity.PARENT_ORG_STUDENT, mIsStudent);
        startActivity(orgIntent);
    }

    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
}
