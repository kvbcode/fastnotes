package com.cyber.fastnotes.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.util.Log;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.model.ArticleItem;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public abstract class ArticleItemDao {

    @Query("SELECT * FROM articleitem")
    public abstract Flowable<List<ArticleItem>> getAll();

    @Query("SELECT * FROM articleitem WHERE article_id = :id")
    public abstract Maybe<List<ArticleItem>> getByArticleId(long id);

    @Query("SELECT * FROM articleitem WHERE id = :id")
    public abstract Maybe<ArticleItem> getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(ArticleItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<ArticleItem> itemsList);

    protected Single<Long> eraseDeletedItems(List<ArticleItem> itemList){
        Log.v(App.TAG, "eraseDeletedItems() from: " + itemList.size());

        return Observable.fromIterable( itemList )
            .filter( ArticleItem::isDeleted )
            .doOnNext( item -> delete( item ))
            .count();
    }

    protected Single<Integer> saveChangedItems(Long articleId, List<ArticleItem> itemList){
        Log.v(App.TAG, "saveChangedItems() Article Id: " + articleId  + ", from: " + itemList.size());

        return Observable.fromIterable( itemList )
            .filter( ArticleItem::isChanged )
            .doOnNext( item -> item.setArticleId( articleId ) )
            .toList()
            .doOnSuccess( changedItemsList -> insertAll( changedItemsList ) )
            .map( changedItemsList -> changedItemsList.size() );
    }

    @Update
    public abstract int update(ArticleItem item);

    @Delete
    public abstract int delete(ArticleItem item);
}
