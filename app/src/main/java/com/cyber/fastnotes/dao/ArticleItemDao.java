package com.cyber.fastnotes.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.cyber.model.ArticleItem;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface ArticleItemDao {

    @Query("SELECT * FROM articleitem")
    Flowable<List<ArticleItem>> getAll();

    @Query("SELECT * FROM articleitem WHERE article_id = :id")
    Flowable<List<ArticleItem>> getByArticleId(long id);

    @Query("SELECT * FROM articleitem WHERE id = :id")
    Maybe<ArticleItem> getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ArticleItem item);

    @Update
    int update(ArticleItem item);

    @Delete
    int delete(ArticleItem item);
}
