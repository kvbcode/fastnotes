package com.cyber.fastnotes.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

@Entity
public abstract class BasicModel{
    public static final int STATE_NEW = 101;
    public static final int STATE_STORED = 102;
    public static final int STATE_CHANGED = 103;
    public static final int STATE_DELETED = 104;

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @Ignore
    private int state = STATE_NEW;

    public BasicModel() {
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        if (id!=null) setState(STATE_STORED);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public static String getStateString(int state){
        switch( state ){
            case STATE_NEW:
                return "NEW";
            case STATE_STORED:
                return "STORED";
            case STATE_CHANGED:
                return "CHANGED";
            case STATE_DELETED:
                return "DELETED";
        }
        return "UNKNOWN";
    }

    public boolean isNew(){
        return (state==STATE_NEW) || (id==null);
    }

    public boolean isStored(){
        return state==STATE_STORED;
    }

    public boolean isChanged(){
        return state==STATE_CHANGED;
    }

    public boolean isDeleted(){
        return state==STATE_DELETED;
    }

    public void setDeleted(){
        state = STATE_DELETED;
    }

    public void setChanged(){
        if (isDeleted()) return;
        state = STATE_CHANGED;
    }

}
