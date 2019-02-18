package com.cyber.fastnotes.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cyber.model.Article;

import static com.cyber.model.Article.ItemType.*;
import static com.cyber.model.Article.TextItem;

public class ArticleView extends LinearLayout{

    Article article;

    public ArticleView(Context context) {
        super(context);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setArticle(Article article) {
        this.article = article;
        notifyDatasetChanged();
    }

    public void notifyDatasetChanged(){
        this.removeAllViews();
        for(int i=0; i<article.size(); i++) {
            this.addView(getItemView(i), i);
        }
    }

    public View getItemView(int index){
        Article.Item item = article.get(index);

        Log.v("CYBER", "getItemView: " + index);

        switch(item.getType()){
            case TEXT:
                return getTextItemView((TextItem)item);
        }

        return getTextItemView((TextItem)item);
    }

    private View getTextItemView(TextItem item) {
        TextView text = new TextView(this.getContext());
        text.setText(item.getText());

        Linkify.addLinks(text,
            Linkify.EMAIL_ADDRESSES |
            Linkify.PHONE_NUMBERS |
            Linkify.WEB_URLS
        );

        return text;
    }




}
