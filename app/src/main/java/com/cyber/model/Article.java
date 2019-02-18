package com.cyber.model;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Article{

    public enum ItemType{
        TEXT, IMAGE, AUDIO
    }

    public static abstract class Item{
        ItemType type;
        String text;

        public ItemType getType() {
            return type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class TextItem extends Item{
        public TextItem(String text) {
            this.type = ItemType.TEXT;
            this.text = text;
        }
    }


    private final List<Item> items;

    public Article(){
        items = new ArrayList<>();
    }

    public void add(Item item){
        items.add(item);
    }

    public Item get(int index){
        return items.get(index);
    }

    public void remove(int index){
        items.remove(index);
    }

    public int size(){
        return items.size();
    }

}
