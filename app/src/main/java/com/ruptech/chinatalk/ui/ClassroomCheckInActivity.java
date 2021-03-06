package com.ruptech.chinatalk.ui;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.adapter.ListViewRadioAdapter;
import com.ruptech.chinatalk.utils.Converter;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class ClassroomCheckInActivity extends ActionBarActivity {

    public static final String EXTRA_JID = "EXTRA_JID";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    static final String TAG = Utils.CATEGORY
            + ServiceActivity.class.getSimpleName();
    private final String JSH = "19971053";
    private final String KCH = "13608400";
    private final String ZXJXJHH = "2014-2015-2-1";
    @InjectView(R.id.activity_checkin_classroom_button)
    Button checkin_start_button;
    @InjectView(R.id.activity_checkin_classroom_textview)
    TextView checkin_log_textview;
    private ListView radioButtonList;
    private ListViewRadioAdapter adapter;
    private List<Map<String, String>> data;
    private int index = -1;
    private ProgressDialog progressDialog;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private boolean start = false;// 是否开始刷卡了
    private String mTitle;
    private String ssDate = "";

    @Override
    public void onBackPressed() {
        finish();
    }

    private void gotoSplashActivity() {
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.readUser() == null) {
            gotoSplashActivity();
            finish();
            return;
        }
        progressDialog = new ProgressDialog(ClassroomCheckInActivity.this);
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在和服务器通讯...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        data = new ArrayList<>();
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
        ssDate = sdf.format(new Date());


        Intent intent = getIntent();
        String jid = intent.getStringExtra("EXTRA_JID");
        String name = intent.getStringExtra("EXTRA_TITLE");

        setContentView(R.layout.activity_checkin_classroom);

        initJWKCB("19971053", ZXJXJHH);

        radioButtonList = (ListView) findViewById(R.id.activity_checkin_classroom_listview);
        adapter = new ListViewRadioAdapter(this, data);
        radioButtonList.setAdapter(adapter);
        radioButtonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //parent.getAdapter().getItem(position);
                //System.out.println("============" + parent.getAdapter().getItem(position));
                RadioButton rd = (RadioButton) view.findViewById(R.id.item_checkin_classroom_listview_radio);
                if (rd.isChecked()) {
                    rd.setChecked(false);
                    index = -1;
                } else {
                    index = position;
                    rd.setChecked(true);
                }
                //System.out.println(index);
            }
        });

        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        checkin_log_textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        checkin_start_button.setClickable(true);

        if (mAdapter == null) {
            checkin_log_textview.setText(R.string.nfc_nomodule);
            checkin_start_button.setClickable(false);
        }

        if (!mAdapter.isEnabled()) {
            checkin_log_textview.setText(R.string.nfc_disabled);
            checkin_start_button.setClickable(false);
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        checkin_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index != -1) {
                    start = !start;
                    if (start) {
                        radioButtonList.setEnabled(false);
                        checkin_start_button.setText(R.string.checkin_stop);
                    } else {
                        radioButtonList.setEnabled(true);
                        checkin_start_button.setText(R.string.checkin_start);
                    }
                } else {
                    Toast.makeText(ClassroomCheckInActivity.this.getApplicationContext(), "请先选择所上课程", Toast.LENGTH_SHORT).show();
                }
            }
        });

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[]{ndef,};
        mTechLists = new String[][]{new String[]{MifareClassic.class.getName()}};
        mTitle = (String) getIntent().getExtras().get(EXTRA_TITLE);
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (start) {
            String cardPhyId = getCardID(intent);
            System.out.println(">>>>>>>>>>current:" + cardPhyId);
            System.out.println(">>>>>>>>>>current:" + index);
            if (null != cardPhyId) {
                String url_save = "http://202.118.89.129/dlmu_rest_webservice/400201";
                String save = null;
                try {
                    save = new saveLogTask().execute(url_save,
                            "1",
                            data.get(index).get("ZXJXJHH"),
                            "19971053",
                            data.get(index).get("SKXQ"),
                            data.get(index).get("SKJC"),
                            data.get(index).get("KCH"),
                            data.get(index).get("KCM"),
                            data.get(index).get("ZCSM"),
                            data.get(index).get("JXLH"),
                            data.get(index).get("JXLM"),
                            data.get(index).get("JASH"),
                            data.get(index).get("JASM"),
                            cardPhyId,//cardPhyId,//"251D0E85",
                            App.readUser().getUsername(),
                            ssDate).get();

                    System.out.println("-------------------" + save);
                    JSONObject json = new JSONObject(save);
                    Integer code = json.getInt("code");
                    if (code == 99) {
                        checkin_log_textview.setText(json.getString("msg") + "\r\n" + checkin_log_textview.getText());
                        //Toast.makeText(this, json.getString("msg"), Toast.LENGTH_SHORT).show();
                        //return false;
                    } else {
                        Toast.makeText(this, json.getString("msg"), Toast.LENGTH_SHORT).show();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统异常，请稍后再试", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "系统异常，请稍后再试", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "刷卡失败，请重刷", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public String getCardID(Intent intent) {
        byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        return Converter.getHexString(myNFCID, myNFCID.length);
    }


    public boolean initJWKCB(String JSH, String ZXJXJHH) {
        String url_query = "http://202.118.89.129/dlmu_rest_webservice/200203?token=1&jsh=" + JSH + "&zxjxjhh=" + ZXJXJHH;
        try {
            System.out.println(url_query);
            String obj = new getJwKcb4TeacherTask().execute(url_query, JSH, ZXJXJHH).get();
            System.out.println(obj);
            JSONObject json = new JSONObject(obj);
            Integer code = json.getInt("code");
            if (code != 99) {
                Toast.makeText(this, json.getString("msg"), Toast.LENGTH_SHORT).show();
                return false;
            }
            JSONArray array = json.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                Map<String, String> m = new HashMap<>();
                JSONObject j = array.getJSONObject(i);
                m.put("ZXJXJHH", j.getString("zxjxjhh"));
                m.put("KCH", j.getString("kch"));
                m.put("KCM", j.getString("kcm"));
                m.put("SKXQ", j.getString("skxq"));
                m.put("SKJC", j.getString("skjc"));
                m.put("ZCSM", j.getString("zcsm"));
                m.put("XQH", j.getString("xqh"));
                m.put("XQM", j.getString("xqm"));
                m.put("JXLH", j.getString("jxlh"));
                m.put("JXLM", j.getString("jxlm"));
                m.put("JASH", j.getString("jash"));
                m.put("JASM", j.getString("jasm"));
                m.put("TITLE", j.getString("kcm") + "/周" + j.getString("skxq") + "/第" + j.getString("skjc") + "节/" + j.getString("jasm"));
                data.add(m);
            }
            //checkin_log_textview.setText("成功读取课表");
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "系统异常，请稍后再试", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "系统异常，请稍后再试", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "系统异常，请稍后再试", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public class getJwKcb4TeacherTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(params[0]);
            String rtn = null;
            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    rtn = EntityUtils.toString(httpEntity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                httpClient.getConnectionManager().shutdown();
            }
            return rtn;
        }
    }

    public class saveLogTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpPost httpRequest = new HttpPost(params[0]);
            List<NameValuePair> p = new ArrayList<NameValuePair>();
            String strResult = null;

            p.add(new BasicNameValuePair("token", params[1]));
            p.add(new BasicNameValuePair("zxjxjhh", params[2]));
            p.add(new BasicNameValuePair("jsh", params[3]));
            p.add(new BasicNameValuePair("skxq", params[4]));
            p.add(new BasicNameValuePair("skjc", params[5]));
            p.add(new BasicNameValuePair("kch", params[6]));
            p.add(new BasicNameValuePair("kcm", params[7]));
            p.add(new BasicNameValuePair("zcsm", params[8]));
            p.add(new BasicNameValuePair("jxlh", params[9]));
            p.add(new BasicNameValuePair("jxlm", params[10]));
            p.add(new BasicNameValuePair("jash", params[11]));
            p.add(new BasicNameValuePair("jasm", params[12]));
            p.add(new BasicNameValuePair("cardphyid", params[13]));
            p.add(new BasicNameValuePair("qdterminal", params[14]));
            p.add(new BasicNameValuePair("qddate", params[15]));
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
