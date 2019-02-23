package com.cyber.fastnotes.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.util.Log;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@Dao
public abstract class ArticleDao {
    public static AppDataBase DB = App.getInstance().getDataBase();

    @Query("SELECT * FROM ARTICLE")
    public abstract Flowable<List<Article>> getAll();

    @Query("SELECT * FROM article WHERE id = :id")
    public abstract Maybe<Article> getById(long id);

    @Insert
    public abstract long insert(Article article);

    @Update
    public abstract int update(Article article);

    @Delete
    public abstract int delete(Article article);

    public void eraseDeletedItems(Article article){
        Observable.fromIterable( article.getItems() )
            .filter( ArticleItem::isDeleted )
            .subscribe( item -> {
                Log.v(App.TAG, "Delete item: " + item);
                DB.articleItemDao().delete( item );
            });
    }

    @Transaction
    public long saveFully(Article article){

        Log.v(App.TAG, "saveFully(): " + article);

        if (article.isNew()){
            article.setId( insert(article) );
        }else{
            update(article);
        }
        final long articleId = article.getId();

        eraseDeletedItems(article);

        Observable.fromIterable( article.getItems() )
            .filter( ArticleItem::isChanged )
            .doOnNext( item -> item.setArticleId( articleId ) )
            .toList()
            .doOnSuccess( itemsList -> Log.v(App.TAG, "Article id: " + articleId + ", saved items count (changed): " + itemsList.size()))
            .subscribe( itemList -> DB.articleItemDao().insertAll(itemList));

        return articleId;
    }


    public Maybe<Article> loadFully(long id){

        return Maybe.defer( () -> getById(id) )
            .zipWith( DB.articleItemDao().getByArticleId( id ), (ar, items) -> {
                ar.setItems(items);
                return ar;
            })
            .doOnEvent((ar, e) -> {
                if (ar!=null) Log.v(App.TAG, "loadFully() article id: " + id + " with items: " + ar.size());
            })
        ;
    }
}
