package com.cyber.fastnotes.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;
import android.util.Log;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.model.BasicModel;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.model.ArticleItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
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

    protected Single<Article> insertOrUpdate(Article article){
        return Single.fromCallable( () -> {
            if(article.isNew()){
                article.setId( insert( article ) );
                article.setState( Article.STATE_STORED );
            }else{
                update(article);
            }
            return article;
        });
    }

    public Single<Long> saveFully(Article article){

        Log.v(App.TAG, "saveFully(): " + article);

        return Single.defer( () -> {
                DB.beginTransaction();
                return insertOrUpdate(article);
            })
            .subscribeOn( Schedulers.io() )
            .doOnSuccess( ar -> {
                int changedItemsCount = DB.articleItemDao().saveChangedItems(article.getId(), ar.getItems() ).blockingGet();
                long deletedItemsCount = DB.articleItemDao().eraseDeletedItems( ar.getItems() ).blockingGet();
                Log.d(App.TAG, "save Article id: " + ar.getId() + ", changed: " + changedItemsCount + ", deleted: " + deletedItemsCount);
                DB.setTransactionSuccessful();
            })
            .doAfterTerminate( () -> DB.endTransaction() )
            .map( ar -> ar.getId());
    }


    public Maybe<Article> loadFully(long id){

        return Maybe.defer( () -> getById(id) )
            .zipWith( DB.articleItemDao().getByArticleId( id ), (ar, items) -> {
                ar.setItems(items);
                return ar;
            })
            .subscribeOn(Schedulers.io())
            .doOnSuccess( ar -> Log.v(App.TAG, "loadFully() article id: " + ar.getId() + " with items: " + ar.size()))
            .doOnError( e ->  Log.e(App.TAG, "loadFully() error: " + e.getMessage()) )
        ;
    }
}
