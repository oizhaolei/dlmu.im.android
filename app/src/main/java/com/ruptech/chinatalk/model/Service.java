package com.ruptech.chinatalk.model;

import java.io.Serializable;

/**
 * Created by gaol on 2015-07-06.
 */
public class Service implements Serializable, Comparable<Service> {

    private String fnid;
    private String title;
    private String url;
    private String param;
    private String icon;
    private int color;
    private int typeid;
    private int pos;
    private int fixed;
    private int checked;
    private boolean isfixed;



    public String getFnid() {
        return fnid;
    }

    public void setFnid(String fnid) {
        this.fnid = fnid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getTypeid() {
        return typeid;
    }

    public void setTypeid(int typeid) {
        this.typeid = typeid;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }


    public int getChecked() {
        return checked;
    }

    public void setChecked(int checked) {
        this.checked = checked;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getFixed() {
        return fixed;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    @Override
    public int compareTo(Service s) {
        return Integer.compare(this.getPos(), s.getPos());
    }

    public boolean getIsfixed() {
        return isfixed;
    }

    public void setIsfixed(boolean isfixed) {
        this.isfixed = isfixed;
    }
}
