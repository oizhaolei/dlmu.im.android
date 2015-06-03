package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServiceListTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.ClassroomCheckInActivity;
import com.ruptech.chinatalk.ui.MeetingCheckInActivity;
import com.ruptech.chinatalk.ui.ServiceActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ServiceFragment extends Fragment {

    private static final String TAG = Utils.CATEGORY
            + ServiceFragment.class.getSimpleName();
    @InjectView(R.id.service_list)
    ListView serviceListView;
    private SimpleAdapter serviceAdapter;
    private List<Map<String, Object>> itemList = new ArrayList<>();

    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.main_tab_service, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showNormalActionBar(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChatLayout();

        retrieveServiceList();
    }

    private void retrieveServiceList() {
        RetrieveServiceListTask RetrieveServiceListTask = new RetrieveServiceListTask();
        TaskAdapter taskListener = new TaskAdapter() {
            @Override
            public void onPostExecute(GenericTask task, TaskResult result) {
                super.onPostExecute(task, result);
                RetrieveServiceListTask RetrieveServiceListTask = (RetrieveServiceListTask) task;

                if (result == TaskResult.OK) {

                    itemList = RetrieveServiceListTask.getServiceList();

                    setAdapter();

                }
            }

        };

        RetrieveServiceListTask.setListener(taskListener);

        RetrieveServiceListTask.execute();
    }

    private void setupChatLayout() {

        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> view, View arg1,
                                    int position, long id) {
                Map<String, Object> item = (Map<String, Object>) view.getAdapter().getItem(position);
                String jid = item.get("fnid").toString();
                String title = (String) item.get("title");
                String url = (String) item.get("url");
                String[] params = new String[]{};
                if (null != item.get("param")) {
                    params = item.get("param").toString().split(";");
                }
                Integer type = (Integer) item.get("typeid");
                switch (type) {
                    case 0:
                        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(ServiceFragment.this.getActivity());

                        if (mAdapter == null) {
                            Toast.makeText(getActivity(), R.string.nfc_nomodule, Toast.LENGTH_SHORT).show();
                        } else {
                            if (!mAdapter.isEnabled()) {
                                Toast.makeText(getActivity(), R.string.nfc_disabled, Toast.LENGTH_SHORT).show();
                            } else {
                                mAdapter = null;
                                if (url.startsWith("COURSE")) {
                                    String kch = url.substring(url.indexOf("_") + 1, url.lastIndexOf("_"));
                                    String zxjxjhh = url.substring(url.lastIndexOf("_") + 1);
                                    startClassroomCheckInActivity(jid, title);
                                }
                                if (url.startsWith("MEETING")) {
                                    String mid = url.substring(url.indexOf("_") + 1);
                                    startMeetingCheckInActivity(jid, title, mid);
                                }
                            }
                        }
                        break;
                    case 1:
                        url = Utils.genUrl(Utils.genParam(params), url);
                        System.out.println(">>>>>>"+url);
                        startServiceActivity(url, title);
                        break;
                    case 2:
                        Toast.makeText(getActivity(), "开发中……", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getActivity(), "开发中……", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void startChatActivity(String userJid, String name) {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
        chatIntent.putExtra(ChatActivity.EXTRA_TITLE, name);
        startActivity(chatIntent);
    }

    private void startServiceActivity(String url, String title) {
        Intent intent = new Intent(getActivity(), ServiceActivity.class);
        intent.putExtra(ServiceActivity.EXTERNAL_URL, url);
        intent.putExtra(ServiceActivity.EXTERNAL_TITLE, title);
        startActivity(intent);
    }

    private void startMeetingCheckInActivity(String userJid, String name, String mid) {
        Intent meetingIntent = new Intent(getActivity(), MeetingCheckInActivity.class);
        meetingIntent.putExtra(MeetingCheckInActivity.EXTRA_JID, userJid);
        meetingIntent.putExtra(MeetingCheckInActivity.EXTRA_TITLE, name);
        meetingIntent.putExtra(MeetingCheckInActivity.EXTRA_MID, mid);

        startActivity(meetingIntent);
    }

    private void startClassroomCheckInActivity(String userJid, String name) {
        Intent classroomIntent = new Intent(getActivity(), ClassroomCheckInActivity.class);
        classroomIntent.putExtra(ClassroomCheckInActivity.EXTRA_JID, userJid);
        classroomIntent.putExtra(ClassroomCheckInActivity.EXTRA_TITLE, name);
        startActivity(classroomIntent);
    }

    private void setAdapter() {
        serviceAdapter = new SimpleAdapter(getActivity(), itemList, R.layout.item_serivce,
                new String[]{"fnid", "title"},
                new int[]{R.id.item_service_jid, R.id.item_service_name});
        serviceListView.setAdapter(serviceAdapter);
    }
}