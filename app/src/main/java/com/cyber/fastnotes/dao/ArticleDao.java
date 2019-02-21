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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Article article);

    @Update
    public abstract int update(Article article);

    @Delete
    public abstract int delete(Article article);

    @Transaction
    public long saveFully(Article article){
        long id = insert(article);
        long itemId;

        ArticleItemDao articleItemDao = DB.articleItemDao();

        for (ArticleItem item:article.getItems()) {
            item.setArticleId(id);
            itemId = articleItemDao.insert(item);
            Log.v(App.TAG, "saved changed article item: id: " + itemId);
        }

        return id;
    }


    public Maybe<Article> loadFully(long id){
        return Maybe.defer( () -> getById(id) )
            .doOnEvent((ar, e) -> Log.v(App.TAG, "loadFully.map: " + ar))
            .zipWith( DB.articleItemDao().getByArticleId( id ), (ar, items) -> {
                ar.setItems(items);
                return ar;
            })
            .doOnEvent((ar, e) -> {
                if (ar!=null) Log.v(App.TAG, "loadFully.map with items: " + ar.size());
            })
        ;
    }
}
