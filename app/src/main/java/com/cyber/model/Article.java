package com.cyber.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;
import android.util.Log;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.service.SharedTypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Article implements RowItem{
    public static final long NO_ID = Long.MIN_VALUE;

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public Date date;

    @Ignore
    List<ArticleItem> items;

    public Article(){
        id = NO_ID;
        title = "";
        date = new Date();
        items = new ArrayList<>();
    }

    public boolean isNew(){
        return id==NO_ID;
    }

    public ArticleItem add(ArticleItem item){
        item.articleId = id;
        items.add(item);
        return item;
    }

    public ArticleItem get(int index){
        return items.get(index);
    }

    public void remove(int index){
        items.remove(index);
    }

    public int size(){
        return items.size();
    }

    public List<ArticleItem> getItems() {
        return items;
    }

    public void setItems(List<ArticleItem> items) {
        if (items!=null) this.items = items;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
