package com.cyber.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.net.Uri;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
    foreignKeys = @ForeignKey(entity = Article.class, parentColumns = "id", childColumns = "article_id", onDelete = CASCADE)
)
public class ArticleItem{
    public static final int TYPE_NONE = 0;
    public static final int TYPE_TEXT = 101;
    public static final int TYPE_IMAGE = 102;
    public static final int TYPE_AUDIO = 103;

    @PrimaryKey(autoGenerate = true)
    public Long id;

    public int type;

    @ColumnInfo(name = "article_id", index = true)
    public Long articleId;

    public String text;

    @Ignore
    Object payload;

    @Ignore
    boolean changed;

    public Uri contentUri;

    public ArticleItem() {
        this.type = TYPE_NONE;
        this.payload = null;
        this.contentUri = null;
        this.changed = false;
    }

    public static ArticleItem fromText(String text){
        ArticleItem item = new ArticleItem();
        item.setType(ArticleItem.TYPE_TEXT);
        item.setText(text);
        return item;
    }

    public static ArticleItem fromBitmap(Uri contentUri){
        ArticleItem item = new ArticleItem();
        item.setType(ArticleItem.TYPE_IMAGE);
        item.setContentUri(contentUri);
        return item;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        setChanged(true);
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
        setChanged(true);
    }

    public boolean isChanged(){
        return changed;
    }

    public void setChanged(boolean value){
        changed = value;
    }

}
