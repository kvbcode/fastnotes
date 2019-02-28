package com.cyber.fastnotes.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
public class Article extends BasicModel implements RowItem{

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
        .append("state=").append(getStateString(getState())).append(", ")
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
