package com.cyber.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RowItem {
    private String title;
    private Date date;

    public RowItem() {
    }

    public RowItem(String title) {
        this.title = title;
        this.date = Calendar.getInstance().getTime();
    }

    public RowItem(String title, Date date) {
        this.title = title;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date!=null? date: new Date();
    }
}
