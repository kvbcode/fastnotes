package com.cyber.fastnotes.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.cyber.fastnotes.App;

import java.io.IOException;

public class IOHelper {

    public static final int IMAGE_ERROR = android.R.drawable.ic_menu_report_image;


    public static String createFilename(String prefix, String extension){
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(System.currentTimeMillis());
        sb.append('.');
        sb.append(extension);

        return sb.toString();
    }

    public static Bitmap loadBitmap(Context context, Uri contentUri){
        Bitmap image;

        Log.v(App.TAG, "load bitmap from uri: " + contentUri);

        try {
            image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), contentUri);
        }catch(IOException e){
            image = BitmapFactory.decodeResource(context.getResources(), IMAGE_ERROR);
            Log.e(App.TAG, "IOHelper.loadBitmap(" + contentUri + ") error: " + e.getMessage());
        }

        return image;
    }

}
