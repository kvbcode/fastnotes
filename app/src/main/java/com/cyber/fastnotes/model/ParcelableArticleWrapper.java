package com.cyber.fastnotes.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ParcelableArticleWrapper implements Parcelable {
    private static final long NO_ID = Long.MIN_VALUE;

    Article article;

    public ParcelableArticleWrapper(Article article){
        this.article = article;
    }

    protected ParcelableArticleWrapper(Parcel in) {
        this.article = loadFromParcel(in);
    }

    public Article getArticle(){
        return article;
    }

    public static final Creator<ParcelableArticleWrapper> CREATOR = new Creator<ParcelableArticleWrapper>() {
        @Override
        public ParcelableArticleWrapper createFromParcel(Parcel in) {
            return new ParcelableArticleWrapper(in);
        }

        @Override
        public ParcelableArticleWrapper[] newArray(int size) {
            return new ParcelableArticleWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if ( article.getId() == null) {
            dest.writeLong( NO_ID );
        }else{
            dest.writeLong(article.getId());
        }

        dest.writeInt( article.getState() );
        dest.writeString( article.getTitle() );
        dest.writeLong( article.getDate().getTime() );

        dest.writeInt( article.getItems().size() );

        for(ArticleItem item:article.getItems()){
            ParcelableArticleItemWrapper itemWrapper = new ParcelableArticleItemWrapper(item);
            itemWrapper.writeToParcel( dest, flags );
        }
    }

    public static Article loadFromParcel(Parcel p){
        Article ar = new Article();

        long articleId = p.readLong();
        if ( articleId != NO_ID ) ar.setId( articleId );

        ar.setState( p.readInt() );
        ar.setTitle( p.readString() );
        ar.setDate( new Date( p.readLong() ) );

        int size = p.readInt();

        for(int i=0; i<size; i++){
            ArticleItem item = ParcelableArticleItemWrapper.loadFromParcel(p);
            ar.add(item);
        }

        return ar;
    }

}
