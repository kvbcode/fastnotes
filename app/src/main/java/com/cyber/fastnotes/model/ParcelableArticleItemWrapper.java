package com.cyber.fastnotes.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableArticleItemWrapper implements Parcelable {
    private static final long NO_ID = Long.MIN_VALUE;

    ArticleItem item;

    public ParcelableArticleItemWrapper(ArticleItem item){
        this.item = item;
    }

    protected ParcelableArticleItemWrapper(Parcel in) {
        this.item = loadFromParcel(in);
    }

    public ArticleItem getArticleItem(){
        return item;
    }

    public static final Creator<ParcelableArticleItemWrapper> CREATOR = new Creator<ParcelableArticleItemWrapper>() {
        @Override
        public ParcelableArticleItemWrapper createFromParcel(Parcel in) {
            return new ParcelableArticleItemWrapper(in);
        }

        @Override
        public ParcelableArticleItemWrapper[] newArray(int size) {
            return new ParcelableArticleItemWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (item.getId() == null){
            dest.writeLong( NO_ID );
        }else {
            dest.writeLong( item.getId() );
        }

        dest.writeInt( item.getType() );
        dest.writeString( item.getText() );
        dest.writeString( item.getContentUri()!=null? item.getContentUri().toString(): "" );
        dest.writeInt( item.getState() );
    }

    public static ArticleItem loadFromParcel(Parcel p){
        ArticleItem item = new ArticleItem();

        long itemId = p.readLong();
        if ( itemId != NO_ID ) item.setId( itemId );

        item.setType( p.readInt() );
        item.setText( p.readString() );

        String uriStr = p.readString();
        if ( !uriStr.isEmpty() ) item.setContentUri( Uri.parse( uriStr ) );

        item.setState( p.readInt() );

        return item;
    }

}
