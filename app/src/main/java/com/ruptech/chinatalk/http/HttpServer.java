package com.ruptech.chinatalk.http;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class HttpServer extends HttpConnection {
    private final String TAG = Utils.CATEGORY
            + HttpServer.class.getSimpleName();


    private User _parseUser(JSONObject result, Response res) throws Exception {
        if (result.getBoolean("success")) {
            JSONObject data0 = (JSONObject) result.getJSONArray("data").get(0);
            return new User(data0);
        } else {
            throw new ServerSideException(result.getString("msg"));
        }
    }


    public User changeUserProfile(String mFunc, String mKey, String mValue)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("func", mFunc);
        params.put("userid", String.valueOf(App.readUser().getId()));
        if ("change_prop".equals(mFunc)) {
            params.put("key", mKey);
            params.put("value", mValue);
        } else {
            if (mKey.equals("username")) {
                params.put("username", mValue);
            } else if (mKey.equals("fullname")) {
                params.put("fullname", mValue);
            } else if (mKey.equals("lang")) {
                params.put("lang", mValue);
            } else if (mKey.equals("gender")) {
                params.put("gender", mValue);
            } else if (mKey.equals("photo_name")) {
                params.put("photo_name", mValue);
            } else if (mKey.equals("user_memo")) {
                params.put("user_memo", mValue);
            }
        }

        Response res = _get("user/user_change_profile.php", params);
        JSONObject result = res.asJSONObject();
        return _parseUser(result, res);
    }


    private Map<String, String> convertJsonItem(JSONObject jo)
            throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jo.getString(key));
        }

        return map;
    }


    @Override
    protected String getAppServerUrl() {
        return App.properties.getProperty("SERVER_BASE_URL");
    }


    /**
     * @throws Exception
     */

    public List<String> sendGroupMessage(String fromJid, String toGroupJid, String subject, String body)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("from_jid", fromJid);
        params.put("to_group", toGroupJid);
        params.put("subject", subject);
        params.put("body", body);

        Response res = _get("send", params);
        JSONArray result = res.asJSONArray();
        List<String> sends = new ArrayList<>(result.length());
        for (int i = 0; i < result.length(); i++) {
            String memberId = result.getString(i);
            sends.add(memberId);
        }
        return sends;
    }

    /**
     * @throws Exception
     */

    public User login(String userid, String password, boolean encrypt)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);
        params.put("password", password);
        params.put("encrypt", String.valueOf(encrypt));
        params.put("serial", String.valueOf(android.os.Build.SERIAL));

        Response res = _get("login", params);
        JSONObject result = res.asJSONObject();
        String code = result.getString("code");
        if ("99".equals(code)) {
            JSONObject user = result.getJSONObject("user");
            User u = new User(user);
            JSONObject props = result.getJSONObject("props");
            Map<String, Object> properties = jsonToMap(props);
            u.setProperties(properties);
            return u;
        }
        throw new Exception("login error");
    }

    public Map retrieveOrgList(String parentJid, String isStudent)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("jid", parentJid);
        params.put("student", isStudent);

        Response res = _get("org", params);
        JSONObject result = res.asJSONObject();
        return toMap(result);

    }

    public List retrieveServiceList(String userid,String ctrl)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);
        params.put("ctrl", ctrl);
        Response res = _get("service", params);
        JSONObject result = res.asJSONObject();
        System.out.println("---------------");
        System.out.println(result.toString());
        System.out.println("---------------");
        return toServiceList(result.getJSONArray("data"));

    }

    public User retrieveUser(String userid)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);

        Response res = _get("user", params);
        JSONObject result = res.asJSONObject();
        String code = result.getString("code");
        if ("99".equals(code)) {
            JSONObject user = result.getJSONObject("user");
            User u = new User(user);
            JSONObject props = result.getJSONObject("props");
            Map<String, Object> properties = jsonToMap(props);
            u.setProperties(properties);
            return u;
        }
        throw new Exception("login error");

    }

    public Properties userProperties(String username)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userid", username);

        Response res = _get("userproperties", params);
        JSONObject result = res.asJSONObject();

        Properties props = new Properties();
        Iterator<String> keys = result.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = result.getString(key);

            props.put(key, value);
        }
        return props;
    }

    //    public String userProperty(String username, String key)
//            throws Exception {
//        Map<String, String> params = new HashMap<>();
//        params.put("userid", username);
//        params.put("key", key);
//
//        Response res = _get("userproperties", params);
//        String value = res.getBody();
//
//        return value;
//    }
    public boolean userProperty(String username, String key, String value)
            throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("userid", username);
        params.put("key", key);
        params.put("value", value);

        Response res = _get("userproperties", params);
        value = res.getBody();

        return "success".equals(value);
    }
}
