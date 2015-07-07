package com.ruptech.chinatalk.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dongdxy.android.ui.dragreordergridview.DragReorderGridView;
import com.dongdxy.android.ui.dragreordergridview.DragReorderListAdapter;
import com.dongdxy.android.ui.dragreordergridview.DragReorderListener;
import com.dongdxy.android.ui.dragreordergridview.EditActionListener;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServiceListTask;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.ui.ClassroomCheckInActivity;
import com.ruptech.chinatalk.ui.MeetingCheckInActivity;
import com.ruptech.chinatalk.ui.ServiceActivity;
import com.ruptech.chinatalk.ui.ServiceSettingActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;

public class ServiceFragment extends Fragment {

    private static final String TAG = Utils.CATEGORY
            + ServiceFragment.class.getSimpleName();

    private ColorAdapter mAdapter;
    private DragReorderGridView mGridView;
    private List<Item> mItems;

    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.main_tab_service, container, false);
        mGridView = (DragReorderGridView) v.findViewById(R.id.service_list_gridview);
        //initData();
        mGridView.setDragReorderListener(mDragReorderListener);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                if (mItems.get(pos).title.endsWith("+")) {
                    Intent intent = new Intent(getActivity(), ServiceSettingActivity.class);
                    intent.putExtra(ServiceActivity.EXTERNAL_TITLE, "服务设置");
                    startActivity(intent);
                    return;
//                    Item item = new Item();
//                    int insertPos = mItems.size() - 1;
//                    item.title = "" + insertPos;
//                    mItems.add(insertPos, item);
//                    mAdapter.notifyDataSetChanged();
//                    return;
                }

                String fnid = mItems.get(pos).fnid;
                String title = mItems.get(pos).title;
                String url = mItems.get(pos).url;
                String[] params = new String[]{};
                if (null != mItems.get(pos).param) {
                    params = mItems.get(pos).param.split(";");
                }

                switch (mItems.get(pos).type) {
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
                                    startClassroomCheckInActivity(fnid, title);
                                }
                                if (url.startsWith("MEETING")) {
                                    String mid = url.substring(url.indexOf("_") + 1);
                                    startMeetingCheckInActivity(fnid, title, mid);
                                }
                            }
                        }
                        break;
                    case 1:
                        url = Utils.genUrl(Utils.genParam(params), url);
                        System.out.println(">>>>>>" + url);
                        startServiceActivity(url, title);
                        break;
                    case 2:
                        Toast.makeText(getActivity(), "开发中……", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        startChatActivity("19971053@im.dlmu.edu.cn", "网络报修");
                        break;
                    default:
                        Toast.makeText(getActivity(), "开发中……", Toast.LENGTH_SHORT).show();

                }
            }
        });

        mGridView.enableEditMode(R.id.delete_icon, new EditActionListener() {

            @Override
            public void onEditAction(int position) {
                Toast.makeText(getActivity(), "deleting " + mAdapter.list.get(position).title, Toast.LENGTH_SHORT).show();
                mAdapter.list.remove(position);
                syncAppService();
                mAdapter.notifyDataSetChanged();
            }
        });

        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onDestroy() {
        //保存Service配置

        try {
            new saveServiceSettingTask().execute("http://202.118.89.129/dlmu_rest_webservice/000002").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showNormalActionBar(getActivity());
        retrieveServiceList();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setupChatLayout();

        retrieveServiceList();
    }

    private void retrieveServiceList() {
        if (App.service == null || App.service.size() == 0) {
            RetrieveServiceListTask RetrieveServiceListTask = new RetrieveServiceListTask(App.readUser().getUsername(), "3");
            TaskAdapter taskListener = new TaskAdapter() {
                @Override
                public void onPostExecute(GenericTask task, TaskResult result) {
                    super.onPostExecute(task, result);
                    RetrieveServiceListTask RetrieveServiceListTask = (RetrieveServiceListTask) task;

                    if (result == TaskResult.OK) {
                        App.service = RetrieveServiceListTask.getServiceList();
                        mItems = new ArrayList<Item>();
                        int i = 0;
                        for (Service m : App.service) {
                            if (m.getChecked() == 1) {
                                Item item = new Item();
                                item.title = m.getTitle();
                                item.fnid = m.getFnid();
                                item.color = Color.rgb(255, 255, 255);
                                item.icon = m.getIcon();
                                item.type = m.getTypeid();
                                item.url = m.getUrl();
                                item.param = m.getParam();
                                item.isFixed = m.getFixed() == 0 ? false : true;
                                mItems.add(i, item);
                                i = i + 1;
                            }
                        }
                        Item addBtn = new Item();
                        addBtn.title = "+";
                        addBtn.fnid = "";
                        addBtn.type = -1;
                        addBtn.isFixed = true;
                        addBtn.color = Color.rgb(0xa0, 0xa0, 0xa0);
                        mItems.add(addBtn);
                        setAdapter();
                    }
                }

            };

            RetrieveServiceListTask.setListener(taskListener);

            RetrieveServiceListTask.execute();
        } else {
            mItems = new ArrayList<Item>();
            Collections.sort(App.service, new Comparator<Service>() {
                public int compare(Service arg0, Service arg1) {
                    return arg0.getPos() - arg1.getPos();
                }
            });

            int i = 0;
            for (Service m : App.service) {
                if (m.getChecked() == 1) {
                    Item item = new Item();
                    item.title = m.getTitle();
                    item.fnid = m.getFnid();
                    item.url = m.getUrl();
                    item.color = Color.rgb(255, 255, 255);
                    item.icon = m.getIcon();
                    item.param = m.getParam();
                    item.type = m.getTypeid();
                    item.isFixed = m.getFixed() == 0 ? false : true;
                    mItems.add(i, item);
                    i = i + 1;
                }
            }
            Item addBtn = new Item();
            addBtn.title = "+";
            addBtn.fnid = "";
            addBtn.isFixed = true;
            addBtn.type = -1;
            addBtn.color = Color.rgb(0xa0, 0xa0, 0xa0);
            mItems.add(addBtn);
            setAdapter();
        }
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
        mAdapter = new ColorAdapter(mItems);
        mGridView.setAdapter(mAdapter);
    }

    public DragReorderListener mDragReorderListener = new DragReorderListener() {

        @Override
        public void onReorder(int fromPosition, int toPosition) {
            ((ColorAdapter) mGridView.getAdapter()).reorder(fromPosition, toPosition);
        }

        @Override
        public void onDragEnded() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onItemLongClicked() {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        }

    };

    private class Item {
        String title;
        String fnid;
        String url;
        String param;
        Integer type;
        String icon;
        int color;
        boolean isFixed = false;
    }

    private class ColorAdapter extends BaseAdapter implements DragReorderListAdapter {

        List<Item> list;

        public ColorAdapter(List<Item> list) {
            this.list = list;
        }

        public void reorder(int from, int to) {
            if (from != to) {
                //App.service.get(from).setPos(to);
                Item item = list.remove(from);
                list.add(to, item);
                //Service s = App.service.remove(from);
                //App.service.add(to, s);
                notifyDataSetChanged();
                syncAppService();
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup result = (ViewGroup) convertView;
            if (result == null) {
                result = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.item_service_gridview, parent, false);
            }

            TextView textView = (TextView) result.findViewById(R.id.text);
            ImageView imageView = (ImageView) result.findViewById(R.id.imageViewIcon);
            if (!list.get(position).title.endsWith("+")) {
                Utils.setUserPicImage(imageView, list.get(position).icon);
            }

            textView.setText(list.get(position).title);
            result.setBackgroundColor(list.get(position).color);
            return result;
        }

        @Override
        public boolean isReorderableItem(int position) {
            return !list.get(position).isFixed;
        }

    }

    private void syncAppService() {
        for (Service s : App.service) {
            s.setChecked(0);
            s.setPos(99);
        }
        for (int i = 0; i < mItems.size(); i++) {
            Item m = mItems.get(i);
            for (Service s : App.service) {
                if (s.getFnid().equals(m.fnid)) {
                    s.setPos(i);
                    s.setChecked(1);
                }
            }
        }
    }

    public class saveServiceSettingTask extends AsyncTask<String, List<Service>, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpPost httpRequest = new HttpPost(params[0]);
            List<NameValuePair> p = new ArrayList<NameValuePair>();
            String strResult = null;
            Collections.sort(App.service, new Comparator<Service>() {
                public int compare(Service arg0, Service arg1) {
                    return arg0.getPos() - arg1.getPos();
                }
            });
            for (int i = 0; i < App.service.size(); i++) {
                if (App.service.get(i).getChecked() == 1) {
                    p.add(new BasicNameValuePair("fnid", App.service.get(i).getFnid()));
                    p.add(new BasicNameValuePair("pos", new Integer(i).toString()));
                }
            }
            p.add(new BasicNameValuePair("userid", App.readUser().getUsername()));
            try {
                // 发出HTTP request
                httpRequest.setEntity(new UrlEncodedFormEntity(p, HTTP.UTF_8));
                // 取得HTTP response
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
                System.out.println(">>>>>" + httpResponse.getStatusLine().getStatusCode());
                // 若状态码为200 ok
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 取出回应字串
                    strResult = EntityUtils.toString(httpResponse.getEntity());
                    System.out.println(">>>>>-----" + strResult);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return strResult;
        }
    }
}