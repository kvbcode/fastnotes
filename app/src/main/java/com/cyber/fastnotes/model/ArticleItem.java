package com.cyber.fastnotes.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.net.Uri;
import android.support.annotation.Nullable;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
    foreignKeys = @ForeignKey(entity = Article.class, parentColumns = "id", childColumns = "article_id", onDelete = CASCADE)
)
public class ArticleItem extends BasicModel{
    public static final int TYPE_NONE = 0;
    public static final int TYPE_TEXT = 101;
    public static final int TYPE_IMAGE = 102;
    public static final int TYPE_AUDIO = 103;
    public static final int TYPE_BARCODE = 104;

    public int type;

    @ColumnInfo(name = "article_id", index = true)
    public Long articleId;

    public String text;

    @Ignore
    Object payload;

    public Uri contentUri;

    public ArticleItem() {
        this.type = TYPE_NONE;
        this.payload = null;
        this.contentUri = null;
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

    public static ArticleItem fromAudio(Uri contentUri){
        ArticleItem item = new ArticleItem();
        item.setType(ArticleItem.TYPE_AUDIO);
        item.setContentUri(contentUri);
        return item;
    }

    public static ArticleItem fromBarcode(String text){
        ArticleItem item = new ArticleItem();
        item.setType(ArticleItem.TYPE_BARCODE);
        item.setText(text);
        return item;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (getType()){
            case TYPE_TEXT:
                sb.append("Text(");
                break;
            case TYPE_IMAGE:
                sb.append("Image(");
                break;
            case TYPE_AUDIO:
                sb.append("Audio(");
                break;
            case TYPE_BARCODE:
                sb.append("Barcode(");
                break;
            default:
                sb.append("None(");
                break;
        }
        sb.append("id=").append(getId()).append(", ")
        .append("state=").append(getStateString(getState())).append(", ")
        .append("aid=").append(getArticleId()).append(", ")
        .append("uri='").append(getContentUri()).append("'")
        .append(")");

        return sb.toString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Nullable
    public Long getArticleId() {
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
        setChanged();
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
        setChanged();
    }

}
