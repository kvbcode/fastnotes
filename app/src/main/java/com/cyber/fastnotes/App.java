package com.cyber.fastnotes;

import android.app.Activity;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.cyber.fastnotes.service.AppDataBase;

import java.util.Arrays;

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

    public static boolean isPermissionsGranted(Context context, @NonNull String[] permissions){
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    Log.w(TAG, "permission is not granted: " + permissions[i]);
                    return false;
                }
            }

        }
        return result;
    }

    public static void requestRuntimePermissions(Activity activity, @NonNull String[] permissions, int requestCode){
        Log.d(TAG, "request runtime permissions: " + Arrays.toString(permissions));
        ActivityCompat.requestPermissions( activity, permissions, requestCode );
    }

    public static boolean isRequestedPermissionsGranted(@NonNull String[] permissions, @NonNull int[] grantResults){
        boolean isPermissionsGranted = true;
        for (int i = 0; i < permissions.length; i++){
            if (grantResults[i]== PackageManager.PERMISSION_DENIED){
                Log.w(TAG, "permission denied: " + permissions[i]);
                isPermissionsGranted = false;
            }
        }
        return isPermissionsGranted;
    }

}
