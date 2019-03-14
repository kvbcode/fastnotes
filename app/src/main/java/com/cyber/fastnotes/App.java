package com.cyber.fastnotes;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.cyber.fastnotes.service.AppDataBase;

public class App extends Application {

    public static final int DEBOUNCE_VALUE = 300;
    public static final String PARAM_LAST_OUTPUT_URI = "output_uri";
    public static final String PARAM_ARTICLE_ITEM = "article_item";
    public static final String PARAM_IS_NEW = "is_new";
    public static final String PARAM_ID = "id";
    public static final String TAG = "FASTNOTES";
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm:ss";

    private static App instance;

    private AppDataBase dataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        RoomDatabase.Builder<AppDataBase> dbBuilder = Room.databaseBuilder(this, AppDataBase.class, "database");
        if (BuildConfig.DEBUG) dbBuilder = dbBuilder.fallbackToDestructiveMigration();
        dataBase = dbBuilder.build();
    }

    public static App getInstance(){
        return instance;
    }

    public AppDataBase getDataBase() {
        return dataBase;
    }

}
