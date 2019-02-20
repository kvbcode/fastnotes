package com.cyber.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;
import android.net.Uri;

import com.cyber.fastnotes.service.SharedTypeConverter;

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
    public long id;

    public int type;

    @ColumnInfo(name = "article_id", index = true)
    public long articleId;

    @Ignore
    Object data;

    public Uri contentUri;

    public ArticleItem() {
        this.type = TYPE_NONE;
        this.data = null;
        this.contentUri = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public static class Text extends ArticleItem{
        public Text(String text) {
            super();
            this.type = ArticleItem.TYPE_TEXT;
            this.data = text;
        }

        @Override
        public String getData() {
            return (String)data;
        }
    }

    public static class Image extends ArticleItem{
        public Image(Bitmap bitmap){
            super();
            this.type = ArticleItem.TYPE_IMAGE;
            this.data = bitmap;
        }

        @Override
        public Bitmap getData() {
            return (Bitmap)data;
        }
    }

}
