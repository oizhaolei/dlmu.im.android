package com.ruptech.chinatalk.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class AppVersion implements Serializable {
    private static final long serialVersionUID = 6055705418910210761L;
    public String appUrl = "";
    public int verCode = 0;
    public String verName = "";


    public String imHost;
    public int imPort;


    public static AppVersion parse(JSONObject verInfo) throws JSONException {
        AppVersion info = new AppVersion();

        info.appUrl = verInfo.getString("apkUrl");
        info.verName = verInfo.getString("verName");
        info.verCode = verInfo.getInt("verCode");

        JSONObject im = verInfo.getJSONObject("im");
        info.imHost = im.getString("host");
        info.imPort = im.getInt("port");

        return info;
    }


    public static String getPortraitUrl(String no) {
        return String.format("http://ecard.dlmu.edu.cn/ecard/photo/%s.jpg",
                no);
    }

    @Override
    public String toString() {
        return verCode + ", " + verName;
    }

}
