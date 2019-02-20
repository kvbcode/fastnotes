package com.cyber.fastnotes;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.cyber.fastnotes.service.AppDataBase;

public class App extends Application {

    private static App instance;

    private AppDataBase dataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        dataBase = Room.databaseBuilder(this, AppDataBase.class, "database")
            .build();
    }

    public static App getInstance(){
        return instance;
    }

    public AppDataBase getDataBase() {
        return dataBase;
    }
}
