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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 */
public class ClassroomCheckInActivity extends ActionBarActivity {

    public static final String EXTRA_JID = "EXTRA_JID";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    static final String TAG = Utils.CATEGORY
            + ServiceActivity.class.getSimpleName();

    @InjectView(R.id.activity_meeting_checkin_button)
    Button checkin_start_button;
    @InjectView(R.id.activity_meeting_checkin_Log_textview)
    TextView checkin_log_textview;
    @InjectView(R.id.activity_meeting_checkin_info_textview)
    TextView checkin_ifno_textview;

    private ProgressDialog progressDialog;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private boolean start = false;// 是否开始刷卡了

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
        Intent intent = getIntent();
        String jid = intent.getStringExtra("EXTRA_JID");
        String name = intent.getStringExtra("EXTRA_TITLE");

        System.out.println("<<<<<<<"+jid+"<<<"+name);
        setContentView(R.layout.activity_meeting_checkin);
        ButterKnife.inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ClassroomCheckInActivity.this);
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在和服务器通讯...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        checkin_log_textview.setMovementMethod(ScrollingMovementMethod.getInstance());
        checkin_start_button.setClickable(true);

        if (mAdapter == null) {
            checkin_log_textview.setText("没有NFC模块");
            checkin_start_button.setClickable(false);
        }

        if (!mAdapter.isEnabled()) {
            checkin_log_textview.setText("NFC模块未启用");
            checkin_start_button.setClickable(false);
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        checkin_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = !start;
                if (start) {
                    checkin_start_button.setText("停止签到");
                    //checkin_log_textview.setText("");
                } else {
                    checkin_start_button.setText("开始签到");
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
            System.out.println("current:" + cardPhyId);
            boolean b = false;
            if (null != cardPhyId) {
                //TODO 检查后台是否已经签到，如果未签到，则保存签到日志
                String url_query = "http://202.118.89.129/dlmu_rest_webservice/CI0101?mid=1&token=1&cardphyid=" + cardPhyId;
                String url_save = "http://202.118.89.129/dlmu_rest_webservice/CI0111";
                try {
                    //System.out.println(url_query);
                    String meetinguser = new queryMeetingUserTask().execute(url_query).get();
                    //System.out.println(meetinguser);
                    JSONObject json = new JSONObject(meetinguser);
                    Integer code = json.getInt("code");
                    if (code != 99)
                        Toast.makeText(this, json.getString("msg"), Toast.LENGTH_SHORT).show();
                    JSONObject data = json.getJSONObject("data");

                    if (data.getInt("muflag") == 0) {
                        String save = new saveLogTask().execute(url_save, "1", data.getString("muuserid"), data.getString("muusername"), data.getString("mudeptcode"), data.getString("mudeptname"), App.readUser().getUsername()).get();
                        System.out.println(save);
                        checkin_log_textview.setText("成功->" + data.getString("muusername") + "@" + data.getString("mudeptname") + "\r\n" + checkin_log_textview.getText());

                    } else {
                        checkin_log_textview.setText("重复->" + data.getString("muusername") + "@" + data.getString("mudeptname") + "\r\n" + checkin_log_textview.getText());
                    }
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
            } else {
                Toast.makeText(this, "刷卡失败，请重刷", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getCardID(Intent intent) {
        byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        return Converter.getHexString(myNFCID, myNFCID.length);
    }

    public class queryMeetingUserTask extends AsyncTask<String, String, String> {
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
                    //System.out.println(rtn);


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
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpRequest = new HttpPost(params[0]);
            List<NameValuePair> p = new ArrayList<NameValuePair>();
            String strResult = null;
            System.out.println((String) params[0]);
            System.out.println((String) params[1]);
            System.out.println((String) params[2]);
            System.out.println((String) params[3]);
            System.out.println((String) params[4]);
            System.out.println((String) params[5]);
            System.out.println((String) params[6]);

            p.add(new BasicNameValuePair("token", "1"));
            p.add(new BasicNameValuePair("mid", params[1]));
            p.add(new BasicNameValuePair("muuserid", params[2]));
            p.add(new BasicNameValuePair("muusername", params[3]));
            p.add(new BasicNameValuePair("mudeptcode", params[4]));
            p.add(new BasicNameValuePair("mudeptname", params[5]));
            p.add(new BasicNameValuePair("mciterminal", params[6]));

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
