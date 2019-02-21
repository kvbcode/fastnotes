package com.cyber.fastnotes.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.cyber.fastnotes.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class IOHelper {

    public static final int IMAGE_ERROR = android.R.drawable.ic_menu_report_image;


    public static String createFilename(String prefix, String extension){
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(System.currentTimeMillis());
        sb.append('.');
        sb.append(extension);

        return sb.toString();
    }

    public static Bitmap loadBitmap(Context context, Uri contentUri, boolean useStubOnError){
        Bitmap image = null;

        Log.v(App.TAG, "load bitmap from uri: " + contentUri);

        try {
            image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), contentUri);
        }catch(IOException e){
            if (useStubOnError) image = getImageStub(context);
            Log.e(App.TAG, "IOHelper.loadBitmap(" + contentUri + ") error: " + e.getMessage());
        }

        return image;
    }

    public static Bitmap getImageStub(Context context){
        return BitmapFactory.decodeResource(context.getResources(), IMAGE_ERROR);
    }

    public static Bitmap getThumbnailFor(Context context, Uri contentUri, int width, int height, boolean useStubOnError){

        Bitmap bitmap, thumbBitmap;

        String fnHash = getHash(contentUri.toString());
        String thumbFileName = fnHash + ".jpg";
        //File thumbFile = new File(context.getExternalFilesDir("thumb"), thumbFileName);
        File thumbFile = new File(context.getExternalCacheDir(), thumbFileName);

        // try to load saved thumbnail

        bitmap = loadBitmap(context, Uri.fromFile(thumbFile), false);
        if (bitmap!=null) return bitmap;

        // try to load original image

        Log.v(App.TAG, "no thumbnail found for : " + contentUri + " (" + thumbFile.getName() + ")");
        bitmap = loadBitmap(context, contentUri, false);
        if (bitmap==null) return getImageStub(context);

        // make thumbnail from original image

        thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
        bitmap.recycle();

        Log.v(App.TAG, "save thumbnail for : " + contentUri + " (" + thumbFile.getName() + ")");
        try(FileOutputStream fout = new FileOutputStream(thumbFile)){
            thumbFile.mkdirs();
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
        }catch(IOException e){
            Log.e(App.TAG, "error saving thumbnail: " + thumbFile + ", msg: " + e.getMessage());
        }

        return thumbBitmap;
    }

    public static String getHash(String str) {
        String ret = null;
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] buf = str.getBytes();
            m.update(buf, 0, buf.length);
            ret = new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
