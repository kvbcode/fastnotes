package com.cyber.fastnotes;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.cyber.fastnotes.service.AppDataBase;

public class App extends Application {

    public static final String EXTRA_IS_NEW_NAME = "is_new";
    public static final String EXTRA_ID_NAME = "id";
    public static final String TAG = "FASTNOTES";

    private static App instance;

    private AppDataBase dataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        dataBase = Room.databaseBuilder(this, AppDataBase.class, "database")
            .fallbackToDestructiveMigration()       // TODO: dev only
            .build();
    }

    public static App getInstance(){
        return instance;
    }

    public AppDataBase getDataBase() {
        return dataBase;
    }

}
