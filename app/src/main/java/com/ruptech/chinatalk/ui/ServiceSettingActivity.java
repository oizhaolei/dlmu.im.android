package com.ruptech.chinatalk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.adapter.ListViewRadioAdapter;
import com.ruptech.chinatalk.adapter.ListViewServiceSettingAdapter;
import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveOrgListTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.AbstractLangListViewAdapter;
import com.ruptech.chinatalk.widget.OrgListArrayAdapter;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class ServiceSettingActivity extends ActionBarActivity {

    public static final String EXTERNAL_TITLE = "EXTERNAL_TITLE";
    static final String TAG = Utils.CATEGORY
            + ServiceSettingActivity.class.getSimpleName();

    ListView mServiceSettingListView;
    private ListViewServiceSettingAdapter adapter;
    private List<Service> uncheckedService;
    private String mTitle;
    private List<Service> result;

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
        //初始化数据
        uncheckedService = new ArrayList();
        for (int i = 0; i < App.service.size(); i++) {
            Service m = App.service.get(i);
            if (m.getChecked() == 0)
                uncheckedService.add(m);

        }
        setContentView(R.layout.activity_service_setting);
        mServiceSettingListView = (ListView) findViewById(R.id.activity_service_setting_listview);
        adapter = new ListViewServiceSettingAdapter(this, uncheckedService);
        mServiceSettingListView.setAdapter(adapter);
        mServiceSettingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.getTag();
                // 改变CheckBox的状态
                CheckBox cb = (CheckBox) view.findViewById(R.id.item_service_setting_listview_checkbox);
                cb.toggle();
                // 将CheckBox的选中状况记录下来
                adapter.isSelected.put(position, cb.isChecked());

                adapter.notifyDataSetChanged();
            }
        });


        ButterKnife.inject(this);
        result = new ArrayList<>();
        mTitle = getIntent().getExtras().getString(EXTERNAL_TITLE);
        setupComponents();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //更新App.service
            for (int i = 0; i < adapter.isSelected.size(); i++) {
                if (adapter.isSelected.get(i)) {
                    System.out.println(adapter.isSelected.get(i));
                    Service s = uncheckedService.get(i);
                    for (int j = 0; j < App.service.size(); j++) {
                        if( App.service.get(j).getFnid().equals(s.getFnid()))
                            App.service.get(j).setChecked(1);
                    }
                }

            }
            onBackPressed();
        }

        return true;
    }

    public void setupComponents() {
        displayTitle();

    }


    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
}
