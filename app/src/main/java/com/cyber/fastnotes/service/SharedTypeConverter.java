package com.cyber.fastnotes.service;

import android.arch.persistence.room.TypeConverter;
import android.net.Uri;
import android.provider.SyncStateContract;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SharedTypeConverter {

    @TypeConverter
    public String stringFromUri(Uri uri){
        if (uri==null) return "";
        return uri.toString();
    }

    @TypeConverter
    public Uri uriFromString(String str){
        if (str.isEmpty()) return null;
        return Uri.parse(str);
    }

    @TypeConverter
    public long longFromDate(Date date){
        if (date==null) return 0;
        return date.getTime();
    }

    @TypeConverter
    public Date dateFromLong(long timestamp){
        if (timestamp==0) return new Date();
        return new Date(timestamp);
    }

}
