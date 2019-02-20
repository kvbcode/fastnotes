package com.cyber.fastnotes.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.cyber.fastnotes.App;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;

@Dao
public abstract class ArticleDao {

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
        ArticleItemDao articleItemDao = App.getInstance().getDataBase().articleItemDao();

        for (ArticleItem item:article.getItems()){
            item.setArticleId(id);
            articleItemDao.insert(item);
        }

        return id;
    }
}
