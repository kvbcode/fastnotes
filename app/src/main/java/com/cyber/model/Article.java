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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
public class Article implements RowItem{
    @PrimaryKey(autoGenerate = true)
    public Long id;

    public String title;

    public Date date;

    @Ignore
    List<ArticleItem> items;

    public Article(){
        title = "";
        date = new Date();
        items = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Article(")
        .append("id=").append(getId()).append(", ")
        .append("title='").append(getTitle()).append("', ")
        .append("date=").append(getDate()).append(", ")
        .append("items=").append(Arrays.toString(getItems().toArray()))
        .append(")");
        return sb.toString();
    }

    public ArticleItem add(ArticleItem item){
        item.setArticleId( getId() );
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
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public void setDate(Date date) {
        this.date = date;
    }
}
