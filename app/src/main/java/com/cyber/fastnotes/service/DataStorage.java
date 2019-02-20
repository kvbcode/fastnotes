package com.cyber.fastnotes.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class DataStorage {
    private static final DataStorage INSTANCE = new DataStorage();



    private DataStorage() {
    }

    public static DataStorage getInstance(){
        return INSTANCE;
    }

    private String createFilename(String prefix, String extension){
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(System.currentTimeMillis());
        sb.append('.');
        sb.append(extension);

        return sb.toString();
    }

    public Uri saveBitmap(Context context, Bitmap img){
        Uri contentUri = null;

        String fileName = createFilename("img_", "jpg");

        try(FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)){
            img.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            contentUri = Uri.withAppendedPath(Uri.fromFile(Environment.getDataDirectory()), fileName);
        }catch(IOException e){
            e.printStackTrace();
        }

        return contentUri;
    }

    public Bitmap loadBitmap(Context context, Uri contentUri){
        Bitmap image = null;

        try {
            image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), contentUri);
        }catch(IOException e){
            e.printStackTrace();
        }

        return image;
    }

}
