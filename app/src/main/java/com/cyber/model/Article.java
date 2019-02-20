package com.cyber.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;

import com.cyber.fastnotes.service.SharedTypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Article implements RowItem{

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public Date date;

    @Ignore
    List<ArticleItem> items;

    public Article(){
        title = "";
        date = new Date();
        items = new ArrayList<>();
    }

    @Ignore
    public Article(String title){
        super();
        this.title = title;
    }

    public ArticleItem add(ArticleItem item){
        item.articleId = id;
        items.add(item);
        return item;
    }

    public ArticleItem.Text add(String text){
        ArticleItem.Text item = new ArticleItem.Text(text);
        add(item);
        return item;
    }

    public ArticleItem.Image add(Bitmap bitmap){
        ArticleItem.Image item = new ArticleItem.Image(bitmap);
        add(item);
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
        this.items = items;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
