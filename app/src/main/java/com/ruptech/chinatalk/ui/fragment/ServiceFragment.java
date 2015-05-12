package com.ruptech.chinatalk.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServiceListTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.ServiceActivity;
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
	private SimpleAdapter serviceAdapter;

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
	}
	@InjectView(R.id.service_list)
	ListView serviceListView;

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
		RetrieveServiceListTask RetrieveServiceListTask = new RetrieveServiceListTask( );
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

	private List<Map<String, Object>> itemList = new ArrayList<>();


	private void setupChatLayout() {

		serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> view, View arg1,
			                        int position, long id) {
				Map<String, Object> item = (Map<String, Object>) view.getAdapter().getItem(position);
				String jid = item.get("fnid").toString();
				String title = (String) item.get("title");
				String url = (String) item.get("url");
				if (Utils.isEmpty(url)) {
					//startChatActivity(jid, title);
                    startMeetingCheckInActivity(jid, title);
				} else {
					startServiceActivity(url);
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

	private void startServiceActivity(String url) {
		Intent intent = new Intent(getActivity(), ServiceActivity.class);
		intent.putExtra(ServiceActivity.EXTERNAL_URL, url);
		startActivity(intent);
	}

    private void startMeetingCheckInActivity(String userJid, String name) {
        Intent meetingIntent = new Intent(getActivity(), ChatActivity.class);
        meetingIntent.putExtra(ChatActivity.EXTRA_JID, userJid);
        meetingIntent.putExtra(ChatActivity.EXTRA_TITLE, name);
        startActivity(meetingIntent);
    }

	private void setAdapter() {
		serviceAdapter = new SimpleAdapter(getActivity(), itemList, R.layout.item_serivce,
				new String[]{"fnid", "title"},
				new int[]{R.id.item_service_jid, R.id.item_service_name});
		serviceListView.setAdapter(serviceAdapter);
	}
}