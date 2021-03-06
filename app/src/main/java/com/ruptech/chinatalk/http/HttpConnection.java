package com.ruptech.chinatalk.http;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.kevinsawicki.http.HttpRequest.UploadProgress;
import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.Service;
import com.ruptech.chinatalk.utils.AppVersion;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.BuildConfig;
import com.ruptech.dlmu.im.R;

import org.apache.http.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class HttpConnection {
    private static final String ANONYMOS_USER_ID = "3637";
    /**
     * 纪录最近n次url访问
     */
    private static LimitedQueue<String> urlHistory = new LimitedQueue<String>(
            5);
    private final String TAG = Utils.CATEGORY
            + HttpConnection.class.getSimpleName();

    public HttpConnection() {
        super();
    }

    public static String getUrlHistory() {
        StringBuffer sb = new StringBuffer();

        for (String url : urlHistory) {
            sb.append(url).append('\n');
        }
        return sb.toString();
    }

    private static String encodeParameters(Map<String, String> params)
            throws RuntimeException {
        StringBuffer buf = new StringBuffer();
        String[] keyArray = params.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);
        int j = 0;
        for (String key : keyArray) {
            String value = params.get(key);
            if (j++ != 0) {
                buf.append("&");
            }
            if (!Utils.isEmpty(value)) {
                try {
                    buf.append(URLEncoder.encode(key, "UTF-8")).append("=")
                            .append(URLEncoder.encode(value, "UTF-8"));
                } catch (java.io.UnsupportedEncodingException neverHappen) {
                    // throw new RuntimeException(neverHappen.getMessage(),
                    // neverHappen);
                }
            }
        }

        return buf.toString();
    }

    public static Map<String, String> genParams(Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        String loginid = ANONYMOS_USER_ID;
        if (App.readUser() != null) {
            loginid = String.valueOf(App.readUser().getId());
        }
        params.put("source", getSource());
        params.put("loginid", loginid);
        String sign = Utils.genSign(params, loginid);
        params.put("sign", sign);

        return params;
    }

    private static String getSource() {
        return "an-" + Utils.getAppVersionCode();
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }


    public static List toServiceList(JSONArray array) throws JSONException {
        List<Service> list = new ArrayList<Service>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONObject) {
                Service s = new Service();
                s.setChecked(((JSONObject) value).getInt("checked"));
                s.setFixed(((JSONObject) value).getInt("fixed"));
                s.setFnid(((JSONObject) value).getString("fnid"));
                s.setIcon(((JSONObject) value).getString("icon"));
                s.setParam(((JSONObject) value).getString("param"));
                if (s.getChecked() == 1)
                    s.setPos(((JSONObject) value).getInt("pos"));
                else s.setPos(99);
                s.setTitle(((JSONObject) value).getString("title"));
                s.setTypeid(((JSONObject) value).getInt("typeid"));
                s.setUrl(((JSONObject) value).getString("url"));
                list.add(s);
            }
        }
        return list;
    }

    public Response get(String url) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url);
        }
        urlHistory.add(url);

        String body = HttpRequest.get(url).body();

        return new Response(body);
    }

    public Response post(String url, Map<String, String> postParams) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url + ", " + postParams);
        }
        urlHistory.add(url + ", " + postParams);

        String body = HttpRequest.post(url).form(postParams).body();

        return new Response(body);
    }

    public Response uploadFile(String url, File file,
                               final UploadProgress uploadProgress) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url + ", " + file.getName());
        }
        urlHistory.add(url + ", " + file.getName());

        String body = HttpRequest.post(url).part("file", file.getName(), file)
                .progress(uploadProgress).body();

        return new Response(body);
    }

    /**
     * Returns the base URL
     *
     * @param ifPage 业务接口名
     * @return the base URL
     */

    public String genRequestURL(String ifPage, Map<String, String> params) {

        String url = ifPage;
        if (!url.startsWith("http")) {
            url = getAppServerUrl() + url;
        }
        url += "?" + encodeParameters(params);
        //System.out.println(">>>>>>>>>>>>>>>>" + url);
        return url;
    }

    /**
     * Issues an HTTP GET request.
     *
     * @return the response
     * @throws HttpException
     */
    protected Response _get(String ifPage, Map<String, String> params)
            throws Exception {
        params = genParams(params);

        String url = "";
        for (int i = 0; i < 2; i++) {
            url = genRequestURL(ifPage, params);
            try {
                Response response = get(url);
                return response;
            } catch (HttpRequestException e1) {
                // App.setServerAppInfo(null);
                // Utils.sendClientException(e1);
            }
        }

        throw new NetworkException(
                App.mContext.getString(R.string.network_is_bad));
    }

    public AppVersion ver() throws Exception {
        String url = App.properties.getProperty("SERVER_BASE_URL") + "ver";
        try {
            Response res = get(url);
            JSONObject verInfo = res.asJSONObject();
            AppVersion info = AppVersion.parse(verInfo);
            return info;
        } catch (Exception e) {
            if (BuildConfig.DEBUG)
                Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    abstract protected String getAppServerUrl();

    protected Response _post(String ifPage, Map<String, String> form)
            throws Exception {
        Map<String, String> params = genParams(null);

        String url = null;
        for (int i = 0; i < 5; i++) {
            url = genRequestURL(ifPage, params);
            try {
                Response response = post(url, form);
                return response;
            } catch (HttpRequestException e1) {
                // App.setServerAppInfo(null);
                Utils.sendClientException(e1);
            }
        }
        throw new NetworkException(
                App.mContext.getString(R.string.network_is_bad));
    }

    protected Response _uploadFile(String ifPage, File file,
                                   UploadProgress uploadProgress) throws Exception {
        Map<String, String> params = genParams(null);

        for (int i = 0; i < 5; i++) {
            String url = genRequestURL(ifPage, params);
            try {
                Response response = uploadFile(url, file, uploadProgress);
                return response;
            } catch (HttpRequestException e1) {
                // App.setServerAppInfo(null);
                Utils.sendClientException(e1);
            }
        }
        throw new NetworkException(
                App.mContext.getString(R.string.network_is_bad));
    }

    static class LimitedQueue<E> extends LinkedList<E> {
        private static final long serialVersionUID = -654911332280856680L;
        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }
            return true;
        }
    }

}
