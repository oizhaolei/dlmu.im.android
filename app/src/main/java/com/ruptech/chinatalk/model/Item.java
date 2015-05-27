package com.ruptech.chinatalk.model;

import java.io.Serializable;
import java.util.Date;

public abstract class Item implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2394068598151253815L;
    public Date create_date;
    protected long id;

    public Date getCreate_date() {
        return create_date;
    }

    public void setCreate_date(Date create_date) {
        this.create_date = create_date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
