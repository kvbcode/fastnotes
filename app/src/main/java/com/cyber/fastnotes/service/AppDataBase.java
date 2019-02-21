package com.cyber.fastnotes.service;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.cyber.fastnotes.dao.ArticleDao;
import com.cyber.fastnotes.dao.ArticleItemDao;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

@Database(entities = {Article.class, ArticleItem.class}, version = 2, exportSchema = false)
@TypeConverters({SharedTypeConverter.class})
public abstract class AppDataBase extends RoomDatabase {

    public abstract ArticleDao articleDao();

    public abstract ArticleItemDao articleItemDao();

}
