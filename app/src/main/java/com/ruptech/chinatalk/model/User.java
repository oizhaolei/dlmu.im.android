package com.ruptech.chinatalk.model;

import com.ruptech.chinatalk.utils.AppPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * A data class representing Basic user information element
 */
public class User extends Item implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1311784597606188334L;

    public String password;
    public String username;
    public String fullname;
    public String deptid;
    public String deptname;
    public String block;

    public User() {
    }

    public User(JSONObject json) throws JSONException {
        id = System.currentTimeMillis();

        username = json.optString("userid");
        password = json.optString("passwd");
        fullname = json.optString("xm");
        deptid = json.optString("deptid");
        deptname = json.optString("deptname");
    }

    public User(String username, String fullname) {
        id = System.currentTimeMillis();

        this.username = username;
        this.fullname = fullname;
    }

    public static String getUsernameFromJid(String jid) {
        try {
            int start = 0;
            int end = jid.indexOf('@');
            return jid.substring(start, end);
        } catch (Exception e) {
            return jid;
        }
    }

    public static boolean isOrg(String jid) {
        return getUsernameFromJid(jid).length() == 6;
    }

    public static boolean isTeacher(String jid) {
        return getUsernameFromJid(jid).length() == 8;
    }

    public static boolean isStudent(String jid) {
        return getUsernameFromJid(jid).length() == 10;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getJid() {
        return getUsername() + "@" + AppPreferences.IM_SERVER_RESOURCE;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
